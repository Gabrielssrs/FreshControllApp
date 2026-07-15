package com.example.freshcontroll.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcontroll.domain.model.Product
import com.example.freshcontroll.domain.repository.AuthRepository
import com.example.freshcontroll.domain.usecase.home.GetNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de gestionar el estado de la pantalla de notificaciones.
 * Mantiene un flujo reactivo actualizado con el inventario crítico.
 */
@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _alertsState = MutableStateFlow<Map<String, List<Product>>>(emptyMap())
    val alertsState: StateFlow<Map<String, List<Product>>> = _alertsState.asStateFlow()

    init {
        loadAlerts()
    }

    fun loadAlerts() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser() ?: return@launch

            getNotificationsUseCase(currentUser.storeId).collect { alertsMap ->
                _alertsState.value = alertsMap
            }
        }
    }
}