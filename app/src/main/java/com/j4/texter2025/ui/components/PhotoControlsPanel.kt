package com.j4.texter2025.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PhotoControlsPanel(
    selectedPhotoUri: String?,
    alpha: Float,
    blur: Float,
    offsetX: Float,
    offsetY: Float,
    scale: Float,
    photoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
    onPhotoSelected: (String) -> Unit,
    onPhotoRemoved: () -> Unit,
    onAlphaChange: (Float) -> Unit,
    onBlurChange: (Float) -> Unit,
    onOffsetXChange: (Float) -> Unit,
    onOffsetYChange: (Float) -> Unit,
    onScaleChange: (Float) -> Unit,
    onBackgroundModeChange: ((com.j4.texter2025.data.PhotoBackgroundMode) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoadingPhoto by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            isLoadingPhoto = true
            scope.launch {
                val internalPath = withContext(Dispatchers.IO) {
                    com.j4.texter2025.data.PhotoStorage.savePhoto(context, selectedUri)
                }
                if (internalPath != null) {
                    onPhotoSelected(internalPath)
                } else {
                    try {
                        context.contentResolver.takePersistableUriPermission(
                            selectedUri,
                            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (_: SecurityException) {}
                    onPhotoSelected(selectedUri.toString())
                }
                isLoadingPhoto = false
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Loading indicator
        AnimatedVisibility(
            visible = isLoadingPhoto,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "⏳ Importing photo...",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Photo selection buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    try { photoPickerLauncher.launch("image/*") }
                    catch (e: Exception) { android.util.Log.e("PhotoControls", "Failed to launch picker", e) }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoadingPhoto
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Pick Photo")
            }

            if (selectedPhotoUri != null) {
                OutlinedButton(
                    onClick = onPhotoRemoved,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoadingPhoto
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Remove")
                }
            }
        }

        // Only show sliders when a photo is selected
        if (selectedPhotoUri != null) {
            // Background mode selector
            if (onBackgroundModeChange != null) {
                Text(
                    "Background Mode",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = photoBackgroundMode == com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
                        onClick = { onBackgroundModeChange(com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC) },
                        label = { Text("Dynamic", style = MaterialTheme.typography.bodyMedium) },
                        enabled = true,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = photoBackgroundMode == com.j4.texter2025.data.PhotoBackgroundMode.STATIC_ZOOM,
                        onClick = { onBackgroundModeChange(com.j4.texter2025.data.PhotoBackgroundMode.STATIC_ZOOM) },
                        label = { Text("Auto-Zoom", style = MaterialTheme.typography.bodyMedium) },
                        enabled = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    when (photoBackgroundMode) {
                        com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC -> "Photo moves with gestures"
                        com.j4.texter2025.data.PhotoBackgroundMode.STATIC_ZOOM -> "Photo auto-zooms to fill area"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Alpha slider
            SliderRow(label = "Alpha", value = alpha, onValueChange = onAlphaChange, valueRange = 0f..1f, displayValue = "${(alpha * 100).roundToInt()}%")

            // Blur slider
            SliderRow(label = "Blur", value = blur, onValueChange = onBlurChange, valueRange = 0f..1f, displayValue = "${String.format("%.1f", blur * 100)}%")

            // Zoom slider
            SliderRow(label = "Zoom", value = scale, onValueChange = onScaleChange, valueRange = 0.5f..3f, displayValue = "${String.format("%.1f", scale)}x")

            // Scale-aware position limits: allow more panning when zoomed in
            val maxOffset = 3f * scale

            // Horizontal position slider
            SliderRow(label = "H-Pos", value = offsetX.coerceIn(-maxOffset, maxOffset), onValueChange = onOffsetXChange, valueRange = -maxOffset..maxOffset, displayValue = "${(offsetX * 100).roundToInt()}%")

            // Vertical position slider
            SliderRow(label = "V-Pos", value = offsetY.coerceIn(-maxOffset, maxOffset), onValueChange = onOffsetYChange, valueRange = -maxOffset..maxOffset, displayValue = "${(offsetY * 100).roundToInt()}%")

            Text(
                "Drag preview to move • Pinch to zoom",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    displayValue: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            label,
            modifier = Modifier.width(44.dp),
            style = MaterialTheme.typography.bodySmall
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.weight(1f)
        )
        Text(
            displayValue,
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
