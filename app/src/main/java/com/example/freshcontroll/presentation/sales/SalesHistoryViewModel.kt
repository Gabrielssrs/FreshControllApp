package com.example.freshcontroll.presentation.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcontroll.domain.model.Sale
import com.example.freshcontroll.domain.model.UserRole
import com.example.freshcontroll.domain.repository.AuthRepository
import com.example.freshcontroll.domain.usecase.sales.GetSalesHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class TimeFilter { TODAY, WEEK, MONTH }

data class SalesHistoryUiState(
    val sales: List<Sale> = emptyList(),
    val employeeBreakdown: Map<String, Double> = emptyMap(),
    val totalAmount: Double = 0.0,
    val salesCount: Int = 0,
    val selectedFilter: TimeFilter = TimeFilter.TODAY,
    val isOwner: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class SalesHistoryViewModel @Inject constructor(
    private val getSalesHistoryUseCase: GetSalesHistoryUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalesHistoryUiState())
    val uiState: StateFlow<SalesHistoryUiState> = _uiState.asStateFlow()

    private var allSales: List<Sale> = emptyList()

    init {
        loadSalesHistory()
    }

    fun loadSalesHistory() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser() ?: return@launch
            val isOwner = currentUser.role == UserRole.OWNER

            getSalesHistoryUseCase(
                storeId = currentUser.storeId,
                userId = currentUser.id,
                role = currentUser.role
            ).collect { salesList ->
                allSales = salesList
                applyFilter(_uiState.value.selectedFilter, isOwner)
            }
        }
    }

    fun setFilter(filter: TimeFilter) {
        val isOwner = _uiState.value.isOwner
        applyFilter(filter, isOwner)
    }

    private fun applyFilter(filter: TimeFilter, isOwner: Boolean) {
        val startTime = calculateStartTime(filter)
        val filteredSales = allSales.filter { it.timestamp >= startTime }
        
        val total = filteredSales.sumOf { it.total }
        
        val breakdown = if (isOwner) {
            filteredSales.groupBy { it.userName }
                .mapValues { entry -> entry.value.sumOf { it.total } }
        } else {
            emptyMap()
        }

        _uiState.value = _uiState.value.copy(
            sales = filteredSales,
            employeeBreakdown = breakdown,
            totalAmount = total,
            salesCount = filteredSales.size,
            selectedFilter = filter,
            isOwner = isOwner,
            isLoading = false
        )
    }

    private fun calculateStartTime(filter: TimeFilter): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        when (filter) {
            TimeFilter.TODAY -> { /* Ya está en el inicio de hoy */ }
            TimeFilter.WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            }
            TimeFilter.MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
            }
        }
        return calendar.timeInMillis
    }
}
