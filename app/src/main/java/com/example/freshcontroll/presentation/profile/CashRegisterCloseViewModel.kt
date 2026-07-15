package com.example.freshcontroll.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcontroll.domain.repository.AuthRepository
import com.example.freshcontroll.domain.usecase.profile.CloseCashRegisterUseCase
import com.example.freshcontroll.domain.usecase.profile.GetSystemCashBalanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para gestionar el cuadre y cierre de caja diario,
 * comparando el efectivo real ingresado contra el monto calculado del sistema.
 */
@HiltViewModel
class CashRegisterCloseViewModel @Inject constructor(
    private val getSystemCashBalanceUseCase: GetSystemCashBalanceUseCase,
    private val closeCashRegisterUseCase: CloseCashRegisterUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _systemBalance = MutableStateFlow(0.0)
    val systemBalance: StateFlow<Double> = _systemBalance.asStateFlow()

    private val _closeSuccess = MutableStateFlow(false)
    val closeSuccess: StateFlow<Boolean> = _closeSuccess.asStateFlow()

    init {
        requestSystemBalance()
    }

    fun requestSystemBalance() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser() ?: return@launch
            _systemBalance.value = getSystemCashBalanceUseCase(currentUser.storeId)
        }
    }

    fun submitRegisterClose(countedAmount: Double) {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser() ?: return@launch

            closeCashRegisterUseCase(
                storeId = currentUser.storeId,
                userId = currentUser.id,
                systemAmount = _systemBalance.value,
                countedAmount = countedAmount
            ).onSuccess {
                _closeSuccess.value = true
            }
        }
    }
}