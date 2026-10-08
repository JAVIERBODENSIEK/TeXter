package com.j4.texter2025.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun UnifiedBackgroundEditor(
    show: Boolean,
    currentColor: Color,
    currentBlur: Float = 0f,
    onColorSelected: (ColorWithBlur) -> Unit,
    onPhotoSelected: (BackgroundOption) -> Unit = {},
    onPhotoUriChange: ((String) -> Unit)? = null,
    onPhotoAlphaChange: ((Float) -> Unit)? = null,
    onPhotoBlurChange: ((Float) -> Unit)? = null,
    onPhotoOffsetXChange: ((Float) -> Unit)? = null,
    onPhotoOffsetYChange: ((Float) -> Unit)? = null,
    onPhotoScaleChange: ((Float) -> Unit)? = null,
    onPhotoRotationChange: ((Float) -> Unit)? = null,
    onPhotoBackgroundModeChange: ((com.j4.texter2025.data.PhotoBackgroundMode) -> Unit)? = null,
    onDismiss: () -> Unit,
    // Current app colors for preview
    customBgColor: Color? = null,
    contentAreaColor: Color? = null,
    memoAreaColor: Color? = null,
    fileListItemColor: Color? = null,
    fileListBackgroundColor: Color? = null,
    targetSection: String = "bg",
    // Current photo state for the section being edited
    currentPhotoUri: String? = null,
    currentPhotoAlpha: Float = 1f,
    currentPhotoBlur: Float = 0f,
    currentPhotoOffsetX: Float = 0f,
    currentPhotoOffsetY: Float = 0f,
    currentPhotoScale: Float = 1f,
    currentPhotoRotation: Float = 0f,
    currentPhotoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
    // Initial mode: 0 = Color, 1 = Photo
    initialMode: Int = 0,
    // Test callback to navigate to main app
    onTestInApp: (() -> Unit)? = null
) {
    if (!show) return

    // Mode toggle: 0 = Color, 1 = Photo
    var selectedMode by remember(show) { mutableStateOf(initialMode) }

    // Color picker state
    var selectedColor by remember { mutableStateOf(currentColor) }
    var red by remember { mutableStateOf(currentColor.red) }
    var green by remember { mutableStateOf(currentColor.green) }
    var blue by remember { mutableStateOf(currentColor.blue) }
    var alpha by remember { mutableStateOf(currentColor.alpha) }
    var blur by remember { mutableStateOf(currentBlur) }
    var hue by remember { mutableStateOf(selectedColor.hue()) }
    var sv by remember { mutableStateOf(svFromColor(selectedColor)) }

    // Photo state — initialize from parameters so first render is correct
    var selectedPhotoUri by remember(show) { mutableStateOf(currentPhotoUri) }
    var photoAlpha by remember(show) { mutableStateOf(currentPhotoAlpha) }
    var photoBlur by remember(show) { mutableStateOf(currentPhotoBlur) }
    var photoOffsetX by remember(show) { mutableStateOf(currentPhotoOffsetX) }
    var photoOffsetY by remember(show) { mutableStateOf(currentPhotoOffsetY) }
    var photoScale by remember(show) { mutableStateOf(currentPhotoScale) }
    var photoRotation by remember(show) { mutableStateOf(currentPhotoRotation) }
    var photoBackgroundMode by remember(show) { mutableStateOf(currentPhotoBackgroundMode) }

    // All sections' photo state loaded from SharedPreferences
    var loadedCustomBgPhotoUri by remember { mutableStateOf<String?>(null) }
    var loadedCustomBgPhotoAlpha by remember { mutableStateOf(1f) }
    var loadedCustomBgPhotoBlur by remember { mutableStateOf(0f) }
    var loadedCustomBgOffsetX by remember { mutableStateOf(0f) }
    var loadedCustomBgOffsetY by remember { mutableStateOf(0f) }
    var loadedCustomBgScale by remember { mutableStateOf(1f) }
    var loadedCustomBgRotation by remember { mutableStateOf(0f) }
    var loadedContentPhotoUri by remember { mutableStateOf<String?>(null) }
    var loadedContentPhotoAlpha by remember { mutableStateOf(1f) }
    var loadedContentPhotoBlur by remember { mutableStateOf(0f) }
    var loadedContentOffsetX by remember { mutableStateOf(0f) }
    var loadedContentOffsetY by remember { mutableStateOf(0f) }
    var loadedContentScale by remember { mutableStateOf(1f) }
    var loadedContentRotation by remember { mutableStateOf(0f) }
    var loadedMemoPhotoUri by remember { mutableStateOf<String?>(null) }
    var loadedMemoPhotoAlpha by remember { mutableStateOf(1f) }
    var loadedMemoPhotoBlur by remember { mutableStateOf(0f) }
    var loadedMemoOffsetX by remember { mutableStateOf(0f) }
    var loadedMemoOffsetY by remember { mutableStateOf(0f) }
    var loadedMemoScale by remember { mutableStateOf(1f) }
    var loadedMemoRotation by remember { mutableStateOf(0f) }
    var loadedFileListItemPhotoUri by remember { mutableStateOf<String?>(null) }
    var loadedFileListItemPhotoAlpha by remember { mutableStateOf(1f) }
    var loadedFileListItemPhotoBlur by remember { mutableStateOf(0f) }
    var loadedFileListItemOffsetX by remember { mutableStateOf(0f) }
    var loadedFileListItemOffsetY by remember { mutableStateOf(0f) }
    var loadedFileListItemScale by remember { mutableStateOf(1f) }
    var loadedFileListItemRotation by remember { mutableStateOf(0f) }
    var loadedFileListBgPhotoUri by remember { mutableStateOf<String?>(null) }
    var loadedFileListBgPhotoAlpha by remember { mutableStateOf(1f) }
    var loadedFileListBgPhotoBlur by remember { mutableStateOf(0f) }
    var loadedFileListBgOffsetX by remember { mutableStateOf(0f) }
    var loadedFileListBgOffsetY by remember { mutableStateOf(0f) }
    var loadedFileListBgScale by remember { mutableStateOf(1f) }
    var loadedFileListBgRotation by remember { mutableStateOf(0f) }

    val context = LocalContext.current
    val baseDensity = LocalDensity.current
    val appUiScale = remember(show) {
        context
            .getSharedPreferences("TeXterPrefs", android.content.Context.MODE_PRIVATE)
            .getFloat("app_ui_scale", 1f)
            .coerceIn(0.7f, 1.3f)
    }
    val dialogDensity = remember(baseDensity, appUiScale) {
        Density(
            density = baseDensity.density * appUiScale,
            fontScale = baseDensity.fontScale * appUiScale
        )
    }

    // Load all photo state from SharedPreferences
    LaunchedEffect(show, targetSection) {
        if (show) {
            val prefs = context.getSharedPreferences("TeXterPrefs", android.content.Context.MODE_PRIVATE)

            // Load this section's positioning
            val (sectionPosition, sectionRotation) = when (targetSection) {
                "content" -> Triple(
                    prefs.getFloat("content_area_photo_offset_x", 0f),
                    prefs.getFloat("content_area_photo_offset_y", 0f),
                    prefs.getFloat("content_area_photo_scale", 1f)
                ) to prefs.getFloat("content_area_photo_rotation", 0f)
                "memo" -> Triple(
                    prefs.getFloat("memo_area_photo_offset_x", 0f),
                    prefs.getFloat("memo_area_photo_offset_y", 0f),
                    prefs.getFloat("memo_area_photo_scale", 1f)
                ) to prefs.getFloat("memo_area_photo_rotation", 0f)
                "fileListItem" -> Triple(
                    prefs.getFloat("file_list_item_photo_offset_x", 0f),
                    prefs.getFloat("file_list_item_photo_offset_y", 0f),
                    prefs.getFloat("file_list_item_photo_scale", 1f)
                ) to prefs.getFloat("file_list_item_photo_rotation", 0f)
                "fileListBg" -> Triple(
                    prefs.getFloat("file_list_background_photo_offset_x", 0f),
                    prefs.getFloat("file_list_background_photo_offset_y", 0f),
                    prefs.getFloat("file_list_background_photo_scale", 1f)
                ) to prefs.getFloat("file_list_background_photo_rotation", 0f)
                else -> Triple(
                    prefs.getFloat("custom_bg_photo_offset_x", 0f),
                    prefs.getFloat("custom_bg_photo_offset_y", 0f),
                    prefs.getFloat("custom_bg_photo_scale", 1f)
                ) to prefs.getFloat("custom_bg_photo_rotation", 0f)
            }
            val (sectionOffsetX, sectionOffsetY, sectionScale) = sectionPosition

            // Only load positioning from SharedPreferences if the parameters
            // were at defaults (0f, 0f, 1f) — i.e. for NEW photos.
            // For EDIT mode, the parameters already have the correct preset values.
            val paramsAreDefaults = currentPhotoOffsetX == 0f && currentPhotoOffsetY == 0f && currentPhotoScale == 1f && currentPhotoRotation == 0f
            if (paramsAreDefaults) {
                val useCustomBgFallback = targetSection != "bg" && sectionOffsetX == 0f && sectionOffsetY == 0f && sectionScale == 1f
                if (useCustomBgFallback) {
                    photoOffsetX = prefs.getFloat("custom_bg_photo_offset_x", 0f)
                    photoOffsetY = prefs.getFloat("custom_bg_photo_offset_y", 0f)
                    photoScale = prefs.getFloat("custom_bg_photo_scale", 1f)
                    photoRotation = prefs.getFloat("custom_bg_photo_rotation", 0f)
                } else {
                    photoOffsetX = sectionOffsetX
                    photoOffsetY = sectionOffsetY
                    photoScale = sectionScale
                    photoRotation = sectionRotation
                }
            }
            // If params are non-default, they were already used to initialize
            // photoOffsetX/Y/Scale via remember { mutableStateOf(currentPhoto*) }

            // Load ALL sections' photo state for preview
            loadedCustomBgPhotoUri = prefs.getString("custom_bg_photo_uri", null)
            loadedCustomBgPhotoAlpha = prefs.getFloat("custom_bg_photo_alpha", 1f)
            loadedCustomBgPhotoBlur = prefs.getFloat("custom_bg_photo_blur", 0f)
            loadedCustomBgOffsetX = prefs.getFloat("custom_bg_photo_offset_x", 0f)
            loadedCustomBgOffsetY = prefs.getFloat("custom_bg_photo_offset_y", 0f)
            loadedCustomBgScale = prefs.getFloat("custom_bg_photo_scale", 1f)
            loadedCustomBgRotation = prefs.getFloat("custom_bg_photo_rotation", 0f)
            loadedContentPhotoUri = prefs.getString("content_area_photo_uri", null)
            loadedContentPhotoAlpha = prefs.getFloat("content_area_photo_alpha", 1f)
            loadedContentPhotoBlur = prefs.getFloat("content_area_photo_blur", 0f)
            loadedContentOffsetX = prefs.getFloat("content_area_photo_offset_x", 0f)
            loadedContentOffsetY = prefs.getFloat("content_area_photo_offset_y", 0f)
            loadedContentScale = prefs.getFloat("content_area_photo_scale", 1f)
            loadedContentRotation = prefs.getFloat("content_area_photo_rotation", 0f)
            loadedMemoPhotoUri = prefs.getString("memo_area_photo_uri", null)
            loadedMemoPhotoAlpha = prefs.getFloat("memo_area_photo_alpha", 1f)
            loadedMemoPhotoBlur = prefs.getFloat("memo_area_photo_blur", 0f)
            loadedMemoOffsetX = prefs.getFloat("memo_area_photo_offset_x", 0f)
            loadedMemoOffsetY = prefs.getFloat("memo_area_photo_offset_y", 0f)
            loadedMemoScale = prefs.getFloat("memo_area_photo_scale", 1f)
            loadedMemoRotation = prefs.getFloat("memo_area_photo_rotation", 0f)
            loadedFileListItemPhotoUri = prefs.getString("file_list_item_photo_uri", null)
            loadedFileListItemPhotoAlpha = prefs.getFloat("file_list_item_photo_alpha", 1f)
            loadedFileListItemPhotoBlur = prefs.getFloat("file_list_item_photo_blur", 0f)
            loadedFileListItemOffsetX = prefs.getFloat("file_list_item_photo_offset_x", 0f)
            loadedFileListItemOffsetY = prefs.getFloat("file_list_item_photo_offset_y", 0f)
            loadedFileListItemScale = prefs.getFloat("file_list_item_photo_scale", 1f)
            loadedFileListItemRotation = prefs.getFloat("file_list_item_photo_rotation", 0f)
            loadedFileListBgPhotoUri = prefs.getString("file_list_background_photo_uri", null)
            loadedFileListBgPhotoAlpha = prefs.getFloat("file_list_background_photo_alpha", 1f)
            loadedFileListBgPhotoBlur = prefs.getFloat("file_list_background_photo_blur", 0f)
            loadedFileListBgOffsetX = prefs.getFloat("file_list_background_photo_offset_x", 0f)
            loadedFileListBgOffsetY = prefs.getFloat("file_list_background_photo_offset_y", 0f)
            loadedFileListBgScale = prefs.getFloat("file_list_background_photo_scale", 1f)
            loadedFileListBgRotation = prefs.getFloat("file_list_background_photo_rotation", 0f)

            selectedPhotoUri = currentPhotoUri
            // Always load alpha/blur from SharedPreferences (source of truth).
            // The preset object passed via currentPhotoAlpha/currentPhotoBlur may be stale
            // because the stored preset list is not updated when sliders change —
            // only SharedPreferences is updated via the onPhotoAlphaChange/onPhotoBlurChange callbacks.
            val (sectionAlpha, sectionBlur) = when (targetSection) {
                "content" -> Pair(
                    prefs.getFloat("content_area_photo_alpha", 1f),
                    prefs.getFloat("content_area_photo_blur", 0f)
                )
                "memo" -> Pair(
                    prefs.getFloat("memo_area_photo_alpha", 1f),
                    prefs.getFloat("memo_area_photo_blur", 0f)
                )
                "fileListItem" -> Pair(
                    prefs.getFloat("file_list_item_photo_alpha", 1f),
                    prefs.getFloat("file_list_item_photo_blur", 0f)
                )
                "fileListBg" -> Pair(
                    prefs.getFloat("file_list_background_photo_alpha", 1f),
                    prefs.getFloat("file_list_background_photo_blur", 0f)
                )
                else -> Pair(
                    prefs.getFloat("custom_bg_photo_alpha", 1f),
                    prefs.getFloat("custom_bg_photo_blur", 0f)
                )
            }
            photoAlpha = sectionAlpha
            photoBlur = sectionBlur
        }
    }

    // Defensive: ensure color channels are valid
    if (red.isNaN() || green.isNaN() || blue.isNaN() || alpha.isNaN() || hue.isNaN()) {
        red = 1f; green = 1f; blue = 1f; alpha = 1f; hue = 0f
        selectedColor = Color.White
    }

    // Active photo URI for preview (freshly picked takes priority)
    val activePhotoUri = selectedPhotoUri ?: currentPhotoUri

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        CompositionLocalProvider(LocalDensity provides dialogDensity) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                EditorHeader(
                    targetSection = targetSection,
                    onReset = {
                        if (selectedMode == 0) {
                            selectedColor = Color.White
                            red = 1f; green = 1f; blue = 1f; alpha = 1f
                            blur = 0f; hue = 0f; sv = Pair(0f, 1f)
                        } else {
                            photoOffsetX = 0f; photoOffsetY = 0f
                            photoScale = 1f; photoRotation = 0f; photoBlur = 0f; photoAlpha = 1f
                            onPhotoOffsetXChange?.invoke(0f)
                            onPhotoOffsetYChange?.invoke(0f)
                            onPhotoScaleChange?.invoke(1f)
                            onPhotoBlurChange?.invoke(0f)
                        }
                    },
                    onClose = onDismiss
                )

                Spacer(Modifier.height(8.dp))

                // Preview area with touch gestures (for photo mode)
                PreviewTouchArea(
                    activePhotoUri = activePhotoUri,
                    selectedMode = selectedMode,
                    // Color state
                    selectedColor = selectedColor,
                    colorAlpha = alpha,
                    colorBlur = blur,
                    // Photo state
                    photoAlpha = photoAlpha,
                    photoBlur = photoBlur,
                    photoOffsetX = photoOffsetX,
                    photoOffsetY = photoOffsetY,
                    photoScale = photoScale,
                    photoRotation = photoRotation,
                    onPhotoOffsetXChange = { newX ->
                        photoOffsetX = newX
                        onPhotoOffsetXChange?.invoke(newX)
                    },
                    onPhotoOffsetYChange = { newY ->
                        photoOffsetY = newY
                        onPhotoOffsetYChange?.invoke(newY)
                    },
                    onPhotoScaleChange = { newScale ->
                        photoScale = newScale
                        onPhotoScaleChange?.invoke(newScale)
                    },
                    onPhotoRotationChange = { newRotation ->
                        photoRotation = newRotation
                        onPhotoRotationChange?.invoke(newRotation)
                    },
                    // App colors for preview
                    customBgColor = customBgColor,
                    contentAreaColor = contentAreaColor,
                    memoAreaColor = memoAreaColor,
                    fileListItemColor = fileListItemColor,
                    fileListBackgroundColor = fileListBackgroundColor,
                    targetSection = targetSection,
                    // Loaded photo state for all sections
                    loadedCustomBgPhotoUri = loadedCustomBgPhotoUri,
                    loadedCustomBgPhotoAlpha = loadedCustomBgPhotoAlpha,
                    loadedCustomBgPhotoBlur = loadedCustomBgPhotoBlur,
                    loadedCustomBgOffsetX = loadedCustomBgOffsetX,
                    loadedCustomBgOffsetY = loadedCustomBgOffsetY,
                    loadedCustomBgScale = loadedCustomBgScale,
                    loadedCustomBgRotation = loadedCustomBgRotation,
                    loadedContentPhotoUri = loadedContentPhotoUri,
                    loadedContentPhotoAlpha = loadedContentPhotoAlpha,
                    loadedContentPhotoBlur = loadedContentPhotoBlur,
                    loadedContentOffsetX = loadedContentOffsetX,
                    loadedContentOffsetY = loadedContentOffsetY,
                    loadedContentScale = loadedContentScale,
                    loadedContentRotation = loadedContentRotation,
                    loadedMemoPhotoUri = loadedMemoPhotoUri,
                    loadedMemoPhotoAlpha = loadedMemoPhotoAlpha,
                    loadedMemoPhotoBlur = loadedMemoPhotoBlur,
                    loadedMemoOffsetX = loadedMemoOffsetX,
                    loadedMemoOffsetY = loadedMemoOffsetY,
                    loadedMemoScale = loadedMemoScale,
                    loadedMemoRotation = loadedMemoRotation,
                    loadedFileListItemPhotoUri = loadedFileListItemPhotoUri,
                    loadedFileListItemPhotoAlpha = loadedFileListItemPhotoAlpha,
                    loadedFileListItemPhotoBlur = loadedFileListItemPhotoBlur,
                    loadedFileListItemOffsetX = loadedFileListItemOffsetX,
                    loadedFileListItemOffsetY = loadedFileListItemOffsetY,
                    loadedFileListItemScale = loadedFileListItemScale,
                    loadedFileListItemRotation = loadedFileListItemRotation,
                    loadedFileListBgPhotoUri = loadedFileListBgPhotoUri,
                    loadedFileListBgPhotoAlpha = loadedFileListBgPhotoAlpha,
                    loadedFileListBgPhotoBlur = loadedFileListBgPhotoBlur,
                    loadedFileListBgOffsetX = loadedFileListBgOffsetX,
                    loadedFileListBgOffsetY = loadedFileListBgOffsetY,
                    loadedFileListBgScale = loadedFileListBgScale,
                    loadedFileListBgRotation = loadedFileListBgRotation,
                    photoBackgroundMode = photoBackgroundMode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                Spacer(Modifier.height(8.dp))

                // Mode toggle
                ModeToggle(
                    selectedMode = selectedMode,
                    onModeChange = { newMode ->
                        selectedMode = newMode
                    }
                )

                Spacer(Modifier.height(8.dp))

                // Scrollable controls area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (selectedMode == 0) {
                        ColorControlsPanel(
                            selectedColor = selectedColor,
                            red = red, green = green, blue = blue,
                            alpha = alpha, blur = blur,
                            hue = hue, sv = sv,
                            onColorChange = { newColor ->
                                selectedColor = newColor
                                red = newColor.red; green = newColor.green
                                blue = newColor.blue; alpha = newColor.alpha
                                hue = newColor.hue(); sv = svFromColor(newColor)
                            },
                            onRedChange = { v -> red = v; selectedColor = Color(red, green, blue, alpha) },
                            onGreenChange = { v -> green = v; selectedColor = Color(red, green, blue, alpha) },
                            onBlueChange = { v -> blue = v; selectedColor = Color(red, green, blue, alpha) },
                            onAlphaChange = { v -> alpha = v; selectedColor = Color(red, green, blue, alpha) },
                            onBlurChange = { blur = it },
                            onHueChange = {
                                hue = it * 360f
                                selectedColor = Color.hsv(hue, 1f, 1f)
                                red = selectedColor.red; green = selectedColor.green; blue = selectedColor.blue
                            },
                            onSvChange = { newSv ->
                                sv = newSv
                                val c = Color.hsv(hue, newSv.first, newSv.second)
                                selectedColor = c
                                red = c.red; green = c.green; blue = c.blue; alpha = c.alpha
                            }
                        )
                    } else {
                        PhotoControlsPanel(
                            selectedPhotoUri = selectedPhotoUri,
                            alpha = photoAlpha,
                            blur = photoBlur,
                            offsetX = photoOffsetX,
                            offsetY = photoOffsetY,
                            scale = photoScale,
                            photoBackgroundMode = photoBackgroundMode,
                            onPhotoSelected = { uri ->
                                selectedPhotoUri = uri
                                onPhotoUriChange?.invoke(uri)
                            },
                            onPhotoRemoved = {
                                selectedPhotoUri = null
                                photoAlpha = 1f; photoBlur = 0f
                                photoOffsetX = 0f; photoOffsetY = 0f; photoScale = 1f; photoRotation = 0f
                            },
                            onAlphaChange = {
                                photoAlpha = it
                                onPhotoAlphaChange?.invoke(it)
                            },
                            onBlurChange = {
                                photoBlur = it
                                onPhotoBlurChange?.invoke(it)
                            },
                            onOffsetXChange = {
                                photoOffsetX = it
                                onPhotoOffsetXChange?.invoke(it)
                            },
                            onOffsetYChange = {
                                photoOffsetY = it
                                onPhotoOffsetYChange?.invoke(it)
                            },
                            onScaleChange = {
                                photoScale = it
                                onPhotoScaleChange?.invoke(it)
                            },
                            onBackgroundModeChange = { mode ->
                                photoBackgroundMode = mode
                                onPhotoBackgroundModeChange?.invoke(mode)
                            }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { try { onDismiss() } catch (e: Exception) { e.printStackTrace() } },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Cancel") }

                    // Test button
                    if (onTestInApp != null) {
                        Button(
                            onClick = {
                                try {
                                    // Apply changes first
                                    if (selectedMode == 0) {
                                        // Color mode - apply color changes
                                        onColorSelected(ColorWithBlur(selectedColor, blur))
                                    } else {
                                        // Photo mode - apply photo changes
                                        onPhotoSelected(
                                            BackgroundOption(
                                                photoUri = selectedPhotoUri,
                                                alpha = photoAlpha,
                                                blur = photoBlur,
                                                photoOffsetX = photoOffsetX,
                                                photoOffsetY = photoOffsetY,
                                                photoScale = photoScale,
                                                photoRotation = photoRotation
                                            )
                                        )
                                    }
                                    // Then trigger test
                                    onTestInApp()
                                } catch (e: Exception) { e.printStackTrace() }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) { Text("Test") }
                    }

                    Button(
                        onClick = {
                            try {
                                if (selectedMode == 0) {
                                    onColorSelected(ColorWithBlur(selectedColor, blur))
                                } else {
                                    onPhotoSelected(
                                        BackgroundOption(
                                            photoUri = selectedPhotoUri,
                                            alpha = photoAlpha,
                                            blur = photoBlur,
                                            photoOffsetX = photoOffsetX,
                                            photoOffsetY = photoOffsetY,
                                            photoScale = photoScale,
                                            photoRotation = photoRotation
                                        )
                                    )
                                }
                            } catch (e: Exception) { e.printStackTrace() }
                            try { onDismiss() } catch (e: Exception) { e.printStackTrace() }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Select") }
                }
            }
        }
        }
    }
}

@Composable
private fun EditorHeader(
    targetSection: String,
    onReset: () -> Unit,
    onClose: () -> Unit
) {
    val sectionLabel = when (targetSection) {
        "bg" -> "Background"
        "content" -> "Content Area"
        "memo" -> "Memo Area"
        "fileListItem" -> "FileList Item"
        "fileListBg" -> "FileList Background"
        else -> "Background"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = sectionLabel,
            style = MaterialTheme.typography.titleMedium
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onReset) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "Reset",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }
    }
}

@Composable
private fun ModeToggle(
    selectedMode: Int,
    onModeChange: (Int) -> Unit
) {
    val modes = listOf("Color", "Photo")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        modes.forEachIndexed { index, label ->
            val isSelected = selectedMode == index
            Surface(
                onClick = {
                    onModeChange(index)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary
                        else Color.Transparent,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                               else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Text(
                    text = if (index == 0) "\uD83C\uDFA8 $label" else "\uD83D\uDCF7 $label",
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PreviewTouchArea(
    activePhotoUri: String?,
    selectedMode: Int,
    // Color state
    selectedColor: Color,
    colorAlpha: Float,
    colorBlur: Float,
    // Photo state
    photoAlpha: Float,
    photoBlur: Float,
    photoOffsetX: Float,
    photoOffsetY: Float,
    photoScale: Float,
    photoRotation: Float,
    onPhotoOffsetXChange: (Float) -> Unit,
    onPhotoOffsetYChange: (Float) -> Unit,
    onPhotoScaleChange: (Float) -> Unit,
    onPhotoRotationChange: (Float) -> Unit,
    // App colors
    customBgColor: Color?,
    contentAreaColor: Color?,
    memoAreaColor: Color?,
    fileListItemColor: Color?,
    fileListBackgroundColor: Color?,
    targetSection: String,
    // Loaded photo state for all sections
    loadedCustomBgPhotoUri: String?,
    loadedCustomBgPhotoAlpha: Float,
    loadedCustomBgPhotoBlur: Float,
    loadedCustomBgOffsetX: Float,
    loadedCustomBgOffsetY: Float,
    loadedCustomBgScale: Float,
    loadedCustomBgRotation: Float,
    loadedContentPhotoUri: String?,
    loadedContentPhotoAlpha: Float,
    loadedContentPhotoBlur: Float,
    loadedContentOffsetX: Float,
    loadedContentOffsetY: Float,
    loadedContentScale: Float,
    loadedContentRotation: Float,
    loadedMemoPhotoUri: String?,
    loadedMemoPhotoAlpha: Float,
    loadedMemoPhotoBlur: Float,
    loadedMemoOffsetX: Float,
    loadedMemoOffsetY: Float,
    loadedMemoScale: Float,
    loadedMemoRotation: Float,
    loadedFileListItemPhotoUri: String?,
    loadedFileListItemPhotoAlpha: Float,
    loadedFileListItemPhotoBlur: Float,
    loadedFileListItemOffsetX: Float,
    loadedFileListItemOffsetY: Float,
    loadedFileListItemScale: Float,
    loadedFileListItemRotation: Float,
    loadedFileListBgPhotoUri: String?,
    loadedFileListBgPhotoAlpha: Float,
    loadedFileListBgPhotoBlur: Float,
    loadedFileListBgOffsetX: Float,
    loadedFileListBgOffsetY: Float,
    loadedFileListBgScale: Float,
    loadedFileListBgRotation: Float,
    photoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
    modifier: Modifier = Modifier
) {
    // Determine preview parameters based on mode
    val previewPhotoUri = if (selectedMode == 1) (activePhotoUri ?: "") else (activePhotoUri ?: "")
    // In photo mode, pass photoAlpha/photoBlur so AppLayoutPreview shows real-time slider changes
    val previewAlpha = if (selectedMode == 0) colorAlpha else photoAlpha
    val previewBlur = if (selectedMode == 0) colorBlur else photoBlur

    // Use rememberUpdatedState so gesture lambda always reads fresh values
    // WITHOUT restarting the gesture detector (which kills ongoing gestures)
    val currentOffsetX by rememberUpdatedState(photoOffsetX)
    val currentOffsetY by rememberUpdatedState(photoOffsetY)
    val currentScale by rememberUpdatedState(photoScale)
    val currentRotation by rememberUpdatedState(photoRotation)
    val currentOnOffsetXChange by rememberUpdatedState(onPhotoOffsetXChange)
    val currentOnOffsetYChange by rememberUpdatedState(onPhotoOffsetYChange)
    val currentOnScaleChange by rememberUpdatedState(onPhotoScaleChange)
    val currentOnRotationChange by rememberUpdatedState(onPhotoRotationChange)

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .then(
                    if (selectedMode == 1 && activePhotoUri != null) {
                        Modifier.pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, rotation ->
                                // Negate pan.x: AppLayoutPreview uses -offsetX for translationX
                                // Keep pan fast and permissive so two-finger repositioning feels free.
                                val sensitivity = 4f
                                val safeWidth = size.width.toFloat().coerceAtLeast(1f)
                                val safeHeight = size.height.toFloat().coerceAtLeast(1f)
                                val newScale = (currentScale * zoom).coerceIn(0.2f, 5f)
                                val newRotation = (currentRotation + rotation).let {
                                    when {
                                        it > 180f -> it - 360f
                                        it < -180f -> it + 360f
                                        else -> it
                                    }
                                }
                                // More permissive scale-aware limits for "free" movement in full preview.
                                val maxOffset = 8f * newScale
                                val newOffsetX = (currentOffsetX - pan.x / safeWidth * sensitivity)
                                    .coerceIn(-maxOffset, maxOffset)
                                val newOffsetY = (currentOffsetY + pan.y / safeHeight * sensitivity)
                                    .coerceIn(-maxOffset, maxOffset)
                                currentOnOffsetXChange(newOffsetX)
                                currentOnOffsetYChange(newOffsetY)
                                currentOnScaleChange(newScale)
                                currentOnRotationChange(newRotation)
                            }
                        }
                    } else Modifier
                )
        ) {
            AppLayoutPreview(
                photoUri = previewPhotoUri,
                alpha = previewAlpha,
                blur = previewBlur,
                customBgColor = if (targetSection == "bg" && selectedMode == 0) selectedColor else customBgColor,
                contentAreaColor = if (targetSection == "content" && selectedMode == 0) selectedColor else contentAreaColor,
                memoAreaColor = if (targetSection == "memo" && selectedMode == 0) selectedColor else memoAreaColor,
                fileListItemColor = if (targetSection == "fileListItem" && selectedMode == 0) selectedColor else fileListItemColor,
                fileListBackgroundColor = if (targetSection == "fileListBg" && selectedMode == 0) selectedColor else fileListBackgroundColor,
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
                photoOffsetX = photoOffsetX,
                photoOffsetY = photoOffsetY,
                photoScale = photoScale,
                photoRotation = photoRotation,
                customBgPhotoOffsetX = if (targetSection == "bg") photoOffsetX else loadedCustomBgOffsetX,
                customBgPhotoOffsetY = if (targetSection == "bg") photoOffsetY else loadedCustomBgOffsetY,
                customBgPhotoScale = if (targetSection == "bg") photoScale else loadedCustomBgScale,
                customBgPhotoRotation = if (targetSection == "bg") photoRotation else loadedCustomBgRotation,
                contentAreaPhotoOffsetX = if (targetSection == "content") photoOffsetX else loadedContentOffsetX,
                contentAreaPhotoOffsetY = if (targetSection == "content") photoOffsetY else loadedContentOffsetY,
                contentAreaPhotoScale = if (targetSection == "content") photoScale else loadedContentScale,
                contentAreaPhotoRotation = if (targetSection == "content") photoRotation else loadedContentRotation,
                memoAreaPhotoOffsetX = if (targetSection == "memo") photoOffsetX else loadedMemoOffsetX,
                memoAreaPhotoOffsetY = if (targetSection == "memo") photoOffsetY else loadedMemoOffsetY,
                memoAreaPhotoScale = if (targetSection == "memo") photoScale else loadedMemoScale,
                memoAreaPhotoRotation = if (targetSection == "memo") photoRotation else loadedMemoRotation,
                fileListItemPhotoOffsetX = if (targetSection == "fileListItem") photoOffsetX else loadedFileListItemOffsetX,
                fileListItemPhotoOffsetY = if (targetSection == "fileListItem") photoOffsetY else loadedFileListItemOffsetY,
                fileListItemPhotoScale = if (targetSection == "fileListItem") photoScale else loadedFileListItemScale,
                fileListItemPhotoRotation = if (targetSection == "fileListItem") photoRotation else loadedFileListItemRotation,
                fileListBgPhotoOffsetX = if (targetSection == "fileListBg") photoOffsetX else loadedFileListBgOffsetX,
                fileListBgPhotoOffsetY = if (targetSection == "fileListBg") photoOffsetY else loadedFileListBgOffsetY,
                fileListBgPhotoScale = if (targetSection == "fileListBg") photoScale else loadedFileListBgScale,
                fileListBgPhotoRotation = if (targetSection == "fileListBg") photoRotation else loadedFileListBgRotation,
                photoBackgroundMode = photoBackgroundMode
            )

            // Gesture hint overlay (photo mode only)
            if (selectedMode == 1 && activePhotoUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .align(Alignment.TopCenter)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Use 2 fingers to move/rotate • Pinch to zoom",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
