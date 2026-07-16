package com.example.freshcontroll.data.repository

import android.net.Uri
import com.example.freshcontroll.domain.repository.StorageRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage
) : StorageRepository {

    override suspend fun uploadProductImage(imageUri: Uri, productId: String): Result<String> = runCatching {
        // Creamos la referencia a la ruta: products/id_del_producto.jpg
        val storageRef = storage.reference.child("products/$productId.jpg")
        
        // Subimos el archivo y esperamos el resultado con .await()
        storageRef.putFile(imageUri).await()
        
        // Obtenemos la URL de descarga y la retornamos
        val downloadUrl = storageRef.downloadUrl.await()
        downloadUrl.toString()
    }
}
