package com.example.freshcontroll.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Registra el historial de ajustes o movimientos de inventario de un producto.
 * Permite trazar ingresos, mermas o correcciones de stock.
 */
@Entity(
    tableName = "stock_movements",
    foreignKeys = [
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["id"],
            childColumns = ["storeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("storeId"), Index("productId"), Index("userId")]
)
data class StockMovementEntity(
    @PrimaryKey val id: String,
    val storeId: String,
    val productId: String,
    val productName: String,
    val previousQuantity: Double,
    val newQuantity: Double,
    val adjustment: Double,
    val reason: String, // "COMPRA", "MERMA", "CORRECCION", "DEVOLUCION"
    val timestamp: Long,
    val userId: String,
    val userName: String,
    val isSynced: Boolean = false
)