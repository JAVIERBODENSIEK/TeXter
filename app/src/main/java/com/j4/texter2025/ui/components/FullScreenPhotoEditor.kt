package com.j4.texter2025.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun FullScreenPhotoEditor(
    photoUri: String,
    initialOffsetX: Float,
    initialOffsetY: Float,
    initialScale: Float,
    initialBlur: Float,
    onDismiss: () -> Unit,
    onApply: (offsetX: Float, offsetY: Float, scale: Float, blur: Float) -> Unit,
    targetSection: String = "none",
    onPositionChange: ((offsetX: Float, offsetY: Float, scale: Float, blur: Float) -> Unit)? = null,
    photoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
    onTest: (() -> Unit)? = null
) {
    var offsetX by remember { mutableStateOf(initialOffsetX) }
    var offsetY by remember { mutableStateOf(initialOffsetY) }
    var scale by remember { mutableStateOf(initialScale) }
    var blur by remember { mutableStateOf(initialBlur) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with title and close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Position Photo",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Test button
                        if (onTest != null) {
                            Button(
                                onClick = {
                                    // Apply changes first
                                    onPositionChange?.invoke(offsetX, offsetY, scale, blur)
                                    // Then trigger test
                                    onTest()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Text("Test")
                            }
                        }
                        
                        // Reset button
                        IconButton(onClick = {
                            offsetX = 0f
                            offsetY = 0f
                            scale = 1f
                            blur = 0f
                            onPositionChange?.invoke(offsetX, offsetY, scale, blur)
                        }) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        // Close button
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    }
                }
                
                // Large preview area with touch gestures
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    // Update offset based on pan gesture
                                    offsetX = (offsetX + pan.x / size.width.toFloat()).coerceIn(-1f, 1f)
                                    offsetY = (offsetY + pan.y / size.height.toFloat()).coerceIn(-1f, 1f)
                                    
                                    // Update scale based on zoom gesture
                                    scale = (scale * zoom).coerceIn(0.2f, 5f)
                                    android.util.Log.d("PhotoPos", "GESTURE FullEditor[$targetSection]: offsetX=$offsetX, offsetY=$offsetY, scale=$scale, containerW=${size.width}, containerH=${size.height}")
                                    
                                    // Notify parent immediately during gestures
                                    onPositionChange?.invoke(offsetX, offsetY, scale, blur)
                                }
                            }
                    ) {
                        // Show the app layout preview with the photo
                        AppLayoutPreview(
                            photoUri = photoUri,
                            alpha = 1f,
                            blur = blur,
                            customBgColor = Color(0xFF1E1E1E),
                            contentAreaColor = Color(0xFF2D2D2D),
                            memoAreaColor = Color(0xFF3D3D3D),
                            fileListItemColor = Color(0xFF2D2D2D),
                            fileListBackgroundColor = Color(0xFF1E1E1E),
                            targetSection = targetSection,
                            selectedColor = Color.White,
                            photoOffsetX = offsetX,
                            photoOffsetY = offsetY,
                            photoScale = scale,
                            // Pass positioning to section-specific parameters based on targetSection
                            customBgPhotoOffsetX = if (targetSection == "bg") offsetX else 0f,
                            customBgPhotoOffsetY = if (targetSection == "bg") offsetY else 0f,
                            customBgPhotoScale = if (targetSection == "bg") scale else 1f,
                            contentAreaPhotoOffsetX = if (targetSection == "content") offsetX else 0f,
                            contentAreaPhotoOffsetY = if (targetSection == "content") offsetY else 0f,
                            contentAreaPhotoScale = if (targetSection == "content") scale else 1f,
                            memoAreaPhotoOffsetX = if (targetSection == "memo") offsetX else 0f,
                            memoAreaPhotoOffsetY = if (targetSection == "memo") offsetY else 0f,
                            memoAreaPhotoScale = if (targetSection == "memo") scale else 1f,
                            fileListItemPhotoOffsetX = if (targetSection == "fileListItem") offsetX else 0f,
                            fileListItemPhotoOffsetY = if (targetSection == "fileListItem") offsetY else 0f,
                            fileListItemPhotoScale = if (targetSection == "fileListItem") scale else 1f,
                            fileListBgPhotoOffsetX = if (targetSection == "fileListBg") offsetX else 0f,
                            fileListBgPhotoOffsetY = if (targetSection == "fileListBg") offsetY else 0f,
                            fileListBgPhotoScale = if (targetSection == "fileListBg") scale else 1f,
                            photoBackgroundMode = photoBackgroundMode
                        )
                        
                        // Instruction overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .align(Alignment.TopCenter)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Drag to move • Pinch to zoom",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                
                // Slider controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Zoom slider
                    Text(
                        text = "Zoom: ${String.format("%.1f", scale)}x",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = scale,
                        onValueChange = { 
                            scale = it
                            onPositionChange?.invoke(offsetX, offsetY, scale, blur)
                        },
                        valueRange = 0.5f..3f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    // Blur slider
                    Text(
                        text = "Blur: ${(blur * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = blur,
                        onValueChange = { 
                            blur = it
                            onPositionChange?.invoke(offsetX, offsetY, scale, blur)
                        },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    // Horizontal position slider
                    Text(
                        text = "Horizontal: ${(offsetX * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = offsetX,
                        onValueChange = { 
                            offsetX = it
                            onPositionChange?.invoke(offsetX, offsetY, scale, blur)
                        },
                        valueRange = -1f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    // Vertical position slider
                    Text(
                        text = "Vertical: ${(offsetY * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = offsetY,
                        onValueChange = { 
                            offsetY = it
                            onPositionChange?.invoke(offsetX, offsetY, scale, blur)
                        },
                        valueRange = -1f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = { onApply(offsetX, offsetY, scale, blur) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}
