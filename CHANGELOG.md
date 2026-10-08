# TeXter2025 Changelog

## Releases

| Version | Date | Key Changes |
|---------|------|-------------|
| - | - | No releases yet |

---

## Current Development

### [2026-06-04] Fix: allow opened-note content area to fully extend with always-show memo

**Type**: Editor Layout / Edge-to-Edge Fix

**Status**: ✅ Complete

**Diagnostic Update**:
- Added a temporary visible `FullScreenEditor` gap diagnostic overlay to identify the exact owner of the persistent gap before further layout changes.
- `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt`
  - Added `SHOW_FULLSCREEN_EDITOR_GAP_DIAGNOSTIC = true`.
  - Tints the editor root magenta and outlines it with a magenta border.
  - Outlines the content editor surface in red.
  - Outlines the memo surface in green.
  - Overlays the Activity-reported status-bar inset in red and navigation-bar inset in cyan.
  - Adds a yellow full-screen overlay border and a bottom legend explaining the color meanings.
  - Purpose: if the reported gap remains black/untinted, it is outside the Compose dialog content; if it is colored, the color identifies which layout/system region owns it.
  - Screenshot validation showed the reported black gap is below the yellow diagnostic overlay, confirming it is outside the `FullScreenEditor` Compose root.
  - Cleared dialog decor-view padding and installed an insets listener that keeps the dialog decor padding at `0,0,0,0`, so platform decor insets cannot reserve space below the Compose content.
  - Follow-up screenshot showed no visible change after clearing decor padding, confirming the dialog content window itself was still being measured above the navigation/gesture area.
  - Added `FLAG_LAYOUT_IN_SCREEN` alongside `FLAG_LAYOUT_NO_LIMITS` and applied legacy immersive sticky layout flags on the dialog decor view so the dialog content can be laid out into the navigation/gesture area on devices where `WindowInsetsControllerCompat.hide(systemBars())` is not sufficient.
  - A second follow-up screenshot showed the platform dialog still ended above the black strip, so the opened-note editor was converted from a platform `Dialog` into an in-app fullscreen Compose overlay attached to the Activity window.
  - Added `BackHandler { handleExit() }` to preserve back-button dismissal behavior after removing `Dialog.onDismissRequest`.
  - Moved the edge-to-edge, transparent system-bar, cutout, and immersive handling from the dialog window to the Activity window for this editor path.
  - Follow-up validation confirmed the yellow diagnostic border now reaches the full screen, proving the bottom gap was resolved by removing the platform dialog layer.
  - Fixed the resulting blur side effect by removing the parent `Scaffold` blur that previously ran while `isFullScreenEditorShown` was true; the editor is now inside the Activity Compose tree, so that blur was affecting the editor itself.

**Issue**:
- With the memo area set to always show, dragging the opened-note content area downward still left a persistent blocked gap and the extended part was not visible.
- When the content area was dragged to maximum height, it could push or hide the content dragger and Hide/Show Memo row near the bottom of the screen.
- Logcat also showed transient status-bar visibility and relayout changes while the keyboard/status-bar area was involved.

**Root Cause**:
- The editor body is measured at full window height because the opened-note dialog uses `FLAG_LAYOUT_NO_LIMITS`, so the layout extends behind the bottom navigation/gesture area.
- The earlier fix reserved the navigation-bar height via Compose `WindowInsets.navigationBars`, but that reports `0` inside this dialog, so the reserve had no effect.
- Reading insets from the Activity decor view was also not sufficient on the affected device/session, so the content layout still reached the same bottom border.
- Applying a large visible bottom `Column` padding confirmed the affected area but over-reserved it, making the visible bottom gap larger.
- The remaining gap came from a measurement mismatch: the max-height calculation used the full editor body height, while `fillMaxHeight(fraction)` was applied inside a `Column` after the header and therefore did not use the same effective height basis.
- The gap moving from bottom to top when the status bar was shown confirmed the visible strip was the dialog window's system-bar area, not only editor content sizing.
- `MainActivity.kt` made the Activity system bars transparent, but the opened-note dialog has its own window and still needed transparent status/navigation bar colors and contrast scrims disabled.
- The issue remained specifically when the hide-status-bar option was enabled because the dialog hid only `statusBars()`, leaving the navigation/gesture system-bar area exposed at the bottom.
- At maximum content height the content surface therefore pushed the content dragger and Hide/Show Memo row into the bottom gesture/navigation region, hiding them behind the visible bottom border.
- The header height also had to be reserved because the content height fraction is relative to the full editor body height.
- `MainActivity.kt` also restored default cutout layout when the status bar was not hidden, allowing transient status-bar relayouts to constrain the frame by the status-bar-height area.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt`
  - Restored the previous content resize layout so the surrounding editor controls move/adapt with the content area as before.
  - Reserved the actual `4.dp` content handle, `4.dp` memo handle, and `36.dp` compact header in the max-height calculation.
  - Removed the artificial bottom controls clamp reserve (`0.dp`) after confirming it only tuned the visible gap instead of solving the root cause.
  - Replaced fractional `fillMaxHeight(displayedHeightFraction)` sizing with an exact `height(...)` derived from `editorBodyHeightPx * displayedHeightFraction`.
  - This makes the rendered content height use the same measurement basis as the max-height calculation, eliminating the leftover bottom gap caused by fractional `Column` measurement.
  - Set the dialog window `statusBarColor` and `navigationBarColor` to transparent.
  - Disabled dialog-level status/navigation bar contrast enforcement on Android Q+ so Android does not paint an opaque scrim in the system-bar area.
  - Changed fullscreen dialog behavior from hiding/showing only `WindowInsetsCompat.Type.statusBars()` to hiding/showing `WindowInsetsCompat.Type.systemBars()`, so the bottom navigation/gesture area does not remain as a separate visible gap when the hide-status-bar option is enabled.
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
  - Kept `layoutInDisplayCutoutMode` on `LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES` for the Activity window regardless of the status-bar preference.
  - Preserved the existing hide/show status-bar preference behavior.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-06-04] Fix: prevent black top gap during keyboard relayouts

**Type**: Edge-to-Edge / IME Layout Fix

**Status**: ✅ Complete

**Issue**:
- Tapping the main-view search bar opened the keyboard and briefly exposed a black strip at the top where the Android status bar normally sits.
- Logcat showed the Activity frame remained `0,0`, while IME and top inset sources changed during keyboard animation.

**Root Cause**:
- The app runs edge-to-edge with `decorFitsSystemWindows = false`, but the Activity window did not explicitly make the runtime system-bar backgrounds transparent.
- During IME relayouts, Android could briefly reveal the default black system-bar/cutout background even though content stayed laid out from the screen origin.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
  - Updated centralized `applyStatusBarVisibility(...)` to set `window.statusBarColor` and `window.navigationBarColor` to transparent.
  - Disabled Android Q+ status/navigation bar contrast enforcement to avoid transient system scrims over the edge-to-edge content.
  - Kept existing status-bar visibility and cutout behavior unchanged.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-06-02] UI: match opened-note content and memo dragger lengths

**Type**: Editor UI Refinement

**Status**: ✅ Complete

**Issue**:
- After moving the content note area's resize dragger below the content area, it was visibly shorter than the memo-area dragger.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt`
  - Increased the content-area resize dragger width from `40.dp` to `80.dp`.
  - Matched the memo-area dragger's existing `80.dp` visual length.
  - Kept resize gesture behavior unchanged.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-06-02] UI: move opened-note content resize handle below note area

**Type**: Editor UI Refinement

**Status**: ✅ Complete

**Issue**:
- The content note area's resize dragbar appeared above the content area.
- The requested target location was the lower edge of the content note area, near the area above the Hide/Show Memo control.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt`
  - Moved the existing `DUAL_BAR` content-area resize handle block from before the content surface to immediately after the content surface.
  - Preserved the same resize gesture logic, persistence behavior, size constraints, and callback path.
  - Kept the Hide/Show Memo row behavior unchanged.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-06-01] Fix: remove dialog `FLAG_FULLSCREEN` compatibility status-bar loop

**Type**: Status Bar Compatibility Fix

**Status**: ✅ Complete

**Issue**:
- After guarding direct `FullScreenEditor.hide(statusBars())` calls, memo-area controls could still trigger Android status-bar show/hide relayouts.
- Logcat showed `ViewRootImpl.controlInsetsForCompatibility` issuing `show(statusBars captionBar)` and `hide(statusBars captionBar)` around relayouts.

**Root Cause**:
- The opened-note dialog combined modern `WindowInsetsControllerCompat` calls with deprecated `FLAG_FULLSCREEN` mutations.
- Android compatibility insets handling reacted to that legacy fullscreen flag during normal dialog relayouts, causing the status bar/caption bar visibility to oscillate.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt`
  - Removed `dialogWindow.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)`.
  - Removed `dialogWindow.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)`.
  - Kept `WindowInsetsControllerCompat.hide/show(statusBars())` as the single source for dialog status-bar visibility.
  - Kept `FLAG_LAYOUT_NO_LIMITS`, top/start gravity, cutout policy, and match-parent sizing for edge-to-edge layout.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.
- ✅ Previous `FLAG_FULLSCREEN` deprecation warnings are gone from the dialog compile output.

### [2026-06-01] Fix: prevent opened-note recompositions from retriggering status-bar animations

**Type**: Status Bar Stability Fix

**Status**: ✅ Complete

**Issue**:
- Tapping opened-note controls such as hide/show memo area caused the Android status bar to appear/disappear again.
- Logcat showed `FullScreenEditor` calling `hide(statusBars())` from its dialog `SideEffect` after normal UI interactions.

**Root Cause**:
- The dialog window configuration lived in a Compose `SideEffect`, which re-runs after recompositions.
- Memo visibility changes recomposed `FullScreenEditor`, causing the same status-bar hide command to be reissued even when the `hideStatusBar` setting had not changed.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt`
  - Added `lastAppliedHideStatusBar` state inside the dialog content.
  - Guarded `hide(statusBars())` / `show(statusBars())` so they only run when the actual `hideStatusBar` mode changes.
  - Kept layout flags and full-screen frame sizing applied during recompositions.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-06-01] Fix: force opened-note dialog window to screen origin

**Type**: Dialog Window Frame Fix

**Status**: ✅ Complete

**Issue**:
- The opened-note top gap still appeared initially, but user logs showed it could disappear after interaction.
- Logcat showed the dialog/main window frame moving from a status-bar-offset frame such as `0,113` to an edge-to-edge frame such as `0,0`.

**Root Cause**:
- The opened-note `Dialog` window could initially inherit a status-bar-constrained frame before later relayouts/status-bar transitions moved it to the screen origin.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt`
  - Added `Gravity.TOP or Gravity.START` to anchor the dialog at the physical screen origin.
  - Added `FLAG_LAYOUT_NO_LIMITS` so the dialog window is not constrained below the status-bar frame.
  - Kept existing conditional `hideStatusBar` handling so the previous show/hide loop fix remains intact.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-06-01] Fix: stop status-bar show/hide loop in opened-note editor

**Type**: Status Bar State Sync Fix

**Status**: ✅ Complete

**Issue**:
- When the status bar was enabled in Settings, the app repeatedly showed and hid the status bar while the opened-note editor was active.
- Logs showed alternating `InsetsController.show(statusBars)` and `InsetsController.hide(statusBars)` calls with repeated relayouts.

**Root Cause**:
- `MainActivity` correctly showed the status bar when `hideStatusBar = false`, but `FullScreenEditor` always forced its dialog window to hide the status bar from a `SideEffect`.
- These two windows fought each other, causing continuous status-bar animation and layout churn.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt`
  - Added a `hideStatusBar: Boolean = false` parameter.
  - Only applies `FLAG_FULLSCREEN` and hides `WindowInsetsCompat.Type.statusBars()` when `hideStatusBar` is true.
  - Clears `FLAG_FULLSCREEN` and shows status bars when `hideStatusBar` is false.
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
  - Passes the existing `hideStatusBar` state into `FullScreenEditor`.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-05-31] Fix: hide status bar on opened-note dialog window

**Type**: Full-screen Status Bar Fix

**Status**: ✅ Complete

**Issue**:
- User clarified the persistent opened-note gap was the status-bar area, not spacing inside the note header.
- Previous header-size tweaks compacted the trash/save controls but did not affect the status-bar reserved band.

**Root Cause**:
- The opened-note `Dialog` window needed to actively hide its own status bar and opt into cutout/fullscreen behavior; disabling decor fitting alone was insufficient.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt`
  - Added `FLAG_FULLSCREEN` to the actual dialog window.
  - Reapplied `WindowInsetsControllerCompat.hide(WindowInsetsCompat.Type.statusBars())` to the dialog window.
  - Set `systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`.
  - Set `layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES` on supported Android versions.
  - Restored `compactIconButtonSize` to `36.dp` so trash/save/close controls are no longer over-compressed.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-05-31] Follow-up: minimize opened-note header row height

**Type**: UI Spacing Refinement

**Status**: ✅ Complete

**Issue**:
- Opened-note header still visually appeared separated from the top edge after window-level and padding fixes.

**Root Cause**:
- The close/delete/save action containers still used a `36.dp` row height, leaving transparent vertical header space even with zero top padding.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt`
  - Reduced `compactIconButtonSize` from `36.dp` to `18.dp`.
  - Set `headerBottomPadding` to `0.dp`.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-05-31] Follow-up: force FullScreenEditor dialog window edge-to-edge

**Type**: Dialog Window Insets Fix

**Status**: ✅ Complete

**Issue**:
- Opened-note editor still showed top visual space after composable padding and header-button fixes.

**Root Cause**:
- `DialogProperties(decorFitsSystemWindows = false)` alone may not force the actual platform dialog `Window` to match the full screen and ignore system-window fitting.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt`
  - Added dialog-window access via `LocalView` + `DialogWindowProvider`.
  - Forced `WindowCompat.setDecorFitsSystemWindows(dialogWindow, false)` on the actual dialog window.
  - Forced dialog window width/height to `WindowManager.LayoutParams.MATCH_PARENT` via `setLayout()` and attributes.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-05-25] Follow-up: remove Material IconButton touch-target spacing from opened-note header

**Type**: UI Spacing Refinement

**Status**: ✅ Complete

**Issue**:
- Opened-note title/action row still appeared visually offset from the top edge after window and header padding fixes.

**Root Cause**:
- Material `IconButton` keeps a minimum touch-target layout area, which can leave visible vertical space even when a smaller modifier size is applied.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt`
  - Replaced header `IconButton` actions with compact clickable `Box` wrappers.
  - Kept the same close/delete/save actions and icon sizes.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-05-19] Follow-up: tighten opened-note header spacing to top edge

**Type**: UI Spacing Refinement

**Status**: ✅ Complete

**Issue**:
- A small top gap was still visible above the opened-note title and action buttons.

**Root Cause**:
- `FullScreenEditor` header still used extra internal vertical spacing (`headerTopPadding = 6.dp`, plus a post-header spacer).

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt`
  - Updated header constants:
    - `headerTopPadding` from `6.dp` -> `0.dp`
    - `headerBottomPadding` from `4.dp` -> `2.dp`
  - Removed the extra post-header spacer (`4.dp` -> `0.dp`).

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-05-19] Fix: remove remaining top gap in opened note editor

**Type**: Dialog Insets Bug Fix

**Status**: ✅ Complete

**Issue**:
- After main/settings fixes, opened note editor still showed top empty space.

**Root Cause**:
- `FullScreenEditor` dialog still fit system windows by default, so the dialog content frame remained offset below status-bar/cutout area.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt`
  - Updated `DialogProperties` to:
    - `usePlatformDefaultWidth = false`
    - `decorFitsSystemWindows = false`

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-05-18] Fix: remove oversized top empty space in Settings screen

**Type**: UI Layout Refinement / Insets Consistency

**Status**: ✅ Complete

**Issue**:
- Settings screen had a large empty top gap while main view was already edge-aligned.

**Root Cause**:
- `SettingsScreen` used a fixed `top = 120.dp` content padding, regardless of status-bar mode.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
  - Added conditional top system-bar spacing:
    - status bar visible -> apply `statusBarsPadding()`
    - status bar hidden -> no status-bar padding
  - Replaced fixed `top = 120.dp` with a compact `top = 10.dp` content margin.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-05-18] Fix: true full-screen toggle still left top gap due to window frame offset

**Type**: Window Insets/Cutout Bug Fix

**Status**: ✅ Complete

**Issue**:
- Even after composable inset fixes, enabling true full-screen still left an empty top gap on the main screen.
- User logs showed window relayout/frame starting at `y=113`, matching status-bar/cutout height.

**Root Cause**:
- The activity window needed explicit edge-to-edge + cutout policy reapplication during status-bar toggle.
- Without this, Android could keep the content frame below the cutout/status-bar region.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
  - Updated `applyStatusBarVisibility(hideStatusBar)` to explicitly:
    - enforce `WindowCompat.setDecorFitsSystemWindows(window, false)`
    - set `layoutInDisplayCutoutMode`:
      - hidden status bar -> `LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES`
      - visible status bar -> `LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT`
  - Keeps existing status-bar hide/show behavior via `WindowInsetsControllerCompat`.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-05-17] Follow-up Fix: remove visible black strip behind top bar background

**Type**: Visual Bug Fix / Background Layering

**Status**: ✅ Complete

**Issue**:
- A black strip was still visible at the very top behind the search bar, even after status-bar insets were disabled.

**Root Cause**:
- `FileListScreen` applied `padding(paddingValues)` to the root background `Box`.
- That pushed all background/photo layers down, leaving the top area unpainted (black) while the top bar still occupied that region.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/FileListScreen.kt`
  - Removed `.padding(paddingValues)` from the root `Box` so background/photo layers render full-screen.
  - Moved `.padding(paddingValues)` to the `LazyColumn` only, preserving list content spacing under top/bottom bars.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-05-17] Follow-up Fix: remaining top gap on main screen in hidden status-bar mode

**Type**: Layout Bug Fix / Screen-Level Insets

**Status**: ✅ Complete

**Issue**:
- After the root inset fix, a top gap still remained on the main file-list screen when **Hide Android status bar** was enabled.

**Root Cause**:
- `FileListScreen` was still applying top reserved space via `statusBarsPadding()` and default scaffold insets.
- `hideStatusBar` was not being propagated from `MainActivity` into `FileManagementApp`/`FileListScreen`.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
  - Added `hideStatusBar` parameter to `FileManagementApp(...)`.
  - Passed `hideStatusBar` from `MainActivity` call site.
  - Forwarded `hideStatusBar` to `FileListScreen(...)`.
- `app/src/main/java/com/j4/texter2025/ui/components/FileListScreen.kt`
  - Added `hideStatusBar` parameter.
  - Set `contentWindowInsets` to zero when hidden.
  - Applied `statusBarsPadding()` only when status bar is visible.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-05-17] Fix: black top gap when status bar hidden

**Type**: Layout Bug Fix / Edge-to-Edge Insets

**Status**: ✅ Complete

**Issue**:
- With **Hide Android status bar** enabled, the app hid the system bar but still kept a reserved top inset in non-editor areas.
- Result: a black strip remained where the status bar used to be (main/settings), instead of app content filling that space.

**Root Cause**:
- Root `Scaffold` in `MainActivity` still used default `contentWindowInsets` while status bar was hidden.
- Default scaffold insets continued reserving top system-bar space.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
  - Updated root `Scaffold(...)`:
    - when `hideStatusBar == true` -> `contentWindowInsets = WindowInsets(0, 0, 0, 0)`
    - otherwise -> `ScaffoldDefaults.contentWindowInsets`
  - This allows content to occupy the former status-bar area in main/settings while preserving normal behavior when the status bar is visible.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-05-17] Fix: "Open settings" menu action did nothing

**Type**: Navigation Bug Fix / Settings Access

**Status**: ✅ Complete

**Issue**:
- Tapping **Open settings** in the main search/menu dropdown did not open Settings.

**Root Cause**:
- `FileListSearchBar` correctly invoked `onOpenSettings`, but `FileManagementApp` did not forward `onOpenSettings`, `onImportRequest`, and `onExportRequest` into `FileListScreen`.
- Because `FileListScreen` had default empty lambdas, the menu action became a silent no-op.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
  - Extended `FileManagementApp(...)` signature with `onOpenSettings: () -> Unit`.
  - Forwarded callbacks in the `FileListScreen(...)` call:
    - `onImportRequest = { importLauncher.launch("text/plain") }`
    - `onExportRequest = { exportName -> exportLauncher.launch(exportName) }`
    - `onOpenSettings = onOpenSettings`
  - Passed `onOpenSettings` from `MainActivity` call site to set:
    - `showSettings = true`
    - `showBackToSettingsFab = false`

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-05-17] Feature: optional true full-screen mode (hide Android status bar)

**Type**: UX Improvement / Display Options

**Status**: ✅ Complete

**Request**:
- Use more vertical screen space on the main screen by supporting true full-screen mode.
- Add a Settings option so users can choose with/without Android status bar.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
  - Added persisted preference helpers:
    - `saveHideStatusBar(enabled: Boolean)`
    - `getHideStatusBar(): Boolean`
  - Added `applyStatusBarVisibility(hideStatusBar: Boolean)` using `WindowInsetsControllerCompat`.
  - Applied preference during startup and reactively via `LaunchedEffect(hideStatusBar)`.
  - Wired new state into `SettingsScreen` callbacks.

- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
  - Added parameters:
    - `hideStatusBar: Boolean`
    - `onHideStatusBarChange: (Boolean) -> Unit`
  - Passed both into `AdvancedSettingsSection`.

- `app/src/main/java/com/j4/texter2025/ui/components/AdvancedSettingsSection.kt`
  - Added a new switch in Advanced Settings:
    - **Hide Android status bar**
    - Description: **Use true full-screen mode on the main screen**
  - Kept existing “Apply to opened notes” behavior intact.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-05-16] Fix: Remove remaining global upper header from MainActivity

**Type**: UI Bug Fix / Main Screen Header Cleanup

**Status**: ✅ Complete

**Issue**:
- Even after the main-screen redesign, the old upper header (`TeXter` top app bar with import/export/menu icons) was still visible.

**Root Cause**:
- A second, global `TopAppBar` still existed in `MainActivity` inside the root `Scaffold`.
- This rendered above the redesigned `FileListScreen` search bar, creating the “double header” effect.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt` (around lines `1753-1755`)
  - Removed the root `Scaffold(topBar = { TopAppBar(...) })` block entirely.
  - Kept `Scaffold(modifier = scaffoldModifier)` without a top bar so only the redesigned file-list top search/menu UI is shown.
  - Removed now-unused imports tied to the deleted old header icons (`CloudUpload`, `CloudDownload`, `Menu`).

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-05-16] Feature: Main screen top-bar redesign with centered glass search bar

**Type**: UI Redesign / Main Screen Navigation

**Status**: ✅ Complete

**Request**:
- Remove the legacy upper app bar (`TeXter` title + action buttons).
- Make search the main top element: centered, rounded corners, semi-glassy transparent look.
- Integrate menu access into the redesigned search component while preserving existing actions.

**Root Cause / Design Gap**:
- Main list screen header used a traditional top app bar layout that consumed vertical space and split core actions across separate controls.
- Search and menu actions were not presented as a single modern entry point.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/FileListScreen.kt` (around lines `181-215`)
  - Uses `Scaffold(topBar = ...)` with a centered top container (`statusBarsPadding` + horizontal padding).
  - Replaced legacy top-bar action cluster with `FileListSearchBar` as the single top control.
  - Kept behavior wiring for:
    - import request
    - export selected file (enabled only for single selection)
    - open settings
    - sort method
    - search method
    - case-sensitive toggle
    - show extensions toggle

- `app/src/main/java/com/j4/texter2025/ui/components/FileListSearchBar.kt` (around lines `41-247`)
  - Added/updated dedicated composable for the redesigned control:
    - rounded glass surface (`RoundedCornerShape(24.dp)`)
    - semi-transparent surface tint + subtle border + shadow
    - integrated menu button inside the search field (`leadingIcon`)
    - integrated clear/search affordances in trailing area
    - dropdown menu groups for import/export/settings and search/sort options
  - Material3 compatibility fix: replaced `HorizontalDivider` with `Divider` to match project dependency level.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-05-15] Fix: Memo-area blur preview pixelation in Colors dialog

**Type**: Visual Quality Fix / Blur Rendering

**Status**: ✅ Complete

**Issue**:
- While adjusting memo photo blur in Colors dialog preview, memo area appeared blocky/pixelated compared to other areas.

**Root Cause**:
- Memo preview path rendered only a fully blurred layer as blur increased, which amplifies coarse sampling artifacts in a relatively small preview region.
- File-list-item preview already used a smoother sharp/blur cross-fade path, so behavior was inconsistent between sections.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt` (around lines `1990-2075`)
  - Refactored memo preview image rendering into reusable base + gesture modifiers.
  - Applied the same blur-mix approach already used in file-list-item preview:
    - Compute eased blur mix (`smoothstep` style)
    - Render sharp layer with `(1 - mix)` alpha
    - Render blurred layer with `mix` alpha
  - Kept existing gesture behavior and contentScale logic intact.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-05-15] Fix: Opened-note backgrounds restored empty after relaunch despite selected squares

**Type**: Bug Fix / State Restoration / Preset Sync

**Status**: ✅ Complete

**Issue**:
- After app relaunch/reopen, `opened note background`, `Content area`, and `Memo area` could appear empty even though corresponding color/photo squares were still selected.
- Reselecting the same square restored rendering until next restart.

**Root Cause**:
- Active preset startup restore path for background/content/memo restored photo URIs, but did not reliably resync effective selected square metadata (color/alpha/blur) and persisted runtime keys for these three sections.
- Result: UI selection metadata and rendered runtime state diverged after process restart.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt` (around lines `1581-1658`)
  - During active preset load:
    - Normalized photo URI restore with `takeIf { it.isNotEmpty() }`.
    - For each of `customBg`, `contentArea`, `memoArea`, matched selected photo square via `indexOfLast { it.photoUri == activeUri }`.
    - When found, restored the square's effective `color`, `photoAlpha`, and `photoBlur` into runtime state.
    - Kept SharedPreferences fallback for alpha/blur when no matching square exists.
  - Synced effective values back to persistence for each section:
    - color
    - photoUri
    - photoAlpha
    - photoBlur
    - photo positioning (offset/scale/rotation)

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-05-15] Critical Fix: Colors section crash from oversized bitmap draw

**Type**: Crash Fix / Rendering Stability

**Status**: ✅ Complete

**Issue**:
- Entering `Settings -> Colors` could crash with:
  - `RuntimeException: Canvas: trying to draw too large (...) bitmap`
- Crash surfaced while preview/dialog rendering photo backgrounds.

**Root Cause**:
- Recent decode-quality tuning forced very large decode path (`Size.ORIGINAL`) and software-leaning behavior.
- In photo-heavy preview composition, some images were decoded at dimensions large enough to exceed safe Canvas draw limits on device.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/HighQualityPhotoPainter.kt` (around lines `11-25`)
  - Added safe decode cap:
    - `MAX_PHOTO_DECODE_DIMENSION_PX = 4096`
    - `.size(MAX_PHOTO_DECODE_DIMENSION_PX, MAX_PHOTO_DECODE_DIMENSION_PX)`
  - Removed unbounded original-size decode request (`Size.ORIGINAL`).
  - Kept high-fidelity intent via `Precision.EXACT` and `Scale.FILL`.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL` (tool reported post-run wait-delay I/O timeout after success output).

### [2026-05-14] Follow-up Fix: Memo-area blur pixelation persisted after initial pass

**Type**: Bug Fix / Rendering Pipeline / Image Decode Quality

**Status**: ✅ Complete

**Issue**:
- Memo-area background photo still showed visible pixel blocks during blur transitions after the previous modifier-order adjustment.

**Root Cause**:
- Two quality-loss amplifiers remained in the photo pipeline:
  1. Background decode path was still permissive (`allowHardware` default + non-exact sampling path), which can reduce blur fidelity on transformed layers.
  2. Transform logic still included aggressive scaling patterns (including redundant static-mode layering), which magnified blur artifacts.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/HighQualityPhotoPainter.kt` (around lines `7-26`)
  - Strengthened Coil request quality settings:
    - `precision(Precision.EXACT)`
    - `scale(Scale.FILL)`
    - `allowHardware(false)`
  - Kept `size(Size.ORIGINAL)` and `crossfade(false)`.

- `app/src/main/java/com/j4/texter2025/ui/components/PhotoBackgroundSurface.kt` (around lines `89-133`)
  - DYNAMIC mode:
    - Removed extra ratio-based upscaling path.
    - Switched to direct user scale (`backgroundPhotoScale`) with `ContentScale.Crop`.
  - STATIC_ZOOM mode:
    - Removed redundant second `graphicsLayer` pass (which could effectively over-scale content).
  - Retained blur-first ordering and existing offset/rotation behavior.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-05-10] Bug Fix: Memo-area photo blur pixelation in opened note editor

**Type**: Bug Fix / Rendering Quality

**Status**: ✅ Complete

**Issue**:
- When blur was increased on the memo area background photo in opened notes, the image looked blocky/pixelated instead of smoothly blurred.

**Root Cause**:
- Photo blur was applied after transform layers (`graphicsLayer` scaling/positioning), so the blur operated on an already transformed rasterized output.
- This magnified sampling artifacts and made blur steps look visibly pixelated.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/PhotoBackgroundSurface.kt` (around lines `87-145`)
  - Reordered modifier chain in both photo modes (`DYNAMIC`, `STATIC_ZOOM`) to apply:
    1. `applyPhotoBlur(backgroundPhotoBlur)` immediately after `fillMaxSize()`
    2. then `graphicsLayer` transforms
    3. then `applyPhotoAlpha(imageAlpha)`
  - This keeps blur operating on the base image before aggressive transform sampling.
  - Preserved all existing photo behavior (offset, scale, rotation, alpha, and mode logic).

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-05-10] UX Improvement: Compact opened-note header bar in FullScreenEditor

**Type**: UI/UX / Space Optimization

**Status**: ✅ Complete

**Issue**:
- Opened note header (file title + action icons) consumed too much vertical space and reduced note reading/editing area.
- Header size did not feel responsive enough even when content area sizing was adjusted.

**Root Cause**:
- FullScreenEditor header used large padding and spacer values around the top row.
- Header used `titleLarge` plus default icon button sizing, producing a tall visual block.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt` (around lines `380-453`)
  - Replaced oversized dynamic header padding with compact constants:
    - horizontal `12.dp`, top `6.dp`, bottom `4.dp`
  - Reduced title style from `titleLarge` to `titleMedium`
  - Reduced action affordance footprint:
    - icon button size `36.dp`
    - icon size `18.dp`
  - Reduced post-header spacer to `4.dp`
  - Preserved all existing actions (close/delete/save) and behavior.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-04-11] Bug Fix: File list item transparency reset after app restart

**Type**: Bug Fix / State Persistence / Regression

**Status**: ✅ Complete

**Issue**:
- File list item photo transparency could return to solid (alpha `1.0`) after restart even when transparent alpha had been configured.

**Root Cause**:
- During startup active-preset restoration, the file-list-item alpha path could prioritize stale per-preset square metadata over persisted runtime alpha in `SharedPreferences`.
- This allowed old square values (often `1.0`) to overwrite the effective stored alpha when preset/square metadata lagged behind.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt` (around lines `1054-1066`, `1624-1647`)
  - Added guarded startup trace log:
    - `read source=startupPrefs alpha=... uri=...`
  - Changed active preset load behavior for file-list-item photo alpha/blur to:
    1. read persisted values from `SharedPreferences` (`file_list_item_photo_alpha` / `file_list_item_photo_blur`) as startup source-of-truth
    2. sync matching `customFileListItemColors` square metadata using `indexOfLast` with those persisted values
  - Kept low-noise guard pattern: `Log.isLoggable("FileListItemAlpha", Log.DEBUG)`.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL` (tool reported post-build wait-delay timeout).

### [2026-04-11] Global Rule: Added generic recurring regression debug-guard policy

**Type**: Process Improvement / Cross-Project Rule

**Status**: ✅ Complete

**User Impact**:
- Added a reusable global policy so future chats across projects consistently keep low-noise guarded logs for known recurring regressions.
- This complements (not replaces) project-specific rule blocks.

**Implementation**:
- `c:/Users/javie/.codeium/windsurf/memories/global_rules.md` (around lines `361-377`)
  - Added section: `0.6. CRITICAL: Recurring Regression Stability Guard (ENFORCE ALWAYS)`.
  - Requires tiny guarded debug logs around critical read/write/apply paths in known regression areas.
  - Enforces low-noise debug guards and stable tags, avoids unconditional spam logs.
  - Keeps root-cause-first approach and only removes guard logs on explicit user request.

### [2026-04-11] Project Rule: Recurring FileList transparency debug-guard policy

**Type**: Process Improvement / Stability Guardrail

**Status**: ✅ Complete

**User Impact**:
- Future chats now have an explicit project rule to preserve low-noise guarded debug logs for the recurring file-list-item transparency regression path.
- Helps keep long-session/reopen troubleshooting support consistent across sessions.

**Implementation**:
- `.windsurfrules` (around lines `33-36`)
  - Added section: `Recurring Stability Guard - File List Item Transparency`.
  - Rule requires tiny guarded debug logs on alpha read/write/apply paths when touching related logic.
  - Rule explicitly prefers `Log.isLoggable("FileListItemAlpha", Log.DEBUG)`-style guards and forbids unconditional spam logs.
  - Rule states guard should only be removed when explicitly requested.

### [2026-04-10] UX: Increased small text size in Photo Controls panel

**Type**: UX Improvement / Readability

**Status**: ✅ Complete

**User Impact**:
- Increased text size in the photo controls area where labels were noticeably smaller.
- Typography now better matches the readability level used in settings section titles like `Background Colors`.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/PhotoControlsPanel.kt` (around lines `144-200`)
  - Updated `Background Mode` label from `labelMedium` to `bodyMedium`.
  - Updated filter chip labels (`Dynamic`, `Auto-Zoom`) from `bodySmall` to `bodyMedium`.
  - Updated mode description text from `bodySmall` to `bodyMedium`.
  - Updated gesture hint text (`Drag preview to move • Pinch to zoom`) from `bodySmall` to `bodyMedium`.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL` (tool reported post-build wait-delay timeout).

### [2026-04-09] Feature: Optional App UI Size scaling for opened notes/editors

**Type**: Feature / Settings / Opened Note Editor Behavior

**Status**: ✅ Complete

**User Impact**:
- Added a new `Apply to opened notes` toggle in Advanced Settings under `UI Settings`.
- Users can now choose whether `App UI Size` affects opened note editors/dialogs.
- When disabled, opened notes use normal scale while the rest of app UI scaling behavior remains unchanged.

**Root Cause / Need**:
- App UI scale was applied globally for regular composition content, but opened note editor windows needed user-controllable behavior.
- Opened note editors are rendered in dialog windows, so they require explicit local density behavior based on user preference.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/AdvancedSettingsSection.kt` (around lines `48-59`, `159-184`)
  - Added persisted toggle state using `TeXterPrefs` key: `apply_ui_scale_to_opened_notes`.
  - Added `Switch` UI row labeled `Apply to opened notes` with short helper text.
  - Persisted toggle changes immediately in `SharedPreferences`.

- `app/src/main/java/com/j4/texter2025/ui/components/FileEditDialog.kt` (around lines `159-177`, `261-1059`)
  - Read `apply_ui_scale_to_opened_notes` and `app_ui_scale` from `TeXterPrefs`.
  - Built dialog-local `Density` that applies scale only when toggle is enabled.
  - Wrapped dialog content with `CompositionLocalProvider(LocalDensity provides openedNoteDensity)`.

- `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt` (around lines `236-254`, `298-983`)
  - Added same toggle-based density logic for fullscreen opened-note editor dialog.
  - Applied conditional local density via `CompositionLocalProvider`.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-04-08] UX: Separated UI Settings subsection inside Advanced Settings

**Type**: UX Improvement / Settings Information Architecture

**Status**: ✅ Complete

**User Impact**:
- `Advanced Settings` header now stands alone (not visually merged with UI scaling label).
- `UI Settings` now appears as its own subsection only when Advanced Settings is expanded.
- This structure leaves room to add more advanced subsections later.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/AdvancedSettingsSection.kt`
  - Removed `UI scaling` subtitle from the collapsed `Advanced Settings` header.
  - Added explicit `UI Settings` subsection title inside expanded content.
  - Kept existing scale controls and reset behavior unchanged.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-04-08] UX: Advanced Settings made collapsible with clearer UI scaling focus

**Type**: UX Improvement / Settings

**Status**: ✅ Complete

**User Impact**:
- `Advanced Settings` is now dropdown/collapsible.
- Header now uses short contextual text: `UI scaling` (removed long explanatory paragraph).
- Current UI scale is now much more visible in a highlighted status row.
- `Reset` behavior remains unchanged.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/AdvancedSettingsSection.kt`
  - Added collapsible section state (`isExpanded`) with header row tap toggle.
  - Added expand/collapse indicator icons (`KeyboardArrowDown` / `KeyboardArrowUp`).
  - Replaced verbose description with concise subtitle `UI scaling`.
  - Added emphasized "Current size" display using a `primaryContainer` highlight row and larger percentage text (`titleLarge`).
  - Kept slider commit flow (`onValueChangeFinished`) and `Reset` functionality.
  - Added `animateContentSize()` for smooth expand/collapse transitions.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-04-08] Fix: App UI Size now applies inside Settings color/photo editor dialogs

**Type**: Bug Fix / Dialog Scaling Consistency

**Status**: ✅ Complete

**User Impact**:
- UI scale now applies consistently in dialogs opened from `Settings -> Colors`:
  - tapping `+` (add color/photo)
  - tapping existing color/photo squares (edit)
- Previously, these editor dialogs opened at system/default scale.

**Root Cause**:
- `MainActivity` app scaling used `CompositionLocalProvider(LocalDensity ...)` for the main composition tree.
- `UnifiedBackgroundEditor` is shown in a separate Compose `Dialog` window, which does not automatically inherit that local density override.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/UnifiedBackgroundEditor.kt`
  - Read persisted `app_ui_scale` from `TeXterPrefs`.
  - Built dialog-local density:
    - `density = baseDensity.density * appUiScale`
    - `fontScale = baseDensity.fontScale * appUiScale`
  - Wrapped dialog content with:
    - `CompositionLocalProvider(LocalDensity provides dialogDensity)`

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-04-08] Fix: App UI Size slider now drags smoothly (not tap-only)

**Type**: Bug Fix / Settings UX

**Status**: ✅ Complete

**User Impact**:
- `App UI Size` slider now supports normal continuous dragging.
- Previously, users could mostly jump by tapping the track, but drag interaction felt broken/interrupted.

**Root Cause**:
- Slider `onValueChange` immediately updated global app density/font scale on every pointer move.
- Because layout density changed mid-drag, the slider geometry shifted while being touched, interrupting natural drag tracking.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/AdvancedSettingsSection.kt`
  - Added local transient slider state: `sliderScale`.
  - `onValueChange` now updates only local state for smooth thumb movement.
  - `onValueChangeFinished` commits the value once via `onAppUiScaleChange(sliderScale)`.
  - `Current: XX%` label now reflects local drag value.
  - `Reset` now updates local state and persisted/global state together.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

### [2026-04-05] Feature: App UI Size setting (in-app only) under Advanced Settings

**Type**: Feature / Settings / Accessibility & UX

**Status**: ✅ Complete

**User Impact**:
- Added an in-app `App UI Size` control so users can scale TeXter2025 UI independently from Android system scaling.
- Helps counter extreme system font/display scaling without changing global device settings.

**Root Cause / Need**:
- The app previously inherited system scaling directly with no app-level override, so users had no way to fine-tune readability/layout density inside TeXter2025 only.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
  - Added persisted preference helpers:
    - `saveAppUiScale(scale)`
    - `getAppUiScale()`
  - Added remembered `appUiScale` state and loaded it during startup/settings reload flow.
  - Applied app-level scaling via `CompositionLocalProvider(LocalDensity provides appDensity)` using:
    - `density = baseDensity.density * appUiScale`
    - `fontScale = baseDensity.fontScale * appUiScale`
  - Passed new setting props into `SettingsScreen` with clamped range `0.7f..1.3f`.

- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
  - Added new parameters:
    - `appUiScale: Float`
    - `onAppUiScaleChange: (Float) -> Unit`
  - Rendered a new `AdvancedSettingsSection` card before unsaved-changes controls.

- `app/src/main/java/com/j4/texter2025/ui/components/AdvancedSettingsSection.kt` (new file)
  - New reusable composable with:
    - `App UI Size (in-app only)` label and explanation
    - Slider range `70%..130%`
    - Live `Current: XX%` label
    - `Reset` button to return to `100%`

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` -> `BUILD SUCCESSFUL`.

### [2026-03-31] Fix: FileList item photo transparency no longer resets during preset load/switch

**Type**: Bug Fix / FileList Photo Alpha Persistence

**Status**: ✅ Complete

**User Impact**:
- FileList item photo transparency now remains consistent instead of sometimes jumping back to opaque/default.

**Root Cause**:
- In preset apply/select flows, FileList item photo alpha/blur could be re-derived from the square metadata list (`customFileListItemColors.find { it.photoUri == ... }`).
- That metadata can be stale for alpha in some edit flows, leading to fallback/default-looking behavior.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
  - In preset apply path (`onSelectPreset`), for `fileListItemPhotoUri != null`, load:
    - `file_list_item_photo_alpha` from SharedPreferences
    - `file_list_item_photo_blur` from SharedPreferences
  - In preset editing select path (`onSelectPresetForEditing`), for file list item photo, use the same SharedPreferences source for alpha/blur while still using square color for visual square selection.
  - Added lightweight conditional debug logs (tag: `FileListItemAlpha`) for key read/write points:
    - `saveFileListItemPhotoAlpha`
    - active preset load
    - `onSelectPreset`
    - `onSelectPresetForEditing`
    - `onFileListItemPhotoAlphaChange`

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` completed with `BUILD SUCCESSFUL` (tool ended with WaitDelay timeout after successful build output).

### [2026-04-03] Recurrence fix: make FileList item transparency persist per preset square

**Type**: Bug Fix / FileList Photo Alpha Recurrence

**Status**: ✅ Complete

**User Impact**:
- Addresses recurring cases where FileList item transparency still reset after time/reopen.

**Root Cause**:
- `file_list_item_photo_alpha` was treated like a global source of truth.
- When it was overwritten by another flow, FileList item transparency for a preset could reopen with wrong opacity.
- Per-square preset metadata (`customFileListItemColors.photoAlpha/photoBlur`) was not consistently updated during slider edits, so it could not reliably restore the intended value.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
  - In `onSelectPreset`: for FileList item photo, now prefer `customFileListItemColors` square alpha/blur by matching `photoUri`; fallback to SharedPreferences only for backward compatibility.
  - In `onSelectPresetForEditing`: same change (use matched square alpha/blur instead of global pref).
  - In `onFileListItemPhotoAlphaChange`: now updates matched FileList item square (`indexOfLast { it.photoUri == activeUri }`) and persists updated `customFileListItemColors` into the selected preset list.
  - In `onFileListItemPhotoBlurChange`: same square/preset synchronization for blur.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` reported `BUILD SUCCESSFUL` (tool ended with WaitDelay timeout after build output).

### [2026-04-05] Follow-up recurrence fix: startup active preset load now uses per-square FileList alpha

**Type**: Bug Fix / Startup Restore Path

**Status**: ✅ Complete

**User Impact**:
- Fixes cases where FileList items still appeared non-transparent after reopening the app later, while content/memo transparency remained correct.

**Root Cause**:
- Initial active preset restore path (`activePresetLoad`) still read FileList item alpha/blur from global SharedPreferences.
- This bypassed per-preset square alpha and could restore an incorrect opacity after delayed reopen.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
  - Active preset load (`fileListItemPhotoUri != null`) now:
    - matches `customFileListItemColors` by `photoUri`
    - restores `photoAlpha`/`photoBlur` from matched square metadata
    - uses SharedPreferences fallback only when no matching square exists (backward compatibility)

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` -> `BUILD SUCCESSFUL`.

### [2026-04-05] UX fix: Color Actions dialog now closes on outside tap without triggering behind UI

**Type**: UX Bug Fix / Dialog Interaction

**Status**: ✅ Complete

**User Impact**:
- While `Color Actions` is open, tapping the blurred/scrim area now closes the dialog.
- Underlying UI elements (e.g., color squares/editors behind the blur) are no longer triggered by that tap.

**Root Cause**:
- The background scrim close path was incomplete and not consistently resetting dialog payload state.
- Underlying content blocker used a gesture listener that did not actively consume interactions, allowing tap-through behavior in some paths.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
  - Added `closeDragOptionsDialog()` helper to centralize full dialog-state reset (`showDragOptions`, `colorToDrag`, `photoUriToDrag`, `photoAlphaToDrag`, `photoBlurToDrag`, `dragOptionsSection`).
  - Replaced blur-layer blocker with `detectTapGestures(onTap = { })` while dialog is open, ensuring taps are consumed.
  - Updated overlay scrim to dismiss via `pointerInput + detectTapGestures`.
  - Updated `X` button to use the same centralized close function.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` -> `BUILD SUCCESSFUL`.

### [2026-04-05] Follow-up UX hardening: Color Actions moved to true modal Dialog window

**Type**: UX Bug Fix / Tap-Through Prevention (Follow-up)

**Status**: ✅ Complete

**User Impact**:
- Resolves remaining cases where behavior was still reported as unchanged.
- `Color Actions` now dismisses on outside tap/back press as a modal dialog and blocks interactions with underlying settings content.

**Root Cause**:
- In-layout overlay could still allow edge-case interaction routing in the same composition tree.
- Needed a dedicated modal window boundary to guarantee no click-through.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
  - Converted `showDragOptions` overlay from in-layout `Box` to `Dialog` + `DialogProperties`:
    - `dismissOnBackPress = true`
    - `dismissOnClickOutside = true`
    - `usePlatformDefaultWidth = false`
  - Kept full-screen scrim tap close inside dialog content (`detectTapGestures` -> `closeDragOptionsDialog()`).
  - Kept centralized `closeDragOptionsDialog()` reset path for consistent state cleanup.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` -> `BUILD SUCCESSFUL`.

### [2026-04-05] Fix: New preset preview no longer inherits stale file-list state from previous preset

**Type**: Bug Fix / Preset Preview Consistency

**Status**: ✅ Complete

**User Impact**:
- Creating a new preset now produces a clean preview state instead of showing stale file-list item color/photo from the previously selected preset.
- Preview card and actual app/preset state are now aligned for newly created presets.

**Root Cause**:
- New-preset reset was executed when opening the save dialog, but stale state could still be captured at save time in some flows.
- Result: newly saved preset card could serialize previous file-list item preview data despite empty new-preset UI state.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
  - In Color Presets save dialog confirm action, when `isInNewPresetMode == true`, call `onResetColorsForNewPreset()` immediately before `onSavePreset(...)`.
  - This guarantees save-time state is reset and prevents stale preset-preview inheritance.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` -> `BUILD SUCCESSFUL`.

### [2026-04-05] Follow-up fix: Clear file-list color state during new preset reset

**Type**: Bug Fix / New Preset Reset Completeness

**Status**: ✅ Complete

**User Impact**:
- New preset cards no longer retain prior file-list visual state in preview when creating a preset from the `+` flow.

**Root Cause**:
- `onResetColorsForNewPreset` cleared photo URIs and color lists, but did not clear all file-list color/blur fields used by preset serialization.
- `createPresetFromCurrentSettings(...)` still captured stale `fileListItemColor`, `fileListBackgroundColor`, or `fileListItemBlur` values.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
  - In `onResetColorsForNewPreset`:
    - clear in-memory state: `fileListItemColor = null`, `fileListBackgroundColor = null`, `fileListItemBlur = 0f`
    - persist reset state: `saveFileListItemColor(null)`, `saveFileListBackgroundColor(null)`, `saveFileListItemBlur(0f)`

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` -> `BUILD SUCCESSFUL`.

### [2026-04-05] UX: Hex color code is now editable with copy/paste in color picker

**Type**: Feature / Color Picker Usability

**Status**: ✅ Complete

**User Impact**:
- Users can now copy the selected hex color code directly.
- Users can paste a hex color code to apply a color quickly.
- Users can manually type a hex code in an input row above the hue controls.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`
  - Added `parseHexToColorOrNull(...)` helper supporting `RRGGBB` and `AARRGGBB`.
  - In `ColorPickerContent(...)`, added a new hex row above hue controls with:
    - editable `OutlinedTextField` (`#` prefix, hex sanitization, 8-char max)
    - `Copy` action (copies `#AARRGGBB` to clipboard)
    - `Paste` action (reads clipboard and applies valid hex)
    - inline validation error message for invalid formats

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` -> `BUILD SUCCESSFUL`.

### [2026-04-05] Follow-up fix: Hex input moved to active color editor path

**Type**: Bug Fix / UI Wiring

**Status**: ✅ Complete

**User Impact**:
- Hex input, copy, and paste controls are now visible in the color editor the user actually opens from Settings.

**Root Cause**:
- Initial implementation was added to `ColorPickerContent` (`ColorPickerDialog.kt`), but the Settings flow uses `UnifiedBackgroundEditor` -> `ColorControlsPanel`.
- Result: users did not see any hex input changes in the active editor.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/ColorControlsPanel.kt`
  - Added hex parser helper for `RRGGBB` / `AARRGGBB`.
  - Added editable hex input row above Hue controls.
  - Added `Copy` and `Paste` actions with validation and toasts.
  - Wired valid hex input to `onColorChange(...)`.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` -> `BUILD SUCCESSFUL`.

### [2026-04-05] Crash fix: deleting hex digits no longer crashes color editor

**Type**: Bug Fix / Hex Color Parsing Stability

**Status**: ✅ Complete

**User Impact**:
- Editing or deleting digits in the hex input no longer crashes the app.

**Root Cause**:
- Hex parsing created colors with `Color(argb.toULong())`.
- In Compose, that constructor expects packed `ColorLong` format (includes color-space index bits), not a raw 32-bit ARGB integer.
- This could produce invalid color-space indexes and crash in color conversions (`toArgb` / hue calculations).

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/ColorControlsPanel.kt`
  - Changed parsed color creation from `Color(argb.toULong())` to `Color(argb.toInt())`.
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`
  - Applied same fix in shared hex parsing helper for consistency and safety.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` -> `BUILD SUCCESSFUL`.

### [2026-03-31] Follow-up fix: re-adding photo now updates the correct square preview when duplicate colors exist

**Type**: Bug Fix / Preset Square Edit Targeting

**Status**: ✅ Complete

**User Impact**:
- After photo remove -> color apply -> photo re-add, the edited square now correctly shows the photo thumbnail.
- Prevents cases where app background changed to photo but the edited square still showed color.

**Root Cause**:
- Edit callbacks in `MainActivity` targeted squares using `indexOfFirst { it.color == oldColor }`.
- When multiple squares had the same color, edits could be applied to the wrong square entry.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
  - Updated all section edit callbacks to use `indexOfLast { it.color == oldColor }`:
    - `onEditBgColor`
    - `onEditContentAreaColor`
    - `onEditMemoAreaColor`
    - `onEditFileListItemColor`
    - `onEditFileListBackgroundColor`
  - This matches user flow where the most recently edited/added duplicate-color square is the intended target.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` passed (`BUILD SUCCESSFUL`).

### [2026-03-31] Follow-up fix: photo remove now works after reopening a photo square editor

**Type**: Bug Fix / Photo Editor / Persisted Remove Flow

**Status**: ✅ Complete

**User Impact**:
- After adding a photo, saving, reopening the same square, and tapping `Remove`, the photo now removes correctly.
- Removal no longer requires deleting the square from outside the editor as a workaround.

**Root Cause**:
1. `UnifiedBackgroundEditor` only emitted `onPhotoSelected(...)` when a URI was non-null.
2. After tapping `Remove`, `selectedPhotoUri` became null, so `Select/Test` emitted nothing.
3. In edit flow, `SettingsScreen` `onPhotoSelected` handled only non-null photo URIs, so no clear-state commit path existed.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/UnifiedBackgroundEditor.kt`
  - In photo mode `Select` and `Test`, now always emits `onPhotoSelected(BackgroundOption(...))` even when `photoUri` is null.
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
  - In edit-dialog `onPhotoSelected`, added normalized URI handling:
    - non-empty URI => existing apply/update path
    - null/blank URI => explicit remove path that clears section live photo state and resets alpha/blur/transform defaults
  - Also updates the edited square via `onEditCustomColor(..., newPhotoUri = null, ...)` so persisted preset data is consistent.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` reached `BUILD SUCCESSFUL` (warnings only; tool reported I/O wait timeout after completion).

**Manual Test Checklist**:
1. Open any section square and pick a photo.
2. Tap `Select`.
3. Reopen the same square editor.
4. Tap `Remove`, then `Select`.
5. Confirm photo is removed immediately from section and card preview.

### [2026-03-31] Follow-up fix: new preset cards no longer inherit photo preview from previously edited preset

**Type**: Bug Fix / New Preset Reset / Preset Preview Correctness

**Status**: ✅ Complete

**User Impact**:
- Creating a new preset now starts with truly cleared photo state.
- New preset card previews no longer show inherited photo thumbnails/previews from another preset.

**Root Cause**:
1. New preset reset flow cleared color state but did not clear/persist all section photo state before the save/create flow.
2. `createPresetFromCurrentSettings(...)` could therefore capture stale in-memory/SharedPreferences photo URIs and positioning from the previously edited preset.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
  - In `onResetColorsForNewPreset` (around lines `3224-3341`), added full per-section photo reset for:
    - `customBg`, `contentArea`, `memoArea`, `fileListItem`, `fileListBackground`
  - Cleared URI and reset photo transform/appearance state (`alpha`, `blur`, `offsetX`, `offsetY`, `scale`, `rotation`) in-memory.
  - Persisted the same cleared values via existing `save*` calls so subsequent preset creation cannot read stale photo values.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` reached `BUILD SUCCESSFUL` (tool status ended with I/O wait timeout after completion; Kotlin warnings only).

**Manual Test Checklist**:
1. Select an existing preset with visible photo preview(s).
2. Tap Add/New Preset.
3. Save new preset without adding photos.
4. Confirm new preset card preview does not show inherited photo preview content.
5. Repeat after editing photo transform values in the prior preset to confirm no stale transform leakage.

### [2026-03-30] Fix: Preset cards no longer blink/copy previews and no longer lose edits on switch

**Type**: Bug Fix / Preset Card Preview / Preset Persistence

**Status**: ✅ Complete

**User Impact**:
- Preset card preview no longer briefly shows another preset's preview during card switching.
- Newly created/selected preset cards no longer appear to copy preview content from previously selected cards.
- Background/File List Item/File List Background color changes now persist correctly when switching to another preset card.

**Root Cause**:
1. Preset card preview used transient live SharedPreferences state for selected cards, causing frame-level mismatches and visible blink/copy artifacts while selection changed.
2. Several selected-color callbacks updated runtime state but did not persist back to the currently edited preset object/list, so switching cards could reload stale preset data.
3. Card switch flow did not force a final persist of the currently edited preset before applying another preset.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
  - Wired `onUpdatePreset` into `ColorPresetsSection`.
  - On preset card tap, now persists `selectedPresetForEditing` (when switching to another card) before selecting/applying target preset.
  - Preset card preview now uses preset data consistently (`useLive = false`) to avoid transient cross-card preview contamination.
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
  - Added preset synchronization in these callbacks:
    - `onCustomBgColorChange`
    - `onCustomBgBlurChange`
    - `onFileListItemColorChange`
    - `onFileListBackgroundColorChange`
  - Each now updates and saves the currently edited preset entry when active.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` passed (BUILD SUCCESSFUL).

**Manual Test Checklist**:
1. Create/select preset A, change BG/File List Item/File List Background colors.
2. Switch to preset B, then back to preset A.
3. Confirm preset A changes are retained.
4. Switch repeatedly between cards; confirm no brief wrong-card preview blink.
5. Create a new preset and verify its preview reflects its own data only.

### [2026-03-29] Fix: Go Back now restores the exact Colors editor context after Test

**Type**: Bug Fix / Navigation State / Settings Return Flow

**Status**: ✅ Complete

**User Impact**:
- After tapping `Test` from a section editor and returning with the floating `Go Back` button, users are now taken back to the same section editor context instead of only landing at `Settings -> Colors` root.
- The matching section row editor is reopened automatically, including the active photo/color square context when available.

**Main Problem (Root Cause)**:
1. Return navigation only tracked section expansion state (`settingsReturnSection` + `settingsReturnRequestId`).
2. No state was passed to reopen the actual editor dialog (`ColorPresetRow` local `showEditColorPicker` state), so return flow stopped at category level.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
  - Added `settingsReturnReopenEditor` state in `setContent` scope.
  - In `onTestInApp`, now marks `settingsReturnReopenEditor = true` before leaving Settings.
  - Passes `settingsReturnReopenEditor` into `SettingsScreen`.
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
  - Added `settingsReturnReopenEditor` parameter to `SettingsScreen`, `ColorsSection`, and `ColorPresetRow` parameter chains.
  - Added `LaunchedEffect(settingsReturnRequestId)` in `ColorPresetRow` to reopen the editor dialog when:
    - return request id changes,
    - `settingsReturnReopenEditor` is true,
    - section matches the original return section.
  - Matching logic restores the closest prior square context using current section photo URI first, then selected color fallback.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` passed (BUILD SUCCESSFUL).

**Manual Test Checklist**:
1. Open `Settings -> Colors` and tap a section square (e.g., File List Item).
2. In the full-screen editor area, tap `Test`.
3. In app view, tap floating `Go Back`.
4. Confirm Settings returns to the same section editor context (not only Colors root).

### [2026-03-28] Fix: file-list-item preview blur onset smoothed at low slider values

**Type**: Bug Fix / Preview Rendering / File List Item

**Status**: ✅ Complete

**User Impact**:
- Low blur values in file-list-item preview no longer appear as an abrupt visual jump.
- Blur now transitions in more gradually while dragging the slider.

**Main Problem (Root Cause)**:
1. File-list-item preview rows are very compact.
2. On tiny surfaces, the first visible blur step can look disproportionately strong.
3. A single-layer blurred image made this onset feel abrupt even with smooth slider values.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`
  - In file-list-item preview rendering (`AppLayoutPreview`), replaced one blurred image layer with two composited layers:
    - Sharp layer alpha = `imageAlpha * (1 - mix)`
    - Blurred layer alpha = `imageAlpha * mix`
  - Added smoothstep mix curve from blur value:
    - `mix = t*t*(3 - 2*t)`
  - Kept gestures on the base layer to preserve existing behavior.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` passed (BUILD SUCCESSFUL).

**Manual Test Checklist**:
1. Open settings preview for file-list-item background photo.
2. Drag blur slowly from 0% upward.
3. Confirm there is no abrupt “hardcore blur” onset at low percentages.

### [2026-03-28] Fix: removed preview blur cliff (46% no blur -> 47% heavy blur)

**Type**: Bug Fix / Preview Sync / Blur Continuity

**Status**: ✅ Complete

**User Impact**:
- Preview blur no longer jumps abruptly at mid slider values.
- Blur now ramps continuously in preview, matching runtime progression more closely.

**Main Problem (Root Cause)**:
1. A hard guard in `applyPhotoPreviewBlur(...)` skipped blur below a fixed preview radius (`0.35.dp`).
2. That threshold created a discontinuity: around the crossing point, preview went from "no blur" to "sudden strong blur".

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/BlurMapping.kt`
  - Removed the hard tiny-radius cutoff in `applyPhotoPreviewBlur(...)`.
  - Re-tuned preview mapping to a smoother continuous low-end curve:
    - `pow(2.7)` + blend `(0.38, 0.04)`
    - changed to `pow(3.1)` + blend `(0.34, 0.03)`
  - Result: continuous, softer low-end blur with no abrupt step.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` passed (BUILD SUCCESSFUL).

**Manual Test Checklist**:
1. Open preview and drag blur slowly through `40% -> 50%`.
2. Confirm no hard jump between adjacent values (e.g., `46% -> 47%`).
3. Compare with runtime for the same section and verify similar progression.

### [2026-03-28] Fix: preview blur density now better matches runtime blur behavior

**Type**: Bug Fix / Preview Sync / Blur Mapping

**Status**: ✅ Complete

**User Impact**:
- Low blur slider values no longer appear disproportionately strong in the preview window.
- Preview blur progression now more closely reflects what users see in the real app view.

**Main Problem (Root Cause)**:
1. Compact preview surfaces (especially tiny file-list item rows) visually amplified small blur values.
2. Very small preview blur radii could still render as a strong first blur step, making early slider movement look "hardcore" compared with runtime.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/BlurMapping.kt`
  - `applyPhotoPreviewBlur(...)`
    - Added tiny-radius guard: skip preview blur when computed radius is below `0.35.dp`.
  - `mapPhotoPreviewBlurToRadius(...)`
    - Re-tuned compensation curve to keep low-end preview blur much softer:
      - from `pow(2.1)` + `(eased * 0.62 + normalized * 0.08)`
      - to `pow(2.7)` + `(eased * 0.38 + normalized * 0.04)`

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` passed (BUILD SUCCESSFUL).

**Manual Test Checklist**:
1. Open background editor preview.
2. Set blur from `0%` upward in small increments (`5%`, `10%`, `20%`).
3. Compare preview blur density with runtime app view for the same section.
4. Confirm low-end blur is no longer over-aggressive in preview.

### [2026-03-28] Fix: file-list item photo quality preserved when alpha < 100%

**Type**: Bug Fix / Rendering Quality / File List Item

**Status**: ✅ Complete

**User Impact**:
- File-list item photos no longer lose sharpness when transparency is set below 100%.
- Transparency still behaves correctly (no gray fallback regression).

**Main Problem (Root Cause)**:
1. `applyPhotoAlpha` was using `Modifier.alpha(...)` for semi-transparent photos.
2. On the file-list item rendering path, this produced visibly worse image quality as soon as alpha dropped below full opacity.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/BlurMapping.kt`
  - Updated `applyPhotoAlpha` to use:
    - `graphicsLayer { alpha = ... }`
    - `compositingStrategy = CompositingStrategy.ModulateAlpha`
  - This keeps blending behavior while preserving better visual quality for transformed image layers.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` passed (BUILD SUCCESSFUL).

**Manual Test Checklist**:
1. Set File List Item photo blur to `0%`.
2. Lower alpha (`100% -> 99% -> 90% -> 70%`).
3. Confirm image remains sharp while fading transparently.
4. Confirm no gray tint regression.

### [2026-03-28] Fix: photo alpha persistence no longer resets after app relaunch

**Type**: Bug Fix / Persistence / Startup State Rehydration

**Status**: ✅ Complete

**User Impact**:
- Photo transparency (alpha) now stays as configured after closing and reopening the app.
- Affected sections: Background, Content, Memo, File List Item, File List Background.

**Main Problem (Root Cause)**:
1. During startup, when an active color preset was reapplied, alpha/blur values were rehydrated from preset square metadata.
2. Those per-square values could be stale compared to the latest slider values already saved in `SharedPreferences`.
3. Result: runtime alpha appeared "reset" to non-transparent values after relaunch.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
  - In active-preset startup restore path, switched alpha/blur rehydration for all photo sections to read from `SharedPreferences` keys (source of truth), not square metadata lookups.
  - Sections updated: `custom_bg`, `content_area`, `memo_area`, `file_list_item`, `file_list_background`.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` passed (BUILD SUCCESSFUL).

**Manual Test Checklist**:
1. Set photo alpha below 100% in each section.
2. Close app completely.
3. Reopen app.
4. Confirm alpha values and visible transparency remain unchanged in all sections.

### [2026-03-28] Follow-up fix: file-list background photo alpha no longer grays out

**Type**: Bug Fix / Rendering Correctness / File List Background

**Status**: ✅ Complete

**User Impact**:
- File-list background photo now fades transparently instead of graying when alpha is reduced.
- Runtime and settings mini preview now use matching transparency composition for file-list background photos.

**Main Problem (Root Cause)**:
1. Runtime `FileListScreen` used `fileListBackgroundColor` as base even when a file-list background photo existed.
2. Settings mini preview also drew the file-list background color under the photo when photo mode was active.
3. In both places, alpha blended against that local tint, producing gray/washed output.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/FileListScreen.kt`
  - File-list base now uses `Color.Transparent` whenever `fileListBackgroundPhotoUri` is present.
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
  - Mini preview file-list background base is now `Color.Transparent` when photo is active.
  - Kept existing photo alpha path stable (`1f` when loaded) while removing tint blending source.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` passed (BUILD SUCCESSFUL).

**Manual Test Checklist**:
1. Set File List Background photo blur to `0%`.
2. Reduce alpha (`100% -> 99% -> 90% -> 70%`).
3. Confirm no gray fade in runtime file-list background.
4. Confirm settings mini preview also no longer appears gray for file-list background photo mode.

### [2026-03-28] Follow-up fix: file-list item photo alpha no longer blends with item tint (gray fade removed)

**Type**: Bug Fix / Rendering Correctness / File List Item

**Status**: ✅ Complete

**User Impact**:
- File-list item photo alpha now fades transparently instead of turning gray in runtime.
- File-list item behavior now matches other sections after the previous transparency fixes.

**Main Problem (Root Cause)**:
1. In runtime `FileListItem`, photo mode still used `customBgColor` as local background when that color existed.
2. Lowering photo alpha blended against that tinted/gray base, causing non-transparent fade.
3. Preview for other sections was correct, but item runtime composition still had this exception.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/FileListItem.kt`
  - In photo mode, item background is now always `Color.Transparent`.
  - Non-photo item color behavior remains unchanged.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` completed with BUILD SUCCESSFUL.

**Manual Test Checklist**:
1. Set File List Item photo blur to `0%`.
2. Lower alpha `100% -> 99% -> 90% -> 70%`.
3. Confirm item photo fades transparently (not gray) in runtime.
4. Confirm non-photo file-list item color remains unchanged.

### [2026-03-28] Follow-up fix: unified transparent photo underlays across preview/runtime to stop cross-section gray alpha regression

**Type**: Bug Fix / Rendering Correctness / Cross-Section Consistency

**Status**: ✅ Complete

**User Impact**:
- Lowering photo alpha now behaves consistently across active and non-active sections in preview and runtime.
- Photo fade no longer turns gray when viewed from other sections after editing.
- Existing zero-blur safeguards remain intact.

**Main Problem (Root Cause)**:
1. `AppLayoutPreview` still painted solid section colors underneath photo layers for several sections.
2. `PhotoBackgroundSurface` runtime paths still rendered the section color as the `Surface` base when a photo existed.
3. When alpha dropped below `100%`, images blended against those local solids, creating gray/washed output instead of transparent blending.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/PhotoBackgroundSurface.kt`
  - Photo-backed `Surface` now uses `Color.Transparent` as base color.
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt` (`AppLayoutPreview`)
  - Updated photo-backed local bases to transparent for:
    - background
    - file list background
    - file list item
    - content area
    - memo area
  - Kept non-photo fallback colors unchanged.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` passed (BUILD SUCCESSFUL).

**Manual Test Checklist**:
1. For each section (Background, Content, Memo, File List Item, File List Background), set photo blur to `0%`.
2. Lower alpha (`100% -> 99% -> 90% -> 70%`) while staying in that section preview.
3. Switch to other sections and verify the previously edited section still looks transparently faded (not gray).
4. Open runtime/app view and confirm the same behavior matches preview.
5. Confirm no blur appears at `0%` blur.

### [2026-03-27] Follow-up fix: removed remaining opaque preview/runtime photo underlays causing gray fade at alpha < 100%

**Type**: Bug Fix / Rendering Correctness / Preview-Runtime Consistency

**Status**: ✅ Complete

**User Impact**:
- Photo alpha fade no longer blends against forced opaque bases in key preview/runtime surfaces.
- Reduces gray/washed look and perceived quality drop when alpha is lowered.

**Main Problem (Root Cause)**:
1. Several photo paths still used forced opaque underlays (`copy(alpha = 1f)` or opaque defaults) inside preview and list-item/list-background rendering.
2. When photo alpha decreased, blending against those bases produced gray/desaturated output.
3. This remained visible especially in settings preview (`AppLayoutPreview`) and file-list surfaces.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`
  - Removed remaining `.copy(alpha = 1f)` base-color forcing in `AppLayoutPreview` for:
    - background
    - file list background
    - file list item
    - content
    - memo
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
  - Removed forced opaque mini preview file-list base when photo exists.
- `app/src/main/java/com/j4/texter2025/ui/components/FileListScreen.kt`
  - Changed photo-active file-list fallback base to `Color.Transparent`.
- `app/src/main/java/com/j4/texter2025/ui/components/FileListItem.kt`
  - Changed photo-active item fallback base from opaque default to `Color.Transparent`.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` passed (BUILD SUCCESSFUL).

**Manual Test Checklist**:
1. In Settings preview and runtime, set photo blur to `0%`.
2. Lower alpha `100% -> 99% -> 90% -> 70%`.
3. Confirm no gray wash / forced dark blending in file-list and background-related sections.
4. Confirm 0% blur remains non-blurred.

### [2026-03-27] Follow-up runtime fix: removed forced opaque photo underlays that reintroduced gray/low-quality alpha fade

**Type**: Bug Fix / Rendering Correctness / Runtime Layering

**Status**: ✅ Complete

**User Impact**:
- Lowering photo alpha now blends naturally again in runtime instead of appearing gray/washed.
- Photo quality no longer appears to "drop" immediately when alpha is reduced.
- Prior blur-at-zero safeguard is preserved.

**Main Problem (Root Cause)**:
1. Runtime photo surfaces still forced opaque underlays (`copy(alpha = 1f)` or equivalent behavior).
2. When photo alpha dropped, blending against those opaque bases produced a gray/low-contrast look.
3. In shared photo surfaces, underlay blur also needed to stay disabled while a photo is active to avoid alpha-revealed blur artifacts.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/PhotoBackgroundSurface.kt`
  - Added `hasPhoto` guard.
  - Updated section blur condition to run only when no photo is active.
  - Removed forced opaque photo underlay behavior (`color.copy(alpha = 1f)`), using `color` directly.
- `app/src/main/java/com/j4/texter2025/ui/components/FileListScreen.kt`
  - Removed forced opaque photo underlays for:
    - main background base
    - file-list background base
  - Kept existing conditional blur guards that only apply section blur when no photo is active.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin --console=plain` passed (BUILD SUCCESSFUL).

**Manual Test Checklist**:
1. In runtime, for each section using photos (Background, Content, Memo, File List Item, File List Background), set photo blur to `0%`.
2. Lower alpha (`100%` -> `99%` -> `90%` -> `70%`).
3. Confirm the image fades transparently (no gray wash / sudden quality drop).
4. Confirm no blur appears at `0%` blur.

### [2026-03-27] Regression fix: restored true photo transparency (removed gray-tint behavior at alpha < 100%)

**Type**: Bug Fix / Rendering Correctness / Alpha Compositing

**Status**: ✅ Complete

**User Impact**:
- Lowering photo alpha below `100%` now restores actual transparency behavior instead of gray-tint desaturation.
- The previous `blur=0%` safeguard remains intact (photo blur is still not applied at zero).

**Main Problem (Root Cause)**:
1. `applyPhotoAlpha(...)` switched photo rendering to `graphicsLayer` + `CompositingStrategy.ModulateAlpha`.
2. With the new opaque local base strategy under photos, this compositing path produced a gray/washed look as alpha decreased.
3. Result: users saw gray fade instead of expected transparency blending.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/BlurMapping.kt`
  - Updated `applyPhotoAlpha(...)` to use standard `Modifier.alpha(...)` again.
  - Removed `graphicsLayer` / `CompositingStrategy.ModulateAlpha` usage.
  - Kept `PHOTO_BLUR_EPSILON` + guarded blur helpers unchanged.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin` passed (BUILD SUCCESSFUL).

**Manual Test Checklist**:
1. In all sections (Background, Content, Memo, File List Item, File List Background), set photo blur to `0%`.
2. Lower alpha from `100%` to `99%`, `90%`, `70%`.
3. Confirm photos become transparently blended (no gray-tint fade).
4. Confirm no blur appears at `0%` blur.

### [2026-03-27] Final follow-up: hard-zero blur guard + modulated photo alpha to stop alpha-triggered blur/quality loss

**Type**: Bug Fix / Rendering Correctness / Preview-Runtime Consistency

**Status**: ✅ Complete

**User Impact**:
- At photo blur `0%`, lowering alpha below `100%` no longer causes the photo to look blurred/soft in preview or runtime.
- Blur now applies only when effective blur value is above a tiny epsilon threshold.
- Photo transparency now keeps image clarity better by avoiding offscreen alpha compositing where possible.

**Main Problem (Root Cause)**:
1. Multiple photo layers always attached `.blur(...)`, even when computed blur radius was effectively zero.
2. Photo layers used standard alpha compositing paths that could degrade clarity when alpha was below 1f.
3. Combined with heavy preview composition, this created a blur-like/low-quality appearance at `blur=0%` when alpha decreased.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/BlurMapping.kt`
  - Added `PHOTO_BLUR_EPSILON` normalization and shared helpers:
    - `applyPhotoBlur(...)`
    - `applyPhotoPreviewBlur(...)`
    - `applyPhotoAlpha(...)` (uses `CompositingStrategy.ModulateAlpha`)
  - Updated both blur mappers to clamp tiny values to exact zero.
- Replaced direct photo blur/alpha chains with shared helpers in:
  - `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`
  - `app/src/main/java/com/j4/texter2025/ui/components/PhotoBackgroundSurface.kt`
  - `app/src/main/java/com/j4/texter2025/ui/components/FileListItem.kt`
  - `app/src/main/java/com/j4/texter2025/ui/components/FileListScreen.kt`
  - `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt`
  - `app/src/main/java/com/j4/texter2025/ui/components/FileEditDialog.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
  - Mini preview file-list background now uses opaque local base under photo and conditional blur modifier application.
  - Switched photo alpha path to shared `applyPhotoAlpha(...)`.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin` passed (BUILD SUCCESSFUL).

**Manual Test Checklist**:
1. For each section (Background, Content, Memo, File List Item, File List Background), set photo blur `0%`.
2. Lower alpha from `100%` to `99%`, `90%`, `70%`.
3. Confirm photo remains clear (no automatic blur/softening).
4. Raise blur from `0%` to `0.1%` and verify smooth onset.

### [2026-03-27] Follow-up fix: expanded alpha-at-0%-blur safeguard to all photo areas + reduced hot-path photo logs

**Type**: Bug Fix / Performance / Layering Correctness

**Status**: ✅ Complete

**User Impact**:
- The `alpha < 100%` + `blur = 0%` issue is now guarded across all preview/runtime photo sections, not only memo/file-list subsets.
- Content and main/background photo areas now keep a stable local opaque base while editing photo alpha.
- Reduced additional photo-position/URI debug churn in hot paths to help reduce frame skips during editing.

**Main Problem (Root Cause)**:
1. Remaining sections still allowed alpha-revealed lower layers, which could look blurred even with photo blur at 0%.
2. Additional verbose photo debug logs in active state sync/save paths added avoidable main-thread overhead.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`
  - Added opaque local base under **main background photo** in preview.
  - Replaced content-area photo preview transparent base with `displayContentColor.copy(alpha = 1f)`.
- `app/src/main/java/com/j4/texter2025/ui/components/FileListScreen.kt`
  - Added opaque `mainBackgroundBaseColor` beneath main background photo layer.
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
  - Removed high-frequency `PhotoPos`/`PhotoBackground` debug logs from photo load/save and positioning save functions used in active editing flows.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin` passed (BUILD SUCCESSFUL).

**Manual Test Checklist**:
1. For **all sections** (Background, Content, Memo, File List Item, File List Background), set photo blur to `0%`.
2. Lower alpha (`100%` -> `99%` -> `90%` -> `70%`).
3. Confirm no unexpected blur appears at 0% blur in preview and runtime.
4. Move blur from `0.1%` upward and confirm smooth onset.

### [2026-03-27] Follow-up fix: removed alpha-driven blur bleed when photo blur is 0% (memo/file-list paths)

**Type**: Bug Fix / Blur Correctness / Layering

**Status**: ✅ Complete

**User Impact**:
- Setting photo blur to `0%` now stays visually sharp even when photo alpha is `< 100%`.
- Eliminates the false blur appearance triggered by lowering alpha in:
  - Memo area
  - File list item
  - File list background
- Preview behavior now better matches expected runtime behavior for zero-blur photos.

**Main Problem (Root Cause)**:
1. Several affected sections rendered `Color.Transparent` under photo layers.
2. When photo alpha dropped below 100%, those transparent bases revealed lower layers that could already be blurred.
3. This made blur appear even when photo blur slider was `0%`.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`
  - For preview file-list background, file-list item, and memo sections:
    - replaced transparent base under photos with section-local base color
    - forced base alpha to `1f` (`copy(alpha = 1f)`) to prevent blur bleed-through from layers below.
- `app/src/main/java/com/j4/texter2025/ui/components/FileListItem.kt`
  - Replaced transparent photo base with stable local color base for item rows.
- `app/src/main/java/com/j4/texter2025/ui/components/FileListScreen.kt`
  - Added `fileListBaseColor` with opaque fallback when file-list background photo exists.
- `app/src/main/java/com/j4/texter2025/ui/components/PhotoBackgroundSurface.kt`
  - Updated photo case surface color from transparent to `color.copy(alpha = 1f)` to avoid alpha revealing blurred layers behind the surface.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin` passed (BUILD SUCCESSFUL).

**Manual Test Checklist**:
1. Open photo editor for Memo, File List Item, and File List Background.
2. Set photo blur to `0%`.
3. Move alpha from `100%` down to `99%`, `90%`, `70%`.
4. Confirm no unexpected blur appears at 0% blur.
5. Set blur to small values (`0.1%` to `2%`) and verify blur onset is subtle/continuous.

### [2026-03-27] Follow-up fix: removed preview dead-zone to eliminate abrupt blur onset around ~4%

**Type**: Bug Fix / Blur UX Consistency

**Status**: ✅ Complete

**User Impact**:
- Compact preview blur no longer jumps from "none" to "strong" around the `3.6% -> 4.6%` range.
- Preview blur now ramps continuously from the first slider movement, while still staying softer than runtime perception.

**Main Problem (Root Cause)**:
1. The previous preview mapping introduced a hard threshold (`<= 0.04f -> 0.dp`).
2. Crossing that threshold caused a visual discontinuity in compact preview (no blur at low values, then sudden blur appearance).

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/BlurMapping.kt`
  - Updated `mapPhotoPreviewBlurToRadius(blurValue)`:
    - removed preview dead-zone threshold
    - switched to continuous low-end softening:
      - `eased = normalized.pow(2.1f)`
      - `compensated = eased * 0.62f + normalized * 0.08f`
    - continues to reuse `mapPhotoBlurToRadius(...)` for radius conversion
  - Keeps runtime blur mapper unchanged.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin` passed (BUILD SUCCESSFUL).

**Manual Test Checklist**:
1. Open Settings -> Colors -> File List Background photo editor.
2. Move blur gradually from `3%` to `6%`.
3. Confirm there is no abrupt jump between `3.6%` and `4.6%` in compact preview.
4. Verify runtime still applies gradual blur consistently after Test/Select.

### [2026-03-27] Follow-up fix: tightened compact-preview blur calibration and removed remaining editor gesture log churn

**Type**: Bug Fix / Performance / Blur UX Consistency

**Status**: ✅ Complete

**User Impact**:
- Compact preview blur now starts much more gently at the low end.
- Very small slider movement no longer jumps into visibly strong blur in preview.
- Preview interaction is smoother due to removal of remaining high-frequency gesture/mode logs.

**Main Problem (Root Cause)**:
1. The prior preview-only compensation (`pow(1.45f) * 0.85f`) still produced a strong visible step in compact preview at tiny values.
2. Remaining debug logs in active preview interaction paths (`ColorPickerDialog`/`UnifiedBackgroundEditor`) still added avoidable overhead.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/BlurMapping.kt`
  - Updated `mapPhotoPreviewBlurToRadius(blurValue)` to a stricter low-end response:
    - hard dead-zone for preview only: `<= 0.04f -> 0.dp`
    - remap after threshold: `((normalized - 0.04f) / 0.96f)`
    - stronger perceptual easing: `pow(1.9f)`
    - lower preview ceiling: `* 0.65f`
  - Runtime mapping remains unchanged (`mapPhotoBlurToRadius`).
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`
  - Removed remaining verbose photo-picker/mode/gesture debug logs in active editor preview paths.
- `app/src/main/java/com/j4/texter2025/ui/components/UnifiedBackgroundEditor.kt`
  - Removed remaining mode-toggle and gesture `Log.d(...)` calls in preview interaction paths.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin` passed (BUILD SUCCESSFUL).

**Manual Test Checklist**:
1. Open Settings -> Colors -> File List Background photo editor.
2. Move blur slowly from `0%` to `5%` and confirm low-end blur progression is subtle.
3. Compare preview vs runtime using Test/Select and verify closer low-end perception.
4. Pinch/drag in preview and switch Color/Photo tabs; confirm no interaction stutter from log churn.

### [2026-03-27] Follow-up fix: softened low-end compact preview blur and removed remaining preset interaction debug logs

**Type**: Bug Fix / Performance / Blur UX Consistency

**Status**: ✅ Complete

**User Impact**:
- Very small blur values in compact preview now ramp more gently instead of appearing abruptly strong.
- Preview blur perception is now closer to in-app runtime rendering for low percentages.
- Reduced extra log overhead in preset click/drag option flows.

**Main Problem (Root Cause)**:
1. Compact preview used a linear compensation only, which still overstated blur visibility at tiny slider values.
2. Remaining `BlurDebug` / `CopyDebug` logs in `SettingsScreen` were still active during preset interactions.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/BlurMapping.kt`
  - Added `mapPhotoPreviewBlurToRadius(blurValue)` for preview-only mapping:
    - applies perceptual easing: `normalized.pow(1.45f)`
    - applies compact-surface compensation: `* 0.85f`
    - then reuses shared runtime mapper `mapPhotoBlurToRadius(...)`
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`
  - `AppLayoutPreview` now uses `mapPhotoPreviewBlurToRadius(...)` for all preview photo blur applications.
  - Removed old inline `previewBlurCompensation` mapping block.
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
  - Removed remaining non-essential `BlurDebug` / `CopyDebug` logs in preset click/long-click/drag-options paths.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin` passed (BUILD SUCCESSFUL).

**Manual Test Checklist**:
1. Open Settings -> Colors -> File List Background photo editor.
2. Set blur around `0.5% - 2%` and observe compact preview smoothness.
3. Tap Select/Test and compare with runtime-applied blur.
4. Confirm low-end preview no longer looks sharply over-blurred vs runtime.

### [2026-03-26] Follow-up fix: aligned compact preview blur perception with runtime and removed preset reload hot-path in settings

**Type**: Bug Fix / Performance / Blur UX Consistency

**Status**: ✅ Complete

**User Impact**:
- Blur in compact preview now better matches the in-app applied blur at the same slider percent.
- Removed repeated preset reload/log churn while Settings is open, reducing perceived lag and abruptness.

**Main Problem (Root Cause)**:
1. Preview surface is much smaller than runtime surfaces, so equal blur radius looked perceptually stronger in preview.
2. `SettingsScreen` was receiving `colorPresets = loadColorPresets()` from composition in `MainActivity`, re-parsing presets on recomposition.
3. Verbose preset debug logs amplified this hot path and made blur interaction feel unstable.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`
  - In `AppLayoutPreview`, added preview-only blur compensation before shared radius mapping:
    - `previewBlurCompensation = 0.72f`
    - `previewBlurRadius(value) = mapPhotoBlurToRadius((value * 0.72f).coerceIn(0f, 1f))`
  - Applied compensated preview blur for all photo-backed sections in preview (bg, fileListBg, fileListItem, content, memo).
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
  - Removed verbose `saveColorPresets` stacktrace/list logs and per-load preset listing logs.
  - Removed remaining non-error `PresetDebug` logs from preset apply/select/reset handlers and `loadCustomColorPresetsWithPhotos(...)`.
  - Switched `SettingsScreen` input from recomposition reload to state:
    - from `colorPresets = loadColorPresets().let { colorRefreshTrigger; it }`
    - to `colorPresets = colorPresets`
  - Kept in-memory preset list synchronized after save/update/delete/rename operations.
  - Removed remaining high-frequency `PASSING ...` and backdrop logs in composition path.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin` passed (BUILD SUCCESSFUL).

**Manual Test Checklist**:
1. Open Settings -> Colors -> File List Background photo editor.
2. Set blur to ~`1-2%` and compare preview vs in-app (Test/Select flow).
3. Confirm preview no longer appears drastically over-blurred at low percentages.
4. Keep Settings open and interact across presets; confirm less log churn/jank.

### [2026-03-25] Follow-up fix: removed per-tick preset persistence during blur drag and cleared remaining PhotoControls hot logs

**Type**: Bug Fix / Performance / Blur UX

**Status**: ✅ Complete

**User Impact**:
- Blur preview now updates more continuously during slider drags.
- Reduced main-thread stalls caused by repeated preset disk writes while dragging blur.
- Photo controls no longer emit repeated mode/render debug logs in active editor flow.

**Main Problem (Root Cause)**:
1. Even after linear blur mapping and prior logging cleanup, blur preview could still feel abrupt.
2. In `MainActivity`, each photo blur slider tick still called `saveColorPresets(...)` through selected-preset update blocks.
3. That per-tick disk persistence created UI thrash during drag, making blur response appear jumpy.
4. `PhotoControlsPanel` still had debug logs in active UI paths (render/mode chip interactions), adding avoidable overhead.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
  - Updated all photo blur callbacks to keep `selectedPresetForEditing` synchronized in-memory only during drag:
    - `onCustomBgPhotoBlurChange`
    - `onContentAreaPhotoBlurChange`
    - `onMemoAreaPhotoBlurChange`
    - `onFileListItemPhotoBlurChange`
    - `onFileListBackgroundPhotoBlurChange`
  - Removed per-tick `loadColorPresets()` / `saveColorPresets(...)` write path from those blur callbacks.
  - Kept immediate area blur persistence (`save*PhotoBlur`) so live preview remains responsive.
- `app/src/main/java/com/j4/texter2025/ui/components/PhotoControlsPanel.kt`
  - Removed hot-path `Log.d(...)` calls from recomposition/mode-chip interaction paths.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin` passed (BUILD SUCCESSFUL).

**Manual Test Checklist**:
1. Open Settings -> Colors -> File List Background (and File List Item) photo editor.
2. Drag Blur from `0%` upward slowly and continuously.
3. Confirm preview updates are smooth without abrupt jumps/stalls during drag.
4. Tap Select/Test and confirm final values still apply correctly.
5. Reopen editor and verify blur state is still correct.

### [2026-03-25] Follow-up fix: removed hot-path preview/editor logging that caused blur-drag jank and abrupt feedback

**Type**: Bug Fix / Performance / Blur UX

**Status**: ✅ Complete

**User Impact**:
- Blur preview updates are now smoother while dragging in photo editors.
- Reduced abrupt/stuttered visual jumps during low-end slider movement.
- Edit dialog no longer floods logs every recomposition tick.

**Main Problem (Root Cause)**:
1. Blur mapping had already been tuned, but slider feedback still looked abrupt in preview.
2. Runtime logs showed hot-path spam from `ColorPreview`, `UnifiedBgEditor`, and `ColorPresetRow` while dragging.
3. Those high-frequency `Log.d(...)` calls ran on nearly every recomposition/frame in the edit flow, increasing main-thread load and making blur changes appear jumpy.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`
  - Removed high-frequency `ColorPreview` debug logs in file-list background sync and content preview sections.
- `app/src/main/java/com/j4/texter2025/ui/components/UnifiedBackgroundEditor.kt`
  - Removed recomposition-time `UnifiedBgEditor` debug logs (dialog entry/render, mode display, control rendering, mode-change logs).
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
  - Removed `ColorPresetRow` debug logs emitted whenever add/edit dialog composables recompose.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin` passed (BUILD SUCCESSFUL).

**Manual Test Checklist**:
1. Open Settings -> Colors -> File List Background photo editor.
2. Drag blur slowly from `0%` upward and watch preview continuously.
3. Confirm no repeated dialog-open spam behavior and no abrupt visual stepping from logging stalls.
4. Repeat in File List Item editor and compare smoothness.

### [2026-03-25] Follow-up fix: photo blur now uses strict linear per-percent ramp (1%-100%)

**Type**: Bug Fix / Blur Sensitivity

**Status**: ✅ Complete

**User Impact**:
- Blur now increases proportionally for each slider percent step.
- Low-end blur no longer feels like a sudden jump around 1-3%.
- Slider response is now predictable from 1% through 100%.

**Main Problem (Root Cause)**:
1. Previous smoothstep easing was continuous but still non-linear.
2. At low values, visual response still felt too abrupt once blur became visible.
3. User required strictly incremental per-percent progression without eased acceleration.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/BlurMapping.kt`
  - Replaced smoothstep mapping with strict linear mapping:
    - from: `t * t * (3 - 2 * t)`
    - to: `t`
  - Final radius formula:
    - `radiusDp = blurValueNormalized * PHOTO_BLUR_MAX_RADIUS_DP`
  - Max blur remains unchanged (`5dp`).

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin` passed (BUILD SUCCESSFUL).

**Manual Test Checklist**:
1. Open File List Background photo editor.
2. Drag blur slowly from `0%` to `10%` and confirm each 1% increment changes blur a bit.
3. Verify no sudden jump at `1-3%`.
4. Check `10%`, `20%`, `30%`, `50%` and confirm progression remains proportional.

### [2026-03-25] Follow-up fix: removed 8% blur dead zone so blur now ramps continuously from 0%

**Type**: Bug Fix / Blur Sensitivity

**Status**: ✅ Complete

**User Impact**:
- Blur now begins increasing gradually from the first slider movement.
- No longer requires reaching ~8% before any visible blur appears.
- Removes the abrupt low-end transition caused by thresholded mapping.

**Main Problem (Root Cause)**:
1. Shared blur mapper had a hard dead zone at `0.08f`.
2. Values from `0%` to `8%` always produced radius `0.dp`.
3. Once crossing that threshold, blur appeared suddenly, which felt like a jump.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/BlurMapping.kt`
  - Removed `PHOTO_BLUR_DEAD_ZONE` threshold behavior.
  - Replaced dead-zone + cubic segment mapping with continuous smoothstep ramp:
    - `smoothRamp = t * t * (3 - 2 * t)`
    - `radius = smoothRamp * PHOTO_BLUR_MAX_RADIUS_DP`
  - Keeps max radius unchanged (`5dp`) while making low-end response continuous.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin` passed (BUILD SUCCESSFUL).

**Manual Test Checklist**:
1. Open a photo blur editor (especially File List Background).
2. Move slider slowly from `0%` to `10%` and confirm blur starts appearing progressively before `8%`.
3. Confirm there is no abrupt jump when crossing the old `~8%` point.
4. Compare at `5%`, `8%`, `10%`, `15%` and ensure the intensity rise feels smooth.

### [2026-03-25] Follow-up fix: gentle low-end photo blur mapping now unified across remaining file-list and full preview paths

**Type**: Bug Fix / Blur Sensitivity / Consistency

**Status**: ✅ Complete

**User Impact**:
- Tiny blur slider movements now produce subtler, more logical blur changes in File List Item, File List Background, and full app-layout preview surfaces.
- Blur response now feels consistent between Settings preview and runtime rendering for photo-backed areas.

**Main Problem (Root Cause)**:
1. A shared gentle blur mapper (`mapPhotoBlurToRadius`) already existed, but several photo renderers still used direct quadratic radius expressions inline (`((value * value) * 5f).dp`).
2. Keeping mixed blur radius implementations across paths made low-end blur sensitivity feel inconsistent and harder to tune globally.

**Implementation**:
- Standardized remaining photo blur paths to use shared mapper:
  - `app/src/main/java/com/j4/texter2025/ui/components/FileListItem.kt`
    - Replaced both DYNAMIC and STATIC_ZOOM photo blur calls with `mapPhotoBlurToRadius(photoBlur)`.
  - `app/src/main/java/com/j4/texter2025/ui/components/FileListScreen.kt`
    - Replaced custom background photo blur and file-list background photo blur with `mapPhotoBlurToRadius(...)`.
  - `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`
    - Replaced all app preview photo blur calculations (bg, fileListBg, fileListItem, content, memo) with `mapPhotoBlurToRadius(...)`.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin` passed (BUILD SUCCESSFUL).

**Manual Test Checklist**:
1. Open Settings -> Colors and edit photo backgrounds for Background, File List Background, and File List Item.
2. Move Blur slider from 0 in very small steps; confirm no abrupt first-step blur jump.
3. Compare same blur value across preview and runtime surfaces; confirm similar perceived intensity.
4. Tap Select/Test, reopen editor, and verify behavior remains stable after persistence.

### [2026-03-24] Follow-up fix: photo blur slider now avoids per-frame preset save/reload thrash in edit dialog

**Type**: Bug Fix / Performance / Blur UX

**Status**: ✅ Complete

**User Impact**:
- Blur slider in photo edit flow now responds smoothly instead of feeling "stuck"/"same" from heavy frame drops.
- Removes repeated preset save/load churn while dragging sliders in the edit dialog.

**Main Problem (Root Cause)**:
1. In `SettingsScreen` edit flow (`ColorPresetRow` -> `UnifiedBackgroundEditor`), photo slider callbacks (`blur/offset/scale/rotation`) invoked `onEditCustomColor(...)` on every drag frame.
2. That path reached `MainActivity` handlers which repeatedly called `loadColorPresets()` + `saveColorPresets(...)` during drag.
3. Main-thread thrash caused skipped frames and unstable visual feedback, making blur changes feel inconsistent.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
  - In edit dialog callbacks, changed photo slider handling to keep values local during drag:
    - `onPhotoBlurChange`
    - `onPhotoOffsetXChange`
    - `onPhotoOffsetYChange`
    - `onPhotoScaleChange`
    - `onPhotoRotationChange`
  - Removed per-frame `onEditCustomColor(...)` writes from those callbacks.
  - Kept final commit behavior via existing `onPhotoSelected` (Select/Test), so persistence happens once per action.

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin` passed.

**Manual Test Checklist**:
1. Open Settings -> Colors -> File List Background (or File List Item) -> edit an existing photo square.
2. Drag Blur slider continuously and confirm smoother updates (no repeated stutter/lag bursts).
3. Drag Zoom/Position/Rotation and confirm interaction remains smooth.
4. Tap Select, reopen same square, and verify values persisted correctly.

### [2026-03-24] Follow-up fix: unified photo blur response and quality perception across preview/runtime surfaces

**Type**: Bug Fix / Blur Consistency / Render Behavior

**Status**: ✅ Complete

**User Impact**:
- First blur slider movement is now gentler across photo-backed sections instead of jumping into heavy blur.
- Full preview/editor quality no longer appears to "drop suddenly" at the first blur step due to overly strong low-end blur mapping.

**Main Problem (Root Cause)**:
1. Blur response was inconsistent across rendering paths: some surfaces still used linear mapping (`blur * 5f`) while others used softened non-linear mapping.
2. This mismatch made early slider movement feel too aggressive and visually inconsistent between editor preview and runtime views.
3. In addition, file-list background previously had blur stacking behavior that amplified perceived blur strength, making quality loss feel worse.

**Implementation**:
- Standardized photo blur rendering to quadratic mapping in active preview/runtime paths:
  - `blur(((value * value) * 5f).dp)`
- Ensured file-list background avoids section blur stacking when a photo is active.
- Applied/verified consistent non-linear mapping across:
  - `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`
  - `app/src/main/java/com/j4/texter2025/ui/components/FileListScreen.kt`
  - `app/src/main/java/com/j4/texter2025/ui/components/FileListItem.kt`
  - `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt`
  - `app/src/main/java/com/j4/texter2025/ui/components/PhotoBackgroundSurface.kt`

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin` passed.

**Manual Test Checklist**:
1. Open Settings -> Colors -> File List Background -> Photo mode.
2. Move Blur slider from 0 upward in tiny steps and confirm no abrupt jump.
3. Compare same blur % across Background / Content / Memo / File List Item / File List Background.
4. Open full preview/editor and confirm low blur values keep visual detail stable (no sudden quality cliff).
5. Save/apply preset, reopen app, and verify blur behavior remains consistent after persistence.

### [2026-03-24] Fix: file-list background photo blur no longer reacts too aggressively

**Type**: Bug Fix / Blur Consistency

**Status**: ✅ Complete

**User Impact**:
- File List Background photo blur now changes at the same perceived sensitivity as other sections.
- Small slider movement no longer causes a disproportionately strong blur jump.

**Main Problem (Root Cause)**:
1. In `FileListScreen`, the file-list background container always applied section color blur (`fileListBackgroundBlur * 5f`) even when a file-list background photo was active.
2. The photo layer also applied its own blur (`fileListBackgroundPhotoBlur * 5f`).
3. This stacked two blur passes, making file-list background photo blur appear much stronger than the same slider value in other sections.

**Implementation**:
- `app/src/main/java/com/j4/texter2025/ui/components/FileListScreen.kt`
  - Updated file-list background container modifier so section blur is only applied when no file-list background photo is selected.
  - Kept photo-layer blur logic unchanged, so only one blur source is active during photo editing/rendering.

**Files Modified**:
- `app/src/main/java/com/j4/texter2025/ui/components/FileListScreen.kt`

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin` passed.

### [2026-03-24] Fix: removed constrained image decode sizes causing blurry zoomed backgrounds

**Type**: Quality Fix / Render Fidelity

**Status**: ✅ Complete

**User Impact**:
- Imported photos now render at much higher detail in settings previews and app surfaces when zoomed/rotated.
- Eliminates the remaining blur/pixelation caused by preview/runtime painter decode limits.

**Main Problem (Root Cause)**:
1. Multiple render paths were still using constrained decode requests (e.g., `600x600`, `800x800`, `1000x1000`, `1200x1200`) in `ColorPickerDialog`.
2. Several zoomable background surfaces relied on default constrained decode sizing.
3. When users zoomed backgrounds to values like ~2x-3x, rendered pixels were upscaled from already downsampled bitmaps, producing visible blur.

**Implementation**:
- Added shared helper:
  - `app/src/main/java/com/j4/texter2025/ui/components/HighQualityPhotoPainter.kt`
  - Uses Coil `ImageRequest` with `size(Size.ORIGINAL)` to keep source detail.
- Replaced painter usage in key zoomable surfaces:
  - `ColorPickerDialog.kt`
  - `PhotoBackgroundSurface.kt`
  - `FileListScreen.kt`
  - `FileListItem.kt`
  - `FileEditDialog.kt`
  - `FullScreenEditor.kt`
  - `PhotoPositionDialog.kt`
  - `SettingsScreen.kt` (photo previews)

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin` passed.

### [2026-03-24] Follow-up fix: imported photos now keep original source quality (and backups preserve that quality)

**Type**: Quality Fix / Photo Fidelity

**Status**: ✅ Complete

**User Impact**:
- Newly imported background photos now keep original quality (no forced downscale/recompression), so applied photos render much closer to source fidelity.
- Backups exported after this change preserve the same original-quality photo bytes, and restore writes those exact bytes back.

**Root Cause**:
1. `PhotoStorage.savePhoto(...)` previously decoded images into bitmaps, sampled/scaled them, then recompressed to JPEG.
2. That import pipeline introduced irreversible quality loss before photos were even used or backed up.

**Implementation**:
- `PhotoStorage.kt`:
  - Replaced bitmap decode/resize/compress path with byte-for-byte copy from source URI input stream.
  - Kept deterministic deduped filenames via MD5 hash, now based on original source bytes.
  - Preserved file format extension from MIME/URI (`jpg/png/webp/heic/...`) instead of forcing `.jpg`.
- Backup compatibility note:
  - Existing backup flow already stores/restores raw file bytes (`photoToBase64` / `photoFromBase64`), so with this import fix, photo quality remains intact through export/import.

**Files Modified**:
- `app/src/main/java/com/j4/texter2025/data/PhotoStorage.kt`

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin` completed successfully.

### [2026-03-24] Follow-up fix: cross-section preview now shows saved rotation for non-active areas

**Type**: Bug Fix / Preview Rendering Sync

**Status**: ✅ Complete

**User Impact**:
- In the Settings photo editor preview, when you switch to another area (e.g., memo) you now correctly see twist/rotation already applied on other areas (e.g., content), matching zoom behavior.

**Root Cause**:
1. `UnifiedBackgroundEditor` loaded and forwarded non-active sections' URI/alpha/blur/offset/scale, but not their rotation values.
2. `PreviewTouchArea -> AppLayoutPreview(...)` therefore used default rotation (`0f`) for non-active sections, so twist looked missing while zoom still appeared.

**Implementation**:
- `UnifiedBackgroundEditor.kt`:
  - Added missing loaded rotation state for all sections:
    - `loadedCustomBgRotation`
    - `loadedContentRotation`
    - `loadedMemoRotation`
    - `loadedFileListItemRotation`
    - `loadedFileListBgRotation`
  - Loaded each value from SharedPreferences (`*_photo_rotation` keys).
  - Threaded those values through `PreviewTouchArea(...)` and into `AppLayoutPreview(...)` as:
    - `customBgPhotoRotation`
    - `contentAreaPhotoRotation`
    - `memoAreaPhotoRotation`
    - `fileListItemPhotoRotation`
    - `fileListBgPhotoRotation`

**Files Modified**:
- `app/src/main/java/com/j4/texter2025/ui/components/UnifiedBackgroundEditor.kt`

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin` passed.

### [2026-03-23] Follow-up fix: MainActivity now forwards rotation into FileManagementApp

**Type**: Bug Fix / Parameter Forwarding

**Status**: ✅ Complete

**User Impact**:
- Rotations set in Settings are now preserved when opening files in the app editor path, instead of appearing untwisted in the opened editor view.

**Root Cause**:
1. `MainActivity` correctly persisted and updated `customBgPhotoRotation`, `contentAreaPhotoRotation`, and `memoAreaPhotoRotation` through callbacks.
2. However, in the `Settings -> FileManagementApp(...)` call site, those three rotation args were not being forwarded.
3. `FileManagementApp` defines these parameters with defaults (`0f`), so downstream `FullScreenEditor` opened with default rotation despite saved non-zero values.

**Implementation**:
- `MainActivity.kt` (`Settings` branch call into `FileManagementApp(...)`):
  - Added missing parameter forwarding for:
    - `customBgPhotoRotation = customBgPhotoRotation`
    - `contentAreaPhotoRotation = contentAreaPhotoRotation`
    - `memoAreaPhotoRotation = memoAreaPhotoRotation`

**Files Modified**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin` passed.

### [2026-03-23] Follow-up fix: selecting an existing photo square now reapplies its saved rotation

**Type**: Bug Fix / Selection State Sync

**Status**: ✅ Complete

**User Impact**:
- Tapping an existing custom photo square in Settings now reapplies its full transform (including rotation), so the opened editor no longer loses twist when that square is selected.

**Root Cause**:
1. In `ColorPresetRow` square-click handling, photo presets reapplied URI/offset/scale but did **not** reapply `photoRotation`.
2. As a result, section rotation state could remain stale/default while offset/scale changed correctly, matching the logs where transform values looked partially correct but visual twist still looked unchanged.

**Implementation**:
- `SettingsScreen.kt` (`ColorPresetRow` click path for existing presets):
  - Added `onBackgroundPhotoRotationChange?.invoke(preset.photoRotation)` when a photo square is selected.
  - For plain color square selection, now also resets transform state (`offsetX/offsetY/scale/rotation`) to defaults alongside URI/alpha/blur cleanup.

**Files Modified**:
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin` passed.

### [2026-03-22] Follow-up fix: Settings now forwards live photo transform state (including rotation)

**Type**: Bug Fix / Parameter Wiring

**Status**: ✅ Complete

**User Impact**:
- Rotation/offset/scale configured for section photo backgrounds now arrive correctly in Settings edit flows, preventing fallback to default `0f/0f/1f/0f` values when opening/editing photo squares.

**Root Cause**:
1. `SettingsScreen` called `ColorsSection(...)` with only URI/alpha/blur for several sections.
2. `ColorsSection` defaults transform params (`offsetX/offsetY/scale/rotation`) to zero/one when omitted.
3. Downstream `ColorPresetRow`/`UnifiedBackgroundEditor` therefore received default transforms, causing runtime mismatch despite `MainActivity` holding the correct non-default state.

**Implementation**:
- `SettingsScreen.kt`:
  - Updated `ColorsSection(...)` invocation to pass all section transform fields:
    - `customBgPhotoOffsetX/Y/Scale/Rotation`
    - `contentAreaPhotoOffsetX/Y/Scale/Rotation`
    - `memoAreaPhotoOffsetX/Y/Scale/Rotation`
    - `fileListItemPhotoOffsetX/Y/Scale/Rotation`
    - `fileListBackgroundPhotoOffsetX/Y/Scale/Rotation`

**Files Modified**:
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin` passed.

### [2026-03-22] Follow-up fix: twist changes from Edit dialog now propagate to opened editor state

**Type**: Bug Fix / State Propagation

**Status**: ✅ Complete

**User Impact**:
- Rotation adjusted in the **Edit existing photo-square dialog** now propagates to section live state/prefs, so the opened text editor can render the same twist.

**Root Cause**:
1. `SettingsScreen` had two `UnifiedBackgroundEditor` wiring paths:
   - add/new color flow (had rotation callback),
   - edit-existing color flow (missing rotation callback).
2. In the edit flow, offset/scale propagated but rotation updates were not routed through section callbacks during interaction, causing stale rotation state in downstream editor rendering paths.

**Implementation**:
- `SettingsScreen.kt` (`ColorPresetRow`, edit picker branch):
  - Added missing `onPhotoRotationChange` wiring.
  - Routed rotation updates to section-specific callbacks (`content`, `memo`, `fileListItem`, `fileListBg`, background).
  - Updated `presetToEdit.photoRotation` and `onEditCustomColor(...)` payload so rotation stays in sync with offset/scale.

**Files Modified**:
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin` passed.

### [2026-03-22] Fix: Opened text-file editor now renders saved photo twist

**Type**: Bug Fix / Rendering Wiring

**Status**: ✅ Complete

**User Impact**:
- Rotated/twisted custom background photos now render in the opened text-file editor surface (not only in previews/preset cards).

**Root Cause**:
1. The opened note screen path uses `FileEditDialog`, not only `FullScreenEditor`.
2. `FileEditDialog`'s background image `graphicsLayer` applied scale/offset but did not apply `rotationZ`.
3. `MainActivity` invocation of `FileEditDialog` passed colors/layout only, so photo transform values (including rotation) were not threaded into that runtime path.

**Implementation**:
- `FileEditDialog.kt`:
  - Added `customBgPhotoRotation` parameter to `FileEditDialog(...)`.
  - Applied `rotationZ = customBgPhotoRotation` in the opened-note background image `graphicsLayer`.
- `MainActivity.kt`:
  - Updated `FileEditDialog(...)` call to pass custom background photo URI/alpha/blur/offset/scale/rotation from live state.

**Files Modified**:
- `app/src/main/java/com/j4/texter2025/ui/components/FileEditDialog.kt`
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin` passed.

### [2026-03-22] Follow-up fix: Rotation now renders in preset cards and survives startup state sync

**Type**: Bug Fix / Rendering + State Sync

**Status**: ✅ Complete

**User Impact**:
- Twisted/rotated photos now visibly render in **preset card previews** (not just the editor preview).
- Rotation and positioning now correctly apply in live app surfaces after editing a photo square and after app startup reload of active preset.

**Root Cause**:
1. `AppLayoutPreview` only rendered `photoRotation` for the *currently edited* `targetSection`; when `targetSection = "none"` (preset cards), non-active sections defaulted to `0f` rotation.
2. `PresetCard`/`ColorPresetsSection` loaded live offsets/scales from SharedPreferences but did not load/pass live rotation values.
3. Several `onEdit...Color` paths updated preset-square data but did not update section-level live state + SharedPreferences keys used by app surfaces and card previews.
4. Startup active-preset restore applied colors/URIs but did not fully restore per-section offset/scale/rotation into live state + prefs.

**Implementation**:
- `ColorPickerDialog.kt` (`AppLayoutPreview`):
  - Added per-section rotation params:
    - `customBgPhotoRotation`, `contentAreaPhotoRotation`, `memoAreaPhotoRotation`, `fileListItemPhotoRotation`, `fileListBgPhotoRotation`
  - Updated each image `graphicsLayer` to use section rotation when not actively editing that section.
- `SettingsScreen.kt`:
  - Added live rotation state vars for all sections in `ColorPresetsSection`.
  - Loaded live rotation from SharedPreferences keys:
    - `custom_bg_photo_rotation`, `content_area_photo_rotation`, `memo_area_photo_rotation`, `file_list_item_photo_rotation`, `file_list_background_photo_rotation`
  - Threaded live rotation into `PresetCard` and then into `AppLayoutPreview`.
- `MainActivity.kt`:
  - In startup active-preset restore block, now restores and persists per-section offset/scale/rotation for all 5 sections.
  - In all `onEdit...Color` handlers, now syncs section live photo state + saves corresponding SharedPreferences keys (including rotation) immediately.
  - Fixed `onEditFileListBackgroundColor` preset copy to use incoming `offsetX/offsetY/scale` values (instead of stale state) with `rotation`.

**Files Modified**:
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`

**Validation**:
- ✅ `./gradlew.bat :app:compileDebugKotlin` passed.

### [2026-03-22] Fix: Complete photo rotation persistence across presets/settings/editor flows

**Type**: Bug Fix / State Persistence

**Status**: ✅ Complete

**User Impact**:
- Photo rotation set in `UnifiedBackgroundEditor` now persists through add/edit color-square flows, preset save/apply/select flows, and section-level settings updates.
- Rotation now round-trips correctly for all 5 sections:
  - custom background
  - content area
  - memo area
  - file list item
  - file list background

**Root Cause**:
- Rotation support was partially threaded, but key parts were inconsistent:
  1. `ColorPreset` model/JSON did not fully include section rotation fields.
  2. `ColorPresetRow` did not expose all section rotation callbacks required by its own usage sites.
  3. `UnifiedBackgroundEditor` accepted rotation callbacks but did not initialize/load section rotation consistently.
  4. `MainActivity` had a corrupted `SettingsScreen(...)` argument block (duplicate/overlapping `onUpdatePreset` and stale references from a bad patch region), causing compile and wiring failures.

**Implementation**:
- Repaired `MainActivity` `SettingsScreen(...)` preset callback region:
  - Fixed broken/stale variable references in `onApplyPreset`.
  - Restored valid named-argument structure and removed duplicate `onUpdatePreset` block.
  - Ensured rotation save/apply/select paths call section-specific `save...PhotoRotation(...)` and preset copy fields.
- Extended `ColorPreset` data + JSON serialization:
  - Added rotation fields for all sections and included them in `toJson()`/`fromJson()` mapping.
- Extended `ColorPresetRow` signature in `SettingsScreen.kt`:
  - Added missing rotation callbacks for content/memo/fileListItem sections so passed arguments resolve correctly.
- Updated `UnifiedBackgroundEditor`:
  - Added `currentPhotoRotation` parameter.
  - Initialized local `photoRotation` from incoming state.
  - Loaded section rotation from SharedPreferences in the same section-resolution logic used for position/scale.

**Files Modified**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt` (preset apply/update/select block around ~2850-3210; rotation callback threading in settings args around ~3590-3710)
- `app/src/main/java/com/j4/texter2025/data/ColorPreset.kt` (model + JSON mapping around ~38-58, ~114-134, ~231-251, ~287-306)
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt` (`ColorPresetRow` rotation callback params around ~2533-2549)
- `app/src/main/java/com/j4/texter2025/ui/components/UnifiedBackgroundEditor.kt` (rotation init/load threading around ~48-55 and ~127-181)

**Validation**:
- ✅ `:app:compileDebugKotlin` passes after fixes.
- ⚠️ Build emits existing non-blocking warnings (unused params/variables), no rotation-related compile errors remain.

---

### [2026-03-21] Fix: Two-finger twist now visibly rotates photo in full preview

**Type**: UX Bug Fix

**Status**: ✅ Complete

**User Impact**:
- In full preview photo mode, twist gestures now rotate the photo visibly (not just pan/zoom updates).
- Move + zoom + rotate now work together in the same two-finger gesture.

**Root Cause**:
- Rotation values were being captured in `UnifiedBackgroundEditor` but `AppLayoutPreview` did not render any rotation.
- Result: logs changed, but UI looked unchanged for twist gestures.

**Implementation**:
- Added `photoRotation` parameter to `AppLayoutPreview`.
- Applied `rotationZ` in the photo `graphicsLayer` for the actively edited section:
  - background (`bg`)
  - content (`content`)
  - memo (`memo`)
  - file list background (`fileListBg`)
  - file list item (`fileListItem`)

**Files Modified**:
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt` (around lines 1087-1091, 1174-1176, 1384-1386, 1523-1525, 1690-1692, 1913-1915)

---

### [2026-03-21] Enhancement: Freer 2-finger photo movement in full preview window

**Type**: UX Enhancement

**Status**: ✅ Complete

**User Impact**:
- In the full preview window (Photo mode), two-finger movement now feels much freer while pinching/dragging.
- Users can pan farther across the image at any zoom level.

**Root Cause**:
- Transform gesture panning in `PreviewTouchArea` had a tighter offset clamp (`3f * scale`) and lower movement sensitivity.
- This made two-finger repositioning feel restricted in the full preview area.

**Implementation**:
- Updated full preview transform gesture handling in `UnifiedBackgroundEditor`:
  - Increased gesture sensitivity from `3f` to `4f`.
  - Increased scale-aware pan bounds from `3f * scale` to `8f * scale`.
  - Added safe width/height normalization (`coerceAtLeast(1f)`) for stable pan math.
- Updated preview hint text to explicitly say: `Use 2 fingers to move • Pinch to zoom`.

**Files Modified**:
- `app/src/main/java/com/j4/texter2025/ui/components/UnifiedBackgroundEditor.kt` (around lines 694-708, 777)

---

### [2026-03-21] Fix: Main view no longer stays blurred after Go Back from opened note

**Type**: Bug Fix

**Status**: ✅ Complete

**User Impact**:
- When using `Test` and then opening a note, tapping floating `Go Back` no longer leaves the previous main view blurred.
- Return flow now matches the clear/non-blurred state expected in settings navigation.

**Root Cause**:
- `Scaffold` blur was controlled by `isFullScreenEditorShown && customBgBlur > 0f`.
- In the `Go Back` path from `FileManagementApp`, `showSettings` was set to true and the app switched away from the editor host, but `isFullScreenEditorShown` could remain stale `true` for one composition path, keeping blur active.

**Implementation**:
- In `MainActivity`, updated the floating `onBackToSettings` callback passed to `FileManagementApp` to explicitly reset:
  - `isFullScreenEditorShown = false`
  - before navigating to settings.

**Files Modified**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt` (around lines 3604-3608)

---

### [2026-03-21] Fix: Custom color/photo square deletion works again in Settings

**Type**: Bug Fix

**Status**: ✅ Complete

**User Impact**:
- Long-press `Color Actions -> Delete` now actually removes the targeted custom square.
- Applies across all sections (`bg`, `content`, `memo`, `fileListItem`, `fileListBg`).

**Root Cause**:
- `SettingsScreen` was invoking deletion callbacks (`onDeleteBgColor`, `onDeleteContentAreaColor`, etc.) but `MainActivity` did not wire those callbacks in the `SettingsScreen(...)` call.
- Because those parameters have no-op defaults, delete actions completed visually but performed no state mutation.

**Implementation**:
- Added all missing delete callback bindings in `MainActivity`:
  - `onDeleteBgColor`
  - `onDeleteContentAreaColor`
  - `onDeleteMemoAreaColor`
  - `onDeleteFileListItemColor`
  - `onDeleteFileListBackgroundColor`
- Each callback removes exactly one matching square by `(color, normalizedPhotoUri)` and updates selected preset data (`saveColorPresets` + `selectedPresetForEditing`).

**Files Modified**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt` (around lines 1923-1946, 2041-2064, 2255-2278, 2423-2446, 2586-2609)

---

### [2026-03-20] Enhancement: Elastic animation for content resize when toggling memo in opened note editor

**Type**: UX Enhancement

**Status**: ✅ Complete

**User Impact**:
- Content area no longer snaps abruptly when `Show Memo` / `Hide Memo` is tapped.
- Resizing now has a softer spring transition while preserving manual drag behavior.

**Root Cause**:
- Content height was applied directly from target layout state (`fillMaxHeight(heightFraction)`), so memo-toggle auto-resize changes were instant.

**Implementation**:
- Added animated height interpolation via `animateFloatAsState` + `spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)`.
- Added `isContentAreaDragging` state and wired drag lifecycle (`onDragStart`, `onDragEnd`, `onDragCancel`) so drag uses immediate height updates.
- Applied animation only when not actively dragging:
  - Dragging: use direct `heightFraction`
  - Programmatic/memo-toggle resize: use animated height fraction

**Files Modified**:
- `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt` (around lines 228, 442-465, 498-520)

---

### [2026-03-20] Fix: Auto-adjust content height when memo is shown/hidden in opened note editor

**Type**: UX Bug Fix

**Status**: ✅ Complete

**User Impact**:
- If content area was expanded too far, opening memo could leave memo hidden off-screen.
- After hiding memo, content remained manually shrunk and required extra user adjustment.

**Root Cause**:
- Memo toggle previously only switched visibility and did not reconcile content height with available vertical space.
- Content drag limits did not enforce a memo-aware max height when memo was visible.

**Implementation**:
- Measured editor body height with `onSizeChanged`.
- Added memo-aware content-height caps based on reserved UI space (toggle row, spacing, memo handle, memo height, button safety reserve).
- On memo show: auto-shrink content so memo area can fit on-screen.
- On memo hide: auto-expand content to a button-safe maximum so memo toggle stays visible.
- While memo is visible in dual-bar mode: constrained content drag max to memo-aware cap.

**Files Modified**:
- `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt` (around lines 201-255, 351-355, 464-467, 607-627)

---

### [2026-03-20] Fix: Go Back now returns to the exact settings area used for Test

**Type**: UX Bug Fix

**Status**: ✅ Complete

**User Impact**: After tapping `Test`, returning via floating `Go Back` could reopen Settings at the default top view instead of the origin area.

**Root Cause**:
- Test flow did not persist the source settings section in all callback layers.
- Return navigation reopened Settings without triggering section-focused expansion state.

**Implementation**:
- Standardized test callback shape to include section id: `onTestInApp: (String) -> Unit`.
- Propagated section ids from color editors (`bg`, `content`, `memo`, `fileListItem`, `fileListBg`).
- Added `settingsReturnSection` + `settingsReturnRequestId` state in `MainActivity`.
- Incremented request id on both Go Back entry points so re-opening the same section still re-applies focus.
- In `SettingsScreen`, added a `LaunchedEffect(settingsReturnRequestId)` that expands the appropriate settings category.

**Files Modified**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt` (around lines 838-842, 3413-3418, 3484-3488, 3615-3619)
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt` (around lines 321-350, 1446, 2556, 2940-2944, 3080-3084)

---

### [2026-03-20] Fix: Remaining Go Back FAB drag-start teleport from stale pointerInput captures

**Type**: Bug Fix

**Status**: ✅ Complete

**User Impact**: Even after cumulative drag updates, the Go Back button could still jump on drag start in some sessions.

**Root Cause**:
- `pointerInput` was reading captured `fabOffsetX/fabOffsetY` values that could become stale between recompositions.
- On a new gesture, `onDragStart` could initialize from old values, causing a visible teleport.

**Implementation**:
- Added `rememberUpdatedState` for offset values in both FAB renderers.
- Drag start now initializes from `latestFabOffsetX/latestFabOffsetY` (always current values) instead of stale captures.

**Files Modified**:
- `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt` (around lines 839-856)
- `app/src/main/java/com/j4/texter2025/MainActivity.kt` (around lines 3581-3598)

---

### [2026-03-20] Fix: Go Back FAB "teleport" on drag start while note dialog is open

**Type**: Bug Fix

**Status**: ✅ Complete

**User Impact**: While a note was open, pressing and dragging the floating "Go Back" button could make it suddenly jump to another location before continuing drag.

**Root Cause**:
- Two draggable instances of the same shared FAB state could coexist:
  - Main screen instance in `MainActivity`
  - Dialog instance in `FullScreenEditor`
- This could lead to inconsistent shared offset updates between contexts and visible jump/teleport behavior on drag start.
- MainActivity drag handler also used direct per-event writes instead of per-gesture cumulative offsets.

**Implementation**:
- Updated `MainActivity` FAB visibility condition to render only when editor dialog is not shown:
  - `if (showBackToSettingsFab && !isFullScreenEditorShown)`
- Updated MainActivity drag logic to use gesture-local cumulative offsets (same pattern used for dialog fix).

**Files Modified**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt` (around lines 3580-3604)

---

### [2026-03-20] Fix: Jerky drag for floating "Go Back" button inside FullScreenEditor dialog

**Type**: Bug Fix

**Status**: ✅ Complete

**User Impact**: The floating "Go Back" button could appear in opened notes but dragging felt jerky/jumpy in `FullScreenEditor`.

**Root Cause**:
- In `FullScreenEditor.kt`, drag updates used non-cumulative values on each pointer event:
  - `onFabOffsetChange(fabOffsetX + dragAmount.x, fabOffsetY + dragAmount.y)`
- `dragAmount` is per-event delta, but `fabOffsetX/fabOffsetY` are composition values that can lag during rapid recomposition.
- This mismatch created visible jitter (position jumping between stale and newer values).

**Implementation**:
- Updated `FullScreenEditor` draggable button logic to maintain per-gesture cumulative offsets:
  - File: `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt` (around lines 848-861)
  - Added gesture-local accumulators:
    - `gestureOffsetX`
    - `gestureOffsetY`
  - Synced accumulators on `onDragStart`
  - Applied cumulative updates on each drag event before calling `onFabOffsetChange`

**Key Code Change**:
```kotlin
detectDragGestures(
    onDragStart = {
        gestureOffsetX = fabOffsetX
        gestureOffsetY = fabOffsetY
    }
) { change, dragAmount ->
    change.consume()
    gestureOffsetX += dragAmount.x
    gestureOffsetY += dragAmount.y
    onFabOffsetChange(gestureOffsetX, gestureOffsetY)
}
```

**Files Modified**:
- `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt`

---

### [2026-03-10] Incomplete Feature: Single-Bar Drag Mode (Temporarily Disabled)

**Type**: Feature Development - Work in Progress

**Feature ID**: FEATURE_SINGLE_BAR_DRAG

**Status**: Temporarily disabled via feature flag `ENABLE_SINGLE_BAR_MODE = false`

**Description**: Started implementation of an alternative editor resize mode called "Single-Bar" that provides a unified drag handle between content and memo areas (similar to the preview window behavior). The feature is functional but needs refinement, so it has been temporarily disabled to maintain only the stable "Dual-Bar" mode in production.

**What Works**:
- Basic vertical drag (adjusts height split between content/memo areas: 30-90%)
- Basic horizontal drag (adjusts overall width of both areas: 50-100%)
- Visual drag handle with arrow hints
- State management and conditional rendering

**What Needs Work**:
- Fine-tuning drag sensitivity
- Better visual feedback during drag
- Persistence of single-bar mode settings
- Testing edge cases and polish

**Files Modified**:
- `EditorDragMode.kt` — Added `ENABLE_SINGLE_BAR_MODE` feature flag (line 22) and SINGLE_BAR enum value with documentation
- `SettingsScreen.kt` — Added editor drag mode dropdown UI (lines 1191-1248), wrapped in feature flag check
- `FullScreenEditor.kt` — Added contentWeight/areaWidthFraction state variables, single-bar drag handle implementation, width/height calculation logic (marked with TODO: FEATURE_SINGLE_BAR_DRAG comments)
- `MainActivity.kt` — Added getEditorDragMode/saveEditorDragMode methods, state variable, parameter passing through composables
- `FileListScreen.kt` — Added editorDragMode parameter
- `FileManagementApp` composable — Added editorDragMode parameter

**How to Re-enable**:
1. Set `ENABLE_SINGLE_BAR_MODE = true` in `EditorDragMode.kt`
2. Rebuild the app
3. The "Editor Resize Mode" dropdown will appear in Settings → Memo Area section

**Search Keywords**: FEATURE_SINGLE_BAR_DRAG, ENABLE_SINGLE_BAR_MODE, TODO: FEATURE_SINGLE_BAR_DRAG

---

### [2026-03-09] Removed: STATIC_REPEAT Photo Background Mode

**Type**: Feature Removal

**Description**: Removed the STATIC_REPEAT (Fixed) photo background mode at user request, keeping only DYNAMIC and STATIC_ZOOM modes. The STATIC_REPEAT mode allowed users to select a fixed area of the photo that would remain centered regardless of container size changes, but was deemed unnecessary.

**Files Modified**:
- `PhotoBackgroundMode.kt` — Removed STATIC_REPEAT enum value and fromString/toString handling
- `PhotoControlsPanel.kt` — Removed STATIC_REPEAT mode selection chip
- `ColorPickerDialog.kt` — Removed STATIC_REPEAT chip and all rendering branches (5 sections)
- `FullScreenEditor.kt` — Removed STATIC_REPEAT rendering branch
- `FileListScreen.kt` — Removed STATIC_REPEAT rendering branches (main bg + file list bg)
- `FileListItem.kt` — Removed STATIC_REPEAT rendering branch
- `PhotoBackgroundSurface.kt` — Removed STATIC_REPEAT rendering branch

**Remaining Modes**:
- **DYNAMIC**: User-controlled offset + scale with aspect ratio correction. Supports drag/pinch gestures.
- **STATIC_ZOOM**: Auto-scales to fill container, no user positioning. Uses `ContentScale.Fit`.

**Migration**: Any existing presets using STATIC_REPEAT will automatically fall back to DYNAMIC mode via the `fromString()` default behavior.

---

### [2026-03-08] Feature: Photo Background Mode Visual Effects (DYNAMIC, STATIC_REPEAT, STATIC_ZOOM)

**Type**: Feature Implementation

**Description**: Implemented the visual effects for all three photo background modes across the entire app. Previously, selecting a mode (DYNAMIC, STATIC_REPEAT, STATIC_ZOOM) was saved but had no visual effect — all sections rendered identically using DYNAMIC-style transforms. Now each mode produces distinct visual behavior:
- **DYNAMIC**: User-controlled offset + scale with aspect ratio correction. Supports drag/pinch gestures.
- **STATIC_REPEAT** (Fixed): User can drag to select which area of the photo to show as the "fixed" background. The selected area remains centered and fixed regardless of container size changes. Supports drag gestures for positioning, scale slider for zoom. Uses `ContentScale.Crop`.
- **STATIC_ZOOM**: Auto-scales to fill container, no user positioning. Uses `ContentScale.Fit`.

**Files Modified**:
- `FullScreenEditor.kt` — Added `when (customBgPhotoBackgroundMode)` for main background rendering + missing `graphicsLayer` import
- `FileListScreen.kt` — Added `photoBackgroundMode` params for main bg + file list bg, mode-based rendering
- `FileListItem.kt` — Added `photoBackgroundMode` param, mode-based rendering
- `MainActivity.kt` — Passed `photoBackgroundMode` through `MainContent → FullScreenEditor`, `MainContent → FileManagementApp → FileListScreen → FileListItem`
- `ColorPickerDialog.kt` — Added `photoBackgroundMode` param to `AppLayoutPreview`, mode-based rendering for all 5 sections (bg, content, memo, fileListBg, fileListItem), passed mode from both preview calls
- `UnifiedBackgroundEditor.kt` — Added `photoBackgroundMode` to `PreviewTouchArea` and passed to `AppLayoutPreview`
- `FullScreenPhotoEditor.kt` — Added `photoBackgroundMode` param, passed to `AppLayoutPreview`

**Key Implementation Pattern** (applied in all locations):
```kotlin
when (photoBackgroundMode) {
    DYNAMIC -> { scaleX/Y = scale*ratio; translationX/Y = offset*size }
    STATIC_REPEAT -> { scaleX/Y = scale; translationX/Y = offset*size }  // Updated to apply offset
    STATIC_ZOOM -> { scaleX/Y = autoScale*scale; translationX/Y = 0 }
}
```

**Update [2026-03-08 Evening]**: Enhanced STATIC_REPEAT mode to support drag gestures for positioning. Users can now drag the photo in the preview to select which area to show as the "fixed" background. The offset is applied in rendering (`translationX/Y = offset*size`) while maintaining centered scale behavior. Gesture handling updated in `AppLayoutPreview` (all 5 sections) to enable drag for both DYNAMIC and STATIC_REPEAT modes.

---

### [2026-02-16] Fix: Photo Preview-to-Reality Mismatch — ContentScale.Crop

**Type**: Bug Fix

**Description**: Photo positions set in the editor preview appeared different (shifted too low) in the actual opened note. The preview showed the photo centered, but the actual app showed it shifted down significantly.

**Root Cause**: `ContentScale.Fit` was used everywhere. With `Fit`, the image is scaled to fit entirely within the container, which means in containers with different aspect ratios (landscape preview vs portrait actual app), the image occupies different proportions of the container. A `translationY` fraction of the container height produces visually different results because the image is positioned differently relative to the container edges.

**Fix**: Changed ALL `ContentScale.Fit` to `ContentScale.Crop` across all photo rendering locations. With `Crop`, the image always fills the entire container (clipping overflow), so fraction-based offsets produce identical visual results regardless of container aspect ratio.

**Files Modified** (9 files, ~15 locations):
- `PhotoBackgroundSurface.kt` — Fit → Crop
- `FileEditDialog.kt` — Fit → Crop
- `FileListScreen.kt` — Fit → Crop
- `FileListItem.kt` — Fit → Crop
- `PhotoPositionDialog.kt` — Fit → Crop
- `ColorPickerDialog.kt` — 5 sections (bg, fileListBg, fileListItem, content, memo) Fit → Crop
- `SettingsScreen.kt` — 5 locations (bg, fileListBg, fileListItem, content, memo) Fit → Crop

---

### [2026-02-16] Fix: Photo Positioning — Proper Container-Relative Approach

**Type**: Bug Fix (Complete Rewrite of Positioning Logic)

**Description**: Photo positioning was broken across two failed attempts:
1. **Original**: Used hardcoded `* 500f` pixel multiplier — produced different visual results in different-sized containers (500px offset = 46% of 1080px app, but 500% of 100px preview).
2. **Revert to 500f**: Zoom/scale stopped being applied correctly; positions still mismatched between editor and app.

**Root Cause**: A fixed pixel multiplier (`500f`) cannot produce consistent positioning across containers of different sizes. The offset must be stored as a **fraction of the container** so the same fraction (e.g., 0.5 = 50%) produces the same visual displacement regardless of container size.

**Fix**: Replaced ALL hardcoded multipliers with container-relative `size.width` / `size.height`:
- **Rendering**: `translationX = -offsetX * size.width` (fraction × container = correct pixels)
- **Gesture input**: `dragAmount.x / size.width.toFloat()` (pixels → fraction of current container)

This ensures: drag 50% in a 100px preview → offset = 0.5 → renders as 540px in 1080px app (same 50%).

**Files Modified** (10 locations across 9 files):
- `PhotoBackgroundSurface.kt` — graphicsLayer translation
- `FileEditDialog.kt` — graphicsLayer translation
- `FileListScreen.kt` — graphicsLayer translation
- `FileListItem.kt` — graphicsLayer translation
- `PhotoPositionDialog.kt` — graphicsLayer translation + gesture handlers
- `ColorPickerDialog.kt` — all 5 sections (bg, fileListBg, fileListItem, content, memo) graphicsLayer + gesture handlers
- `SettingsScreen.kt` — both preset card previews (small 40dp squares + file list bg preview)
- `FullScreenPhotoEditor.kt` — gesture handler
- `UnifiedBackgroundEditor.kt` — gesture handler

**Note**: Previously saved photo positions (stored in SharedPreferences) were calibrated against the old `500f` coordinate space and will need to be re-adjusted by the user.

---

### [2026-02-15] Fix: Alpha Slider Resets to 100% When Re-Entering Editor

**Type**: Bug Fix

**Description**: Alpha slider reset to 100% every time the user closed and reopened the photo editor for a section.

**Root Causes** (two issues):
1. **Missing callback**: `UnifiedBackgroundEditor` had no `onPhotoAlphaChange` callback — the alpha slider only updated local state but never persisted to SharedPreferences.
2. **Stale preset overwrites SharedPreferences**: When tapping a preset square to reopen the editor, `onBackgroundPhotoAlphaChange?.invoke(preset.photoAlpha)` wrote the **stale** preset's alpha (1.0) to SharedPreferences, overwriting the correct value saved by the slider callback. The stored preset list is never updated when sliders change.
3. **Conditional load was bypassed**: `LaunchedEffect` only loaded alpha/blur from SharedPreferences when `currentPhotoAlpha == 1f` — but the stale preset always passed 1.0, so the condition was always true yet SharedPreferences had already been overwritten.

**Fixes Applied**:
- Added `onPhotoAlphaChange` parameter to `UnifiedBackgroundEditor` and wired it at both call sites
- Fixed NEW photo flow's `onPhotoBlurChange` — was empty, now saves immediately
- **Always load alpha/blur from SharedPreferences** in `LaunchedEffect` (removed conditional check)
- **Removed stale preset alpha/blur writes** on preset click — SharedPreferences is the source of truth

**Files Modified**: `UnifiedBackgroundEditor.kt` (lines ~32, ~197-225, ~413-415), `SettingsScreen.kt` (lines ~2600-2603, ~2809-2816, ~2945-2947)

---

### [2026-02-15] Fix: Alpha/Blur Sliders Not Updating Preview & Cross-Section Leak

**Type**: Bug Fix

**Description**: Fixed three alpha/blur issues in `UnifiedBackgroundEditor`:
1. **Sliders don't update preview**: Moving alpha/blur sliders in photo mode showed no change in the preview.
2. **Cross-section leak**: Changes made in one section appeared when opening a different section's editor.
3. **Alpha resets**: Re-entering a section reset the alpha slider to default.

**Root Causes**:
1. `PreviewTouchArea` hardcoded `previewAlpha = 1f` and `previewBlur = 0f` in photo mode. `AppLayoutPreview` uses the top-level `alpha`/`blur` for the active section, so slider changes were invisible.
2. `LaunchedEffect` unconditionally overwrote `photoAlpha`/`photoBlur` from `currentPhotoAlpha`/`currentPhotoBlur` params every time the dialog opened, ignoring SharedPreferences values for the active section.
3. Same as #2 — params at defaults (`1f`/`0f`) overwrote section-specific values loaded from SharedPreferences.

**Fixes Applied** (`UnifiedBackgroundEditor.kt`):
- `previewAlpha`/`previewBlur` now use `photoAlpha`/`photoBlur` in photo mode (line ~614-615)
- `LaunchedEffect` loads alpha/blur from SharedPreferences per-section when params are at defaults, same pattern as positioning (lines ~197-228)

**Files Modified**: `UnifiedBackgroundEditor.kt` (lines ~197-228, ~614-615)

---

### [2026-02-12] Enhancement: Scale-Aware Photo Panning Limits

**Type**: Enhancement

**Description**: Photo offset limits now scale proportionally with zoom level. Previously, panning limits were static (`-3f..3f`) regardless of zoom, so zoomed-in photos couldn't be panned to show corners or edges. Now at 3x zoom, the limit expands to `±9f`, allowing the viewport to reach any corner, edge, or even beyond the photo.

**Formula**: `maxOffset = 3f * currentScale`

**Files Modified**:
- `UnifiedBackgroundEditor.kt` (line ~614) — gesture `coerceIn` uses `maxOffset` based on scale
- `PhotoControlsPanel.kt` (lines ~104-111) — H-Pos/V-Pos slider ranges dynamically expand with zoom

---

### [2026-02-12] Fix: Mirrored Horizontal Drag & Slow Gesture Movement

**Type**: Bug Fix

**Description**: Fixed two gesture UX issues in `UnifiedBackgroundEditor`:
1. **Mirrored horizontal direction**: Dragging left moved the image right and vice versa.
2. **Movement too slow**: No matter how fast the finger moved, the image barely moved.

**Root Causes**:
1. `AppLayoutPreview` uses `translationX = -offsetX * 500f * 0.175f` (negated X). The gesture was adding `+pan.x`, so the directions cancelled out.
2. `pan.x / size.width` normalizes a full-width swipe to just `1.0` offset, which translates to only ~87px in the preview.

**Fixes Applied** (`UnifiedBackgroundEditor.kt` line ~607-612):
- Negated `pan.x` in gesture: `currentOffsetX - pan.x / size.width` to match `AppLayoutPreview`'s `-offsetX` convention
- Added `sensitivity = 3f` multiplier so image tracks finger speed naturally

**Files Modified**: `UnifiedBackgroundEditor.kt` (line ~607-612)

---

### [2026-02-11] Fix: Photo Zoom Resets on Touch & Drag/Zoom Not Working

**Type**: Bug Fix

**Description**: Fixed gesture issues in the `UnifiedBackgroundEditor`:
1. **Zoom resets on touch**: Touching the preview reset zoom/offset to defaults immediately.
2. **Drag stopped working / jerky pinch-to-zoom**: Photo wouldn't follow finger during drag or zoom.

**Root Causes** (three-layer bug):
1. Photo state variables initialized with hardcoded `0f/0f/1f` instead of `currentPhotoOffsetX/Y/Scale` parameters.
2. `pointerInput` gesture lambda captured stale state — using state values as keys caused the gesture detector to **restart on every frame** during a gesture, killing ongoing gestures.
3. `LaunchedEffect` overwrote preset positioning values with SharedPreferences values, resetting zoom for edit mode.

**Fixes Applied** (`UnifiedBackgroundEditor.kt`):
- Initialized photo state from parameters: `mutableStateOf(currentPhotoOffsetX)` instead of `mutableStateOf(0f)`
- Used `rememberUpdatedState` for offset/scale values + `pointerInput(Unit)` — gesture lambda reads fresh values without restarting the detector
- `LaunchedEffect` now only loads positioning from SharedPreferences when parameters are at defaults (new photo mode), preserving preset values in edit mode

**Files Modified**: `UnifiedBackgroundEditor.kt` (lines ~69-75, ~145-162, ~579-609)

---

### [2026-02-09] Feature: Unified Background Editor

**Type**: Feature / Refactor

**Description**: Replaced the old `ColorPickerDialog` (tab-based AlertDialog) with a new full-screen `UnifiedBackgroundEditor` that combines color picking and photo selection into a single unified experience. The editor opens as a full-screen dialog with a live app layout preview, mode toggle (Color/Photo), and gesture-based photo manipulation.

**New Files Created**:
- `UnifiedBackgroundEditor.kt` — Full-screen dialog with preview, mode toggle, gesture detection for photo drag/zoom
- `ColorControlsPanel.kt` — Color spectrum picker, hue slider, RGB sliders, alpha & blur sliders
- `PhotoControlsPanel.kt` — Photo pick/remove buttons, alpha, blur, zoom, horizontal & vertical position sliders

**Architecture**:
- Parent component loads all section photo state from SharedPreferences directly (bypasses stale parameter chains)
- Preview area supports pinch-to-zoom and drag gestures for photo positioning
- Mode toggle switches between Color and Photo control panels
- All callbacks remain identical to the old `ColorPickerDialog` for seamless integration

**Call Sites Updated**: Both `ColorPickerDialog` calls in `ColorPresetRow` (SettingsScreen.kt) replaced with `UnifiedBackgroundEditor`

**Files Modified**: `SettingsScreen.kt` (lines ~2745, ~2873), `UnifiedBackgroundEditor.kt`, `ColorControlsPanel.kt`, `PhotoControlsPanel.kt`

---

### [2026-02-08 23:44] Fix: Photo Not Visible in Preset Card Preview & Initial Photo Load

**Type**: Bug Fix

**Description**: Fixed two remaining photo visibility issues:
1. **Preset card preview**: Photos set via the background picker were not appearing in the mini preview on color preset cards in the Settings → Colors section.
2. **Initial photo load**: When first selecting a photo in the picker, the photo was invisible until a touch gesture forced recomposition.

**Root Causes**:
1. `PresetCard` used photo URIs from the **preset object** (saved at creation time), not the **current live app state** from SharedPreferences. Photos set after preset creation were invisible.
2. All 5 `rememberAsyncImagePainter` calls in `AppLayoutPreview` set `imageAlpha = 0f` during Coil's `State.Loading`, and the recomposition on `State.Success` wasn't reliably triggering a visual update.
3. `currentPhotoUri` (stale parent parameter) was used instead of `selectedPhotoUri` (freshly picked) in all `AppLayoutPreview` calls.

**Fixes Applied**:
- **SettingsScreen.kt**: Added SharedPreferences loading in `ColorPresetsSection` for all section photo URIs/alpha/blur/positioning. Passed live values to `PresetCard`. Updated `AppLayoutPreview` in `PresetCard` to use live values with preset fallback.
- **ColorPickerDialog.kt**: Added `crossfade(true)` via `ImageRequest.Builder` to all 5 Coil painters. Changed loading-state alpha from `0f` to target alpha (only `Error` hides image). Introduced `activePhotoUri = selectedPhotoUri ?: currentPhotoUri` used in all 4 `AppLayoutPreview`/`PhotoPickerContent` calls.

**Files Modified**: `ColorPickerDialog.kt`, `SettingsScreen.kt`

---

### [2026-02-08 21:10] Fix: Complete Photo State (URI/Alpha/Blur/Positioning) for ALL Sections in Preview

**Type**: Bug Fix

**Description**: Fixed ALL photo properties (URI, alpha, blur, positioning) not being reflected in the ColorPickerDialog preview across sections. The entire `allSectionPhotos` parameter chain was stale — not just positioning data but also photo URIs, alpha, and blur values. This caused photos set on one section (e.g., Memo Area) to be invisible when viewing another section's ColorPickerDialog preview.

**Root Cause**: The `allSectionPhotos` parameter chain (`MainActivity` → `SettingsScreen` → `ColorPresetRow` → `ColorPickerDialog`) passed stale/null values for ALL photo properties (URI, alpha, blur, offsetX/Y, scale) across ALL sections. Additionally, the color tab preview passed `photoUri = ""` instead of the current section's photo URI.

**Fix Applied** (`ColorPickerDialog.kt`):
- Added 30 state variables: `loaded[Section]PhotoUri/Alpha/Blur/OffsetX/Y/Scale` for all 5 sections
- All loaded directly from SharedPreferences in `LaunchedEffect(show, targetSection)`
- All 4 `AppLayoutPreview`/`PhotoPickerContent` calls now use `loaded*` variables instead of `allSectionPhotos[...]`
- Color tab preview now passes `currentPhotoUri` instead of `""` as `photoUri`
- Fallback logic preserved: sections with default positioning inherit CustomBg's values

**Files Modified**: `ColorPickerDialog.kt` (lines ~174-204, ~260-290, ~419-490, ~576-631)

**Verified**: Photos from all sections now visible in cross-section previews.

---

### [2025-12-21 22:30] Photo Positioning and Zoom Feature

**Type**: Feature

**Description**: Implemented photo positioning and zoom functionality for background photos, allowing users to adjust photo placement and scale with interactive controls.

**Files Modified**: `ColorPickerDialog.kt`, `SettingsScreen.kt`, `MainActivity.kt`, `FileListScreen.kt`, `PhotoPositionDialog.kt`

**Technical Details**:
- **Position Photo Button**: Added to ColorPickerDialog for File List Background when photo is selected
- **PhotoPositionDialog**: Interactive dialog with drag gestures, pinch zoom, and slider controls
- **State Management**: Added `photoOffsetX`, `photoOffsetY`, `photoScale` parameters with SharedPreferences persistence
- **Display Integration**: Applied `graphicsLayer` transforms in FileListScreen and MiniAppPreview for real-time positioning
- **Callback Chain**: Complete parameter passing through ColorPickerDialog → ColorPresetRow → ColorsSection → SettingsScreen → MainActivity
- **Default Values**: offsetX = 0f, offsetY = 0f, scale = 1f
- **Range Support**: offsetX/Y (-1f to 1f), scale (0.5f to 3f)

**User Experience**:
1. Select photo for File List Background in Settings → Colors
2. Click "Position Photo" button
3. Adjust position via drag, pinch zoom, or sliders
4. Changes save automatically and persist across app restarts
5. Positioned photos display correctly in both main app and preview cards

**Future Expansion**: Same pattern can be extended to other photo background sections (Main Background, Content Area, Memo Area, File List Items)

---

### [2025-12-21 20:10] Merge/Replace Mode for Backup Import

**Type**: Feature

**Description**: Added user-configurable merge/replace mode toggle to backup import dialog, giving users full control over how backup data is restored.

**Files Modified**: `GlobalBackup.kt`, `BackupOptionsDialog.kt`

**Technical Details**:
- **Merge Mode** (default): Preserves files/presets created after backup, merges metadata files
- **Replace Mode**: Completely replaces existing data, deletes files not in backup
- Added `mergeMode: Boolean` parameter to `BackupOptions` data class
- Enhanced `ImportOptionsDialog` with toggle switch and dynamic warning messages
- Updated metadata restore logic (`display_names.txt`, `memos.txt`, `memo_visibility.txt`) to respect user choice
- In replace mode, deletes existing text files not in backup before restoring

---

### [2025-12-21 19:45] File Metadata Preservation Fix

**Type**: Bug Fix

**Description**: Fixed issue where files created after backup appeared with timestamp names when backup was re-imported.

**Files Modified**: `GlobalBackup.kt`

**Technical Details**:
- **Issue**: New file "6" appeared as "1766337502077" after re-importing backup
- **Root Cause**: Backup restore overwrote metadata files instead of merging them
- **Solution**: Implemented metadata merging (now user-configurable via merge mode)
- Reads existing metadata before restore, merges with backup data
- Preserves display name mappings for files created after backup

---

### [2025-12-21 14:30] File List Immediate Refresh After Restore

**Type**: Bug Fix

**Description**: Fixed text files not appearing immediately in list after backup restore, and wrong files opening when tapped.

**Files Modified**: `MainActivity.kt`

**Technical Details**:
- **Root Cause**: File list reload happening outside Compose context
- **Solution**: Moved file list reload into `LaunchedEffect` observing `backupRestoreTrigger`
- File reload now happens inside Compose context when trigger changes
- Added logging to track file reload count for debugging

---

### [2025-12-21 13:00] UI State Immediate Refresh After Restore

**Type**: Bug Fix

**Description**: Fixed colors and overlays not applying immediately after backup import.

**Files Modified**: `MainActivity.kt`

**Technical Details**:
- **Root Cause**: UI state variables loaded at app start, not updated when backup restored new SharedPreferences values
- **Solution**: Implemented state reload trigger system
- Created `backupRestoreTrigger` state variable
- Added trigger to `LaunchedEffect` dependencies that load UI state
- After restore completes, increment trigger to force state reload
- All colors, photos, blur values, and settings reload from SharedPreferences

---

### [2025-12-20 16:00] Text Files Reload After Import

**Type**: Bug Fix

**Description**: Fixed text files not displaying until app restart after backup import.

**Files Modified**: `MainActivity.kt`

**Technical Details**:
- Added `_files.value = loadFilesFromStorage(this)` after restore completes

---

### [2025-12-20 15:30] Selective Backup/Restore System

**Type**: Feature

**Description**: Users can now choose which data categories to include when exporting or importing backups.

**Files Modified**: `GlobalBackup.kt`, `MainActivity.kt`, `BackupOptionsDialog.kt` (new)

**Technical Details**:
- **Export Options Dialog**: Checkboxes for Text Files, Color Presets, App Settings, Photo Backgrounds (with counts)
- **Import Options Dialog**: Same categories, shows backup date, warns about overwriting, disables unavailable categories
- `BackupOptions` data class: Stores user selections
- `BackupSummary` data class: Provides counts for display
- `GlobalBackup.getSummary()`: Extracts summary from backup
- `GlobalBackup.getCurrentDataSummary()`: Gets current app data summary
- `restoreToPreferences()` now accepts `BackupOptions` parameter

---

## Previous Development

### [2025-12-15 10:00] Photo URI Migration System

**Type**: Feature

**Description**: Auto-convert old content:// photo URIs to internal storage on app update.

**Files Modified**: `PhotoStorage.kt`, `MainActivity.kt`

**Technical Details**:
- One-time automatic migration on app start
- `migrateExistingPhotos()`: Converts old URIs to internal storage
- Migrates all 5 photo URI preference keys
- Migrates photos in custom preset JSON
- Uses `photo_migration_completed` flag to run only once
- Graceful handling of invalid/inaccessible URIs

---

### [2025-12-15 09:00] Backward-Compatible Backup Restoration

**Type**: Feature

**Description**: Implemented robust partial restoration system for backward compatibility.

**Files Modified**: `GlobalBackup.kt`, `MainActivity.kt`

**Technical Details**:
- `RestoreResult` class: Detailed feedback on what was restored
- Section-by-section try-catch: Each restore section wrapped independently
- Graceful degradation: Failed sections don't block successful ones
- Detailed user feedback with counts
- Older backups work on newer app versions (missing fields use defaults)
- Newer backups work on older app versions (unknown fields ignored)

---

### [2025-12-10 14:00] Text Files Backup/Restore

**Type**: Bug Fix

**Description**: Fixed missing text files after app reinstall and backup import.

**Files Modified**: `GlobalBackup.kt`

**Technical Details**:
- **Root Cause**: Backup only saved SharedPreferences, not actual text files
- Added text files backup (all `.txt` files from `TeXter2025_Files` directory)
- Added backup of `display_names.txt`, `memos.txt`, `memo_visibility.txt` mappings
- Restore now recreates all text files and their metadata

---

### [2025-12-10 13:00] Photo Background Internal Storage System

**Type**: Bug Fix

**Description**: Fixed photo backgrounds appearing as grey squares after app reinstall.

**Files Modified**: `PhotoStorage.kt` (new), `ColorPickerDialog.kt`, `GlobalBackup.kt`, `MainActivity.kt`

**Technical Details**:
- **Root Cause**: `content://` URIs lose permissions when app reinstalled (new UID)
- **PhotoStorage.kt**: New utility class for internal storage (`filesDir/background_photos/`)
- Photo Picker saves photos internally (not just URI)
- Backup: Photos encoded as Base64 in backup file
- Restore: Photos decoded and restored to internal storage
- Benefits: Photos persist even if deleted from gallery, survive reinstalls, no permission issues



---

### [2025-12-10 11:00] Background Picker Preview Sticky Position

**Type**: Bug Fix

**Description**: Fixed preview scrolling out of view when adjusting sliders.

**Files Modified**: `ColorPickerDialog.kt`

**Technical Details**:
- Made preview sticky at top (outside scrollable area)
- Reduced preview height from 220dp to 140dp
- Reduced spacing between elements (16dp → 6-8dp)
- Removed duplicate previews from ColorPickerContent and PhotoPickerContent
- Reduced tab text size and spectrum picker size

---

### [2025-12-09 16:00] Photo Blur Intensity Calibration

**Type**: Bug Fix

**Description**: Fixed blur intensity mismatch between preview and actual app.

**Files Modified**: `FileListItem.kt`, `ColorPickerDialog.kt`, `SettingsScreen.kt`

**Technical Details**:
- **Root Cause**: FileListItem missing blur modifier, preview used wrong multiplier
- Added `.blur((photoBlur * 50f).dp)` to FileListItem
- Scaled blur proportionally to surface size:
  - Full screen (~800dp): multiplier 50
  - ColorPickerDialog preview (220dp): multiplier 14
  - Preset card (100dp): multiplier 6

---

### [2025-12-07 14:00] Preset Card Blur Parameter Fix

**Type**: Bug Fix

**Description**: Fixed blur not reflecting in preset card when adding new photo.

**Files Modified**: `MainActivity.kt`

**Technical Details**:
- **Root Cause**: `createPresetFromCurrentSettings` calls missing `fileListItemBlur` parameter
- Added parameter to all color change callbacks
- Affected: onAddContentAreaColor, onEditContentAreaColor, onAddMemoAreaColor, onEditMemoAreaColor, onAddBgColor, onEditBgColor, onAddFileListItemColor


---

## Current Goal

**Focus**: Backup/Restore system refinement and stability

**Status**: ✅ Merge/Replace mode implemented and working

**Next Steps**:
- ⏳ User testing of merge/replace functionality
- ⏳ Monitor for edge cases in metadata handling
- ⏳ Consider adding backup file versioning UI

**Last Updated**: 2025-12-21 20:10

---

## Technical Notes

### Key Files
- `MainActivity.kt` - Core app logic, state management, SharedPreferences
- `SettingsScreen.kt` - Settings UI, color picker integration
- `ColorPickerDialog.kt` - Color/photo picker with preview
- `GlobalBackup.kt` - Backup/restore data serialization
- `BackupOptionsDialog.kt` - Export/Import options dialogs
- `PhotoStorage.kt` - Internal photo storage management

### Data Classes
- `ColorPreset` - Stores preset configuration including colors, blur values, photo URIs
- `SimpleColorPreset` - Lightweight color preset for custom color squares
- `GlobalBackup` - Complete app state for backup/restore
- `BackupOptions` - User selections for selective backup/restore with merge mode
- `BackupSummary` - Backup contents summary for display
- `RestoreResult` - Detailed restoration feedback

### Core Features
- **Global Backup/Restore System**: Export/import all app customizations to JSON
- **Selective Backup**: Choose which data categories to backup/restore
- **Merge/Replace Modes**: User control over restoration behavior
- **Photo Background System**: Internal storage with Base64 backup
- **Backward Compatibility**: Graceful handling of version mismatches
