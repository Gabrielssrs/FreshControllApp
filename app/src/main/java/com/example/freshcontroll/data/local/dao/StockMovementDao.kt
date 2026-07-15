package com.example.freshcontroll.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.freshcontroll.data.local.entity.StockMovementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockMovementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: StockMovementEntity)

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY timestamp DESC")
    fun getMovementsByProduct(productId: String): Flow<List<StockMovementEntity>>

    // --- Offline-First Methods ---

    @Query("SELECT * FROM stock_movements WHERE storeId = :storeId AND isSynced = 0")
    suspend fun getUnsyncedMovements(storeId: String): List<StockMovementEntity>

    @Query("UPDATE stock_movements SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}