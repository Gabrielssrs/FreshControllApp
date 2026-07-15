package com.example.freshcontroll.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcontroll.domain.usecase.auth.RecoverPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de gestionar la solicitud de restablecimiento de contraseña.
 */
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val recoverPasswordUseCase: RecoverPasswordUseCase
) : ViewModel() {

    // Se usa un StateFlow simple ya que solo necesitamos exponer un mensaje de feedback (null = Idle/Loading)
    private val _uiState = MutableStateFlow<String?>(null)
    val uiState: StateFlow<String?> = _uiState.asStateFlow()

    fun onResetPasswordRequested(email: String) {
        if (email.isBlank()) {
            _uiState.value = "Por favor, ingresa un correo electrónico válido."
            return
        }

        viewModelScope.launch {
            recoverPasswordUseCase(email)
                .onSuccess {
                    _uiState.value = "Si el correo está registrado, recibirás un enlace para restablecer tu contraseña."
                }
                .onFailure { exception ->
                    _uiState.value = exception.message ?: "No se pudo enviar el correo de recuperación."
                }
        }
    }

    // Función útil para limpiar el mensaje después de que la UI lo haya mostrado (ej. en un Snackbar)
    fun clearMessage() {
        _uiState.value = null
    }
}