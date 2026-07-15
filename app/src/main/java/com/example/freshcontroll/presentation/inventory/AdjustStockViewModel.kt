package com.example.freshcontroll.presentation.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcontroll.domain.model.MovementReason
import com.example.freshcontroll.domain.model.Product
import com.example.freshcontroll.domain.repository.AuthRepository
import com.example.freshcontroll.domain.usecase.inventory.AdjustStockUseCase
import com.example.freshcontroll.domain.usecase.inventory.GetProductDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de gestionar los ajustes manuales de stock y registrar los motivos de corrección.
 */
@HiltViewModel
class AdjustStockViewModel @Inject constructor(
    private val adjustStockUseCase: AdjustStockUseCase,
    private val getProductDetailUseCase: GetProductDetailUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _currentProduct = MutableStateFlow<Product?>(null)
    val currentProduct: StateFlow<Product?> = _currentProduct.asStateFlow()

    private val _adjustmentSuccess = MutableStateFlow<Boolean?>(null)
    val adjustmentSuccess: StateFlow<Boolean?> = _adjustmentSuccess.asStateFlow()

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            getProductDetailUseCase(productId).collect { product ->
                _currentProduct.value = product
            }
        }
    }

    fun onConfirmStockAdjustment(newQuantity: Double, reason: MovementReason) {
        val product = _currentProduct.value ?: return

        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser() ?: return@launch

            adjustStockUseCase(
                productId = product.id,
                storeId = currentUser.storeId,
                previousQuantity = product.currentStock,
                newQuantity = newQuantity,
                reason = reason,
                userId = currentUser.id,
                userName = currentUser.fullName,
                productName = product.name
            )
                .onSuccess {
                    _adjustmentSuccess.value = true
                }
                .onFailure {
                    _adjustmentSuccess.value = false
                }
        }
    }
}