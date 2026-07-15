package com.example.freshcontroll.domain.usecase.profile

import com.example.freshcontroll.domain.model.AuditLog
import com.example.freshcontroll.domain.repository.AuditRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Obtiene el flujo reactivo de acciones de auditoría (acciones sensibles realizadas por empleados).
 */
class GetAuditLogsUseCase @Inject constructor(
    private val auditRepository: AuditRepository
) {
    operator fun invoke(storeId: String): Flow<List<AuditLog>> {
        return auditRepository.getAuditLogs(storeId)
    }
}