package com.example.angocooking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.angocooking.API.ApiService
import com.example.angocooking.Data.AuthManager
import com.example.angocooking.Data.User
import com.example.angocooking.models.AuthResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import retrofit2.Response

class UserViewModel(
    private val apiService: ApiService,
    private val authManager: AuthManager
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _authToken = MutableStateFlow<String?>(null)
    val authToken: StateFlow<String?> = _authToken

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            try {
                val user = authManager.getUser()
                val token = authManager.getToken()
                if (user != null && token != null) {
                    _user.value = user
                    _authToken.value = token
                    fetchUser()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro: ${e.message}"
            }
        }
    }

    private fun fetchUser() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getUserProfile()
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    userResponse?.let { user ->
                        _user.value = user
                        /// Atualiza no Room////
                        _authToken.value?.let { token ->
                            authManager.saveAuth(token, user)
                        }
                    }
                } else {
                    _errorMessage.value = "Falha ao obter dados do usuário"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun login(email: String, senha: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.login(mapOf("email" to email, "senha" to senha))
                handleAuthResponse(response)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError(response.errorBody()?.string() ?: response.message())
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro: ${e.message}"
                onError("Erro: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(nome: String, email: String, senha: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.register(mapOf(
                    "nome" to nome,
                    "email" to email,
                    "senha" to senha
                ))
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError(response.errorBody()?.string() ?: response.message())
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro: ${e.message}"
                onError("Erro: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfilePhoto(photoFile: File, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val requestFile = photoFile.asRequestBody("image/*".toMediaTypeOrNull())
                val photoPart = MultipartBody.Part.createFormData("foto", photoFile.name, requestFile)

                val response = apiService.updateProfilePhoto(photoPart)
                if (response.isSuccessful) {
                    fetchUser()
                    onSuccess()
                } else {
                    onError(response.errorBody()?.string() ?: response.message())
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro: ${e.message}"
                onError("Erro: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun handleAuthResponse(response: Response<AuthResponse>) {
        if (response.isSuccessful) {
            response.body()?.let { authResponse ->
                _authToken.value = authResponse.token
                authResponse.user?.let { user ->
                    _user.value = user
                    // Salva no Room//
                    authManager.saveAuth(authResponse.token, user)
                }
            }
        } else {
            _errorMessage.value = "Falha na autenticação, por favor verifique os seus dados"
        }
    }

    fun logout() {
        viewModelScope.launch {
            authManager.clearAuth()
            _user.value = null
            _authToken.value = null
        }
    }
}