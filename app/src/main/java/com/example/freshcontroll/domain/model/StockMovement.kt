package com.example.freshcontroll.domain.model

/**
 * Clasifica los motivos de alteración de inventario en el negocio.
 */
enum class MovementReason {
    COMPRA,
    MERMA,
    CORRECCION,
    DEVOLUCION
}

/**
 * Representa un cambio en las existencias de un producto para trazabilidad.
 */
data class StockMovement(
    val id: String,
    val storeId: String,
    val productId: String,
    val productName: String,
    val previousQuantity: Double,
    val newQuantity: Double,
    val adjustment: Double,
    val reason: MovementReason,
    val timestamp: Long,
    val userId: String,
    val userName: String
)