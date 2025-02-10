package com.example.angocooking.views

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.angocooking.componentes.GlideImage
import com.example.angocooking.models.Comment
import com.example.angocooking.viewmodel.CommentViewModel
import com.example.angocooking.viewmodel.RecipeViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


private val RecipeAppColors = object {
    val Primary = Color(0xFFFF5722)
    val Surface = Color(0xFFFFFFFF)
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: Int,
    recipeViewModel: RecipeViewModel,
    commentViewModel: CommentViewModel,
    onNavigateBack: () -> Unit,
    currentUserId: Int
) {
    val recipe by recipeViewModel.currentRecipe.collectAsState()
    val comments by commentViewModel.comments.collectAsState()
    val isLoading by recipeViewModel.isLoading.collectAsState()
    val commentText = remember { mutableStateOf("") }


    LaunchedEffect(recipeId) {
        recipeViewModel.getRecipe(recipeId)
        commentViewModel.fetchComments(recipeId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe?.nome ?: "Detalhes da Receita")
                        },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar",tint = RecipeAppColors.Surface)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = RecipeAppColors.Primary)

            )
        }
    ) { padding ->
        if (isLoading && recipe == null) {
            Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        } else {
            recipe?.let { currentRecipe ->
                LazyColumn {
                    item {
                        currentRecipe.imagem?.let {
                            GlideImage(
                                imageUrl = it,
                                contentDescription = currentRecipe.nome,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                            )
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = currentRecipe.nome,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Por ${currentRecipe.autorNome}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = formatDate(currentRecipe.created_at),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Divider(modifier = Modifier.padding(vertical = 16.dp))

                            Text(
                                text = "Ingredientes",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = currentRecipe.ingredientes,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Modo de Preparo",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = currentRecipe.modoPreparo,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Divider(modifier = Modifier.padding(vertical = 16.dp))

                            Text(
                                text = "Comentar",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = commentText.value,
                                onValueChange = { commentText.value = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                label = { Text("Adicionar comentário") },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (commentText.value.isNotBlank()) {
                                                commentViewModel.createComment(
                                                    receitaId = recipeId,
                                                    texto = commentText.value,
                                                    onSuccess = { commentText.value = "" },
                                                    onError = {  }
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Send, "Enviar comentário",tint = RecipeAppColors.Primary)
                                    }
                                }
                            )
                        }
                    }

                    items(comments) { comment ->
                        CommentItem(
                            comment = comment,
                            currentUserId = currentUserId,
                            onDeleteComment = { commentId ->
                                commentViewModel.deleteComment(
                                    id = commentId,
                                    receitaId = recipeId,
                                    onSuccess = { },
                                    onError = {  }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CommentItem(
    comment: Comment,
    currentUserId: Int,
    onDeleteComment: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = comment.autorNome,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatDate(comment.created_at),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (comment.usuarioId == currentUserId) {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Deletar comentário",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = comment.texto,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar exclusão") },
            text = { Text("Deseja realmente excluir este comentário?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteComment(comment.id)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDate(dateString: String): String {
    return try {
        val inputFormatter = DateTimeFormatter.ISO_DATE_TIME
        val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        val date = LocalDateTime.parse(dateString, inputFormatter)
        outputFormatter.format(date)
    } catch (e: Exception) {
        dateString
    }
}