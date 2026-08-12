package com.example.freshcontroll.domain.usecase.profile

import com.example.freshcontroll.domain.repository.AuditRepository
import javax.inject.Inject

class SyncAuditLogsUseCase @Inject constructor(
    private val auditRepository: AuditRepository
) {
    suspend operator fun invoke(storeId: String): Result<Unit> {
        return auditRepository.syncAuditLogs(storeId)
    }
}