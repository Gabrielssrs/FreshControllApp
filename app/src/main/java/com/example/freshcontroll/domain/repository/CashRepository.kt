package com.example.freshcontroll.domain.repository

import com.example.freshcontroll.domain.model.CashRegisterClose
import kotlinx.coroutines.flow.Flow

/**
 * Contrato para el control del flujo de caja.
 * Gestiona los cuadres diarios y calcula el dinero esperado en el sistema.
 */
interface CashRepository {

    suspend fun calculateCurrentSystemAmount(storeId: String): Double

    suspend fun getLastClose(storeId: String): CashRegisterClose?

    fun getAllCloses(storeId: String): Flow<List<CashRegisterClose>>

    suspend fun registerCashClose(close: CashRegisterClose): Result<Unit>
}