package com.example.freshcontroll.presentation.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcontroll.domain.model.Sale
import com.example.freshcontroll.domain.model.SaleDetail
import com.example.freshcontroll.domain.usecase.sales.GetSaleReceiptUseCase
import com.example.freshcontroll.domain.usecase.sales.UpdateSaleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditSaleViewModel @Inject constructor(
    private val getSaleReceiptUseCase: GetSaleReceiptUseCase,
    private val updateSaleUseCase: UpdateSaleUseCase
) : ViewModel() {

    private var originalSale: Sale? = null
    private var originalDetails: List<SaleDetail> = emptyList()

    private val _currentCart = MutableStateFlow<List<SaleDetail>>(emptyList())
    val currentCart: StateFlow<List<SaleDetail>> = _currentCart.asStateFlow()

    private val _total = MutableStateFlow(0.0)
    val total: StateFlow<Double> = _total.asStateFlow()

    private val _editSuccessEvent = MutableSharedFlow<String>()
    val editSuccessEvent: SharedFlow<String> = _editSuccessEvent.asSharedFlow()

    fun loadSale(saleId: String) {
        viewModelScope.launch {
            getSaleReceiptUseCase(saleId)?.let { (sale, details) ->
                originalSale = sale
                originalDetails = details
                _currentCart.value = details
                recalculateTotal(details)
            }
        }
    }

    fun updateProductQuantity(productId: String, newQuantity: Double) {
        val updatedCart = _currentCart.value.map {
            if (it.productId == productId) {
                it.copy(
                    quantity = newQuantity,
                    totalPrice = newQuantity * it.unitPrice
                )
            } else it
        }
        _currentCart.value = updatedCart
        recalculateTotal(updatedCart)
    }

    fun removeProductFromCart(productId: String) {
        val updatedCart = _currentCart.value.filterNot { it.productId == productId }
        _currentCart.value = updatedCart
        recalculateTotal(updatedCart)
    }

    private fun recalculateTotal(cart: List<SaleDetail>) {
        _total.value = cart.sumOf { it.totalPrice }
    }

    fun saveChanges() {
        val sale = originalSale ?: return
        if (_currentCart.value.isEmpty()) return

        viewModelScope.launch {
            updateSaleUseCase(sale, originalDetails, _currentCart.value)
                .onSuccess { _editSuccessEvent.emit(sale.id) }
        }
    }
}