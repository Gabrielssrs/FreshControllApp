package com.example.freshcontroll.domain.repository

import com.example.freshcontroll.domain.model.Product
import com.example.freshcontroll.domain.model.StockMovement
import kotlinx.coroutines.flow.Flow

/**
 * Contrato para la gestión del inventario y catálogo de productos.
 * Incluye operaciones de lectura reactiva, modificaciones de stock y notificaciones.
 */
interface ProductRepository {

    fun getProducts(storeId: String, query: String? = null): Flow<List<Product>>

    fun getProductById(id: String): Flow<Product?>

    suspend fun getProductByBarcode(storeId: String, barcode: String): Product?

    suspend fun saveProduct(product: Product): Result<Unit>

    suspend fun deleteProduct(productId: String): Result<Unit>

    suspend fun adjustStock(movement: StockMovement): Result<Unit>

    suspend fun findByBarcodeLocalOrRemote(storeId: String, barcode: String): BarcodeLookupResult

    fun getProductMovements(productId: String): Flow<List<StockMovement>>

    /**
     * Retorna un mapa reactivo con los productos que requieren atención.
     * Llaves esperadas: "outOfStock", "expiring", "lowStock"
     */
    fun getNotifications(storeId: String): Flow<Map<String, List<Product>>>
}