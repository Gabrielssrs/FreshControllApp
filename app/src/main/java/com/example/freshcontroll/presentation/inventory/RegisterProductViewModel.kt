package com.example.freshcontroll.presentation.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcontroll.domain.model.Product
import com.example.freshcontroll.domain.repository.AuthRepository
import com.example.freshcontroll.domain.usecase.inventory.RegisterProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados posibles al guardar (crear o editar) un producto.
 */
sealed class RegisterProductUiState {
    object Idle : RegisterProductUiState()
    object Loading : RegisterProductUiState()
    object Success : RegisterProductUiState()
    data class Error(val message: String) : RegisterProductUiState()
}

/**
 * ViewModel encargado de la creación y edición de productos, validando las entradas del usuario.
 */
@HiltViewModel
class RegisterProductViewModel @Inject constructor(
    private val registerProductUseCase: RegisterProductUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterProductUiState>(RegisterProductUiState.Idle)
    val uiState: StateFlow<RegisterProductUiState> = _uiState.asStateFlow()

    fun onSaveProduct(
        id: String = "",
        barcode: String?,
        name: String,
        category: String,
        sku: String,
        price: Double,
        currentStock: Double,
        minStock: Double,
        unitType: String,
        expirationDate: Long?,
        imageUrl: String?
    ) {
        if (name.isBlank()) {
            _uiState.value = RegisterProductUiState.Error("El nombre del producto no puede estar vacío.")
            return
        }
        if (price <= 0) {
            _uiState.value = RegisterProductUiState.Error("El precio debe ser mayor a 0.")
            return
        }

        _uiState.value = RegisterProductUiState.Loading

        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                _uiState.value = RegisterProductUiState.Error("No se encontró sesión activa.")
                return@launch
            }

            val product = Product(
                id = id,
                storeId = currentUser.storeId,
                barcode = barcode,
                name = name,
                category = category,
                sku = sku,
                currentStock = currentStock,
                minStock = minStock,
                unitType = unitType,
                price = price,
                expirationDate = expirationDate,
                imageUrl = imageUrl
            )

            registerProductUseCase(product)
                .onSuccess {
                    _uiState.value = RegisterProductUiState.Success
                }
                .onFailure { exception ->
                    _uiState.value = RegisterProductUiState.Error(
                        exception.message ?: "Ocurrió un error al guardar el producto."
                    )
                }
        }
    }
}