package com.example.freshcontroll.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcontroll.domain.model.AuditLog
import com.example.freshcontroll.domain.repository.AuthRepository
import com.example.freshcontroll.domain.usecase.profile.GetAuditLogsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de listar de forma reactiva el registro de eventos sensibles (auditoría).
 */
@HiltViewModel
class AuditViewModel @Inject constructor(
    private val getAuditLogsUseCase: GetAuditLogsUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _auditLogs = MutableStateFlow<List<AuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    init {
        loadAuditLogs()
    }

    fun loadAuditLogs() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser() ?: return@launch

            getAuditLogsUseCase(currentUser.storeId).collect { logs ->
                _auditLogs.value = logs
            }
        }
    }
}