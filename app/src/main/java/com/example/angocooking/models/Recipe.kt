package com.example.angocooking.models

data class Recipe(
    val id: Int,
    val nome: String,
    val ingredientes: String,
    val modoPreparo: String,
    val imagem: String?,
    val usuarioId: Int,
    val autorNome: String,
    val totalComentarios: Int,
    val created_at: String
)
