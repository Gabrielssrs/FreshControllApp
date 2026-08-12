package com.example.freshcontroll.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcontroll.domain.model.AuditEventType
import com.example.freshcontroll.domain.model.AuditLog
import com.example.freshcontroll.domain.repository.AuthRepository
import com.example.freshcontroll.domain.usecase.profile.GetAuditLogsUseCase
import com.example.freshcontroll.domain.usecase.profile.SyncAuditLogsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de listar de forma reactiva el registro de eventos sensibles (auditoría).
 * Filtra para mostrar específicamente las ventas editadas/anuladas.
 */
@HiltViewModel
class AuditViewModel @Inject constructor(
    private val getAuditLogsUseCase: GetAuditLogsUseCase,
    private val syncAuditLogsUseCase: SyncAuditLogsUseCase,
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

            // ⚡ Sync en paralelo para no bloquear la vista de auditoría ⚡
            viewModelScope.launch(Dispatchers.IO) {
                syncAuditLogsUseCase(currentUser.storeId)
            }

            getAuditLogsUseCase(currentUser.storeId).map { logs ->
                // Filtramos para mostrar solo eventos de tipo VENTA_EDITADA según requerimiento
                logs.filter { it.eventType == AuditEventType.VENTA_EDITADA }
            }.collect { filteredLogs ->
                _auditLogs.value = filteredLogs
            }
        }
    }
}
