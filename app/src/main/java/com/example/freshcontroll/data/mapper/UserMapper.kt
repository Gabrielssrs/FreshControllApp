package com.example.freshcontroll.data.mapper

import com.example.freshcontroll.data.local.entity.UserEntity
import com.example.freshcontroll.domain.model.User
import com.example.freshcontroll.domain.model.UserRole

fun UserEntity.toDomain(): User {
    // Manejo seguro del Enum: Si la base de datos tiene un string corrupto, por seguridad asumimos que es empleado.
    val safeRole = runCatching { enumValueOf<UserRole>(this.role) }.getOrDefault(UserRole.EMPLOYEE)

    return User(
        id = this.id,
        storeId = this.storeId,
        fullName = this.fullName,
        email = this.email,
        phone = this.phone,
        role = safeRole,
        hasAccess = this.hasAccess,
        photoUrl = this.photoUrl
    )
}

fun User.toEntity(isSynced: Boolean = false): UserEntity {
    return UserEntity(
        id = this.id,
        storeId = this.storeId,
        fullName = this.fullName,
        email = this.email,
        phone = this.phone,
        role = this.role.name, // Convertimos el Enum a String
        hasAccess = this.hasAccess,
        photoUrl = this.photoUrl,
        isSynced = isSynced
    )
}

fun List<UserEntity>.toDomainList(): List<User> = this.map { it.toDomain() }