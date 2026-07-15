package com.example.freshcontroll.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa a un usuario (Dueño o Empleado) perteneciente a una tienda.
 * Maneja el control de acceso y el rol operativo dentro de la app.
 */
@Entity(
    tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["id"],
            childColumns = ["storeId"],
            onDelete = ForeignKey.CASCADE // Si se elimina la tienda, se eliminan sus usuarios
        )
    ],
    indices = [Index("storeId")]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val storeId: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: String, // "OWNER" o "EMPLOYEE"
    val hasAccess: Boolean,
    val photoUrl: String?,
    val isSynced: Boolean = false
)