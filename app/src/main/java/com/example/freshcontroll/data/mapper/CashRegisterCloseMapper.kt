package com.example.freshcontroll.data.mapper

import com.example.freshcontroll.data.local.entity.CashRegisterCloseEntity
import com.example.freshcontroll.domain.model.CashRegisterClose

fun CashRegisterCloseEntity.toDomain(): CashRegisterClose {
    return CashRegisterClose(
        id = this.id,
        storeId = this.storeId,
        userId = this.userId,
        timestamp = this.timestamp,
        systemAmount = this.systemAmount,
        countedAmount = this.countedAmount,
        differenceAmount = this.differenceAmount,
        isClosed = this.isClosed
    )
}

fun CashRegisterClose.toEntity(isSynced: Boolean = false): CashRegisterCloseEntity {
    return CashRegisterCloseEntity(
        id = this.id,
        storeId = this.storeId,
        userId = this.userId,
        timestamp = this.timestamp,
        systemAmount = this.systemAmount,
        countedAmount = this.countedAmount,
        differenceAmount = this.differenceAmount,
        isClosed = this.isClosed,
        isSynced = isSynced
    )
}

fun List<CashRegisterCloseEntity>.toDomainList(): List<CashRegisterClose> = this.map { it.toDomain() }