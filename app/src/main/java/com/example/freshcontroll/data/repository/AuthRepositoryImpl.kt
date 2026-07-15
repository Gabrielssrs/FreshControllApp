package com.example.freshcontroll.data.repository

import com.example.freshcontroll.data.local.dao.StoreDao
import com.example.freshcontroll.data.local.dao.UserDao
import com.example.freshcontroll.data.mapper.toDomain
import com.example.freshcontroll.data.mapper.toEntity
import com.example.freshcontroll.data.remote.FirebaseAuthService
import com.example.freshcontroll.data.remote.FirestoreService
import com.example.freshcontroll.domain.model.Store
import com.example.freshcontroll.domain.model.User
import com.example.freshcontroll.domain.model.UserRole
import com.example.freshcontroll.domain.repository.AuthRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authService: FirebaseAuthService,
    private val firestoreService: FirestoreService,
    private val userDao: UserDao,
    private val storeDao: StoreDao
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> = runCatching {
        // 1. Autenticar en Firebase Auth
        val uid = authService.login(email, password).getOrThrow()

        // 2. Buscar perfil en Firestore (es necesario por si el usuario cambió de dispositivo)
        val userMap = firestoreService.getDocument("users", uid)
            ?: throw Exception("Perfil de usuario no encontrado en la base de datos.")

        val user = User(
            id = uid,
            storeId = userMap["storeId"] as String,
            fullName = userMap["fullName"] as String,
            email = userMap["email"] as String,
            phone = userMap["phone"] as String,
            role = runCatching { enumValueOf<UserRole>(userMap["role"] as String) }.getOrDefault(UserRole.EMPLOYEE),
            hasAccess = userMap["hasAccess"] as Boolean,
            photoUrl = userMap["photoUrl"] as? String
        )

        // 3. Guardar en Room como Verdad Local (isSynced = true porque viene directo de Firestore)
        userDao.insertUser(user.toEntity(isSynced = true))

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
        return userDao.getUserById(uid).firstOrNull()?.toDomain()
    }

    override fun logout() {
        authService.logout()
    }
}