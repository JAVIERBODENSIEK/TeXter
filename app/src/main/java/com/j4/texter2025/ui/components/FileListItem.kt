package com.j4.texter2025.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.j4.texter2025.data.FileModel
import com.j4.texter2025.ui.components.TypewriterText

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun FileListItem(
    file: FileModel,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onToggleSelection: () -> Unit,
    showExtensions: Boolean,
    isRecentlyAccessed: Boolean = false,
    isSelectionMode: Boolean = false,
    onLongPress: () -> Unit,
    fileNameContent: (@Composable () -> Unit)? = null,
    customBgColor: Color? = null,
    photoUri: String? = null,
    photoAlpha: Float = 1f,
    photoBlur: Float = 0f,
    photoOffsetX: Float = 0f,
    photoOffsetY: Float = 0f,
    photoScale: Float = 1f,
    photoRotation: Float = 0f,
    photoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.95f else 1f,
        label = "scale"
    )
    

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .scale(scale)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { 
                    if (!isSelectionMode) onSelect() 
                    else onToggleSelection()
                },
                onLongClick = { onLongPress() }
            ),
        shape = MaterialTheme.shapes.medium,
        color = Color.Transparent,
        tonalElevation = if (isSelected) 8.dp else 2.dp
    ) {
        // Determine background color
        // Keep a stable local base color even when photo is used, so alpha changes
        // don't reveal blurred lower layers when photo blur is zero.
        // Default color (0xFF333333) matches the MiniAppPreview default for consistency
        val defaultFileListItemColor = Color(0xFF333333)
        val backgroundColor = if (photoUri != null && photoUri.isNotEmpty()) {
            Color.Transparent
        } else {
            customBgColor ?: if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                defaultFileListItemColor
        }
            
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
        ) {
            
            // Photo background layer (if photo is selected) - overlays the color
            if (photoUri != null && photoUri.isNotEmpty()) {
                val painter = rememberHighQualityPhotoPainter(
                    photoUri = photoUri,
                    onError = { error ->
                        android.util.Log.e("FileListItem", "Failed to load photo: ${error.result.throwable.message}")
                    }
                )
                
                val isLoaded = painter.state is coil.compose.AsyncImagePainter.State.Success
                val imageAlpha = if (isLoaded) photoAlpha else 0f
                val imgSize = painter.intrinsicSize
                val cropRatio = if (isLoaded && imgSize.width > 0 && imgSize.height > 0 && imgSize.width.isFinite() && imgSize.height.isFinite()) imgSize else null
                
                when (photoBackgroundMode) {
                    com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC -> {
                        androidx.compose.foundation.Image(
                            painter = painter,
                            contentDescription = "File item background photo",
                            modifier = Modifier
                                .matchParentSize()
                                .graphicsLayer {
                                    rotationZ = photoRotation
                                    val ratio = if (cropRatio != null && size.width > 0 && size.height > 0) {
                                        maxOf(size.width / cropRatio.width, size.height / cropRatio.height) /
                                            minOf(size.width / cropRatio.width, size.height / cropRatio.height)
                                    } else 1f
                                    val effectiveScale = photoScale * ratio
                                    scaleX = effectiveScale
                                    scaleY = effectiveScale
                                    translationX = -photoOffsetX * size.width
                                    translationY = photoOffsetY * size.height
                                }
                                .applyPhotoAlpha(imageAlpha)
                                .applyPhotoBlur(photoBlur),
                            contentScale = if (isLoaded) ContentScale.Fit else ContentScale.Crop
                        )
                    }
                    com.j4.texter2025.data.PhotoBackgroundMode.STATIC_ZOOM -> {
                        androidx.compose.foundation.Image(
                            painter = painter,
                            contentDescription = "File item background photo",
                            modifier = Modifier
                                .matchParentSize()
                                .graphicsLayer {
                                    rotationZ = photoRotation
                                    scaleX = photoScale
                                    scaleY = photoScale
                                    translationX = 0f
                                    translationY = 0f
                                }
                                .applyPhotoAlpha(imageAlpha)
                                .applyPhotoBlur(photoBlur),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
            
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File name and details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (fileNameContent != null) {
                    fileNameContent()
                } else {
                    TypewriterText(
                        text = if (showExtensions) file.name 
                              else file.name.substringBeforeLast(".txt", file.name),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (file.memo.isNotEmpty()) {
                    TypewriterText(
                        text = file.memo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Edit button - opens full editor
            if (!isSelectionMode) {
                IconButton(
                    onClick = { onEdit() },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit file",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        }
    }
}