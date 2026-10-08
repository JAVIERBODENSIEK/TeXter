package com.j4.texter2025.ui.components

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.j4.texter2025.MainActivity
import com.j4.texter2025.data.FileModel
import com.j4.texter2025.data.SearchMethod
import com.j4.texter2025.data.SortMethod
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import java.io.File
import com.j4.texter2025.ui.components.FullScreenEditor
import com.j4.texter2025.ui.components.ContentAreaStyle
import com.j4.texter2025.ui.components.ContentAreaSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileListScreen(
    files: List<FileModel>,
    onFileSelect: (FileModel) -> Unit,
    onFilesDeleted: () -> Unit = {},
    onFileEditRequest: (FileModel?, Boolean) -> Unit, // Added for full screen editor
    onSearch: (String, SearchMethod, Boolean) -> List<FileModel>,
    modifier: Modifier = Modifier,
    saveFile: (Context, FileModel) -> Unit,
    deleteTextFile: (Context, FileModel) -> Unit,
    contentAreaStyle: ContentAreaStyle,
    contentAreaSize: ContentAreaSize,
    customBgColor: Color?,
    contentAreaColor: Color? = null,
    memoBgColor: Color? = null,
    listAreaColor: Color? = null,
    fileListItemColor: Color? = null,
    fileListBackgroundColor: Color? = null,
    // Photo background parameters - separate for each area
    backgroundPhotoUri: String? = null,
    backgroundPhotoAlpha: Float = 1f,
    backgroundPhotoBlur: Float = 0f,
    backgroundPhotoOffsetX: Float = 0f,
    backgroundPhotoOffsetY: Float = 0f,
    backgroundPhotoScale: Float = 1f,
    backgroundPhotoRotation: Float = 0f,
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
    // Blur values for color backgrounds
    customBgBlur: Float = 0f,
    fileListBackgroundBlur: Float = 0f,
    fileListItemBlur: Float = 0f,
    // Photo background modes
    customBgPhotoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
    fileListItemPhotoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
    fileListBackgroundPhotoBackgroundMode: com.j4.texter2025.data.PhotoBackgroundMode = com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC,
    // Editor drag mode
    editorDragMode: EditorDragMode = EditorDragMode.DUAL_BAR,
    hideStatusBar: Boolean = false,
    // Back to Settings FAB
    showBackToSettingsFab: Boolean = false,
    onBackToSettings: () -> Unit = {},
    // Shared FAB position state
    fabOffsetX: Float = 0f,
    fabOffsetY: Float = 0f,
    onFabOffsetChange: (Float, Float) -> Unit = { _, _ -> },
    onImportRequest: () -> Unit = {},
    onExportRequest: (String) -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchMethod by remember { mutableStateOf(SearchMethod.TITLE_AND_CONTENT) }
    var isCaseSensitive by remember { mutableStateOf(false) }
    var displayedFiles by remember { mutableStateOf(files) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var currentSortMethod by remember { mutableStateOf(SortMethod.NAME_ASC) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedFiles by remember { mutableStateOf(emptySet<FileModel>()) }
    var showFileEditDialog: FileModel? by remember { mutableStateOf(null) }
    var showFullEditor by remember { mutableStateOf(false) }
    var lastAccessedFile by remember { mutableStateOf<FileModel?>(null) }
    var isCreatingNewFile by remember { mutableStateOf(false) }
    val context = LocalContext.current

    fun sortFiles(files: List<FileModel>, method: SortMethod): List<FileModel> {
        return when (method) {
            SortMethod.NAME_ASC -> files.sortedBy { it.name.lowercase() }
            SortMethod.NAME_DESC -> files.sortedByDescending { it.name.lowercase() }
            SortMethod.DATE_NEWEST -> files.sortedByDescending { it.lastModified }
            SortMethod.DATE_OLDEST -> files.sortedBy { it.lastModified }
            SortMethod.SIZE_LARGEST -> files.sortedByDescending { it.content.length }
            SortMethod.SIZE_SMALLEST -> files.sortedBy { it.content.length }
        }
    }

    LaunchedEffect(files, searchQuery, searchMethod, isCaseSensitive, currentSortMethod) {
        displayedFiles = if (searchQuery.isEmpty()) {
            sortFiles(files, currentSortMethod)
        } else {
            sortFiles(onSearch(searchQuery, searchMethod, isCaseSensitive), currentSortMethod)
        }
        selectedFiles = selectedFiles.filter { selected -> 
            displayedFiles.any { it.id == selected.id } 
        }.toSet()
        isSelectionMode = selectedFiles.isNotEmpty()
    }

    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete ${selectedFiles.size} selected file(s)?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        (context as? MainActivity)?.let { activity ->
                            activity.deleteFiles(context, selectedFiles.toList())
                            selectedFiles = emptySet()
                            isSelectionMode = false
                            onFilesDeleted()
                            snackbarMessage = "Files deleted successfully"
                            showSnackbar = true
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = if (hideStatusBar) {
            WindowInsets(0, 0, 0, 0)
        } else {
            ScaffoldDefaults.contentWindowInsets
        },
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (hideStatusBar) Modifier else Modifier.statusBarsPadding())
                    .padding(top = 8.dp, start = 10.dp, end = 10.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                FileListSearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    currentSortMethod = currentSortMethod,
                    onSortMethodChange = { currentSortMethod = it },
                    searchMethod = searchMethod,
                    onSearchMethodChange = { searchMethod = it },
                    isCaseSensitive = isCaseSensitive,
                    onCaseSensitiveChange = { isCaseSensitive = it },
                    showFileExtensions = MainActivity.showExtensions,
                    onShowFileExtensionsChange = { MainActivity.showExtensions = it },
                    onImportRequest = onImportRequest,
                    onExportRequest = {
                        val selectedFile = selectedFiles.firstOrNull() ?: return@FileListSearchBar
                        val mainActivity = context as? MainActivity ?: return@FileListSearchBar
                        mainActivity.selectedFileForExport = selectedFile
                        val exportName = selectedFile.name.removeSuffix(".txt") + ".txt"
                        onExportRequest(exportName)
                    },
                    onOpenSettings = onOpenSettings,
                    exportEnabled = selectedFiles.size == 1,
                    modifier = Modifier.fillMaxWidth(0.96f)
                )
            }
        },
        bottomBar = {
            if (isSelectionMode) {
                BottomAppBar {
                    Text(
                        "${selectedFiles.size} selected",
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Save to Storage button
                    IconButton(
                        onClick = {
                            selectedFiles.firstOrNull()?.let { file ->
                                (context as? MainActivity)?.let { mainActivity ->
                                    mainActivity.selectedFileForExport = file
                                    // Remove .txt if it exists and add it back to avoid double extension
                                    val baseFileName = file.name.removeSuffix(".txt")
                                    mainActivity.exportLauncher.launch("$baseFileName.txt")
                                }
                            }
                        },
                        enabled = selectedFiles.size == 1
                    ) {
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Save to Storage",
                            tint = if (selectedFiles.size == 1) 
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        )
                    }

                    // Share button
                    IconButton(
                        onClick = {
                            selectedFiles.firstOrNull()?.let { file ->
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, file.content)
                                    putExtra(Intent.EXTRA_SUBJECT, file.name)
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share file")
                                context.startActivity(shareIntent)
                            }
                        },
                        enabled = selectedFiles.size == 1
                    ) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = "Share file",
                            tint = if (selectedFiles.size == 1) 
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        )
                    }

                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete selected")
                    }
                }
            }
        },
        floatingActionButton = {
            if (!isSelectionMode && !showBackToSettingsFab) {
                // Create new file FAB (only show when not in back-to-settings mode)
                FloatingActionButton(
                    onClick = {
                        showFileEditDialog = null // Clear any existing file selection for dialog
                        isCreatingNewFile = true      // Signal that we are creating a new file
                    }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Create new file")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
        ) {
            val mainBackgroundBaseColor = listAreaColor ?: MaterialTheme.colorScheme.background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(mainBackgroundBaseColor)
            )

            // Photo background layer (if photo is selected)
            if (!backgroundPhotoUri.isNullOrEmpty()) {
                val painterBg = rememberHighQualityPhotoPainter(
                    photoUri = backgroundPhotoUri,
                    onError = { error ->
                        android.util.Log.e("FileListScreen", "Failed to load background photo: ${error.result.throwable.message}")
                    }
                )
                val isLoadedBg = painterBg.state is coil.compose.AsyncImagePainter.State.Success
                val imageAlpha = when (painterBg.state) {
                    is coil.compose.AsyncImagePainter.State.Error -> 0f
                    is coil.compose.AsyncImagePainter.State.Success -> backgroundPhotoAlpha
                    else -> backgroundPhotoAlpha
                }
                val bgImgSize = painterBg.intrinsicSize
                val bgCropRatio = if (
                    isLoadedBg &&
                    bgImgSize.width > 0 &&
                    bgImgSize.height > 0 &&
                    bgImgSize.width.isFinite() &&
                    bgImgSize.height.isFinite()
                ) bgImgSize else null
                val bgMode = customBgPhotoBackgroundMode

                androidx.compose.foundation.Image(
                    painter = painterBg,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = backgroundPhotoRotation
                            when (bgMode) {
                                com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC -> {
                                    val ratio = if (bgCropRatio != null && size.width > 0 && size.height > 0) {
                                        maxOf(size.width / bgCropRatio.width, size.height / bgCropRatio.height) /
                                            minOf(size.width / bgCropRatio.width, size.height / bgCropRatio.height)
                                    } else 1f
                                    val effectiveScale = backgroundPhotoScale * ratio
                                    scaleX = effectiveScale
                                    scaleY = effectiveScale
                                    translationX = -backgroundPhotoOffsetX * size.width
                                    translationY = backgroundPhotoOffsetY * size.height
                                }
                                com.j4.texter2025.data.PhotoBackgroundMode.STATIC_ZOOM -> {
                                    val autoScale = if (bgCropRatio != null && size.width > 0 && size.height > 0) {
                                        maxOf(size.width / bgCropRatio.width, size.height / bgCropRatio.height)
                                    } else 1f
                                    val effectiveScale = autoScale * backgroundPhotoScale
                                    scaleX = effectiveScale
                                    scaleY = effectiveScale
                                    translationX = 0f
                                    translationY = 0f
                                }
                            }
                        }
                        .applyPhotoAlpha(imageAlpha)
                        .applyPhotoBlur(backgroundPhotoBlur),
                    contentScale = when (bgMode) {
                        com.j4.texter2025.data.PhotoBackgroundMode.STATIC_ZOOM -> ContentScale.Fit
                        else -> if (isLoadedBg) ContentScale.Fit else ContentScale.Crop
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur((customBgBlur * 5f).dp)
                        .background(listAreaColor ?: MaterialTheme.colorScheme.background)
                )
            }

            // File list with photo background support
            val fileListBaseColor = if (fileListBackgroundPhotoUri.isNullOrEmpty()) {
                fileListBackgroundColor ?: Color.Transparent
            } else {
                Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (fileListBackgroundPhotoUri.isNullOrEmpty()) {
                            Modifier.blur((fileListBackgroundBlur * 5f).dp)
                        } else {
                            Modifier
                        }
                    )
                    .background(fileListBaseColor)
            ) {
                // File List Background photo layer (if photo is selected) - overlays the color
                if (!fileListBackgroundPhotoUri.isNullOrEmpty()) {
                    val painter = rememberHighQualityPhotoPainter(
                        photoUri = fileListBackgroundPhotoUri,
                        onError = { error ->
                            android.util.Log.e("FileListScreen", "Failed to load photo: ${error.result.throwable.message}")
                        }
                    )
                    val isLoaded = painter.state is coil.compose.AsyncImagePainter.State.Success
                    val imageAlpha = when (painter.state) {
                        is coil.compose.AsyncImagePainter.State.Error -> 0f
                        is coil.compose.AsyncImagePainter.State.Success -> fileListBackgroundPhotoAlpha
                        else -> fileListBackgroundPhotoAlpha
                    }
                    val flBgImgSize = painter.intrinsicSize
                    val flBgCropRatio = if (
                        isLoaded &&
                        flBgImgSize.width > 0 &&
                        flBgImgSize.height > 0 &&
                        flBgImgSize.width.isFinite() &&
                        flBgImgSize.height.isFinite()
                    ) flBgImgSize else null
                    val flBgMode = fileListBackgroundPhotoBackgroundMode

                    androidx.compose.foundation.Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                rotationZ = fileListBackgroundPhotoRotation
                                when (flBgMode) {
                                    com.j4.texter2025.data.PhotoBackgroundMode.DYNAMIC -> {
                                        val ratio = if (flBgCropRatio != null && size.width > 0 && size.height > 0) {
                                            maxOf(size.width / flBgCropRatio.width, size.height / flBgCropRatio.height) /
                                                minOf(size.width / flBgCropRatio.width, size.height / flBgCropRatio.height)
                                        } else 1f
                                        val effectiveScale = fileListBackgroundPhotoScale * ratio
                                        scaleX = effectiveScale
                                        scaleY = effectiveScale
                                        translationX = -fileListBackgroundPhotoOffsetX * size.width
                                        translationY = fileListBackgroundPhotoOffsetY * size.height
                                    }
                                    com.j4.texter2025.data.PhotoBackgroundMode.STATIC_ZOOM -> {
                                        val autoScale = if (flBgCropRatio != null && size.width > 0 && size.height > 0) {
                                            maxOf(size.width / flBgCropRatio.width, size.height / flBgCropRatio.height)
                                        } else 1f
                                        val effectiveScale = autoScale * fileListBackgroundPhotoScale
                                        scaleX = effectiveScale
                                        scaleY = effectiveScale
                                        translationX = 0f
                                        translationY = 0f
                                    }
                                }
                            }
                            .applyPhotoAlpha(imageAlpha)
                            .applyPhotoBlur(fileListBackgroundPhotoBlur),
                        contentScale = when (flBgMode) {
                            com.j4.texter2025.data.PhotoBackgroundMode.STATIC_ZOOM -> ContentScale.Fit
                            else -> if (isLoaded) ContentScale.Fit else ContentScale.Crop
                        }
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingValues)
                ) {
                items(displayedFiles) { file ->
                    FileListItem(
                        file = file,
                        isSelected = selectedFiles.contains(file),
                        onSelect = {
                            if (!isSelectionMode) {
                                onFileEditRequest(file, true) // Request FullScreenEditor
                            } else {
                                selectedFiles = if (selectedFiles.contains(file)) {
                                    selectedFiles - file
                                } else {
                                    selectedFiles + file
                                }
                                if (selectedFiles.isEmpty()) {
                                    isSelectionMode = false
                                }
                            }
                        },
                        onEdit = {
                            if (showFileEditDialog == null) {
                                onFileEditRequest(file, false) // Request FileEditDialog
                            }
                        },
                        onToggleSelection = {
                            selectedFiles = if (selectedFiles.contains(file)) {
                                selectedFiles - file
                            } else {
                                selectedFiles + file
                            }
                            if (selectedFiles.isEmpty()) {
                                isSelectionMode = false
                            }
                        },
                        showExtensions = MainActivity.showExtensions,
                        isRecentlyAccessed = lastAccessedFile == file,
                        isSelectionMode = isSelectionMode,
                        onLongPress = {
                            if (!isSelectionMode) {
                                isSelectionMode = true
                                selectedFiles = setOf(file)
                            }
                        },
                        fileNameContent = if (searchQuery.isNotEmpty()) {
                            {
                                TypewriterText(
                                    text = file.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    autoStart = true
                                )
                            }
                        } else null,
                        customBgColor = fileListItemColor,
                        photoUri = fileListItemPhotoUri,
                        photoAlpha = fileListItemPhotoAlpha,
                        photoBlur = fileListItemPhotoBlur,
                        photoOffsetX = fileListItemPhotoOffsetX,
                        photoOffsetY = fileListItemPhotoOffsetY,
                        photoScale = fileListItemPhotoScale,
                        photoRotation = fileListItemPhotoRotation,
                        photoBackgroundMode = fileListItemPhotoBackgroundMode
                    )
                }
            }
            }

            // Only show FileEditDialog via pencil (edit) button
            if (showFileEditDialog != null || isCreatingNewFile) {
                FileEditDialog(
                    file = showFileEditDialog,
                    onSave = { file ->
                        saveFile(context, file)
                        lastAccessedFile = file
                        showFileEditDialog = null
                        isCreatingNewFile = false
                    },
                    onCancel = {
                        lastAccessedFile = showFileEditDialog
                        showFileEditDialog = null
                        showFullEditor = false
                        isCreatingNewFile = false  // Ensure new file creation mode is reset
                    },
                    onDelete = { file ->
                        deleteTextFile(context, file)
                        lastAccessedFile = null
                        showFileEditDialog = null
                        showFullEditor = false
                        isCreatingNewFile = false
                    },
                    activity = context as MainActivity,
                    importRequestCode = 0,
                    exportRequestCode = 1,
                    existingFiles = files.map { it.name },
                    initialShowFullEditor = false,
                    contentAreaStyle = contentAreaStyle,
                    contentAreaSize = contentAreaSize,
                    customBgColor = customBgColor,
                    contentAreaColor = contentAreaColor,
                    memoBgColor = memoBgColor,
                    customBgPhotoUri = backgroundPhotoUri,
                    customBgPhotoAlpha = backgroundPhotoAlpha,
                    customBgPhotoBlur = backgroundPhotoBlur,
                    customBgPhotoOffsetX = backgroundPhotoOffsetX,
                    customBgPhotoOffsetY = backgroundPhotoOffsetY,
                    customBgPhotoScale = backgroundPhotoScale
                )
            }

            // Show FULL SCREEN editor when tapping entry
            if (showFullEditor && lastAccessedFile != null) {
                FullScreenEditor(
                    file = lastAccessedFile!!,
                    onSave = { file ->
                        saveFile(context, file)
                        lastAccessedFile = file
                        showFullEditor = false
                    },
                    onCancel = {
                        showFullEditor = false
                    },
                    onDelete = { file ->
                        deleteTextFile(context, file)
                        lastAccessedFile = null
                        showFullEditor = false
                    },
                    activity = context as MainActivity,
                    contentAreaStyle = contentAreaStyle,
                    contentAreaSize = contentAreaSize,
                    customBgColor = customBgColor,
                    contentAreaColor = contentAreaColor,
                    memoAreaColor = memoBgColor,
                    showBackToSettingsFab = showBackToSettingsFab,
                    onBackToSettings = onBackToSettings,
                    fabOffsetX = fabOffsetX,
                    fabOffsetY = fabOffsetY,
                    onFabOffsetChange = onFabOffsetChange
                )
            }

            if (showSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { showSnackbar = false }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(snackbarMessage)
                }
            }
        }
    }
}
