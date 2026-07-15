package com.example.freshcontroll.domain.repository

import com.example.freshcontroll.domain.model.Sale
import com.example.freshcontroll.domain.model.SaleDetail
import kotlinx.coroutines.flow.Flow

/**
 * Contrato para el procesamiento y consulta de ventas.
 * Maneja las transacciones (cabecera y detalles) de forma atómica.
 */
interface SaleRepository {

    fun getSalesHistory(storeId: String): Flow<List<Sale>>

    fun getSalesHistoryByUser(storeId: String, userId: String): Flow<List<Sale>>

    suspend fun getSaleWithDetails(saleId: String): Pair<Sale, List<SaleDetail>>?

    suspend fun registerSale(sale: Sale, details: List<SaleDetail>): Result<Unit>

    suspend fun markSaleAsEdited(saleId: String): Result<Unit>
}