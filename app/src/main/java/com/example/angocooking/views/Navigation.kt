package com.example.angocooking.views

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.angocooking.viewmodel.CommentViewModel
import com.example.angocooking.viewmodel.RecipeViewModel
import com.example.angocooking.viewmodel.UserViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(
    viewModel: UserViewModel,
    recipeViewModel: RecipeViewModel,
    commentViewModel: CommentViewModel,
) {
    val navController = rememberNavController()
    val user= viewModel.user.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                navController = navController,
                onLoginSuccess = { navController.navigate("home")}
            )
        }
        composable("register") {
            RegisterScreen(
                viewModel = viewModel,
                navController = navController,
                onRegisterSuccess = {navController.navigate("login")}
            )
        }

        composable("home") {
            HomeScreen(
                viewModel = recipeViewModel,
                navController = navController
            )
        }

        composable("recipe_details/{recipeId}") { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId")?.toIntOrNull() ?: return@composable

            user.value?.let {
                RecipeDetailScreen(
                    recipeId = recipeId,
                    recipeViewModel = recipeViewModel,
                    commentViewModel = commentViewModel,
                    onNavigateBack = { navController.navigateUp() },
                    currentUserId = it.id
                )
            }
        }

        composable("profile") {
            ProfileScreen(
                userViewModel = viewModel,
                recipeViewModel = recipeViewModel,
                navController = navController
            )
        }

        composable("create_recipe") {
            CreateRecipeScreen(
                recipeViewModel = recipeViewModel,
                navController = navController
            )
        }

        composable("edit_recipe/{recipeId}") { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId")?.toIntOrNull() ?: return@composable
            EditRecipeScreen(
                recipeId = recipeId,
                recipeViewModel = recipeViewModel,
                navController = navController
            )
        }


    }
}