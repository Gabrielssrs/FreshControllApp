package com.example.freshcontroll.data.repository

import com.example.freshcontroll.data.local.dao.AuditDao
import com.example.freshcontroll.data.mapper.toDomain
import com.example.freshcontroll.data.mapper.toDomainList
import com.example.freshcontroll.data.mapper.toEntity
import com.example.freshcontroll.data.remote.FirestoreService
import com.example.freshcontroll.domain.model.AuditLog
import com.example.freshcontroll.domain.repository.AuditRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuditRepositoryImpl @Inject constructor(
    private val auditDao: AuditDao,
    private val firestoreService: FirestoreService
) : AuditRepository {

    override fun getAuditLogs(storeId: String): Flow<List<AuditLog>> {
        return auditDao.getAllLogs(storeId).map { it.toDomainList() }
    }

    override fun getAuditLogById(id: String): Flow<AuditLog?> {
        return auditDao.getLogById(id).map { it?.toDomain() }
    }

    override suspend fun insertAuditLog(log: AuditLog) {
        auditDao.insertLog(log.toEntity(isSynced = false))

        val logMap = mapOf(
            "storeId" to log.storeId,
            "eventType" to log.eventType.name,
            "title" to log.title,
            "description" to log.description,
            "timestamp" to log.timestamp,
            "userId" to log.userId,
            "userName" to log.userName
        )

        runCatching {
            firestoreService.saveDocument("audit_logs", log.id, logMap).onSuccess {
                auditDao.markAsSynced(log.id)
            }
        } // Atrapamos de manera silenciosa si falla por red
    }
}