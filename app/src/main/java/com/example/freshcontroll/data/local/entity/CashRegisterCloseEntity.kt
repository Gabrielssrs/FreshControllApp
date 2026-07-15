package com.example.freshcontroll.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa un evento de cierre de caja (cuadre de dinero).
 * Compara el dinero registrado en sistema contra el físico declarado por el empleado.
 */
@Entity(
    tableName = "cash_register_closes",
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
data class CashRegisterCloseEntity(
    @PrimaryKey val id: String,
    val storeId: String,
    val userId: String,
    val timestamp: Long,
    val systemAmount: Double,
    val countedAmount: Double,
    val differenceAmount: Double,
    val isClosed: Boolean,
    val isSynced: Boolean = false
)