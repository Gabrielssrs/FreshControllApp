package com.example.freshcontroll.domain.usecase.profile

import com.example.freshcontroll.domain.model.AuditLog
import com.example.freshcontroll.domain.repository.AuditRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Obtiene el detalle reactivo de una acción de auditoría en específico.
 */
class GetAuditDetailUseCase @Inject constructor(
    private val auditRepository: AuditRepository
) {
    operator fun invoke(logId: String): Flow<AuditLog?> {
        return auditRepository.getAuditLogById(logId)
    }
}