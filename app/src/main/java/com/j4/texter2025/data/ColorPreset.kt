package com.j4.texter2025.data

import androidx.compose.ui.graphics.Color
import org.json.JSONObject
import org.json.JSONArray
import com.j4.texter2025.ui.components.SimpleColorPreset

// Data class for a user-defined color preset
// Stores all relevant colors for a full UI preset

data class ColorPreset(
    val name: String,
    // Background colors and blur
    val customBgColor: Color?,
    val customBgBlur: Float,
    val customBgColors: List<SimpleColorPreset>,
    // Content area colors and blur
    val contentAreaColor: Color?,
    val contentAreaBlur: Float,
    val customContentAreaColors: List<SimpleColorPreset>,
    // Memo area colors and blur
    val memoAreaColor: Color?,
    val memoAreaBlur: Float,
    val customMemoAreaColors: List<SimpleColorPreset>,
    // File list colors and blur
    val fileListItemColor: Color?,
    val fileListItemBlur: Float = 0f,
    val customFileListItemColors: List<SimpleColorPreset>,
    val fileListBackgroundColor: Color?,
    val fileListBackgroundBlur: Float = 0f,
    val customFileListBackgroundColors: List<SimpleColorPreset>,
    // Photo URIs for selected squares (to distinguish between multiple photo squares)
    val customBgPhotoUri: String? = null,
    val contentAreaPhotoUri: String? = null,
    val memoAreaPhotoUri: String? = null,
    val fileListItemPhotoUri: String? = null,
    val fileListBackgroundPhotoUri: String? = null,
    // Photo positioning (offset and scale)
    val customBgPhotoOffsetX: Float = 0f,
    val customBgPhotoOffsetY: Float = 0f,
    val customBgPhotoScale: Float = 1f,
    val customBgPhotoRotation: Float = 0f,
    val contentAreaPhotoOffsetX: Float = 0f,
    val contentAreaPhotoOffsetY: Float = 0f,
    val contentAreaPhotoScale: Float = 1f,
    val contentAreaPhotoRotation: Float = 0f,
    val memoAreaPhotoOffsetX: Float = 0f,
    val memoAreaPhotoOffsetY: Float = 0f,
    val memoAreaPhotoScale: Float = 1f,
    val memoAreaPhotoRotation: Float = 0f,
    val fileListItemPhotoOffsetX: Float = 0f,
    val fileListItemPhotoOffsetY: Float = 0f,
    val fileListItemPhotoScale: Float = 1f,
    val fileListItemPhotoRotation: Float = 0f,
    val fileListBackgroundPhotoOffsetX: Float = 0f,
    val fileListBackgroundPhotoOffsetY: Float = 0f,
    val fileListBackgroundPhotoScale: Float = 1f,
    val fileListBackgroundPhotoRotation: Float = 0f,
    // Photo background modes
    val customBgPhotoBackgroundMode: PhotoBackgroundMode = PhotoBackgroundMode.DYNAMIC,
    val contentAreaPhotoBackgroundMode: PhotoBackgroundMode = PhotoBackgroundMode.DYNAMIC,
    val memoAreaPhotoBackgroundMode: PhotoBackgroundMode = PhotoBackgroundMode.DYNAMIC,
    val fileListItemPhotoBackgroundMode: PhotoBackgroundMode = PhotoBackgroundMode.DYNAMIC,
    val fileListBackgroundPhotoBackgroundMode: PhotoBackgroundMode = PhotoBackgroundMode.DYNAMIC,
    // Legacy fields for backward compatibility
    val listAreaColor: Color? = null,
    val dialogBgColor: Color? = null,
    val memoColor: Color? = null
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("name", name)
        
        // Background settings
        customBgColor?.let { obj.put("customBgColor", it.value.toLong()) }
        obj.put("customBgBlur", customBgBlur)
        obj.put("customBgColors", JSONArray().apply {
            customBgColors.forEach { put(it.toJson()) }
        })
        
        // Content area settings
        contentAreaColor?.let { obj.put("contentAreaColor", it.value.toLong()) }
        obj.put("contentAreaBlur", contentAreaBlur)
        obj.put("customContentAreaColors", JSONArray().apply {
            customContentAreaColors.forEach { put(it.toJson()) }
        })
        
        // Memo area settings
        memoAreaColor?.let { obj.put("memoAreaColor", it.value.toLong()) }
        obj.put("memoAreaBlur", memoAreaBlur)
        obj.put("customMemoAreaColors", JSONArray().apply {
            customMemoAreaColors.forEach { put(it.toJson()) }
        })
        
        // File list settings
        fileListItemColor?.let { obj.put("fileListItemColor", it.value.toLong()) }
        obj.put("fileListItemBlur", fileListItemBlur)
        obj.put("customFileListItemColors", JSONArray().apply {
            customFileListItemColors.forEach { put(it.toJson()) }
        })
        fileListBackgroundColor?.let { obj.put("fileListBackgroundColor", it.value.toLong()) }
        obj.put("fileListBackgroundBlur", fileListBackgroundBlur)
        obj.put("customFileListBackgroundColors", JSONArray().apply {
            customFileListBackgroundColors.forEach { put(it.toJson()) }
        })
        
        // Photo URIs for selected squares
        customBgPhotoUri?.let { obj.put("customBgPhotoUri", it) }
        contentAreaPhotoUri?.let { obj.put("contentAreaPhotoUri", it) }
        memoAreaPhotoUri?.let { obj.put("memoAreaPhotoUri", it) }
        fileListItemPhotoUri?.let { obj.put("fileListItemPhotoUri", it) }
        fileListBackgroundPhotoUri?.let { obj.put("fileListBackgroundPhotoUri", it) }
        
        // Photo positioning
        obj.put("customBgPhotoOffsetX", customBgPhotoOffsetX)
        obj.put("customBgPhotoOffsetY", customBgPhotoOffsetY)
        obj.put("customBgPhotoScale", customBgPhotoScale)
        obj.put("customBgPhotoRotation", customBgPhotoRotation)
        obj.put("contentAreaPhotoOffsetX", contentAreaPhotoOffsetX)
        obj.put("contentAreaPhotoOffsetY", contentAreaPhotoOffsetY)
        obj.put("contentAreaPhotoScale", contentAreaPhotoScale)
        obj.put("contentAreaPhotoRotation", contentAreaPhotoRotation)
        obj.put("memoAreaPhotoOffsetX", memoAreaPhotoOffsetX)
        obj.put("memoAreaPhotoOffsetY", memoAreaPhotoOffsetY)
        obj.put("memoAreaPhotoScale", memoAreaPhotoScale)
        obj.put("memoAreaPhotoRotation", memoAreaPhotoRotation)
        obj.put("fileListItemPhotoOffsetX", fileListItemPhotoOffsetX)
        obj.put("fileListItemPhotoOffsetY", fileListItemPhotoOffsetY)
        obj.put("fileListItemPhotoScale", fileListItemPhotoScale)
        obj.put("fileListItemPhotoRotation", fileListItemPhotoRotation)
        obj.put("fileListBackgroundPhotoOffsetX", fileListBackgroundPhotoOffsetX)
        obj.put("fileListBackgroundPhotoOffsetY", fileListBackgroundPhotoOffsetY)
        obj.put("fileListBackgroundPhotoScale", fileListBackgroundPhotoScale)
        obj.put("fileListBackgroundPhotoRotation", fileListBackgroundPhotoRotation)
        
        // Photo background modes
        obj.put("customBgPhotoBackgroundMode", customBgPhotoBackgroundMode.toString())
        obj.put("contentAreaPhotoBackgroundMode", contentAreaPhotoBackgroundMode.toString())
        obj.put("memoAreaPhotoBackgroundMode", memoAreaPhotoBackgroundMode.toString())
        obj.put("fileListItemPhotoBackgroundMode", fileListItemPhotoBackgroundMode.toString())
        obj.put("fileListBackgroundPhotoBackgroundMode", fileListBackgroundPhotoBackgroundMode.toString())
        
        // Legacy fields for backward compatibility
        listAreaColor?.let { obj.put("listAreaColor", it.value.toLong()) }
        dialogBgColor?.let { obj.put("dialogBgColor", it.value.toLong()) }
        memoColor?.let { obj.put("memoColor", it.value.toLong()) }
        
        return obj
    }

    companion object {
        // Helper function to safely load color from JSON (handles migration from Int to Long)
        private fun safeLoadColorFromJson(obj: JSONObject, key: String): Color? {
            if (!obj.has(key)) return null
            return try {
                Color(obj.getLong(key).toULong())
            } catch (e: Exception) {
                try {
                    Color(obj.getInt(key))
                } catch (e2: Exception) {
                    null
                }
            }
        }
        
        // Helper function to load SimpleColorPreset array from JSON
        private fun loadSimpleColorPresetArrayFromJson(obj: JSONObject, key: String): List<SimpleColorPreset> {
            if (!obj.has(key)) return emptyList()
            val arr = obj.getJSONArray(key)
            val presets = mutableListOf<SimpleColorPreset>()
            for (i in 0 until arr.length()) {
                try {
                    // Try to load as SimpleColorPreset object
                    val presetObj = arr.getJSONObject(i)
                    presets.add(SimpleColorPreset.fromJson(presetObj))
                } catch (e: Exception) {
                    try {
                        // Fallback: Try to load as old format (just color value)
                        val colorValue = arr.getLong(i).toULong()
                        presets.add(SimpleColorPreset(
                            name = "Custom ${i + 1}",
                            color = Color(colorValue)
                        ))
                    } catch (e2: Exception) {
                        try {
                            val colorValue = arr.getInt(i)
                            presets.add(SimpleColorPreset(
                                name = "Custom ${i + 1}",
                                color = Color(colorValue)
                            ))
                        } catch (e3: Exception) {
                            // Skip invalid entries
                        }
                    }
                }
            }
            return presets
        }
        
        fun fromJson(obj: JSONObject): ColorPreset {
            val name = obj.getString("name")
            
            // Load new format data with backward compatibility for old "color" field
            val customBgColor = safeLoadColorFromJson(obj, "customBgColor") 
                ?: safeLoadColorFromJson(obj, "color") // Old format fallback
            val customBgBlur = if (obj.has("customBgBlur")) obj.getDouble("customBgBlur").toFloat() else 0f
            val customBgColors = loadSimpleColorPresetArrayFromJson(obj, "customBgColors")
            
            val contentAreaColor = safeLoadColorFromJson(obj, "contentAreaColor")
            val contentAreaBlur = if (obj.has("contentAreaBlur")) obj.getDouble("contentAreaBlur").toFloat() else 0f
            val customContentAreaColors = loadSimpleColorPresetArrayFromJson(obj, "customContentAreaColors")
            
            val memoAreaColor = safeLoadColorFromJson(obj, "memoAreaColor")
            val memoAreaBlur = if (obj.has("memoAreaBlur")) obj.getDouble("memoAreaBlur").toFloat() else 0f
            val customMemoAreaColors = loadSimpleColorPresetArrayFromJson(obj, "customMemoAreaColors")
            
            val fileListItemColor = safeLoadColorFromJson(obj, "fileListItemColor")
            val fileListItemBlur = if (obj.has("fileListItemBlur")) obj.getDouble("fileListItemBlur").toFloat() else 0f
            val customFileListItemColors = loadSimpleColorPresetArrayFromJson(obj, "customFileListItemColors")
            val fileListBackgroundColor = safeLoadColorFromJson(obj, "fileListBackgroundColor")
            val fileListBackgroundBlur = if (obj.has("fileListBackgroundBlur")) obj.getDouble("fileListBackgroundBlur").toFloat() else 0f
            val customFileListBackgroundColors = loadSimpleColorPresetArrayFromJson(obj, "customFileListBackgroundColors")
            
            // Load photo URIs (convert empty strings to null for consistency)
            val customBgPhotoUri = if (obj.has("customBgPhotoUri")) obj.getString("customBgPhotoUri").takeIf { it.isNotEmpty() } else null
            val contentAreaPhotoUri = if (obj.has("contentAreaPhotoUri")) obj.getString("contentAreaPhotoUri").takeIf { it.isNotEmpty() } else null
            val memoAreaPhotoUri = if (obj.has("memoAreaPhotoUri")) obj.getString("memoAreaPhotoUri").takeIf { it.isNotEmpty() } else null
            val fileListItemPhotoUri = if (obj.has("fileListItemPhotoUri")) obj.getString("fileListItemPhotoUri").takeIf { it.isNotEmpty() } else null
            val fileListBackgroundPhotoUri = if (obj.has("fileListBackgroundPhotoUri")) obj.getString("fileListBackgroundPhotoUri").takeIf { it.isNotEmpty() } else null
            
            // Load photo positioning
            val customBgPhotoOffsetX = if (obj.has("customBgPhotoOffsetX")) obj.getDouble("customBgPhotoOffsetX").toFloat() else 0f
            val customBgPhotoOffsetY = if (obj.has("customBgPhotoOffsetY")) obj.getDouble("customBgPhotoOffsetY").toFloat() else 0f
            val customBgPhotoScale = if (obj.has("customBgPhotoScale")) obj.getDouble("customBgPhotoScale").toFloat() else 1f
            val customBgPhotoRotation = if (obj.has("customBgPhotoRotation")) obj.getDouble("customBgPhotoRotation").toFloat() else 0f
            val contentAreaPhotoOffsetX = if (obj.has("contentAreaPhotoOffsetX")) obj.getDouble("contentAreaPhotoOffsetX").toFloat() else 0f
            val contentAreaPhotoOffsetY = if (obj.has("contentAreaPhotoOffsetY")) obj.getDouble("contentAreaPhotoOffsetY").toFloat() else 0f
            val contentAreaPhotoScale = if (obj.has("contentAreaPhotoScale")) obj.getDouble("contentAreaPhotoScale").toFloat() else 1f
            val contentAreaPhotoRotation = if (obj.has("contentAreaPhotoRotation")) obj.getDouble("contentAreaPhotoRotation").toFloat() else 0f
            val memoAreaPhotoOffsetX = if (obj.has("memoAreaPhotoOffsetX")) obj.getDouble("memoAreaPhotoOffsetX").toFloat() else 0f
            val memoAreaPhotoOffsetY = if (obj.has("memoAreaPhotoOffsetY")) obj.getDouble("memoAreaPhotoOffsetY").toFloat() else 0f
            val memoAreaPhotoScale = if (obj.has("memoAreaPhotoScale")) obj.getDouble("memoAreaPhotoScale").toFloat() else 1f
            val memoAreaPhotoRotation = if (obj.has("memoAreaPhotoRotation")) obj.getDouble("memoAreaPhotoRotation").toFloat() else 0f
            val fileListItemPhotoOffsetX = if (obj.has("fileListItemPhotoOffsetX")) obj.getDouble("fileListItemPhotoOffsetX").toFloat() else 0f
            val fileListItemPhotoOffsetY = if (obj.has("fileListItemPhotoOffsetY")) obj.getDouble("fileListItemPhotoOffsetY").toFloat() else 0f
            val fileListItemPhotoScale = if (obj.has("fileListItemPhotoScale")) obj.getDouble("fileListItemPhotoScale").toFloat() else 1f
            val fileListItemPhotoRotation = if (obj.has("fileListItemPhotoRotation")) obj.getDouble("fileListItemPhotoRotation").toFloat() else 0f
            val fileListBackgroundPhotoOffsetX = if (obj.has("fileListBackgroundPhotoOffsetX")) obj.getDouble("fileListBackgroundPhotoOffsetX").toFloat() else 0f
            val fileListBackgroundPhotoOffsetY = if (obj.has("fileListBackgroundPhotoOffsetY")) obj.getDouble("fileListBackgroundPhotoOffsetY").toFloat() else 0f
            val fileListBackgroundPhotoScale = if (obj.has("fileListBackgroundPhotoScale")) obj.getDouble("fileListBackgroundPhotoScale").toFloat() else 1f
            val fileListBackgroundPhotoRotation = if (obj.has("fileListBackgroundPhotoRotation")) obj.getDouble("fileListBackgroundPhotoRotation").toFloat() else 0f
            
            // Photo background modes
            val customBgPhotoBackgroundMode = if (obj.has("customBgPhotoBackgroundMode")) PhotoBackgroundMode.fromString(obj.getString("customBgPhotoBackgroundMode")) else PhotoBackgroundMode.DYNAMIC
            val contentAreaPhotoBackgroundMode = if (obj.has("contentAreaPhotoBackgroundMode")) PhotoBackgroundMode.fromString(obj.getString("contentAreaPhotoBackgroundMode")) else PhotoBackgroundMode.DYNAMIC
            val memoAreaPhotoBackgroundMode = if (obj.has("memoAreaPhotoBackgroundMode")) PhotoBackgroundMode.fromString(obj.getString("memoAreaPhotoBackgroundMode")) else PhotoBackgroundMode.DYNAMIC
            val fileListItemPhotoBackgroundMode = if (obj.has("fileListItemPhotoBackgroundMode")) PhotoBackgroundMode.fromString(obj.getString("fileListItemPhotoBackgroundMode")) else PhotoBackgroundMode.DYNAMIC
            val fileListBackgroundPhotoBackgroundMode = if (obj.has("fileListBackgroundPhotoBackgroundMode")) PhotoBackgroundMode.fromString(obj.getString("fileListBackgroundPhotoBackgroundMode")) else PhotoBackgroundMode.DYNAMIC
            
            // Legacy compatibility
            val listAreaColor = safeLoadColorFromJson(obj, "listAreaColor")
            val dialogBgColor = safeLoadColorFromJson(obj, "dialogBgColor")
            val memoColor = safeLoadColorFromJson(obj, "memoColor")
            
            return ColorPreset(
                name = name,
                customBgColor = customBgColor,
                customBgBlur = customBgBlur,
                customBgColors = customBgColors,
                contentAreaColor = contentAreaColor,
                contentAreaBlur = contentAreaBlur,
                customContentAreaColors = customContentAreaColors,
                memoAreaColor = memoAreaColor,
                memoAreaBlur = memoAreaBlur,
                customMemoAreaColors = customMemoAreaColors,
                fileListItemColor = fileListItemColor,
                fileListItemBlur = fileListItemBlur,
                customFileListItemColors = customFileListItemColors,
                fileListBackgroundColor = fileListBackgroundColor,
                fileListBackgroundBlur = fileListBackgroundBlur,
                customFileListBackgroundColors = customFileListBackgroundColors,
                customBgPhotoUri = customBgPhotoUri,
                contentAreaPhotoUri = contentAreaPhotoUri,
                memoAreaPhotoUri = memoAreaPhotoUri,
                fileListItemPhotoUri = fileListItemPhotoUri,
                fileListBackgroundPhotoUri = fileListBackgroundPhotoUri,
                customBgPhotoOffsetX = customBgPhotoOffsetX,
                customBgPhotoOffsetY = customBgPhotoOffsetY,
                customBgPhotoScale = customBgPhotoScale,
                customBgPhotoRotation = customBgPhotoRotation,
                contentAreaPhotoOffsetX = contentAreaPhotoOffsetX,
                contentAreaPhotoOffsetY = contentAreaPhotoOffsetY,
                contentAreaPhotoScale = contentAreaPhotoScale,
                contentAreaPhotoRotation = contentAreaPhotoRotation,
                memoAreaPhotoOffsetX = memoAreaPhotoOffsetX,
                memoAreaPhotoOffsetY = memoAreaPhotoOffsetY,
                memoAreaPhotoScale = memoAreaPhotoScale,
                memoAreaPhotoRotation = memoAreaPhotoRotation,
                fileListItemPhotoOffsetX = fileListItemPhotoOffsetX,
                fileListItemPhotoOffsetY = fileListItemPhotoOffsetY,
                fileListItemPhotoScale = fileListItemPhotoScale,
                fileListItemPhotoRotation = fileListItemPhotoRotation,
                fileListBackgroundPhotoOffsetX = fileListBackgroundPhotoOffsetX,
                fileListBackgroundPhotoOffsetY = fileListBackgroundPhotoOffsetY,
                fileListBackgroundPhotoScale = fileListBackgroundPhotoScale,
                fileListBackgroundPhotoRotation = fileListBackgroundPhotoRotation,
                customBgPhotoBackgroundMode = customBgPhotoBackgroundMode,
                contentAreaPhotoBackgroundMode = contentAreaPhotoBackgroundMode,
                memoAreaPhotoBackgroundMode = memoAreaPhotoBackgroundMode,
                fileListItemPhotoBackgroundMode = fileListItemPhotoBackgroundMode,
                fileListBackgroundPhotoBackgroundMode = fileListBackgroundPhotoBackgroundMode,
                listAreaColor = listAreaColor,
                dialogBgColor = dialogBgColor,
                memoColor = memoColor
            )
        }
        fun listToJson(presets: List<ColorPreset>): String {
            val arr = JSONArray()
            presets.forEach { arr.put(it.toJson()) }
            return arr.toString()
        }
        fun listFromJson(json: String): List<ColorPreset> {
            val arr = JSONArray(json)
            val list = mutableListOf<ColorPreset>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(fromJson(obj))
            }
            return list
        }
    }
}
