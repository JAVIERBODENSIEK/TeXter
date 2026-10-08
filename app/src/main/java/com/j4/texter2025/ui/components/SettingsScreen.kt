package com.j4.texter2025.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.CircleShape
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumeDownChange
import androidx.compose.ui.input.pointer.consumePositionChange
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import com.j4.texter2025.data.*
import com.j4.texter2025.ui.components.ContentAreaStyle
import com.j4.texter2025.ui.components.MemoAreaBehavior
import com.j4.texter2025.ui.components.UnsavedChangesBehavior
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

// Data class for color presets (can be color, photo, or both)
data class SimpleColorPreset(
    val name: String,
    val color: Color,
    val photoUri: String? = null,
    val photoAlpha: Float = 1f,
    val photoBlur: Float = 0f,
    val photoOffsetX: Float = 0f,
    val photoOffsetY: Float = 0f,
    val photoScale: Float = 1f,
    val photoRotation: Float = 0f,
    val photoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC
) {
    fun toJson(): org.json.JSONObject {
        val obj = org.json.JSONObject()
        obj.put("name", name)
        obj.put("color", color.value.toLong())
        photoUri?.let { obj.put("photoUri", it) }
        obj.put("photoAlpha", photoAlpha)
        obj.put("photoBlur", photoBlur)
        obj.put("photoOffsetX", photoOffsetX)
        obj.put("photoOffsetY", photoOffsetY)
        obj.put("photoScale", photoScale)
        obj.put("photoRotation", photoRotation)
        obj.put("photoBackgroundMode", photoBackgroundMode.toString())
        return obj
    }
    
    companion object {
        fun fromJson(obj: org.json.JSONObject): SimpleColorPreset {
            val name = obj.getString("name")
            val color = Color(obj.getLong("color").toULong())
            val photoUri = if (obj.has("photoUri")) obj.getString("photoUri").takeIf { it.isNotEmpty() } else null
            val photoAlpha = if (obj.has("photoAlpha")) obj.getDouble("photoAlpha").toFloat() else 1f
            val photoBlur = if (obj.has("photoBlur")) obj.getDouble("photoBlur").toFloat() else 0f
            val photoOffsetX = if (obj.has("photoOffsetX")) obj.getDouble("photoOffsetX").toFloat() else 0f
            val photoOffsetY = if (obj.has("photoOffsetY")) obj.getDouble("photoOffsetY").toFloat() else 0f
            val photoScale = if (obj.has("photoScale")) obj.getDouble("photoScale").toFloat() else 1f
            val photoRotation = if (obj.has("photoRotation")) obj.getDouble("photoRotation").toFloat() else 0f
            val photoBackgroundMode = if (obj.has("photoBackgroundMode")) 
                com.j4.texter2025.data.PhotoBackgroundMode.fromString(obj.getString("photoBackgroundMode")) 
            else 
                com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC
            return SimpleColorPreset(name, color, photoUri, photoAlpha, photoBlur, photoOffsetX, photoOffsetY, photoScale, photoRotation, photoBackgroundMode)
        }
    }
}

@Composable
fun SettingsScreen(
    contentAreaStyle: ContentAreaStyle,
    onContentAreaStyleChange: (ContentAreaStyle) -> Unit,
    contentAreaSize: ContentAreaSize,
    onContentAreaSizeChange: (ContentAreaSize) -> Unit,
    customSizePresets: List<Pair<ContentAreaSize, String>> = emptyList(),
    onSaveCustomPreset: (ContentAreaSize, String) -> Unit,
    onDeleteCustomPreset: (String) -> Unit,
    memoAreaSize: MemoAreaSize,
    onMemoAreaSizeChange: (MemoAreaSize) -> Unit,
    customMemoSizePresets: List<Pair<MemoAreaSize, String>> = emptyList(),
    onSaveCustomMemoPreset: (MemoAreaSize, String) -> Unit,
    onDeleteCustomMemoPreset: (String) -> Unit,
    customBgColor: Color? = null,
    onCustomBgColorChange: (Color?) -> Unit,
    customBgBlur: Float = 0f,
    onCustomBgBlurChange: (Float) -> Unit = {},
    contentAreaColor: Color? = null,
    onContentAreaColorChange: (Color?) -> Unit,
    listAreaColor: Color? = null,
    onListAreaColorChange: (Color?) -> Unit,
    contentAreaBlur: Float = 0f,
    onContentAreaBlurChange: (Float) -> Unit = {},
    memoAreaColor: Color? = null,
    onMemoAreaColorChange: (Color?) -> Unit,
    memoAreaBlur: Float = 0f,
    onMemoAreaBlurChange: (Float) -> Unit = {},
    customContentAreaColors: List<SimpleColorPreset> = emptyList(),
    onAddContentAreaColor: (Color) -> Unit = {},
    onAddContentAreaColorWithPositioning: ((Color, String, Float, Float, Float, Float, Float, Float) -> Unit)? = null,
    onDeleteContentAreaColor: (Color, String?) -> Unit = { _, _ -> },
    onEditContentAreaColor: ((Color, Color, Float, String?, Float, Float, Float, Float) -> Unit)? = null,
    customMemoAreaColors: List<SimpleColorPreset> = emptyList(),
    onAddMemoAreaColor: (Color) -> Unit = {},
    onAddMemoAreaColorWithPositioning: ((Color, String, Float, Float, Float, Float, Float, Float) -> Unit)? = null,
    onDeleteMemoAreaColor: (Color, String?) -> Unit = { _, _ -> },
    onEditMemoAreaColor: ((Color, Color, Float, String?, Float, Float, Float, Float) -> Unit)? = null,
    customBgColors: List<SimpleColorPreset> = emptyList(),
    onAddBgColor: (Color) -> Unit = {},
    onAddBgColorWithPositioning: ((Color, String, Float, Float, Float, Float, Float, Float) -> Unit)? = null,
    onDeleteBgColor: (Color, String?) -> Unit = { _, _ -> },
    onEditBgColor: ((Color, Color, Float, String?, Float, Float, Float, Float) -> Unit)? = null,
    customListAreaColors: List<SimpleColorPreset> = emptyList(),
    onAddListAreaColor: (Color) -> Unit = {},
    onDeleteListAreaColor: (Color, String?) -> Unit = { _, _ -> },
    onEditListAreaColor: ((Color, Color, Float, String?) -> Unit)? = null,
    fileListItemColor: Color? = null,
    onFileListItemColorChange: (Color?) -> Unit = {},
    customFileListItemColors: List<SimpleColorPreset> = emptyList(),
    onAddFileListItemColor: (Color) -> Unit = {},
    onAddFileListItemColorWithPositioning: ((Color, String, Float, Float, Float, Float, Float, Float) -> Unit)? = null,
    onDeleteFileListItemColor: (Color, String?) -> Unit = { _, _ -> },
    onEditFileListItemColor: ((Color, Color, Float, String?, Float, Float, Float, Float) -> Unit)? = null,
    fileListItemBlur: Float = 0f,
    onFileListItemBlurChange: (Float) -> Unit = {},
    fileListBackgroundColor: Color? = null,
    onFileListBackgroundColorChange: (Color?) -> Unit = {},
    customFileListBackgroundColors: List<SimpleColorPreset> = emptyList(),
    onAddFileListBackgroundColor: (Color) -> Unit = {},
    onAddFileListBackgroundColorWithPositioning: ((Color, String, Float, Float, Float, Float, Float, Float) -> Unit)? = null,
    onDeleteFileListBackgroundColor: (Color, String?) -> Unit = { _, _ -> },
    onEditFileListBackgroundColor: ((Color, Color, Float, String?, Float, Float, Float, Float) -> Unit)? = null,
    fileListBackgroundBlur: Float = 0f,
    onFileListBackgroundBlurChange: (Float) -> Unit = {},
    // Photo background parameters - separate for each area
    customBgPhotoUri: String? = null,
    onCustomBgPhotoUriChange: (String?) -> Unit = {},
    customBgPhotoAlpha: Float = 1f,
    onCustomBgPhotoAlphaChange: (Float) -> Unit = {},
    customBgPhotoBlur: Float = 0f,
    onCustomBgPhotoBlurChange: (Float) -> Unit = {},
    customBgPhotoOffsetX: Float = 0f,
    onCustomBgPhotoOffsetXChange: (Float) -> Unit = {},
    customBgPhotoOffsetY: Float = 0f,
    onCustomBgPhotoOffsetYChange: (Float) -> Unit = {},
    customBgPhotoScale: Float = 1f,
    onCustomBgPhotoScaleChange: (Float) -> Unit = {},
    customBgPhotoRotation: Float = 0f,
    onCustomBgPhotoRotationChange: (Float) -> Unit = {},
    customBgPhotoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
    onCustomBgPhotoBackgroundModeChange: (com.j4.texter2025.data.PhotoBackgroundMode) -> Unit = {},
    contentAreaPhotoUri: String? = null,
    onContentAreaPhotoUriChange: (String?) -> Unit = {},
    contentAreaPhotoAlpha: Float = 1f,
    onContentAreaPhotoAlphaChange: (Float) -> Unit = {},
    contentAreaPhotoBlur: Float = 0f,
    onContentAreaPhotoBlurChange: (Float) -> Unit = {},
    contentAreaPhotoOffsetX: Float = 0f,
    onContentAreaPhotoOffsetXChange: (Float) -> Unit = {},
    contentAreaPhotoOffsetY: Float = 0f,
    onContentAreaPhotoOffsetYChange: (Float) -> Unit = {},
    contentAreaPhotoScale: Float = 1f,
    onContentAreaPhotoScaleChange: (Float) -> Unit = {},
    contentAreaPhotoRotation: Float = 0f,
    onContentAreaPhotoRotationChange: (Float) -> Unit = {},
    contentAreaPhotoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
    onContentAreaPhotoBackgroundModeChange: (com.j4.texter2025.data.PhotoBackgroundMode) -> Unit = {},
    memoAreaPhotoUri: String? = null,
    onMemoAreaPhotoUriChange: (String?) -> Unit = {},
    memoAreaPhotoAlpha: Float = 1f,
    onMemoAreaPhotoAlphaChange: (Float) -> Unit = {},
    memoAreaPhotoBlur: Float = 0f,
    onMemoAreaPhotoBlurChange: (Float) -> Unit = {},
    memoAreaPhotoOffsetX: Float = 0f,
    onMemoAreaPhotoOffsetXChange: (Float) -> Unit = {},
    memoAreaPhotoOffsetY: Float = 0f,
    onMemoAreaPhotoOffsetYChange: (Float) -> Unit = {},
    memoAreaPhotoScale: Float = 1f,
    onMemoAreaPhotoScaleChange: (Float) -> Unit = {},
    memoAreaPhotoRotation: Float = 0f,
    onMemoAreaPhotoRotationChange: (Float) -> Unit = {},
    memoAreaPhotoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
    onMemoAreaPhotoBackgroundModeChange: (com.j4.texter2025.data.PhotoBackgroundMode) -> Unit = {},
    fileListItemPhotoUri: String? = null,
    onFileListItemPhotoUriChange: (String?) -> Unit = {},
    fileListItemPhotoAlpha: Float = 1f,
    onFileListItemPhotoAlphaChange: (Float) -> Unit = {},
    fileListItemPhotoBlur: Float = 0f,
    onFileListItemPhotoBlurChange: (Float) -> Unit = {},
    fileListItemPhotoOffsetX: Float = 0f,
    onFileListItemPhotoOffsetXChange: (Float) -> Unit = {},
    fileListItemPhotoOffsetY: Float = 0f,
    onFileListItemPhotoOffsetYChange: (Float) -> Unit = {},
    fileListItemPhotoScale: Float = 1f,
    onFileListItemPhotoScaleChange: (Float) -> Unit = {},
    fileListItemPhotoRotation: Float = 0f,
    onFileListItemPhotoRotationChange: (Float) -> Unit = {},
    fileListItemPhotoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
    onFileListItemPhotoBackgroundModeChange: (com.j4.texter2025.data.PhotoBackgroundMode) -> Unit = {},
    fileListBackgroundPhotoUri: String? = null,
    onFileListBackgroundPhotoUriChange: (String?) -> Unit = {},
    fileListBackgroundPhotoAlpha: Float = 1f,
    onFileListBackgroundPhotoAlphaChange: (Float) -> Unit = {},
    fileListBackgroundPhotoBlur: Float = 0f,
    onFileListBackgroundPhotoBlurChange: (Float) -> Unit = {},
    fileListBackgroundPhotoOffsetX: Float = 0f,
    onFileListBackgroundPhotoOffsetXChange: (Float) -> Unit = {},
    fileListBackgroundPhotoOffsetY: Float = 0f,
    onFileListBackgroundPhotoOffsetYChange: (Float) -> Unit = {},
    fileListBackgroundPhotoScale: Float = 1f,
    onFileListBackgroundPhotoScaleChange: (Float) -> Unit = {},
    fileListBackgroundPhotoRotation: Float = 0f,
    onFileListBackgroundPhotoRotationChange: (Float) -> Unit = {},
    fileListBackgroundPhotoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
    onFileListBackgroundPhotoBackgroundModeChange: (com.j4.texter2025.data.PhotoBackgroundMode) -> Unit = {},
    memoAreaBehavior: MemoAreaBehavior = MemoAreaBehavior.REMEMBER_PER_FILE,
    onMemoAreaBehaviorChange: (MemoAreaBehavior) -> Unit,
    appUiScale: Float = 1f,
    onAppUiScaleChange: (Float) -> Unit = {},
    hideStatusBar: Boolean = false,
    onHideStatusBarChange: (Boolean) -> Unit = {},
    unsavedChangesBehavior: UnsavedChangesBehavior = UnsavedChangesBehavior.ALWAYS_ASK,
    onUnsavedChangesBehaviorChange: (UnsavedChangesBehavior) -> Unit,
    editorDragMode: EditorDragMode = EditorDragMode.DUAL_BAR,
    onEditorDragModeChange: (EditorDragMode) -> Unit,
    onColorDraggedBetweenSections: ((Color, String?, Float, Float, String, String) -> Unit)? = null,
    // Color preset parameters
    colorPresets: List<com.j4.texter2025.data.ColorPreset> = emptyList(),
    onSavePreset: (String) -> Unit = {},
    onApplyPreset: (com.j4.texter2025.data.ColorPreset) -> Unit = {},
    onDeletePreset: (com.j4.texter2025.data.ColorPreset) -> Unit = {},
    onRenamePreset: (com.j4.texter2025.data.ColorPreset, String) -> Unit = { _, _ -> },
    selectedPresetForEditing: com.j4.texter2025.data.ColorPreset? = null,
    onSelectPresetForEditing: (com.j4.texter2025.data.ColorPreset?) -> Unit = {},
    onUpdatePreset: (com.j4.texter2025.data.ColorPreset) -> Unit = {},
    onResetColorsForNewPreset: () -> Unit = {},
    // App-wide photo positioning for allSectionPhotos map
    appCustomBgPhotoOffsetX: Float = 0f,
    appCustomBgPhotoOffsetY: Float = 0f,
    appCustomBgPhotoScale: Float = 1f,
    appContentAreaPhotoOffsetX: Float = 0f,
    appContentAreaPhotoOffsetY: Float = 0f,
    appContentAreaPhotoScale: Float = 1f,
    appMemoAreaPhotoOffsetX: Float = 0f,
    appMemoAreaPhotoOffsetY: Float = 0f,
    appMemoAreaPhotoScale: Float = 1f,
    appFileListBgPhotoOffsetX: Float = 0f,
    appFileListBgPhotoOffsetY: Float = 0f,
    appFileListBgPhotoScale: Float = 1f,
    appFileListItemPhotoOffsetX: Float = 0f,
    appFileListItemPhotoOffsetY: Float = 0f,
    appFileListItemPhotoScale: Float = 1f,
    // Backup/Restore parameters
    onExportBackup: () -> Unit = {},
    onImportBackup: () -> Unit = {},
    lastBackupInfo: String? = null,
    settingsReturnSection: String? = null,
    settingsReturnRequestId: Int = 0,
    settingsReturnReopenEditor: Boolean = false,
    // Test callback to close settings and navigate to main app
    onTestInApp: ((String) -> Unit)? = null
) {
    // State for collapsible sections
    var mainTextAreaExpanded by remember { mutableStateOf(false) }
    var memoAreaExpanded by remember { mutableStateOf(false) }
    var colorsExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(settingsReturnRequestId) {
        if (settingsReturnRequestId <= 0) return@LaunchedEffect
        when (settingsReturnSection) {
            "bg", "content", "memo", "fileListItem", "fileListBg" -> {
                mainTextAreaExpanded = false
                memoAreaExpanded = false
                colorsExpanded = true
            }
            "mainText" -> {
                mainTextAreaExpanded = true
                memoAreaExpanded = false
                colorsExpanded = false
            }
            "memoSettings" -> {
                mainTextAreaExpanded = false
                memoAreaExpanded = true
                colorsExpanded = false
            }
        }
    }
    
    
    // Global drag state for overlay
    var isDragging by remember { mutableStateOf(false) }
    var draggedColor by remember { mutableStateOf<Color?>(null) }
    var draggedFromSection by remember { mutableStateOf<String?>(null) }
    var globalDragOffset by remember { mutableStateOf(Offset.Zero) }
    
    
    // Track drag position for overlay
    var dragPosition by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .then(if (hideStatusBar) Modifier else Modifier.statusBarsPadding())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 10.dp,
                    bottom = 32.dp // Add bottom padding for better scrolling
                )
        ) {
        Text("Settings", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(6.dp))
        
        // 📝 MAIN TEXT AREA CATEGORY
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mainTextAreaExpanded = !mainTextAreaExpanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📝 Main Text Area",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        if (mainTextAreaExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                        contentDescription = if (mainTextAreaExpanded) "Collapse" else "Expand"
                    )
                }
                
                AnimatedVisibility(
                    visible = mainTextAreaExpanded,
                    enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                ) {
                    MainTextAreaSection(
                        contentAreaSize = contentAreaSize,
                        onContentAreaSizeChange = onContentAreaSizeChange,
                        customSizePresets = customSizePresets,
                        onSaveCustomPreset = onSaveCustomPreset,
                        onDeleteCustomPreset = onDeleteCustomPreset
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 📋 MEMO AREA CATEGORY
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { memoAreaExpanded = !memoAreaExpanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📋 Memo Area",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        if (memoAreaExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                        contentDescription = if (memoAreaExpanded) "Collapse" else "Expand"
                    )
                }
                
                AnimatedVisibility(
                    visible = memoAreaExpanded,
                    enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                ) {
                    MemoAreaSection(
                        memoAreaSize = memoAreaSize,
                        onMemoAreaSizeChange = onMemoAreaSizeChange,
                        customMemoSizePresets = customMemoSizePresets,
                        onSaveCustomMemoPreset = onSaveCustomMemoPreset,
                        onDeleteCustomMemoPreset = onDeleteCustomMemoPreset,
                        memoAreaBehavior = memoAreaBehavior,
                        onMemoAreaBehaviorChange = onMemoAreaBehaviorChange,
                        editorDragMode = editorDragMode,
                        onEditorDragModeChange = onEditorDragModeChange
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 🎨 COLORS CATEGORY
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    clip = false
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { colorsExpanded = !colorsExpanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🎨 Colors",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        if (colorsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                        contentDescription = if (colorsExpanded) "Collapse" else "Expand"
                    )
                }
                
                AnimatedVisibility(
                    visible = colorsExpanded,
                    enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                ) {
                    ColorsSection(
                        customBgColor = customBgColor,
                        onCustomBgColorChange = { newColor ->
                            onCustomBgColorChange(newColor)
                            // DO NOT auto-save here - causes race condition where preset.copy() reads stale customBgColors
                            // Complete preset saves are handled by onAddBgColor and other color management handlers
                        },
                        customBgBlur = customBgBlur,
                        onCustomBgBlurChange = { newBlur ->
                            onCustomBgBlurChange(newBlur)
                            // DO NOT auto-save here - causes race condition where preset.copy() reads stale customBgColors
                            // Blur value is already saved via saveCustomBgBlur() in MainActivity
                        },
                        contentAreaColor = contentAreaColor,
                        onContentAreaColorChange = { newColor ->
                            onContentAreaColorChange(newColor)
                            // DO NOT auto-save here - causes race condition where preset.copy() reads stale color lists
                            // Complete preset saves are handled by color management handlers
                        },
                        memoAreaColor = memoAreaColor,
                        onMemoAreaColorChange = { newColor ->
                            onMemoAreaColorChange(newColor)
                            // DO NOT auto-save here - causes race condition where preset.copy() reads stale color lists
                            // Complete preset saves are handled by color management handlers
                        },
                        listAreaColor = listAreaColor,
                        onListAreaColorChange = onListAreaColorChange,
                        fileListItemColor = fileListItemColor,
                        onFileListItemColorChange = { newColor ->
                            onFileListItemColorChange(newColor)
                            // DO NOT auto-save here - causes race condition where preset.copy() reads stale color lists
                            // Complete preset saves are handled by color management handlers
                        },
                        fileListBackgroundColor = fileListBackgroundColor,
                        onFileListBackgroundColorChange = { newColor ->
                            onFileListBackgroundColorChange(newColor)
                            // DO NOT auto-save here - causes race condition where preset.copy() reads stale color lists
                            // Complete preset saves are handled by color management handlers
                        },
                        customBgColors = customBgColors,
                        onAddBgColor = onAddBgColor,
                        onAddBgColorWithPositioning = onAddBgColorWithPositioning,
                        onDeleteBgColor = onDeleteBgColor,
                        onEditBgColor = onEditBgColor,
                        customContentAreaColors = customContentAreaColors,
                        onAddContentAreaColor = onAddContentAreaColor,
                        onAddContentAreaColorWithPositioning = onAddContentAreaColorWithPositioning,
                        onDeleteContentAreaColor = onDeleteContentAreaColor,
                        onEditContentAreaColor = onEditContentAreaColor,
                        customMemoAreaColors = customMemoAreaColors,
                        onAddMemoAreaColor = onAddMemoAreaColor,
                        onAddMemoAreaColorWithPositioning = onAddMemoAreaColorWithPositioning,
                        onDeleteMemoAreaColor = onDeleteMemoAreaColor,
                        onEditMemoAreaColor = onEditMemoAreaColor,
                        customListAreaColors = customListAreaColors,
                        onAddListAreaColor = onAddListAreaColor,
                        onDeleteListAreaColor = onDeleteListAreaColor,
                        onEditListAreaColor = onEditListAreaColor,
                        customFileListItemColors = customFileListItemColors,
                        onAddFileListItemColor = onAddFileListItemColor,
                        onAddFileListItemColorWithPositioning = onAddFileListItemColorWithPositioning,
                        onDeleteFileListItemColor = onDeleteFileListItemColor,
                        onEditFileListItemColor = onEditFileListItemColor,
                        fileListItemBlur = fileListItemBlur,
                        onFileListItemBlurChange = onFileListItemBlurChange,
                        customFileListBackgroundColors = customFileListBackgroundColors,
                        onAddFileListBackgroundColor = onAddFileListBackgroundColor,
                        onAddFileListBackgroundColorWithPositioning = onAddFileListBackgroundColorWithPositioning,
                        onDeleteFileListBackgroundColor = onDeleteFileListBackgroundColor,
                        onEditFileListBackgroundColor = onEditFileListBackgroundColor,
                        fileListBackgroundBlur = fileListBackgroundBlur,
                        onFileListBackgroundBlurChange = onFileListBackgroundBlurChange,
                        onColorDraggedBetweenSections = onColorDraggedBetweenSections,
                        colorPresets = colorPresets,
                        onSavePreset = onSavePreset,
                        onApplyPreset = onApplyPreset,
                        onDeletePreset = onDeletePreset,
                        onRenamePreset = onRenamePreset,
                        selectedPresetForEditing = selectedPresetForEditing,
                        onSelectPresetForEditing = onSelectPresetForEditing,
                        onUpdatePreset = onUpdatePreset,
                        onResetColorsForNewPreset = onResetColorsForNewPreset,
                        // Photo background parameters - use custom background for this section
                        onBackgroundPhotoUriChange = onCustomBgPhotoUriChange,
                        onBackgroundPhotoAlphaChange = onCustomBgPhotoAlphaChange,
                        onBackgroundPhotoBlurChange = onCustomBgPhotoBlurChange,
                        // Area-specific photo background parameters
                        onCustomBgPhotoUriChange = onCustomBgPhotoUriChange,
                        onCustomBgPhotoAlphaChange = onCustomBgPhotoAlphaChange,
                        onCustomBgPhotoBlurChange = onCustomBgPhotoBlurChange,
                        onCustomBgPhotoOffsetXChange = onCustomBgPhotoOffsetXChange,
                        onCustomBgPhotoOffsetYChange = onCustomBgPhotoOffsetYChange,
                        onCustomBgPhotoScaleChange = onCustomBgPhotoScaleChange,
                        onCustomBgPhotoRotationChange = onCustomBgPhotoRotationChange,
                        onContentAreaPhotoUriChange = onContentAreaPhotoUriChange,
                        onContentAreaPhotoAlphaChange = onContentAreaPhotoAlphaChange,
                        onContentAreaPhotoBlurChange = onContentAreaPhotoBlurChange,
                        onContentAreaPhotoOffsetXChange = onContentAreaPhotoOffsetXChange,
                        onContentAreaPhotoOffsetYChange = onContentAreaPhotoOffsetYChange,
                        onContentAreaPhotoScaleChange = onContentAreaPhotoScaleChange,
                        onContentAreaPhotoRotationChange = onContentAreaPhotoRotationChange,
                        onMemoAreaPhotoUriChange = onMemoAreaPhotoUriChange,
                        onMemoAreaPhotoAlphaChange = onMemoAreaPhotoAlphaChange,
                        onMemoAreaPhotoBlurChange = onMemoAreaPhotoBlurChange,
                        onMemoAreaPhotoOffsetXChange = onMemoAreaPhotoOffsetXChange,
                        onMemoAreaPhotoOffsetYChange = onMemoAreaPhotoOffsetYChange,
                        onMemoAreaPhotoScaleChange = onMemoAreaPhotoScaleChange,
                        onMemoAreaPhotoRotationChange = onMemoAreaPhotoRotationChange,
                        onFileListItemPhotoUriChange = onFileListItemPhotoUriChange,
                        onFileListItemPhotoAlphaChange = onFileListItemPhotoAlphaChange,
                        onFileListItemPhotoBlurChange = onFileListItemPhotoBlurChange,
                        onFileListItemPhotoOffsetXChange = onFileListItemPhotoOffsetXChange,
                        onFileListItemPhotoOffsetYChange = onFileListItemPhotoOffsetYChange,
                        onFileListItemPhotoScaleChange = onFileListItemPhotoScaleChange,
                        onFileListItemPhotoRotationChange = onFileListItemPhotoRotationChange,
                        onFileListBackgroundPhotoUriChange = onFileListBackgroundPhotoUriChange,
                        onFileListBackgroundPhotoAlphaChange = onFileListBackgroundPhotoAlphaChange,
                        onFileListBackgroundPhotoBlurChange = onFileListBackgroundPhotoBlurChange,
                        onFileListBackgroundPhotoOffsetXChange = onFileListBackgroundPhotoOffsetXChange,
                        onFileListBackgroundPhotoOffsetYChange = onFileListBackgroundPhotoOffsetYChange,
                        onFileListBackgroundPhotoScaleChange = onFileListBackgroundPhotoScaleChange,
                        onFileListBackgroundPhotoRotationChange = onFileListBackgroundPhotoRotationChange,
                        // Current photo state
                        customBgPhotoUri = customBgPhotoUri,
                        customBgPhotoAlpha = customBgPhotoAlpha,
                        customBgPhotoBlur = customBgPhotoBlur,
                        customBgPhotoOffsetX = customBgPhotoOffsetX,
                        customBgPhotoOffsetY = customBgPhotoOffsetY,
                        customBgPhotoScale = customBgPhotoScale,
                        customBgPhotoRotation = customBgPhotoRotation,
                        contentAreaPhotoUri = contentAreaPhotoUri,
                        contentAreaPhotoAlpha = contentAreaPhotoAlpha,
                        contentAreaPhotoBlur = contentAreaPhotoBlur,
                        contentAreaPhotoOffsetX = contentAreaPhotoOffsetX,
                        contentAreaPhotoOffsetY = contentAreaPhotoOffsetY,
                        contentAreaPhotoScale = contentAreaPhotoScale,
                        contentAreaPhotoRotation = contentAreaPhotoRotation,
                        memoAreaPhotoUri = memoAreaPhotoUri,
                        memoAreaPhotoAlpha = memoAreaPhotoAlpha,
                        memoAreaPhotoBlur = memoAreaPhotoBlur,
                        memoAreaPhotoOffsetX = memoAreaPhotoOffsetX,
                        memoAreaPhotoOffsetY = memoAreaPhotoOffsetY,
                        memoAreaPhotoScale = memoAreaPhotoScale,
                        memoAreaPhotoRotation = memoAreaPhotoRotation,
                        fileListItemPhotoUri = fileListItemPhotoUri,
                        fileListItemPhotoAlpha = fileListItemPhotoAlpha,
                        fileListItemPhotoBlur = fileListItemPhotoBlur,
                        fileListItemPhotoOffsetX = fileListItemPhotoOffsetX,
                        fileListItemPhotoOffsetY = fileListItemPhotoOffsetY,
                        fileListItemPhotoScale = fileListItemPhotoScale,
                        fileListItemPhotoRotation = fileListItemPhotoRotation,
                        fileListBackgroundPhotoUri = fileListBackgroundPhotoUri,
                        fileListBackgroundPhotoAlpha = fileListBackgroundPhotoAlpha,
                        fileListBackgroundPhotoBlur = fileListBackgroundPhotoBlur,
                        fileListBackgroundPhotoOffsetX = fileListBackgroundPhotoOffsetX,
                        fileListBackgroundPhotoOffsetY = fileListBackgroundPhotoOffsetY,
                        fileListBackgroundPhotoScale = fileListBackgroundPhotoScale,
                        fileListBackgroundPhotoRotation = fileListBackgroundPhotoRotation,
                        // Photo background mode callbacks
                        customBgPhotoBackgroundMode = customBgPhotoBackgroundMode,
                        onCustomBgPhotoBackgroundModeChange = onCustomBgPhotoBackgroundModeChange,
                        contentAreaPhotoBackgroundMode = contentAreaPhotoBackgroundMode,
                        onContentAreaPhotoBackgroundModeChange = onContentAreaPhotoBackgroundModeChange,
                        memoAreaPhotoBackgroundMode = memoAreaPhotoBackgroundMode,
                        onMemoAreaPhotoBackgroundModeChange = onMemoAreaPhotoBackgroundModeChange,
                        fileListItemPhotoBackgroundMode = fileListItemPhotoBackgroundMode,
                        onFileListItemPhotoBackgroundModeChange = onFileListItemPhotoBackgroundModeChange,
                        fileListBackgroundPhotoBackgroundMode = fileListBackgroundPhotoBackgroundMode,
                        onFileListBackgroundPhotoBackgroundModeChange = onFileListBackgroundPhotoBackgroundModeChange,
                        settingsReturnSection = settingsReturnSection,
                        settingsReturnRequestId = settingsReturnRequestId,
                        settingsReturnReopenEditor = settingsReturnReopenEditor,
                        onTestInApp = onTestInApp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))

        AdvancedSettingsSection(
            appUiScale = appUiScale,
            onAppUiScaleChange = onAppUiScaleChange,
            onResetAppUiScale = { onAppUiScaleChange(1f) },
            hideStatusBar = hideStatusBar,
            onHideStatusBarChange = onHideStatusBarChange
        )

        Spacer(modifier = Modifier.height(10.dp))
        
        // UNSAVED CHANGES BEHAVIOR (standalone)
        Text("Unsaved Changes Behavior", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Choose how to handle unsaved changes when exiting an editor",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        var showUnsavedChangesDropdown by remember { mutableStateOf(false) }
        
        Box {
            OutlinedButton(
                onClick = { showUnsavedChangesDropdown = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(when(unsavedChangesBehavior) {
                    UnsavedChangesBehavior.ALWAYS_ASK -> "Always Ask"
                    UnsavedChangesBehavior.NEVER_ASK_DISCARD -> "Never Ask - Discard Changes"
                    UnsavedChangesBehavior.NEVER_ASK_SAVE -> "Never Ask - Save Automatically"
                })
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select behavior")
            }
            
            DropdownMenu(
                expanded = showUnsavedChangesDropdown,
                onDismissRequest = { showUnsavedChangesDropdown = false },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                DropdownMenuItem(
                    text = { Text("Always Ask") },
                    onClick = {
                        onUnsavedChangesBehaviorChange(UnsavedChangesBehavior.ALWAYS_ASK)
                        showUnsavedChangesDropdown = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Never Ask - Discard Changes") },
                    onClick = {
                        onUnsavedChangesBehaviorChange(UnsavedChangesBehavior.NEVER_ASK_DISCARD)
                        showUnsavedChangesDropdown = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Never Ask - Save Automatically") },
                    onClick = {
                        onUnsavedChangesBehaviorChange(UnsavedChangesBehavior.NEVER_ASK_SAVE)
                        showUnsavedChangesDropdown = false
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Photo Background Selection Buttons
        Text(
            "Photo Backgrounds",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        // OLD photo selection buttons removed - now using ColorPresetRow "+" buttons which work correctly
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 💾 BACKUP & RESTORE SECTION
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "💾 Backup & Restore",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Export all your customizations (presets, colors, settings) to a file that can be imported after reinstalling or on another device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Last backup info
                if (lastBackupInfo != null) {
                    Text(
                        lastBackupInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Export Button
                    Button(
                        onClick = onExportBackup,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = "Export",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export Backup")
                    }
                    
                    // Import Button
                    OutlinedButton(
                        onClick = onImportBackup,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Import",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import Backup")
                    }
                }
            }
        }
        }
        
        // OLD ColorPickerDialog removed - now using ColorPresetRow system which correctly passes targetSection
        
        // Global drag overlay - renders above all content with highest z-index
        if (isDragging && draggedColor != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(Float.MAX_VALUE)
            ) {
                // Floating drag preview - positioned at center of screen during drag
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.Center)
                        .shadow(16.dp, MaterialTheme.shapes.medium)
                        .clip(MaterialTheme.shapes.medium)
                        .background(draggedColor!!)
                        .border(4.dp, Color.White, MaterialTheme.shapes.medium)
                        .border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
                        .alpha(0.95f)
                        .zIndex(Float.MAX_VALUE)
                )
            }
        }
    }
}

@Composable
fun MainTextAreaSection(
    contentAreaSize: ContentAreaSize,
    onContentAreaSizeChange: (ContentAreaSize) -> Unit,
    customSizePresets: List<Pair<ContentAreaSize, String>>,
    onSaveCustomPreset: (ContentAreaSize, String) -> Unit,
    onDeleteCustomPreset: (String) -> Unit
) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        var showPresetDropdown by remember { mutableStateOf(false) }
        var showSaveDialog by remember { mutableStateOf(false) }
        var customPresetName by remember { mutableStateOf("") }
        
        // Find matching preset label or use "Custom" - use derivedStateOf to avoid infinite recomposition
        val currentPresetLabel by derivedStateOf {
            val allPresets = defaultDialogAreaPresets + customSizePresets
            val matchedLabel = allPresets.find { it.first == contentAreaSize }?.second ?: "Custom"
            android.util.Log.d("SettingsScreen", "currentPresetLabel: $matchedLabel, contentAreaSize: ${contentAreaSize.widthPercent}, ${contentAreaSize.heightPercent}")
            android.util.Log.d("SettingsScreen", "customSizePresets count: ${customSizePresets.size}")
            matchedLabel
        }
        
        Text("Dialog Area Presets", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Box {
            OutlinedButton(onClick = { showPresetDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                Text(currentPresetLabel)
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Show presets")
            }
            DropdownMenu(
                expanded = showPresetDropdown,
                onDismissRequest = { showPresetDropdown = false },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                // Default presets section
                Text(
                    "Default Presets",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                defaultDialogAreaPresets.forEach { (preset, label) ->
                    val isSelected = kotlin.math.abs(preset.widthPercent - contentAreaSize.widthPercent) < 0.001f && 
                                     kotlin.math.abs(preset.heightPercent - contentAreaSize.heightPercent) < 0.001f
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            android.util.Log.d("SettingsScreen", "Default preset clicked: $label, size: ${preset.widthPercent}, ${preset.heightPercent}")
                            onContentAreaSizeChange(preset)
                            showPresetDropdown = false
                        },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary) }
                        } else null
                    )
                }
                
                // Custom presets section (if any)
                if (customSizePresets.isNotEmpty()) {
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        "Custom Presets",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    customSizePresets.forEach { (preset, label) ->
                        var showDeleteConfirmation by remember { mutableStateOf(false) }
                        val isSelected = kotlin.math.abs(preset.widthPercent - contentAreaSize.widthPercent) < 0.001f && 
                                         kotlin.math.abs(preset.heightPercent - contentAreaSize.heightPercent) < 0.001f
                        
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                android.util.Log.d("SettingsScreen", "Custom preset clicked: $label, size: ${preset.widthPercent}, ${preset.heightPercent}")
                                onContentAreaSizeChange(preset)
                                showPresetDropdown = false
                            },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary) }
                            } else null,
                            trailingIcon = {
                                IconButton(
                                    onClick = { showDeleteConfirmation = true }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete preset",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )
                        
                        if (showDeleteConfirmation) {
                            AlertDialog(
                                onDismissRequest = { showDeleteConfirmation = false },
                                title = { Text("Delete Preset") },
                                text = { Text("Are you sure you want to delete the preset \"$label\"?") },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            onDeleteCustomPreset(label)
                                            showDeleteConfirmation = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Delete", color = MaterialTheme.colorScheme.onError)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { 
                                        showDeleteConfirmation = false
                                    }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Current size display
        Text(
            "Current size: ${(contentAreaSize.widthPercent * 100).toInt()}% width × ${(contentAreaSize.heightPercent * 100).toInt()}% height",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "💡 Use the drag handles in the editor to adjust size, then save as preset",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Save Current Size as Preset Button
        Button(
            onClick = { showSaveDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Save, contentDescription = "Save preset")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Current Size as Preset")
        }
        
        // Save Preset Dialog
        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                title = { Text("Save Content Area Preset") },
                text = {
                    Column {
                        Text("Current size: ${(contentAreaSize.widthPercent * 100).toInt()}% width × ${(contentAreaSize.heightPercent * 100).toInt()}% height")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = customPresetName,
                            onValueChange = { customPresetName = it },
                            label = { Text("Preset Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (customPresetName.isNotBlank()) {
                                onSaveCustomPreset(contentAreaSize, customPresetName)
                                customPresetName = ""
                                showSaveDialog = false
                            }
                        },
                        enabled = customPresetName.isNotBlank()
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun MemoAreaSection(
    memoAreaSize: MemoAreaSize,
    onMemoAreaSizeChange: (MemoAreaSize) -> Unit,
    customMemoSizePresets: List<Pair<MemoAreaSize, String>>,
    onSaveCustomMemoPreset: (MemoAreaSize, String) -> Unit,
    onDeleteCustomMemoPreset: (String) -> Unit,
    memoAreaBehavior: MemoAreaBehavior,
    onMemoAreaBehaviorChange: (MemoAreaBehavior) -> Unit,
    editorDragMode: EditorDragMode,
    onEditorDragModeChange: (EditorDragMode) -> Unit
) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        var showMemoPresetDropdown by remember { mutableStateOf(false) }
        var showMemoSaveDialog by remember { mutableStateOf(false) }
        var customMemoPresetName by remember { mutableStateOf("") }
        
        // Find matching memo preset label or use "Custom" - use derivedStateOf to avoid infinite recomposition
        val currentMemoPresetLabel by derivedStateOf {
            val allMemoPresets = defaultMemoAreaPresets + customMemoSizePresets
            allMemoPresets.find { it.first == memoAreaSize }?.second ?: "Custom"
        }
        
        Text("Memo Area Presets", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Box {
            OutlinedButton(onClick = { showMemoPresetDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                Text(currentMemoPresetLabel)
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Show memo presets")
            }
            DropdownMenu(
                expanded = showMemoPresetDropdown,
                onDismissRequest = { showMemoPresetDropdown = false },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                // Default memo presets section
                Text(
                    "Default Presets",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                defaultMemoAreaPresets.forEach { (preset, label) ->
                    val isSelected = kotlin.math.abs(preset.widthPercent - memoAreaSize.widthPercent) < 0.001f && 
                                     kotlin.math.abs(preset.heightPercent - memoAreaSize.heightPercent) < 0.001f
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onMemoAreaSizeChange(preset)
                            showMemoPresetDropdown = false
                        },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary) }
                        } else null
                    )
                }
                
                // Custom memo presets section (if any)
                if (customMemoSizePresets.isNotEmpty()) {
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        "Custom Presets",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    customMemoSizePresets.forEach { (preset, label) ->
                        var showDeleteConfirmation by remember { mutableStateOf(false) }
                        val isSelected = kotlin.math.abs(preset.widthPercent - memoAreaSize.widthPercent) < 0.001f && 
                                         kotlin.math.abs(preset.heightPercent - memoAreaSize.heightPercent) < 0.001f
                        
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onMemoAreaSizeChange(preset)
                                showMemoPresetDropdown = false
                            },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary) }
                            } else null,
                            trailingIcon = {
                                IconButton(
                                    onClick = { showDeleteConfirmation = true }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete memo preset",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )
                        
                        if (showDeleteConfirmation) {
                            AlertDialog(
                                onDismissRequest = { showDeleteConfirmation = false },
                                title = { Text("Delete Preset") },
                                text = { Text("Are you sure you want to delete the preset \"$label\"?") },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            onDeleteCustomMemoPreset(label)
                                            showDeleteConfirmation = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Delete", color = MaterialTheme.colorScheme.onError)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { 
                                        showDeleteConfirmation = false
                                    }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Current memo area size display
        Text(
            "Current size: ${(memoAreaSize.widthPercent * 100).toInt()}% width × ${memoAreaSize.heightPercent.toInt()}dp height",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "💡 Use the drag handles in the editor to resize the memo area",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Save Current Memo Size as Preset Button (ONLY ONE - no duplicate)
        Button(
            onClick = { showMemoSaveDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Save, contentDescription = "Save memo preset")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Current Memo Size as Preset")
        }
        
        // Save Memo Preset Dialog
        if (showMemoSaveDialog) {
            AlertDialog(
                onDismissRequest = { showMemoSaveDialog = false },
                title = { Text("Save Memo Size Preset") },
                text = {
                    Column {
                        Text("Current size: ${(memoAreaSize.widthPercent * 100).toInt()}% width × ${memoAreaSize.heightPercent.toInt()}dp height")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = customMemoPresetName,
                            onValueChange = { customMemoPresetName = it },
                            label = { Text("Preset Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (customMemoPresetName.isNotBlank()) {
                                onSaveCustomMemoPreset(memoAreaSize, customMemoPresetName)
                                customMemoPresetName = ""
                                showMemoSaveDialog = false
                            }
                        },
                        enabled = customMemoPresetName.isNotBlank()
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMemoSaveDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Memo Area Behavior Section
        Text("Memo Area Behavior", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        var showMemoAreaBehaviorDropdown by remember { mutableStateOf(false) }
        
        Box {
            OutlinedButton(onClick = { showMemoAreaBehaviorDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                Text(when(memoAreaBehavior) {
                    MemoAreaBehavior.ALWAYS_SHOW -> "Always Show Memo Area"
                    MemoAreaBehavior.ALWAYS_HIDE -> "Always Hide Memo Area"
                    MemoAreaBehavior.REMEMBER_PER_FILE -> "Remember Per File"
                })
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select memo area behavior")
            }
            
            DropdownMenu(
                expanded = showMemoAreaBehaviorDropdown,
                onDismissRequest = { showMemoAreaBehaviorDropdown = false },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                DropdownMenuItem(
                    text = { Text("Always Show Memo Area") },
                    onClick = {
                        onMemoAreaBehaviorChange(MemoAreaBehavior.ALWAYS_SHOW)
                        showMemoAreaBehaviorDropdown = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Always Hide Memo Area") },
                    onClick = {
                        onMemoAreaBehaviorChange(MemoAreaBehavior.ALWAYS_HIDE)
                        showMemoAreaBehaviorDropdown = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Remember Per File") },
                    onClick = {
                        onMemoAreaBehaviorChange(MemoAreaBehavior.REMEMBER_PER_FILE)
                        showMemoAreaBehaviorDropdown = false
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // TODO: FEATURE_SINGLE_BAR_DRAG - Editor Drag Mode Section
        // This section is conditionally shown based on ENABLE_SINGLE_BAR_MODE feature flag
        // When disabled, users only see dual-bar mode (no UI for selection needed)
        if (ENABLE_SINGLE_BAR_MODE) {
            Text("Editor Resize Mode", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            var showEditorDragModeDropdown by remember { mutableStateOf(false) }
            
            Box {
                OutlinedButton(onClick = { showEditorDragModeDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(when(editorDragMode) {
                        EditorDragMode.DUAL_BAR -> "Dual-Bar (Separate Handles)"
                        EditorDragMode.SINGLE_BAR -> "Single-Bar (Unified Handle)"
                    })
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select editor drag mode")
                }
                
                DropdownMenu(
                    expanded = showEditorDragModeDropdown,
                    onDismissRequest = { showEditorDragModeDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    DropdownMenuItem(
                        text = { 
                            Column {
                                Text("Dual-Bar (Separate Handles)")
                                Text(
                                    "Content and memo areas have independent resize handles",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            onEditorDragModeChange(EditorDragMode.DUAL_BAR)
                            showEditorDragModeDropdown = false
                        }
                    )
                    DropdownMenuItem(
                        text = { 
                            Column {
                                Text("Single-Bar (Unified Handle)")
                                Text(
                                    "One handle between areas adjusts both simultaneously",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            onEditorDragModeChange(EditorDragMode.SINGLE_BAR)
                            showEditorDragModeDropdown = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ColorsSection(
    customBgColor: Color?,
    onCustomBgColorChange: (Color?) -> Unit,
    customBgBlur: Float = 0f,
    onCustomBgBlurChange: (Float) -> Unit = {},
    listAreaColor: Color?,
    onListAreaColorChange: (Color?) -> Unit,
    contentAreaColor: Color?,
    onContentAreaColorChange: (Color?) -> Unit,
    contentAreaBlur: Float = 0f,
    onContentAreaBlurChange: (Float) -> Unit = {},
    memoAreaColor: Color?,
    onMemoAreaColorChange: (Color?) -> Unit,
    memoAreaBlur: Float = 0f,
    onMemoAreaBlurChange: (Float) -> Unit = {},
    customContentAreaColors: List<SimpleColorPreset> = emptyList(),
    onAddContentAreaColor: (Color) -> Unit = {},
    onAddContentAreaColorWithPositioning: ((Color, String, Float, Float, Float, Float, Float, Float) -> Unit)? = null,
    onDeleteContentAreaColor: (Color, String?) -> Unit = { _, _ -> },
    onEditContentAreaColor: ((Color, Color, Float, String?, Float, Float, Float, Float) -> Unit)? = null,
    customMemoAreaColors: List<SimpleColorPreset> = emptyList(),
    onAddMemoAreaColor: (Color) -> Unit = {},
    onAddMemoAreaColorWithPositioning: ((Color, String, Float, Float, Float, Float, Float, Float) -> Unit)? = null,
    onDeleteMemoAreaColor: (Color, String?) -> Unit = { _, _ -> },
    onEditMemoAreaColor: ((Color, Color, Float, String?, Float, Float, Float, Float) -> Unit)? = null,
    customBgColors: List<SimpleColorPreset> = emptyList(),
    onAddBgColor: (Color) -> Unit = {},
    onAddBgColorWithPositioning: ((Color, String, Float, Float, Float, Float, Float, Float) -> Unit)? = null,
    onDeleteBgColor: (Color, String?) -> Unit = { _, _ -> },
    onEditBgColor: ((Color, Color, Float, String?, Float, Float, Float, Float) -> Unit)? = null,
    customListAreaColors: List<SimpleColorPreset> = emptyList(),
    onAddListAreaColor: (Color) -> Unit = {},
    onDeleteListAreaColor: (Color, String?) -> Unit = { _, _ -> },
    onEditListAreaColor: ((Color, Color, Float, String?) -> Unit)? = null,
    fileListItemColor: Color? = null,
    onFileListItemColorChange: (Color?) -> Unit = {},
    customFileListItemColors: List<SimpleColorPreset> = emptyList(),
    onAddFileListItemColor: (Color) -> Unit = {},
    onAddFileListItemColorWithPositioning: ((Color, String, Float, Float, Float, Float, Float, Float) -> Unit)? = null,
    onDeleteFileListItemColor: (Color, String?) -> Unit = { _, _ -> },
    onEditFileListItemColor: ((Color, Color, Float, String?, Float, Float, Float, Float) -> Unit)? = null,
    fileListItemBlur: Float = 0f,
    onFileListItemBlurChange: (Float) -> Unit = {},
    fileListBackgroundColor: Color? = null,
    onFileListBackgroundColorChange: (Color?) -> Unit = {},
    customFileListBackgroundColors: List<SimpleColorPreset> = emptyList(),
    onAddFileListBackgroundColor: (Color) -> Unit = {},
    onAddFileListBackgroundColorWithPositioning: ((Color, String, Float, Float, Float, Float, Float, Float) -> Unit)? = null,
    onDeleteFileListBackgroundColor: (Color, String?) -> Unit = { _, _ -> },
    onEditFileListBackgroundColor: ((Color, Color, Float, String?, Float, Float, Float, Float) -> Unit)? = null,
    fileListBackgroundBlur: Float = 0f,
    onFileListBackgroundBlurChange: (Float) -> Unit = {},
    onColorDraggedBetweenSections: ((Color, String?, Float, Float, String, String) -> Unit)? = null,
    // Photo background parameters - generic (for custom background)
    onBackgroundPhotoUriChange: ((String?) -> Unit)? = null,
    onBackgroundPhotoAlphaChange: ((Float) -> Unit)? = null,
    onBackgroundPhotoBlurChange: ((Float) -> Unit)? = null,
    // Area-specific photo background parameters
    onCustomBgPhotoUriChange: ((String?) -> Unit)? = null,
    onCustomBgPhotoAlphaChange: ((Float) -> Unit)? = null,
    onCustomBgPhotoBlurChange: ((Float) -> Unit)? = null,
    onCustomBgPhotoOffsetXChange: ((Float) -> Unit)? = null,
    onCustomBgPhotoOffsetYChange: ((Float) -> Unit)? = null,
    onCustomBgPhotoScaleChange: ((Float) -> Unit)? = null,
    onCustomBgPhotoRotationChange: ((Float) -> Unit)? = null,
    onContentAreaPhotoUriChange: ((String?) -> Unit)? = null,
    onContentAreaPhotoAlphaChange: ((Float) -> Unit)? = null,
    onContentAreaPhotoBlurChange: ((Float) -> Unit)? = null,
    onContentAreaPhotoOffsetXChange: ((Float) -> Unit)? = null,
    onContentAreaPhotoOffsetYChange: ((Float) -> Unit)? = null,
    onContentAreaPhotoScaleChange: ((Float) -> Unit)? = null,
    onContentAreaPhotoRotationChange: ((Float) -> Unit)? = null,
    onMemoAreaPhotoUriChange: ((String?) -> Unit)? = null,
    onMemoAreaPhotoAlphaChange: ((Float) -> Unit)? = null,
    onMemoAreaPhotoBlurChange: ((Float) -> Unit)? = null,
    onMemoAreaPhotoOffsetXChange: ((Float) -> Unit)? = null,
    onMemoAreaPhotoOffsetYChange: ((Float) -> Unit)? = null,
    onMemoAreaPhotoScaleChange: ((Float) -> Unit)? = null,
    onMemoAreaPhotoRotationChange: ((Float) -> Unit)? = null,
    onFileListItemPhotoUriChange: ((String?) -> Unit)? = null,
    onFileListItemPhotoAlphaChange: ((Float) -> Unit)? = null,
    onFileListItemPhotoBlurChange: ((Float) -> Unit)? = null,
    onFileListItemPhotoOffsetXChange: ((Float) -> Unit)? = null,
    onFileListItemPhotoOffsetYChange: ((Float) -> Unit)? = null,
    onFileListItemPhotoScaleChange: ((Float) -> Unit)? = null,
    onFileListItemPhotoRotationChange: ((Float) -> Unit)? = null,
    onFileListBackgroundPhotoUriChange: ((String?) -> Unit)? = null,
    onFileListBackgroundPhotoAlphaChange: ((Float) -> Unit)? = null,
    onFileListBackgroundPhotoBlurChange: ((Float) -> Unit)? = null,
    onFileListBackgroundPhotoOffsetXChange: ((Float) -> Unit)? = null,
    onFileListBackgroundPhotoOffsetYChange: ((Float) -> Unit)? = null,
    onFileListBackgroundPhotoScaleChange: ((Float) -> Unit)? = null,
    onFileListBackgroundPhotoRotationChange: ((Float) -> Unit)? = null,
    // Current photo state for each area
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
    fileListItemPhotoUri: String? = null,
    fileListItemPhotoAlpha: Float = 1f,
    fileListItemPhotoBlur: Float = 0f,
    fileListItemPhotoOffsetX: Float = 0f,
    fileListItemPhotoOffsetY: Float = 0f,
    fileListItemPhotoScale: Float = 1f,
    fileListItemPhotoRotation: Float = 0f,
    fileListBackgroundPhotoUri: String? = null,
    fileListBackgroundPhotoAlpha: Float = 1f,
    fileListBackgroundPhotoBlur: Float = 0f,
    fileListBackgroundPhotoOffsetX: Float = 0f,
    fileListBackgroundPhotoOffsetY: Float = 0f,
    fileListBackgroundPhotoScale: Float = 1f,
    fileListBackgroundPhotoRotation: Float = 0f,
    // Photo background modes
    customBgPhotoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
    onCustomBgPhotoBackgroundModeChange: ((com.j4.texter2025.data.PhotoBackgroundMode) -> Unit)? = null,
    contentAreaPhotoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
    onContentAreaPhotoBackgroundModeChange: ((com.j4.texter2025.data.PhotoBackgroundMode) -> Unit)? = null,
    memoAreaPhotoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
    onMemoAreaPhotoBackgroundModeChange: ((com.j4.texter2025.data.PhotoBackgroundMode) -> Unit)? = null,
    fileListItemPhotoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
    onFileListItemPhotoBackgroundModeChange: ((com.j4.texter2025.data.PhotoBackgroundMode) -> Unit)? = null,
    fileListBackgroundPhotoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
    onFileListBackgroundPhotoBackgroundModeChange: ((com.j4.texter2025.data.PhotoBackgroundMode) -> Unit)? = null,
    // Color preset parameters
    colorPresets: List<com.j4.texter2025.data.ColorPreset> = emptyList(),
    selectedPresetForEditing: com.j4.texter2025.data.ColorPreset? = null,
    onSelectPresetForEditing: (com.j4.texter2025.data.ColorPreset?) -> Unit = {},
    onSavePreset: (String) -> Unit = {},
    onApplyPreset: (com.j4.texter2025.data.ColorPreset) -> Unit = {},
    onDeletePreset: (com.j4.texter2025.data.ColorPreset) -> Unit = {},
    onRenamePreset: (com.j4.texter2025.data.ColorPreset, String) -> Unit = { _, _ -> },
    onUpdatePreset: (com.j4.texter2025.data.ColorPreset) -> Unit = {},
    onResetColorsForNewPreset: () -> Unit = {},
    settingsReturnSection: String? = null,
    settingsReturnRequestId: Int = 0,
    settingsReturnReopenEditor: Boolean = false,
    // Test callback to close settings
    onTestInApp: ((String) -> Unit)? = null
) {
    // Global drag state
    var isDragging by remember { mutableStateOf(false) }
    var draggedColor by remember { mutableStateOf<Color?>(null) }
    var draggedFromSection by remember { mutableStateOf<String?>(null) }
    
    // Save preset dialog state
    var showSaveDialog by remember { mutableStateOf(false) }
    
    // Global dialog state for drag options
    var showDragOptions by remember { mutableStateOf(false) }
    var colorToDrag by remember { mutableStateOf<Color?>(null) }
    var photoUriToDrag by remember { mutableStateOf<String?>(null) } // Track photoUri for photo squares
    var photoAlphaToDrag by remember { mutableStateOf(1f) } // Track alpha for photo squares
    var photoBlurToDrag by remember { mutableStateOf(0f) } // Track blur for photo squares
    var dragOptionsSection by remember { mutableStateOf<String?>(null) }
    
    // Delete mode state
    var isDeleteMode by remember { mutableStateOf(false) }
    
    // Deletion animation state for dialog - track color, photoUri, and section
    var colorBeingDeletedFromDialog by remember { mutableStateOf<Triple<Color, String?, String>?>(null) }
    
    // Addition animation state - track both color and section
    var colorBeingAddedFromDialog by remember { mutableStateOf<Pair<Color, String>?>(null) }

    fun closeDragOptionsDialog() {
        showDragOptions = false
        colorToDrag = null
        photoUriToDrag = null
        photoAlphaToDrag = 1f
        photoBlurToDrag = 0f
        dragOptionsSection = null
    }
    
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .clip(MaterialTheme.shapes.medium)
            .blur(if (showDragOptions) 8.dp else 0.dp)
            .pointerInput(showDragOptions) {
                if (showDragOptions) {
                    detectTapGestures(onTap = { })
                }
            }
    ) {
        // Color Presets Section
        ColorPresetsSection(
            presets = colorPresets,
            selectedPresetForEditing = selectedPresetForEditing,
            onSelectPresetForEditing = onSelectPresetForEditing,
            onSavePreset = onSavePreset,
            onApplyPreset = onApplyPreset,
            onDeletePreset = onDeletePreset,
            onRenamePreset = onRenamePreset,
            onUpdatePreset = onUpdatePreset,
            onResetColorsForNewPreset = onResetColorsForNewPreset,
            currentPhotoUri = fileListBackgroundPhotoUri,
            currentPhotoOffsetX = fileListBackgroundPhotoOffsetX,
            currentPhotoOffsetY = fileListBackgroundPhotoOffsetY,
            currentPhotoScale = fileListBackgroundPhotoScale
        )
        
        // Only show color categories when a preset is selected or being created
        if (selectedPresetForEditing != null || colorPresets.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Background Colors", style = MaterialTheme.typography.bodyMedium)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Opened Note Background Color with drop zone
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isDragging && draggedFromSection != "bg") 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) 
                    else Color.Transparent,
                    MaterialTheme.shapes.medium
                )
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Opened Note Background", style = MaterialTheme.typography.bodySmall)
                if (isDragging && draggedFromSection != "bg") {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Drop here to copy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            ColorPresetRow(
                selectedColor = customBgColor,
                onColorSelected = onCustomBgColorChange,
                presetColors = customBgColors,
                onAddCustomColor = onAddBgColor,
                onAddBgColorWithPositioning = onAddBgColorWithPositioning,
                onDeleteCustomColor = onDeleteBgColor,
                onEditCustomColor = onEditBgColor,
                currentBlur = customBgBlur,
                onBlurChange = onCustomBgBlurChange,
                sectionType = "bg",
                onColorDraggedToSection = onColorDraggedBetweenSections,
                globalDragState = Triple(isDragging, draggedColor, draggedFromSection),
                onGlobalDragStateChange = { dragging, color, section ->
                    isDragging = dragging
                    draggedColor = color
                    draggedFromSection = section
                },
                onShowDragOptions = { color, photoUri, alpha, blur, section ->
                    colorToDrag = color
                    photoUriToDrag = photoUri
                    photoAlphaToDrag = alpha
                    photoBlurToDrag = blur
                    dragOptionsSection = section
                    showDragOptions = true
                },
                isDeleteMode = isDeleteMode,
                onExitDeleteMode = { isDeleteMode = false },
                colorBeingDeletedFromDialog = colorBeingDeletedFromDialog,
                colorBeingAddedFromDialog = colorBeingAddedFromDialog,
                // Pass current app colors for preview
                appCustomBgColor = customBgColor,
                appContentAreaColor = contentAreaColor,
                appMemoAreaColor = memoAreaColor,
                appFileListItemColor = fileListItemColor,
                appFileListBackgroundColor = fileListBackgroundColor,
                // Pass photo background callbacks for custom background
                onBackgroundPhotoUriChange = onCustomBgPhotoUriChange,
                onBackgroundPhotoAlphaChange = onCustomBgPhotoAlphaChange,
                onBackgroundPhotoBlurChange = onCustomBgPhotoBlurChange,
                onBackgroundPhotoOffsetXChange = onCustomBgPhotoOffsetXChange,
                onBackgroundPhotoOffsetYChange = onCustomBgPhotoOffsetYChange,
                onBackgroundPhotoScaleChange = onCustomBgPhotoScaleChange,
                onBackgroundPhotoRotationChange = onCustomBgPhotoRotationChange,
                // Pass current photo state
                currentPhotoUri = customBgPhotoUri,
                currentPhotoAlpha = customBgPhotoAlpha,
                currentPhotoBlur = customBgPhotoBlur,
                currentPhotoOffsetX = customBgPhotoOffsetX,
                currentPhotoOffsetY = customBgPhotoOffsetY,
                currentPhotoScale = customBgPhotoScale,
                currentPhotoRotation = customBgPhotoRotation,
                // Pass all sections' photo state for preview
                appCustomBgPhotoUri = customBgPhotoUri,
                appCustomBgPhotoAlpha = customBgPhotoAlpha,
                appCustomBgPhotoBlur = customBgPhotoBlur,
                appCustomBgPhotoOffsetX = customBgPhotoOffsetX,
                appCustomBgPhotoOffsetY = customBgPhotoOffsetY,
                appCustomBgPhotoScale = customBgPhotoScale,
                appContentAreaPhotoUri = contentAreaPhotoUri,
                appContentAreaPhotoAlpha = contentAreaPhotoAlpha,
                appContentAreaPhotoBlur = contentAreaPhotoBlur,
                appContentAreaPhotoOffsetX = contentAreaPhotoOffsetX,
                appContentAreaPhotoOffsetY = contentAreaPhotoOffsetY,
                appContentAreaPhotoScale = contentAreaPhotoScale,
                appMemoAreaPhotoUri = memoAreaPhotoUri,
                appMemoAreaPhotoAlpha = memoAreaPhotoAlpha,
                appMemoAreaPhotoBlur = memoAreaPhotoBlur,
                appMemoAreaPhotoOffsetX = memoAreaPhotoOffsetX,
                appMemoAreaPhotoOffsetY = memoAreaPhotoOffsetY,
                appMemoAreaPhotoScale = memoAreaPhotoScale,
                appFileListBgPhotoUri = fileListBackgroundPhotoUri,
                appFileListBgPhotoAlpha = fileListBackgroundPhotoAlpha,
                appFileListBgPhotoBlur = fileListBackgroundPhotoBlur,
                appFileListBgPhotoOffsetX = fileListBackgroundPhotoOffsetX,
                appFileListBgPhotoOffsetY = fileListBackgroundPhotoOffsetY,
                appFileListBgPhotoScale = fileListBackgroundPhotoScale,
                appFileListItemPhotoUri = fileListItemPhotoUri,
                appFileListItemPhotoAlpha = fileListItemPhotoAlpha,
                appFileListItemPhotoBlur = fileListItemPhotoBlur,
                appFileListItemPhotoOffsetX = fileListItemPhotoOffsetX,
                appFileListItemPhotoOffsetY = fileListItemPhotoOffsetY,
                appFileListItemPhotoScale = fileListItemPhotoScale,
                // Photo background mode
                currentPhotoBackgroundMode = customBgPhotoBackgroundMode,
                onPhotoBackgroundModeChange = onCustomBgPhotoBackgroundModeChange,
                settingsReturnSection = settingsReturnSection,
                settingsReturnRequestId = settingsReturnRequestId,
                settingsReturnReopenEditor = settingsReturnReopenEditor,
                onTestInApp = onTestInApp
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Content Area Color with drop zone
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isDragging && draggedFromSection != "content") 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) 
                    else Color.Transparent
                )
                .padding(8.dp)
                .clickable(enabled = isDragging && draggedFromSection != "content") {
                    if (isDragging && draggedColor != null && draggedFromSection != "content") {
                        onColorDraggedBetweenSections?.invoke(draggedColor!!, null, 1f, 0f, draggedFromSection ?: "", "content")
                    }
                }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Content Area Color", style = MaterialTheme.typography.bodySmall)
                if (isDragging && draggedFromSection != "content") {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Drop here to copy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        ColorPresetRow(
            selectedColor = contentAreaColor,
            onColorSelected = onContentAreaColorChange,
            presetColors = customContentAreaColors,
            onAddCustomColor = onAddContentAreaColor,
            onAddCustomColorWithPositioning = onAddContentAreaColorWithPositioning,
            onDeleteCustomColor = onDeleteContentAreaColor,
            onEditCustomColor = { oldColor, newColor, newBlur, newPhotoUri, offsetX, offsetY, scale, rotation ->
                onEditContentAreaColor?.invoke(oldColor, newColor, newBlur, newPhotoUri, offsetX, offsetY, scale, rotation)
            },
            currentBlur = contentAreaBlur,
            onBlurChange = onContentAreaBlurChange,
            sectionType = "content",
            onColorDraggedToSection = onColorDraggedBetweenSections,
            globalDragState = Triple(isDragging, draggedColor, draggedFromSection),
            onGlobalDragStateChange = { dragging, color, fromSection ->
                isDragging = dragging
                draggedColor = color
                draggedFromSection = fromSection
            },
            onShowDragOptions = { color, photoUri, alpha, blur, section ->
                colorToDrag = color
                photoUriToDrag = photoUri
                photoAlphaToDrag = alpha
                photoBlurToDrag = blur
                dragOptionsSection = section
                showDragOptions = true
            },
            isDeleteMode = isDeleteMode,
            onExitDeleteMode = { isDeleteMode = false },
            colorBeingDeletedFromDialog = colorBeingDeletedFromDialog,
            colorBeingAddedFromDialog = colorBeingAddedFromDialog,
            // Pass current app colors for preview
            appCustomBgColor = customBgColor,
            appContentAreaColor = contentAreaColor,
            appMemoAreaColor = memoAreaColor,
            appFileListItemColor = fileListItemColor,
            appFileListBackgroundColor = fileListBackgroundColor,
            // Pass content area photo background callbacks
            onBackgroundPhotoUriChange = onContentAreaPhotoUriChange,
            onBackgroundPhotoAlphaChange = onContentAreaPhotoAlphaChange,
            onBackgroundPhotoBlurChange = onContentAreaPhotoBlurChange,
            // Pass to section-specific callbacks for real-time positioning updates
            onContentAreaPhotoOffsetXChange = onContentAreaPhotoOffsetXChange,
            onContentAreaPhotoOffsetYChange = onContentAreaPhotoOffsetYChange,
            onContentAreaPhotoScaleChange = onContentAreaPhotoScaleChange,
            onContentAreaPhotoRotationChange = onContentAreaPhotoRotationChange,
            // Pass current photo state
            currentPhotoUri = contentAreaPhotoUri,
            currentPhotoAlpha = contentAreaPhotoAlpha,
            currentPhotoBlur = contentAreaPhotoBlur,
            currentPhotoOffsetX = contentAreaPhotoOffsetX,
            currentPhotoOffsetY = contentAreaPhotoOffsetY,
            currentPhotoScale = contentAreaPhotoScale,
            currentPhotoRotation = contentAreaPhotoRotation,
            // Pass all sections' photo state for preview
            appCustomBgPhotoUri = customBgPhotoUri,
            appCustomBgPhotoAlpha = customBgPhotoAlpha,
            appCustomBgPhotoBlur = customBgPhotoBlur,
            appCustomBgPhotoOffsetX = customBgPhotoOffsetX,
            appCustomBgPhotoOffsetY = customBgPhotoOffsetY,
            appCustomBgPhotoScale = customBgPhotoScale,
            appContentAreaPhotoUri = contentAreaPhotoUri,
            appContentAreaPhotoAlpha = contentAreaPhotoAlpha,
            appContentAreaPhotoBlur = contentAreaPhotoBlur,
            appContentAreaPhotoOffsetX = contentAreaPhotoOffsetX,
            appContentAreaPhotoOffsetY = contentAreaPhotoOffsetY,
            appContentAreaPhotoScale = contentAreaPhotoScale,
            appMemoAreaPhotoUri = memoAreaPhotoUri,
            appMemoAreaPhotoAlpha = memoAreaPhotoAlpha,
            appMemoAreaPhotoBlur = memoAreaPhotoBlur,
            appMemoAreaPhotoOffsetX = memoAreaPhotoOffsetX,
            appMemoAreaPhotoOffsetY = memoAreaPhotoOffsetY,
            appMemoAreaPhotoScale = memoAreaPhotoScale,
            appFileListBgPhotoUri = fileListBackgroundPhotoUri,
            appFileListBgPhotoAlpha = fileListBackgroundPhotoAlpha,
            appFileListBgPhotoBlur = fileListBackgroundPhotoBlur,
            appFileListBgPhotoOffsetX = fileListBackgroundPhotoOffsetX,
            appFileListBgPhotoOffsetY = fileListBackgroundPhotoOffsetY,
            appFileListBgPhotoScale = fileListBackgroundPhotoScale,
            appFileListItemPhotoUri = fileListItemPhotoUri,
            appFileListItemPhotoAlpha = fileListItemPhotoAlpha,
            appFileListItemPhotoBlur = fileListItemPhotoBlur,
            appFileListItemPhotoOffsetX = fileListItemPhotoOffsetX,
            appFileListItemPhotoOffsetY = fileListItemPhotoOffsetY,
            appFileListItemPhotoScale = fileListItemPhotoScale,
            // Photo background mode
            currentPhotoBackgroundMode = contentAreaPhotoBackgroundMode,
            onPhotoBackgroundModeChange = onContentAreaPhotoBackgroundModeChange,
            settingsReturnSection = settingsReturnSection,
            settingsReturnRequestId = settingsReturnRequestId,
            settingsReturnReopenEditor = settingsReturnReopenEditor,
            onTestInApp = onTestInApp
        )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Memo Area Color with drop zone
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isDragging && draggedFromSection != "memo") 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) 
                    else Color.Transparent,
                    MaterialTheme.shapes.medium
                )
                .padding(8.dp)
                .clickable(enabled = isDragging && draggedFromSection != "memo") {
                    if (isDragging && draggedColor != null && draggedFromSection != "memo") {
                        onColorDraggedBetweenSections?.invoke(draggedColor!!, null, 1f, 0f, draggedFromSection ?: "", "memo")
                    }
                }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Memo Area Color", style = MaterialTheme.typography.bodySmall)
                if (isDragging && draggedFromSection != "memo") {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Drop here to copy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        ColorPresetRow(
            selectedColor = memoAreaColor,
            onColorSelected = onMemoAreaColorChange,
            presetColors = customMemoAreaColors,
            onAddCustomColor = onAddMemoAreaColor,
            onAddMemoAreaColorWithPositioning = onAddMemoAreaColorWithPositioning,
            onDeleteCustomColor = onDeleteMemoAreaColor,
            onEditCustomColor = { oldColor, newColor, newBlur, newPhotoUri, offsetX, offsetY, scale, rotation ->
                onEditMemoAreaColor?.invoke(oldColor, newColor, newBlur, newPhotoUri, offsetX, offsetY, scale, rotation)
            },
            currentBlur = memoAreaBlur,
            onBlurChange = onMemoAreaBlurChange,
            sectionType = "memo",
            onColorDraggedToSection = onColorDraggedBetweenSections,
            globalDragState = Triple(isDragging, draggedColor, draggedFromSection),
            onGlobalDragStateChange = { dragging, color, fromSection ->
                isDragging = dragging
                draggedColor = color
                draggedFromSection = fromSection
            },
            onShowDragOptions = { color, photoUri, alpha, blur, section ->
                colorToDrag = color
                photoUriToDrag = photoUri
                photoAlphaToDrag = alpha
                photoBlurToDrag = blur
                dragOptionsSection = section
                showDragOptions = true
            },
            isDeleteMode = isDeleteMode,
            onExitDeleteMode = { isDeleteMode = false },
            colorBeingDeletedFromDialog = colorBeingDeletedFromDialog,
            colorBeingAddedFromDialog = colorBeingAddedFromDialog,
            // Pass current app colors for preview
            appCustomBgColor = customBgColor,
            appContentAreaColor = contentAreaColor,
            appMemoAreaColor = memoAreaColor,
            appFileListItemColor = fileListItemColor,
            appFileListBackgroundColor = fileListBackgroundColor,
            // Pass memo area photo background callbacks
            onBackgroundPhotoUriChange = onMemoAreaPhotoUriChange,
            onBackgroundPhotoAlphaChange = onMemoAreaPhotoAlphaChange,
            onBackgroundPhotoBlurChange = onMemoAreaPhotoBlurChange,
            // Pass to section-specific callbacks for real-time positioning updates
            onMemoAreaPhotoOffsetXChange = onMemoAreaPhotoOffsetXChange,
            onMemoAreaPhotoOffsetYChange = onMemoAreaPhotoOffsetYChange,
            onMemoAreaPhotoScaleChange = onMemoAreaPhotoScaleChange,
            onMemoAreaPhotoRotationChange = onMemoAreaPhotoRotationChange,
            // Pass current photo state
            currentPhotoUri = memoAreaPhotoUri,
            currentPhotoAlpha = memoAreaPhotoAlpha,
            currentPhotoBlur = memoAreaPhotoBlur,
            currentPhotoOffsetX = memoAreaPhotoOffsetX,
            currentPhotoOffsetY = memoAreaPhotoOffsetY,
            currentPhotoScale = memoAreaPhotoScale,
            currentPhotoRotation = memoAreaPhotoRotation,
            // Pass all sections' photo state for preview
            appCustomBgPhotoUri = customBgPhotoUri,
            appCustomBgPhotoAlpha = customBgPhotoAlpha,
            appCustomBgPhotoBlur = customBgPhotoBlur,
            appCustomBgPhotoOffsetX = customBgPhotoOffsetX,
            appCustomBgPhotoOffsetY = customBgPhotoOffsetY,
            appCustomBgPhotoScale = customBgPhotoScale,
            appContentAreaPhotoUri = contentAreaPhotoUri,
            appContentAreaPhotoAlpha = contentAreaPhotoAlpha,
            appContentAreaPhotoBlur = contentAreaPhotoBlur,
            appContentAreaPhotoOffsetX = contentAreaPhotoOffsetX,
            appContentAreaPhotoOffsetY = contentAreaPhotoOffsetY,
            appContentAreaPhotoScale = contentAreaPhotoScale,
            appMemoAreaPhotoUri = memoAreaPhotoUri,
            appMemoAreaPhotoAlpha = memoAreaPhotoAlpha,
            appMemoAreaPhotoBlur = memoAreaPhotoBlur,
            appMemoAreaPhotoOffsetX = memoAreaPhotoOffsetX,
            appMemoAreaPhotoOffsetY = memoAreaPhotoOffsetY,
            appMemoAreaPhotoScale = memoAreaPhotoScale,
            appFileListBgPhotoUri = fileListBackgroundPhotoUri,
            appFileListBgPhotoAlpha = fileListBackgroundPhotoAlpha,
            appFileListBgPhotoBlur = fileListBackgroundPhotoBlur,
            appFileListBgPhotoOffsetX = fileListBackgroundPhotoOffsetX,
            appFileListBgPhotoOffsetY = fileListBackgroundPhotoOffsetY,
            appFileListBgPhotoScale = fileListBackgroundPhotoScale,
            appFileListItemPhotoUri = fileListItemPhotoUri,
            appFileListItemPhotoAlpha = fileListItemPhotoAlpha,
            appFileListItemPhotoBlur = fileListItemPhotoBlur,
            appFileListItemPhotoOffsetX = fileListItemPhotoOffsetX,
            appFileListItemPhotoOffsetY = fileListItemPhotoOffsetY,
            appFileListItemPhotoScale = fileListItemPhotoScale,
            // Photo background mode
            currentPhotoBackgroundMode = memoAreaPhotoBackgroundMode,
            onPhotoBackgroundModeChange = onMemoAreaPhotoBackgroundModeChange,
            settingsReturnSection = settingsReturnSection,
            settingsReturnRequestId = settingsReturnRequestId,
            settingsReturnReopenEditor = settingsReturnReopenEditor,
            onTestInApp = onTestInApp
        )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // File List Item Color with drop zone
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isDragging && draggedFromSection != "fileListItem") 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) 
                    else Color.Transparent,
                    MaterialTheme.shapes.medium
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("File List Item Color", style = MaterialTheme.typography.bodySmall)
                if (isDragging && draggedFromSection != "fileListItem") {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Drop here to copy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            // Connect to the actual backend parameters for FileList colors
            ColorPresetRow(
                selectedColor = fileListItemColor,
                onColorSelected = onFileListItemColorChange,
                presetColors = customFileListItemColors,
                onAddCustomColor = onAddFileListItemColor,
                onAddFileListItemColorWithPositioning = onAddFileListItemColorWithPositioning,
                onDeleteCustomColor = onDeleteFileListItemColor,
                onEditCustomColor = { oldColor, newColor, newBlur, newPhotoUri, offsetX, offsetY, scale, rotation ->
                    onEditFileListItemColor?.invoke(oldColor, newColor, newBlur, newPhotoUri, offsetX, offsetY, scale, rotation)
                },
                currentBlur = fileListItemBlur,
                onBlurChange = onFileListItemBlurChange,
                sectionType = "fileListItem",
                onColorDraggedToSection = onColorDraggedBetweenSections,
                globalDragState = Triple(isDragging, draggedColor, draggedFromSection),
                onGlobalDragStateChange = { dragging, color, fromSection ->
                    isDragging = dragging
                    draggedColor = color
                    draggedFromSection = fromSection
                },
                onShowDragOptions = { color, photoUri, alpha, blur, section ->
                    colorToDrag = color
                    photoUriToDrag = photoUri
                    photoAlphaToDrag = alpha
                    photoBlurToDrag = blur
                    dragOptionsSection = section
                    showDragOptions = true
                },
                isDeleteMode = isDeleteMode,
                onExitDeleteMode = { isDeleteMode = false },
                // Pass current app colors for preview
                appCustomBgColor = customBgColor,
                appContentAreaColor = contentAreaColor,
                appMemoAreaColor = memoAreaColor,
                appFileListItemColor = fileListItemColor,
                appFileListBackgroundColor = fileListBackgroundColor,
                // Pass file list item photo background callbacks
                onBackgroundPhotoUriChange = onFileListItemPhotoUriChange,
                onBackgroundPhotoAlphaChange = onFileListItemPhotoAlphaChange,
                onBackgroundPhotoBlurChange = onFileListItemPhotoBlurChange,
                // Pass to section-specific callbacks for real-time positioning updates
                onFileListItemPhotoOffsetXChange = onFileListItemPhotoOffsetXChange,
                onFileListItemPhotoOffsetYChange = onFileListItemPhotoOffsetYChange,
                onFileListItemPhotoScaleChange = onFileListItemPhotoScaleChange,
                onFileListItemPhotoRotationChange = onFileListItemPhotoRotationChange,
                // Pass current photo state
                currentPhotoUri = fileListItemPhotoUri,
                currentPhotoAlpha = fileListItemPhotoAlpha,
                currentPhotoBlur = fileListItemPhotoBlur,
                currentPhotoOffsetX = fileListItemPhotoOffsetX,
                currentPhotoOffsetY = fileListItemPhotoOffsetY,
                currentPhotoScale = fileListItemPhotoScale,
                currentPhotoRotation = fileListItemPhotoRotation,
                // Pass all sections' photo state for preview
                appCustomBgPhotoUri = customBgPhotoUri,
                appCustomBgPhotoAlpha = customBgPhotoAlpha,
                appCustomBgPhotoBlur = customBgPhotoBlur,
                appCustomBgPhotoOffsetX = customBgPhotoOffsetX,
                appCustomBgPhotoOffsetY = customBgPhotoOffsetY,
                appCustomBgPhotoScale = customBgPhotoScale,
                appContentAreaPhotoUri = contentAreaPhotoUri,
                appContentAreaPhotoAlpha = contentAreaPhotoAlpha,
                appContentAreaPhotoBlur = contentAreaPhotoBlur,
                appContentAreaPhotoOffsetX = contentAreaPhotoOffsetX,
                appContentAreaPhotoOffsetY = contentAreaPhotoOffsetY,
                appContentAreaPhotoScale = contentAreaPhotoScale,
                appMemoAreaPhotoUri = memoAreaPhotoUri,
                appMemoAreaPhotoAlpha = memoAreaPhotoAlpha,
                appMemoAreaPhotoBlur = memoAreaPhotoBlur,
                appMemoAreaPhotoOffsetX = memoAreaPhotoOffsetX,
                appMemoAreaPhotoOffsetY = memoAreaPhotoOffsetY,
                appMemoAreaPhotoScale = memoAreaPhotoScale,
                appFileListBgPhotoUri = fileListBackgroundPhotoUri,
                appFileListBgPhotoAlpha = fileListBackgroundPhotoAlpha,
                appFileListBgPhotoBlur = fileListBackgroundPhotoBlur,
                appFileListBgPhotoOffsetX = fileListBackgroundPhotoOffsetX,
                appFileListBgPhotoOffsetY = fileListBackgroundPhotoOffsetY,
                appFileListBgPhotoScale = fileListBackgroundPhotoScale,
                appFileListItemPhotoUri = fileListItemPhotoUri,
                appFileListItemPhotoAlpha = fileListItemPhotoAlpha,
                appFileListItemPhotoBlur = fileListItemPhotoBlur,
                appFileListItemPhotoOffsetX = fileListItemPhotoOffsetX,
                appFileListItemPhotoOffsetY = fileListItemPhotoOffsetY,
                appFileListItemPhotoScale = fileListItemPhotoScale,
                // Photo background mode
                currentPhotoBackgroundMode = fileListItemPhotoBackgroundMode,
                onPhotoBackgroundModeChange = onFileListItemPhotoBackgroundModeChange,
                settingsReturnSection = settingsReturnSection,
                settingsReturnRequestId = settingsReturnRequestId,
                settingsReturnReopenEditor = settingsReturnReopenEditor,
                onTestInApp = onTestInApp
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // FileList Background Color Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isDragging && draggedFromSection != "fileListBg") 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) 
                    else Color.Transparent,
                    MaterialTheme.shapes.medium
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("FileList Background", style = MaterialTheme.typography.bodySmall)
                if (isDragging && draggedFromSection != "fileListBg") {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Drop here to copy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            ColorPresetRow(
                selectedColor = fileListBackgroundColor,
                onColorSelected = onFileListBackgroundColorChange,
                presetColors = customFileListBackgroundColors,
                onAddCustomColor = onAddFileListBackgroundColor,
                onAddFileListBackgroundColorWithPositioning = onAddFileListBackgroundColorWithPositioning,
                onDeleteCustomColor = onDeleteFileListBackgroundColor,
                onEditCustomColor = { oldColor, newColor, newBlur, newPhotoUri, offsetX, offsetY, scale, rotation ->
                    onEditFileListBackgroundColor?.invoke(oldColor, newColor, newBlur, newPhotoUri, offsetX, offsetY, scale, rotation)
                },
                currentBlur = fileListBackgroundBlur,
                onBlurChange = onFileListBackgroundBlurChange,
                sectionType = "fileListBg",
                onColorDraggedToSection = onColorDraggedBetweenSections,
                globalDragState = Triple(isDragging, draggedColor, draggedFromSection),
                onGlobalDragStateChange = { dragging, color, fromSection ->
                    isDragging = dragging
                    draggedColor = color
                    draggedFromSection = fromSection
                },
                onShowDragOptions = { color, photoUri, alpha, blur, section ->
                    colorToDrag = color
                    photoUriToDrag = photoUri
                    photoAlphaToDrag = alpha
                    photoBlurToDrag = blur
                    dragOptionsSection = section
                    showDragOptions = true
                },
                isDeleteMode = isDeleteMode,
                onExitDeleteMode = { isDeleteMode = false },
                colorBeingDeletedFromDialog = colorBeingDeletedFromDialog,
                colorBeingAddedFromDialog = colorBeingAddedFromDialog,
                // Pass current app colors for preview
                appCustomBgColor = customBgColor,
                appContentAreaColor = contentAreaColor,
                appMemoAreaColor = memoAreaColor,
                appFileListItemColor = fileListItemColor,
                appFileListBackgroundColor = fileListBackgroundColor,
                // Pass file list background photo background callbacks
                onBackgroundPhotoUriChange = onFileListBackgroundPhotoUriChange,
                onBackgroundPhotoAlphaChange = onFileListBackgroundPhotoAlphaChange,
                onBackgroundPhotoBlurChange = onFileListBackgroundPhotoBlurChange,
                // Pass to section-specific callbacks for real-time positioning updates
                onFileListBgPhotoOffsetXChange = onFileListBackgroundPhotoOffsetXChange,
                onFileListBgPhotoOffsetYChange = onFileListBackgroundPhotoOffsetYChange,
                onFileListBgPhotoScaleChange = onFileListBackgroundPhotoScaleChange,
                onFileListBgPhotoRotationChange = onFileListBackgroundPhotoRotationChange,
                // Pass current photo state
                currentPhotoUri = fileListBackgroundPhotoUri,
                currentPhotoAlpha = fileListBackgroundPhotoAlpha,
                currentPhotoBlur = fileListBackgroundPhotoBlur,
                currentPhotoOffsetX = fileListBackgroundPhotoOffsetX,
                currentPhotoOffsetY = fileListBackgroundPhotoOffsetY,
                currentPhotoScale = fileListBackgroundPhotoScale,
                currentPhotoRotation = fileListBackgroundPhotoRotation,
                // Pass all sections' photo state for preview
                appCustomBgPhotoUri = customBgPhotoUri,
                appCustomBgPhotoAlpha = customBgPhotoAlpha,
                appCustomBgPhotoBlur = customBgPhotoBlur,
                appCustomBgPhotoOffsetX = customBgPhotoOffsetX,
                appCustomBgPhotoOffsetY = customBgPhotoOffsetY,
                appCustomBgPhotoScale = customBgPhotoScale,
                appContentAreaPhotoUri = contentAreaPhotoUri,
                appContentAreaPhotoAlpha = contentAreaPhotoAlpha,
                appContentAreaPhotoBlur = contentAreaPhotoBlur,
                appContentAreaPhotoOffsetX = contentAreaPhotoOffsetX,
                appContentAreaPhotoOffsetY = contentAreaPhotoOffsetY,
                appContentAreaPhotoScale = contentAreaPhotoScale,
                appMemoAreaPhotoUri = memoAreaPhotoUri,
                appMemoAreaPhotoAlpha = memoAreaPhotoAlpha,
                appMemoAreaPhotoBlur = memoAreaPhotoBlur,
                appMemoAreaPhotoOffsetX = memoAreaPhotoOffsetX,
                appMemoAreaPhotoOffsetY = memoAreaPhotoOffsetY,
                appMemoAreaPhotoScale = memoAreaPhotoScale,
                appFileListBgPhotoUri = fileListBackgroundPhotoUri,
                appFileListBgPhotoAlpha = fileListBackgroundPhotoAlpha,
                appFileListBgPhotoBlur = fileListBackgroundPhotoBlur,
                appFileListBgPhotoOffsetX = fileListBackgroundPhotoOffsetX,
                appFileListBgPhotoOffsetY = fileListBackgroundPhotoOffsetY,
                appFileListBgPhotoScale = fileListBackgroundPhotoScale,
                appFileListItemPhotoUri = fileListItemPhotoUri,
                appFileListItemPhotoAlpha = fileListItemPhotoAlpha,
                appFileListItemPhotoBlur = fileListItemPhotoBlur,
                appFileListItemPhotoOffsetX = fileListItemPhotoOffsetX,
                appFileListItemPhotoOffsetY = fileListItemPhotoOffsetY,
                appFileListItemPhotoScale = fileListItemPhotoScale,
                // Photo background mode
                currentPhotoBackgroundMode = fileListBackgroundPhotoBackgroundMode,
                onPhotoBackgroundModeChange = onFileListBackgroundPhotoBackgroundModeChange,
                settingsReturnSection = settingsReturnSection,
                settingsReturnRequestId = settingsReturnRequestId,
                settingsReturnReopenEditor = settingsReturnReopenEditor,
                onTestInApp = onTestInApp
            )
        }
        } // End of color categories conditional visibility
    }
    
    // Drag options dialog overlay - real modal dialog to prevent click-through
    if (showDragOptions && colorToDrag != null && dragOptionsSection != null) {
        Dialog(
            onDismissRequest = { closeDragOptionsDialog() },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { closeDragOptionsDialog() })
                        }
                )

                // Dialog card
                Card(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                    // Close button row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Color Actions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = Color.Black.copy(alpha = 0.1f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    closeDragOptionsDialog()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "Copy to:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (dragOptionsSection != "bg") {
                            OutlinedButton(
                                onClick = {
                                    onColorDraggedBetweenSections?.invoke(colorToDrag!!, photoUriToDrag, photoAlphaToDrag, photoBlurToDrag, dragOptionsSection!!, "bg")
                                    showDragOptions = false
                                    colorToDrag = null
                                    photoUriToDrag = null
                                    photoAlphaToDrag = 1f
                                    photoBlurToDrag = 0f
                                    dragOptionsSection = null
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    "📱 Background Colors",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        if (dragOptionsSection != "content") {
                            OutlinedButton(
                                onClick = {
                                    onColorDraggedBetweenSections?.invoke(colorToDrag!!, photoUriToDrag, photoAlphaToDrag, photoBlurToDrag, dragOptionsSection!!, "content")
                                    showDragOptions = false
                                    colorToDrag = null
                                    photoUriToDrag = null
                                    photoAlphaToDrag = 1f
                                    photoBlurToDrag = 0f
                                    dragOptionsSection = null
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    "📝 Content Area Colors",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        if (dragOptionsSection != "memo") {
                            OutlinedButton(
                                onClick = {
                                    onColorDraggedBetweenSections?.invoke(colorToDrag!!, photoUriToDrag, photoAlphaToDrag, photoBlurToDrag, dragOptionsSection!!, "memo")
                                    showDragOptions = false
                                    colorToDrag = null
                                    photoUriToDrag = null
                                    photoAlphaToDrag = 1f
                                    photoBlurToDrag = 0f
                                    dragOptionsSection = null
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    "📋 Memo Area Colors",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        if (dragOptionsSection != "fileListItem") {
                            OutlinedButton(
                                onClick = {
                                    onColorDraggedBetweenSections?.invoke(colorToDrag!!, photoUriToDrag, photoAlphaToDrag, photoBlurToDrag, dragOptionsSection!!, "fileListItem")
                                    showDragOptions = false
                                    colorToDrag = null
                                    photoUriToDrag = null
                                    photoAlphaToDrag = 1f
                                    photoBlurToDrag = 0f
                                    dragOptionsSection = null
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    "📄 File List Items",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        if (dragOptionsSection != "fileListBg") {
                            OutlinedButton(
                                onClick = {
                                    onColorDraggedBetweenSections?.invoke(colorToDrag!!, photoUriToDrag, photoAlphaToDrag, photoBlurToDrag, dragOptionsSection!!, "fileListBg")
                                    showDragOptions = false
                                    colorToDrag = null
                                    photoUriToDrag = null
                                    photoAlphaToDrag = 1f
                                    photoBlurToDrag = 0f
                                    dragOptionsSection = null
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    "🖼️ File List Background",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Single Delete button that deletes color AND enters multiple delete mode
                        Button(
                            onClick = {
                                val colorToDelete = colorToDrag!!
                                val photoUriToDelete = photoUriToDrag // Capture the photoUri
                                val sectionToDelete = dragOptionsSection!!
                                
                                // Close dialog first
                                showDragOptions = false
                                colorToDrag = null
                                photoUriToDrag = null
                                dragOptionsSection = null
                                
                                coroutineScope.launch {
                                    // Small delay to let dialog close animation complete
                                    delay(200)
                                    
                                    // Start deletion animation - track color, photoUri, and section
                                    colorBeingDeletedFromDialog = Triple(colorToDelete, photoUriToDelete, sectionToDelete)
                                    
                                    // Wait for animation to complete
                                    delay(400) // Animation duration
                                    
                                    // Delete the specific color/photo that was long-pressed
                                    when (sectionToDelete) {
                                        "bg" -> onDeleteBgColor(colorToDelete, photoUriToDelete)
                                        "content" -> onDeleteContentAreaColor(colorToDelete, photoUriToDelete)
                                        "memo" -> onDeleteMemoAreaColor(colorToDelete, photoUriToDelete)
                                        "fileListItem" -> onDeleteFileListItemColor(colorToDelete, photoUriToDelete)
                                        "fileListBg" -> onDeleteFileListBackgroundColor(colorToDelete, photoUriToDelete)
                                    }
                                    
                                    // Reset animation state
                                    colorBeingDeletedFromDialog = null
                                    
                                    // After deletion, activate delete mode for multiple deletions
                                    isDeleteMode = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                "🗑️ Delete",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
    }
    
    // Save Preset Dialog
    if (showSaveDialog) {
        var presetName by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Color Preset") },
            text = {
                Column {
                    Text("Enter a name for this color preset:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = presetName,
                        onValueChange = { presetName = it },
                        label = { Text("Preset Name") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (presetName.isNotBlank()) {
                            onSavePreset(presetName.trim())
                            showSaveDialog = false
                        }
                    },
                    enabled = presetName.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun FileListColorPickerRow(
    selectedColor: Color?,
    onColorSelected: (Color?) -> Unit,
    customColors: List<Color>,
    onAddCustomColor: (Color) -> Unit,
    onDeleteCustomColor: (Color, String?) -> Unit,
    isDragging: Boolean,
    draggedColor: Color?,
    draggedFromSection: String?,
    onGlobalDragStateChange: (Boolean, Color?, String?) -> Unit
) {
    // Use the exact same ColorPresetRow component as other sections
    ColorPresetRow(
        selectedColor = selectedColor,
        onColorSelected = onColorSelected,
        presetColors = customColors.mapIndexed { index, color -> 
            SimpleColorPreset("Custom ${index + 1}", color) 
        } + listOf(
            SimpleColorPreset("Red", Color.Red),
            SimpleColorPreset("Green", Color.Green),
            SimpleColorPreset("Blue", Color.Blue),
            SimpleColorPreset("Yellow", Color.Yellow),
            SimpleColorPreset("Magenta", Color.Magenta),
            SimpleColorPreset("Cyan", Color.Cyan),
            SimpleColorPreset("Gray", Color(0xFF808080))
        ),
        onAddCustomColor = onAddCustomColor,
        onDeleteCustomColor = onDeleteCustomColor,
        onEditCustomColor = null,
        currentBlur = 0f,
        onBlurChange = { },
        sectionType = "fileListItem",
        onColorDraggedToSection = null,
        globalDragState = Triple(isDragging, draggedColor, draggedFromSection),
        onGlobalDragStateChange = onGlobalDragStateChange,
        // For now, pass null values since this function doesn't have access to app colors
        appCustomBgColor = null,
        appContentAreaColor = null,
        appMemoAreaColor = null,
        appFileListItemColor = null,
        appFileListBackgroundColor = null
    )
}

// Helper Functions and Data Classes

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColorPresetRow(
    selectedColor: Color?,
    onColorSelected: (Color?) -> Unit,
    presetColors: List<SimpleColorPreset>,
    onAddCustomColor: ((Color) -> Unit)? = null,
    onAddCustomColorWithPositioning: ((Color, String, Float, Float, Float, Float, Float, Float) -> Unit)? = null,
    onAddMemoAreaColorWithPositioning: ((Color, String, Float, Float, Float, Float, Float, Float) -> Unit)? = null,
    onAddBgColorWithPositioning: ((Color, String, Float, Float, Float, Float, Float, Float) -> Unit)? = null,
    onAddFileListItemColorWithPositioning: ((Color, String, Float, Float, Float, Float, Float, Float) -> Unit)? = null,
    onAddFileListBackgroundColorWithPositioning: ((Color, String, Float, Float, Float, Float, Float, Float) -> Unit)? = null,
    onDeleteCustomColor: ((Color, String?) -> Unit)? = null, // (color, photoUri) -> Unit
    onEditCustomColor: ((Color, Color, Float, String?, Float, Float, Float, Float) -> Unit)? = null, // (oldColor, newColor, newBlur, oldPhotoUri, offsetX, offsetY, scale, rotation) -> Unit
    currentBlur: Float = 0f,
    onBlurChange: ((Float) -> Unit)? = null,
    sectionType: String = "unknown", // "bg", "content", "memo"
    settingsReturnSection: String? = null,
    settingsReturnRequestId: Int = 0,
    settingsReturnReopenEditor: Boolean = false,
    onColorDraggedToSection: ((Color, String?, Float, Float, String, String) -> Unit)? = null, // (color, photoUri, alpha, blur, fromSection, toSection) -> Unit
    globalDragState: Triple<Boolean, Color?, String?> = Triple(false, null, null), // (isDragging, draggedColor, draggedFromSection)
    onGlobalDragStateChange: ((Boolean, Color?, String?) -> Unit)? = null,
    onShowDragOptions: ((Color, String?, Float, Float, String) -> Unit)? = null, // (color, photoUri, alpha, blur, section) -> Unit
    isDeleteMode: Boolean = false,
    onExitDeleteMode: (() -> Unit)? = null,
    colorBeingDeletedFromDialog: Triple<Color, String?, String>? = null, // (color, photoUri, section)
    colorBeingAddedFromDialog: Pair<Color, String>? = null,
    // Current app colors for preview
    appCustomBgColor: Color? = null,
    appContentAreaColor: Color? = null,
    appMemoAreaColor: Color? = null,
    appFileListItemColor: Color? = null,
    appFileListBackgroundColor: Color? = null,
    // Photo background callbacks (generic - used for bg section)
    onBackgroundPhotoUriChange: ((String?) -> Unit)? = null,
    onBackgroundPhotoAlphaChange: ((Float) -> Unit)? = null,
    onBackgroundPhotoBlurChange: ((Float) -> Unit)? = null,
    onBackgroundPhotoOffsetXChange: ((Float) -> Unit)? = null,
    onBackgroundPhotoOffsetYChange: ((Float) -> Unit)? = null,
    onBackgroundPhotoScaleChange: ((Float) -> Unit)? = null,
    onBackgroundPhotoRotationChange: ((Float) -> Unit)? = null,
    // Section-specific photo positioning callbacks for real-time updates
    onContentAreaPhotoOffsetXChange: ((Float) -> Unit)? = null,
    onContentAreaPhotoOffsetYChange: ((Float) -> Unit)? = null,
    onContentAreaPhotoScaleChange: ((Float) -> Unit)? = null,
    onContentAreaPhotoRotationChange: ((Float) -> Unit)? = null,
    onMemoAreaPhotoOffsetXChange: ((Float) -> Unit)? = null,
    onMemoAreaPhotoOffsetYChange: ((Float) -> Unit)? = null,
    onMemoAreaPhotoScaleChange: ((Float) -> Unit)? = null,
    onMemoAreaPhotoRotationChange: ((Float) -> Unit)? = null,
    onFileListItemPhotoOffsetXChange: ((Float) -> Unit)? = null,
    onFileListItemPhotoOffsetYChange: ((Float) -> Unit)? = null,
    onFileListItemPhotoScaleChange: ((Float) -> Unit)? = null,
    onFileListItemPhotoRotationChange: ((Float) -> Unit)? = null,
    onFileListBgPhotoOffsetXChange: ((Float) -> Unit)? = null,
    onFileListBgPhotoOffsetYChange: ((Float) -> Unit)? = null,
    onFileListBgPhotoScaleChange: ((Float) -> Unit)? = null,
    onFileListBgPhotoRotationChange: ((Float) -> Unit)? = null,
    // Current photo state
    currentPhotoUri: String? = null,
    currentPhotoAlpha: Float = 1f,
    currentPhotoBlur: Float = 0f,
    currentPhotoOffsetX: Float = 0f,
    currentPhotoOffsetY: Float = 0f,
    currentPhotoScale: Float = 1f,
    currentPhotoRotation: Float = 0f,
    // All sections' photo state for preview
    appCustomBgPhotoUri: String? = null,
    appCustomBgPhotoAlpha: Float = 1f,
    appCustomBgPhotoBlur: Float = 0f,
    appCustomBgPhotoOffsetX: Float = 0f,
    appCustomBgPhotoOffsetY: Float = 0f,
    appCustomBgPhotoScale: Float = 1f,
    appContentAreaPhotoUri: String? = null,
    appContentAreaPhotoAlpha: Float = 1f,
    appContentAreaPhotoBlur: Float = 0f,
    appContentAreaPhotoOffsetX: Float = 0f,
    appContentAreaPhotoOffsetY: Float = 0f,
    appContentAreaPhotoScale: Float = 1f,
    appMemoAreaPhotoUri: String? = null,
    appMemoAreaPhotoAlpha: Float = 1f,
    appMemoAreaPhotoBlur: Float = 0f,
    appMemoAreaPhotoOffsetX: Float = 0f,
    appMemoAreaPhotoOffsetY: Float = 0f,
    appMemoAreaPhotoScale: Float = 1f,
    appFileListBgPhotoUri: String? = null,
    appFileListBgPhotoAlpha: Float = 1f,
    appFileListBgPhotoBlur: Float = 0f,
    appFileListBgPhotoOffsetX: Float = 0f,
    appFileListBgPhotoOffsetY: Float = 0f,
    appFileListBgPhotoScale: Float = 1f,
    appFileListItemPhotoUri: String? = null,
    appFileListItemPhotoAlpha: Float = 1f,
    appFileListItemPhotoBlur: Float = 0f,
    appFileListItemPhotoOffsetX: Float = 0f,
    appFileListItemPhotoOffsetY: Float = 0f,
    appFileListItemPhotoScale: Float = 1f,
    // Additional parameters with MainActivity naming convention
    fileListItemPhotoOffsetX: Float = 0f,
    fileListItemPhotoOffsetY: Float = 0f,
    fileListItemPhotoScale: Float = 1f,
    fileListBackgroundPhotoOffsetX: Float = 0f,
    fileListBackgroundPhotoOffsetY: Float = 0f,
    fileListBackgroundPhotoScale: Float = 1f,
    // Photo background mode
    currentPhotoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
    onPhotoBackgroundModeChange: ((com.j4.texter2025.data.PhotoBackgroundMode) -> Unit)? = null,
    // Test callback to close settings
    onTestInApp: ((String) -> Unit)? = null
) {
    var showColorPicker by remember { mutableStateOf(false) }
    var showEditColorPicker by remember { mutableStateOf(false) }
    var colorToEdit by remember { mutableStateOf<Color?>(null) }
    var presetToEdit by remember { mutableStateOf<SimpleColorPreset?>(null) } // Track the full preset being edited
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var colorToDelete by remember { mutableStateOf<Color?>(null) }

    LaunchedEffect(settingsReturnRequestId) {
        if (settingsReturnRequestId <= 0) return@LaunchedEffect
        if (!settingsReturnReopenEditor || settingsReturnSection != sectionType) return@LaunchedEffect

        val matchingPreset = when {
            !currentPhotoUri.isNullOrEmpty() -> {
                presetColors.firstOrNull { it.photoUri == currentPhotoUri }
            }
            selectedColor != null -> {
                presetColors.firstOrNull { it.photoUri.isNullOrEmpty() && it.color == selectedColor }
            }
            else -> null
        }

        val targetPreset = matchingPreset ?: presetColors.firstOrNull()
        colorToEdit = targetPreset?.color ?: selectedColor ?: Color.White
        presetToEdit = targetPreset
        showEditColorPicker = true
    }
    
    // Deletion animation state - track both color and photoUri to uniquely identify photo squares
    var colorBeingDeleted by remember { mutableStateOf<Pair<Color, String?>?>(null) }
    
    // Addition animation state
    var colorBeingAdded by remember { mutableStateOf<Color?>(null) }
    
    // Use global drag state
    val (globalIsDragging, globalDraggedColor, globalDraggedFromSection) = globalDragState
    var localIsDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    
    // Shaking animation for deletion mode with elastic effect - only animate when in delete mode
    val shakeOffset by if (isDeleteMode) {
        val infiniteTransition = rememberInfiniteTransition(label = "shake")
        infiniteTransition.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = infiniteRepeatable(
                animation = tween(150, easing = FastOutSlowInEasing),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
            ),
            label = "shake"
        )
    } else {
        remember { mutableStateOf(0f) }
    }
    
    // Deletion animation for individual colors
    fun animateColorDeletion(color: Color, photoUri: String? = null, onComplete: (() -> Unit)? = null) {
        colorBeingDeleted = Pair(color, photoUri)
        coroutineScope.launch {
            delay(400) // Animation duration
            onDeleteCustomColor?.invoke(color, photoUri)
            colorBeingDeleted = null
            onComplete?.invoke()
        }
    }
    
    // Addition animation for individual colors
    fun animateColorAddition(color: Color, onComplete: (() -> Unit)? = null) {
        // Reset animation state first to ensure clean start
        colorBeingAdded = null
        coroutineScope.launch {
            delay(50) // Small delay to ensure state reset is processed
            colorBeingAdded = color // Trigger animation
            delay(500) // Animation duration (slightly longer for bouncy effect)
            colorBeingAdded = null
            onComplete?.invoke()
        }
    }
    
    // Helper function to determine drop target based on drag distance
    fun determineDropTarget(offset: Offset, currentSection: String): String {
        return when {
            offset.y < -150f -> when (currentSection) {
                "memo" -> "content"
                "content" -> "bg"
                else -> currentSection
            }
            offset.y > 150f -> when (currentSection) {
                "bg" -> "content"
                "content" -> "memo"
                else -> currentSection
            }
            else -> currentSection
        }
    }
    
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Delete mode cancel button
        if (isDeleteMode) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(Color(0xFF87CEEB)) // Soft sky blue
                    .clickable { onExitDeleteMode?.invoke() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Exit delete mode",
                    tint = Color(0xFF1A1A1A), // Dark color for contrast
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        // Scrollable color area
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState())
                .graphicsLayer {
                    clip = false
                },
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
        // Default/None option (only show when NOT in delete mode)
        if (!isDeleteMode) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.small)
                    .border(
                        2.dp,
                        if (selectedColor == null) MaterialTheme.colorScheme.primary else Color.Transparent,
                        MaterialTheme.shapes.small
                    )
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onColorSelected(null) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "No color",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        // Preset colors
        presetColors.forEach { preset ->
            val isCustomColor = preset.name.startsWith("Custom")
            
            // Animation states for this specific color
            // For photo squares, match both color AND photoUri to avoid animating all gray squares
            val isBeingDeleted = (colorBeingDeleted?.first == preset.color && colorBeingDeleted?.second == preset.photoUri) || 
                (colorBeingDeletedFromDialog?.first == preset.color && colorBeingDeletedFromDialog?.second == preset.photoUri && colorBeingDeletedFromDialog?.third == sectionType)
            val isBeingAdded = colorBeingAdded == preset.color || 
                (colorBeingAddedFromDialog?.first == preset.color && colorBeingAddedFromDialog?.second == sectionType)
            
            // Deletion animation
            val deletionScale by animateFloatAsState(
                targetValue = if (isBeingDeleted) 0f else 1f,
                animationSpec = tween(400),
                label = "deletionScale"
            )
            val deletionAlpha by animateFloatAsState(
                targetValue = if (isBeingDeleted) 0f else 1f,
                animationSpec = tween(400),
                label = "deletionAlpha"
            )
            
            // Addition animation (bouncy scale-up effect from 0.3 to 1.0)
            val additionScale by animateFloatAsState(
                targetValue = if (isBeingAdded) 1f else 0.3f,
                animationSpec = if (isBeingAdded) spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ) else tween(0),
                label = "additionScale"
            )
            val additionAlpha by animateFloatAsState(
                targetValue = if (isBeingAdded) 1f else 0f,
                animationSpec = if (isBeingAdded) tween(300) else tween(0),
                label = "additionAlpha"
            )
            
            // Combined scale and alpha values
            val finalScale = if (isBeingDeleted) deletionScale else if (isBeingAdded) additionScale else 1f
            val finalAlpha = if (isBeingDeleted) deletionAlpha else if (isBeingAdded) additionAlpha else 1f
            
            // Check if THIS specific preset has a photo
            val hasPhoto = preset.photoUri != null && preset.photoUri.isNotEmpty()
            // Determine if this specific square is selected (same logic as checkmark)
            val isThisSquareSelected = if (hasPhoto) {
                // Photo square: only selected if THIS photo URI is active
                currentPhotoUri == preset.photoUri && currentPhotoUri != null && currentPhotoUri.isNotEmpty()
            } else {
                // Color square: only selected if color matches AND no photo is active
                selectedColor == preset.color && (currentPhotoUri == null || currentPhotoUri.isEmpty())
            }
            
            Box {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .graphicsLayer {
                            // Apply combined animations
                            scaleX = finalScale
                            scaleY = finalScale
                            alpha = finalAlpha
                            
                            // Apply shaking animation if in delete mode and not being deleted
                            if (isDeleteMode && isCustomColor && !isBeingDeleted) {
                                translationX = shakeOffset
                                translationY = 0f
                            }
                        }
                        .clip(MaterialTheme.shapes.small)
                        .border(
                            2.dp,
                            if (isThisSquareSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            MaterialTheme.shapes.small
                        )
                        .background(preset.color)
                        .combinedClickable(
                            onClick = {
                                if (!globalIsDragging) {
                                    if (globalDraggedColor != null) {
                                        // Drop the dragged color here (no photo info available from drag state)
                                        onColorDraggedToSection?.invoke(globalDraggedColor, null, 1f, 0f, globalDraggedFromSection ?: sectionType, sectionType)
                                        onGlobalDragStateChange?.invoke(false, null, null)
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    } else if (!isDeleteMode) {
                                        // Apply the color/photo immediately
                                        onColorSelected(preset.color)
                                        // If this preset has a photo, also apply the photo
                                        if (preset.photoUri != null && preset.photoUri.isNotEmpty()) {
                                            onBackgroundPhotoUriChange?.invoke(preset.photoUri)
                                            // Don't overwrite alpha/blur from preset — the stored preset
                                            // may have stale values. SharedPreferences (updated by slider
                                            // callbacks) is the source of truth for alpha/blur.
                                            onBackgroundPhotoOffsetXChange?.invoke(preset.photoOffsetX)
                                            onBackgroundPhotoOffsetYChange?.invoke(preset.photoOffsetY)
                                            onBackgroundPhotoScaleChange?.invoke(preset.photoScale)
                                            onBackgroundPhotoRotationChange?.invoke(preset.photoRotation)
                                        } else {
                                            // Plain color square - clear photo state
                                            onBackgroundPhotoUriChange?.invoke(null)
                                            onBackgroundPhotoAlphaChange?.invoke(1f)
                                            onBackgroundPhotoBlurChange?.invoke(0f)
                                            onBackgroundPhotoOffsetXChange?.invoke(0f)
                                            onBackgroundPhotoOffsetYChange?.invoke(0f)
                                            onBackgroundPhotoScaleChange?.invoke(1f)
                                            onBackgroundPhotoRotationChange?.invoke(0f)
                                        }
                                        // Open editor dialog for further customization
                                        colorToEdit = preset.color
                                        presetToEdit = preset // Track the full preset
                                        showEditColorPicker = true
                                    }
                                }
                            },
                            onLongClick = {
                                if (!globalIsDragging && isCustomColor) {
                                    // Show drag options dialog immediately on long press
                                    onShowDragOptions?.invoke(preset.color, preset.photoUri, preset.photoAlpha, preset.photoBlur, sectionType)
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            }
                        )
                ) {
                    // Show photo thumbnail if this preset has a photo
                    if (hasPhoto) {
                        val painter = rememberHighQualityPhotoPainter(photoUri = preset.photoUri)
                        val isLoadedPreset = painter.state is coil.compose.AsyncImagePainter.State.Success
                        val presetImgSize = painter.intrinsicSize
                        val presetCropRatio = if (isLoadedPreset && presetImgSize.width > 0 && presetImgSize.height > 0 && presetImgSize.width.isFinite() && presetImgSize.height.isFinite()) presetImgSize else null
                        androidx.compose.foundation.Image(
                            painter = painter,
                            contentDescription = "Photo background",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    val ratio = if (presetCropRatio != null && size.width > 0 && size.height > 0) {
                                        maxOf(size.width / presetCropRatio.width, size.height / presetCropRatio.height) /
                                            minOf(size.width / presetCropRatio.width, size.height / presetCropRatio.height)
                                    } else 1f
                                    val effectiveScale = preset.photoScale * ratio
                                    scaleX = effectiveScale
                                    scaleY = effectiveScale
                                    rotationZ = preset.photoRotation
                                    translationX = -preset.photoOffsetX * size.width
                                    translationY = preset.photoOffsetY * size.height
                                }
                                .alpha(preset.photoAlpha),
                            contentScale = if (isLoadedPreset) androidx.compose.ui.layout.ContentScale.Fit else androidx.compose.ui.layout.ContentScale.Crop
                        )
                        
                        // Show small photo icon indicator
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(2.dp)
                                .size(12.dp)
                                .background(
                                    Color.White.copy(alpha = 0.9f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = "Photo",
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.Center),
                                tint = Color(0xFF1976D2)
                            )
                        }
                    }
                    
                    // Show checkmark if selected
                    if (isThisSquareSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = if (hasPhoto) {
                                Color.White // Always white on photo
                            } else if (preset.color.luminance() > 0.5f) {
                                Color.Black
                            } else {
                                Color.White
                            },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                
                // X button positioned outside the color square to create true hovering effect
                if (isDeleteMode && isCustomColor) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .offset(x = 32.dp, y = (-8).dp)
                            .zIndex(10f)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                            .clickable {
                                // Animate deletion instead of immediate removal
                                animateColorDeletion(preset.color, preset.photoUri)
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                // Keep deletion mode active for multiple deletions
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "×",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        // Custom color picker
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.small)
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { showColorPicker = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "+",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        } // Close scrollable color area Row
    } // Close outer Row
    
    if (showColorPicker) {
        UnifiedBackgroundEditor(
            show = showColorPicker,
            currentColor = selectedColor ?: Color.White,
            currentBlur = currentBlur,
            onTestInApp = onTestInApp?.let { testCallback ->
                {
                    showColorPicker = false
                    testCallback(sectionType)
                }
            },
            onColorSelected = { colorWithBlur ->
                // Clear photo background FIRST before adding color
                onBackgroundPhotoUriChange?.invoke("")
                onBackgroundPhotoAlphaChange?.invoke(1f)
                onBackgroundPhotoBlurChange?.invoke(0f)
                
                onColorSelected(colorWithBlur.toColor())
                // Add the custom color to the preset list if callback is provided
                onAddCustomColor?.invoke(colorWithBlur.toColor())
                // Save the blur value if callback is provided
                onBlurChange?.invoke(colorWithBlur.blur)
                // Trigger addition animation
                animateColorAddition(colorWithBlur.toColor())
                showColorPicker = false
            },
            onPhotoSelected = { backgroundOption ->
                // Create a new color square with photo data
                backgroundOption.photoUri?.let { uri ->
                    // Use a neutral gray color as base for photo squares
                    val photoSquareColor = Color(0xFF808080)
                    
                    android.util.Log.d("PhotoPositioning", "NEW photo - Setting positioning: offsetX=${backgroundOption.photoOffsetX}, offsetY=${backgroundOption.photoOffsetY}, scale=${backgroundOption.photoScale}")
                    
                    // Set ALL photo state including positioning
                    onBackgroundPhotoUriChange?.invoke(uri)
                    onBackgroundPhotoAlphaChange?.invoke(backgroundOption.alpha)
                    onBackgroundPhotoBlurChange?.invoke(backgroundOption.blur)
                    onBackgroundPhotoOffsetXChange?.invoke(backgroundOption.photoOffsetX)
                    onBackgroundPhotoOffsetYChange?.invoke(backgroundOption.photoOffsetY)
                    onBackgroundPhotoScaleChange?.invoke(backgroundOption.photoScale)
                    
                    // CRITICAL: Pass positioning values directly to avoid async state timing issues
                    // Use the appropriate callback based on section type
                    when (sectionType) {
                        "content" -> onAddCustomColorWithPositioning?.invoke(
                            photoSquareColor, uri, backgroundOption.alpha, backgroundOption.blur,
                            backgroundOption.photoOffsetX, backgroundOption.photoOffsetY, backgroundOption.photoScale, backgroundOption.photoRotation
                        )
                        "memo" -> onAddMemoAreaColorWithPositioning?.invoke(
                            photoSquareColor, uri, backgroundOption.alpha, backgroundOption.blur,
                            backgroundOption.photoOffsetX, backgroundOption.photoOffsetY, backgroundOption.photoScale, backgroundOption.photoRotation
                        )
                        "bg" -> onAddBgColorWithPositioning?.invoke(
                            photoSquareColor, uri, backgroundOption.alpha, backgroundOption.blur,
                            backgroundOption.photoOffsetX, backgroundOption.photoOffsetY, backgroundOption.photoScale, backgroundOption.photoRotation
                        )
                        "fileListItem" -> onAddFileListItemColorWithPositioning?.invoke(
                            photoSquareColor, uri, backgroundOption.alpha, backgroundOption.blur,
                            backgroundOption.photoOffsetX, backgroundOption.photoOffsetY, backgroundOption.photoScale, backgroundOption.photoRotation
                        )
                        "fileListBg" -> onAddFileListBackgroundColorWithPositioning?.invoke(
                            photoSquareColor, uri, backgroundOption.alpha, backgroundOption.blur,
                            backgroundOption.photoOffsetX, backgroundOption.photoOffsetY, backgroundOption.photoScale, backgroundOption.photoRotation
                        )
                        else -> onAddCustomColor?.invoke(photoSquareColor)
                    }
                    animateColorAddition(photoSquareColor)
                }
                showColorPicker = false
            },
            onPhotoAlphaChange = { alpha ->
                // Save alpha immediately so it persists when re-entering the editor
                onBackgroundPhotoAlphaChange?.invoke(alpha)
            },
            onPhotoBlurChange = { blur ->
                // Save blur immediately so it persists when re-entering the editor
                onBackgroundPhotoBlurChange?.invoke(blur)
            },
            onPhotoOffsetXChange = { offsetX ->
                // Route to correct section-specific callback
                when (sectionType) {
                    "content" -> onContentAreaPhotoOffsetXChange?.invoke(offsetX)
                    "memo" -> onMemoAreaPhotoOffsetXChange?.invoke(offsetX)
                    "fileListItem" -> onFileListItemPhotoOffsetXChange?.invoke(offsetX)
                    "fileListBg" -> onFileListBgPhotoOffsetXChange?.invoke(offsetX)
                    else -> onBackgroundPhotoOffsetXChange?.invoke(offsetX)
                }
            },
            onPhotoOffsetYChange = { offsetY ->
                // Route to correct section-specific callback
                when (sectionType) {
                    "content" -> onContentAreaPhotoOffsetYChange?.invoke(offsetY)
                    "memo" -> onMemoAreaPhotoOffsetYChange?.invoke(offsetY)
                    "fileListItem" -> onFileListItemPhotoOffsetYChange?.invoke(offsetY)
                    "fileListBg" -> onFileListBgPhotoOffsetYChange?.invoke(offsetY)
                    else -> onBackgroundPhotoOffsetYChange?.invoke(offsetY)
                }
            },
            onPhotoScaleChange = { scale ->
                // Route to correct section-specific callback
                when (sectionType) {
                    "content" -> onContentAreaPhotoScaleChange?.invoke(scale)
                    "memo" -> onMemoAreaPhotoScaleChange?.invoke(scale)
                    "fileListItem" -> onFileListItemPhotoScaleChange?.invoke(scale)
                    "fileListBg" -> onFileListBgPhotoScaleChange?.invoke(scale)
                    else -> onBackgroundPhotoScaleChange?.invoke(scale)
                }
            },
            onPhotoRotationChange = { rotation ->
                when (sectionType) {
                    "content" -> onContentAreaPhotoRotationChange?.invoke(rotation)
                    "memo" -> onMemoAreaPhotoRotationChange?.invoke(rotation)
                    "fileListItem" -> onFileListItemPhotoRotationChange?.invoke(rotation)
                    "fileListBg" -> onFileListBgPhotoRotationChange?.invoke(rotation)
                    else -> onBackgroundPhotoRotationChange?.invoke(rotation)
                }
            },
            onDismiss = { showColorPicker = false },
            // Pass current app colors for preview
            customBgColor = appCustomBgColor,
            contentAreaColor = appContentAreaColor,
            memoAreaColor = appMemoAreaColor,
            fileListItemColor = appFileListItemColor,
            fileListBackgroundColor = appFileListBackgroundColor,
            targetSection = sectionType,
            // For NEW color/photo: Start with empty photo state
            currentPhotoUri = null,
            currentPhotoAlpha = 1f,
            currentPhotoBlur = 0f,
            currentPhotoOffsetX = 0f,
            currentPhotoOffsetY = 0f,
            currentPhotoScale = 1f,
            currentPhotoRotation = 0f,
            currentPhotoBackgroundMode = currentPhotoBackgroundMode,
            onPhotoBackgroundModeChange = onPhotoBackgroundModeChange
        )
    }
    
    // Edit ColorPickerDialog for existing custom colors
    if (showEditColorPicker && colorToEdit != null) {
        UnifiedBackgroundEditor(
            show = showEditColorPicker,
            currentColor = colorToEdit!!,
            currentBlur = presetToEdit?.photoBlur ?: currentBlur,
            onTestInApp = onTestInApp?.let { testCallback ->
                {
                    showEditColorPicker = false
                    testCallback(sectionType)
                }
            },
            onColorSelected = { colorWithBlur ->
                // Clear photo background FIRST when color is selected
                onBackgroundPhotoUriChange?.invoke("")
                onBackgroundPhotoAlphaChange?.invoke(1f)
                onBackgroundPhotoBlurChange?.invoke(0f)
                
                // Edit the existing custom color
                onEditCustomColor?.invoke(
                    colorToEdit!!, 
                    colorWithBlur.toColor(), 
                    colorWithBlur.blur, 
                    presetToEdit?.photoUri,
                    0f, // offsetX - reset when selecting color
                    0f, // offsetY - reset when selecting color
                    1f, // scale - reset when selecting color
                    0f  // rotation - reset when selecting color
                )
                // Save the blur value if callback is provided
                onBlurChange?.invoke(colorWithBlur.blur)
                showEditColorPicker = false
                colorToEdit = null
                presetToEdit = null
                onExitDeleteMode?.invoke() // Exit delete mode after editing
            },
            onPhotoUriChange = { uri ->
                // Update presetToEdit immediately when photo URI changes in picker
                if (presetToEdit != null) {
                    presetToEdit = presetToEdit?.copy(photoUri = uri)
                }
            },
            onPhotoSelected = { backgroundOption ->
                val normalizedPhotoUri = backgroundOption.photoUri?.takeIf { it.isNotBlank() }

                if (normalizedPhotoUri != null) {
                    onBackgroundPhotoUriChange?.invoke(normalizedPhotoUri)
                    onBackgroundPhotoAlphaChange?.invoke(backgroundOption.alpha)
                    onBackgroundPhotoBlurChange?.invoke(backgroundOption.blur)
                    onBackgroundPhotoOffsetXChange?.invoke(backgroundOption.photoOffsetX)
                    onBackgroundPhotoOffsetYChange?.invoke(backgroundOption.photoOffsetY)
                    onBackgroundPhotoScaleChange?.invoke(backgroundOption.photoScale)

                    // Update the preset with ALL photo data including positioning AND the new photo URI
                    if (colorToEdit != null && presetToEdit != null) {
                        presetToEdit = presetToEdit?.copy(
                            photoUri = normalizedPhotoUri,
                            photoBlur = backgroundOption.blur,
                            photoOffsetX = backgroundOption.photoOffsetX,
                            photoOffsetY = backgroundOption.photoOffsetY,
                            photoScale = backgroundOption.photoScale,
                            photoRotation = backgroundOption.photoRotation
                        )
                        onEditCustomColor?.invoke(
                            colorToEdit!!,
                            colorToEdit!!,
                            backgroundOption.blur,
                            normalizedPhotoUri,
                            backgroundOption.photoOffsetX,
                            backgroundOption.photoOffsetY,
                            backgroundOption.photoScale,
                            backgroundOption.photoRotation
                        )
                    }
                } else {
                    // Photo was removed in the editor: persist full clear state.
                    onBackgroundPhotoUriChange?.invoke(null)
                    onBackgroundPhotoAlphaChange?.invoke(1f)
                    onBackgroundPhotoBlurChange?.invoke(0f)
                    onBackgroundPhotoOffsetXChange?.invoke(0f)
                    onBackgroundPhotoOffsetYChange?.invoke(0f)
                    onBackgroundPhotoScaleChange?.invoke(1f)
                    onBackgroundPhotoRotationChange?.invoke(0f)

                    if (colorToEdit != null && presetToEdit != null) {
                        presetToEdit = presetToEdit?.copy(
                            photoUri = null,
                            photoAlpha = 1f,
                            photoBlur = 0f,
                            photoOffsetX = 0f,
                            photoOffsetY = 0f,
                            photoScale = 1f,
                            photoRotation = 0f
                        )
                        onEditCustomColor?.invoke(
                            colorToEdit!!,
                            colorToEdit!!,
                            0f,
                            null,
                            0f,
                            0f,
                            1f,
                            0f
                        )
                    }
                }
                showEditColorPicker = false
                colorToEdit = null
                presetToEdit = null
            },
            onPhotoAlphaChange = { alpha ->
                // Save alpha immediately when slider changes
                onBackgroundPhotoAlphaChange?.invoke(alpha)
                presetToEdit = presetToEdit?.copy(photoAlpha = alpha)
            },
            onPhotoBlurChange = { blur ->
                // Keep blur local while dragging; commit once on Select/Test via onPhotoSelected
                presetToEdit = presetToEdit?.copy(photoBlur = blur)
            },
            onPhotoOffsetXChange = { offsetX ->
                // Keep transform local while dragging; commit once on Select/Test via onPhotoSelected
                if (presetToEdit != null) {
                    presetToEdit = presetToEdit?.copy(photoOffsetX = offsetX)
                }
            },
            onPhotoOffsetYChange = { offsetY ->
                // Keep transform local while dragging; commit once on Select/Test via onPhotoSelected
                if (presetToEdit != null) {
                    presetToEdit = presetToEdit?.copy(photoOffsetY = offsetY)
                }
            },
            onPhotoScaleChange = { scale ->
                // Keep transform local while dragging; commit once on Select/Test via onPhotoSelected
                if (presetToEdit != null) {
                    presetToEdit = presetToEdit?.copy(photoScale = scale)
                }
            },
            onPhotoRotationChange = { rotation ->
                // Keep transform local while dragging; commit once on Select/Test via onPhotoSelected
                if (presetToEdit != null) {
                    presetToEdit = presetToEdit?.copy(photoRotation = rotation)
                }
            },
            onDismiss = { 
                showEditColorPicker = false
                colorToEdit = null
                presetToEdit = null
            },
            // Pass current app colors for preview
            customBgColor = appCustomBgColor,
            contentAreaColor = appContentAreaColor,
            memoAreaColor = appMemoAreaColor,
            fileListItemColor = appFileListItemColor,
            fileListBackgroundColor = appFileListBackgroundColor,
            targetSection = sectionType,
            // Pass current photo state - use preset values if editing an existing preset
            currentPhotoUri = presetToEdit?.photoUri ?: currentPhotoUri,
            currentPhotoAlpha = presetToEdit?.photoAlpha ?: currentPhotoAlpha,
            currentPhotoBlur = presetToEdit?.photoBlur ?: currentPhotoBlur,
            currentPhotoOffsetX = presetToEdit?.photoOffsetX ?: currentPhotoOffsetX,
            currentPhotoOffsetY = presetToEdit?.photoOffsetY ?: currentPhotoOffsetY,
            currentPhotoScale = presetToEdit?.photoScale ?: currentPhotoScale,
            currentPhotoRotation = presetToEdit?.photoRotation ?: 0f,
            currentPhotoBackgroundMode = presetToEdit?.photoBackgroundMode ?: currentPhotoBackgroundMode,
            onPhotoBackgroundModeChange = if (onPhotoBackgroundModeChange != null) {
                val callback = onPhotoBackgroundModeChange
                { mode ->
                    if (presetToEdit != null) {
                        presetToEdit = presetToEdit?.copy(photoBackgroundMode = mode)
                    }
                    callback.invoke(mode)
                }
            } else {
                null
            },
            // Open Photo mode if the clicked square has a photo, otherwise Color mode
            initialMode = if (presetToEdit?.photoUri != null && presetToEdit?.photoUri?.isNotEmpty() == true) 1 else 0
        )
    }
}

// Data classes for area sizes
data class ContentAreaSize(
    val widthPercent: Float,
    val heightPercent: Float
) {
    companion object {
        fun listFromJson(json: String): List<Pair<ContentAreaSize, String>> {
            return try {
                val jsonArray = org.json.JSONArray(json)
                (0 until jsonArray.length()).map { i ->
                    val obj = jsonArray.getJSONObject(i)
                    val size = ContentAreaSize(
                        obj.getDouble("widthPercent").toFloat(),
                        obj.getDouble("heightPercent").toFloat()
                    )
                    val name = obj.getString("name")
                    size to name
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
        
        fun listToJson(presets: List<Pair<ContentAreaSize, String>>): String {
            val jsonArray = org.json.JSONArray()
            presets.forEach { (size, name) ->
                val obj = org.json.JSONObject()
                obj.put("widthPercent", size.widthPercent.toDouble())
                obj.put("heightPercent", size.heightPercent.toDouble())
                obj.put("name", name)
                jsonArray.put(obj)
            }
            return jsonArray.toString()
        }
    }
}

data class MemoAreaSize(
    val widthPercent: Float,
    val heightPercent: Float
) {
    companion object {
        fun listFromJson(json: String): List<Pair<MemoAreaSize, String>> {
            return try {
                val jsonArray = org.json.JSONArray(json)
                (0 until jsonArray.length()).map { i ->
                    val obj = jsonArray.getJSONObject(i)
                    val size = MemoAreaSize(
                        obj.getDouble("widthPercent").toFloat(),
                        obj.getDouble("heightPercent").toFloat()
                    )
                    val name = obj.getString("name")
                    size to name
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
        
        fun listToJson(presets: List<Pair<MemoAreaSize, String>>): String {
            val jsonArray = org.json.JSONArray()
            presets.forEach { (size, name) ->
                val obj = org.json.JSONObject()
                obj.put("widthPercent", size.widthPercent.toDouble())
                obj.put("heightPercent", size.heightPercent.toDouble())
                obj.put("name", name)
                jsonArray.put(obj)
            }
            return jsonArray.toString()
        }
    }
}

// Default preset data - simplified to one preset each
val defaultDialogAreaPresets = listOf(
    ContentAreaSize(0.97f, 1.0f) to "97% × 100% (Default)"
)

val defaultMemoAreaPresets = listOf(
    MemoAreaSize(1.0f, 80f) to "Small (80dp)"
)


val backgroundColorPresets = listOf(
    SimpleColorPreset("Dark Gray", Color(0xFF2D2D2D)),
    SimpleColorPreset("Black", Color.Black),
    SimpleColorPreset("Dark Blue", Color(0xFF1A237E)),
    SimpleColorPreset("Dark Green", Color(0xFF1B5E20)),
    SimpleColorPreset("Dark Purple", Color(0xFF4A148C))
)

val contentAreaColorPresets = listOf(
    SimpleColorPreset("White", Color.White),
    SimpleColorPreset("Light Gray", Color(0xFFF5F5F5)),
    SimpleColorPreset("Dark Gray", Color(0xFF424242)),
    SimpleColorPreset("Light Blue", Color(0xFFE3F2FD)),
    SimpleColorPreset("Blue", Color(0xFF2196F3)),
    SimpleColorPreset("Light Green", Color(0xFFE8F5E8)),
    SimpleColorPreset("Green", Color(0xFF4CAF50)),
    SimpleColorPreset("Light Yellow", Color(0xFFFFFDE7)),
    SimpleColorPreset("Yellow", Color(0xFFFFEB3B)),
    SimpleColorPreset("Light Purple", Color(0xFFF3E5F5)),
    SimpleColorPreset("Purple", Color(0xFF9C27B0))
)

@Composable
fun ColorPresetsSection(
    presets: List<com.j4.texter2025.data.ColorPreset>,
    selectedPresetForEditing: com.j4.texter2025.data.ColorPreset? = null,
    onSelectPresetForEditing: (com.j4.texter2025.data.ColorPreset?) -> Unit = {},
    onSavePreset: (String) -> Unit,
    onApplyPreset: (com.j4.texter2025.data.ColorPreset) -> Unit,
    onDeletePreset: (com.j4.texter2025.data.ColorPreset) -> Unit,
    onRenamePreset: (com.j4.texter2025.data.ColorPreset, String) -> Unit,
    onUpdatePreset: (com.j4.texter2025.data.ColorPreset) -> Unit = {},
    onResetColorsForNewPreset: () -> Unit = {},
    currentPhotoUri: String? = null,
    currentPhotoOffsetX: Float = 0f,
    currentPhotoOffsetY: Float = 0f,
    currentPhotoScale: Float = 1f
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var presetToRename by remember { mutableStateOf<com.j4.texter2025.data.ColorPreset?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var presetToDelete by remember { mutableStateOf<com.j4.texter2025.data.ColorPreset?>(null) }
    var isInNewPresetMode by remember { mutableStateOf(false) }
    
    // Track the last applied preset name for selection indicator
    var lastAppliedPresetName by remember { mutableStateOf<String?>(null) }
    
    // Load current live photo state from SharedPreferences for preset card previews
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("TeXterPrefs", android.content.Context.MODE_PRIVATE) }
    
    var liveCustomBgPhotoUri by remember { mutableStateOf<String?>(null) }
    var liveContentPhotoUri by remember { mutableStateOf<String?>(null) }
    var liveMemoPhotoUri by remember { mutableStateOf<String?>(null) }
    var liveFileListItemPhotoUri by remember { mutableStateOf<String?>(null) }
    var liveFileListBgPhotoUri by remember { mutableStateOf<String?>(null) }
    var liveCustomBgPhotoAlpha by remember { mutableStateOf(1f) }
    var liveContentPhotoAlpha by remember { mutableStateOf(1f) }
    var liveMemoPhotoAlpha by remember { mutableStateOf(1f) }
    var liveFileListItemPhotoAlpha by remember { mutableStateOf(1f) }
    var liveFileListBgPhotoAlpha by remember { mutableStateOf(1f) }
    var liveCustomBgPhotoBlur by remember { mutableStateOf(0f) }
    var liveContentPhotoBlur by remember { mutableStateOf(0f) }
    var liveMemoPhotoBlur by remember { mutableStateOf(0f) }
    var liveFileListItemPhotoBlur by remember { mutableStateOf(0f) }
    var liveFileListBgPhotoBlur by remember { mutableStateOf(0f) }
    var liveCustomBgOffsetX by remember { mutableStateOf(0f) }
    var liveCustomBgOffsetY by remember { mutableStateOf(0f) }
    var liveCustomBgScale by remember { mutableStateOf(1f) }
    var liveCustomBgRotation by remember { mutableStateOf(0f) }
    var liveContentOffsetX by remember { mutableStateOf(0f) }
    var liveContentOffsetY by remember { mutableStateOf(0f) }
    var liveContentScale by remember { mutableStateOf(1f) }
    var liveContentRotation by remember { mutableStateOf(0f) }
    var liveMemoOffsetX by remember { mutableStateOf(0f) }
    var liveMemoOffsetY by remember { mutableStateOf(0f) }
    var liveMemoScale by remember { mutableStateOf(1f) }
    var liveMemoRotation by remember { mutableStateOf(0f) }
    var liveFileListItemOffsetX by remember { mutableStateOf(0f) }
    var liveFileListItemOffsetY by remember { mutableStateOf(0f) }
    var liveFileListItemScale by remember { mutableStateOf(1f) }
    var liveFileListItemRotation by remember { mutableStateOf(0f) }
    var liveFileListBgOffsetX by remember { mutableStateOf(0f) }
    var liveFileListBgOffsetY by remember { mutableStateOf(0f) }
    var liveFileListBgScale by remember { mutableStateOf(1f) }
    var liveFileListBgRotation by remember { mutableStateOf(0f) }
    
    // Load live photo state once when presets change or section opens
    androidx.compose.runtime.LaunchedEffect(presets, selectedPresetForEditing) {
        liveCustomBgPhotoUri = prefs.getString("custom_bg_photo_uri", null)
        liveContentPhotoUri = prefs.getString("content_area_photo_uri", null)
        liveMemoPhotoUri = prefs.getString("memo_area_photo_uri", null)
        liveFileListItemPhotoUri = prefs.getString("file_list_item_photo_uri", null)
        liveFileListBgPhotoUri = prefs.getString("file_list_background_photo_uri", null)
        liveCustomBgPhotoAlpha = prefs.getFloat("custom_bg_photo_alpha", 1f)
        liveContentPhotoAlpha = prefs.getFloat("content_area_photo_alpha", 1f)
        liveMemoPhotoAlpha = prefs.getFloat("memo_area_photo_alpha", 1f)
        liveFileListItemPhotoAlpha = prefs.getFloat("file_list_item_photo_alpha", 1f)
        liveFileListBgPhotoAlpha = prefs.getFloat("file_list_background_photo_alpha", 1f)
        liveCustomBgPhotoBlur = prefs.getFloat("custom_bg_photo_blur", 0f)
        liveContentPhotoBlur = prefs.getFloat("content_area_photo_blur", 0f)
        liveMemoPhotoBlur = prefs.getFloat("memo_area_photo_blur", 0f)
        liveFileListItemPhotoBlur = prefs.getFloat("file_list_item_photo_blur", 0f)
        liveFileListBgPhotoBlur = prefs.getFloat("file_list_background_photo_blur", 0f)
        liveCustomBgOffsetX = prefs.getFloat("custom_bg_photo_offset_x", 0f)
        liveCustomBgOffsetY = prefs.getFloat("custom_bg_photo_offset_y", 0f)
        liveCustomBgScale = prefs.getFloat("custom_bg_photo_scale", 1f)
        liveCustomBgRotation = prefs.getFloat("custom_bg_photo_rotation", 0f)
        liveContentOffsetX = prefs.getFloat("content_area_photo_offset_x", 0f)
        liveContentOffsetY = prefs.getFloat("content_area_photo_offset_y", 0f)
        liveContentScale = prefs.getFloat("content_area_photo_scale", 1f)
        liveContentRotation = prefs.getFloat("content_area_photo_rotation", 0f)
        liveMemoOffsetX = prefs.getFloat("memo_area_photo_offset_x", 0f)
        liveMemoOffsetY = prefs.getFloat("memo_area_photo_offset_y", 0f)
        liveMemoScale = prefs.getFloat("memo_area_photo_scale", 1f)
        liveMemoRotation = prefs.getFloat("memo_area_photo_rotation", 0f)
        liveFileListItemOffsetX = prefs.getFloat("file_list_item_photo_offset_x", 0f)
        liveFileListItemOffsetY = prefs.getFloat("file_list_item_photo_offset_y", 0f)
        liveFileListItemScale = prefs.getFloat("file_list_item_photo_scale", 1f)
        liveFileListItemRotation = prefs.getFloat("file_list_item_photo_rotation", 0f)
        liveFileListBgOffsetX = prefs.getFloat("file_list_background_photo_offset_x", 0f)
        liveFileListBgOffsetY = prefs.getFloat("file_list_background_photo_offset_y", 0f)
        liveFileListBgScale = prefs.getFloat("file_list_background_photo_scale", 1f)
        liveFileListBgRotation = prefs.getFloat("file_list_background_photo_rotation", 0f)
    }
    
    // LazyRow state for auto-scrolling
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    
    // Auto-scroll to selected preset when screen opens
    androidx.compose.runtime.LaunchedEffect(selectedPresetForEditing) {
        selectedPresetForEditing?.let { preset ->
            // Find the index of the selected preset (+1 because AddPresetCard is first)
            val index = presets.indexOfFirst { it.name == preset.name }
            if (index != -1) {
                // Scroll to the selected preset (index + 1 to account for AddPresetCard)
                listState.animateScrollToItem(index + 1)
            }
        }
    }
    
    Column {
        Text(
            "Color Presets",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
            reverseLayout = false
        ) {
            // Add new preset button (always first)
            item {
                AddPresetCard(
                    onClick = {
                        // Always show save dialog to create new preset
                        android.util.Log.d("PresetDebug", "AddPresetCard clicked - showing save dialog")
                        onResetColorsForNewPreset()
                        isInNewPresetMode = true
                        // Clear selection when creating new preset
                        lastAppliedPresetName = null
                        onSelectPresetForEditing(null)
                        showSaveDialog = true
                        android.util.Log.d("PresetDebug", "Save dialog shown for new preset")
                    },
                    isInSaveMode = false // Always show "+" icon
                )
            }
            
            // Existing presets
            items(presets) { preset ->
                // Use selectedPresetForEditing if this preset is selected (has latest data)
                // Otherwise use the preset from the list
                val isSelected = selectedPresetForEditing?.name == preset.name
                val displayPreset = if (isSelected && selectedPresetForEditing != null) {
                    selectedPresetForEditing!!
                } else {
                    preset
                }
                
                PresetCard(
                    preset = displayPreset,
                    isSelected = isSelected,
                    onTap = { 
                        android.util.Log.d("PresetDebug", "PresetCard tapped: ${preset.name}")
                        // Persist the currently edited preset before switching cards.
                        // This prevents edits from being dropped when user taps another preset.
                        selectedPresetForEditing
                            ?.takeIf { it.name != preset.name }
                            ?.let { onUpdatePreset(it) }
                        // Tap to select for editing and apply colors
                        onSelectPresetForEditing(preset)
                        android.util.Log.d("PresetDebug", "Calling onApplyPreset for: ${preset.name}")
                        onApplyPreset(preset)
                        android.util.Log.d("PresetDebug", "onApplyPreset completed")
                        // Track which preset was applied for selection indicator
                        lastAppliedPresetName = preset.name
                        // Exit new preset mode when selecting existing preset
                        isInNewPresetMode = false
                    },
                    onRename = { 
                        presetToRename = preset
                        showRenameDialog = true
                    },
                    onDelete = {
                        presetToDelete = preset
                        showDeleteConfirmation = true
                    },
                    liveCustomBgPhotoUri = liveCustomBgPhotoUri,
                    liveCustomBgPhotoAlpha = liveCustomBgPhotoAlpha,
                    liveCustomBgPhotoBlur = liveCustomBgPhotoBlur,
                    liveContentPhotoUri = liveContentPhotoUri,
                    liveContentPhotoAlpha = liveContentPhotoAlpha,
                    liveContentPhotoBlur = liveContentPhotoBlur,
                    liveMemoPhotoUri = liveMemoPhotoUri,
                    liveMemoPhotoAlpha = liveMemoPhotoAlpha,
                    liveMemoPhotoBlur = liveMemoPhotoBlur,
                    liveFileListItemPhotoUri = liveFileListItemPhotoUri,
                    liveFileListItemPhotoAlpha = liveFileListItemPhotoAlpha,
                    liveFileListItemPhotoBlur = liveFileListItemPhotoBlur,
                    liveFileListBgPhotoUri = liveFileListBgPhotoUri,
                    liveFileListBgPhotoAlpha = liveFileListBgPhotoAlpha,
                    liveFileListBgPhotoBlur = liveFileListBgPhotoBlur,
                    liveCustomBgOffsetX = liveCustomBgOffsetX,
                    liveCustomBgOffsetY = liveCustomBgOffsetY,
                    liveCustomBgScale = liveCustomBgScale,
                    liveCustomBgRotation = liveCustomBgRotation,
                    liveContentOffsetX = liveContentOffsetX,
                    liveContentOffsetY = liveContentOffsetY,
                    liveContentScale = liveContentScale,
                    liveContentRotation = liveContentRotation,
                    liveMemoOffsetX = liveMemoOffsetX,
                    liveMemoOffsetY = liveMemoOffsetY,
                    liveMemoScale = liveMemoScale,
                    liveMemoRotation = liveMemoRotation,
                    liveFileListItemOffsetX = liveFileListItemOffsetX,
                    liveFileListItemOffsetY = liveFileListItemOffsetY,
                    liveFileListItemScale = liveFileListItemScale,
                    liveFileListItemRotation = liveFileListItemRotation,
                    liveFileListBgOffsetX = liveFileListBgOffsetX,
                    liveFileListBgOffsetY = liveFileListBgOffsetY,
                    liveFileListBgScale = liveFileListBgScale,
                    liveFileListBgRotation = liveFileListBgRotation
                )
            }
        }
    }
    
    // Save Preset Dialog
    if (showSaveDialog) {
        var presetName by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Color Preset") },
            text = {
                OutlinedTextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    label = { Text("Preset Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (presetName.isNotBlank()) {
                            if (isInNewPresetMode) {
                                // Re-apply reset right before save to avoid stale state
                                // being captured from a previously selected preset.
                                onResetColorsForNewPreset()
                            }
                            onSavePreset(presetName.trim())
                            showSaveDialog = false
                            isInNewPresetMode = false
                        }
                    },
                    enabled = presetName.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Rename Preset Dialog
    if (showRenameDialog && presetToRename != null) {
        var newPresetName by remember { mutableStateOf(presetToRename!!.name) }
        
        AlertDialog(
            onDismissRequest = { 
                showRenameDialog = false
                presetToRename = null
            },
            title = { Text("Rename Preset") },
            text = {
                OutlinedTextField(
                    value = newPresetName,
                    onValueChange = { newPresetName = it },
                    label = { Text("Preset Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPresetName.isNotBlank()) {
                            presetToRename?.let { onRenamePreset(it, newPresetName.trim()) }
                            showRenameDialog = false
                            presetToRename = null
                        }
                    },
                    enabled = newPresetName.isNotBlank()
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showRenameDialog = false
                    presetToRename = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Delete Confirmation Dialog
    if (showDeleteConfirmation && presetToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteConfirmation = false
                presetToDelete = null
            },
            title = { Text("Delete Preset") },
            text = { Text("Are you sure you want to delete \"${presetToDelete!!.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        presetToDelete?.let { onDeletePreset(it) }
                        showDeleteConfirmation = false
                        presetToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteConfirmation = false
                    presetToDelete = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PresetCard(
    preset: com.j4.texter2025.data.ColorPreset,
    isSelected: Boolean = false,
    onTap: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    // Live photo state from SharedPreferences for accurate preview
    liveCustomBgPhotoUri: String? = null,
    liveCustomBgPhotoAlpha: Float = 1f,
    liveCustomBgPhotoBlur: Float = 0f,
    liveContentPhotoUri: String? = null,
    liveContentPhotoAlpha: Float = 1f,
    liveContentPhotoBlur: Float = 0f,
    liveMemoPhotoUri: String? = null,
    liveMemoPhotoAlpha: Float = 1f,
    liveMemoPhotoBlur: Float = 0f,
    liveFileListItemPhotoUri: String? = null,
    liveFileListItemPhotoAlpha: Float = 1f,
    liveFileListItemPhotoBlur: Float = 0f,
    liveFileListBgPhotoUri: String? = null,
    liveFileListBgPhotoAlpha: Float = 1f,
    liveFileListBgPhotoBlur: Float = 0f,
    liveCustomBgOffsetX: Float = 0f,
    liveCustomBgOffsetY: Float = 0f,
    liveCustomBgScale: Float = 1f,
    liveCustomBgRotation: Float = 0f,
    liveContentOffsetX: Float = 0f,
    liveContentOffsetY: Float = 0f,
    liveContentScale: Float = 1f,
    liveContentRotation: Float = 0f,
    liveMemoOffsetX: Float = 0f,
    liveMemoOffsetY: Float = 0f,
    liveMemoScale: Float = 1f,
    liveMemoRotation: Float = 0f,
    liveFileListItemOffsetX: Float = 0f,
    liveFileListItemOffsetY: Float = 0f,
    liveFileListItemScale: Float = 1f,
    liveFileListItemRotation: Float = 0f,
    liveFileListBgOffsetX: Float = 0f,
    liveFileListBgOffsetY: Float = 0f,
    liveFileListBgScale: Float = 1f,
    liveFileListBgRotation: Float = 0f
) {
    var showMenu by remember { mutableStateOf(false) }
    
    // Selection fade animation (removed infinite animations to prevent continuous recomposition)
    val selectionAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(300),
        label = "selection"
    )
    
    // Get all colors from the preset for dynamic cycling
    val allPresetColors = listOfNotNull(
        preset.customBgColor,
        preset.contentAreaColor,
        preset.memoAreaColor,
        preset.fileListItemColor,
        preset.fileListBackgroundColor
    ).takeIf { it.isNotEmpty() } ?: listOf(MaterialTheme.colorScheme.primary)
    
    // Create dynamic color palette with enhanced brightness
    val dynamicColors = allPresetColors.map { color ->
        // Increase brightness by 3x
        Color(
            red = (color.red * 3f).coerceAtMost(1f),
            green = (color.green * 3f).coerceAtMost(1f),
            blue = (color.blue * 3f).coerceAtMost(1f),
            alpha = color.alpha
        )
    }
    
    Card(
        modifier = Modifier
            .size(140.dp)
            .then(
                if (isSelected) {
                    Modifier
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor = dynamicColors.firstOrNull()?.copy(alpha = 0.6f) ?: MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                        .background(
                            brush = Brush.sweepGradient(
                                colors = buildList {
                                    for (i in dynamicColors.indices) {
                                        val currentColor = dynamicColors[i]
                                        val nextColor = dynamicColors[(i + 1) % dynamicColors.size]
                                        
                                        // Current color
                                        add(currentColor.copy(alpha = 0.3f * selectionAlpha))
                                        
                                        // Blend to next
                                        add(Color(
                                            red = currentColor.red * 0.5f + nextColor.red * 0.5f,
                                            green = currentColor.green * 0.5f + nextColor.green * 0.5f,
                                            blue = currentColor.blue * 0.5f + nextColor.blue * 0.5f,
                                            alpha = 0.2f * selectionAlpha
                                        ))
                                    }
                                    
                                    // Complete the circle
                                    if (dynamicColors.isNotEmpty()) {
                                        add(dynamicColors.first().copy(alpha = 0.3f * selectionAlpha))
                                    }
                                    
                                    add(Color.Transparent)
                                }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    dynamicColors.getOrNull(1)?.copy(alpha = 0.4f * selectionAlpha) ?: Color.Transparent,
                                    dynamicColors.firstOrNull()?.copy(alpha = 0.15f * selectionAlpha) ?: Color.Transparent,
                                    Color.Transparent
                                ),
                                radius = 250f,
                                center = Offset(0.5f, 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                } else {
                    Modifier
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) {
            BorderStroke(
                width = 2.dp,
                brush = Brush.sweepGradient(
                    colors = buildList {
                        for (i in dynamicColors.indices) {
                            val currentColor = dynamicColors[i]
                            val nextColor = dynamicColors[(i + 1) % dynamicColors.size]
                            
                            add(currentColor.copy(alpha = 0.9f * selectionAlpha))
                            
                            // Smooth transitions
                            add(Color(
                                red = currentColor.red * 0.75f + nextColor.red * 0.25f,
                                green = currentColor.green * 0.75f + nextColor.green * 0.25f,
                                blue = currentColor.blue * 0.75f + nextColor.blue * 0.25f,
                                alpha = 0.85f * selectionAlpha
                            ))
                            
                            add(Color(
                                red = currentColor.red * 0.5f + nextColor.red * 0.5f,
                                green = currentColor.green * 0.5f + nextColor.green * 0.5f,
                                blue = currentColor.blue * 0.5f + nextColor.blue * 0.5f,
                                alpha = 0.8f * selectionAlpha
                            ))
                            
                            add(Color(
                                red = currentColor.red * 0.25f + nextColor.red * 0.75f,
                                green = currentColor.green * 0.25f + nextColor.green * 0.75f,
                                blue = currentColor.blue * 0.25f + nextColor.blue * 0.75f,
                                alpha = 0.85f * selectionAlpha
                            ))
                        }
                        
                        if (dynamicColors.isNotEmpty()) {
                            add(dynamicColors.first().copy(alpha = 0.9f * selectionAlpha))
                        }
                    }
                )
            )
        } else null
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onTap() }
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Mini app preview (square format) - using AppLayoutPreview from ColorPickerDialog
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                ) {
                    // Only use live state for the currently selected preset; others use their own saved data
                    val useLive = false
                    AppLayoutPreview(
                        photoUri = if (useLive) (liveCustomBgPhotoUri ?: preset.customBgPhotoUri ?: "") else (preset.customBgPhotoUri ?: ""),
                        alpha = 1f,
                        blur = 0f,
                        customBgColor = preset.customBgColor,
                        contentAreaColor = preset.contentAreaColor,
                        memoAreaColor = preset.memoAreaColor,
                        fileListItemColor = preset.fileListItemColor,
                        fileListBackgroundColor = preset.fileListBackgroundColor,
                        targetSection = "none",
                        selectedColor = Color.White,
                        customBgPhotoUri = if (useLive) (liveCustomBgPhotoUri ?: preset.customBgPhotoUri) else preset.customBgPhotoUri,
                        customBgPhotoAlpha = if (useLive) liveCustomBgPhotoAlpha else 1f,
                        customBgPhotoBlur = if (useLive) liveCustomBgPhotoBlur else 0f,
                        contentAreaPhotoUri = if (useLive) (liveContentPhotoUri ?: preset.contentAreaPhotoUri) else preset.contentAreaPhotoUri,
                        contentAreaPhotoAlpha = if (useLive) liveContentPhotoAlpha else 1f,
                        contentAreaPhotoBlur = if (useLive) liveContentPhotoBlur else 0f,
                        memoAreaPhotoUri = if (useLive) (liveMemoPhotoUri ?: preset.memoAreaPhotoUri) else preset.memoAreaPhotoUri,
                        memoAreaPhotoAlpha = if (useLive) liveMemoPhotoAlpha else 1f,
                        memoAreaPhotoBlur = if (useLive) liveMemoPhotoBlur else 0f,
                        fileListBgPhotoUri = if (useLive) (liveFileListBgPhotoUri ?: preset.fileListBackgroundPhotoUri) else preset.fileListBackgroundPhotoUri,
                        fileListBgPhotoAlpha = if (useLive) liveFileListBgPhotoAlpha else 1f,
                        fileListBgPhotoBlur = if (useLive) liveFileListBgPhotoBlur else 0f,
                        fileListItemPhotoUri = if (useLive) (liveFileListItemPhotoUri ?: preset.fileListItemPhotoUri) else preset.fileListItemPhotoUri,
                        fileListItemPhotoAlpha = if (useLive) liveFileListItemPhotoAlpha else 1f,
                        fileListItemPhotoBlur = if (useLive) liveFileListItemPhotoBlur else 0f,
                        photoOffsetX = 0f,
                        photoOffsetY = 0f,
                        photoScale = 1f,
                        customBgPhotoOffsetX = if (useLive) liveCustomBgOffsetX else preset.customBgPhotoOffsetX,
                        customBgPhotoOffsetY = if (useLive) liveCustomBgOffsetY else preset.customBgPhotoOffsetY,
                        customBgPhotoScale = if (useLive) liveCustomBgScale else preset.customBgPhotoScale,
                        customBgPhotoRotation = if (useLive) liveCustomBgRotation else preset.customBgPhotoRotation,
                        contentAreaPhotoOffsetX = if (useLive) liveContentOffsetX else preset.contentAreaPhotoOffsetX,
                        contentAreaPhotoOffsetY = if (useLive) liveContentOffsetY else preset.contentAreaPhotoOffsetY,
                        contentAreaPhotoScale = if (useLive) liveContentScale else preset.contentAreaPhotoScale,
                        contentAreaPhotoRotation = if (useLive) liveContentRotation else preset.contentAreaPhotoRotation,
                        memoAreaPhotoOffsetX = if (useLive) liveMemoOffsetX else preset.memoAreaPhotoOffsetX,
                        memoAreaPhotoOffsetY = if (useLive) liveMemoOffsetY else preset.memoAreaPhotoOffsetY,
                        memoAreaPhotoScale = if (useLive) liveMemoScale else preset.memoAreaPhotoScale,
                        memoAreaPhotoRotation = if (useLive) liveMemoRotation else preset.memoAreaPhotoRotation,
                        fileListItemPhotoOffsetX = if (useLive) liveFileListItemOffsetX else preset.fileListItemPhotoOffsetX,
                        fileListItemPhotoOffsetY = if (useLive) liveFileListItemOffsetY else preset.fileListItemPhotoOffsetY,
                        fileListItemPhotoScale = if (useLive) liveFileListItemScale else preset.fileListItemPhotoScale,
                        fileListItemPhotoRotation = if (useLive) liveFileListItemRotation else preset.fileListItemPhotoRotation,
                        fileListBgPhotoOffsetX = if (useLive) liveFileListBgOffsetX else preset.fileListBackgroundPhotoOffsetX,
                        fileListBgPhotoOffsetY = if (useLive) liveFileListBgOffsetY else preset.fileListBackgroundPhotoOffsetY,
                        fileListBgPhotoScale = if (useLive) liveFileListBgScale else preset.fileListBackgroundPhotoScale,
                        fileListBgPhotoRotation = if (useLive) liveFileListBgRotation else preset.fileListBackgroundPhotoRotation
                    )
                }
            }
            
            // No checkmark - using breathing glow effect instead
            
            // Menu button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Options",
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = {
                            onRename()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onDelete()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MiniAppPreview(
    bgColor: Color,
    contentColor: Color,
    memoColor: Color,
    fileListItemColor: Color,
    fileListBgColor: Color,
    bgPhotoUri: String? = null,
    contentPhotoUri: String? = null,
    memoPhotoUri: String? = null,
    fileListItemPhotoUri: String? = null,
    fileListBgPhotoUri: String? = null,
    bgBlur: Float = 0f,
    contentBlur: Float = 0f,
    memoBlur: Float = 0f,
    fileListItemBlur: Float = 0f,
    fileListBgBlur: Float = 0f,
    fileListBgPhotoOffsetX: Float = 0f,
    fileListBgPhotoOffsetY: Float = 0f,
    fileListBgPhotoScale: Float = 1f
) {
    val hasBgPhoto = !bgPhotoUri.isNullOrEmpty()
    val hasContentPhoto = !contentPhotoUri.isNullOrEmpty()
    val hasMemoPhoto = !memoPhotoUri.isNullOrEmpty()
    val hasFileListItemPhoto = !fileListItemPhotoUri.isNullOrEmpty()
    val hasFileListBgPhoto = !fileListBgPhotoUri.isNullOrEmpty()
    
    // Scale blur for mini preview - proportional to preview size vs actual app
    // Actual app uses 50f multiplier at ~800dp, mini preview is ~100dp, so ratio = 100/800 = 0.125
    // Use multiplier of 6 (50 * 0.125 ≈ 6) for perceptually similar blur
    val blurMultiplier = 6f
    val scaledBgBlur = (bgBlur * blurMultiplier).coerceIn(0f, 30f)
    val scaledContentBlur = (contentBlur * blurMultiplier).coerceIn(0f, 30f)
    val scaledMemoBlur = (memoBlur * blurMultiplier).coerceIn(0f, 30f)
    val scaledFileListItemBlur = (fileListItemBlur * blurMultiplier).coerceIn(0f, 30f)
    val scaledFileListBgBlur = (fileListBgBlur * blurMultiplier).coerceIn(0f, 30f)
    
    
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Background - photo or color (only show if NOT using file list background photo)
        // This prevents main background from showing behind file list when file list has alpha
        if (hasBgPhoto && !hasFileListBgPhoto) {
            AsyncImage(
                model = bgPhotoUri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (scaledBgBlur > 0f) Modifier.blur(scaledBgBlur.dp) else Modifier),
                contentScale = ContentScale.Crop
            )
        } else if (!hasFileListBgPhoto) {
            Box(modifier = Modifier.fillMaxSize().background(bgColor))
        }
        
        // Match dialog preview layout structure exactly
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            // Top bar simulation (same as dialog)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        RoundedCornerShape(4.dp)
                    )
            )
            
            Spacer(modifier = Modifier.height(3.dp))
            
            // Split layout with Row (same as dialog)
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // File list area (left side - 30% width to match dialog)
                Box(
                    modifier = Modifier
                        .weight(0.3f)
                        .fillMaxHeight()
                ) {
                    // Background color layer (same as dialog)
                    val fileListBgColorToShow = if (hasFileListBgPhoto) {
                        Color.Transparent
                    } else {
                        fileListBgColor
                    }
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                fileListBgColorToShow,
                                RoundedCornerShape(4.dp)
                            )
                    )
                    
                    // File list background photo
                    if (hasFileListBgPhoto) {
                        val painter = rememberHighQualityPhotoPainter(photoUri = fileListBgPhotoUri)
                        val imageAlpha = when (painter.state) {
                            is coil.compose.AsyncImagePainter.State.Success -> 1f
                            else -> 0f
                        }
                        
                        androidx.compose.foundation.Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(4.dp))
                                .graphicsLayer {
                                    val flBgImgSz = painter.intrinsicSize
                                    val isLoadedFlBgP = painter.state is coil.compose.AsyncImagePainter.State.Success
                                    val ratio = if (isLoadedFlBgP && flBgImgSz.width > 0 && flBgImgSz.height > 0 && flBgImgSz.width.isFinite() && flBgImgSz.height.isFinite() && size.width > 0 && size.height > 0) {
                                        maxOf(size.width / flBgImgSz.width, size.height / flBgImgSz.height) /
                                            minOf(size.width / flBgImgSz.width, size.height / flBgImgSz.height)
                                    } else 1f
                                    val effectiveScale = fileListBgPhotoScale * ratio
                                    scaleX = effectiveScale
                                    scaleY = effectiveScale
                                    translationX = -fileListBgPhotoOffsetX * size.width
                                    translationY = fileListBgPhotoOffsetY * size.height
                                }
                                .applyPhotoAlpha(imageAlpha)
                                .then(
                                    if (scaledFileListBgBlur > 0f) {
                                        Modifier.blur(scaledFileListBgBlur.dp)
                                    } else {
                                        Modifier
                                    }
                                ),
                            contentScale = if (painter.state is coil.compose.AsyncImagePainter.State.Success) ContentScale.Fit else ContentScale.Crop
                        )
                    }
            
            // Mini file list items
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                repeat(5) {
                    if (hasFileListItemPhoto) {
                        AsyncImage(
                            model = fileListItemPhotoUri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .then(if (scaledFileListItemBlur > 0f) Modifier.blur(scaledFileListItemBlur.dp) else Modifier),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(fileListItemColor, RoundedCornerShape(1.dp))
                        )
                    }
                }
            }
                }
                
                // Content and memo area (right side) - inside Row to match dialog
                Column(
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
            // Content area (top, larger)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.65f)
                    .clip(RoundedCornerShape(1.dp))
            ) {
                // Content background - photo or color
                if (hasContentPhoto) {
                    AsyncImage(
                        model = contentPhotoUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (scaledContentBlur > 0f) Modifier.blur(scaledContentBlur.dp) else Modifier),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(contentColor))
                }
                
                // Simulate text lines
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(3.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    repeat(6) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (it == 5) 0.6f else 0.9f)
                                .height(2.dp)
                                .background(
                                    Color.White.copy(alpha = 0.3f),
                                    RoundedCornerShape(0.5.dp)
                                )
                        )
                    }
                }
            }
            
            // Memo area (bottom, smaller)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
                    .clip(RoundedCornerShape(1.dp))
            ) {
                // Memo background - photo or color
                if (hasMemoPhoto) {
                    AsyncImage(
                        model = memoPhotoUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (scaledMemoBlur > 0f) Modifier.blur(scaledMemoBlur.dp) else Modifier),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(memoColor))
                }
                
                // Simulate memo text
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(3.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(2.dp)
                                .background(
                                    Color.White.copy(alpha = 0.25f),
                                    RoundedCornerShape(0.5.dp)
                                )
                        )
                    }
                }
            }
                }
            }
        }
    }
}

@Composable
fun AddPresetCard(
    onClick: () -> Unit,
    isInSaveMode: Boolean = false
) {
    Card(
        modifier = Modifier
            .size(140.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    if (isInSaveMode) Icons.Default.Save else Icons.Default.Add,
                    contentDescription = if (isInSaveMode) "Save Preset" else "Add Preset",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    if (isInSaveMode) "Save Changes" else "New Preset",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

val memoAreaColorPresets = listOf(
    SimpleColorPreset("Light Amber", Color(0xFFFFF8E1)),
    SimpleColorPreset("Amber", Color(0xFFFFC107)),
    SimpleColorPreset("Light Orange", Color(0xFFFFF3E0)),
    SimpleColorPreset("Orange", Color(0xFFFF9800)),
    SimpleColorPreset("Light Pink", Color(0xFFFCE4EC)),
    SimpleColorPreset("Pink", Color(0xFFE91E63)),
    SimpleColorPreset("Light Cyan", Color(0xFFE0F7FA)),
    SimpleColorPreset("Cyan", Color(0xFF00BCD4)),
    SimpleColorPreset("Light Lime", Color(0xFFF9FBE7)),
    SimpleColorPreset("Lime", Color(0xFFCDDC39)),
    SimpleColorPreset("Light Teal", Color(0xFFE0F2F1)),
    SimpleColorPreset("Teal", Color(0xFF009688))
)
