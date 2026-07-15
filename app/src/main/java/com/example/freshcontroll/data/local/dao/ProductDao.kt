package com.example.freshcontroll.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.freshcontroll.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products WHERE storeId = :storeId")
    fun getAllProducts(storeId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: String): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE storeId = :storeId AND barcode = :barcode")
    suspend fun getProductByBarcode(storeId: String, barcode: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Query("UPDATE products SET currentStock = :newStock WHERE id = :id")
    suspend fun updateStock(id: String, newStock: Double)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("SELECT * FROM products WHERE storeId = :storeId AND currentStock <= minStock AND currentStock > 0")
    fun getLowStockProducts(storeId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE storeId = :storeId AND expirationDate IS NOT NULL AND expirationDate <= :thresholdTimestamp")
    fun getExpiringProducts(storeId: String, thresholdTimestamp: Long): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE storeId = :storeId AND currentStock <= 0")
    fun getOutOfStockProducts(storeId: String): Flow<List<ProductEntity>>

    // --- Offline-First Methods ---

    @Query("SELECT * FROM products WHERE storeId = :storeId AND isSynced = 0")
    suspend fun getUnsyncedProducts(storeId: String): List<ProductEntity>

    @Query("UPDATE products SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}