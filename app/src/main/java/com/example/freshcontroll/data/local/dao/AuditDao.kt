package com.example.freshcontroll.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.freshcontroll.data.local.entity.AuditLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity)

    @Query("SELECT * FROM audit_logs WHERE storeId = :storeId ORDER BY timestamp DESC")
    fun getAllLogs(storeId: String): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE id = :id")
    fun getLogById(id: String): Flow<AuditLogEntity?>

    // --- Offline-First Methods ---

    @Query("SELECT * FROM audit_logs WHERE storeId = :storeId AND isSynced = 0")
    suspend fun getUnsyncedLogs(storeId: String): List<AuditLogEntity>

    @Query("UPDATE audit_logs SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}