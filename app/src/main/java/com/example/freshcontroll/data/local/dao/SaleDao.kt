package com.example.freshcontroll.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.freshcontroll.data.local.entity.SaleDetailEntity
import com.example.freshcontroll.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleDetails(details: List<SaleDetailEntity>)

    @Query("SELECT * FROM sales WHERE storeId = :storeId ORDER BY timestamp DESC")
    fun getAllSales(storeId: String): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE storeId = :storeId AND userId = :userId ORDER BY timestamp DESC")
    fun getSalesByUser(storeId: String, userId: String): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sale_details WHERE saleId = :saleId")
    suspend fun getSaleDetailsBySaleId(saleId: String): List<SaleDetailEntity>

    @Query("SELECT * FROM sales WHERE id = :id")
    fun getSaleById(id: String): Flow<SaleEntity?>

    @Query("UPDATE sales SET isEdited = 1 WHERE id = :id")
    suspend fun markSaleAsEdited(id: String)

    // --- Offline-First Methods ---

    @Query("SELECT * FROM sales WHERE storeId = :storeId AND isSynced = 0")
    suspend fun getUnsyncedSales(storeId: String): List<SaleEntity>

    @Query("UPDATE sales SET isSynced = 1 WHERE id = :id")
    suspend fun markSaleAsSynced(id: String)
}