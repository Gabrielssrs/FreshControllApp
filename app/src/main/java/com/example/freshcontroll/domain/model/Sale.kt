package com.example.freshcontroll.domain.model

/**
 * Representa la cabecera de una venta (ticket) en el dominio.
 */
data class Sale(
    val id: String,
    val storeId: String,
    val ticketNumber: String,
    val userId: String,
    val userName: String,
    val timestamp: Long,
    val subtotal: Double,
    val taxes: Double,
    val total: Double,
    val isEdited: Boolean
)