package com.example.freshcontroll.domain.usecase.sales

import com.example.freshcontroll.domain.repository.SaleRepository
import javax.inject.Inject

/**
 * Caso de uso para finalizar formalmente una venta y aplicar el descuento de inventario.
 */
class FinalizeSaleUseCase @Inject constructor(
    private val saleRepository: SaleRepository
) {
    suspend operator fun invoke(saleId: String): Result<Unit> {
        return saleRepository.finalizeAndDeductStock(saleId)
    }
}
