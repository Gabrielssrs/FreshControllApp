package com.example.freshcontroll.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcontroll.domain.model.UserRole
import com.example.freshcontroll.domain.repository.AuthRepository
import com.example.freshcontroll.domain.usecase.profile.AddEmployeeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel que gestiona la creación de cuentas de empleados,
 * capturando la contraseña temporal generada para mostrarla en la UI.
 */
@HiltViewModel
class AddEmployeeViewModel @Inject constructor(
    private val addEmployeeUseCase: AddEmployeeUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _generatedPassword = MutableStateFlow<String?>(null)
    val generatedPassword: StateFlow<String?> = _generatedPassword.asStateFlow()

    private val _operationCompleted = MutableStateFlow(false)
    val operationCompleted: StateFlow<Boolean> = _operationCompleted.asStateFlow()

    fun createEmployeeAccount(name: String, email: String, phone: String, role: UserRole) {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser() ?: return@launch

            addEmployeeUseCase(
                storeId = currentUser.storeId,
                fullName = name,
                email = email,
                phone = phone,
                role = role
            ).onSuccess { tempPassword ->
                _generatedPassword.value = tempPassword
                _operationCompleted.value = true
            }.onFailure {
                // Manejar posible error exponiendo un UiState si es necesario,
                // o mostrar log en la consola para depuración.
            }
        }
    }

    fun resetState() {
        _generatedPassword.value = null
        _operationCompleted.value = false
    }
}