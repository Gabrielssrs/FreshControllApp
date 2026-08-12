package com.example.freshcontroll.data.remote

import com.example.freshcontroll.util.FreshLogger
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
        FreshLogger.network("Firestore", "SAVE", "Collection: $collection, ID: $documentId")
        firestore.collection(collection).document(documentId).set(data).await()
        Unit
    }.onFailure {
        FreshLogger.e("FirestoreError", "Failed to save document $documentId in $collection", it)
    }

    /**
     * Actualiza campos específicos de un documento sin sobrescribir el resto.
     */
    suspend fun updateDocument(collection: String, documentId: String, data: Map<String, Any?>): Result<Unit> = runCatching {
        FreshLogger.network("Firestore", "UPDATE", "Collection: $collection, ID: $documentId")
        firestore.collection(collection).document(documentId).update(data).await()
        Unit
    }.onFailure {
        FreshLogger.e("FirestoreError", "Failed to update document $documentId in $collection", it)
    }

    /**
     * Obtiene un documento específico por su ID.
     * Inyecta el ID del documento dentro del mapa de datos retornado.
     * @return El mapa de datos del documento, o null si no existe o hay un error.
     */
    suspend fun getDocument(collection: String, documentId: String): Map<String, Any?>? {
        FreshLogger.network("Firestore", "GET", "Collection: $collection, ID: $documentId")
        return try {
            val snapshot = firestore.collection(collection).document(documentId).get().await()
            if (snapshot.exists()) {
                val data = snapshot.data?.toMutableMap() ?: mutableMapOf()
                data["id"] = snapshot.id
                data
            } else {
                FreshLogger.w("Firestore", "Document $documentId not found in $collection")
                null
            }
        } catch (e: Exception) {
            FreshLogger.e("FirestoreError", "Failed to get document $documentId in $collection", e)
            null
        }
    }

    /**
     * Realiza una consulta simple en una colección filtrando por un campo y su valor exacto.
     * Inyecta el ID de cada documento dentro de su respectivo mapa.
     * Útil para traer datos específicos de una tienda (ej. field = "storeId").
     */
    suspend fun getDocumentsByField(collection: String, field: String, value: Any): List<Map<String, Any?>> {
        FreshLogger.network("Firestore", "QUERY", "Collection: $collection, Filter: $field == $value")
        return try {
            val snapshot = firestore.collection(collection)
                .whereEqualTo(field, value)
                .get()
                .await()

            FreshLogger.d("Firestore", "Query result: ${snapshot.size()} documents found")

            snapshot.documents.mapNotNull { document ->
                val data = document.data?.toMutableMap() ?: return@mapNotNull null
                data["id"] = document.id
                data
            }
        } catch (e: Exception) {
            FreshLogger.e("FirestoreError", "Query failed in $collection ($field == $value)", e)
            emptyList() // En un fallo de red o permisos, retorna una lista vacía para no romper el flujo offline
        }
    }

    /**
     * Elimina un documento específico de una colección.
     */
    suspend fun deleteDocument(collection: String, documentId: String): Result<Unit> = runCatching {
        FreshLogger.network("Firestore", "DELETE", "Collection: $collection, ID: $documentId")
        firestore.collection(collection).document(documentId).delete().await()
        Unit
    }.onFailure {
        FreshLogger.e("FirestoreError", "Failed to delete document $documentId in $collection", it)
    }
}
