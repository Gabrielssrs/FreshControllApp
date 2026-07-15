package com.example.freshcontroll.domain.usecase.sales

import com.example.freshcontroll.domain.model.Sale
import com.example.freshcontroll.domain.model.UserRole
import com.example.freshcontroll.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Obtiene el historial de ventas reaccionando al rol del usuario.
 * Un dueño puede ver las ventas globales del local, un empleado solo las que procesó él.
 */
class GetSalesHistoryUseCase @Inject constructor(
    private val saleRepository: SaleRepository
) {
    operator fun invoke(storeId: String, userId: String?, role: UserRole): Flow<List<Sale>> {
        return if (role == UserRole.OWNER) {
            saleRepository.getSalesHistory(storeId)
        } else {
            saleRepository.getSalesHistoryByUser(storeId, userId ?: "")
        }
    }
}