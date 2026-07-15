package com.example.freshcontroll.domain.usecase.profile

import com.example.freshcontroll.domain.model.CashRegisterClose
import com.example.freshcontroll.domain.repository.CashRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Efectúa el cierre de caja, calculando la posible discrepancia (faltante/sobrante) entre
 * el sistema y el dinero físico contado por el usuario.
 */
class CloseCashRegisterUseCase @Inject constructor(
    private val cashRepository: CashRepository
) {
    suspend operator fun invoke(
        storeId: String,
        userId: String,
        systemAmount: Double,
        countedAmount: Double
    ): Result<Unit> {
        val differenceAmount = countedAmount - systemAmount

        val close = CashRegisterClose(
            id = UUID.randomUUID().toString(),
            storeId = storeId,
            userId = userId,
            timestamp = System.currentTimeMillis(),
            systemAmount = systemAmount,
            countedAmount = countedAmount,
            differenceAmount = differenceAmount,
            isClosed = true
        )

        return cashRepository.registerCashClose(close)
    }
}