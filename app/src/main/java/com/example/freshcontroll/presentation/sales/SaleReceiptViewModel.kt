package com.example.freshcontroll.presentation.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshcontroll.domain.model.Sale
import com.example.freshcontroll.domain.model.SaleDetail
import com.example.freshcontroll.domain.usecase.sales.GetSaleReceiptUseCase
import com.example.freshcontroll.domain.usecase.sales.VoidSaleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel que obtiene los detalles completos (cabecera e ítems) de una venta
 * específica para mostrarla en la pantalla de Boleta.
 */
@HiltViewModel
class SaleReceiptViewModel @Inject constructor(
    private val getSaleReceiptUseCase: GetSaleReceiptUseCase,
    private val voidSaleUseCase: VoidSaleUseCase
) : ViewModel() {

    private val _receiptData = MutableStateFlow<Pair<Sale, List<SaleDetail>>?>(null)
    val receiptData: StateFlow<Pair<Sale, List<SaleDetail>>?> = _receiptData.asStateFlow()

    private val _voidEvent = MutableSharedFlow<Result<Unit>>()
    val voidEvent: SharedFlow<Result<Unit>> = _voidEvent.asSharedFlow()

    fun fetchReceipt(saleId: String) {
        viewModelScope.launch {
            _receiptData.value = getSaleReceiptUseCase(saleId)
        }
    }

    fun voidSale(saleId: String) {
        viewModelScope.launch {
            val result = voidSaleUseCase(saleId)
            _voidEvent.emit(result)
        }
    }
}