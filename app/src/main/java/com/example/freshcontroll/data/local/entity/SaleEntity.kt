package com.example.freshcontroll.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa la cabecera de una venta (boleta/ticket) realizada en el local.
 * Registra el total y el usuario (cajero/dueño) que procesó la transacción.
 */
@Entity(
    tableName = "sales",
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
            onDelete = ForeignKey.NO_ACTION // Si se elimina el usuario, la venta se mantiene por historial
        )
    ],
    indices = [Index("storeId"), Index("userId")]
)
data class SaleEntity(
    @PrimaryKey val id: String,
    val storeId: String,
    val ticketNumber: String,
    val userId: String,
    val userName: String,
    val timestamp: Long,
    val subtotal: Double,
    val taxes: Double,
    val total: Double,
    val isEdited: Boolean,
    val isSynced: Boolean = false
)