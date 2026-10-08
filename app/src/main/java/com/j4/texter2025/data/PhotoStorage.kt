package com.j4.texter2025.data

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

/**
 * Handles saving photos to app's internal storage.
 * Photos are copied from content URIs to internal storage so they:
 * 1. Persist even if deleted from gallery
 * 2. Can be backed up and restored
 * 3. Don't have permission issues after reinstall
 */
object PhotoStorage {
    private const val TAG = "PhotoStorage"
    private const val PHOTOS_DIR = "background_photos"
    
    /**
     * Get the photos directory, creating it if needed
     */
    private fun getPhotosDir(context: Context): File {
        val dir = File(context.filesDir, PHOTOS_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    /**
     * Save a photo from a content URI to internal storage.
     * Returns the internal file path, or null if failed.
     */
    fun savePhoto(context: Context, contentUri: Uri): String? {
        return try {
            // Keep source quality: copy original bytes as-is (no decode, no resample, no recompress).
            val sourceBytes = context.contentResolver.openInputStream(contentUri)?.use { it.readBytes() }
                ?: return null
            if (sourceBytes.isEmpty()) {
                Log.e(TAG, "Source photo is empty: $contentUri")
                return null
            }

            val extension = resolvePhotoExtension(context, contentUri)

            // Generate unique filename based on original source bytes.
            val filename = generateFilename(sourceBytes, extension)
            val file = File(getPhotosDir(context), filename)
            
            // Check if file already exists (same photo)
            if (file.exists()) {
                Log.d(TAG, "Photo already exists: ${file.absolutePath}")
                return file.absolutePath
            }
            
            // Save original bytes exactly to preserve quality.
            FileOutputStream(file).use { out ->
                out.write(sourceBytes)
            }
            
            Log.d(TAG, "Saved photo: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error saving photo from $contentUri", e)
            null
        }
    }
    
    /**
     * Check if a URI is an internal photo path
     */
    fun isInternalPhoto(uri: String?): Boolean {
        if (uri == null) return false
        return uri.startsWith("/") && uri.contains(PHOTOS_DIR)
    }
    
    /**
     * Check if a photo file exists
     */
    fun photoExists(path: String?): Boolean {
        if (path == null) return false
        return File(path).exists()
    }
    
    /**
     * Delete a photo from internal storage
     */
    fun deletePhoto(path: String?): Boolean {
        if (path == null) return false
        val file = File(path)
        return if (file.exists() && file.absolutePath.contains(PHOTOS_DIR)) {
            file.delete()
        } else {
            false
        }
    }
    
    /**
     * Get all photo files in internal storage
     */
    fun getAllPhotos(context: Context): List<File> {
        return getPhotosDir(context).listFiles()?.toList() ?: emptyList()
    }
    
    /**
     * Clean up unused photos (photos not referenced by any preset or setting)
     */
    fun cleanupUnusedPhotos(context: Context, usedPaths: Set<String>) {
        val photosDir = getPhotosDir(context)
        photosDir.listFiles()?.forEach { file ->
            if (!usedPaths.contains(file.absolutePath)) {
                Log.d(TAG, "Deleting unused photo: ${file.absolutePath}")
                file.delete()
            }
        }
    }
    
    /**
     * Convert photo to Base64 for backup
     */
    fun photoToBase64(path: String): String? {
        return try {
            val file = File(path)
            if (!file.exists()) return null
            
            val bytes = file.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Error converting photo to Base64: $path", e)
            null
        }
    }
    
    /**
     * Restore photo from Base64 backup data
     */
    fun photoFromBase64(context: Context, base64: String, originalFilename: String): String? {
        return try {
            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            val file = File(getPhotosDir(context), originalFilename)
            
            // Don't overwrite if already exists
            if (file.exists()) {
                return file.absolutePath
            }
            
            file.writeBytes(bytes)
            Log.d(TAG, "Restored photo from backup: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring photo from Base64", e)
            null
        }
    }
    
    private fun generateFilename(bytes: ByteArray, extension: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val hash = digest.digest(bytes)
        val hexString = hash.joinToString("") { "%02x".format(it) }
        
        return "photo_$hexString.$extension"
    }

    private fun resolvePhotoExtension(context: Context, contentUri: Uri): String {
        val mimeType = context.contentResolver.getType(contentUri)?.lowercase()
        val fromMime = when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/heic" -> "heic"
            "image/heif" -> "heif"
            "image/gif" -> "gif"
            "image/bmp" -> "bmp"
            "image/tiff", "image/x-tiff" -> "tiff"
            else -> null
        }
        if (fromMime != null) return fromMime

        val path = contentUri.lastPathSegment ?: return "jpg"
        val candidate = path.substringAfterLast('.', missingDelimiterValue = "").lowercase()
        return when (candidate) {
            "jpg", "jpeg", "png", "webp", "heic", "heif", "gif", "bmp", "tiff", "tif" -> {
                if (candidate == "jpeg") "jpg" else if (candidate == "tif") "tiff" else candidate
            }
            else -> "jpg"
        }
    }
    
    /**
     * Migrate existing content:// URIs to internal storage.
     * This should be called once on app start to convert old URIs.
     * Returns the number of photos migrated.
     */
    fun migrateExistingPhotos(context: Context): Int {
        val prefs = context.getSharedPreferences("TeXterPrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        var migratedCount = 0
        
        // List of photo URI preference keys to migrate
        val photoUriKeys = listOf(
            "custom_bg_photo_uri",
            "content_area_photo_uri",
            "memo_area_photo_uri",
            "file_list_item_photo_uri",
            "file_list_background_photo_uri"
        )
        
        for (key in photoUriKeys) {
            val uri = prefs.getString(key, null)
            if (uri != null && !isInternalPhoto(uri)) {
                // This is a content:// URI, try to migrate it
                try {
                    val contentUri = Uri.parse(uri)
                    val internalPath = savePhoto(context, contentUri)
                    if (internalPath != null) {
                        editor.putString(key, internalPath)
                        migratedCount++
                        Log.d(TAG, "Migrated $key: $uri -> $internalPath")
                    } else {
                        // Could not migrate, clear the invalid URI
                        editor.remove(key)
                        Log.w(TAG, "Could not migrate $key, clearing invalid URI: $uri")
                    }
                } catch (e: Exception) {
                    // URI is invalid or inaccessible, clear it
                    editor.remove(key)
                    Log.w(TAG, "Error migrating $key, clearing: ${e.message}")
                }
            }
        }
        
        // Also migrate photos in custom presets JSON
        migratedCount += migrateCustomPresetsPhotos(context, prefs, editor)
        
        editor.apply()
        
        if (migratedCount > 0) {
            Log.d(TAG, "Photo migration complete: $migratedCount photos migrated")
        }
        
        return migratedCount
    }
    
    /**
     * Migrate photos stored in custom presets JSON strings
     */
    private fun migrateCustomPresetsPhotos(
        context: Context,
        prefs: android.content.SharedPreferences,
        editor: android.content.SharedPreferences.Editor
    ): Int {
        var migratedCount = 0
        
        // Custom preset keys that may contain photo URIs
        val presetKeys = listOf(
            "custom_presets_bg",
            "custom_presets_content",
            "custom_presets_memo",
            "custom_presets_filelist",
            "custom_presets_filelistbg"
        )
        
        for (key in presetKeys) {
            val json = prefs.getString(key, null) ?: continue
            
            try {
                val jsonArray = org.json.JSONArray(json)
                var modified = false
                
                for (i in 0 until jsonArray.length()) {
                    val preset = jsonArray.getJSONObject(i)
                    
                    // Check for photoUri field
                    if (preset.has("photoUri")) {
                        val photoUri = preset.optString("photoUri", "")
                        if (photoUri.isNotEmpty() && !isInternalPhoto(photoUri)) {
                            try {
                                val contentUri = Uri.parse(photoUri)
                                val internalPath = savePhoto(context, contentUri)
                                if (internalPath != null) {
                                    preset.put("photoUri", internalPath)
                                    modified = true
                                    migratedCount++
                                    Log.d(TAG, "Migrated preset photo in $key: $photoUri -> $internalPath")
                                } else {
                                    // Clear invalid URI
                                    preset.put("photoUri", "")
                                    modified = true
                                }
                            } catch (e: Exception) {
                                preset.put("photoUri", "")
                                modified = true
                                Log.w(TAG, "Error migrating preset photo in $key: ${e.message}")
                            }
                        }
                    }
                }
                
                if (modified) {
                    editor.putString(key, jsonArray.toString())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing presets JSON for $key", e)
            }
        }
        
        return migratedCount
    }
}
