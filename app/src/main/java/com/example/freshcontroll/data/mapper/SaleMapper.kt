package com.example.freshcontroll.data.mapper

import com.example.freshcontroll.data.local.entity.SaleEntity
import com.example.freshcontroll.domain.model.Sale

fun SaleEntity.toDomain(): Sale {
    return Sale(
        id = this.id,
        storeId = this.storeId,
        ticketNumber = this.ticketNumber,
        userId = this.userId,
        userName = this.userName,
        timestamp = this.timestamp,
        subtotal = this.subtotal,
        taxes = this.taxes,
        total = this.total,
        isEdited = this.isEdited
    )
}

fun Sale.toEntity(isSynced: Boolean = false): SaleEntity {
    return SaleEntity(
        id = this.id,
        storeId = this.storeId,
        ticketNumber = this.ticketNumber,
        userId = this.userId,
        userName = this.userName,
        timestamp = this.timestamp,
        subtotal = this.subtotal,
        taxes = this.taxes,
        total = this.total,
        isEdited = this.isEdited,
        isSynced = isSynced
    )
}

fun List<SaleEntity>.toDomainList(): List<Sale> = this.map { it.toDomain() }