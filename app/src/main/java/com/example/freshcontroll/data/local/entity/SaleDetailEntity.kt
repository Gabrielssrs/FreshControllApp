package com.example.freshcontroll.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa cada línea de producto (ítem) dentro de una venta específica.
 * Tiene una dependencia fuerte con SaleEntity.
 */
@Entity(
    tableName = "sale_details",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE // Dependencia fuerte: Si se anula/borra la venta, se borran sus detalles
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("saleId"), Index("productId")]
)
data class SaleDetailEntity(
    @PrimaryKey val id: String,
    val saleId: String,
    val productId: String,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val totalPrice: Double
)