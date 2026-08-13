package com.example.freshcontroll.presentation.sales.model

import com.example.freshcontroll.domain.model.SaleDetail

/**
 * Modelo de UI que envuelve el detalle de venta con información extra
 * para el control de inventario en tiempo real.
 */
data class CartItemUiModel(
    val detail: SaleDetail,
    val availableStock: Double,
    val isStockLimitReached: Boolean = detail.quantity >= availableStock
)
