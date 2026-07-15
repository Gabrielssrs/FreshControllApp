package com.example.freshcontroll.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.freshcontroll.data.local.entity.CashRegisterCloseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CashDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClose(close: CashRegisterCloseEntity)

    @Query("SELECT * FROM cash_register_closes WHERE storeId = :storeId ORDER BY timestamp DESC")
    fun getAllCloses(storeId: String): Flow<List<CashRegisterCloseEntity>>

    @Query("SELECT * FROM cash_register_closes WHERE storeId = :storeId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastClose(storeId: String): CashRegisterCloseEntity?

    // --- Offline-First Methods ---

    @Query("SELECT * FROM cash_register_closes WHERE storeId = :storeId AND isSynced = 0")
    suspend fun getUnsyncedCloses(storeId: String): List<CashRegisterCloseEntity>

    @Query("UPDATE cash_register_closes SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}