package com.j4.texter2025.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val Icons.Filled.CaseSensitive: ImageVector
    get() = materialIcon(name = "Filled.CaseSensitive") {
        materialPath {
            // Draw "Aa" icon
            moveTo(6f, 20f)
            lineTo(10f, 8f)
            lineTo(14f, 20f)
            moveTo(8f, 16f)
            horizontalLineTo(12f)
            
            // Small 'a'
            moveTo(15f, 16f)
            horizontalLineTo(17f)
            verticalLineTo(14f)
            horizontalLineTo(15f)
            verticalLineTo(16f)
            
            moveTo(15f, 12f)
            horizontalLineTo(17f)
            verticalLineTo(14f)
            horizontalLineTo(15f)
            verticalLineTo(12f)
        }
    }
