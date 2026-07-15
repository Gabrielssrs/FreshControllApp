package com.example.freshcontroll.data.repository

import com.example.freshcontroll.data.local.dao.ProductDao
import com.example.freshcontroll.data.local.dao.StockMovementDao
import com.example.freshcontroll.data.mapper.toDomain
import com.example.freshcontroll.data.mapper.toDomainList
import com.example.freshcontroll.data.mapper.toEntity
import com.example.freshcontroll.data.remote.FirestoreService
import com.example.freshcontroll.data.remote.OpenFoodFactsApiService
import com.example.freshcontroll.domain.model.Product
import com.example.freshcontroll.domain.model.StockMovement
import com.example.freshcontroll.domain.repository.BarcodeLookupResult
import com.example.freshcontroll.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val stockMovementDao: StockMovementDao,
    private val firestoreService: FirestoreService,
    private val apiService: OpenFoodFactsApiService // Inyección del servicio de red de Open Food Facts
) : ProductRepository {

    override fun getProducts(storeId: String, query: String?): Flow<List<Product>> {
        return productDao.getAllProducts(storeId).map { entities ->
            val domainList = entities.toDomainList()
            if (query.isNullOrBlank()) {
                domainList
            } else {
                domainList.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            (it.barcode?.contains(query) == true) ||
                            it.category.contains(query, ignoreCase = true)
                }
            }
        }
    }

    override fun getProductById(id: String): Flow<Product?> {
        return productDao.getProductById(id).map { it?.toDomain() }
    }

    override suspend fun getProductByBarcode(storeId: String, barcode: String): Product? {
        return productDao.getProductByBarcode(storeId, barcode)?.toDomain()
    }

    // --- IMPLEMENTACIÓN DE LA BÚSQUEDA HÍBRIDA ---
    override suspend fun findByBarcodeLocalOrRemote(
        storeId: String,
        barcode: String
    ): BarcodeLookupResult {
        return try {
            // 1. Intentar buscar en la base de datos local de Room usando el DAO existente
            val localProductEntity = productDao.getProductByBarcode(storeId, barcode)

            if (localProductEntity != null) {
                // Si existe localmente, lo transformamos al dominio y retornamos éxito local
                BarcodeLookupResult.LocalSuccess(localProductEntity.toDomain())
            } else {
                // 2. Si no existe en Room, consultamos la API de Open Food Facts
                val response = apiService.getProductByBarcode(barcode)

                if (response.status == 1 && response.product != null) {
                    val remoteProduct = response.product

                    BarcodeLookupResult.RemoteSuccess(
                        barcode = barcode,
                        prefilledName = remoteProduct.product_name ?: "",
                        prefilledCategory = remoteProduct.categories?.split(",")?.firstOrNull()?.trim() ?: "",
                        prefilledImageUrl = remoteProduct.image_url
                    )
                } else {
                    // No se encontró en ninguna base de datos externa
                    BarcodeLookupResult.NotFound(barcode)
                }
            }
        } catch (e: IOException) {
            // Errores asociados a la conectividad a internet
            BarcodeLookupResult.Error("No se pudo conectar con el servidor externo. Verifica tu internet.")
        } catch (e: Exception) {
            // Cualquier otro error de parseo o base de datos local
            BarcodeLookupResult.Error(e.message ?: "Ocurrió un error inesperado al buscar el código.")
        }
    }

    override suspend fun saveProduct(product: Product): Result<Unit> = runCatching {
        // 1. Guardar en Room primero
        productDao.insertProduct(product.toEntity(isSynced = false))

        // 2. Intentar subir a Firestore
        val productMap = mapOf(
            "storeId" to product.storeId,
            "barcode" to product.barcode,
            "name" to product.name,
            "category" to product.category,
            "sku" to product.sku,
            "currentStock" to product.currentStock,
            "minStock" to product.minStock,
            "unitType" to product.unitType,
            "price" to product.price,
            "expirationDate" to product.expirationDate,
            "imageUrl" to product.imageUrl
        )
        firestoreService.saveDocument("products", product.id, productMap).onSuccess {
            productDao.markAsSynced(product.id)
        }
    }

    override suspend fun deleteProduct(productId: String): Result<Unit> = runCatching {
        // 1. Obtener la entidad de Room para poder pasarla al método @Delete
        val productEntity = productDao.getProductById(productId).firstOrNull()

        // 2. Si existe, borrarla de la base de datos local primero
        if (productEntity != null) {
            productDao.deleteProduct(productEntity)
        }

        // 3. Intentar borrar en Firestore de forma silenciosa (si falla por red, Room ya está actualizado)
        runCatching {
            firestoreService.deleteDocument("products", productId).getOrThrow()
        }
    }

    override suspend fun adjustStock(movement: StockMovement): Result<Unit> = runCatching {
        stockMovementDao.insertMovement(movement.toEntity(isSynced = false))
        productDao.updateStock(movement.productId, movement.newQuantity)

        val movementMap = mapOf(
            "storeId" to movement.storeId,
            "productId" to movement.productId,
            "productName" to movement.productName,
            "previousQuantity" to movement.previousQuantity,
            "newQuantity" to movement.newQuantity,
            "adjustment" to movement.adjustment,
            "reason" to movement.reason.name,
            "timestamp" to movement.timestamp,
            "userId" to movement.userId,
            "userName" to movement.userName
        )

        firestoreService.saveDocument("stock_movements", movement.id, movementMap).onSuccess {
            stockMovementDao.markAsSynced(movement.id)
        }
    }

    override fun getProductMovements(productId: String): Flow<List<StockMovement>> {
        return stockMovementDao.getMovementsByProduct(productId).map { it.toDomainList() }
    }

    override fun getNotifications(storeId: String): Flow<Map<String, List<Product>>> {
        val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
        val threshold = System.currentTimeMillis() + thirtyDaysInMillis

        return combine(
            productDao.getOutOfStockProducts(storeId),
            productDao.getExpiringProducts(storeId, threshold),
            productDao.getLowStockProducts(storeId)
        ) { outOfStock, expiring, lowStock ->
            mapOf(
                "outOfStock" to outOfStock.toDomainList(),
                "expiring" to expiring.toDomainList(),
                "lowStock" to lowStock.toDomainList()
            )
        }
    }
}