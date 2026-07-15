package com.example.freshcontroll.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Registra acciones sensibles o críticas dentro del sistema (auditoría).
 * Esencial para el control antifraude por parte del dueño.
 */
@Entity(
    tableName = "audit_logs",
    foreignKeys = [
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["id"],
            childColumns = ["storeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("storeId"), Index("userId")]
)
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val storeId: String,
    val eventType: String, // "VENTA_EDITADA", "AJUSTE_STOCK", "PRODUCTO_ELIMINADO"
    val title: String,
    val description: String,
    val timestamp: Long,
    val userId: String,
    val userName: String,
    val isSynced: Boolean = false
)