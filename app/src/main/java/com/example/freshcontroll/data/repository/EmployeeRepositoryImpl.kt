package com.example.freshcontroll.data.repository

import com.example.freshcontroll.data.local.dao.UserDao
import com.example.freshcontroll.data.mapper.toDomainList
import com.example.freshcontroll.data.mapper.toEntity
import com.example.freshcontroll.data.remote.FirebaseAuthService
import com.example.freshcontroll.data.remote.FirestoreService
import com.example.freshcontroll.domain.model.User
import com.example.freshcontroll.domain.repository.EmployeeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EmployeeRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val firestoreService: FirestoreService,
    private val authService: FirebaseAuthService
) : EmployeeRepository {

    override fun getEmployees(storeId: String): Flow<List<User>> {
        return userDao.getEmployees(storeId).map { it.toDomainList() }
    }

    override suspend fun addEmployee(user: User, temporaryPassword: String): Result<Unit> = runCatching {
        // Registra al empleado en Auth (en la app real necesitarías re-autenticar al dueño después, o usar Cloud Functions)
        val uid = authService.register(user.email, temporaryPassword).getOrThrow()
        val finalUser = user.copy(id = uid)

        userDao.insertUser(finalUser.toEntity(isSynced = false))

        val userMap = mapOf(
            "storeId" to finalUser.storeId,
            "fullName" to finalUser.fullName,
            "email" to finalUser.email,
            "phone" to finalUser.phone,
            "role" to finalUser.role.name,
            "hasAccess" to finalUser.hasAccess,
            "photoUrl" to finalUser.photoUrl
        )

        firestoreService.saveDocument("users", finalUser.id, userMap).onSuccess {
            userDao.markAsSynced(finalUser.id)
        }
    }

    override suspend fun updateEmployeeAccess(userId: String, hasAccess: Boolean): Result<Unit> = runCatching {
        userDao.updateAccess(userId, hasAccess)
        // Actualizamos de forma silenciosa el campo remoto
        runCatching { firestoreService.saveDocument("users", userId, mapOf("hasAccess" to hasAccess)) }
    }

    override suspend fun updateProfile(userId: String, fullName: String, email: String, phone: String, photoUrl: String?): Result<Unit> = runCatching {
        userDao.updateProfile(userId, fullName, email, phone, photoUrl)

        val updateMap = mapOf(
            "fullName" to fullName,
            "email" to email,
            "phone" to phone,
            "photoUrl" to photoUrl
        )
        runCatching { firestoreService.saveDocument("users", userId, updateMap) }
    }
}