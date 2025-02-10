package com.example.angocooking.API

import android.content.Context
import com.example.angocooking.Data.AuthDatabase
import com.example.angocooking.Data.AuthManager
import com.example.angocooking.Data.User
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking

class AuthInterceptor(private val authManager: AuthManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        //  login e registro
        if (originalRequest.url.encodedPath.endsWith("login") ||
            originalRequest.url.encodedPath.endsWith("register")) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking { authManager.getToken() }
        val modifiedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(modifiedRequest)
    }
}

object RetrofitInstance {
    private const val BASE_URL = base.Url
    private lateinit var authManager: AuthManager

    fun initialize(context: Context) {
        val database = AuthDatabase.getInstance(context)
        authManager = AuthManager(database.authDao())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(authManager))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }

    suspend fun saveAuthData(token: String, user: User) {
        authManager.saveAuth(token, user)
    }

    suspend fun clearAuthData() {
        authManager.clearAuth()
    }

    suspend fun getCurrentUser() = authManager.getUser()
}

object base {
    const val Url: String = "http://192.168.1.140:3000"
}