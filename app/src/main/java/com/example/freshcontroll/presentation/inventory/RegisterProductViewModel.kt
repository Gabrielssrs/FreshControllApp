package com.example.freshcontroll.presentation.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcontroll.domain.model.Product
import com.example.freshcontroll.domain.repository.AuthRepository
import com.example.freshcontroll.domain.repository.StorageRepository
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
    private val authRepository: AuthRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterProductUiState>(RegisterProductUiState.Idle)
    val uiState: StateFlow<RegisterProductUiState> = _uiState.asStateFlow()

    // Uri local temporal para la imagen seleccionada de la galería
    private val _selectedImageUri = MutableStateFlow<android.net.Uri?>(null)
    val selectedImageUri: StateFlow<android.net.Uri?> = _selectedImageUri.asStateFlow()

    fun setImageUri(uri: android.net.Uri?) {
        _selectedImageUri.value = uri
    }

    fun onSaveProduct(
        id: String = "",
        barcode: String?,
        name: String,
        category: String,
        sku: String,
        price: Double,
        costPrice: Double,
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

            // --- PASO PREVIO: SUBIDA A FIREBASE STORAGE ---
            // Si hay una Uri local seleccionada, aquí es donde debemos subirla
            // para obtener la URL pública (String) de Firebase.
            var finalImageUrl = imageUrl // Por defecto usamos la que ya venía (ej. de Open Food Facts)

            _selectedImageUri.value?.let { localUri ->
                // Generamos un ID temporal si es un producto nuevo para el nombre del archivo
                val imageId = id.ifBlank { java.util.UUID.randomUUID().toString() }
                
                storageRepository.uploadProductImage(localUri, imageId)
                    .onSuccess { downloadUrl ->
                        finalImageUrl = downloadUrl
                    }
                    .onFailure { exception ->
                        _uiState.value = RegisterProductUiState.Error("Error al subir la imagen: ${exception.message}")
                        return@launch
                    }
            }

            val product = Product(
                id = id.ifBlank { java.util.UUID.randomUUID().toString() },
                storeId = currentUser.storeId,
                barcode = barcode,
                name = name,
                category = category,
                sku = sku,
                currentStock = currentStock,
                minStock = minStock,
                unitType = unitType,
                price = price,
                costPrice = costPrice,
                expirationDate = expirationDate,
                imageUrl = finalImageUrl // Guardamos la URL final (remota o previa)
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