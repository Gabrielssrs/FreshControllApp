package com.example.freshcontroll.presentation.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcontroll.domain.model.Sale
import com.example.freshcontroll.domain.repository.AuthRepository
import com.example.freshcontroll.domain.usecase.sales.GetSalesHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel exclusivo para el Dueño/Administrador.
 * Obtiene el historial global de ventas de todo el negocio.
 */
@HiltViewModel
class SalesHistoryViewModel @Inject constructor(
    private val getSalesHistoryUseCase: GetSalesHistoryUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _allStoreSales = MutableStateFlow<List<Sale>>(emptyList())
    val allStoreSales: StateFlow<List<Sale>> = _allStoreSales.asStateFlow()

    init {
        fetchAllSales()
    }

    fun fetchAllSales() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser() ?: return@launch

            // Pasamos el rol real del usuario. Por diseño de la UI debería ser OWNER,
            // pero esto asegura que la capa de dominio valide y aplique el filtro correcto.
            getSalesHistoryUseCase(
                storeId = currentUser.storeId,
                userId = currentUser.id,
                role = currentUser.role
            ).collect { salesList ->
                _allStoreSales.value = salesList
            }
        }
    }
}