package com.example.angocooking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.angocooking.API.ApiService
import com.example.angocooking.models.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class RecipeViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes

    private val _currentRecipe = MutableStateFlow<Recipe?>(null)
    val currentRecipe: StateFlow<Recipe?> = _currentRecipe

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getRecipes()
                if (response.isSuccessful) {
                    _recipes.value = response.body() ?: emptyList()
                } else {
                    _errorMessage.value = "Falha ao carregar receitas"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getRecipe(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getRecipe(id)
                if (response.isSuccessful) {
                    _currentRecipe.value = response.body()
                } else {
                    _errorMessage.value = "Falha ao carregar receita"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createRecipe(
        nome: String,
        ingredientes: String,
        modoPreparo: String,
        imageFile: File?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val nomeBody = nome.toRequestBody("text/plain".toMediaTypeOrNull())
                val ingredientesBody = ingredientes.toRequestBody("text/plain".toMediaTypeOrNull())
                val modoPreparoBody = modoPreparo.toRequestBody("text/plain".toMediaTypeOrNull())

                val imagePart = imageFile?.let {
                    val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("imagem", it.name, requestFile)
                }

                val response = apiService.createRecipe(
                    nome = nomeBody,
                    ingredientes = ingredientesBody,
                    modoPreparo = modoPreparoBody,
                    imagem = imagePart
                )

                if (response.isSuccessful) {
                    fetchRecipes()
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

    fun updateRecipe(
        id: Int,
        nome: String,
        ingredientes: String,
        modoPreparo: String,
        imageFile: File?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val nomeBody = nome.toRequestBody("text/plain".toMediaTypeOrNull())
                val ingredientesBody = ingredientes.toRequestBody("text/plain".toMediaTypeOrNull())
                val modoPreparoBody = modoPreparo.toRequestBody("text/plain".toMediaTypeOrNull())

                val imagePart = imageFile?.let {
                    val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("imagem", it.name, requestFile)
                }

                val response = apiService.updateRecipe(
                    id = id,
                    nome = nomeBody,
                    ingredientes = ingredientesBody,
                    modoPreparo = modoPreparoBody,
                    imagem = imagePart
                )

                if (response.isSuccessful) {
                    fetchRecipes()
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

    fun deleteRecipe(id: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.deleteRecipe(id)
                if (response.isSuccessful) {
                    fetchRecipes()
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
}