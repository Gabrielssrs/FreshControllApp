package com.example.freshcontroll.domain.usecase.home

import com.example.freshcontroll.domain.model.Product
import com.example.freshcontroll.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Obtiene las alertas consolidadas de inventario (stock bajo, productos por vencer y stock nulo)
 * para el panel de notificaciones.
 */
class GetNotificationsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(storeId: String): Flow<Map<String, List<Product>>> {
        return productRepository.getNotifications(storeId)
    }
}