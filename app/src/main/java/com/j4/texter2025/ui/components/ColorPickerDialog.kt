package com.j4.texter2025.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import com.j4.texter2025.ui.components.ColorSpectrumPicker
import com.j4.texter2025.ui.components.svFromColor
import com.j4.texter2025.ui.components.ColorWithBlur

// Extension functions - must be defined before use
fun Color.toHexString(): String {
    val argb = (alpha * 255).toInt().shl(24) or
        (red * 255).toInt().shl(16) or
        (green * 255).toInt().shl(8) or
        (blue * 255).toInt()
    return String.format("%08X", argb)
}

fun Color.hue(): Float {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    return hsv[0]
}

private fun parseHexToColorOrNull(rawInput: String): Color? {
    val cleaned = rawInput.trim().removePrefix("#").uppercase()
    val normalized = when (cleaned.length) {
        6 -> "FF$cleaned"
        8 -> cleaned
        else -> return null
    }

    val argb = normalized.toLongOrNull(16) ?: return null
    return Color(argb.toInt())
}

// Data class for background options (color or photo)
data class BackgroundOption(
    val color: Color? = null,
    val photoUri: String? = null,
    val alpha: Float = 1f,
    val blur: Float = 0f,
    val photoOffsetX: Float = 0f,  // Horizontal offset (-1f to 1f)
    val photoOffsetY: Float = 0f,  // Vertical offset (-1f to 1f)
    val photoScale: Float = 1f,    // Zoom scale (0.5f to 3f)
    val photoRotation: Float = 0f  // Rotation in degrees (-180f..180f)
)

// Data class for section photo state (used in preview)
data class SectionPhotoState(
    val photoUri: String? = null,
    val alpha: Float = 1f,
    val blur: Float = 0f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scale: Float = 1f
)

// Simple color palette for demonstration, can be expanded
val defaultColorPalette = listOf(
    Color(0xFF121212), Color(0xFF212121), Color(0xFF333333), Color(0xFF424242), // Dark
    Color(0xFF1976D2), Color(0xFF388E3C), Color(0xFFFBC02D), Color(0xFFD32F2F), // Accent
    Color(0xFFFFFFFF), Color(0xFFE0E0E0), Color(0xFFBDBDBD) // Light
)

@Composable
fun ColorPickerDialog(
    show: Boolean,
    currentColor: Color,
    currentBlur: Float = 0f,
    onColorSelected: (ColorWithBlur) -> Unit,
    onPhotoSelected: (BackgroundOption) -> Unit = {},
    onPhotoUriChange: ((String) -> Unit)? = null,
    onPhotoBlurChange: ((Float) -> Unit)? = null,
    onPhotoOffsetXChange: ((Float) -> Unit)? = null,
    onPhotoOffsetYChange: ((Float) -> Unit)? = null,
    onPhotoScaleChange: ((Float) -> Unit)? = null,
    onDismiss: () -> Unit,
    // Current app colors for preview
    customBgColor: Color? = null,
    contentAreaColor: Color? = null,
    memoAreaColor: Color? = null,
    fileListItemColor: Color? = null,
    fileListBackgroundColor: Color? = null,
    targetSection: String = "bg", // Which section is being edited: "bg", "content", "memo", "fileListItem", "fileListBg"
    // Current photo state for the section being edited
    currentPhotoUri: String? = null,
    currentPhotoAlpha: Float = 1f,
    currentPhotoBlur: Float = 0f,
    currentPhotoOffsetX: Float = 0f,
    currentPhotoOffsetY: Float = 0f,
    currentPhotoScale: Float = 1f,
    // Current photo state for ALL sections (for preview)
    allSectionPhotos: Map<String, SectionPhotoState> = emptyMap(), // Map of sectionType to photo state
    // Initial tab to show (0 = Pick a Color, 1 = Pick a Photo)
    initialTabIndex: Int = 0,
    // Photo background mode
    currentPhotoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
    onPhotoBackgroundModeChange: ((com.j4.texter2025.data.PhotoBackgroundMode) -> Unit)? = null,
    // Test callback to navigate to main app
    onTestInApp: (() -> Unit)? = null
) {
    if (!show) return
    
    // Tab state - initialize with the provided initial tab
    var selectedTabIndex by remember(show) { mutableStateOf(initialTabIndex) }
    val tabs = listOf("Pick a Color", "Pick a Photo")
    
    // Color picker state
    var selectedColor by remember { mutableStateOf(currentColor) }
    var red by remember { mutableStateOf(currentColor.red) }
    var green by remember { mutableStateOf(currentColor.green) }
    var blue by remember { mutableStateOf(currentColor.blue) }
    var alpha by remember { mutableStateOf(currentColor.alpha) }
    var blur by remember { mutableStateOf(currentBlur) } // Blur effect for glassy appearance
    var hue by remember { mutableStateOf(selectedColor.hue()) }
    var sv by remember { mutableStateOf<Pair<Float, Float>>(svFromColor(selectedColor)) }
    
    // Photo picker state - initialize with current values
    var selectedPhotoUri by remember { mutableStateOf<String?>(null) }
    var photoAlpha by remember { mutableStateOf(1f) }
    var photoBlur by remember { mutableStateOf(0f) }
    var photoOffsetX by remember { mutableStateOf(0f) }
    var photoOffsetY by remember { mutableStateOf(0f) }
    var photoScale by remember { mutableStateOf(1f) }
    var photoBackgroundMode by remember { mutableStateOf(currentPhotoBackgroundMode) }
    
    // All sections' photo state loaded from SharedPreferences (bypasses stale allSectionPhotos chain)
    var loadedCustomBgPhotoUri by remember { mutableStateOf<String?>(null) }
    var loadedCustomBgPhotoAlpha by remember { mutableStateOf(1f) }
    var loadedCustomBgPhotoBlur by remember { mutableStateOf(0f) }
    var loadedCustomBgOffsetX by remember { mutableStateOf(0f) }
    var loadedCustomBgOffsetY by remember { mutableStateOf(0f) }
    var loadedCustomBgScale by remember { mutableStateOf(1f) }
    var loadedContentPhotoUri by remember { mutableStateOf<String?>(null) }
    var loadedContentPhotoAlpha by remember { mutableStateOf(1f) }
    var loadedContentPhotoBlur by remember { mutableStateOf(0f) }
    var loadedContentOffsetX by remember { mutableStateOf(0f) }
    var loadedContentOffsetY by remember { mutableStateOf(0f) }
    var loadedContentScale by remember { mutableStateOf(1f) }
    var loadedMemoPhotoUri by remember { mutableStateOf<String?>(null) }
    var loadedMemoPhotoAlpha by remember { mutableStateOf(1f) }
    var loadedMemoPhotoBlur by remember { mutableStateOf(0f) }
    var loadedMemoOffsetX by remember { mutableStateOf(0f) }
    var loadedMemoOffsetY by remember { mutableStateOf(0f) }
    var loadedMemoScale by remember { mutableStateOf(1f) }
    var loadedFileListItemPhotoUri by remember { mutableStateOf<String?>(null) }
    var loadedFileListItemPhotoAlpha by remember { mutableStateOf(1f) }
    var loadedFileListItemPhotoBlur by remember { mutableStateOf(0f) }
    var loadedFileListItemOffsetX by remember { mutableStateOf(0f) }
    var loadedFileListItemOffsetY by remember { mutableStateOf(0f) }
    var loadedFileListItemScale by remember { mutableStateOf(1f) }
    var loadedFileListBgPhotoUri by remember { mutableStateOf<String?>(null) }
    var loadedFileListBgPhotoAlpha by remember { mutableStateOf(1f) }
    var loadedFileListBgPhotoBlur by remember { mutableStateOf(0f) }
    var loadedFileListBgOffsetX by remember { mutableStateOf(0f) }
    var loadedFileListBgOffsetY by remember { mutableStateOf(0f) }
    var loadedFileListBgScale by remember { mutableStateOf(1f) }
    
    // Full-screen photo editor state
    var showFullScreenEditor by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    // Initialize positioning: use section's own saved values, or fall back to CustomBg from SharedPreferences
    LaunchedEffect(show, targetSection) {
        if (show) {
            val prefs = context.getSharedPreferences("TeXterPrefs", android.content.Context.MODE_PRIVATE)
            
            // Load this section's own saved positioning
            val (sectionOffsetX, sectionOffsetY, sectionScale) = when (targetSection) {
                "content" -> Triple(
                    prefs.getFloat("content_area_photo_offset_x", 0f),
                    prefs.getFloat("content_area_photo_offset_y", 0f),
                    prefs.getFloat("content_area_photo_scale", 1f)
                )
                "memo" -> Triple(
                    prefs.getFloat("memo_area_photo_offset_x", 0f),
                    prefs.getFloat("memo_area_photo_offset_y", 0f),
                    prefs.getFloat("memo_area_photo_scale", 1f)
                )
                "fileListItem" -> Triple(
                    prefs.getFloat("file_list_item_photo_offset_x", 0f),
                    prefs.getFloat("file_list_item_photo_offset_y", 0f),
                    prefs.getFloat("file_list_item_photo_scale", 1f)
                )
                "fileListBg" -> Triple(
                    prefs.getFloat("file_list_background_photo_offset_x", 0f),
                    prefs.getFloat("file_list_background_photo_offset_y", 0f),
                    prefs.getFloat("file_list_background_photo_scale", 1f)
                )
                else -> Triple(
                    prefs.getFloat("custom_bg_photo_offset_x", 0f),
                    prefs.getFloat("custom_bg_photo_offset_y", 0f),
                    prefs.getFloat("custom_bg_photo_scale", 1f)
                )
            }
            
            // If section has default values and is not "bg", fall back to CustomBg positioning
            val useCustomBgFallback = targetSection != "bg" && sectionOffsetX == 0f && sectionOffsetY == 0f && sectionScale == 1f
            val finalOffsetX: Float
            val finalOffsetY: Float
            val finalScale: Float
            if (useCustomBgFallback) {
                finalOffsetX = prefs.getFloat("custom_bg_photo_offset_x", 0f)
                finalOffsetY = prefs.getFloat("custom_bg_photo_offset_y", 0f)
                finalScale = prefs.getFloat("custom_bg_photo_scale", 1f)
            } else {
                finalOffsetX = sectionOffsetX
                finalOffsetY = sectionOffsetY
                finalScale = sectionScale
            }
            
            // Load ALL sections' photo state from SharedPreferences for the preview
            loadedCustomBgPhotoUri = prefs.getString("custom_bg_photo_uri", null)
            loadedCustomBgPhotoAlpha = prefs.getFloat("custom_bg_photo_alpha", 1f)
            loadedCustomBgPhotoBlur = prefs.getFloat("custom_bg_photo_blur", 0f)
            loadedCustomBgOffsetX = prefs.getFloat("custom_bg_photo_offset_x", 0f)
            loadedCustomBgOffsetY = prefs.getFloat("custom_bg_photo_offset_y", 0f)
            loadedCustomBgScale = prefs.getFloat("custom_bg_photo_scale", 1f)
            loadedContentPhotoUri = prefs.getString("content_area_photo_uri", null)
            loadedContentPhotoAlpha = prefs.getFloat("content_area_photo_alpha", 1f)
            loadedContentPhotoBlur = prefs.getFloat("content_area_photo_blur", 0f)
            loadedContentOffsetX = prefs.getFloat("content_area_photo_offset_x", 0f)
            loadedContentOffsetY = prefs.getFloat("content_area_photo_offset_y", 0f)
            loadedContentScale = prefs.getFloat("content_area_photo_scale", 1f)
            loadedMemoPhotoUri = prefs.getString("memo_area_photo_uri", null)
            loadedMemoPhotoAlpha = prefs.getFloat("memo_area_photo_alpha", 1f)
            loadedMemoPhotoBlur = prefs.getFloat("memo_area_photo_blur", 0f)
            loadedMemoOffsetX = prefs.getFloat("memo_area_photo_offset_x", 0f)
            loadedMemoOffsetY = prefs.getFloat("memo_area_photo_offset_y", 0f)
            loadedMemoScale = prefs.getFloat("memo_area_photo_scale", 1f)
            loadedFileListItemPhotoUri = prefs.getString("file_list_item_photo_uri", null)
            loadedFileListItemPhotoAlpha = prefs.getFloat("file_list_item_photo_alpha", 1f)
            loadedFileListItemPhotoBlur = prefs.getFloat("file_list_item_photo_blur", 0f)
            loadedFileListItemOffsetX = prefs.getFloat("file_list_item_photo_offset_x", 0f)
            loadedFileListItemOffsetY = prefs.getFloat("file_list_item_photo_offset_y", 0f)
            loadedFileListItemScale = prefs.getFloat("file_list_item_photo_scale", 1f)
            loadedFileListBgPhotoUri = prefs.getString("file_list_background_photo_uri", null)
            loadedFileListBgPhotoAlpha = prefs.getFloat("file_list_background_photo_alpha", 1f)
            loadedFileListBgPhotoBlur = prefs.getFloat("file_list_background_photo_blur", 0f)
            loadedFileListBgOffsetX = prefs.getFloat("file_list_background_photo_offset_x", 0f)
            loadedFileListBgOffsetY = prefs.getFloat("file_list_background_photo_offset_y", 0f)
            loadedFileListBgScale = prefs.getFloat("file_list_background_photo_scale", 1f)
            
            selectedPhotoUri = currentPhotoUri
            photoAlpha = currentPhotoAlpha
            photoBlur = currentPhotoBlur
            photoOffsetX = finalOffsetX
            photoOffsetY = finalOffsetY
            photoScale = finalScale
            
        }
    }
    
    val scrollState = rememberScrollState()
    
    // Defensive: ensure color channels are valid
    if (red.isNaN() || green.isNaN() || blue.isNaN() || alpha.isNaN() || hue.isNaN()) {
        red = 1f; green = 1f; blue = 1f; alpha = 1f; hue = 0f
        selectedColor = Color.White
    }
    AlertDialog(
        onDismissRequest = {
            try { onDismiss() } catch (e: Exception) { e.printStackTrace() }
        },
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(0.9f),
        confirmButton = {
            Button(onClick = {
                try { 
                    if (selectedTabIndex == 0) {
                        // Color tab selected
                        onColorSelected(ColorWithBlur(selectedColor, blur))
                    } else {
                        // Photo tab selected
                        selectedPhotoUri?.let { uri ->
                            // Use local state variables which reflect the gesture changes
                            onPhotoSelected(BackgroundOption(
                                photoUri = uri,
                                alpha = photoAlpha,
                                blur = photoBlur,
                                photoOffsetX = photoOffsetX,
                                photoOffsetY = photoOffsetY,
                                photoScale = photoScale
                            ))
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }
                try { onDismiss() } catch (e: Exception) { e.printStackTrace() }
            }) { Text("Select") }
        },
        dismissButton = {
            Button(onClick = {
                try { onDismiss() } catch (e: Exception) { e.printStackTrace() }
            }) { Text("Cancel") }
        },
        title = { 
            Column {
                Text("Background Picker")
                Spacer(modifier = Modifier.height(4.dp))
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
            }
        },
        text = {
            Column {
                // Sticky preview at top (outside scroll)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Preview", style = MaterialTheme.typography.labelSmall)
                    
                    // Show full-screen edit button when photo is selected
                    if (selectedTabIndex == 1 && selectedPhotoUri != null) {
                        TextButton(
                            onClick = { showFullScreenEditor = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Full Screen Edit",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Full Screen Edit",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                // Use selectedPhotoUri (updates when user picks new photo) with fallback to currentPhotoUri (from parent)
                val activePhotoUri = selectedPhotoUri ?: currentPhotoUri
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f) // Make it square
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (selectedTabIndex == 0) {
                        // Color picker preview - pass current photo URI so section's photo is visible
                        AppLayoutPreview(
                            photoUri = activePhotoUri ?: "",
                            alpha = alpha,
                            blur = blur,
                            customBgColor = if (targetSection == "bg") selectedColor else customBgColor,
                            contentAreaColor = if (targetSection == "content") selectedColor else contentAreaColor,
                            memoAreaColor = if (targetSection == "memo") selectedColor else memoAreaColor,
                            fileListItemColor = if (targetSection == "fileListItem") selectedColor else fileListItemColor,
                            fileListBackgroundColor = if (targetSection == "fileListBg") selectedColor else fileListBackgroundColor,
                            targetSection = targetSection,
                            selectedColor = selectedColor,
                            customBgPhotoUri = if (targetSection == "bg") activePhotoUri else loadedCustomBgPhotoUri,
                            customBgPhotoAlpha = if (targetSection == "bg") photoAlpha else loadedCustomBgPhotoAlpha,
                            customBgPhotoBlur = if (targetSection == "bg") photoBlur else loadedCustomBgPhotoBlur,
                            contentAreaPhotoUri = if (targetSection == "content") activePhotoUri else loadedContentPhotoUri,
                            contentAreaPhotoAlpha = if (targetSection == "content") photoAlpha else loadedContentPhotoAlpha,
                            contentAreaPhotoBlur = if (targetSection == "content") photoBlur else loadedContentPhotoBlur,
                            memoAreaPhotoUri = if (targetSection == "memo") activePhotoUri else loadedMemoPhotoUri,
                            memoAreaPhotoAlpha = if (targetSection == "memo") photoAlpha else loadedMemoPhotoAlpha,
                            memoAreaPhotoBlur = if (targetSection == "memo") photoBlur else loadedMemoPhotoBlur,
                            fileListBgPhotoUri = if (targetSection == "fileListBg") activePhotoUri else loadedFileListBgPhotoUri,
                            fileListBgPhotoAlpha = if (targetSection == "fileListBg") photoAlpha else loadedFileListBgPhotoAlpha,
                            fileListBgPhotoBlur = if (targetSection == "fileListBg") photoBlur else loadedFileListBgPhotoBlur,
                            fileListItemPhotoUri = if (targetSection == "fileListItem") activePhotoUri else loadedFileListItemPhotoUri,
                            fileListItemPhotoAlpha = if (targetSection == "fileListItem") photoAlpha else loadedFileListItemPhotoAlpha,
                            fileListItemPhotoBlur = if (targetSection == "fileListItem") photoBlur else loadedFileListItemPhotoBlur,
                            customBgPhotoOffsetX = if (targetSection == "bg") photoOffsetX else loadedCustomBgOffsetX,
                            customBgPhotoOffsetY = if (targetSection == "bg") photoOffsetY else loadedCustomBgOffsetY,
                            customBgPhotoScale = if (targetSection == "bg") photoScale else loadedCustomBgScale,
                            contentAreaPhotoOffsetX = if (targetSection == "content") photoOffsetX else loadedContentOffsetX,
                            contentAreaPhotoOffsetY = if (targetSection == "content") photoOffsetY else loadedContentOffsetY,
                            contentAreaPhotoScale = if (targetSection == "content") photoScale else loadedContentScale,
                            memoAreaPhotoOffsetX = if (targetSection == "memo") photoOffsetX else loadedMemoOffsetX,
                            memoAreaPhotoOffsetY = if (targetSection == "memo") photoOffsetY else loadedMemoOffsetY,
                            memoAreaPhotoScale = if (targetSection == "memo") photoScale else loadedMemoScale,
                            fileListItemPhotoOffsetX = if (targetSection == "fileListItem") photoOffsetX else loadedFileListItemOffsetX,
                            fileListItemPhotoOffsetY = if (targetSection == "fileListItem") photoOffsetY else loadedFileListItemOffsetY,
                            fileListItemPhotoScale = if (targetSection == "fileListItem") photoScale else loadedFileListItemScale,
                            fileListBgPhotoOffsetX = if (targetSection == "fileListBg") photoOffsetX else loadedFileListBgOffsetX,
                            fileListBgPhotoOffsetY = if (targetSection == "fileListBg") photoOffsetY else loadedFileListBgOffsetY,
                            fileListBgPhotoScale = if (targetSection == "fileListBg") photoScale else loadedFileListBgScale,
                            photoBackgroundMode = photoBackgroundMode
                        )
                    } else {
                        // Photo picker preview
                        val currentPhotoUri = selectedPhotoUri
                        if (currentPhotoUri != null) {
                            AppLayoutPreview(
                                photoUri = currentPhotoUri,
                                alpha = photoAlpha,
                                blur = photoBlur,
                                onPhotoOffsetXChange = { offsetX ->
                                    photoOffsetX = offsetX
                                    onPhotoOffsetXChange?.invoke(offsetX)
                                },
                                onPhotoOffsetYChange = { offsetY ->
                                    photoOffsetY = offsetY
                                    onPhotoOffsetYChange?.invoke(offsetY)
                                },
                                onPhotoScaleChange = { scale ->
                                    photoScale = scale
                                    onPhotoScaleChange?.invoke(scale)
                                },
                                customBgColor = customBgColor,
                                contentAreaColor = contentAreaColor,
                                memoAreaColor = memoAreaColor,
                                fileListItemColor = fileListItemColor,
                                fileListBackgroundColor = fileListBackgroundColor,
                                targetSection = targetSection,
                                customBgPhotoUri = if (targetSection == "bg") currentPhotoUri else loadedCustomBgPhotoUri,
                                customBgPhotoAlpha = if (targetSection == "bg") photoAlpha else loadedCustomBgPhotoAlpha,
                                customBgPhotoBlur = if (targetSection == "bg") photoBlur else loadedCustomBgPhotoBlur,
                                contentAreaPhotoUri = if (targetSection == "content") currentPhotoUri else loadedContentPhotoUri,
                                contentAreaPhotoAlpha = if (targetSection == "content") photoAlpha else loadedContentPhotoAlpha,
                                contentAreaPhotoBlur = if (targetSection == "content") photoBlur else loadedContentPhotoBlur,
                                memoAreaPhotoUri = if (targetSection == "memo") currentPhotoUri else loadedMemoPhotoUri,
                                memoAreaPhotoAlpha = if (targetSection == "memo") photoAlpha else loadedMemoPhotoAlpha,
                                memoAreaPhotoBlur = if (targetSection == "memo") photoBlur else loadedMemoPhotoBlur,
                                fileListBgPhotoUri = if (targetSection == "fileListBg") currentPhotoUri else loadedFileListBgPhotoUri,
                                fileListBgPhotoAlpha = if (targetSection == "fileListBg") photoAlpha else loadedFileListBgPhotoAlpha,
                                fileListBgPhotoBlur = if (targetSection == "fileListBg") photoBlur else loadedFileListBgPhotoBlur,
                                fileListItemPhotoUri = if (targetSection == "fileListItem") currentPhotoUri else loadedFileListItemPhotoUri,
                                fileListItemPhotoAlpha = if (targetSection == "fileListItem") photoAlpha else loadedFileListItemPhotoAlpha,
                                fileListItemPhotoBlur = if (targetSection == "fileListItem") photoBlur else loadedFileListItemPhotoBlur,
                                photoOffsetX = photoOffsetX,
                                photoOffsetY = photoOffsetY,
                                photoScale = photoScale,
                                customBgPhotoOffsetX = if (targetSection == "bg") photoOffsetX else loadedCustomBgOffsetX,
                                customBgPhotoOffsetY = if (targetSection == "bg") photoOffsetY else loadedCustomBgOffsetY,
                                customBgPhotoScale = if (targetSection == "bg") photoScale else loadedCustomBgScale,
                                contentAreaPhotoOffsetX = if (targetSection == "content") photoOffsetX else loadedContentOffsetX,
                                contentAreaPhotoOffsetY = if (targetSection == "content") photoOffsetY else loadedContentOffsetY,
                                contentAreaPhotoScale = if (targetSection == "content") photoScale else loadedContentScale,
                                memoAreaPhotoOffsetX = if (targetSection == "memo") photoOffsetX else loadedMemoOffsetX,
                                memoAreaPhotoOffsetY = if (targetSection == "memo") photoOffsetY else loadedMemoOffsetY,
                                memoAreaPhotoScale = if (targetSection == "memo") photoScale else loadedMemoScale,
                                fileListItemPhotoOffsetX = if (targetSection == "fileListItem") photoOffsetX else loadedFileListItemOffsetX,
                                fileListItemPhotoOffsetY = if (targetSection == "fileListItem") photoOffsetY else loadedFileListItemOffsetY,
                                fileListItemPhotoScale = if (targetSection == "fileListItem") photoScale else loadedFileListItemScale,
                                fileListBgPhotoOffsetX = if (targetSection == "fileListBg") photoOffsetX else loadedFileListBgOffsetX,
                                fileListBgPhotoOffsetY = if (targetSection == "fileListBg") photoOffsetY else loadedFileListBgOffsetY,
                                fileListBgPhotoScale = if (targetSection == "fileListBg") photoScale else loadedFileListBgScale,
                                photoBackgroundMode = photoBackgroundMode
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(32.dp))
                                    Text("No photo", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Scrollable content below preview
                Column(modifier = Modifier.verticalScroll(scrollState)) {
                if (selectedTabIndex == 0) {
                    // Color Picker Tab Content
                    ColorPickerContent(
                        selectedColor = selectedColor,
                        red = red,
                        green = green,
                        blue = blue,
                        alpha = alpha,
                        blur = blur,
                        hue = hue,
                        sv = sv,
                        onColorChange = { newColor ->
                            selectedColor = newColor
                            red = newColor.red
                            green = newColor.green
                            blue = newColor.blue
                            alpha = newColor.alpha
                            hue = newColor.hue()
                            sv = svFromColor(newColor)
                        },
                        onRedChange = { v -> red = v; selectedColor = Color(red, green, blue, alpha) },
                        onGreenChange = { v -> green = v; selectedColor = Color(red, green, blue, alpha) },
                        onBlueChange = { v -> blue = v; selectedColor = Color(red, green, blue, alpha) },
                        onAlphaChange = { v -> alpha = v; selectedColor = Color(red, green, blue, alpha) },
                        onBlurChange = { blur = it },
                        onHueChange = { 
                            hue = it * 360f
                            selectedColor = Color.hsv(hue, 1f, 1f)
                            red = selectedColor.red
                            green = selectedColor.green
                            blue = selectedColor.blue
                        },
                        onSvChange = { newSv ->
                            sv = newSv
                            val c = Color.hsv(hue, newSv.first, newSv.second)
                            selectedColor = c
                            red = c.red
                            green = c.green
                            blue = c.blue
                            alpha = c.alpha
                        },
                        // Pass preview parameters
                        customBgColor = customBgColor,
                        contentAreaColor = contentAreaColor,
                        memoAreaColor = memoAreaColor,
                        fileListItemColor = fileListItemColor,
                        fileListBackgroundColor = fileListBackgroundColor,
                        targetSection = targetSection,
                        customBgPhotoUri = if (targetSection == "bg") activePhotoUri else loadedCustomBgPhotoUri,
                        customBgPhotoAlpha = if (targetSection == "bg") photoAlpha else loadedCustomBgPhotoAlpha,
                        customBgPhotoBlur = if (targetSection == "bg") photoBlur else loadedCustomBgPhotoBlur,
                        contentAreaPhotoUri = if (targetSection == "content") activePhotoUri else loadedContentPhotoUri,
                        contentAreaPhotoAlpha = if (targetSection == "content") photoAlpha else loadedContentPhotoAlpha,
                        contentAreaPhotoBlur = if (targetSection == "content") photoBlur else loadedContentPhotoBlur,
                        memoAreaPhotoUri = if (targetSection == "memo") activePhotoUri else loadedMemoPhotoUri,
                        memoAreaPhotoAlpha = if (targetSection == "memo") photoAlpha else loadedMemoPhotoAlpha,
                        memoAreaPhotoBlur = if (targetSection == "memo") photoBlur else loadedMemoPhotoBlur,
                        fileListBgPhotoUri = if (targetSection == "fileListBg") activePhotoUri else loadedFileListBgPhotoUri,
                        fileListBgPhotoAlpha = if (targetSection == "fileListBg") photoAlpha else loadedFileListBgPhotoAlpha,
                        fileListBgPhotoBlur = if (targetSection == "fileListBg") photoBlur else loadedFileListBgPhotoBlur,
                        fileListItemPhotoUri = if (targetSection == "fileListItem") activePhotoUri else loadedFileListItemPhotoUri,
                        fileListItemPhotoAlpha = if (targetSection == "fileListItem") photoAlpha else loadedFileListItemPhotoAlpha,
                        fileListItemPhotoBlur = if (targetSection == "fileListItem") photoBlur else loadedFileListItemPhotoBlur
                    )
                } else {
                    // Photo Picker Tab Content
                    PhotoPickerContent(
                        selectedPhotoUri = selectedPhotoUri,
                        alpha = photoAlpha,
                        blur = photoBlur,
                        onPhotoSelected = { uri -> 
                            selectedPhotoUri = uri
                            onPhotoUriChange?.invoke(uri)
                        },
                        onAlphaChange = { photoAlpha = it },
                        onBlurChange = { 
                            photoBlur = it
                            onPhotoBlurChange?.invoke(it)
                        },
                        // Pass current colors for preview
                        customBgColor = customBgColor,
                        contentAreaColor = contentAreaColor,
                        memoAreaColor = memoAreaColor,
                        fileListItemColor = fileListItemColor,
                        fileListBackgroundColor = fileListBackgroundColor,
                        targetSection = targetSection,
                        // Pass current photo URIs for all sections - use activePhotoUri for active section
                        customBgPhotoUri = if (targetSection == "bg") activePhotoUri else loadedCustomBgPhotoUri,
                        customBgPhotoAlpha = if (targetSection == "bg") photoAlpha else loadedCustomBgPhotoAlpha,
                        customBgPhotoBlur = if (targetSection == "bg") photoBlur else loadedCustomBgPhotoBlur,
                        contentAreaPhotoUri = if (targetSection == "content") activePhotoUri else loadedContentPhotoUri,
                        contentAreaPhotoAlpha = if (targetSection == "content") photoAlpha else loadedContentPhotoAlpha,
                        contentAreaPhotoBlur = if (targetSection == "content") photoBlur else loadedContentPhotoBlur,
                        memoAreaPhotoUri = if (targetSection == "memo") activePhotoUri else loadedMemoPhotoUri,
                        memoAreaPhotoAlpha = if (targetSection == "memo") photoAlpha else loadedMemoPhotoAlpha,
                        memoAreaPhotoBlur = if (targetSection == "memo") photoBlur else loadedMemoPhotoBlur,
                        fileListBgPhotoUri = if (targetSection == "fileListBg") activePhotoUri else loadedFileListBgPhotoUri,
                        fileListBgPhotoAlpha = if (targetSection == "fileListBg") photoAlpha else loadedFileListBgPhotoAlpha,
                        fileListBgPhotoBlur = if (targetSection == "fileListBg") photoBlur else loadedFileListBgPhotoBlur,
                        fileListItemPhotoUri = if (targetSection == "fileListItem") activePhotoUri else loadedFileListItemPhotoUri,
                        fileListItemPhotoAlpha = if (targetSection == "fileListItem") photoAlpha else loadedFileListItemPhotoAlpha,
                        fileListItemPhotoBlur = if (targetSection == "fileListItem") photoBlur else loadedFileListItemPhotoBlur,
                        // Photo background mode
                        photoBackgroundMode = photoBackgroundMode,
                        onPhotoBackgroundModeChange = { newMode ->
                            photoBackgroundMode = newMode
                            onPhotoBackgroundModeChange?.invoke(newMode)
                        }
                    )
                }
                }
            }
        }
    )
    
    // Full-screen photo editor dialog
    if (showFullScreenEditor && selectedPhotoUri != null) {
        FullScreenPhotoEditor(
            photoUri = selectedPhotoUri!!,
            initialOffsetX = photoOffsetX,
            initialOffsetY = photoOffsetY,
            initialScale = photoScale,
            initialBlur = photoBlur,
            onDismiss = { showFullScreenEditor = false },
            onApply = { newOffsetX, newOffsetY, newScale, newBlur ->
                photoOffsetX = newOffsetX
                photoOffsetY = newOffsetY
                photoScale = newScale
                photoBlur = newBlur
                
                // Notify parent of changes
                onPhotoOffsetXChange?.invoke(newOffsetX)
                onPhotoOffsetYChange?.invoke(newOffsetY)
                onPhotoScaleChange?.invoke(newScale)
                onPhotoBlurChange?.invoke(newBlur)
                
                showFullScreenEditor = false
            },
            targetSection = targetSection,
            onPositionChange = { newOffsetX, newOffsetY, newScale, newBlur ->
                // Update local state in real-time during gestures
                photoOffsetX = newOffsetX
                photoOffsetY = newOffsetY
                photoScale = newScale
                photoBlur = newBlur
                
                // Notify parent immediately
                onPhotoOffsetXChange?.invoke(newOffsetX)
                onPhotoOffsetYChange?.invoke(newOffsetY)
                onPhotoScaleChange?.invoke(newScale)
                onPhotoBlurChange?.invoke(newBlur)
            },
            onTest = onTestInApp
        )
    }
    
}

@Composable
fun ColorPickerContent(
    selectedColor: Color,
    red: Float,
    green: Float,
    blue: Float,
    alpha: Float,
    blur: Float,
    hue: Float,
    sv: Pair<Float, Float>,
    onColorChange: (Color) -> Unit,
    onRedChange: (Float) -> Unit,
    onGreenChange: (Float) -> Unit,
    onBlueChange: (Float) -> Unit,
    onAlphaChange: (Float) -> Unit,
    onBlurChange: (Float) -> Unit,
    onHueChange: (Float) -> Unit,
    onSvChange: (Pair<Float, Float>) -> Unit,
    // Preview parameters
    customBgColor: Color? = null,
    contentAreaColor: Color? = null,
    memoAreaColor: Color? = null,
    fileListItemColor: Color? = null,
    fileListBackgroundColor: Color? = null,
    targetSection: String = "bg",
    customBgPhotoUri: String? = null,
    customBgPhotoAlpha: Float = 1f,
    customBgPhotoBlur: Float = 0f,
    contentAreaPhotoUri: String? = null,
    contentAreaPhotoAlpha: Float = 1f,
    contentAreaPhotoBlur: Float = 0f,
    memoAreaPhotoUri: String? = null,
    memoAreaPhotoAlpha: Float = 1f,
    memoAreaPhotoBlur: Float = 0f,
    fileListBgPhotoUri: String? = null,
    fileListBgPhotoAlpha: Float = 1f,
    fileListBgPhotoBlur: Float = 0f,
    fileListItemPhotoUri: String? = null,
    fileListItemPhotoAlpha: Float = 1f,
    fileListItemPhotoBlur: Float = 0f
) {
    // Preview is now in parent dialog - this component only contains controls
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var hexInput by remember(selectedColor) { mutableStateOf(selectedColor.toHexString()) }
    var hasHexError by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            androidx.compose.material3.OutlinedTextField(
                value = hexInput,
                onValueChange = { value ->
                    val sanitized = value
                        .removePrefix("#")
                        .filter { it.isDigit() || it.uppercaseChar() in 'A'..'F' }
                        .uppercase()
                        .take(8)

                    hexInput = sanitized
                    val parsedColor = parseHexToColorOrNull(sanitized)
                    if (parsedColor != null) {
                        onColorChange(parsedColor)
                        hasHexError = false
                    } else {
                        hasHexError = sanitized.isNotEmpty() && sanitized.length != 6 && sanitized.length != 8
                    }
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Hex") },
                prefix = { Text("#") },
                isError = hasHexError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
            )

            TextButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString("#${selectedColor.toHexString()}"))
                    Toast.makeText(context, "Color code copied", Toast.LENGTH_SHORT).show()
                }
            ) {
                Text("Copy")
            }

            TextButton(
                onClick = {
                    val pasted = clipboardManager.getText()?.text?.toString().orEmpty()
                    val sanitized = pasted
                        .trim()
                        .removePrefix("#")
                        .filter { it.isDigit() || it.uppercaseChar() in 'A'..'F' }
                        .uppercase()
                        .take(8)

                    hexInput = sanitized
                    val parsedColor = parseHexToColorOrNull(sanitized)
                    if (parsedColor != null) {
                        onColorChange(parsedColor)
                        hasHexError = false
                    } else {
                        hasHexError = true
                        Toast.makeText(context, "Invalid hex color", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Paste")
            }
        }

        if (hasHexError) {
            Text(
                text = "Use 6 or 8 hex digits (RRGGBB or AARRGGBB)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(8.dp))

        // Hue slider
        Text("Hue", style = MaterialTheme.typography.labelSmall)
        HueSlider(
            hue = hue,
            onHueChange = { newHue -> onHueChange(newHue / 360f) }
        )
        
        Spacer(Modifier.height(8.dp))
        Text("Spectrum Picker", style = MaterialTheme.typography.labelSmall)
        ColorSpectrumPicker(
            hue = hue,
            modifier = Modifier.size(120.dp),
            sv = sv,
            onSvChanged = onSvChange
        )
        
        Spacer(Modifier.height(6.dp))
        
        // Advanced Picker dropdown section
        var advancedPickerExpanded by remember { mutableStateOf(false) }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { advancedPickerExpanded = !advancedPickerExpanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Advanced Picker",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (advancedPickerExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (advancedPickerExpanded) "Collapse" else "Expand"
                    )
                }
                
                AnimatedVisibility(
                    visible = advancedPickerExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        ColorSlider("Red", red, onRedChange, Color.Red)
                        ColorSlider("Green", green, onGreenChange, Color.Green)
                        ColorSlider("Blue", blue, onBlueChange, Color.Blue)
                    }
                }
            }
        }
        
        Spacer(Modifier.height(8.dp))
        ColorSlider("Alpha", alpha, onAlphaChange, Color.Gray)
        
        // Blur slider for glassy effect
        if (alpha < 1f) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Blur", modifier = Modifier.width(48.dp))
                    Slider(
                        value = blur,
                        onValueChange = onBlurChange,
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.LightGray,
                            activeTrackColor = Color.LightGray,
                            inactiveTrackColor = Color.LightGray.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text("${(blur * 50).roundToInt()}dp", modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
                }
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Effect Button (placeholder for now)
        Button(
            onClick = { /* TODO: Implement effect functionality */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Effect")
        }
        
        Spacer(Modifier.height(8.dp))
        Surface(
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.CenterHorizontally)
                .border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
            color = selectedColor,
            shape = MaterialTheme.shapes.small,
            tonalElevation = 2.dp
        ) {}
        Spacer(Modifier.height(8.dp))
        Text("Selected: #" + selectedColor.toHexString(), modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PhotoPickerContent(
    selectedPhotoUri: String?,
    alpha: Float,
    blur: Float,
    onPhotoSelected: (String) -> Unit,
    onAlphaChange: (Float) -> Unit,
    onBlurChange: (Float) -> Unit,
    // Current app colors for preview
    customBgColor: Color? = null,
    contentAreaColor: Color? = null,
    memoAreaColor: Color? = null,
    fileListItemColor: Color? = null,
    fileListBackgroundColor: Color? = null,
    targetSection: String = "bg", // Which section is being edited
    // Current photo URIs for all sections (to show in preview)
    customBgPhotoUri: String? = null,
    customBgPhotoAlpha: Float = 1f,
    customBgPhotoBlur: Float = 0f,
    contentAreaPhotoUri: String? = null,
    contentAreaPhotoAlpha: Float = 1f,
    contentAreaPhotoBlur: Float = 0f,
    memoAreaPhotoUri: String? = null,
    memoAreaPhotoAlpha: Float = 1f,
    memoAreaPhotoBlur: Float = 0f,
    fileListBgPhotoUri: String? = null,
    fileListBgPhotoAlpha: Float = 1f,
    fileListBgPhotoBlur: Float = 0f,
    fileListItemPhotoUri: String? = null,
    fileListItemPhotoAlpha: Float = 1f,
    fileListItemPhotoBlur: Float = 0f,
    // Photo background mode
    photoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
    onPhotoBackgroundModeChange: ((com.j4.texter2025.data.PhotoBackgroundMode) -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoadingPhoto by remember { mutableStateOf(false) }
    
    // Photo picker launcher - saves photo to internal storage for persistence
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            isLoadingPhoto = true
            scope.launch(Dispatchers.Main) {
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
                    } catch (e: SecurityException) {
                    }
                    onPhotoSelected(selectedUri.toString())
                }
                isLoadingPhoto = false
            }
        }
    }
    // Preview is now in parent dialog - this component only contains controls
    Column {
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

        // Photo selection button
        Button(
            onClick = { 
                try {
                    photoPickerLauncher.launch("image/*")
                } catch (e: Exception) {
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoadingPhoto
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text(if (isLoadingPhoto) "Importing..." else "Select Photo")
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Alpha and Blur sliders
        ColorSlider("Alpha", alpha, onAlphaChange, Color.Gray)
        
        Spacer(Modifier.height(6.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Blur", modifier = Modifier.width(42.dp), style = MaterialTheme.typography.bodySmall)
            Slider(
                value = blur,
                onValueChange = onBlurChange,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = Color.LightGray,
                    activeTrackColor = Color.LightGray,
                    inactiveTrackColor = Color.LightGray.copy(alpha = 0.3f)
                ),
                modifier = Modifier.weight(1f)
            )
            Text("${(blur * 50).roundToInt()}dp", modifier = Modifier.width(36.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall)
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Background Mode selection (only show if photo is selected and callback is provided)
        if (selectedPhotoUri != null && selectedPhotoUri.isNotEmpty() && onPhotoBackgroundModeChange != null) {
            Text(
                "Background Mode",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.material3.FilterChip(
                    selected = photoBackgroundMode == com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
                    onClick = {
                        onPhotoBackgroundModeChange(com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC)
                    },
                    label = { Text("Dynamic", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.weight(1f)
                )
                androidx.compose.material3.FilterChip(
                    selected = photoBackgroundMode == com.j4.texter2025.data.PhotoBackgroundMode.STATIC_ZOOM,
                    onClick = {
                        onPhotoBackgroundModeChange(com.j4.texter2025.data.PhotoBackgroundMode.STATIC_ZOOM)
                    },
                    label = { Text("Auto-Zoom", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
        }
        
        // Instructions for direct positioning on preview
        if (targetSection == "fileListBg" && selectedPhotoUri != null && selectedPhotoUri.isNotEmpty()) {
            Text(
                "💡 Drag to move • Pinch to zoom on preview",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
        }
        
        // Effect Button (placeholder for now)
        Button(
            onClick = { /* TODO: Implement effect functionality */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Effect")
        }
    }
}

@Composable
fun AppLayoutPreview(
    photoUri: String,
    alpha: Float = 1f,
    blur: Float = 0f,
    onPhotoOffsetXChange: ((Float) -> Unit)? = null,
    onPhotoOffsetYChange: ((Float) -> Unit)? = null,
    onPhotoScaleChange: ((Float) -> Unit)? = null,
    customBgColor: Color? = null,
    contentAreaColor: Color? = null,
    memoAreaColor: Color? = null,
    fileListItemColor: Color? = null,
    fileListBackgroundColor: Color? = null,
    targetSection: String = "bg", // Which section is being edited: "bg", "content", "memo", "fileListItem", "fileListBg"
    selectedColor: Color = Color.White, // The currently selected color in the color picker
    // Current photo URIs for all sections (to show in preview)
    customBgPhotoUri: String? = null,
    customBgPhotoAlpha: Float = 1f,
    customBgPhotoBlur: Float = 0f,
    contentAreaPhotoUri: String? = null,
    contentAreaPhotoAlpha: Float = 1f,
    contentAreaPhotoBlur: Float = 0f,
    memoAreaPhotoUri: String? = null,
    memoAreaPhotoAlpha: Float = 1f,
    memoAreaPhotoBlur: Float = 0f,
    fileListBgPhotoUri: String? = null,
    fileListBgPhotoAlpha: Float = 1f,
    fileListBgPhotoBlur: Float = 0f,
    fileListItemPhotoUri: String? = null,
    fileListItemPhotoAlpha: Float = 1f,
    fileListItemPhotoBlur: Float = 0f,
    // Photo positioning parameters for all areas
    photoOffsetX: Float = 0f,
    photoOffsetY: Float = 0f,
    photoScale: Float = 1f,
    photoRotation: Float = 0f,
    customBgPhotoOffsetX: Float = 0f,
    customBgPhotoOffsetY: Float = 0f,
    customBgPhotoScale: Float = 1f,
    customBgPhotoRotation: Float = 0f,
    contentAreaPhotoOffsetX: Float = 0f,
    contentAreaPhotoOffsetY: Float = 0f,
    contentAreaPhotoScale: Float = 1f,
    contentAreaPhotoRotation: Float = 0f,
    memoAreaPhotoOffsetX: Float = 0f,
    memoAreaPhotoOffsetY: Float = 0f,
    memoAreaPhotoScale: Float = 1f,
    memoAreaPhotoRotation: Float = 0f,
    fileListItemPhotoOffsetX: Float = 0f,
    fileListItemPhotoOffsetY: Float = 0f,
    fileListItemPhotoScale: Float = 1f,
    fileListItemPhotoRotation: Float = 0f,
    fileListBgPhotoOffsetX: Float = 0f,
    fileListBgPhotoOffsetY: Float = 0f,
    fileListBgPhotoScale: Float = 1f,
    fileListBgPhotoRotation: Float = 0f,
    photoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
    ) {
        // Background layer - show background photo if it exists, regardless of which section is being edited
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Determine which photo to show based on what's being edited
            val backgroundPhotoToShow = if (targetSection == "bg" && photoUri.isNotEmpty()) {
                photoUri // Show the photo being edited
            } else {
                customBgPhotoUri // Show the existing background photo
            }
            
            val backgroundPhotoAlphaToShow = if (targetSection == "bg") {
                alpha // Use the alpha being edited
            } else {
                customBgPhotoAlpha // Use the existing background photo alpha
            }
            
            if (backgroundPhotoToShow != null && backgroundPhotoToShow.isNotEmpty()) {
                val backgroundBaseColor = Color.Transparent
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(backgroundBaseColor)
                )

                val painter = rememberHighQualityPhotoPainter(
                    photoUri = backgroundPhotoToShow
                )
                
                val isLoaded = painter.state is coil.compose.AsyncImagePainter.State.Success
                val imageAlpha = when (painter.state) {
                    is coil.compose.AsyncImagePainter.State.Error -> 0f
                    is coil.compose.AsyncImagePainter.State.Success -> backgroundPhotoAlphaToShow
                    else -> backgroundPhotoAlphaToShow
                }
                val imgSize = painter.intrinsicSize
                val bgCropRatio = if (isLoaded && imgSize.width > 0 && imgSize.height > 0 && imgSize.width.isFinite() && imgSize.height.isFinite()) imgSize else null
                
                val backgroundBlurToShow = if (targetSection == "bg") blur else customBgPhotoBlur
                
                // Use positioning values - when editing bg use gesture state, otherwise use saved values
                var currentOffsetX by remember(customBgPhotoOffsetX) { 
                    mutableStateOf(customBgPhotoOffsetX) 
                }
                var currentOffsetY by remember(customBgPhotoOffsetY) { 
                    mutableStateOf(customBgPhotoOffsetY) 
                }
                var currentScale by remember(customBgPhotoScale) { 
                    mutableStateOf(customBgPhotoScale) 
                }
                
                // Update state when parameters change
                LaunchedEffect(customBgPhotoOffsetX, customBgPhotoOffsetY, customBgPhotoScale) {
                    currentOffsetX = customBgPhotoOffsetX
                    currentOffsetY = customBgPhotoOffsetY
                    currentScale = customBgPhotoScale
                }
                
                val bgMode = if (targetSection == "bg") photoBackgroundMode else com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC
                androidx.compose.foundation.Image(
                    painter = painter,
                    contentDescription = "Background photo preview",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = if (targetSection == "bg") photoRotation else customBgPhotoRotation
                            when (bgMode) {
                                com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC -> {
                                    val ratio = if (bgCropRatio != null && size.width > 0 && size.height > 0) {
                                        maxOf(size.width / bgCropRatio.width, size.height / bgCropRatio.height) /
                                            minOf(size.width / bgCropRatio.width, size.height / bgCropRatio.height)
                                    } else 1f
                                    val effectiveScale = currentScale * ratio
                                    scaleX = effectiveScale
                                    scaleY = effectiveScale
                                    translationX = -currentOffsetX * size.width
                                    translationY = currentOffsetY * size.height
                                }
                                com.j4.texter2025.data.PhotoBackgroundMode.STATIC_ZOOM -> {
                                    val autoScale = if (bgCropRatio != null && size.width > 0 && size.height > 0) {
                                        maxOf(size.width / bgCropRatio.width, size.height / bgCropRatio.height)
                                    } else 1f
                                    val effectiveScale = autoScale * currentScale
                                    scaleX = effectiveScale
                                    scaleY = effectiveScale
                                    translationX = 0f
                                    translationY = 0f
                                }
                            }
                        }
                        .then(
                            if (targetSection == "bg" && onPhotoOffsetXChange != null && bgMode == com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC) {
                                Modifier
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            val maxRange = currentScale * 2f
                                            currentOffsetX = (currentOffsetX - dragAmount.x / size.width.toFloat()).coerceIn(-maxRange, maxRange)
                                            currentOffsetY = (currentOffsetY + dragAmount.y / size.height.toFloat()).coerceIn(-maxRange, maxRange)
                                            onPhotoOffsetXChange.invoke(currentOffsetX)
                                            onPhotoOffsetYChange?.invoke(currentOffsetY)
                                        }
                                    }
                                    .pointerInput(Unit) {
                                        detectTransformGestures { centroid, pan, zoom, rotation ->
                                            currentScale = (currentScale * zoom).coerceIn(0.2f, 5f)
                                            val maxRange = currentScale * 2f
                                            currentOffsetX = (currentOffsetX - pan.x / size.width.toFloat()).coerceIn(-maxRange, maxRange)
                                            currentOffsetY = (currentOffsetY + pan.y / size.height.toFloat()).coerceIn(-maxRange, maxRange)
                                            onPhotoOffsetXChange.invoke(currentOffsetX)
                                            onPhotoOffsetYChange?.invoke(currentOffsetY)
                                            onPhotoScaleChange?.invoke(currentScale)
                                        }
                                    }
                            } else Modifier
                        )
                        .applyPhotoAlpha(imageAlpha)
                        .applyPhotoPreviewBlur(backgroundBlurToShow),
                    contentScale = when (bgMode) {
                        com.j4.texter2025.data.PhotoBackgroundMode.STATIC_ZOOM -> ContentScale.Fit
                        else -> if (isLoaded) ContentScale.Fit else ContentScale.Crop
                    }
                )
                
                // Photo selected indicator - only show when editing background
                if (targetSection == "bg") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .background(
                                Color.White.copy(alpha = 0.9f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = "Photo selected",
                                modifier = Modifier.size(12.dp),
                                tint = Color(0xFF1976D2)
                            )
                            Text(
                                "Photo",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF1565C0)
                            )
                        }
                    }
                }
            } else {
                // Show selected color or fallback color when no photo is selected
                val bgColorToShow = if (targetSection == "bg") {
                    // When editing background, show the currently selected color
                    selectedColor.copy(alpha = alpha)
                } else {
                    // Otherwise show the current background color
                    customBgColor ?: Color(0xFF4A90E2)
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bgColorToShow)
                )
            }
        }
        
        // App layout overlay - Split view: File list left, Note view right
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            // Top bar simulation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        RoundedCornerShape(4.dp)
                    )
            ) {
                Text(
                    "TeXter",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(horizontal = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(3.dp))
            
            // Split layout: File list (left) and Note view (right)
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // File list area (left side - 30% width, smaller to give more space to content)
                Box(
                    modifier = Modifier
                        .weight(0.3f)
                        .fillMaxHeight()
                ) {
                    // Photo layer - show current photo being edited OR existing file list bg photo
                    val fileListBgPhotoToShow = if (targetSection == "fileListBg") photoUri else (fileListBgPhotoUri ?: "")
                    val fileListBgPhotoAlphaToShow = if (targetSection == "fileListBg") alpha else fileListBgPhotoAlpha
                    
                    // Keep a stable local base color under the photo so alpha changes
                    // don't reveal blurred layers from other sections.
                    val fileListBgBaseColor = if (fileListBgPhotoToShow.isNotEmpty()) {
                        Color.Transparent
                    } else {
                        fileListBackgroundColor ?: MaterialTheme.colorScheme.surface
                    }
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                fileListBgBaseColor,
                                RoundedCornerShape(4.dp)
                            )
                    )
                    
                    if (fileListBgPhotoToShow.isNotEmpty()) {
                        val painter = rememberHighQualityPhotoPainter(
                            photoUri = fileListBgPhotoToShow
                        )
                        val isLoadedFlBg = painter.state is coil.compose.AsyncImagePainter.State.Success
                        val imageAlpha = when (painter.state) {
                            is coil.compose.AsyncImagePainter.State.Error -> 0f
                            is coil.compose.AsyncImagePainter.State.Success -> fileListBgPhotoAlphaToShow
                            else -> fileListBgPhotoAlphaToShow
                        }
                        val flBgImgSize = painter.intrinsicSize
                        val flBgCropRatio = if (isLoadedFlBg && flBgImgSize.width > 0 && flBgImgSize.height > 0 && flBgImgSize.width.isFinite() && flBgImgSize.height.isFinite()) flBgImgSize else null
                        val fileListBgBlurToShow = if (targetSection == "fileListBg") blur else fileListBgPhotoBlur
                        
                        // Use positioning values - when editing fileListBg use gesture state, otherwise use saved values
                        var currentOffsetX by remember(fileListBgPhotoOffsetX) { 
                            mutableStateOf(fileListBgPhotoOffsetX) 
                        }
                        var currentOffsetY by remember(fileListBgPhotoOffsetY) { 
                            mutableStateOf(fileListBgPhotoOffsetY) 
                        }
                        var currentScale by remember(fileListBgPhotoScale) { 
                            mutableStateOf(fileListBgPhotoScale) 
                        }
                        
                        // Update state when parameters change
                        LaunchedEffect(fileListBgPhotoOffsetX, fileListBgPhotoOffsetY, fileListBgPhotoScale) {
                            currentOffsetX = fileListBgPhotoOffsetX
                            currentOffsetY = fileListBgPhotoOffsetY
                            currentScale = fileListBgPhotoScale
                        }
                        
                        val flBgMode = if (targetSection == "fileListBg") photoBackgroundMode else com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC
                        androidx.compose.foundation.Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(4.dp))
                                .graphicsLayer {
                                    rotationZ = if (targetSection == "fileListBg") photoRotation else fileListBgPhotoRotation
                                    when (flBgMode) {
                                        com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC -> {
                                            val ratio = if (flBgCropRatio != null && size.width > 0 && size.height > 0) {
                                                maxOf(size.width / flBgCropRatio.width, size.height / flBgCropRatio.height) /
                                                    minOf(size.width / flBgCropRatio.width, size.height / flBgCropRatio.height)
                                            } else 1f
                                            val effectiveScale = currentScale * ratio
                                            scaleX = effectiveScale
                                            scaleY = effectiveScale
                                            translationX = -currentOffsetX * size.width
                                            translationY = currentOffsetY * size.height
                                        }
                                        com.j4.texter2025.data.PhotoBackgroundMode.STATIC_ZOOM -> {
                                            val autoScale = if (flBgCropRatio != null && size.width > 0 && size.height > 0) {
                                                maxOf(size.width / flBgCropRatio.width, size.height / flBgCropRatio.height)
                                            } else 1f
                                            val effectiveScale = autoScale * currentScale
                                            scaleX = effectiveScale
                                            scaleY = effectiveScale
                                            translationX = 0f
                                            translationY = 0f
                                        }
                                    }
                                }
                                .then(
                                    // Add gestures only when editing file list background
                                    if (targetSection == "fileListBg" && onPhotoOffsetXChange != null && flBgMode == com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC) {
                                        Modifier
                                            .pointerInput(Unit) {
                                                detectDragGestures { change, dragAmount ->
                                                    change.consume()
                                                    val maxRange = currentScale * 2f
                                                    currentOffsetX = (currentOffsetX - dragAmount.x / size.width.toFloat()).coerceIn(-maxRange, maxRange)
                                                    currentOffsetY = (currentOffsetY + dragAmount.y / size.height.toFloat()).coerceIn(-maxRange, maxRange)
                                                    onPhotoOffsetXChange.invoke(currentOffsetX)
                                                    onPhotoOffsetYChange?.invoke(currentOffsetY)
                                                }
                                            }
                                            .pointerInput(Unit) {
                                                detectTransformGestures { centroid: androidx.compose.ui.geometry.Offset, pan: androidx.compose.ui.geometry.Offset, zoom: Float, rotation: Float ->
                                                    // Apply zoom
                                                    currentScale = (currentScale * zoom).coerceIn(0.2f, 5f)
                                                    val maxRange = currentScale * 2f
                                                    currentOffsetX = (currentOffsetX - pan.x / size.width.toFloat()).coerceIn(-maxRange, maxRange)
                                                    currentOffsetY = (currentOffsetY + pan.y / size.height.toFloat()).coerceIn(-maxRange, maxRange)
                                                    onPhotoOffsetXChange.invoke(currentOffsetX)
                                                    onPhotoOffsetYChange?.invoke(currentOffsetY)
                                                    onPhotoScaleChange?.invoke(currentScale)
                                                }
                                            }
                                    } else Modifier
                                )
                                .applyPhotoAlpha(imageAlpha)
                                .applyPhotoPreviewBlur(fileListBgBlurToShow),
                            contentScale = when (flBgMode) {
                                com.j4.texter2025.data.PhotoBackgroundMode.STATIC_ZOOM -> ContentScale.Fit
                                else -> if (isLoadedFlBg) ContentScale.Fit else ContentScale.Crop
                            }
                        )
                    }
                    
                    Box(modifier = Modifier.padding(3.dp)) {
                    Column {
                        Text(
                            "Files",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        repeat(5) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                            ) {
                                // Photo layer - show current photo being edited OR existing file list item photo
                                val fileListItemPhotoToShow = if (targetSection == "fileListItem") photoUri else (fileListItemPhotoUri ?: "")
                                val fileListItemPhotoAlphaToShow = if (targetSection == "fileListItem") alpha else fileListItemPhotoAlpha
                                
                                // Keep a stable local base color under the photo so alpha
                                // changes don't reveal blurred layers from below.
                                val fileListItemBaseColor = if (fileListItemPhotoToShow.isNotEmpty()) {
                                    Color.Transparent
                                } else {
                                    fileListItemColor ?: MaterialTheme.colorScheme.surfaceVariant
                                }
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(
                                            fileListItemBaseColor,
                                            RoundedCornerShape(2.dp)
                                        )
                                )
                                
                                if (fileListItemPhotoToShow.isNotEmpty()) {
                                    val painter = rememberHighQualityPhotoPainter(
                                        photoUri = fileListItemPhotoToShow
                                    )
                                    val isLoadedFlItem = painter.state is coil.compose.AsyncImagePainter.State.Success
                                    val imageAlpha = when (painter.state) {
                                        is coil.compose.AsyncImagePainter.State.Error -> 0f
                                        is coil.compose.AsyncImagePainter.State.Success -> fileListItemPhotoAlphaToShow
                                        else -> fileListItemPhotoAlphaToShow
                                    }
                                    val flItemImgSize = painter.intrinsicSize
                                    val flItemCropRatio = if (isLoadedFlItem && flItemImgSize.width > 0 && flItemImgSize.height > 0 && flItemImgSize.width.isFinite() && flItemImgSize.height.isFinite()) flItemImgSize else null
                                    val fileListItemBlurToShow = if (targetSection == "fileListItem") blur else fileListItemPhotoBlur
                                    
                                    // Use positioning values - when editing fileListItem use gesture state, otherwise use saved values
                                    var currentOffsetX by remember(fileListItemPhotoOffsetX) { 
                                        mutableStateOf(fileListItemPhotoOffsetX) 
                                    }
                                    var currentOffsetY by remember(fileListItemPhotoOffsetY) { 
                                        mutableStateOf(fileListItemPhotoOffsetY) 
                                    }
                                    var currentScale by remember(fileListItemPhotoScale) { 
                                        mutableStateOf(fileListItemPhotoScale) 
                                    }
                                    
                                    // Update state when parameters change
                                    LaunchedEffect(fileListItemPhotoOffsetX, fileListItemPhotoOffsetY, fileListItemPhotoScale) {
                                        currentOffsetX = fileListItemPhotoOffsetX
                                        currentOffsetY = fileListItemPhotoOffsetY
                                        currentScale = fileListItemPhotoScale
                                    }
                                    
                                    val flItemMode = if (targetSection == "fileListItem") photoBackgroundMode else com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC
                                    val flItemBaseModifier = Modifier
                                        .matchParentSize()
                                        .clip(RoundedCornerShape(2.dp))
                                        .graphicsLayer {
                                            rotationZ = if (targetSection == "fileListItem") photoRotation else fileListItemPhotoRotation
                                            when (flItemMode) {
                                                com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC -> {
                                                    val ratio = if (flItemCropRatio != null && size.width > 0 && size.height > 0) {
                                                        maxOf(size.width / flItemCropRatio.width, size.height / flItemCropRatio.height) /
                                                            minOf(size.width / flItemCropRatio.width, size.height / flItemCropRatio.height)
                                                    } else 1f
                                                    val effectiveScale = currentScale * ratio
                                                    scaleX = effectiveScale
                                                    scaleY = effectiveScale
                                                    translationX = -currentOffsetX * size.width
                                                    translationY = currentOffsetY * size.height
                                                }
                                                com.j4.texter2025.data.PhotoBackgroundMode.STATIC_ZOOM -> {
                                                    val autoScale = if (flItemCropRatio != null && size.width > 0 && size.height > 0) {
                                                        maxOf(size.width / flItemCropRatio.width, size.height / flItemCropRatio.height)
                                                    } else 1f
                                                    val effectiveScale = autoScale * currentScale
                                                    scaleX = effectiveScale
                                                    scaleY = effectiveScale
                                                    translationX = 0f
                                                    translationY = 0f
                                                }
                                            }
                                        }

                                    val flItemGestureModifier = if (targetSection == "fileListItem" && onPhotoOffsetXChange != null && flItemMode == com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC) {
                                        Modifier
                                            .pointerInput(Unit) {
                                                detectDragGestures { change, dragAmount ->
                                                    change.consume()
                                                    val maxRange = currentScale * 2f
                                                    currentOffsetX = (currentOffsetX - dragAmount.x / size.width.toFloat()).coerceIn(-maxRange, maxRange)
                                                    currentOffsetY = (currentOffsetY + dragAmount.y / size.height.toFloat()).coerceIn(-maxRange, maxRange)
                                                    onPhotoOffsetXChange.invoke(currentOffsetX)
                                                    onPhotoOffsetYChange?.invoke(currentOffsetY)
                                                }
                                            }
                                            .pointerInput(Unit) {
                                                detectTransformGestures { centroid, pan, zoom, rotation ->
                                                    currentScale = (currentScale * zoom).coerceIn(0.2f, 5f)
                                                    val maxRange = currentScale * 2f
                                                    currentOffsetX = (currentOffsetX - pan.x / size.width.toFloat()).coerceIn(-maxRange, maxRange)
                                                    currentOffsetY = (currentOffsetY + pan.y / size.height.toFloat()).coerceIn(-maxRange, maxRange)
                                                    onPhotoOffsetXChange.invoke(currentOffsetX)
                                                    onPhotoOffsetYChange?.invoke(currentOffsetY)
                                                    onPhotoScaleChange?.invoke(currentScale)
                                                }
                                            }
                                    } else Modifier

                                    val previewBlurMix = fileListItemBlurToShow.coerceIn(0f, 1f)
                                        .let { t -> (t * t * (3f - 2f * t)).coerceIn(0f, 1f) }
                                    val sharpLayerAlpha = imageAlpha * (1f - previewBlurMix)
                                    val blurLayerAlpha = imageAlpha * previewBlurMix

                                    // Keep preview blur onset smooth on tiny rows by cross-fading
                                    // from sharp -> blurred instead of switching to full blur early.
                                    androidx.compose.foundation.Image(
                                        painter = painter,
                                        contentDescription = null,
                                        modifier = flItemBaseModifier
                                            .then(flItemGestureModifier)
                                            .applyPhotoAlpha(sharpLayerAlpha),
                                        contentScale = when (flItemMode) {
                                            com.j4.texter2025.data.PhotoBackgroundMode.STATIC_ZOOM -> ContentScale.Fit
                                            else -> if (isLoadedFlItem) ContentScale.Fit else ContentScale.Crop
                                        }
                                    )

                                    if (blurLayerAlpha > 0.001f) {
                                        androidx.compose.foundation.Image(
                                            painter = painter,
                                            contentDescription = null,
                                            modifier = flItemBaseModifier
                                                .applyPhotoAlpha(blurLayerAlpha)
                                                .applyPhotoPreviewBlur(fileListItemBlurToShow),
                                            contentScale = when (flItemMode) {
                                                com.j4.texter2025.data.PhotoBackgroundMode.STATIC_ZOOM -> ContentScale.Fit
                                                else -> if (isLoadedFlItem) ContentScale.Fit else ContentScale.Crop
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(1.dp))
                        }
                    }
                    }
                }
                
                // Note view area (right side - 70% width, larger to show more content)
                Column(
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Dynamic resize simulation for content/memo areas
                    val isResizable = targetSection == "content" || targetSection == "memo"
                    var contentWeight by remember { mutableStateOf(0.8f) }
                    var areaWidthFraction by remember { mutableStateOf(1f) }
                    
                    // Content area (main text area - much larger to show opened note background)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (isResizable) areaWidthFraction else 1f)
                            .weight(contentWeight)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        // Background color layer - use selected color if editing this section
                        val displayContentColor = if (targetSection == "content") {
                            // When editing content area, show the currently selected color from color picker
                            val color = Color(
                                red = selectedColor.red,
                                green = selectedColor.green,
                                blue = selectedColor.blue,
                                alpha = alpha // Use the alpha slider value
                            )
                            color
                        } else {
                            val fallback = contentAreaColor ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                            fallback
                        }
                        
                        // Photo layer - show current photo being edited OR existing content area photo
                        val contentPhotoToShow = if (targetSection == "content") photoUri else (contentAreaPhotoUri ?: "")
                        val contentPhotoAlpha = if (targetSection == "content") alpha else contentAreaPhotoAlpha
                        
                        // Keep a stable local base color under the photo so alpha changes
                        // don't reveal blurred layers from beneath this area.
                        val contentColorToShow = if (contentPhotoToShow.isNotEmpty()) {
                            Color.Transparent
                        } else {
                            displayContentColor
                        }
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    contentColorToShow,
                                    RoundedCornerShape(4.dp)
                                )
                        )
                        
                        if (contentPhotoToShow.isNotEmpty()) {
                            val painter = rememberHighQualityPhotoPainter(
                                photoUri = contentPhotoToShow
                            )
                            val isLoadedContent = painter.state is coil.compose.AsyncImagePainter.State.Success
                            val imageAlpha = when (painter.state) {
                                is coil.compose.AsyncImagePainter.State.Error -> 0f
                                is coil.compose.AsyncImagePainter.State.Success -> contentPhotoAlpha
                                else -> contentPhotoAlpha
                            }
                            val contentImgSize = painter.intrinsicSize
                            val contentCropRatio = if (isLoadedContent && contentImgSize.width > 0 && contentImgSize.height > 0 && contentImgSize.width.isFinite() && contentImgSize.height.isFinite()) contentImgSize else null
                            val contentBlurToShow = if (targetSection == "content") blur else contentAreaPhotoBlur
                            
                            // Use positioning values - when editing content use gesture state, otherwise use saved values
                            var currentOffsetX by remember(contentAreaPhotoOffsetX) { 
                                mutableStateOf(contentAreaPhotoOffsetX) 
                            }
                            var currentOffsetY by remember(contentAreaPhotoOffsetY) { 
                                mutableStateOf(contentAreaPhotoOffsetY) 
                            }
                            var currentScale by remember(contentAreaPhotoScale) { 
                                mutableStateOf(contentAreaPhotoScale) 
                            }
                            
                            // Update state when parameters change
                            LaunchedEffect(contentAreaPhotoOffsetX, contentAreaPhotoOffsetY, contentAreaPhotoScale) {
                                currentOffsetX = contentAreaPhotoOffsetX
                                currentOffsetY = contentAreaPhotoOffsetY
                                currentScale = contentAreaPhotoScale
                            }
                            
                            val contentMode = if (targetSection == "content") photoBackgroundMode else com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC
                            androidx.compose.foundation.Image(
                                painter = painter,
                                contentDescription = null,
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(4.dp))
                                    .graphicsLayer {
                                        rotationZ = if (targetSection == "content") photoRotation else contentAreaPhotoRotation
                                        when (contentMode) {
                                            com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC -> {
                                                val ratio = if (contentCropRatio != null && size.width > 0 && size.height > 0) {
                                                    maxOf(size.width / contentCropRatio.width, size.height / contentCropRatio.height) /
                                                        minOf(size.width / contentCropRatio.width, size.height / contentCropRatio.height)
                                                } else 1f
                                                val effectiveScale = currentScale * ratio
                                                scaleX = effectiveScale
                                                scaleY = effectiveScale
                                                translationX = -currentOffsetX * size.width
                                                translationY = currentOffsetY * size.height
                                            }
                                            com.j4.texter2025.data.PhotoBackgroundMode.STATIC_ZOOM -> {
                                                val autoScale = if (contentCropRatio != null && size.width > 0 && size.height > 0) {
                                                    maxOf(size.width / contentCropRatio.width, size.height / contentCropRatio.height)
                                                } else 1f
                                                val effectiveScale = autoScale * currentScale
                                                scaleX = effectiveScale
                                                scaleY = effectiveScale
                                                translationX = 0f
                                                translationY = 0f
                                            }
                                        }
                                    }
                                    .then(
                                        if (targetSection == "content" && onPhotoOffsetXChange != null && contentMode == com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC) {
                                            Modifier
                                                .pointerInput(Unit) {
                                                    detectDragGestures { change, dragAmount ->
                                                        change.consume()
                                                        val maxRange = currentScale * 2f
                                                        currentOffsetX = (currentOffsetX - dragAmount.x / size.width.toFloat()).coerceIn(-maxRange, maxRange)
                                                        currentOffsetY = (currentOffsetY + dragAmount.y / size.height.toFloat()).coerceIn(-maxRange, maxRange)
                                                        onPhotoOffsetXChange.invoke(currentOffsetX)
                                                        onPhotoOffsetYChange?.invoke(currentOffsetY)
                                                    }
                                                }
                                                .pointerInput(Unit) {
                                                    detectTransformGestures { centroid, pan, zoom, rotation ->
                                                        currentScale = (currentScale * zoom).coerceIn(0.2f, 5f)
                                                        val maxRange = currentScale * 2f
                                                        currentOffsetX = (currentOffsetX - pan.x / size.width.toFloat()).coerceIn(-maxRange, maxRange)
                                                        currentOffsetY = (currentOffsetY + pan.y / size.height.toFloat()).coerceIn(-maxRange, maxRange)
                                                        onPhotoOffsetXChange.invoke(currentOffsetX)
                                                        onPhotoOffsetYChange?.invoke(currentOffsetY)
                                                        onPhotoScaleChange?.invoke(currentScale)
                                                    }
                                                }
                                        } else Modifier
                                    )
                                    .applyPhotoAlpha(imageAlpha)
                                    .applyPhotoPreviewBlur(contentBlurToShow),
                                contentScale = when (contentMode) {
                                    com.j4.texter2025.data.PhotoBackgroundMode.STATIC_ZOOM -> ContentScale.Fit
                                    else -> if (isLoadedContent) ContentScale.Fit else ContentScale.Crop
                                }
                            )
                        }
                        
                        Box(modifier = Modifier.padding(4.dp)) {
                        Column {
                            Text(
                                "Note Content",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            // More text lines to simulate opened note content
                            repeat(6) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(if (it == 5) 0.6f else 1f) // Last line shorter
                                        .height(5.dp)
                                        .background(
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                            RoundedCornerShape(1.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            // Add cursor simulation
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(8.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(0.5.dp)
                                    )
                            )
                        }
                        }
                    }
                    
                    // Draggable resize handle between content and memo
                    if (isResizable) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (isResizable) areaWidthFraction else 1f)
                                .height(14.dp)
                                .align(Alignment.CenterHorizontally)
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        contentWeight = (contentWeight + dragAmount.y / 600f).coerceIn(0.3f, 0.9f)
                                        areaWidthFraction = (areaWidthFraction + dragAmount.x / 400f).coerceIn(0.5f, 1f)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Left arrow hint
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(4.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                            RoundedCornerShape(2.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                // Main handle bar
                                Box(
                                    modifier = Modifier
                                        .width(28.dp)
                                        .height(3.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                            RoundedCornerShape(1.5.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                // Right arrow hint
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(4.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                            RoundedCornerShape(2.dp)
                                        )
                                )
                            }
                        }
                    }
                    
                    // Memo area (bottom of note view - smaller)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (isResizable) areaWidthFraction else 1f)
                            .weight(1f - contentWeight)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        // Photo layer - show current photo being edited OR existing memo area photo
                        val memoPhotoToShow = if (targetSection == "memo") photoUri else (memoAreaPhotoUri ?: "")
                        val memoPhotoAlpha = if (targetSection == "memo") alpha else memoAreaPhotoAlpha
                        
                        // Keep a stable local base color under the photo so alpha changes
                        // don't reveal blurred layers from the content/background beneath.
                        val memoBaseColor = if (memoPhotoToShow.isNotEmpty()) {
                            Color.Transparent
                        } else {
                            memoAreaColor ?: MaterialTheme.colorScheme.secondaryContainer
                        }
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    memoBaseColor,
                                    RoundedCornerShape(4.dp)
                                )
                        )
                        
                        if (memoPhotoToShow.isNotEmpty()) {
                            val painter = rememberHighQualityPhotoPainter(
                                photoUri = memoPhotoToShow
                            )
                            val isLoadedMemo = painter.state is coil.compose.AsyncImagePainter.State.Success
                            val imageAlpha = when (painter.state) {
                                is coil.compose.AsyncImagePainter.State.Error -> 0f
                                is coil.compose.AsyncImagePainter.State.Success -> memoPhotoAlpha
                                else -> memoPhotoAlpha
                            }
                            val memoImgSize = painter.intrinsicSize
                            val memoCropRatio = if (isLoadedMemo && memoImgSize.width > 0 && memoImgSize.height > 0 && memoImgSize.width.isFinite() && memoImgSize.height.isFinite()) memoImgSize else null
                            val memoBlurToShow = if (targetSection == "memo") blur else memoAreaPhotoBlur
                            
                            // Use positioning values - when editing memo use gesture state, otherwise use saved values
                            var currentOffsetX by remember(memoAreaPhotoOffsetX) { 
                                mutableStateOf(memoAreaPhotoOffsetX) 
                            }
                            var currentOffsetY by remember(memoAreaPhotoOffsetY) { 
                                mutableStateOf(memoAreaPhotoOffsetY) 
                            }
                            var currentScale by remember(memoAreaPhotoScale) { 
                                mutableStateOf(memoAreaPhotoScale) 
                            }
                            
                            // Update state when parameters change
                            LaunchedEffect(memoAreaPhotoOffsetX, memoAreaPhotoOffsetY, memoAreaPhotoScale) {
                                currentOffsetX = memoAreaPhotoOffsetX
                                currentOffsetY = memoAreaPhotoOffsetY
                                currentScale = memoAreaPhotoScale
                            }
                            
                            val memoMode = if (targetSection == "memo") photoBackgroundMode else com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC
                            val memoBaseModifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(4.dp))
                                .graphicsLayer {
                                    rotationZ = if (targetSection == "memo") photoRotation else memoAreaPhotoRotation
                                    when (memoMode) {
                                        com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC -> {
                                            val ratio = if (memoCropRatio != null && size.width > 0 && size.height > 0) {
                                                maxOf(size.width / memoCropRatio.width, size.height / memoCropRatio.height) /
                                                    minOf(size.width / memoCropRatio.width, size.height / memoCropRatio.height)
                                            } else 1f
                                            val effectiveScale = currentScale * ratio
                                            scaleX = effectiveScale
                                            scaleY = effectiveScale
                                            translationX = -currentOffsetX * size.width
                                            translationY = currentOffsetY * size.height
                                        }
                                        com.j4.texter2025.data.PhotoBackgroundMode.STATIC_ZOOM -> {
                                            val autoScale = if (memoCropRatio != null && size.width > 0 && size.height > 0) {
                                                maxOf(size.width / memoCropRatio.width, size.height / memoCropRatio.height)
                                            } else 1f
                                            val effectiveScale = autoScale * currentScale
                                            scaleX = effectiveScale
                                            scaleY = effectiveScale
                                            translationX = 0f
                                            translationY = 0f
                                        }
                                    }
                                }

                            val memoGestureModifier = if (targetSection == "memo" && onPhotoOffsetXChange != null && memoMode == com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC) {
                                Modifier
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            val maxRange = currentScale * 2f
                                            currentOffsetX = (currentOffsetX - dragAmount.x / size.width.toFloat()).coerceIn(-maxRange, maxRange)
                                            currentOffsetY = (currentOffsetY + dragAmount.y / size.height.toFloat()).coerceIn(-maxRange, maxRange)
                                            onPhotoOffsetXChange.invoke(currentOffsetX)
                                            onPhotoOffsetYChange?.invoke(currentOffsetY)
                                        }
                                    }
                                    .pointerInput(Unit) {
                                        detectTransformGestures { centroid, pan, zoom, rotation ->
                                            currentScale = (currentScale * zoom).coerceIn(0.2f, 5f)
                                            val maxRange = currentScale * 2f
                                            currentOffsetX = (currentOffsetX - pan.x / size.width.toFloat()).coerceIn(-maxRange, maxRange)
                                            currentOffsetY = (currentOffsetY + pan.y / size.height.toFloat()).coerceIn(-maxRange, maxRange)
                                            onPhotoOffsetXChange.invoke(currentOffsetX)
                                            onPhotoOffsetYChange?.invoke(currentOffsetY)
                                            onPhotoScaleChange?.invoke(currentScale)
                                        }
                                    }
                            } else Modifier

                            val previewBlurMix = memoBlurToShow.coerceIn(0f, 1f)
                                .let { t -> (t * t * (3f - 2f * t)).coerceIn(0f, 1f) }
                            val sharpLayerAlpha = imageAlpha * (1f - previewBlurMix)
                            val blurLayerAlpha = imageAlpha * previewBlurMix

                            androidx.compose.foundation.Image(
                                painter = painter,
                                contentDescription = null,
                                modifier = memoBaseModifier
                                    .then(memoGestureModifier)
                                    .applyPhotoAlpha(sharpLayerAlpha),
                                contentScale = when (memoMode) {
                                    com.j4.texter2025.data.PhotoBackgroundMode.STATIC_ZOOM -> ContentScale.Fit
                                    else -> if (isLoadedMemo) ContentScale.Fit else ContentScale.Crop
                                }
                            )

                            if (blurLayerAlpha > 0.001f) {
                                androidx.compose.foundation.Image(
                                    painter = painter,
                                    contentDescription = null,
                                    modifier = memoBaseModifier
                                        .applyPhotoAlpha(blurLayerAlpha)
                                        .applyPhotoPreviewBlur(memoBlurToShow),
                                    contentScale = when (memoMode) {
                                        com.j4.texter2025.data.PhotoBackgroundMode.STATIC_ZOOM -> ContentScale.Fit
                                        else -> if (isLoadedMemo) ContentScale.Fit else ContentScale.Crop
                                    }
                                )
                            }
                        }
                        
                        Box(modifier = Modifier.padding(3.dp)) {
                            Column {
                                Text(
                                    "Memo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f)
                                        .height(4.dp)
                                        .background(
                                            MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.3f),
                                            RoundedCornerShape(1.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Preview label
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                "Preview",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ColorSlider(label: String, value: Float, onValueChange: (Float) -> Unit, thumbColor: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.width(48.dp))
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = thumbColor,
                    activeTrackColor = thumbColor,
                    inactiveTrackColor = thumbColor.copy(alpha = 0.3f)
                ),
                modifier = Modifier.weight(1f)
            )
            Text((value * 255).roundToInt().toString(), modifier = Modifier.width(32.dp), textAlign = TextAlign.End)
        }
    }
}

@Composable
fun HueSlider(
    hue: Float,
    onHueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Rainbow gradient colors - remember to avoid recreation
    val rainbowColors = remember {
        listOf(
            Color.hsv(0f, 1f, 1f),    // Red
            Color.hsv(60f, 1f, 1f),   // Yellow
            Color.hsv(120f, 1f, 1f),  // Green
            Color.hsv(180f, 1f, 1f),  // Cyan
            Color.hsv(240f, 1f, 1f),  // Blue
            Color.hsv(300f, 1f, 1f),  // Magenta
            Color.hsv(360f, 1f, 1f)   // Red (wrap around)
        )
    }
    val rainbowBrush = remember { Brush.horizontalGradient(rainbowColors) }
    
    val sliderHeight = 32.dp
    val thumbWidthDp = 28.dp
    val density = androidx.compose.ui.platform.LocalDensity.current
    val thumbWidthPx = with(density) { thumbWidthDp.toPx() }
    
    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(sliderHeight)
            .clip(RoundedCornerShape(sliderHeight / 2))
            .background(brush = rainbowBrush)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newHue = (offset.x / size.width).coerceIn(0f, 1f) * 360f
                    onHueChange(newHue)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val newHue = (change.position.x / size.width).coerceIn(0f, 1f) * 360f
                    onHueChange(newHue)
                }
            }
    ) {
        val containerWidthPx = with(density) { maxWidth.toPx() }
        val thumbPosition = (hue / 360f).coerceIn(0f, 1f)
        val thumbOffsetPx = (containerWidthPx - thumbWidthPx) * thumbPosition
        
        // Thumb indicator - use graphicsLayer for smooth animation without recomposition
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(thumbWidthDp)
                .graphicsLayer {
                    translationX = thumbOffsetPx
                }
                .padding(vertical = 2.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White)
                .border(
                    width = 3.dp,
                    color = Color.hsv(hue, 1f, 1f),
                    shape = RoundedCornerShape(50)
                )
        )
    }
}

