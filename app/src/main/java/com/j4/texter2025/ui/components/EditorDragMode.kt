package com.j4.texter2025.ui.components

/**
 * Feature flag for single-bar drag mode
 * 
 * FEATURE_SINGLE_BAR_DRAG: Incomplete feature - temporarily disabled
 * 
 * Status: Work in progress - basic implementation exists but needs refinement
 * To enable: Set this to true and rebuild
 * 
 * What works:
 * - Basic vertical drag (height split between content/memo)
 * - Basic horizontal drag (width adjustment)
 * - Visual drag handle with arrow hints
 * 
 * What needs work:
 * - Fine-tuning drag sensitivity
 * - Better visual feedback during drag
 * - Persistence of single-bar mode settings
 * - Testing edge cases and polish
 */
const val ENABLE_SINGLE_BAR_MODE = false

/**
 * Defines drag mode options for resizing content and memo areas in the editor
 */
enum class EditorDragMode {
    /**
     * Dual-bar mode: Separate drag handles for content area and memo area
     * This is the stable, production-ready mode.
     */
    DUAL_BAR,
    
    /**
     * Single-bar mode: One drag handle between areas that adjusts both simultaneously
     * Similar to the preview window behavior.
     * 
     * NOTE: This mode is currently disabled via ENABLE_SINGLE_BAR_MODE feature flag.
     * The implementation exists but is hidden from users until further refinement.
     */
    SINGLE_BAR
}
