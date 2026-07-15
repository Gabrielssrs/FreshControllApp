package com.example.freshcontroll.domain.usecase.sales

import com.example.freshcontroll.domain.model.Sale
import com.example.freshcontroll.domain.model.SaleDetail
import com.example.freshcontroll.domain.repository.SaleRepository
import javax.inject.Inject

/**
 * Recupera de forma asíncrona y puntual el detalle completo (cabecera e ítems) de una boleta.
 */
class GetSaleReceiptUseCase @Inject constructor(
    private val saleRepository: SaleRepository
) {
    suspend operator fun invoke(saleId: String): Pair<Sale, List<SaleDetail>>? {
        return saleRepository.getSaleWithDetails(saleId)
    }
}