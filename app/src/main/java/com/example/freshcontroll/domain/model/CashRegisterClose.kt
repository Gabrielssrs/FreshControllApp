package com.example.freshcontroll.domain.model

/**
 * Representa el proceso y resultado de un cierre de caja (cuadre).
 */
data class CashRegisterClose(
    val id: String,
    val storeId: String,
    val userId: String,
    val timestamp: Long,
    val systemAmount: Double,
    val countedAmount: Double,
    val differenceAmount: Double,
    val isClosed: Boolean
)