package com.example.freshcontroll.data.repository

import com.example.freshcontroll.data.local.dao.CashDao
import com.example.freshcontroll.data.local.dao.SaleDao
import com.example.freshcontroll.data.mapper.toDomain
import com.example.freshcontroll.data.mapper.toDomainList
import com.example.freshcontroll.data.mapper.toEntity
import com.example.freshcontroll.data.remote.FirestoreService
import com.example.freshcontroll.domain.model.CashRegisterClose
import com.example.freshcontroll.domain.repository.CashRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

class CashRepositoryImpl @Inject constructor(
    private val cashDao: CashDao,
    private val saleDao: SaleDao,
    private val firestoreService: FirestoreService
) : CashRepository {

    override suspend fun calculateCurrentSystemAmount(storeId: String): Double {
        // Calculamos el inicio del día actual (medianoche)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis

        // Obtenemos de manera puntual las ventas con `first()`
        val allSales = saleDao.getAllSales(storeId).first()

        // Filtramos las de hoy y sumamos
        return allSales
            .filter { it.timestamp >= startOfDay }
            .sumOf { it.total }
    }

    override suspend fun getLastClose(storeId: String): CashRegisterClose? {
        return cashDao.getLastClose(storeId)?.toDomain()
    }

    override fun getAllCloses(storeId: String): Flow<List<CashRegisterClose>> {
        return cashDao.getAllCloses(storeId).map { it.toDomainList() }
    }

    override suspend fun registerCashClose(close: CashRegisterClose): Result<Unit> = runCatching {
        cashDao.insertClose(close.toEntity(isSynced = false))

        val closeMap = mapOf(
            "storeId" to close.storeId,
            "userId" to close.userId,
            "timestamp" to close.timestamp,
            "systemAmount" to close.systemAmount,
            "countedAmount" to close.countedAmount,
            "differenceAmount" to close.differenceAmount,
            "isClosed" to close.isClosed
        )

        firestoreService.saveDocument("cash_register_closes", close.id, closeMap).onSuccess {
            cashDao.markAsSynced(close.id)
        }
    }
}