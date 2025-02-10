package com.example.angocooking.models

data class Comment(
    val id: Int,
    val texto: String,
    val usuarioId: Int,
    val receitaId: Int,
    val autorNome: String,
    val created_at: String
)