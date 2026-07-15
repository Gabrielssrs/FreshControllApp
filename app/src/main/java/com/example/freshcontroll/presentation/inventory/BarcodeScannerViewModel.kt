package com.example.freshcontroll.presentation.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcontroll.domain.usecase.inventory.LookupProductByBarcodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BarcodeScannerViewModel @Inject constructor(
    private val lookupProductByBarcodeUseCase: LookupProductByBarcodeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Idle)
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    fun processBarcode(barcode: String) {
        _uiState.value = ScannerUiState.Loading
        viewModelScope.launch {
            val result = lookupProductByBarcodeUseCase(barcode)
            _uiState.value = ScannerUiState.Success(result)
        }
    }

    fun resetScanner() {
        _uiState.value = ScannerUiState.Idle
    }
}