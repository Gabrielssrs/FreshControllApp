package com.example.freshcontroll.domain.model

/**
 * Tipifica los eventos críticos que requieren supervisión por el dueño.
 */
enum class AuditEventType {
    VENTA_EDITADA,
    AJUSTE_STOCK,
    PRODUCTO_ELIMINADO
}

/**
 * Representa un registro de auditoría antifraude o de control interno.
 */
data class AuditLog(
    val id: String,
    val storeId: String,
    val eventType: AuditEventType,
    val title: String,
    val description: String,
    val timestamp: Long,
    val userId: String,
    val userName: String
)