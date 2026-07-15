package com.example.freshcontroll.presentation.inventory

import com.example.freshcontroll.domain.repository.BarcodeLookupResult

sealed class ScannerUiState {
    object Idle : ScannerUiState()
    object Loading : ScannerUiState()
    data class Success(val result: BarcodeLookupResult) : ScannerUiState()
    data class Error(val message: String) : ScannerUiState()
}