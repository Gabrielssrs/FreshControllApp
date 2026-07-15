package com.example.freshcontroll.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Servicio remoto genérico para interactuar con Cloud Firestore.
 * Utiliza estructuras de datos genéricas (Map<String, Any?>) para ser reutilizable
 * en todas las colecciones de la arquitectura.
 */
class FirestoreService(
    private val firestore: FirebaseFirestore
) {

    /**
     * Guarda o sobrescribe un documento completo en la colección especificada.
     */
    suspend fun saveDocument(collection: String, documentId: String, data: Map<String, Any?>): Result<Unit> = runCatching {
        firestore.collection(collection).document(documentId).set(data).await()
    }

    /**
     * Obtiene un documento específico por su ID.
     * Inyecta el ID del documento dentro del mapa de datos retornado.
     * @return El mapa de datos del documento, o null si no existe o hay un error.
     */
    suspend fun getDocument(collection: String, documentId: String): Map<String, Any?>? {
        return try {
            val snapshot = firestore.collection(collection).document(documentId).get().await()
            if (snapshot.exists()) {
                val data = snapshot.data?.toMutableMap() ?: mutableMapOf()
                data["id"] = snapshot.id
                data
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Realiza una consulta simple en una colección filtrando por un campo y su valor exacto.
     * Inyecta el ID de cada documento dentro de su respectivo mapa.
     * Útil para traer datos específicos de una tienda (ej. field = "storeId").
     */
    suspend fun getDocumentsByField(collection: String, field: String, value: Any): List<Map<String, Any?>> {
        return try {
            val snapshot = firestore.collection(collection)
                .whereEqualTo(field, value)
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                val data = document.data?.toMutableMap() ?: return@mapNotNull null
                data["id"] = document.id
                data
            }
        } catch (e: Exception) {
            emptyList() // En un fallo de red o permisos, retorna una lista vacía para no romper el flujo offline
        }
    }

    /**
     * Elimina un documento específico de una colección.
     */
    suspend fun deleteDocument(collection: String, documentId: String): Result<Unit> = runCatching {
        firestore.collection(collection).document(documentId).delete().await()
    }
}