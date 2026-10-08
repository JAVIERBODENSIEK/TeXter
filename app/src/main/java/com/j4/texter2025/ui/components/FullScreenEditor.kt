package com.j4.texter2025.ui.components

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.zIndex
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.j4.texter2025.data.FileModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import com.j4.texter2025.ui.components.TypewriterText
import com.j4.texter2025.MainActivity
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.remember
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.AnimatedVisibility
import androidx.activity.compose.BackHandler
import com.j4.texter2025.ui.components.ContentAreaStyle
import com.j4.texter2025.ui.components.ContentAreaSize
import com.j4.texter2025.ui.components.BlurredSurface
import com.j4.texter2025.ui.components.PhotoBackgroundSurface

private const val SHOW_FULLSCREEN_EDITOR_GAP_DIAGNOSTIC = true

@Composable
fun FullScreenEditor(
    file: FileModel,
    onSave: (FileModel) -> Unit,
    onCancel: () -> Unit,
    onDelete: (FileModel) -> Unit,
    activity: Activity,
    modifier: Modifier = Modifier,
    contentAreaStyle: ContentAreaStyle = ContentAreaStyle.EDGE_TO_EDGE, // default fallback
    contentAreaSize: ContentAreaSize = ContentAreaSize(0.98f, 1.0f), // default fallback with 98% width
    memoAreaSize: MemoAreaSize = MemoAreaSize(0.98f, 120f), // default fallback for memo area size
    customBgColor: Color? = null,
    contentAreaColor: Color? = null,
    memoAreaColor: Color? = null,
    contentAreaBlur: Float = 0f,
    memoAreaBlur: Float = 0f,
    customBgBlur: Float = 0f,
    // Photo background parameters - separate for each area
    customBgPhotoUri: String? = null,
    customBgPhotoAlpha: Float = 1f,
    customBgPhotoBlur: Float = 0f,
    customBgPhotoOffsetX: Float = 0f,
    customBgPhotoOffsetY: Float = 0f,
    customBgPhotoScale: Float = 1f,
    customBgPhotoRotation: Float = 0f,
    contentAreaPhotoUri: String? = null,
    contentAreaPhotoAlpha: Float = 1f,
    contentAreaPhotoBlur: Float = 0f,
    contentAreaPhotoOffsetX: Float = 0f,
    contentAreaPhotoOffsetY: Float = 0f,
    contentAreaPhotoScale: Float = 1f,
    contentAreaPhotoRotation: Float = 0f,
    memoAreaPhotoUri: String? = null,
    memoAreaPhotoAlpha: Float = 1f,
    memoAreaPhotoBlur: Float = 0f,
    memoAreaPhotoOffsetX: Float = 0f,
    memoAreaPhotoOffsetY: Float = 0f,
    memoAreaPhotoScale: Float = 1f,
    memoAreaPhotoRotation: Float = 0f,
    // Photo background modes
    customBgPhotoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
    contentAreaPhotoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
    memoAreaPhotoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
    // Editor drag mode
    editorDragMode: EditorDragMode = EditorDragMode.DUAL_BAR,
    hideStatusBar: Boolean = false,
    // Size change callbacks
    onContentAreaSizeChange: (ContentAreaSize) -> Unit = {},
    onMemoAreaSizeChange: (MemoAreaSize) -> Unit = {},
    // Back to Settings FAB
    showBackToSettingsFab: Boolean = false,
    onBackToSettings: () -> Unit = {},
    // Shared FAB position state
    fabOffsetX: Float = 0f,
    fabOffsetY: Float = 0f,
    onFabOffsetChange: (Float, Float) -> Unit = { _, _ -> }
) {
    var fileContent by remember { mutableStateOf(file.content) }
    var fileName by remember { mutableStateOf(file.name) }
    var memo by remember { mutableStateOf(file.memo) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showExitConfirmation by remember { mutableStateOf(false) }
    
    // Track initial values for unsaved changes detection
    val initialContent = remember(file) { file.content }
    val initialName = remember(file) { file.name }
    val initialMemo = remember(file) { file.memo }
    
    // Track if there are unsaved changes
    val hasUnsavedChanges by remember(fileName, fileContent, memo, initialContent, initialName, initialMemo) {
        derivedStateOf {
            val nameChanged = fileName.trim() != initialName.trim()
            val contentChanged = fileContent.trim() != initialContent.trim()
            val memoChanged = memo.trim() != initialMemo.trim()
            nameChanged || contentChanged || memoChanged
        }
    }
    
    // Get the memo area behavior setting from MainActivity
    val memoAreaBehavior = (activity as? MainActivity)?.getMemoAreaBehavior() ?: MemoAreaBehavior.REMEMBER_PER_FILE
    
    // Initialize showMemo based on the memo area behavior setting
    var showMemo by remember { 
        mutableStateOf(
            when (memoAreaBehavior) {
                MemoAreaBehavior.ALWAYS_SHOW -> true
                MemoAreaBehavior.ALWAYS_HIDE -> false
                MemoAreaBehavior.REMEMBER_PER_FILE -> file.memoVisible
            }
        ) 
    }
    
    fun handleExit() {
        val unsavedChangesBehavior = (activity as? MainActivity)?.getUnsavedChangesBehavior() ?: UnsavedChangesBehavior.ALWAYS_ASK
        
        when {
            !hasUnsavedChanges -> {
                // No unsaved changes, just exit
                onCancel()
            }
            unsavedChangesBehavior == UnsavedChangesBehavior.ALWAYS_ASK -> {
                // Show confirmation dialog
                showExitConfirmation = true
            }
            unsavedChangesBehavior == UnsavedChangesBehavior.NEVER_ASK_SAVE -> {
                // Auto-save and exit
                onSave(file.copy(
                    name = fileName, 
                    content = fileContent, 
                    memo = memo,
                    memoVisible = showMemo
                ))
                onCancel()
            }
            unsavedChangesBehavior == UnsavedChangesBehavior.NEVER_ASK_DISCARD -> {
                // Discard changes and exit
                onCancel()
            }
        }
    }
    
    var hasInteractedWithContent by remember { mutableStateOf(false) }
    
    // We'll persist the memo visibility state when the user explicitly saves or closes the file
    // This is handled in the onSave, onCancel, and onDelete callbacks
    
    // State for content area size that can be modified by drag gesture
    // Get the last manually set content area size from MainActivity if available
    val lastContentSize = (activity as? MainActivity)?.getLastContentAreaSize()
    var localContentAreaSize by remember { 
        mutableStateOf(
            if (lastContentSize != null) {
                ContentAreaSize(lastContentSize.first, lastContentSize.second)
            } else {
                contentAreaSize
            }
        )
    }
    
    // Update localContentAreaSize when contentAreaSize parameter changes (from preset selection)
    LaunchedEffect(contentAreaSize) {
        localContentAreaSize = contentAreaSize
    }
    
    // Fixed minimal space between main content and memo areas
    // Keep spacing thin and consistent regardless of memo area size
    val totalSpaceBetweenContentAndMemo = 8.dp // Fixed minimal spacing
    val memoToggleRowHeight = 32.dp
    val memoHandleReservedHeight = 4.dp
    val contentHandleReservedHeight = 4.dp
    val headerReservedHeight = 36.dp
    val bottomControlsSafePadding = 0.dp
    
    // State for memo area size that can be modified by drag gesture
    // Get the last manually set memo area size from MainActivity if available
    val lastMemoHeightDp = (activity as? MainActivity)?.getLastMemoHeight() ?: memoAreaSize.heightPercent
    var localMemoAreaSize by remember { 
        mutableStateOf(
            MemoAreaSize(memoAreaSize.widthPercent, lastMemoHeightDp)
        )
    }
    var memoHeight by remember { mutableStateOf(lastMemoHeightDp.dp) }
    
    // Update localMemoAreaSize when memoAreaSize parameter changes (from preset selection)
    LaunchedEffect(memoAreaSize) {
        localMemoAreaSize = memoAreaSize
        memoHeight = memoAreaSize.heightPercent.dp
    }
    
    // TODO: FEATURE_SINGLE_BAR_DRAG - State for single-bar drag mode (controls split between content and memo)
    // These state variables are used when ENABLE_SINGLE_BAR_MODE is true
    var contentWeight by remember { mutableStateOf(0.6f) } // 60% content, 40% memo by default
    var areaWidthFraction by remember { mutableStateOf(0.98f) } // Width of content/memo areas in single-bar mode
    var editorBodyHeightPx by remember { mutableStateOf(0f) }
    var isContentAreaDragging by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val density = LocalDensity.current
    val prefs = remember(context) {
        context.getSharedPreferences("TeXterPrefs", Context.MODE_PRIVATE)
    }
    val applyScaleToOpenedNotes = remember(prefs) {
        prefs.getBoolean("apply_ui_scale_to_opened_notes", false)
    }
    val openedNoteUiScale = remember(applyScaleToOpenedNotes, prefs) {
        if (applyScaleToOpenedNotes) {
            prefs.getFloat("app_ui_scale", 1f).coerceIn(0.7f, 1.3f)
        } else {
            1f
        }
    }
    val openedNoteDensity = remember(density, openedNoteUiScale) {
        Density(
            density = density.density * openedNoteUiScale,
            fontScale = density.fontScale * openedNoteUiScale
        )
    }

    fun calculateMaxContentHeightFraction(memoVisible: Boolean): Float {
        if (editorBodyHeightPx <= 0f) return 1f

        val reservedPx = with(density) {
            if (memoVisible) {
                (headerReservedHeight + contentHandleReservedHeight + memoToggleRowHeight + totalSpaceBetweenContentAndMemo + memoHandleReservedHeight + memoHeight + bottomControlsSafePadding).toPx()
            } else {
                (headerReservedHeight + contentHandleReservedHeight + memoToggleRowHeight + bottomControlsSafePadding).toPx()
            }
        }

        return ((editorBodyHeightPx - reservedPx) / editorBodyHeightPx).coerceIn(0.3f, 1f)
    }

    val maxContentHeightWhenMemoVisible = calculateMaxContentHeightFraction(memoVisible = true)
    val buttonSafeContentHeightWhenMemoHidden = calculateMaxContentHeightFraction(memoVisible = false)

    LaunchedEffect(showMemo, memoHeight, editorBodyHeightPx, editorDragMode) {
        if (editorDragMode != EditorDragMode.DUAL_BAR) return@LaunchedEffect
        if (showMemo) {
            localContentAreaSize = localContentAreaSize.copy(
                heightPercent = localContentAreaSize.heightPercent.coerceAtMost(maxContentHeightWhenMemoVisible)
            )
        }
    }

    val contentBgColor = contentAreaColor ?: MaterialTheme.colorScheme.surfaceVariant
    val memoBgColor = memoAreaColor ?: contentBgColor.copy(
        red = contentBgColor.red * 0.5f,
        green = contentBgColor.green * 0.5f,
        blue = contentBgColor.blue * 0.5f
    )
    
    // Debug blur values
    LaunchedEffect(customBgBlur) {
        android.util.Log.d("FullScreenEditor", "Received customBgBlur: $customBgBlur")
    }

    BackHandler {
        handleExit()
    }

    var lastAppliedHideStatusBar by remember { mutableStateOf<Boolean?>(null) }
    SideEffect {
        val editorWindow = activity.window
        WindowCompat.setDecorFitsSystemWindows(editorWindow, false)
        editorWindow.statusBarColor = android.graphics.Color.TRANSPARENT
        editorWindow.navigationBarColor = android.graphics.Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            editorWindow.isStatusBarContrastEnforced = false
            editorWindow.isNavigationBarContrastEnforced = false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            editorWindow.attributes = editorWindow.attributes.apply {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        editorWindow.decorView.systemUiVisibility =
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                if (hideStatusBar) {
                    android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
                        android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                } else {
                    0
                }
        WindowCompat.getInsetsController(editorWindow, editorWindow.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (lastAppliedHideStatusBar != hideStatusBar) {
                if (hideStatusBar) {
                    hide(WindowInsetsCompat.Type.systemBars())
                } else {
                    show(WindowInsetsCompat.Type.systemBars())
                }
                lastAppliedHideStatusBar = hideStatusBar
            }
        }
    }

        CompositionLocalProvider(LocalDensity provides openedNoteDensity) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .then(
                    if (SHOW_FULLSCREEN_EDITOR_GAP_DIAGNOSTIC) {
                        Modifier
                            .background(Color.Magenta.copy(alpha = 0.08f))
                            .border(2.dp, Color.Magenta)
                    } else {
                        Modifier
                    }
                )
        ) {
            // Background layer - photo or color background (uses custom background)
            if (customBgPhotoUri != null && customBgPhotoUri.isNotEmpty()) {
                val painter = rememberHighQualityPhotoPainter(
                    photoUri = customBgPhotoUri,
                    onError = { error ->
                        android.util.Log.e("FullScreenEditor", "Failed to load photo: ${error.result.throwable.message}")
                    }
                )
                
                val isLoaded = painter.state is coil.compose.AsyncImagePainter.State.Success
                val imageAlpha = if (isLoaded) customBgPhotoAlpha else 0f
                val imgSize = painter.intrinsicSize
                val cropRatio = if (isLoaded && imgSize.width > 0 && imgSize.height > 0 && imgSize.width.isFinite() && imgSize.height.isFinite()) imgSize else null
                
                when (customBgPhotoBackgroundMode) {
                    com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC -> {
                        androidx.compose.foundation.Image(
                            painter = painter,
                            contentDescription = "Background photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    rotationZ = customBgPhotoRotation
                                    val ratio = if (cropRatio != null && size.width > 0 && size.height > 0) {
                                        maxOf(size.width / cropRatio.width, size.height / cropRatio.height) /
                                            minOf(size.width / cropRatio.width, size.height / cropRatio.height)
                                    } else 1f
                                    val effectiveScale = customBgPhotoScale * ratio
                                    scaleX = effectiveScale
                                    scaleY = effectiveScale
                                    translationX = -customBgPhotoOffsetX * size.width
                                    translationY = customBgPhotoOffsetY * size.height
                                }
                                .applyPhotoAlpha(imageAlpha)
                                .applyPhotoBlur(customBgPhotoBlur),
                            contentScale = if (isLoaded) ContentScale.Fit else ContentScale.Crop
                        )
                    }
                    com.j4.texter2025.data.PhotoBackgroundMode.STATIC_ZOOM -> {
                        androidx.compose.foundation.Image(
                            painter = painter,
                            contentDescription = "Background photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    rotationZ = customBgPhotoRotation
                                    scaleX = customBgPhotoScale
                                    scaleY = customBgPhotoScale
                                    translationX = 0f
                                    translationY = 0f
                                }
                                .applyPhotoAlpha(imageAlpha)
                                .applyPhotoBlur(customBgPhotoBlur),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
            
            // Color background (always show, photo overlays if present)
            if (customBgPhotoUri == null || customBgPhotoUri.isEmpty()) {
                // Color background fallback
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = MaterialTheme.shapes.medium,
                    color = customBgColor ?: MaterialTheme.colorScheme.surface
                ) {}
            }
            
            // Content layer - always transparent to allow window effect
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = MaterialTheme.shapes.medium,
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { editorBodyHeightPx = it.height.toFloat() }
                ) {
                    // Compact header to preserve note editing space.
                    val headerHorizontalPadding = 12.dp
                    val headerTopPadding = 0.dp
                    val headerBottomPadding = 0.dp
                    val compactIconButtonSize = 36.dp
                    val compactIconSize = 18.dp
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = headerHorizontalPadding,
                                end = headerHorizontalPadding,
                                top = headerTopPadding,
                                bottom = headerBottomPadding
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TypewriterText(
                            text = fileName,
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Default,
                            color = MaterialTheme.colorScheme.onBackground,
                            autoStart = true
                        )
                        Row {
                            Box(
                                modifier = Modifier
                                    .size(compactIconButtonSize)
                                    .clickable { handleExit() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Close",
                                    modifier = Modifier.size(compactIconSize)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(compactIconButtonSize)
                                    .clickable { onDelete(file) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(compactIconSize)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(compactIconButtonSize)
                                    .clickable {
                                    if (fileName.isBlank()) {
                                        showError = true
                                        errorMessage = "File name cannot be empty"
                                    } else {
                                        // Save the file with the current memo visibility state
                                        onSave(file.copy(
                                            name = fileName,
                                            content = fileContent,
                                            memo = memo,
                                            memoVisible = showMemo
                                        ))
                                    }
                                },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Save,
                                    contentDescription = "Save",
                                    modifier = Modifier.size(compactIconSize)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(0.dp))
                    // Content area with dynamic size - use local state values or contentWeight/areaWidthFraction based on mode
                    // TODO: FEATURE_SINGLE_BAR_DRAG - Width/height calculation for single-bar mode
                    val widthFraction = if (editorDragMode == EditorDragMode.SINGLE_BAR && showMemo) {
                        areaWidthFraction.coerceIn(0.5f, 1.0f)
                    } else {
                        localContentAreaSize.widthPercent.coerceIn(0.3f, 1.0f)
                    }
                    val heightFraction = if (editorDragMode == EditorDragMode.SINGLE_BAR && showMemo) {
                        contentWeight.coerceIn(0.3f, 0.9f)
                    } else {
                        localContentAreaSize.heightPercent.coerceIn(0.3f, 1.0f)
                    }
                    val animatedHeightFraction by animateFloatAsState(
                        targetValue = heightFraction,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "contentHeightFraction"
                    )
                    val displayedHeightFraction = if (isContentAreaDragging) {
                        heightFraction
                    } else {
                        animatedHeightFraction
                    }
                    val contentHeightDp = with(density) {
                        (editorBodyHeightPx * displayedHeightFraction).coerceAtLeast(0f).toDp()
                    }
                    
                    // Calculate padding based on percentage values (smooth transition)
                    // As we approach 100%, padding gradually reduces to 0
                    val paddingFactor = 1f - ((widthFraction.coerceIn(0.9f, 1f) - 0.9f) / 0.1f).coerceIn(0f, 1f)
                    val contentPadding = (16 * paddingFactor).dp
                    val textPadding = (8 * paddingFactor).dp
                    
                    val contentModifier = Modifier
                        .fillMaxWidth(widthFraction)
                        .then(
                            if (editorBodyHeightPx > 0f) {
                                Modifier.height(contentHeightDp)
                            } else {
                                Modifier.fillMaxHeight(displayedHeightFraction)
                            }
                        )
                        .align(Alignment.CenterHorizontally)
                        .then(
                            if (SHOW_FULLSCREEN_EDITOR_GAP_DIAGNOSTIC) {
                                Modifier.border(2.dp, Color.Red)
                            } else {
                                Modifier
                            }
                        )
                        .then(
                            if (contentAreaStyle == ContentAreaStyle.DIALOG) {
                                Modifier.padding(contentPadding)
                            } else {
                                Modifier
                            }
                        )
                    when (contentAreaStyle) {
                        ContentAreaStyle.EDGE_TO_EDGE -> {
                            PhotoBackgroundSurface(
                                modifier = contentModifier,
                                color = contentBgColor,
                                blur = contentAreaBlur,
                                elevation = 6.dp,
                                shape = MaterialTheme.shapes.medium,
                                backgroundPhotoUri = contentAreaPhotoUri,
                                backgroundPhotoAlpha = contentAreaPhotoAlpha,
                                backgroundPhotoBlur = contentAreaPhotoBlur,
                                backgroundPhotoOffsetX = contentAreaPhotoOffsetX,
                                backgroundPhotoOffsetY = contentAreaPhotoOffsetY,
                                backgroundPhotoScale = contentAreaPhotoScale,
                                backgroundPhotoRotation = contentAreaPhotoRotation,
                                photoBackgroundMode = contentAreaPhotoBackgroundMode
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    if (!hasInteractedWithContent) {
                                        TypewriterText(
                                            text = fileContent,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontFamily = FontFamily.Monospace,
                                                lineHeight = 24.sp,
                                                color = MaterialTheme.colorScheme.onBackground
                                            ),
                                            modifier = Modifier
                                                .matchParentSize()
                                                .clickable { hasInteractedWithContent = true },
                                            autoStart = true
                                        )
                                    } else {
                                        BasicTextField(
                                            value = fileContent,
                                            onValueChange = { fileContent = it },
                                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                                fontFamily = FontFamily.Monospace,
                                                lineHeight = 24.sp,
                                                color = MaterialTheme.colorScheme.onBackground
                                            ),
                                            modifier = Modifier.matchParentSize()
                                        )
                                    }
                                }
                            }
                        }
                        ContentAreaStyle.DIALOG -> {
                            PhotoBackgroundSurface(
                                modifier = contentModifier,
                                color = contentBgColor,
                                elevation = 6.dp,
                                shape = MaterialTheme.shapes.medium,
                                backgroundPhotoUri = contentAreaPhotoUri,
                                backgroundPhotoAlpha = contentAreaPhotoAlpha,
                                backgroundPhotoBlur = contentAreaPhotoBlur,
                                backgroundPhotoOffsetX = contentAreaPhotoOffsetX,
                                backgroundPhotoOffsetY = contentAreaPhotoOffsetY,
                                backgroundPhotoScale = contentAreaPhotoScale,
                                backgroundPhotoRotation = contentAreaPhotoRotation,
                                photoBackgroundMode = contentAreaPhotoBackgroundMode
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(textPadding)
                                ) {
                                    if (!hasInteractedWithContent) {
                                        TypewriterText(
                                            text = fileContent,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontFamily = FontFamily.Monospace,
                                                lineHeight = 24.sp,
                                                color = MaterialTheme.colorScheme.onBackground
                                            ),
                                            modifier = Modifier
                                                .matchParentSize()
                                                .clickable { hasInteractedWithContent = true },
                                            autoStart = true
                                        )
                                    } else {
                                        BasicTextField(
                                            value = fileContent,
                                            onValueChange = { fileContent = it },
                                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                                fontFamily = FontFamily.Monospace,
                                                lineHeight = 24.sp,
                                                color = MaterialTheme.colorScheme.onBackground
                                            ),
                                            modifier = Modifier.matchParentSize()
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Drag handle indicator for main content area - only show in DUAL_BAR mode
                    if (editorDragMode == EditorDragMode.DUAL_BAR) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 0.dp, bottom = 0.dp) // Remove padding to minimize space
                                .height(4.dp)
                                .align(Alignment.CenterHorizontally)
                        ) {
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(4.dp)
                                .align(Alignment.Center)
                                .background(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(2.dp)
                                )
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = {
                                            isContentAreaDragging = true
                                        },
                                        onDragEnd = {
                                            isContentAreaDragging = false
                                            // When drag ends, save the content area size and update the state
                                            val newSize = com.j4.texter2025.ui.components.ContentAreaSize(
                                                localContentAreaSize.widthPercent,
                                                localContentAreaSize.heightPercent
                                            )
                                            // Save to preferences
                                            (activity as? MainActivity)?.let { mainActivity ->
                                                val prefs = mainActivity.getSharedPreferences("TeXterPrefs", android.content.Context.MODE_PRIVATE)
                                                prefs.edit()
                                                    .putFloat("content_area_width_percent", newSize.widthPercent)
                                                    .putFloat("content_area_height_percent", newSize.heightPercent)
                                                    .apply()
                                            }
                                            // Notify MainActivity of the size change
                                            onContentAreaSizeChange(newSize)
                                        },
                                        onDragCancel = {
                                            isContentAreaDragging = false
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            // Calculate new values based on drag
                                            // Drag right/down = increase size, drag left/up = decrease size
                                            val newWidth = localContentAreaSize.widthPercent + (dragAmount.x / 1000f)
                                            val newHeight = localContentAreaSize.heightPercent + (dragAmount.y / 1000f)
                                            
                                            // Constrain values between 0.3f and 1.0f
                                            val constrainedWidth = newWidth.coerceIn(0.3f, 1.0f)
                                            val maxAllowedHeight = if (showMemo) maxContentHeightWhenMemoVisible else 1.0f
                                            val constrainedHeight = newHeight.coerceIn(0.3f, maxAllowedHeight)
                                            
                                            // Update the local state
                                            localContentAreaSize = ContentAreaSize(constrainedWidth, constrainedHeight)
                                        }
                                    )
                                }
                        )
                        }
                    }
                    // Memo section with toggle (dialog-style, matches FileEditDialog)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 0.dp) // Remove vertical padding to minimize space
                            .height(memoToggleRowHeight), // Set a fixed height for the row to match text button height
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { 
                                // Get the memo area behavior setting
                                val memoAreaBehavior = (activity as? MainActivity)?.getMemoAreaBehavior() ?: MemoAreaBehavior.REMEMBER_PER_FILE
                                
                                // Toggle memo visibility
                                val nextShowMemo = !showMemo
                                showMemo = nextShowMemo

                                if (editorDragMode == EditorDragMode.DUAL_BAR) {
                                    val adjustedHeightPercent = if (nextShowMemo) {
                                        localContentAreaSize.heightPercent.coerceAtMost(maxContentHeightWhenMemoVisible)
                                    } else {
                                        buttonSafeContentHeightWhenMemoHidden
                                    }
                                    localContentAreaSize = localContentAreaSize.copy(heightPercent = adjustedHeightPercent)
                                }
                                
                                // Only update memo visibility state if behavior is REMEMBER_PER_FILE
                                if (memoAreaBehavior == MemoAreaBehavior.REMEMBER_PER_FILE) {
                                    (activity as? MainActivity)?.updateMemoVisibility(fileName, showMemo)
                                }
                            },
                            modifier = Modifier
                                .padding(0.dp)
                                .height(28.dp), // Minimal height for the button
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp) // Minimal padding
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (showMemo) Icons.Filled.KeyboardArrowUp 
                                    else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = if (showMemo) "Hide memo" else "Show memo"
                                )
                                Text(if (showMemo) "Hide Memo" else "Show Memo")
                            }
                        }
                        if (showError) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                    
                    // Add a spacer that uses the calculated space between content and memo
                    // This spacer will be the only element controlling the space between areas
                    if (showMemo && editorDragMode == EditorDragMode.DUAL_BAR) {
                        Spacer(modifier = Modifier.height(totalSpaceBetweenContentAndMemo))
                    }
                    
                    // TODO: FEATURE_SINGLE_BAR_DRAG - Single-bar drag handle between content and memo
                    // This unified drag handle adjusts both vertical split and horizontal width
                    if (showMemo && editorDragMode == EditorDragMode.SINGLE_BAR) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(areaWidthFraction)
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
                    
                    // Animated memo field with Surface styling and drag-to-resize functionality
                    AnimatedVisibility(
                        visible = showMemo,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        // Calculate the remaining height for the memo area
                        // Center the memo area in the available space
                        Column(
                            modifier = Modifier
                                // TODO: FEATURE_SINGLE_BAR_DRAG - Apply width fraction to memo area in single-bar mode
                                .fillMaxWidth(if (editorDragMode == EditorDragMode.SINGLE_BAR) areaWidthFraction else 1f)
                                .align(Alignment.CenterHorizontally)
                                .padding(horizontal = 16.dp) // Add some padding to match the main content area
                        ) {
                            // Drag handle indicator at the top - minimal padding
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 0.dp, bottom = 0.dp) // Remove padding to minimize space
                                    .height(4.dp)
                                    .align(Alignment.CenterHorizontally)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(80.dp) // Increased width for better hitbox
                                        .height(12.dp) // Increased height for better hitbox
                                        .align(Alignment.Center)
                                        .background(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .pointerInput(Unit) {
                                            detectDragGestures(
                                                onDragEnd = {
                                                    // When drag ends, save the memo area size to preferences
                                                    val newMemoSize = MemoAreaSize(
                                                        localMemoAreaSize.widthPercent,
                                                        memoHeight.value
                                                    )
                                                    // Save to preferences
                                                    (activity as? MainActivity)?.let { mainActivity ->
                                                        val prefs = mainActivity.getSharedPreferences("TeXterPrefs", android.content.Context.MODE_PRIVATE)
                                                        prefs.edit()
                                                            .putFloat("memo_area_width_percent", newMemoSize.widthPercent)
                                                            .putFloat("memo_area_height_dp", newMemoSize.heightPercent)
                                                            .apply()
                                                    }
                                                    // Notify MainActivity of the size change
                                                    onMemoAreaSizeChange(newMemoSize)
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    // Handle both width and height resizing
                                                    // Drag right = increase width, drag left = decrease width
                                                    // Drag DOWN = increase height, drag UP = decrease height (natural direction)
                                                    val newWidth = localMemoAreaSize.widthPercent + (dragAmount.x / 1000f)
                                                    // Use same speed as main content area
                                                    // Convert dp to relative scale: memo height range is 80-800dp
                                                    val newHeight = memoHeight.value + (dragAmount.y / 3f) // Drag down (positive) = expand memo area
                                                    
                                                    // Constrain width between 0.5f and 1.0f (50% to 100%)
                                                    val constrainedWidth = newWidth.coerceIn(0.5f, 1.0f)
                                                    // Constrain height between 80dp and 800dp
                                                    val constrainedHeight = newHeight.coerceIn(80f, 800f)
                                                    
                                                    // Update both width and height
                                                    localMemoAreaSize = MemoAreaSize(constrainedWidth, constrainedHeight)
                                                    memoHeight = constrainedHeight.dp
                                                }
                                            )
                                        }
                                )
                            }
                            
                            // Memo content area with proper constraints
                            // When using the slider (memoAreaSize), we calculate height based on available space
                            // When using the drag handle, we use the explicit memoHeight
                            // Use animateDpAsState for immediate response with subtle elastic effect
                            val animatedMemoHeight by animateDpAsState(
                                targetValue = memoHeight,
                                animationSpec = spring(
                                    dampingRatio = 0.7f, // Less bouncy for faster response
                                    stiffness = 300f    // Higher stiffness for immediate reaction
                                )
                            )
                            
                            // Mirror the main content area's centering approach with Box + contentAlignment
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 80.dp, max = 800.dp)
                                    .height(animatedMemoHeight), // Use animated height for elastic effect
                                contentAlignment = Alignment.Center // This centers the Surface horizontally
                            ) {
                                PhotoBackgroundSurface(
                                    modifier = Modifier
                                        .fillMaxWidth(localMemoAreaSize.widthPercent) // Use local memo area width that can be dragged
                                        .fillMaxHeight()
                                        .then(
                                            if (SHOW_FULLSCREEN_EDITOR_GAP_DIAGNOSTIC) {
                                                Modifier.border(2.dp, Color.Green)
                                            } else {
                                                Modifier
                                            }
                                        ), // Fill available height
                                    shape = RoundedCornerShape(12.dp),
                                    color = memoBgColor,
                                    blur = memoAreaBlur,
                                    elevation = 6.dp,
                                    backgroundPhotoUri = memoAreaPhotoUri,
                                    backgroundPhotoAlpha = memoAreaPhotoAlpha,
                                    backgroundPhotoBlur = memoAreaPhotoBlur,
                                    backgroundPhotoOffsetX = memoAreaPhotoOffsetX,
                                    backgroundPhotoOffsetY = memoAreaPhotoOffsetY,
                                    backgroundPhotoScale = memoAreaPhotoScale,
                                    backgroundPhotoRotation = memoAreaPhotoRotation,
                                    photoBackgroundMode = memoAreaPhotoBackgroundMode
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(textPadding) // Use same exact padding as TypewriterText for perfect alignment
                                    ) {
                                        BasicTextField(
                                            value = memo,
                                            onValueChange = { memo = it },
                                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onBackground
                                            ),
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (SHOW_FULLSCREEN_EDITOR_GAP_DIAGNOSTIC) {
            FullScreenEditorGapDiagnosticOverlay(
                activity = activity,
                hideStatusBar = hideStatusBar
            )
        }
        
        // Exit confirmation dialog when there are unsaved changes
        if (showExitConfirmation) {
            AlertDialog(
                onDismissRequest = { showExitConfirmation = false },
                title = { Text("Discard Changes") },
                text = { Text("You have unsaved changes. Are you sure you want to exit?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showExitConfirmation = false
                            onCancel()
                        }
                    ) {
                        Text("Discard")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitConfirmation = false }) {
                        Text("Continue Editing")
                    }
                }
            )
        }
        
        // Draggable "Go Back" floating button - persists in FullScreenEditor
        if (showBackToSettingsFab) {
            val latestFabOffsetX by rememberUpdatedState(fabOffsetX)
            val latestFabOffsetY by rememberUpdatedState(fabOffsetY)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset { androidx.compose.ui.unit.IntOffset(fabOffsetX.toInt(), fabOffsetY.toInt()) }
                        .pointerInput(showBackToSettingsFab) {
                            var gestureOffsetX = latestFabOffsetX
                            var gestureOffsetY = latestFabOffsetY
                            detectDragGestures(
                                onDragStart = {
                                    gestureOffsetX = latestFabOffsetX
                                    gestureOffsetY = latestFabOffsetY
                                }
                            ) { change, dragAmount ->
                                change.consume()
                                gestureOffsetX += dragAmount.x
                                gestureOffsetY += dragAmount.y
                                onFabOffsetChange(gestureOffsetX, gestureOffsetY)
                            }
                        }
                        .shadow(8.dp, RoundedCornerShape(24.dp))
                        .clickable(onClick = onBackToSettings),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Back to Settings",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Go Back",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
        }
    }

@Composable
private fun FullScreenEditorGapDiagnosticOverlay(
    activity: Activity,
    hideStatusBar: Boolean
) {
    val density = LocalDensity.current
    val rootInsets = ViewCompat.getRootWindowInsets(activity.window.decorView)
    val statusBarHeight = with(density) {
        (rootInsets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0).toDp()
    }
    val navigationBarHeight = with(density) {
        (rootInsets?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0).toDp()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1000f)
            .border(2.dp, Color.Yellow)
    ) {
        if (statusBarHeight > 0.dp) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .height(statusBarHeight)
                    .background(Color.Red.copy(alpha = 0.38f))
            ) {
                Text(
                    text = "STATUS inset ${statusBarHeight.value.toInt()}dp",
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        if (navigationBarHeight > 0.dp) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(navigationBarHeight)
                    .background(Color.Cyan.copy(alpha = 0.38f))
            ) {
                Text(
                    text = "NAV inset ${navigationBarHeight.value.toInt()}dp",
                    color = Color.Black,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .background(Color.White.copy(alpha = 0.72f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        Text(
            text = "GAP DEBUG: magenta=root, red=content, green=memo, cyan=nav, yellow=overlay | hideStatusBar=$hideStatusBar",
            color = Color.Yellow,
            fontSize = 10.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = navigationBarHeight + 6.dp)
                .background(Color.Black.copy(alpha = 0.72f))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}
