package com.j4.texter2025.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale

private const val MAX_PHOTO_DECODE_DIMENSION_PX = 4096

@Composable
fun rememberHighQualityPhotoPainter(
    photoUri: String?,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null
): AsyncImagePainter {
    val context = LocalContext.current
    return rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(photoUri)
            .precision(Precision.EXACT)
            .scale(Scale.FILL)
            .size(MAX_PHOTO_DECODE_DIMENSION_PX, MAX_PHOTO_DECODE_DIMENSION_PX)
            .crossfade(false)
            .build(),
        onError = onError
    )
}
