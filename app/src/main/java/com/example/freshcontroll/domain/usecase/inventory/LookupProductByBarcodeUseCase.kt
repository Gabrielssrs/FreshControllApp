package com.example.freshcontroll.domain.usecase.inventory

import com.example.freshcontroll.domain.repository.AuthRepository
import com.example.freshcontroll.domain.repository.BarcodeLookupResult
import com.example.freshcontroll.domain.repository.ProductRepository
import javax.inject.Inject

/**
 * Caso de uso encargado de buscar un producto por código de barras.
 * Utiliza una estrategia híbrida: busca en la base de datos local del negocio y,
 * si no se encuentra, consulta la API remota de Open Food Facts para autocompletar.
 */
class LookupProductByBarcodeUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(barcode: String): BarcodeLookupResult {
        if (barcode.isBlank()) {
            return BarcodeLookupResult.Error("El código de barras no puede estar vacío.")
        }

        val currentUser = authRepository.getCurrentUser()
            ?: return BarcodeLookupResult.Error("No se encontró una sesión de usuario activa.")

        return productRepository.findByBarcodeLocalOrRemote(currentUser.storeId, barcode)
    }
}