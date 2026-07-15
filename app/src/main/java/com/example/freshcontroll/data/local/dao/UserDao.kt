package com.example.freshcontroll.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.freshcontroll.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE storeId = :storeId AND role = 'EMPLOYEE'")
    fun getEmployees(storeId: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("UPDATE users SET hasAccess = :hasAccess WHERE id = :id")
    suspend fun updateAccess(id: String, hasAccess: Boolean)

    @Query("UPDATE users SET fullName = :fullName, email = :email, phone = :phone, photoUrl = :photoUrl WHERE id = :id")
    suspend fun updateProfile(id: String, fullName: String, email: String, phone: String, photoUrl: String?)

    // --- Offline-First Methods ---

    @Query("SELECT * FROM users WHERE storeId = :storeId AND isSynced = 0")
    suspend fun getUnsyncedUsers(storeId: String): List<UserEntity>

    @Query("UPDATE users SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}