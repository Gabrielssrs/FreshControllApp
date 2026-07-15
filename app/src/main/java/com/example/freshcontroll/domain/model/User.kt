package com.example.freshcontroll.domain.model

/**
 * Define los roles posibles de un usuario dentro del sistema.
 */
enum class UserRole {
    OWNER,
    EMPLOYEE
}

/**
 * Representa a un usuario del sistema (dueño o cajero).
 */
data class User(
    val id: String,
    val storeId: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val hasAccess: Boolean,
    val photoUrl: String?
)