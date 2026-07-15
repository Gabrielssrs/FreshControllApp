package com.example.freshcontroll.presentation.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcontroll.domain.model.Product
import com.example.freshcontroll.domain.model.SaleDetail
import com.example.freshcontroll.domain.repository.AuthRepository
import com.example.freshcontroll.domain.usecase.sales.AddProductToCartUseCase
import com.example.freshcontroll.domain.usecase.sales.ProcessSaleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel que gestiona el carrito de compras en memoria, calcula los totales
 * en tiempo real y procesa la transacción final (Checkout).
 */
@HiltViewModel
class NewSaleViewModel @Inject constructor(
    private val addProductToCartUseCase: AddProductToCartUseCase,
    private val processSaleUseCase: ProcessSaleUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _currentCart = MutableStateFlow<List<SaleDetail>>(emptyList())
    val currentCart: StateFlow<List<SaleDetail>> = _currentCart.asStateFlow()

    // Triple(Subtotal, Taxes, Total)
    private val _saleTotals = MutableStateFlow(Triple(0.0, 0.0, 0.0))
    val saleTotals: StateFlow<Triple<Double, Double, Double>> = _saleTotals.asStateFlow()

    private val _saleCompletedEvent = MutableStateFlow<String?>(null)
    val saleCompletedEvent: StateFlow<String?> = _saleCompletedEvent.asStateFlow()

    fun addProductToCart(product: Product, quantity: Double) {
        val updatedCart = addProductToCartUseCase(_currentCart.value, product, quantity)
        _currentCart.value = updatedCart
        recalculateTotals(updatedCart)
    }

    fun removeProductFromCart(productId: String) {
        val updatedCart = _currentCart.value.filterNot { it.productId == productId }
        _currentCart.value = updatedCart
        recalculateTotals(updatedCart)
    }

    private fun recalculateTotals(cart: List<SaleDetail>) {
        val subtotal = cart.sumOf { it.totalPrice }
        val taxes = 0.0 // Por ahora no manejamos impuestos
        val total = subtotal + taxes
        _saleTotals.value = Triple(subtotal, taxes, total)
    }

    fun checkoutCart() {
        if (_currentCart.value.isEmpty()) return

        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser() ?: return@launch

            // Generador simple de ticket (FC- + últimos 4 dígitos del timestamp actual)
            val timestampStr = System.currentTimeMillis().toString()
            val ticketNumber = "FC-${timestampStr.takeLast(4)}"

            // Asumiendo que ProcessSaleUseCase ahora retorna Result<String> con el saleId
            processSaleUseCase(
                storeId = currentUser.storeId,
                userId = currentUser.id,
                userName = currentUser.fullName,
                ticketNumber = ticketNumber,
                cart = _currentCart.value,
                taxRate = 0.0
            ).onSuccess { generatedSaleId ->
                _saleCompletedEvent.value = generatedSaleId
            }.onFailure {
                // Aquí podrías emitir un estado de error para mostrar un Snackbar
            }
        }
    }

    fun clearCompletedEvent() {
        _saleCompletedEvent.value = null
    }
}