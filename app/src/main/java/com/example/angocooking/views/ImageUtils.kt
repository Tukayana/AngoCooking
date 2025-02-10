package com.example.angocooking.views

import androidx.compose.runtime.Composable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.io.File

object ImageUtils {
    fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    val path = it.getString(columnIndex)
                    return File(path)
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}


@Composable
fun rememberImagePicker(onImagePicked: (Uri) -> Unit): ImagePicker {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImagePicked(it) }
    }

    return ImagePicker(launcher)
}

class ImagePicker(private val launcher: androidx.activity.result.ActivityResultLauncher<String>) {
    fun launch() {
        launcher.launch("image/*")
    }
}
