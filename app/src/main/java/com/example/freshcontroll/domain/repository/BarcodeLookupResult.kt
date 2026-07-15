package com.example.freshcontroll.domain.repository

import com.example.freshcontroll.domain.model.Product

sealed class BarcodeLookupResult {
    // Caso 1: El producto ya está registrado en tu base de datos local
    data class LocalSuccess(val product: Product) : BarcodeLookupResult()

    // Caso 2: No existe localmente, pero se encontró en la API externa (con datos listos para autocompletar)
    data class RemoteSuccess(
        val barcode: String,
        val prefilledName: String,
        val prefilledCategory: String,
        val prefilledImageUrl: String?
    ) : BarcodeLookupResult()

    // Caso 3: No se encontró en ninguna parte (el usuario deberá crearlo desde cero)
    data class NotFound(val barcode: String) : BarcodeLookupResult()

    // Caso 4: Ocurrió un fallo en la red o base de datos
    data class Error(val message: String) : BarcodeLookupResult()
}