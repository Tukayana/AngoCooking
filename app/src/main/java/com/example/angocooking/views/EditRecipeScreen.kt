package com.example.angocooking.views

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.angocooking.componentes.GlideImage
import com.example.angocooking.viewmodel.RecipeViewModel
import java.io.File

private val RecipeAppColors = object {
    val Primary = Color(0xFFFF5722)
    val Background = Color(0xFFFAFAFA)
    val Surface = Color(0xFFFFFFFF)
    val CardOverlay = Color(0x80000000)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecipeScreen(
    recipeId: Int,
    recipeViewModel: RecipeViewModel,
    navController: NavController
) {
    val recipe by recipeViewModel.currentRecipe.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var nome by remember { mutableStateOf("") }
    var ingredientes by remember { mutableStateOf("") }
    var modoPreparo by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imagePreview by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val imagePicker = rememberImagePicker { uri ->
        selectedImageUri = uri
        uri?.let {
            val filePath = ImageUtils2.getPathFromUri(context, it)
            imagePreview = filePath
        }
    }

    LaunchedEffect(recipeId) {
        recipeViewModel.getRecipe(recipeId)
    }

    LaunchedEffect(recipe) {
        recipe?.let {
            nome = it.nome
            ingredientes = it.ingredientes
            modoPreparo = it.modoPreparo
            if (imagePreview == null) {
                imagePreview = it.imagem
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Receita", color = RecipeAppColors.Surface) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Voltar", tint = RecipeAppColors.Surface)
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Apagar Receita", tint = RecipeAppColors.Surface)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = RecipeAppColors.Primary)
            )
        }
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
            ) {
                if (imagePreview != null) {
                    GlideImage(
                        imageUrl = imagePreview!!,
                        contentDescription = "Imagem da Receita",
                        modifier = Modifier.fillMaxSize(),
                        isOffline = selectedImageUri != null
                    )
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp),
                    shape = CircleShape,
                    color = RecipeAppColors.CardOverlay
                ) {
                    Icon(
                        Icons.Default.Add, "Alterar Imagem",
                        modifier = Modifier.padding(12.dp),
                        tint = RecipeAppColors.Surface
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome da Receita") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = ingredientes,
                    onValueChange = { ingredientes = it },
                    label = { Text("Ingredientes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = modoPreparo,
                    onValueChange = { modoPreparo = it },
                    label = { Text("Modo de Preparo") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 6
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val imageFile = selectedImageUri?.let { uri ->
                            ImageUtils.uriToFile(context, uri)
                        }

                        recipeViewModel.updateRecipe(
                            id = recipeId,
                            nome = nome,
                            ingredientes = ingredientes,
                            modoPreparo = modoPreparo,
                            imageFile = imageFile,
                            onSuccess = { navController.navigateUp() },
                            onError = {  }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RecipeAppColors.Primary)
                ) {
                    Text("Salvar Alterações", color = RecipeAppColors.Surface)
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Apagar Receita", color = RecipeAppColors.Primary) },
                text = { Text("Tem certeza que deseja apagar esta receita? Esta ação não pode ser desfeita.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            recipeViewModel.deleteRecipe(
                                id = recipeId,
                                onSuccess = { navController.navigateUp() },
                                onError = { /* Handle error */ }
                            )
                        }
                    ) {
                        Text("Apagar", color = RecipeAppColors.Primary)
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
}

object ImageUtils2 {
    fun getPathFromUri(context: Context, uri: Uri): String {
        var filePath = ""
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                if (idx != -1) {
                    filePath = it.getString(idx)
                }
            }
        }
        return filePath
    }

    fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val filePath = getPathFromUri(context, uri)
            if (filePath.isNotEmpty()) {
                File(filePath)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
