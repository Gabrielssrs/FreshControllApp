package com.example.freshcontroll.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa un producto físico en el inventario del minimarket.
 * Contiene la información de stock, precio y características del ítem.
 */
@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["id"],
            childColumns = ["storeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("storeId")]
)
data class ProductEntity(
    @PrimaryKey val id: String,
    val storeId: String,
    val barcode: String?,
    val name: String,
    val category: String,
    val sku: String,
    val currentStock: Double,
    val minStock: Double,
    val unitType: String,
    val price: Double,
    val costPrice: Double,
    val expirationDate: Long?,
    val imageUrl: String?,
    val isSynced: Boolean = false
)