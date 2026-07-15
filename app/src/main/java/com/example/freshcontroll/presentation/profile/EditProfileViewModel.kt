package com.example.freshcontroll.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcontroll.domain.repository.AuthRepository
import com.example.freshcontroll.domain.repository.EmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados posibles al editar el perfil de un usuario.
 */
sealed class EditProfileUiState {
    object Idle : EditProfileUiState()
    object Loading : EditProfileUiState()
    object Success : EditProfileUiState()
    data class Error(val message: String) : EditProfileUiState()
}

/**
 * ViewModel encargado de procesar la actualización de datos personales del usuario.
 */
@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val employeeRepository: EmployeeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditProfileUiState>(EditProfileUiState.Idle)
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    fun saveProfileChanges(fullName: String, email: String, phone: String, photoUrl: String?) {
        if (fullName.isBlank() || email.isBlank()) {
            _uiState.value = EditProfileUiState.Error("El nombre y el correo no pueden estar vacíos.")
            return
        }

        _uiState.value = EditProfileUiState.Loading

        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                _uiState.value = EditProfileUiState.Error("No se encontró sesión activa.")
                return@launch
            }

            employeeRepository.updateProfile(
                userId = currentUser.id,
                fullName = fullName,
                email = email,
                phone = phone,
                photoUrl = photoUrl
            ).onSuccess {
                _uiState.value = EditProfileUiState.Success
            }.onFailure { exception ->
                _uiState.value = EditProfileUiState.Error(
                    exception.message ?: "Error al actualizar el perfil."
                )
            }
        }
    }
}