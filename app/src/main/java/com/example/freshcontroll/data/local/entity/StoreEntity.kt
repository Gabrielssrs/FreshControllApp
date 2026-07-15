package com.example.freshcontroll.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa una tienda o minimarket en la base de datos local.
 * Es la entidad raíz de la cual dependen todos los demás registros.
 */
@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val createdAt: Long,
    val isSynced: Boolean = false
)