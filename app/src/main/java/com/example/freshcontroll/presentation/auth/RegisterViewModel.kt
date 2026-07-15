package com.example.freshcontroll.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcontroll.domain.usecase.auth.RegisterStoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados posibles de la pantalla de registro de tienda y dueño.
 */
sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    object Success : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

/**
 * ViewModel encargado de gestionar la creación de una nueva cuenta y registro del local.
 * Realiza validaciones previas de UI antes de procesar el registro.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerStoreUseCase: RegisterStoreUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onCreateStoreAndOwner(
        storeName: String,
        ownerName: String,
        email: String,
        phone: String,
        address: String,
        pass: String,
        confirmPass: String
    ) {
        // Validación local en el ViewModel antes de consumir recursos o llamar al Dominio
        if (pass != confirmPass) {
            _uiState.value = RegisterUiState.Error("Las contraseñas no coinciden.")
            return
        }

        if (pass.length < 6) {
            _uiState.value = RegisterUiState.Error("La contraseña debe tener al menos 6 caracteres.")
            return
        }

        _uiState.value = RegisterUiState.Loading

        viewModelScope.launch {
            registerStoreUseCase(storeName, ownerName, email, phone, address, pass)
                .onSuccess {
                    _uiState.value = RegisterUiState.Success
                }
                .onFailure { exception ->
                    _uiState.value = RegisterUiState.Error(
                        exception.message ?: "Error al registrar la tienda. Verifica tu conexión e intenta nuevamente."
                    )
                }
        }
    }
}