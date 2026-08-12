package com.example.freshcontroll.data.repository

import android.net.Uri
import com.example.freshcontroll.domain.repository.StorageRepository
import com.example.freshcontroll.util.FreshLogger
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage
) : StorageRepository {

    override suspend fun uploadProductImage(imageUri: Uri, productId: String): Result<String> = runCatching {
        FreshLogger.network("Storage", "UPLOAD", "Product: $productId, Uri: $imageUri")
        
        // Creamos la referencia a la ruta: products/id_del_producto.jpg
        val storageRef = storage.reference.child("products/$productId.jpg")
        
        // Subimos el archivo y esperamos el resultado con .await()
        storageRef.putFile(imageUri).await()
        
        // Obtenemos la URL de descarga y la retornamos
        val downloadUrl = storageRef.downloadUrl.await()
        val urlString = downloadUrl.toString()
        
        FreshLogger.d("Storage", "Upload success: $urlString")
        urlString
    }.onFailure {
        FreshLogger.e("StorageError", "Upload failed for product $productId", it)
    }

    override suspend fun uploadProfileImage(imageUri: Uri, userId: String): Result<String> = runCatching {
        FreshLogger.network("Storage", "UPLOAD_PROFILE", "User: $userId, Uri: $imageUri")
        
        val storageRef = storage.reference.child("profiles/$userId.jpg")
        storageRef.putFile(imageUri).await()
        
        val downloadUrl = storageRef.downloadUrl.await()
        val urlString = downloadUrl.toString()
        
        FreshLogger.d("Storage", "Profile upload success: $urlString")
        urlString
    }.onFailure {
        FreshLogger.e("StorageError", "Profile upload failed for user $userId", it)
    }
}
