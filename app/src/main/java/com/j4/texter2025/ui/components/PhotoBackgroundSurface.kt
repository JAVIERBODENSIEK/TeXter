package com.j4.texter2025.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * A surface that can display either a photo background or color background
 * with optional blur effects for glassy appearance
 */
@Composable
fun PhotoBackgroundSurface(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    blur: Float = 0f,
    shape: Shape = RoundedCornerShape(8.dp),
    elevation: Dp = 0.dp,
    // Photo background parameters
    backgroundPhotoUri: String? = null,
    backgroundPhotoAlpha: Float = 1f,
    backgroundPhotoBlur: Float = 0f,
    backgroundPhotoOffsetX: Float = 0f,
    backgroundPhotoOffsetY: Float = 0f,
    backgroundPhotoScale: Float = 1f,
    backgroundPhotoRotation: Float = 0f,
    photoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
    content: @Composable () -> Unit
) {
    val hasPhoto = backgroundPhotoUri != null && backgroundPhotoUri.isNotEmpty()

    // Apply blur effect only if blur value is greater than 0 AND color has transparency
    // and no photo is active (otherwise alpha-revealed photo underlay can look unintentionally blurred).
    val shouldBlur = blur > 0f && color.alpha < 1f && !hasPhoto
    val blurRadius = if (shouldBlur) (blur * 5f).dp else 0.dp
    
    // Apply blur to the entire surface if blur is enabled
    val surfaceModifier = if (shouldBlur) {
        modifier.blur(radius = blurRadius)
    } else {
        modifier
    }
    
    val surfaceColor = if (hasPhoto) Color.Transparent else color

    Surface(
        modifier = surfaceModifier,
        shape = shape,
        color = surfaceColor,
        tonalElevation = elevation
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Photo background layer (if photo is selected) - overlays the color
            if (hasPhoto) {
                val painter = rememberHighQualityPhotoPainter(
                    photoUri = backgroundPhotoUri,
                    onError = { error ->
                        android.util.Log.e("PhotoBackgroundSurface", "Failed to load photo: ${error.result.throwable.message}")
                    }
                )
                
                val isLoaded = painter.state is coil.compose.AsyncImagePainter.State.Success
                val imageAlpha = if (isLoaded) backgroundPhotoAlpha else 0f
                
                val imgSize = painter.intrinsicSize
                val cropRatio = if (isLoaded && imgSize.width > 0 && imgSize.height > 0 && imgSize.width.isFinite() && imgSize.height.isFinite()) {
                    imgSize
                } else null
                
                when (photoBackgroundMode) {
                    com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC -> {
                        // Current behavior - photo moves with gestures
                        androidx.compose.foundation.Image(
                            painter = painter,
                            contentDescription = "Background photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .applyPhotoBlur(backgroundPhotoBlur)
                                .graphicsLayer {
                                    rotationZ = backgroundPhotoRotation
                                    scaleX = backgroundPhotoScale
                                    scaleY = backgroundPhotoScale
                                    translationX = -backgroundPhotoOffsetX * size.width
                                    translationY = backgroundPhotoOffsetY * size.height
                                }
                                .applyPhotoAlpha(imageAlpha),
                            contentScale = ContentScale.Crop
                        )
                    }
                    com.j4.texter2025.data.PhotoBackgroundMode.STATIC_ZOOM -> {
                        // Photo auto-zooms to always fill the area
                        androidx.compose.foundation.Image(
                            painter = painter,
                            contentDescription = "Background photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .applyPhotoBlur(backgroundPhotoBlur)
                                .graphicsLayer {
                                    rotationZ = backgroundPhotoRotation
                                    // Auto-calculate scale to always fill the container
                                    val autoScale = if (cropRatio != null && size.width > 0 && size.height > 0) {
                                        maxOf(size.width / cropRatio.width, size.height / cropRatio.height)
                                    } else 1f
                                    
                                    // Apply user's scale on top of auto-scale
                                    val effectiveScale = autoScale * backgroundPhotoScale
                                    scaleX = effectiveScale
                                    scaleY = effectiveScale
                                    
                                    // Position stays fixed (no translation based on offset)
                                    // Only apply minimal centering adjustment
                                    translationX = 0f
                                    translationY = 0f
                                }
                                .applyPhotoAlpha(imageAlpha),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
            
            // Content layer
            content()
        }
    }
}
