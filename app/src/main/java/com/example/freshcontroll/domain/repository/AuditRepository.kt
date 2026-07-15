package com.example.freshcontroll.domain.repository

import com.example.freshcontroll.domain.model.AuditLog
import kotlinx.coroutines.flow.Flow

/**
 * Contrato para el registro y consulta de auditorías.
 * Asegura la trazabilidad de operaciones críticas en el sistema.
 */
interface AuditRepository {

    fun getAuditLogs(storeId: String): Flow<List<AuditLog>>

    fun getAuditLogById(id: String): Flow<AuditLog?>

    suspend fun insertAuditLog(log: AuditLog)
}