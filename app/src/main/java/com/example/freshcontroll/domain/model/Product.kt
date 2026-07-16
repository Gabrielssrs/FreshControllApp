package com.example.freshcontroll.domain.model

/**
 * Representa un producto físico dentro de la lógica de negocio.
 */
data class Product(
    val id: String,
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
    val imageUrl: String?
) {
    override fun toString(): String {
        return if (barcode.isNullOrBlank()) name else "$name ($barcode)"
    }
}