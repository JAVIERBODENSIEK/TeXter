package com.j4.texter2025.ui.components

/**
 * Defines behavior options for how the app should handle unsaved changes when exiting an editor
 */
enum class UnsavedChangesBehavior {
    /**
     * Always ask the user what to do with unsaved changes
     */
    ALWAYS_ASK,
    
    /**
     * Never ask and discard unsaved changes
     */
    NEVER_ASK_DISCARD,
    
    /**
     * Never ask and automatically save changes
     */
    NEVER_ASK_SAVE
}
