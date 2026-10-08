package com.j4.texter2025.ui.components

/**
 * Defines behavior options for how the memo area should appear when opening files
 */
enum class MemoAreaBehavior {
    /**
     * Always show the memo area when opening a file
     */
    ALWAYS_SHOW,
    
    /**
     * Always hide the memo area when opening a file
     */
    ALWAYS_HIDE,
    
    /**
     * Remember the last state of the memo area for each file
     */
    REMEMBER_PER_FILE
}
