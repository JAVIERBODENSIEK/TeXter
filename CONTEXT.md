# TeXter2025 - Project Context

## 1. Tech Stack & Key Dependencies

### Core Framework
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material3)
- **Android SDK**: Target 35, Min 24
- **Build System**: Gradle with Kotlin DSL

### Key Dependencies
- `androidx.compose.material3` - Material Design 3 components
- `androidx.compose.material:material-icons-extended` - Extended icon set
- `androidx.lifecycle.runtime.ktx` - Lifecycle-aware components
- `androidx.activity.compose` - Compose integration with Activity
- `androidx.core.ktx` - Android KTX extensions
- Coil (image loading) - Used via `rememberHighQualityPhotoPainter` for photo backgrounds
- `io.mockk:mockk` - Testing framework

### Permissions
- `READ_EXTERNAL_STORAGE` (maxSdkVersion 32)
- `WRITE_EXTERNAL_STORAGE` (maxSdkVersion 32)
- `READ_MEDIA_IMAGES` (Android 13+)

---

## 2. Project Architecture & Folder Structure

### Root Structure
```
TeXter2025/
├── app/
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/j4/texter2025/
│       │   ├── MainActivity.kt (single Activity entry point)
│       │   ├── data/ (data models and persistence)
│       │   └── ui/ (Compose UI components)
│       └── res/ (resources)
├── CHANGELOG.md (detailed development history)
├── CHAT_HISTORY.md (session-by-session chat logs)
└── gradle/ (build configuration)
```

### Package Structure

#### `com.j4.texter2025.data`
- `FileModel.kt` - Data model for text files (name, content, memo, metadata)
- `ColorPreset.kt` - Color preset definitions
- `GlobalBackup.kt` - Backup/restore functionality
- `PhotoBackgroundMode.kt` - Photo rendering modes (DYNAMIC, STATIC_ZOOM)
- `PhotoStorage.kt` - Photo file storage management
- `SearchMethod.kt` - Search algorithm options
- `SortMethod.kt` - File sorting options

#### `com.j4.texter2025.ui.components`
- `FullScreenEditor.kt` - Main fullscreen note editor (in-app overlay, NOT a Dialog)
- `FileListScreen.kt` - Main file list view with search
- `FileListSearchBar.kt` - Search input component
- `FileListItem.kt` - Individual file list item
- `FileEditDialog.kt` - Dialog-based editor (legacy, fullscreen editor preferred)
- `SettingsScreen.kt` - Comprehensive settings UI
- `ColorPickerDialog.kt` - Color selection with spectrum picker
- `PhotoControlsPanel.kt` - Photo background controls
- `PhotoPositionDialog.kt` - Photo positioning dialog
- `BackupOptionsDialog.kt` - Backup/restore options
- `BlurredSurface.kt` - Blur effect wrapper
- `HighQualityPhotoPainter.kt` - Coil-based high-quality image loading
- `MemoAreaBehavior.kt` - Memo visibility behavior enum
- `EditorDragMode.kt` - Editor drag handle mode enum
- `ContentAreaStyle.kt` - Content area styling options
- `UnsavedChangesBehavior.kt` - Unsaved changes handling enum

#### `com.j4.texter2025.ui.theme`
- Theme definitions (Material3 theming)
- Color schemes
- Typography

#### `com.j4.texter2025.ui.icons`
- Custom icon definitions

---

## 3. Implemented Core Features & Logic

### File Management
- Create, edit, delete text files
- Import/export text files
- File list with search and sort
- File metadata tracking (last modified, memo visibility)

### FullScreenEditor (Primary Editor)
- **Architecture**: In-app fullscreen Compose overlay (converted from platform Dialog to fix bottom gap)
- **Features**:
  - Resizable content area with dual drag handles (content + memo)
  - Memo area with independent sizing and drag handle
  - Photo background support (per-area: custom, content, memo)
  - Blur effects on backgrounds (custom, content, memo, file list items)
  - Color customization (custom, content, memo, list areas)
  - Edge-to-edge display with optional status bar hiding
  - Back button dismissal via `BackHandler`
  - Unsaved changes detection and confirmation dialog
  - Delete confirmation dialog
  - "Back to Settings" FAB for settings navigation

### Settings System
- Comprehensive settings screen with categorized sections
- Color presets and custom color pickers
- Photo background controls (position, scale, rotation, offset, blur, alpha)
- Blur intensity controls
- Content area size and style configuration
- Memo area behavior (ALWAYS_SHOW, ALWAYS_HIDE, REMEMBER_PER_FILE)
- Editor drag mode selection
- Status bar visibility toggle
- Backup/restore functionality
- UI scale adjustment

### Visual Effects
- **Blur Rendering**: Dual-layer cross-fade (sharp + blurred) for smooth transitions
- **Photo Backgrounds**: Two modes
  - DYNAMIC: Photo scales to fill with offset/rotation support
  - STATIC_ZOOM: Fixed fit scaling
- **High-Quality Image Loading**: Coil with custom decode settings

### Edge-to-Edge Display
- Activity window configured with `decorFitsSystemWindows = false`
- Transparent system bar backgrounds
- Immersive sticky layout flags for fullscreen editor
- Proper cutout handling (`LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES`)

### State Management
- SharedPreferences for persistence
- Compose state with `remember`, `mutableStateOf`, `derivedStateOf`
- `LaunchedEffect` for side effects
- `SideEffect` for window-level configuration

---

## 4. Current State of Development & Open Issues

### Recently Completed (2026-06-04 to 2026-06-07)
- ✅ Fixed bottom gap in FullScreenEditor by converting from platform Dialog to in-app overlay
- ✅ Fixed blur side effect from parent Scaffold blur affecting the overlay
- ✅ Fixed black top gap during keyboard relayouts via transparent system bar colors
- ✅ Moved content resize handle below content area
- ✅ Matched content and memo dragger lengths
- ✅ Removed deprecated `FLAG_FULLSCREEN` to fix status bar compatibility loop

### Known TODOs/FIXMEs (found in codebase)
- `MainActivity.kt` (11 TODO/FIXME comments)
- `GlobalBackup.kt` (16 TODO/FIXME comments)
- `SettingsScreen.kt` (5 TODO/FIXME comments)
- `FullScreenEditor.kt` (4 TODO/FIXME comments)
- `ColorPickerDialog.kt` (2 TODO/FIXME comments)

### Diagnostic Overlay
- `SHOW_FULLSCREEN_EDITOR_GAP_DIAGNOSTIC = true` in `FullScreenEditor.kt`
- Visual diagnostic with colored borders to identify layout regions
- Should be disabled once gap issues are fully resolved

### Legacy Files
- `FileEditDialog.kt.new` - Backup of previous dialog-based editor
- `FileListScreen.kt.new` - Backup of previous file list screen
- `FullScreenEditor_Fixed.kt` - Empty placeholder
- Consider removing these after confirming stability

---

## 5. Next Planned Tasks / TODOs

### Immediate
1. **Disable Diagnostic Overlay**: Set `SHOW_FULLSCREEN_EDITOR_GAP_DIAGNOSTIC = false` in `FullScreenEditor.kt` after confirming gap fix is stable
2. **Clean Up Legacy Files**: Remove `.new` backup files and empty placeholder files
3. **Address TODO Comments**: Review and resolve TODO/FIXME comments in:
   - `GlobalBackup.kt` (backup/restore improvements)
   - `SettingsScreen.kt` (settings refinements)
   - `MainActivity.kt` (activity-level improvements)
   - `FullScreenEditor.kt` (editor refinements)
   - `ColorPickerDialog.kt` (color picker improvements)

### Medium Priority
1. **Testing**: Comprehensive testing of the new in-app overlay editor on different devices/screen sizes
2. **Performance**: Monitor performance of blur effects and photo rendering, especially on lower-end devices
3. **Accessibility**: Verify accessibility features (screen readers, contrast, touch targets)
4. **Localization**: Consider adding string resources for internationalization if needed

### Long-term
1. **Release Preparation**: Version 1.0 release preparation (proguard rules, release build testing)
2. **Feature Enhancements**: Based on user feedback and TODO comments
3. **Documentation**: Update README with user-facing documentation

---

## Important Notes for AI Continuation

### Critical Architecture Decision
- **FullScreenEditor is NOT a Dialog**: It was converted from a platform `Dialog` to an in-app Compose overlay to fix a bottom gap issue. This means:
  - It lives in the Activity Compose tree
  - It uses `BackHandler` for dismissal
  - It controls the Activity window directly for edge-to-edge display
  - Do NOT wrap it in a `Dialog` composable

### Blur Effects
- Blur is applied via `BlurredSurface.kt` using dual-layer rendering
- Parent Scaffold blur was removed to avoid blurring the in-app overlay
- Blur values are passed through the component hierarchy from settings

### Photo Backgrounds
- Use `PhotoBackgroundSurface.kt` for rendering
- Two modes: DYNAMIC (with transforms) and STATIC_ZOOM (fixed fit)
- High-quality loading via `HighQualityPhotoPainter.kt` (Coil wrapper)

### State Persistence
- SharedPreferences key: `PREFS_NAME = "TeXterPrefs"`
- File state persisted in `FileModel` (memoVisible, etc.)
- Size preferences (content area, memo area) persisted separately

### Edge-to-Edge Handling
- Activity window configured in `MainActivity.applyStatusBarVisibility()`
- FullScreenEditor applies immersive flags via `SideEffect`
- System bar colors set to transparent
- Cutout mode set to `SHORT_EDGES`

### Component Size Limits (User Rules)
- UI Components: 500 lines MAX
- Regular Functions: 200 lines MAX
- State Variables per Component: 10 MAX
- Effect Hooks per Component: 5 MAX
- Classes: 1000 lines MAX
- Files: 2000 lines MAX
- **Always extract sub-components/dialogs/constants before hitting limits**

### Build Commands
```bash
# Compile check
./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none

# Full build
./gradlew.bat build
```

### Key Files to Understand First
1. `MainActivity.kt` - Entry point, state coordination, file operations
2. `FullScreenEditor.kt` - Main editor (in-app overlay)
3. `FileListScreen.kt` - Main file list UI
4. `SettingsScreen.kt` - Settings configuration
5. `FileModel.kt` - Data model
6. `CHANGELOG.md` - Detailed development history
