package com.example.angocooking.models

import com.example.angocooking.Data.User

data class AuthResponse(
    val token: String,
    val user: User
)