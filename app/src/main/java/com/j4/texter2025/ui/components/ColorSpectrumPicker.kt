package com.j4.texter2025.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Canvas as ComposeCanvas
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect

/**
 * A square HSV color spectrum picker for Jetpack Compose.
 * Uses efficient gradient-based rendering instead of pixel-by-pixel bitmap generation.
 *
 * @param hue The hue value (0-360) for the spectrum
 * @param modifier The modifier for sizing
 * @param sv The saturation and value (brightness) pair
 * @param onSvChanged Callback when the saturation and value change
 */
@Composable
fun ColorSpectrumPicker(
    hue: Float,
    modifier: Modifier = Modifier,
    sv: Pair<Float, Float>,
    onSvChanged: (Pair<Float, Float>) -> Unit
) {
    val pointerRadius = 12.dp
    val pointerStroke = 2.dp
    
    // Calculate the hue color once
    val hueColor = remember(hue) { Color.hsv(hue.coerceIn(0f, 360f), 1f, 1f) }

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
    ) {
        val density = LocalDensity.current
        val wPx = with(density) { maxWidth.toPx() }
        val hPx = with(density) { maxHeight.toPx() }
        
        // Use Box with gradient backgrounds instead of expensive bitmap generation
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(hueColor) // Base hue color
                .background(
                    // Saturation gradient: white to transparent (left to right)
                    Brush.horizontalGradient(
                        colors = listOf(Color.White, Color.Transparent)
                    )
                )
                .background(
                    // Value gradient: transparent to black (top to bottom)
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black)
                    )
                )
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val s = (offset.x / wPx).coerceIn(0f, 1f)
                        val v = 1f - (offset.y / hPx).coerceIn(0f, 1f)
                        onSvChanged(s to v)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val s = (change.position.x / wPx).coerceIn(0f, 1f)
                        val v = 1f - (change.position.y / hPx).coerceIn(0f, 1f)
                        onSvChanged(s to v)
                    }
                }
        )
        
        // Draw pointer using Canvas overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val px = sv.first * wPx
            val py = (1f - sv.second) * hPx
            drawCircle(
                color = Color.Black,
                radius = pointerRadius.toPx() + pointerStroke.toPx(),
                center = Offset(px, py),
                alpha = 0.5f
            )
            drawCircle(
                color = Color.White,
                radius = pointerRadius.toPx(),
                center = Offset(px, py)
            )
        }
    }
}

fun svFromColor(color: Color): Pair<Float, Float> {
    val hsv = FloatArray(3)
    @Suppress("DEPRECATION")
    android.graphics.Color.colorToHSV(color.toArgb(), hsv)
    return hsv[1] to hsv[2]
}
