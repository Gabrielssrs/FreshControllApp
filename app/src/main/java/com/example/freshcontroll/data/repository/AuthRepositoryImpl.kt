package com.example.freshcontroll.data.repository

import com.example.freshcontroll.data.local.dao.ProductDao
import com.example.freshcontroll.data.local.dao.SaleDao
import com.example.freshcontroll.data.local.dao.StoreDao
import com.example.freshcontroll.data.local.dao.UserDao
import com.example.freshcontroll.data.local.database.FreshControlDatabase
import com.example.freshcontroll.data.local.entity.ProductEntity
import com.example.freshcontroll.data.local.entity.SaleEntity
import com.example.freshcontroll.data.mapper.toDomain
import com.example.freshcontroll.data.mapper.toEntity
import com.example.freshcontroll.data.remote.FirebaseAuthService
import com.example.freshcontroll.data.remote.FirestoreService
import com.example.freshcontroll.domain.model.Store
import com.example.freshcontroll.domain.model.User
import com.example.freshcontroll.domain.model.UserRole
import com.example.freshcontroll.domain.repository.AuthRepository
import com.example.freshcontroll.util.FreshLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authService: FirebaseAuthService,
    private val firestoreService: FirestoreService,
    private val userDao: UserDao,
    private val storeDao: StoreDao,
    private val saleDao: SaleDao,
    private val productDao: ProductDao,
    private val database: FreshControlDatabase
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> = runCatching {
        // 1. Autenticar en Firebase Auth
        val uid = authService.login(email, password).getOrThrow()

        // 2. Buscar perfil en Firestore
        val userMap = firestoreService.getDocument("users", uid)
            ?: throw Exception("Perfil de usuario no encontrado en la base de datos.")

        val storeId = userMap["storeId"] as String

        // 3. Buscar datos de la Tienda
        val storeMap = firestoreService.getDocument("stores", storeId)
            ?: throw Exception("Información de la tienda no encontrada.")

        val user = User(
            id = uid,
            storeId = storeId,
            fullName = userMap["fullName"] as String,
            email = userMap["email"] as String,
            phone = userMap["phone"] as String,
            role = runCatching { enumValueOf<UserRole>(userMap["role"] as String) }.getOrDefault(UserRole.EMPLOYEE),
            hasAccess = userMap["hasAccess"] as? Boolean ?: true,
            photoUrl = userMap["photoUrl"] as? String
        )

        val store = Store(
            id = storeId,
            name = storeMap["name"] as String,
            email = storeMap["email"] as String,
            phone = storeMap["phone"] as String,
            address = storeMap["address"] as String,
            createdAt = (storeMap["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )

        // 4. Guardar en Room (Tienda y Usuario)
        storeDao.insertStore(store.toEntity(isSynced = true))
        userDao.insertUser(user.toEntity(isSynced = true))

        // --- ⚡ SYNC INICIAL DE CORTESÍA ⚡ ---
        FreshLogger.i("Auth", "Starting initial background sync for store: $storeId")
        
        CoroutineScope(Dispatchers.IO).launch {
            // A. Sincronizar Productos
            runCatching {
                val remoteProducts = firestoreService.getDocumentsByField("products", "storeId", storeId)
                val entities = remoteProducts.map { map ->
                    ProductEntity(
                        id = map["id"] as String,
                        storeId = map["storeId"] as String,
                        barcode = map["barcode"] as? String,
                        name = map["name"] as String,
                        category = map["category"] as String,
                        sku = map["sku"] as String,
                        currentStock = (map["currentStock"] as? Number)?.toDouble() ?: 0.0,
                        minStock = (map["minStock"] as? Number)?.toDouble() ?: 0.0,
                        unitType = map["unitType"] as String,
                        price = (map["price"] as? Number)?.toDouble() ?: 0.0,
                        costPrice = (map["costPrice"] as? Number)?.toDouble() ?: 0.0,
                        expirationDate = (map["expirationDate"] as? Number)?.toLong(),
                        imageUrl = map["imageUrl"] as? String,
                        isSynced = true
                    )
                }
                if (entities.isNotEmpty()) productDao.insertProducts(entities)
            }

            // B. Sincronizar Ventas (Cabeceras y Detalles)
            runCatching {
                val remoteSales = firestoreService.getDocumentsByField("sales", "storeId", storeId)
                val salesEntities = remoteSales.map { map ->
                    SaleEntity(
                        id = map["id"] as String,
                        storeId = map["storeId"] as String,
                        ticketNumber = map["ticketNumber"] as String,
                        userId = map["userId"] as String,
                        userName = map["userName"] as String,
                        timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L,
                        subtotal = (map["subtotal"] as? Number)?.toDouble() ?: 0.0,
                        taxes = (map["taxes"] as? Number)?.toDouble() ?: 0.0,
                        total = (map["total"] as? Number)?.toDouble() ?: 0.0,
                        isEdited = map["isEdited"] as? Boolean ?: false,
                        isSynced = true
                    )
                }
                if (salesEntities.isNotEmpty()) {
                    saleDao.insertSales(salesEntities)
                    // Detalles de cada venta
                    salesEntities.forEach { sale ->
                        val detailsMaps = firestoreService.getDocumentsByField("sale_details", "saleId", sale.id)
                        val detailEntities = detailsMaps.map { map ->
                            com.example.freshcontroll.data.local.entity.SaleDetailEntity(
                                id = map["id"] as String,
                                saleId = sale.id,
                                productId = map["productId"] as String,
                                productName = map["productName"] as String,
                                quantity = (map["quantity"] as? Number)?.toDouble() ?: 0.0,
                                unitPrice = (map["unitPrice"] as? Number)?.toDouble() ?: 0.0,
                                totalPrice = (map["totalPrice"] as? Number)?.toDouble() ?: 0.0
                            )
                        }
                        if (detailEntities.isNotEmpty()) saleDao.insertSaleDetails(detailEntities)
                    }
                }
            }
        }

        user
    }

    override suspend fun registerStoreAndOwner(store: Store, user: User, password: String): Result<Unit> = runCatching {
        // 1. Crear en Auth
        val uid = authService.register(user.email, password).getOrThrow()
        val finalUser = user.copy(id = uid)

        // 2. Guardar en Room (Offline-First)
        storeDao.insertStore(store.toEntity(isSynced = false))
        userDao.insertUser(finalUser.toEntity(isSynced = false))

        // 3. Sincronizar Store a Firestore
        val storeMap = mapOf(
            "name" to store.name,
            "email" to store.email,
            "phone" to store.phone,
            "address" to store.address,
            "createdAt" to store.createdAt
        )
        firestoreService.saveDocument("stores", store.id, storeMap).onSuccess {
            storeDao.markAsSynced(store.id)
        }

        // 4. Sincronizar User a Firestore
        val userMap = mapOf(
            "storeId" to finalUser.storeId,
            "fullName" to finalUser.fullName,
            "email" to finalUser.email,
            "phone" to finalUser.phone,
            "role" to finalUser.role.name,
            "hasAccess" to finalUser.hasAccess,
            "photoUrl" to finalUser.photoUrl
        )
        firestoreService.saveDocument("users", finalUser.id, userMap).onSuccess {
            userDao.markAsSynced(finalUser.id)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return authService.sendPasswordResetEmail(email)
    }

    override suspend fun getCurrentUser(): User? {
        val uid = authService.getCurrentUserId() ?: return null
        val localUser = userDao.getUserById(uid).firstOrNull()?.toDomain() ?: return null

        // Auto-curación: Verificar si la tienda existe localmente para evitar errores de Foreign Key
        val storeExists = storeDao.getStoreById(localUser.storeId).firstOrNull() != null
        if (!storeExists) {
            runCatching {
                val storeMap = firestoreService.getDocument("stores", localUser.storeId)
                if (storeMap != null) {
                    val store = Store(
                        id = localUser.storeId,
                        name = storeMap["name"] as String,
                        email = storeMap["email"] as String,
                        phone = storeMap["phone"] as String,
                        address = storeMap["address"] as String,
                        createdAt = (storeMap["createdAt"] as? Long) ?: System.currentTimeMillis()
                    )
                    storeDao.insertStore(store.toEntity(isSynced = true))
                }
            }
        }

        return localUser
    }

    override fun getStore(storeId: String): Flow<Store?> {
        return storeDao.getStoreById(storeId).map { it?.toDomain() }
    }

    override suspend fun syncEmployees(storeId: String): Result<Unit> = runCatching {
        val remoteUsers = firestoreService.getDocumentsByField("users", "storeId", storeId)
        val entities = remoteUsers.map { map ->
            com.example.freshcontroll.data.local.entity.UserEntity(
                id = map["id"] as String,
                storeId = map["storeId"] as String,
                fullName = map["fullName"] as String,
                email = map["email"] as String,
                phone = map["phone"] as String,
                role = map["role"] as String,
                hasAccess = map["hasAccess"] as? Boolean ?: true,
                photoUrl = map["photoUrl"] as? String,
                isSynced = true
            )
        }
        if (entities.isNotEmpty()) {
            userDao.insertUsers(entities)
        }
    }

    override fun logout() {
        authService.logout()
        // Limpiamos Room para asegurar privacidad y limpieza entre sesiones
        CoroutineScope(Dispatchers.IO).launch {
            database.clearAllTables()
        }
    }
}