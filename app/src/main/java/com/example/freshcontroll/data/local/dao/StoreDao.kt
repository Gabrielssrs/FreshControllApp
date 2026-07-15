package com.example.freshcontroll.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.freshcontroll.data.local.entity.StoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStore(store: StoreEntity)

    @Query("SELECT * FROM stores WHERE id = :id")
    fun getStoreById(id: String): Flow<StoreEntity?>

    @Query("SELECT * FROM stores WHERE isSynced = 0")
    suspend fun getUnsyncedStores(): List<StoreEntity>

    @Query("UPDATE stores SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}