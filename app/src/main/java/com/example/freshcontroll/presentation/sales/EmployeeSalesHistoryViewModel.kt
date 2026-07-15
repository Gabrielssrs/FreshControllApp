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
import javax.inject.Inject

/**
 * ViewModel exclusivo para el Empleado/Cajero.
 * Filtra el historial de ventas estrictamente al usuario logueado en la sesión.
 */
@HiltViewModel
class EmployeeSalesHistoryViewModel @Inject constructor(
    private val getSalesHistoryUseCase: GetSalesHistoryUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _personalSales = MutableStateFlow<List<Sale>>(emptyList())
    val personalSales: StateFlow<List<Sale>> = _personalSales.asStateFlow()

    init {
        fetchPersonalSales()
    }

    fun fetchPersonalSales() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser() ?: return@launch

            // Forzamos el rol a EMPLOYEE en la llamada al UseCase para garantizar
            // que la capa de dominio aplique el filtro restrictivo por userId,
            // independientemente del rol real, ya que esta vista es "Mis Ventas".
            getSalesHistoryUseCase(
                storeId = currentUser.storeId,
                userId = currentUser.id,
                role = UserRole.EMPLOYEE
            ).collect { salesList ->
                _personalSales.value = salesList
            }
        }
    }
}