package com.example.angocooking

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.example.angocooking.API.RetrofitInstance
import com.example.angocooking.Data.AuthDatabase
import com.example.angocooking.Data.AuthManager
import com.example.angocooking.ui.theme.AngoCookingTheme
import com.example.angocooking.viewmodel.CommentViewModel
import com.example.angocooking.viewmodel.RecipeViewModel
import com.example.angocooking.viewmodel.UserViewModel
import com.example.angocooking.views.Navigation

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o banco de dados e AuthManager
        val database = AuthDatabase.getInstance(applicationContext)
        val authManager = AuthManager(database.authDao())

        // Inicializa o Retrofit com o novo sistema de autenticação
        RetrofitInstance.initialize(applicationContext)

        // Inicializa os ViewModels
        val userViewModel = UserViewModel(RetrofitInstance.api, authManager)
        val recipeViewModel = RecipeViewModel(RetrofitInstance.api)
        val commentViewModel = CommentViewModel(RetrofitInstance.api)

        setContent {
            AngoCookingTheme {
                Navigation(userViewModel, recipeViewModel, commentViewModel)
            }
        }
    }
}
