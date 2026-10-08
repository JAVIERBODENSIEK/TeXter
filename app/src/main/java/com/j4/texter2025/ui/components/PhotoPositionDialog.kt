package com.j4.texter2025.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import kotlin.math.abs

/**
 * Dialog for adjusting photo position and zoom
 * Allows user to pan and zoom photo backgrounds to position them correctly
 */
@Composable
fun PhotoPositionDialog(
    photoUri: String,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f,
    initialScale: Float = 1f,
    areaName: String = "Background",
    showAppLayout: Boolean = false, // Show app layout overlay for file list background
    onDismiss: () -> Unit,
    onApply: (offsetX: Float, offsetY: Float, scale: Float) -> Unit
) {
    var offsetX by remember { mutableStateOf(initialOffsetX) }
    var offsetY by remember { mutableStateOf(initialOffsetY) }
    var scale by remember { mutableStateOf(initialScale) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Position $areaName Photo",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Reset button
                    IconButton(
                        onClick = {
                            offsetX = 0f
                            offsetY = 0f
                            scale = 1f
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Position",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Instructions
                Text(
                    text = "• Drag to move\n• Pinch to zoom\n• Use sliders for fine adjustment",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Photo preview area with gestures
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1A1A1A))
                        .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                ) {
                    // For file list background: show full photo in narrow aspect ratio to match file list area
                    // Use aspectRatio to constrain width while showing full photo height
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(if (showAppLayout) 0.3f else 1f) // Narrow aspect ratio for file list
                            .align(Alignment.Center)
                    ) {
                        val painter = rememberHighQualityPhotoPainter(photoUri = photoUri)
                        val isLoaded = painter.state is coil.compose.AsyncImagePainter.State.Success
                        val imgSize = painter.intrinsicSize
                        val cropRatio = if (isLoaded && imgSize.width > 0 && imgSize.height > 0 && imgSize.width.isFinite() && imgSize.height.isFinite()) imgSize else null
                        
                        androidx.compose.foundation.Image(
                            painter = painter,
                            contentDescription = "Photo to position",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    val ratio = if (cropRatio != null && size.width > 0 && size.height > 0) {
                                        maxOf(size.width / cropRatio.width, size.height / cropRatio.height) /
                                            minOf(size.width / cropRatio.width, size.height / cropRatio.height)
                                    } else 1f
                                    val effectiveScale = scale * ratio
                                    scaleX = effectiveScale
                                    scaleY = effectiveScale
                                    translationX = -offsetX * size.width
                                    translationY = offsetY * size.height
                                }
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        val maxRange = scale * 2f
                                        offsetX = (offsetX + dragAmount.x / size.width.toFloat()).coerceIn(-maxRange, maxRange)
                                        offsetY = (offsetY + dragAmount.y / size.height.toFloat()).coerceIn(-maxRange, maxRange)
                                    }
                                }
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        scale = (scale * zoom).coerceIn(0.2f, 5f)
                                        val maxRange = scale * 2f
                                        offsetX = (offsetX + pan.x / size.width.toFloat()).coerceIn(-maxRange, maxRange)
                                        offsetY = (offsetY + pan.y / size.height.toFloat()).coerceIn(-maxRange, maxRange)
                                    }
                                },
                            contentScale = if (isLoaded) ContentScale.Fit else ContentScale.Crop
                        )
                    }
                    
                    // App layout overlay - only show for file list background
                    if (showAppLayout) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            // Top bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                        RoundedCornerShape(6.dp)
                                    )
                            )
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            // Split layout
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // File list area (left) - highlighted
                                Box(
                                    modifier = Modifier
                                        .weight(0.3f)
                                        .fillMaxHeight()
                                        .border(
                                            3.dp,
                                            Color.Red.copy(alpha = 0.7f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .background(
                                            Color.Red.copy(alpha = 0.1f),
                                            RoundedCornerShape(6.dp)
                                        )
                                )
                                
                                // Content area (right)
                                Box(
                                    modifier = Modifier
                                        .weight(0.7f)
                                        .fillMaxHeight()
                                        .background(
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                            RoundedCornerShape(6.dp)
                                        )
                                )
                            }
                        }
                    }
                    
                    // Crosshair indicator
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Vertical line
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(Color.White.copy(alpha = 0.3f))
                        )
                        // Horizontal line
                        Box(
                            modifier = Modifier
                                .height(1.dp)
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.3f))
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Zoom slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Zoom",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${(scale * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = scale,
                        onValueChange = { scale = it },
                        valueRange = 0.5f..3f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Horizontal position slider
                Column {
                    val maxRange = scale * 2f // Dynamic range based on zoom
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Horizontal",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (abs(offsetX) < 0.01f) "Center" 
                                  else if (offsetX > 0) "Right ${(offsetX * 50).toInt()}%" 
                                  else "Left ${(-offsetX * 50).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = offsetX,
                        onValueChange = { offsetX = it },
                        valueRange = -maxRange..maxRange,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Vertical position slider
                Column {
                    val maxRange = scale * 2f // Dynamic range based on zoom
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Vertical",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (abs(offsetY) < 0.01f) "Center" 
                                  else if (offsetY > 0) "Down ${(offsetY * 50).toInt()}%" 
                                  else "Up ${(-offsetY * 50).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = offsetY,
                        onValueChange = { offsetY = it },
                        valueRange = -maxRange..maxRange,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onApply(offsetX, offsetY, scale)
                            onDismiss()
                        }
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}
