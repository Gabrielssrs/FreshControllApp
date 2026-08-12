package com.example.freshcontroll.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcontroll.domain.model.UserRole
import com.example.freshcontroll.domain.repository.AuthRepository
import com.example.freshcontroll.domain.usecase.home.GetNotificationsUseCase
import com.example.freshcontroll.domain.usecase.home.GetStoreUseCase
import com.example.freshcontroll.domain.usecase.sales.GetSalesHistoryUseCase
import com.example.freshcontroll.domain.usecase.sales.SyncSalesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * Estado que representa los datos consolidados del panel principal (Dashboard).
 */
data class DashboardUiState(
    val ventasDelDia: Double = 0.0,
    val transaccionesHoy: Int = 0,
    val hayAlertas: Boolean = false,
    val cantidadAlertas: Int = 0,
    val nombreNegocio: String = "",
    val userRole: UserRole = UserRole.EMPLOYEE,
    val isLoading: Boolean = true
)

/**
 * ViewModel encargado de gestionar el estado del panel principal.
 * Observa de forma reactiva el historial de ventas y las alertas de inventario.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getSalesHistoryUseCase: GetSalesHistoryUseCase,
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val getStoreUseCase: GetStoreUseCase,
    private val syncSalesUseCase: SyncSalesUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()

            if (currentUser == null) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                return@launch
            }

            // ⚡ LANZAMOS SYNC EN PARALELO (NO BLOQUEANTE) ⚡
            viewModelScope.launch(Dispatchers.IO) {
                syncSalesUseCase(currentUser.storeId)
            }

            // Calculamos el inicio del día actual (medianoche)
            val startOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val salesFlow = getSalesHistoryUseCase(
                storeId = currentUser.storeId,
                userId = currentUser.id,
                role = currentUser.role
            )
            val alertsFlow = getNotificationsUseCase(currentUser.storeId)
            val storeFlow = getStoreUseCase(currentUser.storeId)

            // Combinamos los flujos reactivos para emitir un solo estado de UI
            combine(salesFlow, alertsFlow, storeFlow) { sales, alerts, store ->
                val todaySales = sales.filter { it.timestamp >= startOfDay }

                val totalAmount = todaySales.sumOf { it.total }
                val totalTransactions = todaySales.size

                val outOfStockCount = alerts["outOfStock"]?.size ?: 0
                val expiringCount = alerts["expiring"]?.size ?: 0
                val lowStockCount = alerts["lowStock"]?.size ?: 0
                val totalAlerts = outOfStockCount + expiringCount + lowStockCount

                DashboardUiState(
                    ventasDelDia = totalAmount,
                    transaccionesHoy = totalTransactions,
                    hayAlertas = totalAlerts > 0,
                    cantidadAlertas = totalAlerts,
                    nombreNegocio = store?.name ?: "",
                    userRole = currentUser.role,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
}