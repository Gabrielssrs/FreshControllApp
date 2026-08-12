package com.example.freshcontroll.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcontroll.domain.model.User
import com.example.freshcontroll.domain.repository.AuthRepository
import com.example.freshcontroll.domain.repository.StorageRepository
import com.example.freshcontroll.domain.usecase.profile.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de gestionar la vista del perfil del usuario actual,
 * permitiendo actualizar su foto y manejar el cierre de sesión.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val storageRepository: StorageRepository,
    private val updateProfileUseCase: UpdateProfileUseCase
) : ViewModel() {

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        viewModelScope.launch {
            _userProfile.value = authRepository.getCurrentUser()
        }
    }

    fun updateProfilePhoto(uri: android.net.Uri) {
        val user = _userProfile.value ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            
            // 1. Subir a Storage
            storageRepository.uploadProfileImage(uri, user.id)
                .onSuccess { downloadUrl ->
                    // 2. Actualizar perfil con la nueva URL
                    updateProfileUseCase(
                        userId = user.id,
                        fullName = user.fullName,
                        email = user.email,
                        phone = user.phone,
                        photoUrl = downloadUrl
                    ).onSuccess {
                        fetchProfile() // Recargar datos locales para reflejar la foto
                    }
                }
            
            _isLoading.value = false
        }
    }

    fun onLogout() {
        authRepository.logout()
    }
}