package com.j4.texter2025.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A surface that can apply blur effects for glassy appearance when transparency is enabled
 */
@Composable
fun BlurredSurface(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    blur: Float = 0f,
    shape: Shape = RoundedCornerShape(8.dp),
    elevation: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    // Apply blur effect only if blur value is greater than 0 AND color has transparency
    val shouldBlur = blur > 0f && color.alpha < 1f
    val blurRadius = if (shouldBlur) (blur * 50f).dp else 0.dp
    
    // Apply blur to the entire surface if blur is enabled
    val surfaceModifier = if (shouldBlur) {
        modifier.blur(radius = blurRadius)
    } else {
        modifier
    }
    
    Surface(
        modifier = surfaceModifier,
        shape = shape,
        color = color,
        tonalElevation = elevation
    ) {
        content()
    }
}
