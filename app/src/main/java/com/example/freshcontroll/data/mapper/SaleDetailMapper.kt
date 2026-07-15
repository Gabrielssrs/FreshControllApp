package com.example.freshcontroll.data.mapper

import com.example.freshcontroll.data.local.entity.SaleDetailEntity
import com.example.freshcontroll.domain.model.SaleDetail

fun SaleDetailEntity.toDomain(): SaleDetail {
    return SaleDetail(
        id = this.id,
        saleId = this.saleId,
        productId = this.productId,
        productName = this.productName,
        quantity = this.quantity,
        unitPrice = this.unitPrice,
        totalPrice = this.totalPrice
    )
}

// SaleDetail no lleva isSynced, ya que se sincroniza atómicamente junto con su Sale
fun SaleDetail.toEntity(): SaleDetailEntity {
    return SaleDetailEntity(
        id = this.id,
        saleId = this.saleId,
        productId = this.productId,
        productName = this.productName,
        quantity = this.quantity,
        unitPrice = this.unitPrice,
        totalPrice = this.totalPrice
    )
}

fun List<SaleDetailEntity>.toDomainList(): List<SaleDetail> = this.map { it.toDomain() }