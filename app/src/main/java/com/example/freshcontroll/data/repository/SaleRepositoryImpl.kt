package com.example.freshcontroll.data.repository

import com.example.freshcontroll.data.local.dao.SaleDao
import com.example.freshcontroll.data.local.entity.SaleEntity
import com.example.freshcontroll.data.mapper.toDomain
import com.example.freshcontroll.data.mapper.toDomainList
import com.example.freshcontroll.data.mapper.toEntity
import com.example.freshcontroll.data.remote.FirestoreService
import com.example.freshcontroll.domain.model.AuditEventType
import com.example.freshcontroll.domain.model.AuditLog
import com.example.freshcontroll.domain.model.Sale
import com.example.freshcontroll.domain.model.SaleDetail
import com.example.freshcontroll.domain.repository.AuditRepository
import com.example.freshcontroll.domain.repository.AuthRepository
import com.example.freshcontroll.domain.repository.SaleRepository
import com.example.freshcontroll.util.FreshLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class SaleRepositoryImpl @Inject constructor(
    private val saleDao: SaleDao,
    private val firestoreService: FirestoreService,
    private val authRepository: AuthRepository,
    private val auditRepository: AuditRepository
) : SaleRepository {

    override fun getSalesHistory(storeId: String): Flow<List<Sale>> {
        return saleDao.getAllSales(storeId).map { it.toDomainList() }
    }

    override fun getSalesHistoryByUser(storeId: String, userId: String): Flow<List<Sale>> {
        return saleDao.getSalesByUser(storeId, userId).map { it.toDomainList() }
    }

    override suspend fun getSaleWithDetails(saleId: String): Pair<Sale, List<SaleDetail>>? {
        // 1. Intentar local
        val localSale = saleDao.getSaleById(saleId).firstOrNull()?.toDomain()
        val localDetails = saleDao.getSaleDetailsBySaleId(saleId).toDomainList()

        if (localSale != null && localDetails.isNotEmpty()) {
            return Pair(localSale, localDetails)
        }

        // 2. Si no hay local o faltan detalles, buscar en remoto
        return runCatching {
            val saleMap = firestoreService.getDocument("sales", saleId) ?: return null
            val detailsMaps = firestoreService.getDocumentsByField("sale_details", "saleId", saleId)

            val sale = Sale(
                id = saleId,
                storeId = saleMap["storeId"] as String,
                ticketNumber = saleMap["ticketNumber"] as String,
                userId = saleMap["userId"] as String,
                userName = saleMap["userName"] as String,
                timestamp = (saleMap["timestamp"] as? Long) ?: 0L,
                subtotal = (saleMap["subtotal"] as? Number)?.toDouble() ?: 0.0,
                taxes = (saleMap["taxes"] as? Number)?.toDouble() ?: 0.0,
                total = (saleMap["total"] as? Number)?.toDouble() ?: 0.0,
                isEdited = saleMap["isEdited"] as? Boolean ?: false
            )

            val details = detailsMaps.map { map ->
                SaleDetail(
                    id = map["id"] as String,
                    saleId = saleId,
                    productId = map["productId"] as String,
                    productName = map["productName"] as String,
                    quantity = (map["quantity"] as? Number)?.toDouble() ?: 0.0,
                    unitPrice = (map["unitPrice"] as? Number)?.toDouble() ?: 0.0,
                    totalPrice = (map["totalPrice"] as? Number)?.toDouble() ?: 0.0
                )
            }

            // 3. Guardar localmente para futuras consultas
            saleDao.insertSale(sale.toEntity(isSynced = true))
            saleDao.insertSaleDetails(details.map { it.toEntity() })

            Pair(sale, details)
        }.getOrNull()
    }

    override suspend fun registerSale(sale: Sale, details: List<SaleDetail>): Result<Unit> = runCatching {
        // 1. Guardar atómicamente en Room
        saleDao.insertSale(sale.toEntity(isSynced = false))
        saleDao.insertSaleDetails(details.map { it.toEntity() })

        // 2. Sincronizar Cabecera a Firestore
        val saleMap = mapOf(
            "storeId" to sale.storeId,
            "ticketNumber" to sale.ticketNumber,
            "userId" to sale.userId,
            "userName" to sale.userName,
            "timestamp" to sale.timestamp,
            "subtotal" to sale.subtotal,
            "taxes" to sale.taxes,
            "total" to sale.total,
            "isEdited" to sale.isEdited
        )
        firestoreService.saveDocument("sales", sale.id, saleMap).onSuccess {
            saleDao.markSaleAsSynced(sale.id)
        }

        // 3. Sincronizar Detalles a Firestore
        details.forEach { detail ->
            val detailMap = mapOf(
                "saleId" to detail.saleId,
                "productId" to detail.productId,
                "productName" to detail.productName,
                "quantity" to detail.quantity,
                "unitPrice" to detail.unitPrice,
                "totalPrice" to detail.totalPrice
            )
            // Se guardan de manera silenciosa si hay errores temporales
            runCatching { firestoreService.saveDocument("sale_details", detail.id, detailMap) }
        }
    }

    override suspend fun markSaleAsEdited(saleId: String): Result<Unit> = runCatching {
        saleDao.markSaleAsEdited(saleId)
        // La actualización remota de 'isEdited' a true se manejaría aquí
        runCatching { firestoreService.saveDocument("sales", saleId, mapOf("isEdited" to true)) }
    }

    override suspend fun deleteSale(saleId: String): Result<Unit> = runCatching {
        // 0. Obtener datos antes de borrar para Auditoría
        val saleData = getSaleWithDetails(saleId)
        val currentUser = authRepository.getCurrentUser()

        // 1. Borrar en Local
        saleDao.deleteSaleById(saleId)
        saleDao.deleteSaleDetailsBySaleId(saleId)

        // 2. Borrar en Firestore
        runCatching { firestoreService.deleteDocument("sales", saleId) }

        // 3. Registrar en Auditoría
        if (saleData != null && currentUser != null) {
            val (sale, details) = saleData
            val auditLog = AuditLog(
                id = UUID.randomUUID().toString(),
                storeId = sale.storeId,
                eventType = AuditEventType.VENTA_EDITADA,
                title = "Venta Anulada",
                description = "El usuario ${currentUser.fullName} anuló la venta ${sale.ticketNumber} por un total de S/ ${sale.total}",
                timestamp = System.currentTimeMillis(),
                userId = currentUser.id,
                userName = currentUser.fullName,
                beforeState = "Ticket: ${sale.ticketNumber}\nTotal: S/ ${sale.total}\nArtículos: ${details.joinToString { it.productName }}",
                afterState = "ELIMINADO"
            )
            auditRepository.insertAuditLog(auditLog)
        }
    }

    override suspend fun syncSales(storeId: String): Result<Unit> = runCatching {
        FreshLogger.i("Sync", "📡 Fetching sales for store: $storeId")
        
        // 1. Obtener ventas de Firestore
        val remoteSales = firestoreService.getDocumentsByField("sales", "storeId", storeId)
        FreshLogger.d("Sync", "✅ Found ${remoteSales.size} sales in Firestore. Inserting to Local DB...")
        
        // 2. Mapear a entidades (Mapeo de seguridad)
        val entities = remoteSales.map { map ->
            SaleEntity(
                id = map["id"] as String,
                storeId = map["storeId"] as String,
                ticketNumber = map["ticketNumber"] as String,
                userId = map["userId"] as String,
                userName = map["userName"] as String,
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L,
                subtotal = (map["subtotal"] as? Number)?.toDouble() ?: 0.0,
                taxes = (map["taxes"] as? Number)?.toDouble() ?: 0.0,
                total = (map["total"] as? Number)?.toDouble() ?: 0.0,
                isEdited = map["isEdited"] as? Boolean ?: false,
                isSynced = true
            )
        }

        // 3. Insertar en Room
        if (entities.isNotEmpty()) {
            saleDao.insertSales(entities)
        }
        FreshLogger.i("Sync", "🏁 Headers sync completed for $storeId. Details will sync on-demand.")
        Unit
    }.onFailure {
        FreshLogger.e("SyncError", "❌ Sales sync failed", it)
    }
}