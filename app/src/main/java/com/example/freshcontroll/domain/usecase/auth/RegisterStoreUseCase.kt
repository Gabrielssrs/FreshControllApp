package com.example.freshcontroll.domain.usecase.auth

import com.example.freshcontroll.domain.model.Store
import com.example.freshcontroll.domain.model.User
import com.example.freshcontroll.domain.model.UserRole
import com.example.freshcontroll.domain.repository.AuthRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Caso de uso para registrar un nuevo negocio en el sistema.
 * Se encarga de generar los IDs únicos y construir los modelos iniciales del negocio y del dueño.
 */
class RegisterStoreUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        storeName: String,
        ownerName: String,
        email: String,
        phone: String,
        address: String,
        pass: String
    ): Result<Unit> {
        val storeId = UUID.randomUUID().toString()
        val userId = UUID.randomUUID().toString()

        val store = Store(
            id = storeId,
            name = storeName,
            email = email,
            phone = phone,
            address = address,
            createdAt = System.currentTimeMillis()
        )

        val user = User(
            id = userId,
            storeId = storeId,
            fullName = ownerName,
            email = email,
            phone = phone,
            role = UserRole.OWNER,
            hasAccess = true,
            photoUrl = null
        )

        return authRepository.registerStoreAndOwner(store, user, pass)
    }
}