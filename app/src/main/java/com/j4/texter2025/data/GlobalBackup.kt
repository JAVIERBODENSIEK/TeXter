package com.j4.texter2025.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.ui.graphics.Color
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Options for selective backup export/import
 */
data class BackupOptions(
    val includeTextFiles: Boolean = true,
    val includeColorPresets: Boolean = true,
    val includeSettings: Boolean = true,
    val includePhotoBackgrounds: Boolean = true,
    val mergeMode: Boolean = true  // true = merge with existing, false = replace existing
)

/**
 * Summary of backup contents for display in dialogs
 */
data class BackupSummary(
    val textFileCount: Int = 0,
    val colorPresetCount: Int = 0,
    val hasSettings: Boolean = false,
    val photoCount: Int = 0,
    val backupVersion: Int = 0,
    val backupDate: String = ""
)

/**
 * Result of a backup restoration operation.
 * Provides detailed information about what was restored successfully.
 */
data class RestoreResult(
    val success: Boolean = true,
    val restoredPhotos: Int = 0,
    val restoredTextFiles: Int = 0,
    val restoredPresets: Int = 0,
    val restoredSettings: Boolean = false,
    val warnings: List<String> = emptyList(),
    val errors: List<String> = emptyList()
) {
    fun getSummaryMessage(): String {
        val parts = mutableListOf<String>()
        if (restoredPresets > 0) parts.add("$restoredPresets preset(s)")
        if (restoredTextFiles > 0) parts.add("$restoredTextFiles file(s)")
        if (restoredPhotos > 0) parts.add("$restoredPhotos photo(s)")
        if (restoredSettings) parts.add("settings")
        
        return if (parts.isEmpty()) {
            if (errors.isNotEmpty()) "Restore failed"
            else "No data to restore"
        } else {
            "Backup restored successfully"
        }
    }
}

/**
 * GlobalBackup handles exporting and importing all app customizations.
 * This allows users to backup their settings and restore them after reinstalling
 * or across different app versions.
 * 
 * Backward Compatibility:
 * - All fields use default values, so older backups without newer fields will work
 * - Newer backups on older app versions will ignore unknown fields
 * - Parsing uses try-catch to handle corrupted or incompatible data gracefully
 */
data class GlobalBackup(
    val version: Int = CURRENT_VERSION,
    val timestamp: Long = System.currentTimeMillis(),
    val appVersion: String = "",
    
    // Color Presets (the main presets created by users)
    val colorPresets: List<ColorPreset> = emptyList(),
    val activePresetName: String? = null,
    
    // Current color settings
    val customBgColor: Long? = null,
    val customBgBlur: Float = 0f,
    val contentAreaColor: Long? = null,
    val contentAreaBlur: Float = 0f,
    val memoAreaColor: Long? = null,
    val memoAreaBlur: Float = 0f,
    val fileListItemColor: Long? = null,
    val fileListBackgroundColor: Long? = null,
    val listAreaColor: Long? = null,
    
    // Custom color squares for each area (with photo data)
    val customBgPresets: String? = null,           // JSON string of SimpleColorPreset list
    val customContentPresets: String? = null,
    val customMemoPresets: String? = null,
    val customFileListPresets: String? = null,
    val customFileListBgPresets: String? = null,
    
    // Photo URIs for current selections
    val customBgPhotoUri: String? = null,
    val customBgPhotoAlpha: Float = 1f,
    val customBgPhotoBlur: Float = 0f,
    val contentAreaPhotoUri: String? = null,
    val contentAreaPhotoAlpha: Float = 1f,
    val contentAreaPhotoBlur: Float = 0f,
    val memoAreaPhotoUri: String? = null,
    val memoAreaPhotoAlpha: Float = 1f,
    val memoAreaPhotoBlur: Float = 0f,
    val fileListItemPhotoUri: String? = null,
    val fileListItemPhotoAlpha: Float = 1f,
    val fileListItemPhotoBlur: Float = 0f,
    val fileListBackgroundPhotoUri: String? = null,
    val fileListBackgroundPhotoAlpha: Float = 1f,
    val fileListBackgroundPhotoBlur: Float = 0f,
    
    // Layout settings
    val lastMemoHeightDp: Float = 25f,
    val memoHeightFromDrag: Boolean = false,
    val lastContentWidth: Float = 0.98f,
    val lastContentHeight: Float = 1.0f,
    val contentSizeFromDrag: Boolean = false,
    
    // Behavior settings
    val memoAreaBehavior: Int = 0,  // Ordinal of MemoAreaBehavior enum
    val unsavedChangesBehavior: Int = 0,  // Ordinal of UnsavedChangesBehavior enum
    
    // Display settings
    val showExtensions: Boolean = false,
    
    // Size presets
    val customSizePresets: String? = null,  // JSON string
    val customMemoSizePresets: String? = null,  // JSON string
    
    // Preset-specific photo data (map of presetName_areaType -> JSON)
    val presetPhotoData: Map<String, String> = emptyMap(),
    
    // Text files data (map of filename -> content)
    val textFiles: Map<String, String> = emptyMap(),
    // Display names mapping (id -> displayName)
    val displayNames: Map<String, String> = emptyMap(),
    // Memos mapping (id -> memo content)
    val memos: Map<String, String> = emptyMap(),
    // Memo visibility mapping (id -> visible)
    val memoVisibility: Map<String, Boolean> = emptyMap(),
    
    // Background photos (map of filename -> base64 encoded image data)
    val backgroundPhotos: Map<String, String> = emptyMap()
) {
    
    fun toJson(): JSONObject {
        val obj = JSONObject()
        
        // Metadata
        obj.put("version", version)
        obj.put("timestamp", timestamp)
        obj.put("appVersion", appVersion)
        obj.put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp)))
        
        // Color presets
        obj.put("colorPresets", JSONArray(ColorPreset.listToJson(colorPresets)))
        activePresetName?.let { obj.put("activePresetName", it) }
        
        // Current colors (stored as Long for precision)
        customBgColor?.let { obj.put("customBgColor", it) }
        obj.put("customBgBlur", customBgBlur.toDouble())
        contentAreaColor?.let { obj.put("contentAreaColor", it) }
        obj.put("contentAreaBlur", contentAreaBlur.toDouble())
        memoAreaColor?.let { obj.put("memoAreaColor", it) }
        obj.put("memoAreaBlur", memoAreaBlur.toDouble())
        fileListItemColor?.let { obj.put("fileListItemColor", it) }
        fileListBackgroundColor?.let { obj.put("fileListBackgroundColor", it) }
        listAreaColor?.let { obj.put("listAreaColor", it) }
        
        // Custom color presets (JSON strings)
        customBgPresets?.let { obj.put("customBgPresets", it) }
        customContentPresets?.let { obj.put("customContentPresets", it) }
        customMemoPresets?.let { obj.put("customMemoPresets", it) }
        customFileListPresets?.let { obj.put("customFileListPresets", it) }
        customFileListBgPresets?.let { obj.put("customFileListBgPresets", it) }
        
        // Photo URIs
        customBgPhotoUri?.let { obj.put("customBgPhotoUri", it) }
        obj.put("customBgPhotoAlpha", customBgPhotoAlpha.toDouble())
        obj.put("customBgPhotoBlur", customBgPhotoBlur.toDouble())
        contentAreaPhotoUri?.let { obj.put("contentAreaPhotoUri", it) }
        obj.put("contentAreaPhotoAlpha", contentAreaPhotoAlpha.toDouble())
        obj.put("contentAreaPhotoBlur", contentAreaPhotoBlur.toDouble())
        memoAreaPhotoUri?.let { obj.put("memoAreaPhotoUri", it) }
        obj.put("memoAreaPhotoAlpha", memoAreaPhotoAlpha.toDouble())
        obj.put("memoAreaPhotoBlur", memoAreaPhotoBlur.toDouble())
        fileListItemPhotoUri?.let { obj.put("fileListItemPhotoUri", it) }
        obj.put("fileListItemPhotoAlpha", fileListItemPhotoAlpha.toDouble())
        obj.put("fileListItemPhotoBlur", fileListItemPhotoBlur.toDouble())
        fileListBackgroundPhotoUri?.let { obj.put("fileListBackgroundPhotoUri", it) }
        obj.put("fileListBackgroundPhotoAlpha", fileListBackgroundPhotoAlpha.toDouble())
        obj.put("fileListBackgroundPhotoBlur", fileListBackgroundPhotoBlur.toDouble())
        
        // Layout settings
        obj.put("lastMemoHeightDp", lastMemoHeightDp.toDouble())
        obj.put("memoHeightFromDrag", memoHeightFromDrag)
        obj.put("lastContentWidth", lastContentWidth.toDouble())
        obj.put("lastContentHeight", lastContentHeight.toDouble())
        obj.put("contentSizeFromDrag", contentSizeFromDrag)
        
        // Behavior settings
        obj.put("memoAreaBehavior", memoAreaBehavior)
        obj.put("unsavedChangesBehavior", unsavedChangesBehavior)
        
        // Display settings
        obj.put("showExtensions", showExtensions)
        
        // Size presets
        customSizePresets?.let { obj.put("customSizePresets", it) }
        customMemoSizePresets?.let { obj.put("customMemoSizePresets", it) }
        
        // Preset photo data
        if (presetPhotoData.isNotEmpty()) {
            val photoDataObj = JSONObject()
            presetPhotoData.forEach { (key, value) ->
                photoDataObj.put(key, value)
            }
            obj.put("presetPhotoData", photoDataObj)
        }
        
        // Text files data
        if (textFiles.isNotEmpty()) {
            val textFilesObj = JSONObject()
            textFiles.forEach { (key, value) ->
                textFilesObj.put(key, value)
            }
            obj.put("textFiles", textFilesObj)
        }
        
        // Display names
        if (displayNames.isNotEmpty()) {
            val displayNamesObj = JSONObject()
            displayNames.forEach { (key, value) ->
                displayNamesObj.put(key, value)
            }
            obj.put("displayNames", displayNamesObj)
        }
        
        // Memos
        if (memos.isNotEmpty()) {
            val memosObj = JSONObject()
            memos.forEach { (key, value) ->
                memosObj.put(key, value)
            }
            obj.put("memos", memosObj)
        }
        
        // Memo visibility
        if (memoVisibility.isNotEmpty()) {
            val memoVisibilityObj = JSONObject()
            memoVisibility.forEach { (key, value) ->
                memoVisibilityObj.put(key, value)
            }
            obj.put("memoVisibility", memoVisibilityObj)
        }
        
        // Background photos (base64 encoded)
        if (backgroundPhotos.isNotEmpty()) {
            val photosObj = JSONObject()
            backgroundPhotos.forEach { (key, value) ->
                photosObj.put(key, value)
            }
            obj.put("backgroundPhotos", photosObj)
        }
        
        return obj
    }
    
    companion object {
        const val CURRENT_VERSION = 1
        const val BACKUP_FILE_EXTENSION = ".texter_backup"
        const val PREFS_NAME = "TeXterPrefs"
        
        /**
         * Get a summary of backup contents for display in dialogs
         */
        fun getSummary(backup: GlobalBackup): BackupSummary {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            return BackupSummary(
                textFileCount = backup.textFiles.size,
                colorPresetCount = backup.colorPresets.size,
                hasSettings = true, // Settings are always present
                photoCount = backup.backgroundPhotos.size,
                backupVersion = backup.version,
                backupDate = dateFormat.format(Date(backup.timestamp))
            )
        }
        
        /**
         * Get a summary of current app data for export dialog
         */
        fun getCurrentDataSummary(context: Context): BackupSummary {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            // Count text files
            val appFilesDir = File(context.getExternalFilesDir(null), "TeXter2025_Files")
            val textFileCount = if (appFilesDir.exists()) {
                appFilesDir.listFiles()?.count { it.isFile && it.extension == "txt" && !it.name.startsWith("display_names") && !it.name.startsWith("memos") && !it.name.startsWith("memo_visibility") } ?: 0
            } else 0
            
            // Count color presets
            val presetsJson = prefs.getString("color_presets", "[]") ?: "[]"
            val presetCount = try {
                ColorPreset.listFromJson(presetsJson).size
            } catch (e: Exception) { 0 }
            
            // Count photos
            val photoCount = PhotoStorage.getAllPhotos(context).size
            
            return BackupSummary(
                textFileCount = textFileCount,
                colorPresetCount = presetCount,
                hasSettings = true,
                photoCount = photoCount,
                backupVersion = CURRENT_VERSION,
                backupDate = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())
            )
        }
        
        fun fromJson(json: String): GlobalBackup {
            val obj = JSONObject(json)
            
            // Parse color presets
            val colorPresetsJson = if (obj.has("colorPresets")) {
                val presetsValue = obj.get("colorPresets")
                when (presetsValue) {
                    is JSONArray -> presetsValue.toString()
                    is String -> presetsValue
                    else -> "[]"
                }
            } else "[]"
            
            val colorPresets = try {
                ColorPreset.listFromJson(colorPresetsJson)
            } catch (e: Exception) {
                emptyList()
            }
            
            // Parse preset photo data (with error handling for older versions)
            val presetPhotoData = mutableMapOf<String, String>()
            try {
                if (obj.has("presetPhotoData")) {
                    val photoDataObj = obj.getJSONObject("presetPhotoData")
                    photoDataObj.keys().forEach { key ->
                        presetPhotoData[key] = photoDataObj.getString(key)
                    }
                }
            } catch (e: Exception) {
                Log.w("GlobalBackup", "Could not parse presetPhotoData, skipping: ${e.message}")
            }
            
            // Parse text files (with error handling)
            val textFiles = mutableMapOf<String, String>()
            try {
                if (obj.has("textFiles")) {
                    val textFilesObj = obj.getJSONObject("textFiles")
                    textFilesObj.keys().forEach { key ->
                        textFiles[key] = textFilesObj.getString(key)
                    }
                }
            } catch (e: Exception) {
                Log.w("GlobalBackup", "Could not parse textFiles, skipping: ${e.message}")
            }
            
            // Parse display names (with error handling)
            val displayNames = mutableMapOf<String, String>()
            try {
                if (obj.has("displayNames")) {
                    val displayNamesObj = obj.getJSONObject("displayNames")
                    displayNamesObj.keys().forEach { key ->
                        displayNames[key] = displayNamesObj.getString(key)
                    }
                }
            } catch (e: Exception) {
                Log.w("GlobalBackup", "Could not parse displayNames, skipping: ${e.message}")
            }
            
            // Parse memos (with error handling)
            val memos = mutableMapOf<String, String>()
            try {
                if (obj.has("memos")) {
                    val memosObj = obj.getJSONObject("memos")
                    memosObj.keys().forEach { key ->
                        memos[key] = memosObj.getString(key)
                    }
                }
            } catch (e: Exception) {
                Log.w("GlobalBackup", "Could not parse memos, skipping: ${e.message}")
            }
            
            // Parse memo visibility (with error handling)
            val memoVisibility = mutableMapOf<String, Boolean>()
            try {
                if (obj.has("memoVisibility")) {
                    val memoVisibilityObj = obj.getJSONObject("memoVisibility")
                    memoVisibilityObj.keys().forEach { key ->
                        memoVisibility[key] = memoVisibilityObj.getBoolean(key)
                    }
                }
            } catch (e: Exception) {
                Log.w("GlobalBackup", "Could not parse memoVisibility, skipping: ${e.message}")
            }
            
            // Parse background photos (with error handling - may not exist in older backups)
            val backgroundPhotos = mutableMapOf<String, String>()
            try {
                if (obj.has("backgroundPhotos")) {
                    val photosObj = obj.getJSONObject("backgroundPhotos")
                    photosObj.keys().forEach { key ->
                        backgroundPhotos[key] = photosObj.getString(key)
                    }
                }
            } catch (e: Exception) {
                Log.w("GlobalBackup", "Could not parse backgroundPhotos, skipping: ${e.message}")
            }
            
            return GlobalBackup(
                version = obj.optInt("version", 1),
                timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                appVersion = obj.optString("appVersion", ""),
                
                colorPresets = colorPresets,
                activePresetName = if (obj.has("activePresetName")) obj.getString("activePresetName") else null,
                
                customBgColor = if (obj.has("customBgColor")) obj.getLong("customBgColor") else null,
                customBgBlur = obj.optDouble("customBgBlur", 0.0).toFloat(),
                contentAreaColor = if (obj.has("contentAreaColor")) obj.getLong("contentAreaColor") else null,
                contentAreaBlur = obj.optDouble("contentAreaBlur", 0.0).toFloat(),
                memoAreaColor = if (obj.has("memoAreaColor")) obj.getLong("memoAreaColor") else null,
                memoAreaBlur = obj.optDouble("memoAreaBlur", 0.0).toFloat(),
                fileListItemColor = if (obj.has("fileListItemColor")) obj.getLong("fileListItemColor") else null,
                fileListBackgroundColor = if (obj.has("fileListBackgroundColor")) obj.getLong("fileListBackgroundColor") else null,
                listAreaColor = if (obj.has("listAreaColor")) obj.getLong("listAreaColor") else null,
                
                customBgPresets = if (obj.has("customBgPresets")) obj.getString("customBgPresets") else null,
                customContentPresets = if (obj.has("customContentPresets")) obj.getString("customContentPresets") else null,
                customMemoPresets = if (obj.has("customMemoPresets")) obj.getString("customMemoPresets") else null,
                customFileListPresets = if (obj.has("customFileListPresets")) obj.getString("customFileListPresets") else null,
                customFileListBgPresets = if (obj.has("customFileListBgPresets")) obj.getString("customFileListBgPresets") else null,
                
                customBgPhotoUri = if (obj.has("customBgPhotoUri")) obj.getString("customBgPhotoUri") else null,
                customBgPhotoAlpha = obj.optDouble("customBgPhotoAlpha", 1.0).toFloat(),
                customBgPhotoBlur = obj.optDouble("customBgPhotoBlur", 0.0).toFloat(),
                contentAreaPhotoUri = if (obj.has("contentAreaPhotoUri")) obj.getString("contentAreaPhotoUri") else null,
                contentAreaPhotoAlpha = obj.optDouble("contentAreaPhotoAlpha", 1.0).toFloat(),
                contentAreaPhotoBlur = obj.optDouble("contentAreaPhotoBlur", 0.0).toFloat(),
                memoAreaPhotoUri = if (obj.has("memoAreaPhotoUri")) obj.getString("memoAreaPhotoUri") else null,
                memoAreaPhotoAlpha = obj.optDouble("memoAreaPhotoAlpha", 1.0).toFloat(),
                memoAreaPhotoBlur = obj.optDouble("memoAreaPhotoBlur", 0.0).toFloat(),
                fileListItemPhotoUri = if (obj.has("fileListItemPhotoUri")) obj.getString("fileListItemPhotoUri") else null,
                fileListItemPhotoAlpha = obj.optDouble("fileListItemPhotoAlpha", 1.0).toFloat(),
                fileListItemPhotoBlur = obj.optDouble("fileListItemPhotoBlur", 0.0).toFloat(),
                fileListBackgroundPhotoUri = if (obj.has("fileListBackgroundPhotoUri")) obj.getString("fileListBackgroundPhotoUri") else null,
                fileListBackgroundPhotoAlpha = obj.optDouble("fileListBackgroundPhotoAlpha", 1.0).toFloat(),
                fileListBackgroundPhotoBlur = obj.optDouble("fileListBackgroundPhotoBlur", 0.0).toFloat(),
                
                lastMemoHeightDp = obj.optDouble("lastMemoHeightDp", 25.0).toFloat(),
                memoHeightFromDrag = obj.optBoolean("memoHeightFromDrag", false),
                lastContentWidth = obj.optDouble("lastContentWidth", 0.98).toFloat(),
                lastContentHeight = obj.optDouble("lastContentHeight", 1.0).toFloat(),
                contentSizeFromDrag = obj.optBoolean("contentSizeFromDrag", false),
                
                memoAreaBehavior = obj.optInt("memoAreaBehavior", 0),
                unsavedChangesBehavior = obj.optInt("unsavedChangesBehavior", 0),
                
                showExtensions = obj.optBoolean("showExtensions", false),
                
                customSizePresets = if (obj.has("customSizePresets")) obj.getString("customSizePresets") else null,
                customMemoSizePresets = if (obj.has("customMemoSizePresets")) obj.getString("customMemoSizePresets") else null,
                
                presetPhotoData = presetPhotoData,
                
                textFiles = textFiles,
                displayNames = displayNames,
                memos = memos,
                memoVisibility = memoVisibility,
                backgroundPhotos = backgroundPhotos
            )
        }
        
        /**
         * Create a backup from current SharedPreferences
         */
        fun createFromPreferences(context: Context, appVersion: String): GlobalBackup {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            // Load color presets
            val colorPresetsJson = prefs.getString("color_presets", null)
            val colorPresets = if (colorPresetsJson != null) {
                try {
                    ColorPreset.listFromJson(colorPresetsJson)
                } catch (e: Exception) {
                    emptyList()
                }
            } else emptyList()
            
            // Load preset photo data (all keys starting with "preset_" and ending with "_photos")
            val presetPhotoData = mutableMapOf<String, String>()
            prefs.all.forEach { (key, value) ->
                if (key.startsWith("preset_") && key.endsWith("_photos") && value is String) {
                    presetPhotoData[key] = value
                }
            }
            
            // Load text files from app's external files directory
            val textFiles = mutableMapOf<String, String>()
            val displayNames = mutableMapOf<String, String>()
            val memos = mutableMapOf<String, String>()
            val memoVisibility = mutableMapOf<String, Boolean>()
            
            try {
                val appFilesDir = File(context.getExternalFilesDir(null), "TeXter2025_Files")
                if (appFilesDir.exists()) {
                    // Load text files
                    appFilesDir.listFiles()?.filter { 
                        it.isFile && it.name.endsWith(".txt") && 
                        !listOf("memo_visibility.txt", "display_names.txt", "memos.txt").contains(it.name) 
                    }?.forEach { file ->
                        textFiles[file.name] = file.readText()
                    }
                    
                    // Load display names
                    val displayNameFile = File(appFilesDir, "display_names.txt")
                    if (displayNameFile.exists()) {
                        displayNameFile.readLines().forEach { line ->
                            val parts = line.split("=", limit = 2)
                            if (parts.size == 2) {
                                displayNames[parts[0]] = parts[1]
                            }
                        }
                    }
                    
                    // Load memos
                    val memoFile = File(appFilesDir, "memos.txt")
                    if (memoFile.exists()) {
                        memoFile.readLines().forEach { line ->
                            val parts = line.split("=", limit = 2)
                            if (parts.size == 2) {
                                memos[parts[0]] = parts[1]
                            }
                        }
                    }
                    
                    // Load memo visibility
                    val memoVisibilityFile = File(appFilesDir, "memo_visibility.txt")
                    if (memoVisibilityFile.exists()) {
                        memoVisibilityFile.readLines().forEach { line ->
                            val parts = line.split("=", limit = 2)
                            if (parts.size == 2) {
                                memoVisibility[parts[0]] = parts[1].toBoolean()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("GlobalBackup", "Error loading text files for backup", e)
            }
            
            // Load background photos from internal storage
            val backgroundPhotos = mutableMapOf<String, String>()
            try {
                PhotoStorage.getAllPhotos(context).forEach { photoFile ->
                    val base64 = PhotoStorage.photoToBase64(photoFile.absolutePath)
                    if (base64 != null) {
                        backgroundPhotos[photoFile.name] = base64
                    }
                }
                Log.d("GlobalBackup", "Loaded ${backgroundPhotos.size} background photos for backup")
            } catch (e: Exception) {
                Log.e("GlobalBackup", "Error loading background photos for backup", e)
            }
            
            return GlobalBackup(
                version = CURRENT_VERSION,
                timestamp = System.currentTimeMillis(),
                appVersion = appVersion,
                
                colorPresets = colorPresets,
                activePresetName = prefs.getString("active_preset_name", null),
                
                customBgColor = safeGetLong(prefs, "custom_bg_color"),
                customBgBlur = prefs.getFloat("custom_bg_blur", 0f),
                contentAreaColor = safeGetLong(prefs, "content_area_color"),
                contentAreaBlur = prefs.getFloat("content_area_blur", 0f),
                memoAreaColor = safeGetLong(prefs, "memo_area_color"),
                memoAreaBlur = prefs.getFloat("memo_area_blur", 0f),
                fileListItemColor = safeGetLong(prefs, "file_list_item_color"),
                fileListBackgroundColor = safeGetLong(prefs, "file_list_background_color"),
                listAreaColor = safeGetLong(prefs, "list_area_color"),
                
                customBgPresets = prefs.getString("custom_colors_with_photos_bg", null).also { 
                    android.util.Log.d("GlobalBackup", "BACKUP CREATION - customBgPresets: ${it?.take(100) ?: "NULL"}")
                },
                customContentPresets = prefs.getString("custom_colors_with_photos_content", null).also { 
                    android.util.Log.d("GlobalBackup", "BACKUP CREATION - customContentPresets: ${it?.take(100) ?: "NULL"}")
                },
                customMemoPresets = prefs.getString("custom_colors_with_photos_memo", null).also { 
                    android.util.Log.d("GlobalBackup", "BACKUP CREATION - customMemoPresets: ${it?.take(100) ?: "NULL"}")
                },
                customFileListPresets = prefs.getString("custom_colors_with_photos_filelist", null).also { 
                    android.util.Log.d("GlobalBackup", "BACKUP CREATION - customFileListPresets: ${it?.take(100) ?: "NULL"}")
                },
                customFileListBgPresets = prefs.getString("custom_colors_with_photos_filelistbg", null).also { 
                    android.util.Log.d("GlobalBackup", "BACKUP CREATION - customFileListBgPresets: ${it?.take(100) ?: "NULL"}")
                },
                
                customBgPhotoUri = prefs.getString("custom_bg_photo_uri", null),
                customBgPhotoAlpha = prefs.getFloat("custom_bg_photo_alpha", 1f),
                customBgPhotoBlur = prefs.getFloat("custom_bg_photo_blur", 0f),
                contentAreaPhotoUri = prefs.getString("content_area_photo_uri", null),
                contentAreaPhotoAlpha = prefs.getFloat("content_area_photo_alpha", 1f),
                contentAreaPhotoBlur = prefs.getFloat("content_area_photo_blur", 0f),
                memoAreaPhotoUri = prefs.getString("memo_area_photo_uri", null),
                memoAreaPhotoAlpha = prefs.getFloat("memo_area_photo_alpha", 1f),
                memoAreaPhotoBlur = prefs.getFloat("memo_area_photo_blur", 0f),
                fileListItemPhotoUri = prefs.getString("file_list_item_photo_uri", null),
                fileListItemPhotoAlpha = prefs.getFloat("file_list_item_photo_alpha", 1f),
                fileListItemPhotoBlur = prefs.getFloat("file_list_item_photo_blur", 0f),
                fileListBackgroundPhotoUri = prefs.getString("file_list_background_photo_uri", null),
                fileListBackgroundPhotoAlpha = prefs.getFloat("file_list_background_photo_alpha", 1f),
                fileListBackgroundPhotoBlur = prefs.getFloat("file_list_background_photo_blur", 0f),
                
                lastMemoHeightDp = prefs.getFloat("last_memo_height_dp", 25f),
                memoHeightFromDrag = prefs.getBoolean("memo_height_from_drag", false),
                lastContentWidth = prefs.getFloat("last_content_width", 0.98f),
                lastContentHeight = prefs.getFloat("last_content_height", 1.0f),
                contentSizeFromDrag = prefs.getBoolean("content_size_from_drag", false),
                
                memoAreaBehavior = prefs.getInt("memo_area_behavior", 0),
                unsavedChangesBehavior = prefs.getInt("unsaved_changes_behavior", 0),
                
                showExtensions = prefs.getBoolean("show_extensions", false),
                
                customSizePresets = prefs.getString("custom_size_presets", null),
                customMemoSizePresets = prefs.getString("custom_memo_size_presets", null),
                
                presetPhotoData = presetPhotoData,
                
                textFiles = textFiles,
                displayNames = displayNames,
                memos = memos,
                memoVisibility = memoVisibility,
                backgroundPhotos = backgroundPhotos
            )
        }
        
        /**
         * Restore backup to SharedPreferences, text files, and background photos.
         * Returns a RestoreResult with detailed information about what was restored.
         * 
         * Backward Compatibility:
         * - Each section is restored independently with try-catch
         * - If a section fails, others will still be restored
         * - Unknown fields in backup are ignored
         * - Missing fields use defaults
         */
        fun restoreToPreferences(context: Context, backup: GlobalBackup): RestoreResult {
            return restoreToPreferences(context, backup, BackupOptions())
        }
        
        /**
         * Restore backup with selective options.
         */
        fun restoreToPreferences(context: Context, backup: GlobalBackup, options: BackupOptions): RestoreResult {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()
            val warnings = mutableListOf<String>()
            val errors = mutableListOf<String>()
            var restoredSettings = false
            
            // Helper function to merge JSON arrays - defined at function level for accessibility
            fun mergeJsonArrays(existingJson: String?, backupJson: String?): String? {
                if (backupJson == null) return existingJson
                if (!options.mergeMode || existingJson == null) return backupJson
                
                return try {
                    val existingArray = org.json.JSONArray(existingJson)
                    val backupArray = org.json.JSONArray(backupJson)
                    
                    val allSquares = mutableListOf<org.json.JSONObject>()
                    for (i in 0 until existingArray.length()) {
                        allSquares.add(existingArray.getJSONObject(i))
                    }
                    
                    for (i in 0 until backupArray.length()) {
                        val backupObj = backupArray.getJSONObject(i)
                        val backupColor = backupObj.optLong("color", 0L)
                        val backupPhotoUri = backupObj.optString("photoUri", "")
                        
                        val isDuplicate = allSquares.any { existing ->
                            val existingColor = existing.optLong("color", 0L)
                            val existingPhotoUri = existing.optString("photoUri", "")
                            existingColor == backupColor && existingPhotoUri == backupPhotoUri
                        }
                        
                        if (!isDuplicate) {
                            var uniqueName = backupObj.optString("name", "Custom")
                            var counter = 1
                            while (allSquares.any { it.optString("name", "") == uniqueName }) {
                                uniqueName = "${backupObj.optString("name", "Custom")} ($counter)"
                                counter++
                            }
                            val newObj = org.json.JSONObject(backupObj.toString())
                            newObj.put("name", uniqueName)
                            allSquares.add(newObj)
                        }
                    }
                    
                    val mergedArray = org.json.JSONArray()
                    allSquares.forEach { mergedArray.put(it) }
                    mergedArray.toString()
                } catch (e: Exception) {
                    android.util.Log.e("GlobalBackup", "Error merging JSON arrays", e)
                    backupJson
                }
            }
            
            // Color presets (only if includeColorPresets)
            if (options.includeColorPresets) {
                try {
                    if (backup.colorPresets.isNotEmpty()) {
                        val finalPresets = if (options.mergeMode) {
                            // Merge mode: Combine existing presets with backup presets
                            val existingJson = prefs.getString("color_presets", null)
                            val existingPresets = if (existingJson != null) {
                                try {
                                    ColorPreset.listFromJson(existingJson)
                                } catch (e: Exception) {
                                    emptyList()
                                }
                            } else {
                                emptyList()
                            }
                            
                            // Create a map of existing presets by name for easy lookup
                            val existingMap = existingPresets.associateBy { it.name }.toMutableMap()
                            
                            // Add/update with backup presets (backup presets override existing ones with same name)
                            backup.colorPresets.forEach { backupPreset ->
                                existingMap[backupPreset.name] = backupPreset
                            }
                            
                            existingMap.values.toList()
                        } else {
                            // Replace mode: Use only backup presets
                            backup.colorPresets
                        }
                        
                        editor.putString("color_presets", ColorPreset.listToJson(finalPresets))
                    }
                    // Note: activePresetName will be set after migration if needed
                    
                    // MIGRATION: Collect colors from ALL sections and create comprehensive ColorPreset objects
                    // ONLY run migration if backup doesn't have colorPresets (old backup format)
                    if (backup.colorPresets.isEmpty()) {
                        try {
                        // Step 2: Parse colors from all sections by preset name
                        val presetColorMap = mutableMapOf<String, MutableMap<String, Any?>>()
                        
                        // Helper to parse colors from a JSON array
                        fun parseColorsFromJson(jsonString: String, section: String) {
                            val jsonArray = org.json.JSONArray(jsonString)
                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)
                                val name = obj.optString("name", "Preset ${i + 1}")
                                val colorValue = obj.optLong("color", 0L)
                                val photoUri = obj.optString("photoUri", "")
                                
                                // Initialize preset entry if not exists
                                if (!presetColorMap.containsKey(name)) {
                                    presetColorMap[name] = mutableMapOf()
                                }
                                
                                val color = if (colorValue != 0L) androidx.compose.ui.graphics.Color(colorValue.toULong()) else null
                                presetColorMap[name]!!["${section}_color"] = color
                                presetColorMap[name]!!["${section}_photoUri"] = if (photoUri.isNotEmpty()) photoUri else null
                            }
                        }
                        
                        // Parse all sections
                        backup.customBgPresets?.let { parseColorsFromJson(it, "bg") }
                        backup.customContentPresets?.let { parseColorsFromJson(it, "content") }
                        backup.customMemoPresets?.let { parseColorsFromJson(it, "memo") }
                        
                        // Step 3: Create ColorPreset objects with all colors
                        val migratedPresets = mutableListOf<ColorPreset>()
                        presetColorMap.forEach { (name, colorData) ->
                            val bgColor = colorData["bg_color"] as? androidx.compose.ui.graphics.Color
                            val contentColor = colorData["content_color"] as? androidx.compose.ui.graphics.Color
                            val memoColor = colorData["memo_color"] as? androidx.compose.ui.graphics.Color
                            val bgPhotoUri = colorData["bg_photoUri"] as? String
                            val contentPhotoUri = colorData["content_photoUri"] as? String
                            val memoPhotoUri = colorData["memo_photoUri"] as? String
                            
                            val preset = ColorPreset(
                                name = name,
                                customBgColor = bgColor,
                                customBgBlur = 0f,
                                customBgColors = if (bgColor != null) listOf(com.j4.texter2025.ui.components.SimpleColorPreset(name = "Custom 1", color = bgColor)) else emptyList(),
                                contentAreaColor = contentColor,
                                contentAreaBlur = 0f,
                                customContentAreaColors = if (contentColor != null) listOf(com.j4.texter2025.ui.components.SimpleColorPreset(name = "Custom 1", color = contentColor)) else emptyList(),
                                memoAreaColor = memoColor,
                                memoAreaBlur = 0f,
                                customMemoAreaColors = if (memoColor != null) listOf(com.j4.texter2025.ui.components.SimpleColorPreset(name = "Custom 1", color = memoColor)) else emptyList(),
                                fileListItemColor = null,
                                fileListItemBlur = 0f,
                                customFileListItemColors = emptyList(),
                                fileListBackgroundColor = null,
                                fileListBackgroundBlur = 0f,
                                customFileListBackgroundColors = emptyList(),
                                customBgPhotoUri = bgPhotoUri,
                                contentAreaPhotoUri = contentPhotoUri,
                                memoAreaPhotoUri = memoPhotoUri
                            )
                            migratedPresets.add(preset)
                            android.util.Log.d("GlobalBackup", "Migrated preset: $name, BG=$bgColor, Content=$contentColor, Memo=$memoColor")
                        }
                        
                        // Step 4: Merge with existing presets
                        val existingPresets = prefs.getString("color_presets", null)?.let { 
                            ColorPreset.listFromJson(it).toMutableList() 
                        } ?: mutableListOf()
                        
                        migratedPresets.forEach { newPreset ->
                            // Check if duplicate based on all colors
                            val duplicateExists = existingPresets.any { existing ->
                                existing.customBgColor == newPreset.customBgColor &&
                                existing.contentAreaColor == newPreset.contentAreaColor &&
                                existing.memoAreaColor == newPreset.memoAreaColor &&
                                existing.customBgPhotoUri == newPreset.customBgPhotoUri
                            }
                            
                            if (!duplicateExists) {
                                // Make name unique if needed
                                var uniqueName = newPreset.name
                                var counter = 1
                                while (existingPresets.any { it.name == uniqueName }) {
                                    uniqueName = "${newPreset.name} (${counter})"
                                    counter++
                                }
                                
                                val finalPreset = if (uniqueName != newPreset.name) {
                                    newPreset.copy(name = uniqueName)
                                } else {
                                    newPreset
                                }
                                
                                existingPresets.add(finalPreset)
                                android.util.Log.d("GlobalBackup", "Added preset with unique name: $uniqueName")
                            } else {
                                android.util.Log.d("GlobalBackup", "Skipped duplicate preset: ${newPreset.name}")
                            }
                        }
                        
                        val presetsJson = ColorPreset.listToJson(existingPresets)
                        editor.putString("color_presets", presetsJson)
                        android.util.Log.d("GlobalBackup", "Migrated ${migratedPresets.size} old presets to new format")
                        
                        // Set the last MIGRATED preset as active
                        if (migratedPresets.isNotEmpty()) {
                            val lastMigratedPreset = migratedPresets.lastOrNull()
                            if (lastMigratedPreset != null) {
                                val finalPreset = existingPresets.lastOrNull { 
                                    it.customBgColor == lastMigratedPreset.customBgColor &&
                                    it.contentAreaColor == lastMigratedPreset.contentAreaColor &&
                                    it.memoAreaColor == lastMigratedPreset.memoAreaColor
                                }
                                if (finalPreset != null) {
                                    editor.putString("active_preset_name", finalPreset.name)
                                    android.util.Log.d("GlobalBackup", "Set active preset to last migrated: ${finalPreset.name}")
                                }
                            }
                        }
                        } catch (e: Exception) {
                            android.util.Log.e("GlobalBackup", "Error migrating old presets", e)
                        }
                    } else {
                        android.util.Log.d("GlobalBackup", "Skipping migration - backup already has colorPresets")
                    }
                    
                    backup.customFileListPresets?.let { backupJson ->
                        android.util.Log.d("GlobalBackup", "Restoring customFileListPresets: ${backupJson.take(100)}...")
                        val existingJson = prefs.getString("custom_colors_with_photos_filelist", null)
                        val finalJson = mergeJsonArrays(existingJson, backupJson)
                        finalJson?.let { editor.putString("custom_colors_with_photos_filelist", it) }
                    } ?: android.util.Log.d("GlobalBackup", "No customFileListPresets in backup")
                    
                    backup.customFileListBgPresets?.let { backupJson ->
                        android.util.Log.d("GlobalBackup", "Restoring customFileListBgPresets: ${backupJson.take(100)}...")
                        val existingJson = prefs.getString("custom_colors_with_photos_filelistbg", null)
                        val finalJson = mergeJsonArrays(existingJson, backupJson)
                        finalJson?.let { editor.putString("custom_colors_with_photos_filelistbg", it) }
                    } ?: android.util.Log.d("GlobalBackup", "No customFileListBgPresets in backup")
                    
                    // Current colors
                    backup.customBgColor?.let { editor.putLong("custom_bg_color", it) }
                    editor.putFloat("custom_bg_blur", backup.customBgBlur)
                    backup.contentAreaColor?.let { editor.putLong("content_area_color", it) }
                    editor.putFloat("content_area_blur", backup.contentAreaBlur)
                    backup.memoAreaColor?.let { editor.putLong("memo_area_color", it) }
                    editor.putFloat("memo_area_blur", backup.memoAreaBlur)
                    backup.fileListItemColor?.let { editor.putLong("file_list_item_color", it) }
                    backup.fileListBackgroundColor?.let { editor.putLong("file_list_background_color", it) }
                    backup.listAreaColor?.let { editor.putLong("list_area_color", it) }
                    
                    // Preset photo data
                    backup.presetPhotoData.forEach { (key, value) ->
                        editor.putString(key, value)
                    }
                } catch (e: Exception) {
                    Log.e("GlobalBackup", "Error restoring color presets", e)
                    warnings.add("Could not restore some color presets")
                }
            }
            
            // Photo URIs (only if includePhotoBackgrounds)
            if (options.includePhotoBackgrounds) {
                try {
                    backup.customBgPhotoUri?.let { editor.putString("custom_bg_photo_uri", it) }
                    editor.putFloat("custom_bg_photo_alpha", backup.customBgPhotoAlpha)
                    editor.putFloat("custom_bg_photo_blur", backup.customBgPhotoBlur)
                    backup.contentAreaPhotoUri?.let { editor.putString("content_area_photo_uri", it) }
                    editor.putFloat("content_area_photo_alpha", backup.contentAreaPhotoAlpha)
                    editor.putFloat("content_area_photo_blur", backup.contentAreaPhotoBlur)
                    backup.memoAreaPhotoUri?.let { editor.putString("memo_area_photo_uri", it) }
                    editor.putFloat("memo_area_photo_alpha", backup.memoAreaPhotoAlpha)
                    editor.putFloat("memo_area_photo_blur", backup.memoAreaPhotoBlur)
                    backup.fileListItemPhotoUri?.let { editor.putString("file_list_item_photo_uri", it) }
                    editor.putFloat("file_list_item_photo_alpha", backup.fileListItemPhotoAlpha)
                    editor.putFloat("file_list_item_photo_blur", backup.fileListItemPhotoBlur)
                    backup.fileListBackgroundPhotoUri?.let { editor.putString("file_list_background_photo_uri", it) }
                    editor.putFloat("file_list_background_photo_alpha", backup.fileListBackgroundPhotoAlpha)
                    editor.putFloat("file_list_background_photo_blur", backup.fileListBackgroundPhotoBlur)
                } catch (e: Exception) {
                    Log.e("GlobalBackup", "Error restoring photo URIs", e)
                    warnings.add("Could not restore photo background settings")
                }
            }
            
            // Settings (only if includeSettings)
            if (options.includeSettings) {
                try {
                    // Layout settings
                    editor.putFloat("last_memo_height_dp", backup.lastMemoHeightDp)
                    editor.putBoolean("memo_height_from_drag", backup.memoHeightFromDrag)
                    editor.putFloat("last_content_width", backup.lastContentWidth)
                    editor.putFloat("last_content_height", backup.lastContentHeight)
                    editor.putBoolean("content_size_from_drag", backup.contentSizeFromDrag)
                    
                    // Behavior settings
                    editor.putInt("memo_area_behavior", backup.memoAreaBehavior)
                    editor.putInt("unsaved_changes_behavior", backup.unsavedChangesBehavior)
                    
                    // Display settings
                    editor.putBoolean("show_extensions", backup.showExtensions)
                    
                    // Size presets
                    backup.customSizePresets?.let { editor.putString("custom_size_presets", it) }
                    backup.customMemoSizePresets?.let { editor.putString("custom_memo_size_presets", it) }
                    
                    restoredSettings = true
                } catch (e: Exception) {
                    Log.e("GlobalBackup", "Error restoring settings", e)
                    warnings.add("Could not restore some settings")
                }
            }
            
            editor.apply()
            
            // Restore all color squares to their global pools (for ALL backups)
            // MUST happen AFTER editor.apply() so the data is committed to SharedPreferences
            backup.customBgPresets?.let { backupJson ->
                android.util.Log.d("GlobalBackup", "Restoring customBgPresets: ${backupJson.take(100)}...")
                val existingJson = prefs.getString("custom_colors_with_photos_bg", null)
                val finalJson = mergeJsonArrays(existingJson, backupJson)
                finalJson?.let { 
                    prefs.edit().putString("custom_colors_with_photos_bg", it).apply()
                    android.util.Log.d("GlobalBackup", "Saved customBgPresets to SharedPreferences")
                }
            } ?: android.util.Log.d("GlobalBackup", "No customBgPresets in backup")
            
            backup.customContentPresets?.let { backupJson ->
                android.util.Log.d("GlobalBackup", "Restoring customContentPresets: ${backupJson.take(100)}...")
                val existingJson = prefs.getString("custom_colors_with_photos_content", null)
                val finalJson = mergeJsonArrays(existingJson, backupJson)
                finalJson?.let { 
                    prefs.edit().putString("custom_colors_with_photos_content", it).apply()
                    android.util.Log.d("GlobalBackup", "Saved customContentPresets to SharedPreferences")
                }
            } ?: android.util.Log.d("GlobalBackup", "No customContentPresets in backup")
            
            backup.customMemoPresets?.let { backupJson ->
                android.util.Log.d("GlobalBackup", "Restoring customMemoPresets: ${backupJson.take(100)}...")
                val existingJson = prefs.getString("custom_colors_with_photos_memo", null)
                val finalJson = mergeJsonArrays(existingJson, backupJson)
                finalJson?.let { 
                    prefs.edit().putString("custom_colors_with_photos_memo", it).apply()
                    android.util.Log.d("GlobalBackup", "Saved customMemoPresets to SharedPreferences")
                }
            } ?: android.util.Log.d("GlobalBackup", "No customMemoPresets in backup")
            
            backup.customFileListPresets?.let { backupJson ->
                android.util.Log.d("GlobalBackup", "Restoring customFileListPresets: ${backupJson.take(100)}...")
                val existingJson = prefs.getString("custom_colors_with_photos_filelist", null)
                val finalJson = mergeJsonArrays(existingJson, backupJson)
                finalJson?.let { 
                    prefs.edit().putString("custom_colors_with_photos_filelist", it).apply()
                    android.util.Log.d("GlobalBackup", "Saved customFileListPresets to SharedPreferences")
                }
            } ?: android.util.Log.d("GlobalBackup", "No customFileListPresets in backup")
            
            backup.customFileListBgPresets?.let { backupJson ->
                android.util.Log.d("GlobalBackup", "Restoring customFileListBgPresets: ${backupJson.take(100)}...")
                val existingJson = prefs.getString("custom_colors_with_photos_filelistbg", null)
                val finalJson = mergeJsonArrays(existingJson, backupJson)
                finalJson?.let { 
                    prefs.edit().putString("custom_colors_with_photos_filelistbg", it).apply()
                    android.util.Log.d("GlobalBackup", "Saved customFileListBgPresets to SharedPreferences")
                }
            } ?: android.util.Log.d("GlobalBackup", "No customFileListBgPresets in backup")
            
            // Restore background photos from backup (only if includePhotoBackgrounds)
            var restoredPhotoCount = 0
            if (options.includePhotoBackgrounds) {
                try {
                    backup.backgroundPhotos.forEach { (filename, base64) ->
                        val restoredPath = PhotoStorage.photoFromBase64(context, base64, filename)
                        if (restoredPath != null) {
                            restoredPhotoCount++
                        }
                    }
                    Log.d("GlobalBackup", "Restored $restoredPhotoCount background photos")
                } catch (e: Exception) {
                    Log.e("GlobalBackup", "Error restoring background photos", e)
                    warnings.add("Could not restore some photo backgrounds")
                }
            }
            
            // Restore text files (only if includeTextFiles)
            var restoredTextFileCount = 0
            if (options.includeTextFiles) {
                try {
                    val appFilesDir = File(context.getExternalFilesDir(null), "TeXter2025_Files")
                    if (!appFilesDir.exists()) {
                        appFilesDir.mkdirs()
                    }
                    
                    // In replace mode, delete existing text files not in backup
                    if (!options.mergeMode) {
                        appFilesDir.listFiles()?.filter { 
                            it.isFile && it.name.endsWith(".txt") && 
                            !listOf("memo_visibility.txt", "display_names.txt", "memos.txt").contains(it.name) &&
                            !backup.textFiles.containsKey(it.name)
                        }?.forEach { file ->
                            file.delete()
                            Log.d("GlobalBackup", "Deleted file not in backup: ${file.name}")
                        }
                    }
                    
                    // Restore text files
                    backup.textFiles.forEach { (filename, content) ->
                        val file = File(appFilesDir, filename)
                        file.writeText(content)
                        restoredTextFileCount++
                    }
                    
                    // Restore display names
                    if (backup.displayNames.isNotEmpty()) {
                        val displayNameFile = File(appFilesDir, "display_names.txt")
                        
                        val finalDisplayNames = if (options.mergeMode) {
                            // Merge mode: Read existing and merge with backup
                            val existingDisplayNames = if (displayNameFile.exists()) {
                                displayNameFile.readLines()
                                    .map { line -> line.split("=", limit = 2) }
                                    .filter { it.size == 2 }
                                    .associate { it[0] to it[1] }
                            } else {
                                emptyMap()
                            }
                            // Merge: backup names take precedence, but keep existing entries not in backup
                            existingDisplayNames + backup.displayNames
                        } else {
                            // Replace mode: Use only backup data
                            backup.displayNames
                        }
                        
                        displayNameFile.writeText(finalDisplayNames.entries.joinToString("\n") { (id, name) ->
                            "$id=$name"
                        })
                    }
                    
                    // Restore memos
                    if (backup.memos.isNotEmpty()) {
                        val memoFile = File(appFilesDir, "memos.txt")
                        
                        val finalMemos = if (options.mergeMode) {
                            // Merge mode: Read existing and merge with backup
                            val existingMemos = if (memoFile.exists()) {
                                memoFile.readLines()
                                    .map { line -> line.split("=", limit = 2) }
                                    .filter { it.size == 2 }
                                    .associate { it[0] to it[1] }
                            } else {
                                emptyMap()
                            }
                            // Merge: backup memos take precedence, but keep existing entries not in backup
                            existingMemos + backup.memos
                        } else {
                            // Replace mode: Use only backup data
                            backup.memos
                        }
                        
                        memoFile.writeText(finalMemos.entries.joinToString("\n") { (id, memo) ->
                            "$id=$memo"
                        })
                    }
                    
                    // Restore memo visibility
                    if (backup.memoVisibility.isNotEmpty()) {
                        val memoVisibilityFile = File(appFilesDir, "memo_visibility.txt")
                        
                        val finalMemoVisibility = if (options.mergeMode) {
                            // Merge mode: Read existing and merge with backup
                            val existingMemoVisibility = if (memoVisibilityFile.exists()) {
                                memoVisibilityFile.readLines()
                                    .map { line -> line.split("=", limit = 2) }
                                    .filter { it.size == 2 }
                                    .associate { it[0] to it[1].toBoolean() }
                            } else {
                                emptyMap()
                            }
                            // Merge: backup visibility takes precedence, but keep existing entries not in backup
                            existingMemoVisibility + backup.memoVisibility
                        } else {
                            // Replace mode: Use only backup data
                            backup.memoVisibility
                        }
                        
                        memoVisibilityFile.writeText(finalMemoVisibility.entries.joinToString("\n") { (id, visible) ->
                            "$id=$visible"
                        })
                    }
                    
                    Log.d("GlobalBackup", "Restored $restoredTextFileCount text files")
                } catch (e: Exception) {
                    Log.e("GlobalBackup", "Error restoring text files", e)
                    errors.add("Could not restore text files: ${e.message}")
                }
            }
            
            return RestoreResult(
                success = errors.isEmpty(),
                restoredPhotos = restoredPhotoCount,
                restoredTextFiles = restoredTextFileCount,
                restoredPresets = backup.colorPresets.size,
                restoredSettings = restoredSettings,
                warnings = warnings,
                errors = errors
            )
        }
        
        /**
         * Safely get Long from SharedPreferences, handling Int to Long migration
         */
        private fun safeGetLong(prefs: SharedPreferences, key: String): Long? {
            return try {
                if (prefs.contains(key)) {
                    prefs.getLong(key, 0L)
                } else null
            } catch (e: ClassCastException) {
                // Try to get as Int and convert
                try {
                    prefs.getInt(key, 0).toLong()
                } catch (e2: Exception) {
                    null
                }
            }
        }
        
        /**
         * Generate a default backup filename
         */
        fun generateBackupFilename(): String {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            return "TeXter_Backup_${dateFormat.format(Date())}$BACKUP_FILE_EXTENSION"
        }
    }
}
