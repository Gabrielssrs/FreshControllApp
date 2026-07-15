package com.example.freshcontroll.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcontroll.domain.model.AuditLog
import com.example.freshcontroll.domain.usecase.profile.GetAuditDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel que observa los detalles específicos de un registro de auditoría.
 */
@HiltViewModel
class AuditDetailViewModel @Inject constructor(
    private val getAuditDetailUseCase: GetAuditDetailUseCase
) : ViewModel() {

    private val _logDetail = MutableStateFlow<AuditLog?>(null)
    val logDetail: StateFlow<AuditLog?> = _logDetail.asStateFlow()

    fun loadLogDetails(id: String) {
        viewModelScope.launch {
            getAuditDetailUseCase(id).collect { detail ->
                _logDetail.value = detail
            }
        }
    }
}