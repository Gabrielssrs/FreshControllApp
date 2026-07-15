package com.example.freshcontroll.data.mapper

import com.example.freshcontroll.data.local.entity.StoreEntity
import com.example.freshcontroll.domain.model.Store

fun StoreEntity.toDomain(): Store {
    return Store(
        id = this.id,
        name = this.name,
        email = this.email,
        phone = this.phone,
        address = this.address,
        createdAt = this.createdAt
    )
}

fun Store.toEntity(isSynced: Boolean = false): StoreEntity {
    return StoreEntity(
        id = this.id,
        name = this.name,
        email = this.email,
        phone = this.phone,
        address = this.address,
        createdAt = this.createdAt,
        isSynced = isSynced
    )
}

fun List<StoreEntity>.toDomainList(): List<Store> = this.map { it.toDomain() }