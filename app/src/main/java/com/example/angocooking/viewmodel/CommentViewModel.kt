package com.example.angocooking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.angocooking.API.ApiService
import com.example.angocooking.models.Comment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class CommentViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchComments(receitaId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getComments(receitaId)
                if (response.isSuccessful) {
                    _comments.value = response.body() ?: emptyList()
                } else {
                    _errorMessage.value = "Falha ao carregar comentários"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createComment(
        receitaId: Int,
        texto: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.createComment(receitaId, mapOf("texto" to texto))
                if (response.isSuccessful) {
                    fetchComments(receitaId)
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

    fun updateComment(
        id: Int,
        receitaId: Int,
        texto: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.updateComment(id, mapOf("texto" to texto))
                if (response.isSuccessful) {
                    fetchComments(receitaId)
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

    fun deleteComment(
        id: Int,
        receitaId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.deleteComment(id)
                if (response.isSuccessful) {
                    fetchComments(receitaId)
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
