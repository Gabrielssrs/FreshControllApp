package com.example.freshcontroll.domain.usecase.sales

import com.example.freshcontroll.domain.model.Sale
import com.example.freshcontroll.domain.model.SaleDetail
import com.example.freshcontroll.domain.repository.SaleRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Procesa la venta actual calculando totales e impuestos, construyendo los objetos finales
 * y vinculando los detalles con el ID de la boleta antes de delegar la persistencia.
 */
class ProcessSaleUseCase @Inject constructor(
    private val saleRepository: SaleRepository
) {
    suspend operator fun invoke(
        storeId: String,
        userId: String,
        userName: String,
        ticketNumber: String,
        cart: List<SaleDetail>,
        taxRate: Double = 0.0
    ): Result<String> {
        if (cart.isEmpty()) return Result.failure(IllegalArgumentException("El carrito está vacío"))

        val saleId = UUID.randomUUID().toString()
        val subtotal = cart.sumOf { it.totalPrice }
        val taxes = subtotal * taxRate
        val total = subtotal + taxes

        val sale = Sale(
            id = saleId,
            storeId = storeId,
            ticketNumber = ticketNumber,
            userId = userId,
            userName = userName,
            timestamp = System.currentTimeMillis(),
            subtotal = subtotal,
            taxes = taxes,
            total = total,
            isEdited = false
        )

        // Asignamos el ID de la cabecera recién generado a todos los ítems del detalle
        val finalizedCart = cart.map { it.copy(saleId = saleId) }

        return saleRepository.registerSale(sale, finalizedCart)
            .map { saleId } // Transforma Result<Unit> en Result<String> con el ID generado
    }
}