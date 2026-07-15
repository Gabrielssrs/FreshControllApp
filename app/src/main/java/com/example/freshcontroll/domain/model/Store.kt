package com.example.freshcontroll.domain.model

/**
 * Representa una tienda o minimarket en el dominio.
 * Entidad principal que agrupa todo el inventario, ventas y empleados.
 */
data class Store(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val createdAt: Long
)