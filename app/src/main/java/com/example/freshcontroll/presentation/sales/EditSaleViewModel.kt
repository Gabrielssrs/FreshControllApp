package com.example.freshcontroll.presentation.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcontroll.domain.model.Product
import com.example.freshcontroll.domain.model.Sale
import com.example.freshcontroll.domain.model.SaleDetail
import com.example.freshcontroll.domain.repository.AuthRepository
import com.example.freshcontroll.domain.usecase.inventory.GetProductsUseCase
import com.example.freshcontroll.domain.usecase.sales.AddProductToCartUseCase
import com.example.freshcontroll.domain.usecase.sales.GetSaleReceiptUseCase
import com.example.freshcontroll.domain.usecase.sales.UpdateSaleUseCase
import com.example.freshcontroll.presentation.sales.model.CartItemUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditSaleViewModel @Inject constructor(
    private val getSaleReceiptUseCase: GetSaleReceiptUseCase,
    private val updateSaleUseCase: UpdateSaleUseCase,
    private val getProductsUseCase: GetProductsUseCase,
    private val addProductToCartUseCase: AddProductToCartUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private var originalSale: Sale? = null
    private var originalDetails: List<SaleDetail> = emptyList()

    private val _currentCart = MutableStateFlow<List<SaleDetail>>(emptyList())
    val currentCart: StateFlow<List<SaleDetail>> = _currentCart.asStateFlow()

    private val _availableProducts = MutableStateFlow<List<Product>>(emptyList())
    val availableProducts: StateFlow<List<Product>> = _availableProducts.asStateFlow()

    // Exponemos modelos UI con validación de stock para el adapter
    val cartUiModels: StateFlow<List<CartItemUiModel>> = combine(_currentCart, _availableProducts) { cart, products ->
        cart.map { detail ->
            val product = products.find { it.id == detail.productId }
            CartItemUiModel(detail, product?.currentStock ?: 0.0)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _total = MutableStateFlow(0.0)
    val total: StateFlow<Double> = _total.asStateFlow()

    private val _editSuccessEvent = MutableSharedFlow<String>()
    val editSuccessEvent: SharedFlow<String> = _editSuccessEvent.asSharedFlow()

    private val _errorEvent = MutableStateFlow<String?>(null)
    val errorEvent: StateFlow<String?> = _errorEvent.asStateFlow()

    init {
        loadAvailableProducts()
    }

    private fun loadAvailableProducts() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser() ?: return@launch
            getProductsUseCase(currentUser.storeId).collect { products ->
                _availableProducts.value = products.filter { it.currentStock > 0 }
            }
        }
    }

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

    fun addProductToCart(product: Product, quantity: Double) {
        if (product.currentStock <= 0) {
            _errorEvent.value = "El producto ${product.name} está agotado."
            return
        }

        val currentItem = _currentCart.value.find { it.productId == product.id }
        val totalRequested = (currentItem?.quantity ?: 0.0) + quantity

        if (totalRequested > product.currentStock) {
            _errorEvent.value = "Solo quedan ${product.currentStock} unidades de ${product.name}."
            return
        }

        val updatedCart = addProductToCartUseCase(_currentCart.value, product, quantity)
        _currentCart.value = updatedCart
        recalculateTotal(updatedCart)
    }

    fun addProductToCartByBarcode(barcode: String) {
        viewModelScope.launch {
            val product = _availableProducts.value.find { it.barcode == barcode }
            if (product != null) {
                addProductToCart(product, 1.0)
            } else {
                _errorEvent.value = "Producto no encontrado o agotado."
            }
        }
    }

    fun updateProductQuantity(productId: String, newQuantity: Double) {
        val product = _availableProducts.value.find { it.id == productId }
        if (product != null && newQuantity > product.currentStock) {
            _errorEvent.value = "No hay suficiente stock."
            return
        }

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
            // Validación final de stock
            for (item in _currentCart.value) {
                val product = _availableProducts.value.find { it.id == item.productId }
                if (product == null || item.quantity > product.currentStock) {
                    _errorEvent.value = "Stock insuficiente para ${item.productName}."
                    return@launch
                }
            }

            updateSaleUseCase(sale, originalDetails, _currentCart.value)
                .onSuccess { _editSuccessEvent.emit(sale.id) }
        }
    }

    fun clearErrorEvent() {
        _errorEvent.value = null
    }
}
