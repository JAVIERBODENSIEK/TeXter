package com.j4.texter2025.data

enum class PhotoBackgroundMode {
    DYNAMIC,        // Current behavior - photo moves with gestures
    STATIC_ZOOM;    // Photo stays fixed, auto-zooms to always fill the area
    
    companion object {
        fun fromString(value: String?): PhotoBackgroundMode {
            return when (value) {
                "STATIC_ZOOM" -> STATIC_ZOOM
                else -> DYNAMIC // Default to dynamic for backward compatibility
            }
        }
    }
    
    override fun toString(): String {
        return when (this) {
            DYNAMIC -> "DYNAMIC"
            STATIC_ZOOM -> "STATIC_ZOOM"
        }
    }
}
