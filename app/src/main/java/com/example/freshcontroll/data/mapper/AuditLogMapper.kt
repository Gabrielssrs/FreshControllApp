package com.example.freshcontroll.data.mapper

import com.example.freshcontroll.data.local.entity.AuditLogEntity
import com.example.freshcontroll.domain.model.AuditEventType
import com.example.freshcontroll.domain.model.AuditLog

fun AuditLogEntity.toDomain(): AuditLog {
    val safeEventType = runCatching { enumValueOf<AuditEventType>(this.eventType) }.getOrDefault(AuditEventType.VENTA_EDITADA)

    return AuditLog(
        id = this.id,
        storeId = this.storeId,
        eventType = safeEventType,
        title = this.title,
        description = this.description,
        timestamp = this.timestamp,
        userId = this.userId,
        userName = this.userName
    )
}

fun AuditLog.toEntity(isSynced: Boolean = false): AuditLogEntity {
    return AuditLogEntity(
        id = this.id,
        storeId = this.storeId,
        eventType = this.eventType.name,
        title = this.title,
        description = this.description,
        timestamp = this.timestamp,
        userId = this.userId,
        userName = this.userName,
        isSynced = isSynced
    )
}

fun List<AuditLogEntity>.toDomainList(): List<AuditLog> = this.map { it.toDomain() }