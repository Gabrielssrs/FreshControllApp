package com.example.freshcontroll.data.repository

import com.example.freshcontroll.data.local.dao.SaleDao
import com.example.freshcontroll.data.mapper.toDomain
import com.example.freshcontroll.data.mapper.toDomainList
import com.example.freshcontroll.data.mapper.toEntity
import com.example.freshcontroll.data.remote.FirestoreService
import com.example.freshcontroll.domain.model.Sale
import com.example.freshcontroll.domain.model.SaleDetail
import com.example.freshcontroll.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SaleRepositoryImpl @Inject constructor(
    private val saleDao: SaleDao,
    private val firestoreService: FirestoreService
) : SaleRepository {

    override fun getSalesHistory(storeId: String): Flow<List<Sale>> {
        return saleDao.getAllSales(storeId).map { it.toDomainList() }
    }

    override fun getSalesHistoryByUser(storeId: String, userId: String): Flow<List<Sale>> {
        return saleDao.getSalesByUser(storeId, userId).map { it.toDomainList() }
    }

    override suspend fun getSaleWithDetails(saleId: String): Pair<Sale, List<SaleDetail>>? {
        // Usamos firstOrNull() (o first()) para tomar la emisión actual y liberar la corrutina
        val sale = saleDao.getSaleById(saleId).firstOrNull()?.toDomain() ?: return null
        val details = saleDao.getSaleDetailsBySaleId(saleId).toDomainList()
        return Pair(sale, details)
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
}