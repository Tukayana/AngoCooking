package com.example.angocooking.componentes

import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import com.bumptech.glide.Glide
import com.example.angocooking.API.base
import java.io.File

const val BASE_URL = base.Url

@Composable
fun GlideImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    isOffline: Boolean = false
) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            ImageView(ctx).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
        },
        modifier = modifier,
        update = { imageView ->
            val imageSource = if (isOffline) {
                File(imageUrl)
            } else {
                "$BASE_URL$imageUrl"
            }

            Glide.with(context)
                .load(imageSource)
                .centerCrop()
                .into(imageView)
        }
    )
}