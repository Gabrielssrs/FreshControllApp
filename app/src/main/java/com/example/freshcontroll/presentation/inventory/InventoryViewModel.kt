package com.example.freshcontroll.presentation.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcontroll.domain.model.Product
import com.example.freshcontroll.domain.repository.AuthRepository
import com.example.freshcontroll.domain.usecase.inventory.GetProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de listar el catálogo de productos y manejar su estado de carga o vacío.
 */
@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _isEmpty = MutableStateFlow(false)
    val isEmpty: StateFlow<Boolean> = _isEmpty.asStateFlow()

    private var isLoading = true
    private var searchJob: Job? = null

    init {
        loadInventory()
    }

    fun loadInventory() {
        searchProduct(null)
    }

    fun searchProduct(query: String?) {
        searchJob?.cancel() // Cancelamos la escucha anterior para no cruzar flujos

        searchJob = viewModelScope.launch {
            isLoading = true
            val currentUser = authRepository.getCurrentUser()

            if (currentUser == null) {
                isLoading = false
                _isEmpty.value = true
                return@launch
            }

            getProductsUseCase(currentUser.storeId, query).collect { productList ->
                _products.value = productList
                isLoading = false
                _isEmpty.value = productList.isEmpty()
            }
        }
    }
}