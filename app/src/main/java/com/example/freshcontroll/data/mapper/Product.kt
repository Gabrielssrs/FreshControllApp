package com.example.freshcontroll.data.mapper

import com.example.freshcontroll.data.remote.OFFResponseDto
import com.example.freshcontroll.domain.model.Product
import java.util.UUID

/**
 * Convierte la respuesta de Open Food Facts en un Product de dominio.
 * Devuelve null si la API no encontró el producto (status = 0).
 */
fun OFFResponseDto.toDomainProduct(): Product? {
    if (status != 1 || product == null) return null

    return Product(
        id = UUID.randomUUID().toString(), // Generamos un ID de tipo String único
        storeId = "", // Valor por defecto vacío ya que se asociará en el formulario/caso de uso
        barcode = code, // Usamos el código de barras retornado por la API
        name = product.product_name?.takeIf { it.isNotBlank() } ?: "Producto sin nombre",
        category = product.categories?.substringBefore(",")?.trim() ?: "",
        sku = "", // Valor por defecto
        currentStock = 0.0, // Valor por defecto
        minStock = 0.0, // Valor por defecto
        unitType = "", // Valor por defecto
        price = 0.0, // Valor por defecto
        expirationDate = null, // Sin fecha de expiración inicial
        imageUrl = product.image_url
    )
}