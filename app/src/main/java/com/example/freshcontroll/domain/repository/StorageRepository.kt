package com.example.freshcontroll.domain.repository

import android.net.Uri

/**
 * Interfaz de la capa de Dominio para la gestión de archivos multimedia.
 */
interface StorageRepository {
    /**
     * Sube una imagen de producto a Firebase Storage y retorna la URL pública.
     * @param imageUri Uri local de la imagen.
     * @param productId Identificador único para el nombre del archivo.
     * @return Result con la URL de descarga (String) o una excepción.
     */
    suspend fun uploadProductImage(imageUri: Uri, productId: String): Result<String>
}
