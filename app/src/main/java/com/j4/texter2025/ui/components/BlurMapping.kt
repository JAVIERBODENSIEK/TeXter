package com.j4.texter2025.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.pow

private const val PHOTO_BLUR_MAX_RADIUS_DP = 5f
private const val PHOTO_BLUR_EPSILON = 0.0005f

private fun normalizePhotoBlur(blurValue: Float): Float {
    val normalized = blurValue.coerceIn(0f, 1f)
    return if (normalized <= PHOTO_BLUR_EPSILON) 0f else normalized
}

fun Modifier.applyPhotoBlur(blurValue: Float): Modifier {
    val normalized = normalizePhotoBlur(blurValue)
    if (normalized <= 0f) return this
    return this.blur(mapPhotoBlurToRadius(normalized))
}

fun Modifier.applyPhotoPreviewBlur(blurValue: Float): Modifier {
    val normalized = normalizePhotoBlur(blurValue)
    if (normalized <= 0f) return this
    return this.blur(mapPhotoPreviewBlurToRadius(normalized))
}

fun Modifier.applyPhotoAlpha(alphaValue: Float): Modifier {
    val alpha = alphaValue.coerceIn(0f, 1f)
    if (alpha >= 0.9995f) return this
    return this.graphicsLayer {
        this.alpha = alpha
        compositingStrategy = CompositingStrategy.ModulateAlpha
    }
}

fun mapPhotoBlurToRadius(blurValue: Float): Dp {
    val normalized = normalizePhotoBlur(blurValue)
    if (normalized <= 0f) return 0.dp

    // Strict linear ramp so every 1% step increases blur proportionally.
    return (normalized * PHOTO_BLUR_MAX_RADIUS_DP).dp
}

fun mapPhotoPreviewBlurToRadius(blurValue: Float): Dp {
    val normalized = normalizePhotoBlur(blurValue)
    if (normalized <= 0f) return 0.dp

    // Keep preview blur intentionally softer than runtime, but fully continuous
    // (no threshold cliff) so slider motion never jumps from "none" to "too much".
    val eased = normalized.pow(3.1f)
    val compensated = (eased * 0.34f + normalized * 0.03f).coerceIn(0f, 1f)
    return mapPhotoBlurToRadius(compensated)
}
