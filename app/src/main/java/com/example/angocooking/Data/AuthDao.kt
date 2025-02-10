package com.example.angocooking.Data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthDao {
    @Query("SELECT * FROM auth_data WHERE id = 1")
    fun getAuthData(): Flow<AuthData?>

    @Query("SELECT * FROM auth_data WHERE id = 1")
    suspend fun getAuthDataSync(): AuthData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAuthData(authData: AuthData)

    @Query("DELETE FROM auth_data")
    suspend fun clearAuthData()
}


class AuthManager(private val authDao: AuthDao) {
    suspend fun saveAuth(token: String, user: User) {
        val authData = AuthData(token = token, user = user)
        authDao.saveAuthData(authData)
    }

    suspend fun saveToken(token: String) {
        val currentAuth = authDao.getAuthDataSync()
        val authData = AuthData(token = token, user = currentAuth?.user)
        authDao.saveAuthData(authData)
    }

    fun getAuthData(): Flow<AuthData?> = authDao.getAuthData()

    suspend fun getToken(): String? = authDao.getAuthDataSync()?.token

    suspend fun getUser(): User? = authDao.getAuthDataSync()?.user

    suspend fun clearAuth() = authDao.clearAuthData()
}
