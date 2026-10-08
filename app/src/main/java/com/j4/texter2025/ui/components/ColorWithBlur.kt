package com.j4.texter2025.ui.components

import androidx.compose.ui.graphics.Color

/**
 * Data class representing a color with blur effect for glassy appearance
 */
data class ColorWithBlur(
    val color: Color,
    val blur: Float = 0f
) {
    fun toColor(): Color = color
}
