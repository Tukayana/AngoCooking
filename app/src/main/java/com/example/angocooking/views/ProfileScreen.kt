package com.example.angocooking.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.angocooking.Data.User
import com.example.angocooking.componentes.GlideImage
import com.example.angocooking.models.Recipe
import com.example.angocooking.viewmodel.RecipeViewModel
import com.example.angocooking.viewmodel.UserViewModel

private val RecipeAppColors = object {
    val Primary = Color(0xFFFF5722)
    val Secondary = Color(0xFF4CAF50)
    val Background = Color(0xFFFAFAFA)
    val Surface = Color(0xFFFFFFFF)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userViewModel: UserViewModel,
    recipeViewModel: RecipeViewModel,
    navController: NavController
) {
    val user by userViewModel.user.collectAsState()
    val recipes by recipeViewModel.recipes.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        recipeViewModel.fetchRecipes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meu Perfil", color = RecipeAppColors.Surface) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = RecipeAppColors.Surface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.ExitToApp, "Terminar Sessão", tint = RecipeAppColors.Surface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RecipeAppColors.Primary)
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(RecipeAppColors.Background)
        ) {
            ProfileHeader(
                user = user,
                onPhotoClick = { }
            )

            Text(
                text = "Minhas Receitas",
                style = MaterialTheme.typography.titleLarge,
                color = RecipeAppColors.Primary,
                modifier = Modifier.padding(16.dp)
            )

            val userRecipes = recipes.filter { it.usuarioId == user?.id }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(userRecipes) { recipe ->
                    RecipeCard2(
                        recipe = recipe,
                        onClick = { navController.navigate("edit_recipe/${recipe.id}") }
                    )
                }
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Terminar Sessão") },
                text = { Text("Tem certeza que deseja sair?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            userViewModel.logout()
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    ) {
                        Text("Sim", color = RecipeAppColors.Secondary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Não", color = Color.Red)
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileHeader(
    user: User?,
    onPhotoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .size(120.dp)
                .clickable(onClick = onPhotoClick),
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = RecipeAppColors.Surface)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user?.foto_perfil ?: "https://via.placeholder.com/120")
                    .crossfade(true)
                    .build(),
                contentDescription = "Foto de Perfil",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user?.nome ?: "",
            style = MaterialTheme.typography.headlineSmall,
        )

        Text(
            text = user?.email ?: "",
            style = MaterialTheme.typography.bodyLarge,

        )
    }
}

@Composable
fun RecipeCard2(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = RecipeAppColors.Surface)
    ) {
        Column {
            recipe.imagem?.let {
                GlideImage(
                    imageUrl = it,
                    contentDescription = recipe.nome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = recipe.nome,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = RecipeAppColors.Primary
                )

                Text(
                    text = "Comentários: ${recipe.totalComentarios}",
                    style = MaterialTheme.typography.bodySmall,

                )
            }
        }
    }
}
