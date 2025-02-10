package com.example.angocooking.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.graphics.Color
import com.example.angocooking.viewmodel.RecipeViewModel

private val RecipeAppColors = object {
    val Primary = Color(0xFFFF5722)
    val PrimaryVariant = Color(0xFFE64A19)
    val Secondary = Color(0xFF4CAF50)
    val Background = Color(0xFFFAFAFA)
    val Surface = Color(0xFFFFFFFF)
    val CardOverlay = Color(0x80000000)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(
    recipeViewModel: RecipeViewModel,
    navController: NavController
) {
    var nome by remember { mutableStateOf("") }
    var ingredientes by remember { mutableStateOf("") }
    var modoPreparo by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val imagePicker = rememberImagePicker { uri ->
        selectedImageUri = uri
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nova Receita", color = RecipeAppColors.Surface) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Voltar", tint = RecipeAppColors.Surface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RecipeAppColors.Primary
                )
            )
        },
        containerColor = RecipeAppColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(RecipeAppColors.Background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { imagePicker.launch() }
                    .background(RecipeAppColors.Surface)
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(selectedImageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Imagem da Receita",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = RecipeAppColors.Surface
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                "Adicionar Imagem",
                                modifier = Modifier.size(48.dp),
                                tint = RecipeAppColors.PrimaryVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Adicionar Foto",
                                style = MaterialTheme.typography.bodyLarge,
                                color = RecipeAppColors.PrimaryVariant
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp)

            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome da Receita") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = ingredientes,
                    onValueChange = { ingredientes = it },
                    label = { Text("Ingredientes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    enabled = !isLoading,
                    placeholder = { Text("Digite um ingrediente por linha", color = RecipeAppColors.Secondary) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = modoPreparo,
                    onValueChange = { modoPreparo = it },
                    label = { Text("Modo de Preparo") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 6,
                    enabled = !isLoading,
                    placeholder = { Text("Descreva o passo a passo do preparo", color = RecipeAppColors.Secondary) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        isLoading = true
                        val imageFile = selectedImageUri?.let { uri ->
                            ImageUtils.uriToFile(context, uri)
                        }

                        recipeViewModel.createRecipe(
                            nome = nome,
                            ingredientes = ingredientes,
                            modoPreparo = modoPreparo,
                            imageFile = imageFile,
                            onSuccess = {
                                isLoading = false
                                navController.navigateUp()
                            },
                            onError = {
                                isLoading = false
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && nome.isNotBlank() &&
                            ingredientes.isNotBlank() && modoPreparo.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = RecipeAppColors.Primary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = RecipeAppColors.Surface
                        )
                    } else {
                        Text("Publicar Receita", color = RecipeAppColors.Surface)
                    }
                }
            }
        }
    }
}
