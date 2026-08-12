package com.example.freshcontroll.domain.usecase.sales

import com.example.freshcontroll.domain.model.AuditEventType
import com.example.freshcontroll.domain.model.AuditLog
import com.example.freshcontroll.domain.model.Sale
import com.example.freshcontroll.domain.model.SaleDetail
import com.example.freshcontroll.domain.repository.AuditRepository
import com.example.freshcontroll.domain.repository.AuthRepository
import com.example.freshcontroll.domain.repository.SaleRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Caso de uso para editar una venta existente.
 * Recalcula totales, actualiza la persistencia y registra en Auditoría.
 */
class UpdateSaleUseCase @Inject constructor(
    private val saleRepository: SaleRepository,
    private val authRepository: AuthRepository,
    private val auditRepository: AuditRepository
) {
    suspend operator fun invoke(
        originalSale: Sale,
        originalDetails: List<SaleDetail>,
        updatedDetails: List<SaleDetail>
    ): Result<Unit> = runCatching {
        val currentUser = authRepository.getCurrentUser() ?: throw Exception("Sesión no iniciada")

        // 1. Recalcular total de la venta editada
        val newSubtotal = updatedDetails.sumOf { it.totalPrice }
        val newTotal = newSubtotal // Asumiendo 0 impuestos por ahora

        val updatedSale = originalSale.copy(
            subtotal = newSubtotal,
            total = newTotal,
            isEdited = true
        )

        // 2. Persistir cambios (Primero borramos y luego insertamos para simplificar la lógica de detalles)
        // Nota: En una app real se haría un update granular, aquí reemplazamos por simplicidad.
        saleRepository.deleteSale(originalSale.id) 
        saleRepository.registerSale(updatedSale, updatedDetails)

        // 3. Registrar en Auditoría
        val beforeDesc = originalDetails.joinToString { "${it.productName}(x${it.quantity})" }
        val afterDesc = updatedDetails.joinToString { "${it.productName}(x${it.quantity})" }

        val auditLog = AuditLog(
            id = UUID.randomUUID().toString(),
            storeId = originalSale.storeId,
            eventType = AuditEventType.VENTA_EDITADA,
            title = "Venta Editada",
            description = "El usuario ${currentUser.fullName} editó la venta ${originalSale.ticketNumber}. Total original: S/ ${originalSale.total} -> Nuevo total: S/ $newTotal",
            timestamp = System.currentTimeMillis(),
            userId = currentUser.id,
            userName = currentUser.fullName,
            beforeState = "Detalles: $beforeDesc\nTotal: S/ ${originalSale.total}",
            afterState = "Detalles: $afterDesc\nTotal: S/ $newTotal"
        )
        auditRepository.insertAuditLog(auditLog)
    }
}