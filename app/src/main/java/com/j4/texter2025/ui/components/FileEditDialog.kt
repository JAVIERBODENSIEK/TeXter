package com.j4.texter2025.ui.components

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.isSystemInDarkTheme
import com.j4.texter2025.data.FileModel
import com.j4.texter2025.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

private object FileDialogUtils {
    fun validateFileName(name: String): Boolean {
        // Check if name is not blank
        if (name.isBlank()) {
            throw IllegalArgumentException("File name cannot be empty")
        }
        
        // Check for invalid characters
        if (name.contains(Regex("[<>:\"/\\\\|?*]"))) {
            throw IllegalArgumentException("File name contains invalid characters")
        }
        
        return true
    }

    fun ensureFileExtension(name: String): String {
        return if (name.endsWith(".txt", ignoreCase = true)) name else "$name.txt"
    }

    fun getDisplayFileName(name: String): String {
        return if (!MainActivity.showExtensions && name.endsWith(".txt", ignoreCase = true)) {
            name.substringBeforeLast(".", missingDelimiterValue = name)
        } else {
            name
        }
    }

    fun importFile(activity: Activity, requestCode: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "text/plain"
        }
        activity.startActivityForResult(intent, requestCode)
    }

    fun exportFile(fileModel: FileModel, activity: Activity, requestCode: Int) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, fileModel.name)
        }
        activity.startActivityForResult(intent, requestCode)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
@Composable
fun FileEditDialog(
    file: FileModel? = null,
    onSave: (FileModel) -> Unit,
    onCancel: () -> Unit,
    onDelete: (FileModel) -> Unit,
    activity: Activity,
    importRequestCode: Int,
    exportRequestCode: Int,
    existingFiles: List<String> = emptyList(),
    initialShowFullEditor: Boolean = false,
    customBgColor: Color? = null,
    contentAreaColor: Color? = MaterialTheme.colorScheme.surface,
    memoBgColor: Color? = MaterialTheme.colorScheme.surface,
    contentAreaStyle: ContentAreaStyle = ContentAreaStyle.EDGE_TO_EDGE, // Default to edge-to-edge
    contentAreaSize: ContentAreaSize = ContentAreaSize(0.98f, 1.0f), // Default to 98% width, full height
    // Photo background parameters for custom background
    customBgPhotoUri: String? = null,
    customBgPhotoAlpha: Float = 1f,
    customBgPhotoBlur: Float = 0f,
    customBgPhotoOffsetX: Float = 0f,
    customBgPhotoOffsetY: Float = 0f,
    customBgPhotoScale: Float = 1f,
    customBgPhotoRotation: Float = 0f
) {
    val mainActivity = activity as? MainActivity
    if (mainActivity == null) {
        Log.e("FileEditDialog", "Activity is not MainActivity: ${activity.javaClass.name}")
        Toast.makeText(activity, "Error: Could not access MainActivity", Toast.LENGTH_SHORT).show()
        onCancel()
        return
    }

    // Don't apply photo alpha to color - colors are saved with full opacity
    // Photo alpha is only for photos (applied to AsyncImage below)
    val bgColor = customBgColor ?: MaterialTheme.colorScheme.surface
    
    // Debug logging
    Log.d("FileEditDialog", "customBgColor: $customBgColor")
    Log.d("FileEditDialog", "customBgPhotoAlpha: $customBgPhotoAlpha")
    Log.d("FileEditDialog", "bgColor: $bgColor")
    Log.d("FileEditDialog", "customBgPhotoUri: $customBgPhotoUri")

    var showFullEditor by remember { mutableStateOf(initialShowFullEditor) }
    var fullScreen by remember { mutableStateOf(false) }
    var fileName by remember { mutableStateOf(FileDialogUtils.getDisplayFileName(file?.name ?: "")) }
    var fileNameError by remember { mutableStateOf<String?>(null) }
    var fileContent by remember { mutableStateOf(file?.content ?: "") }
    var fileMemo by remember { mutableStateOf(file?.memo ?: "") }
    var showMemo by remember { mutableStateOf(file?.memoVisible ?: false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showCaseConfirmation by remember { mutableStateOf(false) }
    var existingCaseFile by remember { mutableStateOf("") }
    var showMemoDropdown by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }
    var isFileNameError by remember { mutableStateOf(false) }
    var previewScrollOffset by remember { mutableStateOf(0) }
    var pendingEditorScrollOffset by remember { mutableStateOf<Int?>(null) }

    val density = LocalDensity.current
    val lineHeightSp = 24.sp
    val lineHeightPx = with(density) { lineHeightSp.toPx() }

    val prefs = remember(activity) {
        activity.getSharedPreferences("TeXterPrefs", android.content.Context.MODE_PRIVATE)
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

    // Determine if we should animate (TypewriterText) or not
    val showFileNameTypewriter = file != null && fileName.isNotBlank() && fileName == FileDialogUtils.getDisplayFileName(file.name)
    val showContentTypewriter = file != null && fileContent.isNotBlank() && fileContent == file.content
    val showMemoTypewriter = file != null && fileMemo.isNotBlank() && fileMemo == file.memo

    // Track if there are unsaved changes
    val initialContent = remember(file) { file?.content ?: "" }
    val initialName = remember(file) { file?.name ?: "" }
    val initialMemo = remember(file) { file?.memo ?: "" }
    
    val hasUnsavedChanges by remember(fileName, fileContent, fileMemo, initialContent, initialName, initialMemo) {
        derivedStateOf {
            val nameChanged = fileName.trim() != FileDialogUtils.getDisplayFileName(initialName).trim()
            val contentChanged = fileContent.trim() != initialContent.trim()
            val fileMemoChanged = fileMemo.trim() != initialMemo.trim()
            
            Log.d("FileEditDialog", """
                Change Detection:
                Name: $fileName vs ${FileDialogUtils.getDisplayFileName(initialName)} = $nameChanged
                Content: ${fileContent.take(20)} vs ${initialContent.take(20)} = $contentChanged
                Memo: ${fileMemo.take(20)} vs ${initialMemo.take(20)} = $fileMemoChanged
            """.trimIndent())
            
            nameChanged || contentChanged || fileMemoChanged
        }
    }

    val contentBgColor = contentAreaColor ?: MaterialTheme.colorScheme.surfaceVariant
    // Use the provided memoBgColor parameter or derive from contentBgColor
    val effectiveMemoBgColor = memoBgColor ?: contentBgColor.copy(
        red = contentBgColor.red * 0.5f,
        green = contentBgColor.green * 0.5f,
        blue = contentBgColor.blue * 0.5f
    )

    fun handleSave() {
        if (fileName.trim().isEmpty()) {
            isFileNameError = true
            showError = true
            errorMessage = "Please enter a file name"
            return
        }
        
        try {
            val finalName = FileDialogUtils.ensureFileExtension(fileName)
            FileDialogUtils.validateFileName(finalName)
            onSave(FileModel(finalName, fileContent, fileMemo, file?.id, keepBothFiles = false))
            onCancel()
        } catch (e: IllegalArgumentException) {
            fileNameError = e.message
            showError = true
            errorMessage = e.message ?: "Invalid file name"
        } catch (e: Exception) {
            showError = true
            errorMessage = e.message ?: "Failed to save file"
        }
    }

    fun handleExit() {
        Log.d("FileEditDialog", "handleExit called, hasUnsavedChanges: $hasUnsavedChanges")
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
                handleSave()
            }
            unsavedChangesBehavior == UnsavedChangesBehavior.NEVER_ASK_DISCARD -> {
                // Discard changes and exit
                onCancel()
            }
        }
    }

    CompositionLocalProvider(LocalDensity provides openedNoteDensity) {
    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            title = { Text("Save Changes?") },
            text = { Text("Do you want to save your changes before exiting?") },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { 
                            showExitConfirmation = false
                            onCancel()
                        }
                    ) {
                        Text("Don't Save")
                    }
                    Button(
                        onClick = {
                            try {
                                val finalName = FileDialogUtils.ensureFileExtension(fileName)
                                FileDialogUtils.validateFileName(finalName)
                                onSave(FileModel(finalName, fileContent, fileMemo, file?.id, keepBothFiles = false))
                                showExitConfirmation = false
                                onCancel()
                            } catch (e: IllegalArgumentException) {
                                showError = true
                                errorMessage = e.message ?: "Invalid file name"
                            } catch (e: Exception) {
                                showError = true
                                errorMessage = e.message ?: "Failed to save file"
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        )
    }

    if (showCaseConfirmation) {
        AlertDialog(
            onDismissRequest = { showCaseConfirmation = false },
            title = { Text("File with similar name exists") },
            text = { 
                Column {
                    Text("A file named '$existingCaseFile' already exists.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Do you want to replace the existing file or keep both with a numbered suffix?")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCaseConfirmation = false
                        // Keep both files with numbered suffix
                        try {
                            val finalName = FileDialogUtils.ensureFileExtension(fileName)
                            FileDialogUtils.validateFileName(finalName)
                            onSave(FileModel(finalName, fileContent, fileMemo, file?.id, keepBothFiles = true))
                            onCancel()
                        } catch (e: Exception) {
                            showError = true
                            errorMessage = e.message ?: "Failed to save file"
                        }
                    }
                ) {
                    Text("Keep Both")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCaseConfirmation = false
                        // Replace existing file
                        try {
                            val finalName = FileDialogUtils.ensureFileExtension(fileName)
                            FileDialogUtils.validateFileName(finalName)
                            onSave(FileModel(finalName, fileContent, fileMemo, file?.id, keepBothFiles = false))
                            onCancel()
                        } catch (e: Exception) {
                            showError = true
                            errorMessage = e.message ?: "Failed to save file"
                        }
                    }
                ) {
                    Text("Replace")
                }
            }
        )
    }

    val editorScrollState = rememberScrollState()
    // Treat 100% x 100% as true full-screen (legacy edge-to-edge behaviour)
    val isFullScreen = contentAreaSize.widthPercent >= 0.999f && contentAreaSize.heightPercent >= 0.999f

    // Use AlertDialog for preview mode (initialShowFullEditor = false) and Dialog with Surface for full editor mode
    if (!initialShowFullEditor) {
        // Preview mode - use AlertDialog for compact display
        AlertDialog(
            onDismissRequest = { /* Do nothing to prevent auto-dismiss */ },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            title = { 
                Text(
                    text = if (file == null) "Create New File" else "Edit File",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    // File name field
                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        label = { Text("File Name") },
                        singleLine = true,
                        isError = showError || fileNameError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            color = if (showError || fileNameError != null)
                                MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    
                    // Error message if any
                    if (showError || fileNameError != null) {
                        Text(
                            text = if (showError) errorMessage else fileNameError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    // Content field
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 150.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 1.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        BasicTextField(
                            value = fileContent,
                            onValueChange = { fileContent = it },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 24.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (fileContent.isEmpty()) {
                                        Text(
                                            "Enter text content here",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontFamily = FontFamily.Monospace,
                                                lineHeight = 24.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                    
                    // Memo toggle button - matches FullScreenEditor
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showMemo = !showMemo },
                            modifier = Modifier
                                .padding(0.dp)
                                .height(28.dp), // Exact same height as FullScreenEditor
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp) // Exact same padding as FullScreenEditor
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
                    }
                    
                    // Memo area
                    AnimatedVisibility(visible = showMemo) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            // Memo field
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 80.dp, max = 120.dp),
                                color = effectiveMemoBgColor,
                                shape = RoundedCornerShape(8.dp),
                                tonalElevation = 1.dp
                            ) {
                                BasicTextField(
                                    value = fileMemo,
                                    onValueChange = { fileMemo = it },
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 20.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    ),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    decorationBox = { innerTextField ->
                                        Box {
                                            if (fileMemo.isEmpty()) {
                                                Text(
                                                    "Enter memo here",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontFamily = FontFamily.Monospace,
                                                        lineHeight = 20.sp
                                                    ),
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { handleSave() }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onCancel() }
                ) {
                    Text("Cancel")
                }
            }
        )
    } else {
        // Full editor mode - use Dialog with Surface for more control
        Dialog(
            onDismissRequest = { /* Do nothing to prevent auto-dismiss */ },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = false,  // Prevent back button from dismissing
                dismissOnClickOutside = false // Prevent outside clicks from dismissing
            )
        ) {
            Surface(
                modifier = if (!isFullScreen) {
                    Modifier
                        .fillMaxWidth(0.9f) // Use 90% of screen width
                        .fillMaxHeight(0.8f) // Use 80% of screen height
                        .padding(8.dp) // Reduced padding for more content space
                } else {
                    // For full-screen (100 %) we fill the whole window like the old Edge-to-Edge mode
                    Modifier
                        .fillMaxSize()
                        .padding(0.dp) // No padding in full-screen mode
                },
                shape = if (isFullScreen) RoundedCornerShape(0.dp) else RoundedCornerShape(16.dp),
                color = bgColor, // Keep the color on Surface
                tonalElevation = 2.dp
            ) {
                // Photo background layer (if photo is selected) - overlays the color
                if (customBgPhotoUri != null && customBgPhotoUri.isNotEmpty()) {
                    val painter = rememberHighQualityPhotoPainter(
                        photoUri = customBgPhotoUri,
                        onError = { error ->
                            android.util.Log.e("FileEditDialog", "Failed to load photo: ${error.result.throwable.message}")
                        }
                    )
                    
                    val isLoaded = painter.state is coil.compose.AsyncImagePainter.State.Success
                    val imageAlpha = if (isLoaded) customBgPhotoAlpha else 0f
                    val imgSize = painter.intrinsicSize
                    val cropRatio = if (isLoaded && imgSize.width > 0 && imgSize.height > 0 && imgSize.width.isFinite() && imgSize.height.isFinite()) imgSize else null
                    
                    Box(modifier = Modifier.fillMaxSize()) {
                        androidx.compose.foundation.Image(
                            painter = painter,
                            contentDescription = "Opened Note background photo",
                            modifier = Modifier
                                .matchParentSize()
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
                                .applyPhotoAlpha(imageAlpha),
                            contentScale = if (isLoaded) androidx.compose.ui.layout.ContentScale.Fit else androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                }
                
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header with title and actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = if (isFullScreen) 4.dp else 16.dp,
                            end = if (isFullScreen) 4.dp else 16.dp,
                            top = if (isFullScreen) 0.dp else 16.dp,
                            bottom = if (isFullScreen) 4.dp else 16.dp
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (file == null) "Create New File" else "Edit File",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Share button
                        IconButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, fileContent)
                                    putExtra(Intent.EXTRA_SUBJECT, fileName)
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share file")
                                activity.startActivity(shareIntent)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share file"
                            )
                        }
                        // Save to device storage button
                        IconButton(
                            onClick = {
                                FileDialogUtils.exportFile(
                                    FileModel(fileName, fileContent, fileMemo),
                                    activity,
                                    exportRequestCode
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Save to device storage"
                            )
                        }
                        // Full screen toggle button
                        IconButton(onClick = { fullScreen = !fullScreen }) {
                            Icon(
                                imageVector = if (fullScreen) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                                contentDescription = if (fullScreen) "Exit Full Screen" else "Full Screen"
                            )
                        }
                        // Delete button (only for existing files)
                        if (file != null) {
                            IconButton(
                                onClick = { 
                                    Log.d("FileEditDialog", "Delete icon clicked, current showDeleteConfirmation: $showDeleteConfirmation")
                                    // Set the state to show the delete confirmation dialog
                                    showDeleteConfirmation = true
                                    Log.d("FileEditDialog", "After setting, showDeleteConfirmation: $showDeleteConfirmation")
                                }
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                        // Close button
                        IconButton(onClick = { handleExit() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    }
                }
                // Content area with dynamic height
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(contentBgColor)
                        .verticalScroll(editorScrollState)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        // File name field (hide in fullScreen for distraction-free, but keep accessible via a floating bar if needed)
                        if (!fullScreen) {
                            OutlinedTextField(
                                value = fileName,
                                onValueChange = { fileName = it },
                                label = { Text("File Name") },
                                singleLine = true,
                                isError = showError || fileNameError != null,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                textStyle = LocalTextStyle.current.copy(
                                    color = if (showError || fileNameError != null)
                                        MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            )
                            if (showError || fileNameError != null) {
                                Text(
                                    text = if (showError) errorMessage else fileNameError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                                )
                            }
                        }
                        // Content field (scrollable, editable OutlinedTextField)
                        // Drag handle indicator for main content area - exactly like FullScreenEditor
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 0.dp, bottom = 0.dp)
                                .height(4.dp)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(4.dp)
                                    .align(Alignment.Center)
                                    .background(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                        
                        // Content area with styling matching FullScreenEditor
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            color = contentBgColor,
                            tonalElevation = 6.dp,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            ) {
                                BasicTextField(
                                    value = fileContent,
                                    onValueChange = { fileContent = it },
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 24.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    ),
                                    modifier = Modifier.fillMaxSize(),
                                    decorationBox = { innerTextField ->
                                        Box {
                                            if (fileContent.isEmpty()) {
                                                Text(
                                                    "Enter text content here",
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontFamily = FontFamily.Monospace,
                                                        lineHeight = 24.sp
                                                    ),
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                        }
                        
                        // Memo section with toggle - exactly match FullScreenEditor style
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 0.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { showMemo = !showMemo },
                                modifier = Modifier
                                    .padding(0.dp)
                                    .height(28.dp), // Exact same height as FullScreenEditor
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp) // Exact same padding as FullScreenEditor
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
                        
                        // Add proper spacing between content and memo - match FullScreenEditor
                        if (showMemo) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        // Animated memo field - exactly match FullScreenEditor styling
                        AnimatedVisibility(
                            visible = showMemo,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            // Center the memo area in the available space
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.CenterHorizontally)
                                    .padding(horizontal = 16.dp)
                            ) {
                                // Drag handle indicator at the top - minimal padding
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 0.dp, bottom = 0.dp)
                                        .height(4.dp)
                                        .align(Alignment.CenterHorizontally)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(40.dp)
                                            .height(4.dp)
                                            .align(Alignment.Center)
                                            .background(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(2.dp)
                                            )
                                    )
                                }
                                
                                // Memo content area with proper styling - match FullScreenEditor exactly
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth(0.98f) // Match FullScreenEditor's width percentage
                                            .fillMaxHeight(),
                                        shape = RoundedCornerShape(12.dp),
                                        color = effectiveMemoBgColor,
                                        tonalElevation = 6.dp
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(8.dp) // Exact same padding as FullScreenEditor
                                        ) {
                                            BasicTextField(
                                                value = fileMemo,
                                                onValueChange = { fileMemo = it },
                                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                                    color = MaterialTheme.colorScheme.onBackground
                                                ),
                                                modifier = Modifier.fillMaxSize(),
                                                decorationBox = { innerTextField ->
                                                    Box {
                                                        if (fileMemo.isEmpty()) {
                                                            Text(
                                                                "Enter memo here",
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                            )
                                                        }
                                                        innerTextField()
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                // Add proper spacing before bottom bar
                Spacer(modifier = Modifier.height(16.dp))
                
                // Bottom bar with action buttons - match FullScreenEditor styling
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = contentBgColor,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { 
                                Log.d("FileEditDialog", "Cancel button clicked, calling onCancel directly")
                                // Always close the dialog when Cancel is clicked
                                onCancel()
                            }
                        ) {
                            Text("Cancel", style = MaterialTheme.typography.bodyLarge)
                        }
                        Button(
                            onClick = {
                                try {
                                    val finalName = FileDialogUtils.ensureFileExtension(fileName)
                                    FileDialogUtils.validateFileName(finalName)
                                    onSave(FileModel(finalName, fileContent, fileMemo, file?.id))
                                    handleExit()
                                } catch (e: Exception) {
                                    showError = true
                                    errorMessage = e.message ?: "Failed to save file"
                                }
                            }
                        ) {
                            Text("Save", style = MaterialTheme.typography.bodyLarge)
                        }
                        if (file != null) {
                            TextButton(onClick = { showDeleteConfirmation = true }) {
                                Text("Delete")
                            }
                        }
                    }
                }
                }
            }
        }
    }

    // Delete confirmation dialog
    Log.d("FileEditDialog", "Delete confirmation check: showDeleteConfirmation=$showDeleteConfirmation, file=${file?.name}")
    if (showDeleteConfirmation && file != null) {
        Log.d("FileEditDialog", "Rendering delete confirmation dialog for file: ${file.name}")
        // Use a separate Dialog instead of AlertDialog to prevent interference with main dialog
        Dialog(
            onDismissRequest = { 
                Log.d("FileEditDialog", "Delete confirmation dialog dismissed")
                showDeleteConfirmation = false 
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Delete File",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Are you sure you want to delete '${FileDialogUtils.getDisplayFileName(file.name)}'?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                Log.d("FileEditDialog", "Delete canceled")
                                showDeleteConfirmation = false
                            }
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                try {
                                    Log.d("FileEditDialog", "Delete confirmed, calling onDelete for file: ${file.name}")
                                    onDelete.invoke(file)
                                    Log.d("FileEditDialog", "File deletion initiated for: ${file.name}")
                                    showDeleteConfirmation = false
                                    onCancel()
                                } catch (e: Exception) {
                                    Log.e("FileEditDialog", "Error deleting file: ${e.message}")
                                    showError = true
                                    errorMessage = "Failed to delete file: ${e.message}"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
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
    }
}
