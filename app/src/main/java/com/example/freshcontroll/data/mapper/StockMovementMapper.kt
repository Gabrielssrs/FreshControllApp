package com.example.freshcontroll.data.mapper

import com.example.freshcontroll.data.local.entity.StockMovementEntity
import com.example.freshcontroll.domain.model.MovementReason
import com.example.freshcontroll.domain.model.StockMovement

fun StockMovementEntity.toDomain(): StockMovement {
    val safeReason = runCatching { enumValueOf<MovementReason>(this.reason) }.getOrDefault(MovementReason.CORRECCION)

    return StockMovement(
        id = this.id,
        storeId = this.storeId,
        productId = this.productId,
        productName = this.productName,
        previousQuantity = this.previousQuantity,
        newQuantity = this.newQuantity,
        adjustment = this.adjustment,
        reason = safeReason,
        timestamp = this.timestamp,
        userId = this.userId,
        userName = this.userName
    )
}

fun StockMovement.toEntity(isSynced: Boolean = false): StockMovementEntity {
    return StockMovementEntity(
        id = this.id,
        storeId = this.storeId,
        productId = this.productId,
        productName = this.productName,
        previousQuantity = this.previousQuantity,
        newQuantity = this.newQuantity,
        adjustment = this.adjustment,
        reason = this.reason.name,
        timestamp = this.timestamp,
        userId = this.userId,
        userName = this.userName,
        isSynced = isSynced
    )
}

fun List<StockMovementEntity>.toDomainList(): List<StockMovement> = this.map { it.toDomain() }