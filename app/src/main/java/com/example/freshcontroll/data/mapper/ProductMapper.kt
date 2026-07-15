package com.example.freshcontroll.data.mapper

import com.example.freshcontroll.data.local.entity.ProductEntity
import com.example.freshcontroll.domain.model.Product

fun ProductEntity.toDomain(): Product {
    return Product(
        id = this.id,
        storeId = this.storeId,
        barcode = this.barcode,
        name = this.name,
        category = this.category,
        sku = this.sku,
        currentStock = this.currentStock,
        minStock = this.minStock,
        unitType = this.unitType,
        price = this.price,
        expirationDate = this.expirationDate,
        imageUrl = this.imageUrl
    )
}

fun Product.toEntity(isSynced: Boolean = false): ProductEntity {
    return ProductEntity(
        id = this.id,
        storeId = this.storeId,
        barcode = this.barcode,
        name = this.name,
        category = this.category,
        sku = this.sku,
        currentStock = this.currentStock,
        minStock = this.minStock,
        unitType = this.unitType,
        price = this.price,
        expirationDate = this.expirationDate,
        imageUrl = this.imageUrl,
        isSynced = isSynced
    )
}

fun List<ProductEntity>.toDomainList(): List<Product> = this.map { it.toDomain() }