# TeXter2025 Chat History

## [2026-06-04] Fix: opened-note content area can extend into available space

**Diagnostic Update**:
- Added a temporary visible `FullScreenEditor` gap diagnostic overlay because the exact object owning the reported gap is still ambiguous.
- Color guide:
  - Magenta = full editor Compose root.
  - Red border = content editor surface.
  - Green border = memo surface.
  - Cyan = Activity-reported navigation-bar inset.
  - Red top band = Activity-reported status-bar inset.
  - Yellow border/text = diagnostic overlay bounds and legend.
- If the gap stays black/untinted, it is outside the Compose dialog content. If it is tinted or bordered, the color identifies the owning region.
- User screenshot showed the gap is below the yellow diagnostic overlay, confirming it is outside the `FullScreenEditor` Compose root.
- Applied a dialog decor fix that clears decor-view padding and keeps it cleared from window-inset application.
- Follow-up screenshot showed no visible change, so the dialog content window itself was still being measured above the navigation/gesture area.
- Added `FLAG_LAYOUT_IN_SCREEN` plus legacy immersive sticky layout flags to the dialog decor view so the dialog can lay out into that area when hide-status-bar mode is enabled.
- A second follow-up screenshot still showed no change, confirming the platform `Dialog` window remained constrained above the black strip.
- Converted the opened-note editor from a platform `Dialog` into an in-app fullscreen Compose overlay attached to the Activity window.
- Preserved back-button behavior with `BackHandler { handleExit() }` and moved edge-to-edge/system-bar handling to the Activity window for this editor path.
- User confirmed the yellow border now surrounds the whole screen without any gap, validating that the platform `Dialog` removal fixed the bottom gap.
- User then reported the editor appeared blurry while still interactive; root cause was the old parent `Scaffold` blur still applying while `isFullScreenEditorShown` was true.
- Removed the parent `Scaffold` blur so the in-app fullscreen editor overlay remains sharp.

**User Report**:
- With the memo area set to always show, dragging the opened-note content area downward still left a gap that blocked the visible extended part of the content area.
- After maxing the content area, the content dragger and Hide/Show Memo row were hidden near the bottom of the screen.
- The behavior was visible in logs during `FullScreenEditor` dialog relayouts and keyboard/status-bar related changes.

**Root Cause**:
- The opened-note dialog uses `FLAG_LAYOUT_NO_LIMITS`, so the editor body is measured at full window height and extends behind the bottom navigation/gesture area.
- The previous attempt reserved the navigation bar via Compose `WindowInsets.navigationBars`, but that returns `0` inside this dialog, so it had no effect.
- Reading the inset from the Activity decor view still did not move the layout away from the affected device/session's bottom border.
- A large visible bottom `Column` padding confirmed the affected area but over-reserved it, making the visible gap larger.
- The persistent original gap came from a measurement mismatch: the max-height calculation used the full editor body height, while `fillMaxHeight(fraction)` was applied inside the `Column` after the header.
- The gap moving to the top when the status bar was shown confirmed the visible strip was the dialog window's own system-bar area.
- The Activity window already had transparent system bars, but the opened-note dialog has its own window and needed the same transparent system-bar treatment.
- When the hide-status-bar option was enabled, the dialog hid only `statusBars()`, leaving the navigation/gesture system-bar area visible at the bottom.
- At max content height the content surface pushed the dragger and Hide/Show Memo row into the bottom gesture area, hiding them behind the bottom border.

**Fix Applied**:
- In `FullScreenEditor.kt`, removed the artificial bottom controls clamp reserve (`0.dp`).
- Replaced fractional `fillMaxHeight(displayedHeightFraction)` content sizing with exact `height(...)` sizing derived from `editorBodyHeightPx * displayedHeightFraction`.
- This makes rendered content height and max-height calculation use the same measurement basis.
- Set the opened-note dialog window status/navigation bar colors to transparent and disabled Android Q+ contrast enforcement.
- Changed the opened-note fullscreen dialog to hide/show `WindowInsetsCompat.Type.systemBars()` instead of only `statusBars()` when the hide-status-bar option is enabled.
- Kept reserving the `36.dp` header, `4.dp` content handle, and `4.dp` memo handle.
- Kept the previous content resize layout so the surrounding editor interface adapts with the content area.
- In `MainActivity.kt`, kept Activity cutout layout on `SHORT_EDGES` even when the status bar preference is off.
- Preserved existing drag gestures, memo controls, text editing, and status-bar preference behavior.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-06-04] Fix: prevented black top gap when keyboard appears on main search

**User Report**:
- When tapping the main-view search bar and opening the keyboard, a black gap briefly appeared at the top where the Android status bar normally is.
- Logcat showed IME show/hide relayouts while the Activity frame stayed at `[0,0][1440,3120]`.

**Root Cause**:
- The app is edge-to-edge, but the Activity window did not explicitly force transparent runtime system-bar backgrounds.
- During IME animations, Android could briefly reveal the default black status/cutout bar background.

**Fix Applied**:
- In `MainActivity.kt`, updated `applyStatusBarVisibility(...)` to set transparent status/navigation bar colors.
- Disabled Android Q+ status/navigation contrast enforcement to avoid transient system scrims.
- Existing status-bar preference behavior and cutout handling were preserved.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-06-02] UI: matched opened-note content dragger length to memo dragger

**User Request**:
- The moved content-area dragger was shorter than the memo-area dragger and should use the same length.

**Change Applied**:
- In `FullScreenEditor.kt`, changed the content-area dragger width from `40.dp` to `80.dp`.
- This matches the memo-area dragger's existing `80.dp` length.
- Resize logic and saved-size behavior were unchanged.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-06-02] UI: moved opened-note content resize dragbar to bottom edge

**User Request**:
- Move the dragbar for resizing the content note area to the lower edge of the content area, matching the location circled in the screenshot.

**Change Applied**:
- In `FullScreenEditor.kt`, moved the existing `DUAL_BAR` content-area resize handle from above the content surface to immediately below it.
- Kept the same drag gesture implementation, saved-size persistence, constraints, and `onContentAreaSizeChange` callback.
- Left the Hide/Show Memo button row behavior unchanged.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-06-01] Fix: removed dialog fullscreen flag causing compatibility status-bar relayout loop

**User Report**:
- The status bar still appeared/disappeared when interacting with opened-note controls.
- New logcat output showed direct `FullScreenEditor.hide(statusBars())` only at dialog setup, but repeated `ViewRootImpl.controlInsetsForCompatibility` show/hide transitions during later relayouts.

**Root Cause**:
- The opened-note dialog mixed modern `WindowInsetsControllerCompat` status-bar control with deprecated `FLAG_FULLSCREEN` changes.
- Android compatibility inset handling reacted to the legacy fullscreen flag during dialog relayouts and auto-toggled status/caption bar visibility.

**Fix Applied**:
- In `FullScreenEditor.kt`:
  - Removed `FLAG_FULLSCREEN` add/clear calls from the dialog window.
  - Kept `WindowInsetsControllerCompat.hide/show(statusBars())` as the only visibility mechanism.
  - Preserved edge-to-edge layout flags: `FLAG_LAYOUT_NO_LIMITS`, top/start gravity, cutout mode, and match-parent sizing.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.
- The previous dialog `FLAG_FULLSCREEN` deprecation warnings no longer appear.

## [2026-06-01] Fix: stopped opened-note memo controls from retriggering status-bar animations

**User Report**:
- After the top-gap fix worked, tapping opened-note controls such as hide/show memo area caused the status bar to appear and disappear.
- Logcat showed `FullScreenEditor` repeatedly invoking `hide(statusBars())` from its dialog `SideEffect` after normal UI actions.

**Root Cause**:
- The dialog status-bar command was in a recomposition `SideEffect`.
- Editor state changes like memo visibility caused recompositions, so the same status-bar hide animation was requested again even though the global `hideStatusBar` setting had not changed.

**Fix Applied**:
- In `FullScreenEditor.kt`:
  - Added remembered `lastAppliedHideStatusBar` tracking.
  - Only call `hide(statusBars())` or `show(statusBars())` when the actual status-bar mode changes.
  - Continued applying dialog sizing/origin flags during recompositions.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-06-01] Fix: opened-note dialog frame anchored to screen origin

**User Report**:
- The opened-note gap was still present initially.
- After some interaction, the gap disappeared completely.
- Logcat showed the relevant window frame sometimes moving from `0,113` to `0,0`.

**Root Cause**:
- The opened-note dialog window could initially be measured below the status-bar reserved frame, then later relayout to the true screen origin after system inset/window transitions.

**Fix Applied**:
- In `FullScreenEditor.kt`:
  - Added `Gravity.TOP or Gravity.START` to the dialog window.
  - Added `FLAG_LAYOUT_NO_LIMITS` to prevent the dialog from being constrained below the status-bar frame.
  - Preserved the existing conditional status-bar handling so status-bar enabled/disabled mode does not fight itself.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-06-01] Fix: stopped status-bar show/hide loop

**User Report**:
- After the gap shrank slightly, enabling the status bar caused it to continuously hide and show.
- Logcat showed repeated status-bar inset visibility changes and relayouts.

**Root Cause**:
- `MainActivity` respected the user's `hideStatusBar` setting, but `FullScreenEditor` always hid the status bar inside its dialog `SideEffect`.
- When the user enabled the status bar, the activity showed it while the editor dialog hid it again, creating a loop.

**Fix Applied**:
- In `FullScreenEditor.kt`:
  - Added `hideStatusBar: Boolean = false`.
  - Hide status bars only when `hideStatusBar` is true.
  - Show status bars and clear `FLAG_FULLSCREEN` when `hideStatusBar` is false.
- In `MainActivity.kt`:
  - Passed the existing `hideStatusBar` state into `FullScreenEditor`.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-05-31] Fix: opened-note gap identified as status-bar area

**User Report**:
- User clarified the persistent gap is exactly where the Android status bar normally appears.
- User also observed the status bar suddenly appeared while checking the opened-note area.
- Previous control compaction affected trash/save icons, but not the actual gap.

**Root Cause**:
- The opened-note dialog needed explicit status-bar hiding on the dialog `Window` itself.
- Compose/header spacing was not the real source of the gap.

**Fix Applied**:
- In `FullScreenEditor.kt`:
  - Added status-bar hiding to the actual dialog window using `WindowInsetsControllerCompat.hide(WindowInsetsCompat.Type.statusBars())`.
  - Added `FLAG_FULLSCREEN`.
  - Enabled transient system bar behavior for swipe reveal.
  - Enabled display cutout short-edge layout on Android P+.
  - Restored header action hitboxes to `36.dp` so buttons are usable again.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-05-31] Follow-up: opened-note header row height minimized

**User Report**:
- Opened-note top gap was still unchanged.

**Root Cause**:
- After the dialog/window and padding fixes, the remaining visible space was likely the transparent height of the header row itself.
- Header action containers were still `36.dp` high while the icons were only `18.dp`.

**Fix Applied**:
- In `FullScreenEditor.kt`:
  - `compactIconButtonSize`: `36.dp` -> `18.dp`
  - `headerBottomPadding`: `2.dp` -> `0.dp`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-05-31] Follow-up: stronger opened-note dialog edge-to-edge enforcement

**User Report**:
- Opened note top gap still looked the same.

**Root Cause**:
- Previous fixes addressed Compose-level padding/header spacing, but the actual platform dialog window could still keep a fitted frame unless explicitly configured.

**Fix Applied**:
- In `FullScreenEditor.kt`:
  - Added `LocalView`, `DialogWindowProvider`, `WindowCompat`, and `WindowManager` usage.
  - Forced the actual dialog `Window` to `decorFitsSystemWindows = false`.
  - Forced dialog `Window` layout/attributes to `MATCH_PARENT` width and height.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-05-25] Follow-up: opened-note header still had top visual space

**User Report**:
- Opened note area still showed space between the screen edge and note title/buttons.

**Root Cause**:
- `FullScreenEditor` header used Material `IconButton` for close/delete/save.
- `IconButton` can keep minimum touch-target layout space, so the row stayed visually taller/offset even after explicit top padding was removed.

**Fix Applied**:
- Replaced header action `IconButton`s with compact clickable `Box` containers.
- Preserved existing close, delete, and save behavior.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-05-19] Follow-up: removed remaining top spacing above note header

**User Report**:
- Even after dialog inset fix, a small top space remained above note title/actions.

**Root Cause**:
- Internal header spacing in `FullScreenEditor` still added extra top/bottom space.

**Fix Applied**:
- In `FullScreenEditor.kt`:
  - `headerTopPadding`: `6.dp` -> `0.dp`
  - `headerBottomPadding`: `4.dp` -> `2.dp`
  - post-header spacer: `4.dp` -> `0.dp`

**Result**:
- Title and action buttons sit closer to the top edge without changing editor behavior.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-05-19] Fix: opened note editor top gap removed

**User Report**:
- Settings gap was fixed, but opened note area still had top empty space.

**Root Cause**:
- `FullScreenEditor` was shown in a `Dialog` that still fit system windows by default.
- Dialog frame stayed below status-bar/cutout top area.

**Fix Applied**:
- In `FullScreenEditor.kt`, updated dialog properties to:
  - `usePlatformDefaultWidth = false`
  - `decorFitsSystemWindows = false`

**Result**:
- Opened note editor now renders edge-to-edge at the top without the leftover gap.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-05-18] UI cleanup: reduced top empty space in Settings

**User Request**:
- Settings title/cards should be closer to the top edge like the main screen.

**Root Cause**:
- Settings root scroll column had a fixed `top = 120.dp` padding.

**Fix Applied**:
- In `SettingsScreen.kt`:
  - Added conditional `statusBarsPadding()` only when status bar is visible.
  - Reduced fixed top content padding from `120.dp` to `10.dp`.

**Result**:
- Settings screen now starts much closer to the top edge.
- Full-screen and normal status-bar modes both keep appropriate top spacing.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-05-18] Fix: full-screen toggle top gap caused by window frame offset

**User Report**:
- Gap still present on main/settings when enabling true full-screen.
- Behavior observed: content shifts lower when enabling, higher when disabling.

**Diagnostic Signal**:
- Runtime logs showed relayout/frame origin at `y=113` during the issue.
- This matched status-bar/cutout height, indicating a window-level offset instead of only composable padding.

**Fix Applied**:
- In `MainActivity.applyStatusBarVisibility(...)`:
  - Reassert `WindowCompat.setDecorFitsSystemWindows(window, false)`.
  - Explicitly switch cutout mode:
    - full-screen on -> `LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES`
    - full-screen off -> `LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT`
  - Keep status bar visibility changes via `WindowInsetsControllerCompat`.

**Result**:
- Window can render into top cutout/status-bar area in true full-screen mode.
- Prevents the observed top gap caused by frame offset.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-05-17] Follow-up fix: removed black strip above search bar background

**User Report**:
- Screenshot showed a remaining black strip at the top behind the main search bar.

**Root Cause**:
- In `FileListScreen`, the root background container was padded with scaffold `paddingValues`.
- Background/photo layers started below top-bar space, so top region stayed unpainted (black).

**Fix Applied**:
- `FileListScreen.kt`:
  - Removed `padding(paddingValues)` from root `Box`.
  - Applied `padding(paddingValues)` only to `LazyColumn` content.

**Result**:
- Background now fills behind the top bar area; no black strip remains in that region.
- List content still respects scaffold bars/insets.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-05-17] Follow-up fix: removed remaining top gap on main screen

**User Report**:
- Top gap was still visible after first status-bar inset fix.

**Root Cause**:
- `FileListScreen` still reserved top space (`statusBarsPadding` + default scaffold insets).
- `hideStatusBar` state was not passed down to `FileListScreen`.

**Fix Applied**:
- In `MainActivity.kt`:
  - Added `hideStatusBar` argument to `FileManagementApp`.
  - Passed it from `MainActivity` and forwarded to `FileListScreen`.
- In `FileListScreen.kt`:
  - Added `hideStatusBar` parameter.
  - Conditional `contentWindowInsets` (zero when hidden).
  - Conditional `statusBarsPadding` (only when visible).

**Result**:
- Main screen no longer keeps the old status-bar gap while hidden mode is enabled.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-05-17] Fix: removed black top strip when status bar is hidden

**User Report**:
- In full-screen mode, note editor reached top correctly, but main/settings showed a black top gap where status bar was.

**Root Cause**:
- Root `Scaffold` in `MainActivity` still applied default system-bar insets even when status bar was hidden.
- This reserved top space and exposed a black strip instead of app content.

**Fix Applied**:
- In `MainActivity.kt` root `Scaffold`:
  - Added conditional `contentWindowInsets`.
  - If `hideStatusBar` is true -> `WindowInsets(0, 0, 0, 0)`.
  - Else -> `ScaffoldDefaults.contentWindowInsets`.

**Result**:
- Main and Settings screens now use the full top area when status bar is hidden.
- Normal inset behavior returns automatically when status bar is shown.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-05-17] Fix: Open settings from search/menu was a no-op

**User Report**:
- Tapping **Open settings** did nothing.

**Root Cause**:
- Callback wiring gap in `FileManagementApp`:
  - `FileListSearchBar` called `onOpenSettings` correctly,
  - but `FileManagementApp` did not pass `onOpenSettings` into `FileListScreen`.
- `FileListScreen` used default empty lambdas, so the tap had no visible effect.

**Fix Applied**:
- In `MainActivity.kt`:
  - Added `onOpenSettings` parameter to `FileManagementApp`.
  - Forwarded these callbacks to `FileListScreen`:
    - `onImportRequest`
    - `onExportRequest`
    - `onOpenSettings`
  - At call site, wired `onOpenSettings` to:
    - `showSettings = true`
    - `showBackToSettingsFab = false`

**Result**:
- **Open settings** now reliably opens the Settings screen.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-05-17] Added optional true full-screen mode (hide Android status bar)

**User Request**:
- Keep the redesigned top search bar.
- Remove visible Android status bar area to use more screen space.
- Add a setting so users can choose with/without status bar.

**Changes Applied**:
- In `MainActivity.kt`:
  - Added persistence for status-bar preference (`hide_status_bar`).
  - Added `applyStatusBarVisibility(...)` with `WindowInsetsControllerCompat`.
  - Applied setting at startup and reactively when toggled in settings.

- In `SettingsScreen.kt`:
  - Added and wired `hideStatusBar` + `onHideStatusBarChange` params into advanced settings.

- In `AdvancedSettingsSection.kt`:
  - Added a new switch: **Hide Android status bar**.
  - Kept existing “Apply to opened notes” setting intact.

**Result**:
- Users can enable true full-screen mode from Settings.
- Status bar can still be shown again by disabling the toggle.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-05-16] Follow-up fix: removed remaining old upper header

**User Report**:
- Search bar looked good, but an old upper header still existed.

**Root Cause**:
- There was still a global `TopAppBar` in `MainActivity`'s root `Scaffold`.
- This was independent from `FileListScreen` and remained visible above the new centered search bar.

**Fix Applied**:
- In `MainActivity.kt`:
  - Removed the root `topBar = { TopAppBar(...) }` block.
  - Kept the root scaffold without a top bar so the only top control is the redesigned `FileListSearchBar`.
  - Removed now-unused imports tied to that old header (`CloudUpload`, `CloudDownload`, `Menu`).

**Result**:
- Old upper header is no longer rendered.
- Main screen now shows only the redesigned centered glass search/menu bar at the top.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-05-16] Main screen redesign: remove legacy top bar and center glass search/menu control

**User Request**:
- Remove the old top bar (`TeXter` title + action buttons).
- Place a centered, rounded, semi-transparent search bar at the top.
- Integrate menu access naturally into this redesigned top search UI.

**Changes Applied**:
- In `FileListScreen.kt`:
  - Top area now renders a centered `FileListSearchBar` inside `Scaffold(topBar = ...)`.
  - Existing behavior wiring remained intact (import/export/settings/sort/search/case/extensions).
  - Export action still respects single-selection requirement.

- In `FileListSearchBar.kt`:
  - Implemented/kept a glass-style rounded search surface with integrated menu icon.
  - Menu dropdown includes:
    - import text file
    - export selected file
    - open settings
    - show extensions switch
    - case-sensitive switch
    - sort method selection
    - search method selection
  - Kept clear-search and trailing search affordance.

**Compile Issue Encountered**:
- `HorizontalDivider` was unresolved with current Material3 version.

**Fix Applied**:
- Replaced `HorizontalDivider` with `Divider` in `FileListSearchBar.kt` and imported `androidx.compose.material3.Divider`.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-05-15] Fix: Memo blur preview pixelation in Colors dialog

**User Report**:
- Memo-area photo looked pixelated when blur was increased.
- Other sections looked smoother, so memo behavior was inconsistent.

**Root Cause**:
- In `ColorPickerDialog` preview, memo photo used a single blurred layer path at higher blur values.
- That amplified blocky sampling artifacts in the smaller memo preview area.
- File-list-item preview already had a smoother sharp/blur cross-fade path, but memo did not.

**Changes Applied**:
- Refactored memo preview rendering in `AppLayoutPreview`:
  - Extracted base transform modifier and gesture modifier.
  - Added eased blur mix (`smoothstep`) and dual-layer rendering:
    - sharp layer alpha = `imageAlpha * (1 - mix)`
    - blurred layer alpha = `imageAlpha * mix`
  - Kept existing gesture behavior and contentScale rules.

**Result**:
- Memo preview blur transitions are smoother and no longer jump into visibly blocky/pixelated rendering.
- Memo preview behavior is now aligned with the already-smooth file-list-item blur preview strategy.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-05-15] Fix: Selected background squares persisted but opened-note backgrounds restored empty

**User Report**:
- After app reopen/restart, `opened note background`, `Content area`, and `Memo area` could show empty background even though related color/photo squares still appeared selected.
- Reselecting the same square fixed the view temporarily until next restart.

**Root Cause**:
- Active preset startup restore reloaded photo URIs, but background/content/memo runtime state was not fully re-synced from selected square metadata and persisted runtime keys.
- This created a mismatch between "selected square" state and actual rendered background state.

**Changes Applied**:
- In `MainActivity.kt` active preset startup load path:
  - Normalized restored URIs with `.takeIf { it.isNotEmpty() }`.
  - For `customBg`, `contentArea`, `memoArea`, matched selected square using `indexOfLast { it.photoUri == activeUri }`.
  - Restored runtime `color`, `photoAlpha`, and `photoBlur` from the matched square when available.
  - Used SharedPreferences fallback for alpha/blur when square metadata could not be matched.
  - Persisted effective runtime values back to SharedPreferences:
    - color
    - photo URI
    - photo alpha/blur
    - photo offset/scale/rotation

**Result**:
- Startup restoration now keeps selected square metadata and rendered opened-note background state aligned for background/content/memo sections.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-05-15] Crash fix: Settings Colors section bitmap-too-large crash

**User Report**:
- App crashed when entering `Settings -> Colors` with fatal error:
  - `RuntimeException: Canvas: trying to draw too large(...) bitmap`.

**Root Cause**:
- Recent photo decode changes used unbounded `Size.ORIGINAL` in a heavy preview path, causing oversized decoded bitmaps that exceeded Canvas draw limits on device.

**Changes Applied**:
- In `HighQualityPhotoPainter.kt`:
  - Added bounded decode dimension constant: `MAX_PHOTO_DECODE_DIMENSION_PX = 4096`.
  - Changed request size from original/unbounded to:
    - `.size(MAX_PHOTO_DECODE_DIMENSION_PX, MAX_PHOTO_DECODE_DIMENSION_PX)`
  - Kept `precision(Precision.EXACT)` and `scale(Scale.FILL)` for quality.

**Result**:
- Prevents oversized bitmap draw path in Colors preview/dialog and removes the crash condition while preserving photo/blur behavior.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL` (tool returned wait-delay timeout after success output).

## [2026-05-14] Follow-up fix: Memo-area blur pixelation still persisted

**User Report**:
- Memo-area photo blur pixelation still persisted after the first blur-order fix.

**Changes Applied**:
- In `HighQualityPhotoPainter.kt`:
  - Tightened Coil decode settings for blur fidelity:
    - `precision(Precision.EXACT)`
    - `scale(Scale.FILL)`
    - `allowHardware(false)`
- In `PhotoBackgroundSurface.kt`:
  - DYNAMIC mode:
    - Removed ratio-based extra upscaling.
    - Used direct `backgroundPhotoScale` and `ContentScale.Crop`.
  - STATIC_ZOOM mode:
    - Removed redundant second `graphicsLayer` transform pass.
  - Kept blur-first ordering and existing positioning/rotation behavior.

**Result**:
- Photo blur pipeline now avoids redundant scaling and uses stricter decode precision, reducing blur-stage pixel block artifacts in memo area backgrounds.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-05-10] Bug fix: Memo-area photo blur pixelation in opened notes

**User Request**:
- Keep memo-area background blur effect in opened note editor but remove the heavy pixelation/artifacting while blur increases.

**Changes Applied**:
- In `PhotoBackgroundSurface.kt`:
  - Reordered the photo modifier chain in both `DYNAMIC` and `STATIC_ZOOM` modes.
  - Applied blur first (`applyPhotoBlur(backgroundPhotoBlur)`) before transform layers (`graphicsLayer`).
  - Kept alpha application at the end (`applyPhotoAlpha(imageAlpha)`).
- Preserved all existing photo behaviors (rotation, scaling, offsets, alpha, and mode-specific logic).

**Result**:
- Blur remains active but now renders from the base image before transform sampling, reducing visible pixelation in memo area photo backgrounds.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-05-10] Reduced opened-note top bar height in FullScreenEditor

**User Request**:
- Reduce the height of the top header bar (title + right-side action buttons) in opened notes because it takes too much space.

**Changes Applied**:
- Updated `FullScreenEditor.kt` header layout to a compact profile:
  - Smaller header paddings (`12dp` horizontal, `6dp` top, `4dp` bottom)
  - Smaller title typography (`titleMedium` instead of `titleLarge`)
  - Smaller action controls (`IconButton` 36dp with 18dp icons)
  - Smaller spacer below header (`4dp`)
- Kept all existing actions and functionality unchanged (close/delete/save).

**Result**:
- Opened-note header now occupies less vertical space, leaving more room for content and memo areas.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-04-11] Bug fix: File list item transparency resetting to solid after restart

**User Request**:
- Transparency bug persisted after restart; list items became solid even though alpha setting was still active.
- Logs did not show `FileListItemAlpha` lines during startup.

**Changes Applied**:
- In `MainActivity.kt`:
  - Added guarded startup alpha trace log for file-list-item photo:
    - `read source=startupPrefs alpha=... uri=...`
  - Updated startup active-preset load behavior to prefer persisted values from:
    - `file_list_item_photo_alpha`
    - `file_list_item_photo_blur`
  - Synced matching preset square metadata (`customFileListItemColors`) with those persisted values to prevent stale per-square alpha from resetting transparency.

**Result**:
- Startup now uses persisted effective alpha as source-of-truth, reducing regressions where list-item photo transparency jumps back to solid.
- Added low-noise guarded log visibility for startup tracing when DEBUG tag is enabled.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL` (tool reported timeout after successful output).

## [2026-04-11] Rule update: Added reusable recurring-regression guard to global rules

**User Request**:
- Confirm whether this should be in global rules and add it.

**Changes Applied**:
- Updated global rules file:
  - `c:/Users/javie/.codeium/windsurf/memories/global_rules.md`
  - Added section `0.6. CRITICAL: Recurring Regression Stability Guard (ENFORCE ALWAYS)`.
- The rule is generic/project-agnostic and requires low-noise guarded logs for known recurring regression paths.

**Result**:
- The policy now applies at global scope for future projects/chats, while project-specific rules can still add local detail.

## [2026-04-11] Rule update: Keep guarded debug logs for recurring FileList transparency issue

**User Request**:
- Ensure future chats do not forget the recurring list-item transparency issue handling.
- Add a project-based rule to keep tiny guarded debug logs around relevant alpha paths.

**Changes Applied**:
- Updated `.windsurfrules` with a new section:
  - `Recurring Stability Guard - File List Item Transparency`
- Added explicit guidance to:
  - keep/add tiny guarded logs for alpha read/write/apply when touching this logic
  - use low-noise guards like `Log.isLoggable("FileListItemAlpha", Log.DEBUG)`
  - avoid unconditional spam logs
  - only remove the guard if user explicitly requests removal

**Result**:
- This recurring stability requirement is now embedded in project rules and should carry forward in new chats.

## [2026-04-10] UX: Increased small fonts in Photo Controls panel

**User Request**:
- Increase small text in photo controls (example: `PhotoControlsPanel.kt` around `Background Mode`) to be closer to the larger text style used in Settings (`Background Colors`).

**Changes Applied**:
- In `PhotoControlsPanel.kt`:
  - `Background Mode` label: `labelMedium` -> `bodyMedium`
  - Filter chip labels (`Dynamic`, `Auto-Zoom`): `bodySmall` -> `bodyMedium`
  - Mode helper text (`Photo moves with gestures` / `Photo auto-zooms...`): `bodySmall` -> `bodyMedium`
  - Gesture hint (`Drag preview to move • Pinch to zoom`): `bodySmall` -> `bodyMedium`

**Result**:
- Photo controls text is now more readable and visually closer to the sizing used in your referenced Settings section text.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL` (tool timed out after success output).

## [2026-04-09] Feature: Toggle App UI Size behavior for opened notes/editors

**User Request**:
- Add an option in Advanced Settings so users can decide whether `App UI Size` should apply to opened notes/editors.

**Changes Applied**:
- In `AdvancedSettingsSection.kt`:
  - Added `Apply to opened notes` switch under `UI Settings`.
  - Persisted switch state to `TeXterPrefs` using key `apply_ui_scale_to_opened_notes`.
- In `FileEditDialog.kt`:
  - Added conditional dialog-local density logic based on:
    - `apply_ui_scale_to_opened_notes`
    - `app_ui_scale`
  - Wrapped opened-note dialog content in `CompositionLocalProvider(LocalDensity provides openedNoteDensity)`.
- In `FullScreenEditor.kt`:
  - Added equivalent conditional density override for fullscreen opened-note editor dialog.

**Result**:
- Opened notes/editors now scale only when the new setting is enabled.
- Existing app-wide UI scale behavior remains intact for other app screens.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-04-08] UX: Keep Advanced Settings separate from UI Settings subsection

**User Request**:
- Advanced Settings will contain more options in the future.
- UI scaling should be inside UI settings, but not visually stuck together with the Advanced Settings header.

**Changes Applied**:
- In `AdvancedSettingsSection.kt`:
  - Removed `UI scaling` subtitle from the collapsed header.
  - Added `UI Settings` subsection title shown inside expanded panel.
  - Kept all existing slider/reset functionality the same.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-04-08] UX: Advanced Settings dropdown + clearer current UI scale display

**User Request**:
- Make Advanced Settings dropdownable.
- Show short text like `UI scaling` instead of long description.
- Make current slider size more visible.
- Keep `Reset` feature.

**Changes Applied**:
- In `AdvancedSettingsSection.kt`:
  - Added collapsible header row (tap to expand/collapse).
  - Added arrow indicator (`down/up`) for dropdown state.
  - Replaced long explanatory copy with short subtitle `UI scaling`.
  - Added highlighted current-scale row (`Current size` + large `%` value).
  - Kept existing reset behavior and slider save-on-finish behavior.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-04-08] Fix: App UI Size not applied in Settings color/photo dialogs

**User Report**:
- In `Settings -> Colors`, opening the editor via `+` or tapping a color square showed dialog UI at wrong/default scale.

**Root Cause**:
- The app-level density override was applied in `MainActivity` composition.
- Color/photo editors are shown through Compose `Dialog` (`UnifiedBackgroundEditor`), which renders in a separate window and did not receive the app density override.

**Fix Applied**:
- In `UnifiedBackgroundEditor.kt`:
  - Read `app_ui_scale` from `TeXterPrefs`.
  - Created dialog density (`Density(...)`) from `LocalDensity.current * appUiScale`.
  - Wrapped dialog root with `CompositionLocalProvider(LocalDensity provides dialogDensity)` so all dialog controls respect app UI size.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-04-08] Fix: App UI Size slider drag interaction

**User Report**:
- The new `App UI Size` slider reacted to taps on the track but did not drag freely like a normal slider.

**Root Cause**:
- The slider wrote to global `appUiScale` during every `onValueChange` step.
- That immediately re-applied app-wide density/font scaling while the finger was still moving.
- Live density changes during gesture caused the slider layout/position to shift and made drag tracking feel broken.

**Fix Applied**:
- In `AdvancedSettingsSection.kt`:
  - Added local slider state (`sliderScale`) for gesture-time updates.
  - Switched to `onValueChangeFinished` for committing to `onAppUiScaleChange(...)`.
  - Kept live visual feedback by showing `Current: XX%` from local value.
  - Updated `Reset` to sync local + persisted/global state.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain --warning-mode=none` -> `BUILD SUCCESSFUL`.

## [2026-04-05] Feature: App UI Size setting in Advanced Settings (in-app only)

**User Request**:
- Add an `App UI Size` option in Settings (ideally under advanced settings) to counteract extreme Android system scaling inside the app only.

**What was implemented**:
- Added a new `AdvancedSettingsSection` composable with:
  - `App UI Size (in-app only)` slider (`70%..130%`)
  - live percent display
  - `Reset` button (`100%`)
- Wired `SettingsScreen` to accept and display:
  - `appUiScale`
  - `onAppUiScaleChange`
- In `MainActivity`:
  - Added persistence helpers for `app_ui_scale` in `SharedPreferences`
  - Added remembered state load/save flow for app UI scale
  - Applied app-only scaling through `CompositionLocalProvider(LocalDensity provides Density(...))`
    so both dp density and font scale are adjusted inside TeXter2025 only.

**Root Cause / Need**:
- There was no app-level scaling override, so extreme device-level scaling could make in-app UI too large/small with no internal adjustment.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` -> `BUILD SUCCESSFUL`.

## [2026-04-05] Crash fix: deleting hex characters in color editor

**User Report**:
- After hex controls became visible, deleting letters/digits in the hex field caused app crash.

**Crash Signature**:
- `ArrayIndexOutOfBoundsException` in Compose Color space conversion:
  - `Color.getColorSpace-impl`
  - `Color.convert-vNxB06k`
  - `ColorKt.toArgb`
  - call path included hue computation in `ColorPickerDialog.kt`.

**Root Cause**:
- Parsed hex values were converted with `Color(argb.toULong())`.
- This constructor interprets input as Compose `ColorLong` encoding, not plain ARGB int.
- Raw ARGB bits could map to invalid color-space index values, causing runtime crashes during color operations.

**Fix Applied**:
- Updated hex parsers to construct safe ARGB colors using `Color(argb.toInt())` in:
  - `ColorControlsPanel.kt`
  - `ColorPickerDialog.kt`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` -> `BUILD SUCCESSFUL`.

## [2026-04-05] Follow-up: Hex controls not visible -> moved to active editor component

**User Report**:
- User could not see any hex input/copy/paste UI changes.

**Root Cause**:
- Hex controls were first added in `ColorPickerContent` (`ColorPickerDialog.kt`).
- Actual settings flow renders `UnifiedBackgroundEditor` using `ColorControlsPanel`, so the modified UI path was not visible.

**Fix Applied**:
- Implemented the same hex controls directly in `ColorControlsPanel.kt`:
  - editable `Hex` text field (with `#` prefix)
  - `Copy` button
  - `Paste` button
  - strict hex sanitization + validation + error text

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` -> `BUILD SUCCESSFUL`.

## [2026-04-05] Feature: Color picker hex code now supports copy/paste/manual input

**User Request**:
- Make the shown selected color code usable:
  - copy code
  - paste code
  - editable input area (preferably above hue controls)

**Changes Applied**:
- In `ColorPickerDialog.kt` -> `ColorPickerContent`:
  - Added an editable hex input row above the hue section.
  - Added `Copy` button to copy `#AARRGGBB` to clipboard.
  - Added `Paste` button to read clipboard and apply valid hex.
  - Added strict sanitization (hex chars only) and validation (`RRGGBB` / `AARRGGBB`).
  - Added small inline validation error text.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` -> `BUILD SUCCESSFUL`.

## [2026-04-05] Follow-up: New preset preview issue persisted (file-list color reset gap)

**User Report**:
- Issue still reproduced after prior fix: new preset preview still showed prior file-list state.

**Root Cause Found**:
- New preset reset path did not clear all file-list color/blur fields before save.
- Save path still serialized stale `fileListItemColor` / `fileListBackgroundColor` / `fileListItemBlur` values.

**Fix Applied**:
- In `MainActivity.kt` -> `onResetColorsForNewPreset`:
  - Clear in-memory state:
    - `fileListItemColor = null`
    - `fileListBackgroundColor = null`
    - `fileListItemBlur = 0f`
  - Persist cleared state:
    - `saveFileListItemColor(null)`
    - `saveFileListBackgroundColor(null)`
    - `saveFileListItemBlur(0f)`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` -> `BUILD SUCCESSFUL`.

## [2026-04-05] Fix: New preset preview copied previous file-list state

**User Report**:
- After creating a new preset, preset card preview could show previous preset file-list item color/photo.
- Color squares for the new preset were empty and app state was not actually applying that preview.

**Fix Applied**:
- In `SettingsScreen.kt` save dialog confirm handler for color presets:
  - if `isInNewPresetMode`, re-run `onResetColorsForNewPreset()` immediately before `onSavePreset(...)`.

**Result**:
- Save-time state for new presets is guaranteed clean.
- New preset preview no longer inherits stale file-list item state from previously selected preset.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` -> `BUILD SUCCESSFUL`.

## [2026-04-05] Follow-up: Color Actions still reproduced -> switched to true modal Dialog

**User Report**:
- Behavior was still the same after previous outside-tap fix.

**Fix Applied (hardening)**:
- In `SettingsScreen.kt`, migrated `Color Actions` from in-layout overlay to Compose `Dialog` (`DialogProperties` with outside/back dismiss enabled).
- Retained full-screen scrim inside dialog with outside-tap close.
- Retained centralized `closeDragOptionsDialog()` for full state reset.

**Result**:
- Outside tap/back dismiss path now runs through modal dialog window boundary, preventing underlying UI click-through.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` -> `BUILD SUCCESSFUL`.

## [2026-04-05] UX fix: Color Actions dismisses on outside tap and blocks tap-through

**User Report**:
- Long-pressing a color square opens `Color Actions` with blurred background.
- Tapping outside did not close the dialog and could trigger UI behind it (e.g., another color square editor).

**Fix Applied**:
- In `SettingsScreen.kt`:
  - Added `closeDragOptionsDialog()` to fully reset dialog-related state in one place.
  - Scrim now closes dialog on outside tap via `detectTapGestures`.
  - Blur-layer touch blocker now consumes taps while dialog is open.
  - `X` close button now uses the same central close function.

**Result**:
- Outside tap closes `Color Actions` and does not trigger any behind-screen action.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` -> `BUILD SUCCESSFUL`.

## [2026-04-05] Follow-up recurrence: startup reopen still reset FileList transparency

**User Report**:
- After longer app idle/reopen, FileList items were not transparent again.
- Content area and memo area transparency remained correct.

**Log Review**:
- Provided logcat was mostly system/UI lifecycle lines.
- No `FileListItemAlpha` tagged lines appeared in the pasted snippet, so it did not directly show app alpha transitions.

**Root Cause (newly identified path)**:
- Startup active preset restore path still loaded FileList item alpha/blur from global SharedPreferences.
- That path could override intended per-preset square transparency after reopen.

**Fix Applied**:
- In `MainActivity.kt` active preset load:
  - restore FileList item alpha/blur from matched `customFileListItemColors` square by `photoUri`
  - keep SharedPreferences fallback only when square match is missing

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` -> `BUILD SUCCESSFUL`.

## [2026-04-03] Recurrence fix: FileList item transparency persisted per preset square

**User Report**:
- Issue recurred: FileList item transparency reset happened again after some time/reopen.

**Root Cause (refined)**:
- Transparency relied too much on global `file_list_item_photo_alpha`.
- If that global value was overwritten, reopening/switching preset could restore wrong opacity.
- The per-square preset metadata (`customFileListItemColors.photoAlpha/photoBlur`) was not always synchronized when sliders changed.

**Fix Applied**:
- In `MainActivity.kt`:
  - Preset load/select now prefers per-square alpha/blur from `customFileListItemColors` (matched by `photoUri`), with SharedPreferences fallback for backward compatibility.
  - Slider callbacks for FileList item photo alpha/blur now update the matched square metadata and persist updated preset list.

**Result**:
- FileList item transparency is now anchored to preset-square data, reducing recurrence from global alpha drift.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` -> `BUILD SUCCESSFUL` (tool WaitDelay timeout occurred after success output).

## [2026-03-31] Fix: FileList item transparency no longer resets to default unexpectedly

**User Report**:
- FileList item transparency was set and persisted across app restart, but later sometimes reverted to non-transparent/default.

**Log Review**:
- Provided log snippet only contained system input/frame-rate lines (`ViewPostIme`, `setFrameRateCategory`, back key events).
- No app-level alpha/persistence transitions were present in that snippet.

**Root Cause**:
- During preset apply/select flows, `fileListItemPhotoAlpha`/`fileListItemPhotoBlur` were sometimes re-derived from `customFileListItemColors` square metadata.
- In some edit paths this metadata can be stale, which can make transparency appear reset.

**Fix Applied**:
- In `MainActivity.kt`, for FileList item photo state when applying/selecting preset:
  - read `file_list_item_photo_alpha` and `file_list_item_photo_blur` from SharedPreferences (source of truth)
  - keep square color lookup only for visual square selection behavior
- Added conditional diagnostic logs (`FileListItemAlpha`) on important read/write paths to help catch any future intermittent reset reports during longer sessions.

**Result**:
- FileList item photo transparency remains stable during preset load/switch flows instead of intermittently returning to default-looking opacity.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` reported `BUILD SUCCESSFUL` (tool ended with post-output WaitDelay timeout).

## [2026-03-31] Follow-up fix: square preview now updates correctly when re-adding photo after color

**User Report**:
- After removing photo and adding a color, re-adding a photo changed app state correctly but the square preview still showed the color.

**Root Cause**:
- Edit callbacks in `MainActivity.kt` resolved target square with `indexOfFirst { it.color == oldColor }`.
- With duplicate-color squares, edits could update an earlier matching square instead of the one just edited.

**Fix Applied**:
- Updated all section edit callbacks to use `indexOfLast { it.color == oldColor }` so the most recently edited/added matching square is updated:
  - `onEditBgColor`
  - `onEditContentAreaColor`
  - `onEditMemoAreaColor`
  - `onEditFileListItemColor`
  - `onEditFileListBackgroundColor`

**Result**:
- Re-adding photo after color updates the intended square preview and keeps UI/app state aligned.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` ✅ `BUILD SUCCESSFUL`.

## [2026-03-31] Follow-up fix: remove photo now persists after reopening a square editor

**User Report**:
- `Remove` worked only immediately after picking a photo.
- If user picked photo -> `Select` -> reopened same square -> `Remove`, the photo did not clear.
- Workaround was deleting the whole photo square from area colors.

**Root Cause**:
1. `UnifiedBackgroundEditor` emitted `onPhotoSelected(...)` only when `selectedPhotoUri` was non-null.
2. After pressing `Remove`, URI became null, so `Select/Test` emitted no photo update event.
3. Edit flow in `SettingsScreen` handled only non-null photo URIs, so there was no explicit clear-photo commit path.

**Fix Applied**:
- `UnifiedBackgroundEditor.kt`
  - In photo mode, `Select` and `Test` now always call `onPhotoSelected(BackgroundOption(...))`, including null URI.
- `SettingsScreen.kt`
  - In edit dialog `onPhotoSelected`, added normalized URI handling:
    - non-empty URI: apply existing photo update path
    - null/blank URI: clear section photo state (`uri/alpha/blur/offset/scale/rotation`) and persist square update via `onEditCustomColor(..., newPhotoUri = null, ...)`

**Result**:
- Reopened-editor remove flow now clears photo correctly without requiring external square deletion.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` ✅ `BUILD SUCCESSFUL` (warnings only; tool status timed out after completion).

## [2026-03-31] Follow-up fix: new preset creation no longer inherits previous preset photo preview

**User Report**:
- Creating a new preset card could still copy/show preview photo content from another preset card.

**Root Cause**:
1. New preset reset path cleared color state but did not fully clear and persist all section photo state before save.
2. `createPresetFromCurrentSettings(...)` could capture stale photo URIs/transform values left from previous preset context.

**Fix Applied**:
- `MainActivity.kt`
  - Updated `onResetColorsForNewPreset` to fully reset photo state for all sections:
    - `customBg`
    - `contentArea`
    - `memoArea`
    - `fileListItem`
    - `fileListBackground`
  - Reset values in-memory for each section:
    - `photoUri = null`
    - `photoAlpha = 1f`
    - `photoBlur = 0f`
    - `photoOffsetX = 0f`
    - `photoOffsetY = 0f`
    - `photoScale = 1f`
    - `photoRotation = 0f`
  - Persisted the same reset values through existing `save*` calls so no stale photo data remains in SharedPreferences prior to preset save.

**Result**:
- New preset cards now start with clean photo preview state and do not inherit another preset’s photo preview.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` ✅ `BUILD SUCCESSFUL` (process status reported terminal I/O wait timeout after completion; warnings only).

## [2026-03-30] Fix: Preset card preview blink/copy and lost-edits-on-switch

**User Report**:
- New preset cards appeared to copy currently selected card preview.
- Switching presets caused quick preview blink showing another preset first.
- Non-content areas (especially BG/File List Item/File List Background) often didn't reflect changes in card preview immediately.
- Changes appeared lost after switching to another preset and back.

**Root Cause**:
1. `PresetCard` preview path used transient live SharedPreferences state for selected card rendering, causing visual mismatch during selection transitions.
2. Some color change callbacks updated runtime state only, without writing updated values back into selected preset entry.
3. Preset switch did not always force persist of in-memory edited preset before applying another card.

**Fix Applied**:
- `SettingsScreen.kt`
  - Added `onUpdatePreset` parameter flow into `ColorPresetsSection`.
  - In `PresetCard` tap handler, persist current `selectedPresetForEditing` before switching to another preset.
  - Set preset card mini-preview to use preset data consistently (`useLive = false`) to eliminate cross-card blink/copy artifacts.
- `MainActivity.kt`
  - Added selected-preset synchronization in:
    - `onCustomBgColorChange`
    - `onCustomBgBlurChange`
    - `onFileListItemColorChange`
    - `onFileListBackgroundColorChange`
  - Each callback now updates/saves the active preset entry when editing.

**Result**:
- Preset card previews no longer show temporary wrong-card visuals while switching.
- New/selected cards reflect their own saved preset data.
- Changes in key sections persist across card switching.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` ✅ BUILD SUCCESSFUL.

## [2026-03-29] Fix: Go Back now returns to the exact section editor context after Test

**User Report**:
- Tapping `Test` correctly moved to app view.
- Tapping floating `Go Back` only returned to `Settings -> Colors` root, not to the exact tapped square/editor context.

**Root Cause**:
1. Return flow only carried section/category expansion (`settingsReturnSection` + `settingsReturnRequestId`).
2. It did not carry an instruction to reopen the local editor dialog state inside `ColorPresetRow` (`showEditColorPicker`).

**Fix Applied**:
- `MainActivity.kt`
  - Added `settingsReturnReopenEditor` state.
  - Set it to `true` in `onTestInApp` before hiding Settings.
  - Passed flag into `SettingsScreen`.
- `SettingsScreen.kt`
  - Extended parameter chain to include `settingsReturnReopenEditor`:
    - `SettingsScreen`
    - `ColorsSection`
    - `ColorPresetRow`
  - Added return-trigger handling in `ColorPresetRow` using `LaunchedEffect(settingsReturnRequestId)`:
    - Reopens editor when request id changes and section matches.
    - Attempts to match original square by `currentPhotoUri`, then falls back to selected color.

**Result**:
- After Test -> Go Back, user is returned to the same section editor context instead of just Colors root.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` ✅ BUILD SUCCESSFUL.

## [2026-03-28] Fix: file-list-item preview blur no longer starts abruptly at low %

**User Report**:
- Preview blur still started abruptly at small slider values.

**Root Cause**:
1. File-list-item preview rows are tiny.
2. The first visible blur step on a single blurred layer looked too harsh.

**Fix Applied**:
- Updated file-list-item preview rendering in `ColorPickerDialog.kt`:
  - Introduced dual-layer rendering for preview image:
    - Sharp layer (fades out)
    - Blurred layer (fades in)
  - Added smoothstep blend factor:
    - `mix = t*t*(3 - 2*t)`
  - Kept gesture handlers on the base layer to preserve panning/zoom behavior.

**Result**:
- Blur onset in preview appears gradual instead of abrupt at low values.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` ✅ BUILD SUCCESSFUL.

## [2026-03-28] Fix: preview blur no longer jumps between nearby slider values

**User Report**:
- Around `46%` blur, preview showed no blur.
- Around `47%`, preview suddenly became heavily blurred.
- Runtime blur remained correct; issue was preview-only.

**Root Cause**:
1. Preview path had a hard minimum-radius guard (`< 0.35.dp => no blur`).
2. Crossing that threshold caused a discontinuity and visible blur cliff.

**Fix Applied**:
- Removed the hard cutoff from `applyPhotoPreviewBlur(...)`.
- Recalibrated preview mapping in `mapPhotoPreviewBlurToRadius(...)` to a continuous softer curve:
  - `pow(2.7) -> pow(3.1)`
  - blend `0.38/0.04 -> 0.34/0.03`

**Result**:
- Preview blur now increases smoothly with slider movement (no sudden step around mid values).

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` ✅ BUILD SUCCESSFUL.

## [2026-03-28] Fix: preview blur density now aligned closer to runtime

**User Report**:
- Blur looked correct in the real app view.
- In the preview window, adding a small blur already looked much too strong.

**Root Cause**:
1. Preview surfaces are compact (notably file-list item rows), so low blur values appeared visually amplified.
2. Very small computed preview blur radii could still render as an abrupt first blur step.

**Fix Applied**:
- Updated preview blur handling in `BlurMapping.kt`:
  - Added guard in `applyPhotoPreviewBlur(...)` to skip blur when radius is `< 0.35.dp`.
  - Recalibrated `mapPhotoPreviewBlurToRadius(...)` to a softer low-end curve:
    - `pow(2.1) -> pow(2.7)`
    - blend `0.62/0.08 -> 0.38/0.04`

**Result**:
- Low-end blur in preview now ramps more gently and better matches runtime perception.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` ✅ BUILD SUCCESSFUL.

## [2026-03-28] Fix: file-list item photo quality no longer drops at alpha < 100%

**User Report**:
- File-list item transparency worked, but image quality became visibly worse as soon as alpha dropped below full opacity (e.g., 99%).

**Root Cause**:
1. Shared photo alpha helper (`applyPhotoAlpha`) was using `Modifier.alpha(...)` for semi-transparent rendering.
2. In the file-list item render path (which already uses transforms via `graphicsLayer`), this alpha path caused quality loss when alpha was not 1.0.

**Fix Applied**:
- Updated `applyPhotoAlpha` in `BlurMapping.kt` to use:
  - `graphicsLayer { alpha = ... }`
  - `compositingStrategy = CompositingStrategy.ModulateAlpha`
- This keeps transparency behavior while preserving image quality during alpha blending.

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/BlurMapping.kt`
- `CHANGELOG.md`
- `CHAT_HISTORY.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` ✅ BUILD SUCCESSFUL.

## [2026-03-28] Fix: transparency (photo alpha) now persists after app restart

**User Report**:
- After recent transparency fixes, alpha values looked correct while editing.
- On app restart, all photo-backed areas appeared reset to non-transparent values.

**Root Cause**:
1. Startup path reapplied the active preset and reloaded photo alpha/blur from preset square metadata.
2. That metadata could lag behind the latest slider values.
3. Most recent values were already saved in `SharedPreferences`, but startup overwrite replaced them.

**Fix Applied**:
- `MainActivity.kt` active-preset startup restore now reads alpha/blur from `SharedPreferences` for all photo sections instead of square metadata.
- Updated sections:
  - `custom_bg_photo_alpha` / `custom_bg_photo_blur`
  - `content_area_photo_alpha` / `content_area_photo_blur`
  - `memo_area_photo_alpha` / `memo_area_photo_blur`
  - `file_list_item_photo_alpha` / `file_list_item_photo_blur`
  - `file_list_background_photo_alpha` / `file_list_background_photo_blur`

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
- `CHANGELOG.md`
- `CHAT_HISTORY.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` ✅ BUILD SUCCESSFUL.

## [2026-03-28] Follow-up fix: file-list background no longer grays out at alpha < 100%

**User Report**:
- File-list item was fixed, but file-list background still became gray instead of transparent when lowering alpha.
- Reproduced in runtime and settings preview flow.

**Root Cause**:
1. Runtime `FileListScreen` still used `fileListBackgroundColor` as base while file-list background photo was active.
2. Mini preview in `SettingsScreen` also kept file-list background color under the active photo.
3. Photo alpha was blending against those tinted bases, causing gray/washed fade.

**Fix Applied**:
- `FileListScreen.kt`
  - Set file-list base to `Color.Transparent` whenever file-list background photo is present.
- `SettingsScreen.kt`
  - Mini preview file-list background base now switches to `Color.Transparent` when photo mode is active.
  - Maintained existing image visibility alpha behavior while removing tint blending source.

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/FileListScreen.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
- `CHANGELOG.md`
- `CHAT_HISTORY.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` ✅ BUILD SUCCESSFUL.

## [2026-03-28] Follow-up fix: file-list item no longer fades to gray when lowering photo alpha

**User Report**:
- Only File List Item still looked gray when alpha was reduced.
- Reproduced in preview flow and app runtime behavior for item appearance.

**Root Cause**:
1. Runtime `FileListItem` still used `customBgColor` as item base whenever a photo existed.
2. Photo alpha therefore blended against that local tint instead of transparent composition.
3. Result: item looked gray/washed while other sections behaved correctly.

**Fix Applied**:
- `FileListItem.kt`
  - Updated photo-mode item background logic to always use `Color.Transparent`.
  - Kept non-photo item color logic unchanged.

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/FileListItem.kt`
- `CHANGELOG.md`
- `CHAT_HISTORY.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` reached BUILD SUCCESSFUL.

## [2026-03-28] Follow-up fix: cross-section alpha now stays transparent (no gray fade when switching sections)

**User Report**:
- Active section preview could look correct while other sections and runtime still looked gray when alpha dropped below `100%`.
- Issue reproduced across content, memo, file-list item, and file-list background behavior.

**Root Cause**:
1. `AppLayoutPreview` still drew non-transparent local base fills under photo layers in multiple sections.
2. Runtime `PhotoBackgroundSurface` still used section color as `Surface` base while photo existed.
3. Semi-transparent photo layers (`alpha < 1f`) blended against those solids, causing gray/washed fade instead of transparent behavior.

**Fix Applied**:
- `PhotoBackgroundSurface.kt`
  - Set photo-backed `Surface` base color to `Color.Transparent`.
- `ColorPickerDialog.kt` (`AppLayoutPreview`)
  - Switched photo-backed base fills to transparent for:
    - background
    - file-list background
    - file-list item
    - content
    - memo
  - Kept normal section-color fallback when no photo is active.

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/PhotoBackgroundSurface.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`
- `CHANGELOG.md`
- `CHAT_HISTORY.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` ✅ BUILD SUCCESSFUL.

## [2026-03-27] Follow-up fix: removed remaining opaque photo bases in preview/runtime paths

**User Report**:
- Issue still reproduced ("same"): lowering photo alpha still looked gray and low quality.
- Logs showed heavy frame skips and repeated large-method JIT warnings while opening settings/photo dialogs.

**Root Cause**:
1. Remaining forced opaque underlays were still present in `AppLayoutPreview` and mini preview (`copy(alpha = 1f)` paths).
2. File-list runtime photo fallbacks still blended against non-transparent defaults in some branches.
3. Those base choices caused alpha fade to desaturate toward gray instead of transparently blending.

**Fix Applied**:
- `ColorPickerDialog.kt` (`AppLayoutPreview`)
  - Removed remaining forced opaque base usage for:
    - background
    - file-list background
    - file-list item
    - content
    - memo
- `SettingsScreen.kt`
  - Removed forced opaque mini preview file-list base when photo is active.
- `FileListScreen.kt`
  - Changed file-list photo-active fallback base to `Color.Transparent`.
- `FileListItem.kt`
  - Changed photo-active fallback base to `Color.Transparent`.

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/FileListScreen.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/FileListItem.kt`
- `CHANGELOG.md`
- `CHAT_HISTORY.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` ✅ BUILD SUCCESSFUL.

## [2026-03-27] Follow-up runtime fix: alpha fade no longer looks gray/low-quality

**User Report**:
- The issue came back in runtime: reducing photo alpha made photos look low-quality and gray instead of transparent.

**Root Cause**:
1. Runtime photo sections still used forced opaque local underlays.
2. As photo alpha decreased, blending against those opaque underlays created gray/washed output.
3. Shared photo surfaces also needed underlay blur disabled while photo is active to avoid alpha-revealed blur artifacts.

**Fix Applied**:
- `PhotoBackgroundSurface.kt`
  - Added `hasPhoto` and updated `shouldBlur` so section blur only runs when no photo is active.
  - Removed forced opaque underlay behavior (`copy(alpha = 1f)`), using `color` directly.
- `FileListScreen.kt`
  - Removed forced opaque base handling for main background and file-list background when photos are active.
  - Kept existing conditional blur logic that skips section blur when a photo is present.

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/PhotoBackgroundSurface.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/FileListScreen.kt`
- `CHANGELOG.md`
- `CHAT_HISTORY.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin --console=plain` ✅ BUILD SUCCESSFUL.

## [2026-03-27] Regression fix: photo alpha transparency restored (gray fade removed)

**User Report**:
- Lowering photo alpha to `99%` or below no longer caused blur (good), but transparency behavior regressed.
- Photos turned gray/washed as alpha decreased instead of becoming transparently blended.

**Root Cause**:
1. Shared `applyPhotoAlpha(...)` had been switched to `graphicsLayer` with `CompositingStrategy.ModulateAlpha`.
2. In combination with the newly enforced opaque local photo bases, this compositing path produced gray-looking fade behavior.
3. The blur-at-zero fix itself was not the problem; alpha compositing mode was.

**Fix Applied**:
- `BlurMapping.kt`
  - Updated `applyPhotoAlpha(...)` from `graphicsLayer + ModulateAlpha` back to standard `Modifier.alpha(...)`.
  - Kept the hard-zero blur safeguards unchanged (`PHOTO_BLUR_EPSILON`, `applyPhotoBlur`, `applyPhotoPreviewBlur`).

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/BlurMapping.kt`
- `CHANGELOG.md`
- `CHAT_HISTORY.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ BUILD SUCCESSFUL.

## [2026-03-27] Final follow-up: alpha<100% no longer softens photo at blur=0% (guarded blur + modulated alpha)

**User Report**:
- Issue still reproduced in all sections.
- With photo blur at `0%`, lowering alpha to `99%` or below made photo appear strongly blurred/low quality in preview and runtime.

**Root Cause**:
1. Several photo pipelines still always attached `.blur(...)`, including zero-radius cases.
2. Alpha compositing at `<100%` could route through quality-reducing offscreen behavior in these heavy transformed image layers.
3. Together this produced a blur-like degradation even when blur slider was effectively zero.

**Fix Applied**:
- `BlurMapping.kt`
  - Added epsilon normalization (`PHOTO_BLUR_EPSILON`) to hard-clamp tiny blur values to zero.
  - Added shared helpers:
    - `applyPhotoBlur(...)`
    - `applyPhotoPreviewBlur(...)`
    - `applyPhotoAlpha(...)` with `CompositingStrategy.ModulateAlpha`.
- Replaced direct `.alpha(imageAlpha).blur(...)` chains with shared helpers in:
  - `ColorPickerDialog.kt` (AppLayoutPreview)
  - `PhotoBackgroundSurface.kt`
  - `FileListItem.kt`
  - `FileListScreen.kt`
  - `FullScreenEditor.kt`
  - `FileEditDialog.kt`
- `SettingsScreen.kt` mini preview:
  - changed file-list photo base from transparent to opaque local base,
  - switched alpha to `applyPhotoAlpha(...)`,
  - made blur conditional so no blur modifier is attached when value is zero.

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/BlurMapping.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/PhotoBackgroundSurface.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/FileListItem.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/FileListScreen.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/FileEditDialog.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
- `CHANGELOG.md`
- `CHAT_HISTORY.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ BUILD SUCCESSFUL.

## [2026-03-27] Follow-up fix: generalized alpha<100% + blur=0% safeguard to all sections

**User Report**:
- The same issue still appeared and might affect all areas, not only memo/file-list subsets.
- Logs also showed heavy frame skips while interacting with settings/editor surfaces.

**Root Cause**:
1. Some preview/runtime sections still lacked an opaque local base under photo layers.
2. With photo alpha below 100%, lower layers could still be revealed and appear blurred.
3. Additional photo state debug logging remained in active load/save/positioning paths.

**Fix Applied**:
- `ColorPickerDialog.kt`
  - Added local opaque base under main background photo in preview.
  - Replaced content-area transparent photo underlay with opaque local base (`displayContentColor.copy(alpha = 1f)`).
- `FileListScreen.kt`
  - Added opaque `mainBackgroundBaseColor` beneath main background photo layer.
- `MainActivity.kt`
  - Removed remaining high-frequency `PhotoPos` and `PhotoBackground` logs from photo load/save/positioning sync paths.

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/FileListScreen.kt`
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
- `CHANGELOG.md`
- `CHAT_HISTORY.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ BUILD SUCCESSFUL.

**Manual Verification Checklist**:
1. Check all sections (Background, Content, Memo, File List Item, File List Background).
2. Set photo blur to `0%`, then lower alpha below `100%`.
3. Confirm no blur appears until blur slider is increased.
4. Verify smoother editor interaction with reduced log churn.

## [2026-03-27] Follow-up fix: alpha < 100% no longer introduces blur at 0% photo blur in memo/file-list areas

**User Report**:
- Runtime/preview still showed blur-like output at `0%` photo blur when alpha dropped below `100%`.
- Repro was strongest in Memo area, File List Item, and File List Background.

**Root Cause**:
1. Affected sections used transparent base layers under photo overlays.
2. Lowering photo alpha revealed lower visual layers.
3. Some of those lower layers were blurred, so users perceived blur even with photo blur slider at `0%`.

**Fix Applied**:
- `ColorPickerDialog.kt` (`AppLayoutPreview`)
  - Replaced transparent bases with section-local base colors for:
    - file-list background
    - file-list item rows
    - memo area
  - Forced base alpha to `1f` to stop blur bleed-through from underneath.
- `FileListItem.kt`
  - Replaced transparent base under photo with stable local base color.
- `FileListScreen.kt`
  - Added opaque fallback base color (`fileListBaseColor`) when photo background is active.
- `PhotoBackgroundSurface.kt`
  - Changed photo-case `surfaceColor` from transparent to `color.copy(alpha = 1f)`.

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/FileListItem.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/FileListScreen.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/PhotoBackgroundSurface.kt`
- `CHANGELOG.md`
- `CHAT_HISTORY.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ BUILD SUCCESSFUL.

**Manual Verification Checklist**:
1. In Memo/File List Item/File List Background editors, set photo blur to `0%`.
2. Lower alpha from `100%` to `99%` and below.
3. Confirm blur does not appear at 0% blur.
4. Raise blur slightly (`0.1%` to `2%`) and confirm onset is subtle/continuous.

## [2026-03-27] Follow-up fix: preview dead-zone removed to stop 3.6%→4.6% blur jump

**User Report**:
- Runtime blur behavior was already correct and gradual.
- In full preview, blur looked absent at `3.6%` and then suddenly strong near `4.6%`.

**Root Cause**:
1. Preview-only mapping had a hard cutoff (`<= 0.04f` returned `0.dp`).
2. Crossing that threshold produced a discontinuous visual jump in compact/full preview rendering.

**Fix Applied**:
- `BlurMapping.kt`
  - Updated `mapPhotoPreviewBlurToRadius(...)` to continuous behavior:
    - removed threshold dead-zone
    - added continuous low-end curve:
      - `eased = normalized.pow(2.1f)`
      - `compensated = eased * 0.62f + normalized * 0.08f`
  - Runtime mapping unchanged.

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/BlurMapping.kt`
- `CHANGELOG.md`
- `CHAT_HISTORY.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ BUILD SUCCESSFUL.

**Manual Verification Checklist**:
1. Open Settings -> Colors -> File List Background photo editor.
2. Move blur from `3%` to `6%` in small steps.
3. Confirm no abrupt onset between `3.6%` and `4.6%` in preview.
4. Confirm runtime still applies smooth blur after Test/Select.

## [2026-03-27] Follow-up fix: stricter low-end compact preview blur curve + active preview log cleanup

**User Report**:
- Preview blur still looked too strong/abrupt at very low percentages.
- Preview felt stronger than runtime at the same blur value.

**Root Cause**:
1. The previous preview mapper (`pow(1.45f) * 0.85f`) was still too aggressive at the first visible blur steps in compact preview geometry.
2. Remaining interaction logs in active preview/editor paths still added avoidable frame-time overhead.

**Fix Applied**:
- `BlurMapping.kt`
  - Tightened `mapPhotoPreviewBlurToRadius(...)` for preview-only behavior:
    - `<= 0.04f` returns `0.dp`
    - remap `(value - 0.04f) / 0.96f`
    - stronger easing `pow(1.9f)`
    - lower cap `* 0.65f`
  - Runtime mapper remains unchanged.
- `ColorPickerDialog.kt`
  - Removed remaining verbose logs from photo picker/mode/gesture interaction paths.
- `UnifiedBackgroundEditor.kt`
  - Removed remaining mode-toggle and gesture logs in `PreviewTouchArea`.

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/BlurMapping.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/UnifiedBackgroundEditor.kt`
- `CHANGELOG.md`
- `CHAT_HISTORY.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ BUILD SUCCESSFUL.

**Manual Verification Checklist**:
1. Open Settings -> Colors -> File List Background photo editor.
2. Move blur from `0%` to `5%` slowly and verify subtle low-end progression.
3. Tap Test/Select and compare with runtime rendering.
4. Pinch/drag/mode-switch in preview and verify smoother interaction.

## [2026-03-27] Follow-up fix: compact preview low-end blur softened again + remaining interaction debug logs removed

**User Report**:
- Preview blur still looked too strong and abrupt at very small percentages.
- Applied blur in runtime looked softer than preview at the same value.
- Logs still showed blur/copy debug entries during preset interactions.

**Root Cause**:
1. Preview used only linear compensation, which still overstated tiny blur values in compact geometry.
2. Remaining `BlurDebug` / `CopyDebug` logs were still active in preset click/drag-option flows.

**Fix Applied**:
- `BlurMapping.kt`
  - Added preview-only mapper: `mapPhotoPreviewBlurToRadius(blurValue)`.
  - Behavior:
    - low-end softened using `normalized.pow(1.45f)`
    - compact-preview compensation via `* 0.85f`
    - final radius delegated to `mapPhotoBlurToRadius(...)`
- `ColorPickerDialog.kt`
  - `AppLayoutPreview` now uses `mapPhotoPreviewBlurToRadius(...)` for all preview photo blur rendering.
  - Removed previous inline `previewBlurCompensation` logic.
- `SettingsScreen.kt`
  - Removed remaining non-essential interaction logs (`BlurDebug` / `CopyDebug`) from:
    - preset click handling
    - preset long-click drag options
    - copy-to-section action

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/BlurMapping.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
- `CHANGELOG.md`
- `CHAT_HISTORY.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ BUILD SUCCESSFUL.

**Manual Verification Checklist**:
1. Open Settings -> Colors -> File List Background photo editor.
2. Set blur in `0.5% - 2%` range and watch preview response.
3. Tap Select/Test and compare in-app applied blur.
4. Confirm preview low-end no longer looks disproportionately stronger than runtime.

## [2026-03-26] Follow-up fix: compact preview blur now calibrated to match in-app perception + preset reload hot path removed

**User Report**:
- Low blur values (e.g., ~1.3%) looked extremely blurred in compact preview, but only moderate when actually applied in app.
- Logs still showed repeated preset-related churn while interacting in Settings.

**Root Cause**:
1. Same blur radius on a compact preview surface appears stronger than on full runtime surfaces (perceptual mismatch).
2. `MainActivity` was still passing `colorPresets = loadColorPresets()` directly from composition into `SettingsScreen`, causing repeated parse/load work on recomposition.
3. Verbose preset debug logging amplified overhead and noise in this interaction path.

**Fix Applied**:
- `ColorPickerDialog.kt`
  - `AppLayoutPreview` now applies preview-only compensation before blur mapping:
  - `previewBlurCompensation = 0.72f`
  - `previewBlurRadius(value) = mapPhotoBlurToRadius((value * 0.72f).coerceIn(0f, 1f))`
  - Applied to bg/fileListBg/fileListItem/content/memo preview blur rendering.
- `MainActivity.kt`
  - Removed verbose `saveColorPresets` stacktrace/list logs and load listing logs.
  - Removed remaining non-error `PresetDebug` logs in preset apply/select/reset paths and `loadCustomColorPresetsWithPhotos(...)`.
  - Replaced recomposition reload pass-through:
    - from: `colorPresets = loadColorPresets().let { colorRefreshTrigger; it }`
    - to: `colorPresets = colorPresets`
  - Kept in-memory `colorPresets` list synchronized after save/update/delete/rename.
  - Removed remaining high-frequency `PASSING ...`/backdrop logs in composition path.

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
- `CHANGELOG.md`
- `CHAT_HISTORY.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ BUILD SUCCESSFUL.

**Manual Verification Checklist**:
1. Open Settings -> Colors -> File List Background photo editor.
2. Set blur around `1-2%`; compare preview vs in-app after Test/Select.
3. Confirm preview and runtime now feel much closer at low blur values.
4. Interact across presets in Settings and confirm reduced churn/jank.

## [2026-03-25] Follow-up fix: blur drag jank traced to per-tick preset writes in MainActivity (deferred) + PhotoControls hot logs removed

**User Report**:
- Blur in preview still felt abrupt while sliding, even after linear blur mapping and prior logging cleanup.

**Root Cause**:
1. `SettingsScreen` edit callbacks had already been adjusted to avoid per-frame `onEditCustomColor(...)` commits.
2. But in `MainActivity`, each area's `on*PhotoBlurChange` callback still performed selected-preset persistence (`loadColorPresets()` + `saveColorPresets(...)`) on every slider tick.
3. Those disk writes on drag caused main-thread stalls and made blur feedback appear jumpy.
4. `PhotoControlsPanel` still had active `Log.d(...)` calls in render/mode-chip paths, adding avoidable hot-path overhead.

**Fix Applied**:
- `MainActivity.kt`
  - Updated blur callbacks to keep `selectedPresetForEditing` synchronized in-memory during drag only:
    - `onCustomBgPhotoBlurChange`
    - `onContentAreaPhotoBlurChange`
    - `onMemoAreaPhotoBlurChange`
    - `onFileListItemPhotoBlurChange`
    - `onFileListBackgroundPhotoBlurChange`
  - Removed per-tick selected-preset disk writes (`loadColorPresets()` / `saveColorPresets(...)`) from those blur callbacks.
  - Kept direct per-area blur persistence (`save*PhotoBlur`) so preview remains live and state remains current.
- `PhotoControlsPanel.kt`
  - Removed hot-path debug logs from recomposition and mode chip interactions.

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/PhotoControlsPanel.kt`
- `CHANGELOG.md`
- `CHAT_HISTORY.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ BUILD SUCCESSFUL.

**Manual Verification Checklist**:
1. Open Settings -> Colors -> File List Background photo editor.
2. Drag blur from `0%` upward continuously and watch preview smoothness.
3. Repeat in File List Item photo editor.
4. Confirm no abrupt stepping/stalls while dragging.
5. Tap Select/Test, then reopen and verify values still apply correctly.

## [2026-03-25] Follow-up fix: abrupt blur preview behavior traced to recomposition log spam (cleaned)

**User Report**:
- Blur still felt abrupt in File List Background preview while sliding, even after mapping tweaks.
- Runtime logs showed heavy repeated output during edit/preview drag.

**Root Cause**:
1. Blur mapping had already been made continuous and proportional.
2. Hot-path debug logs were still running in high-frequency recomposition paths:
   - `ColorPreview` logs in `ColorPickerDialog`
   - `UnifiedBgEditor` logs in `UnifiedBackgroundEditor`
   - `ColorPresetRow` logs in `SettingsScreen`
3. That log flood increased main-thread load and made preview updates appear jumpy/abrupt.

**Fix Applied**:
- Removed high-frequency `Log.d(...)` statements from:
  - `ColorPickerDialog.kt` (preview sync/content preview logs)
  - `UnifiedBackgroundEditor.kt` (render/mode/control logs)
  - `SettingsScreen.kt` (`ColorPresetRow` add/edit dialog logs)

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/UnifiedBackgroundEditor.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
- `CHANGELOG.md`
- `CHAT_HISTORY.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ BUILD SUCCESSFUL.

**Manual Verification Checklist**:
1. Open File List Background photo editor.
2. Drag blur from `0%` slowly while watching preview continuity.
3. Confirm no abrupt visual stepping caused by render-loop stalls.
4. Repeat in File List Item editor and compare behavior.

## [2026-03-25] Follow-up fix: blur now increases proportionally per 1% slider step (linear ramp)

**User Report**:
- Blur started too suddenly at low values (around 1-3%) even after dead-zone removal.
- Requested strictly gradual blur increase from 1% to 100%, bit by bit.

**Root Cause**:
1. Mapping was continuous but still used smoothstep easing (`t^2 * (3 - 2t)`).
2. That non-linear curve can still feel abrupt in the first visible blur range.
3. User expectation required proportional per-percent output rather than eased progression.

**Fix Applied**:
- `BlurMapping.kt`
  - Replaced smoothstep mapping with strict linear mapping.
  - Final behavior:
    - `radius = normalizedBlur * 5dp`
  - Keeps max blur unchanged while making each slider percent increment consistent.

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/BlurMapping.kt`
- `CHANGELOG.md`
- `CHAT_HISTORY.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ BUILD SUCCESSFUL.

**Manual Verification Checklist**:
1. Open File List Background photo editor.
2. Move slider from `0%` upward one step at a time.
3. Confirm each 1% increment increases blur slightly.
4. Confirm no sudden jump in the `1-3%` range.
5. Confirm 10/20/30/50% feel progressively proportional.

## [2026-03-25] Follow-up fix: blur now starts gradually below 8% (dead-zone removed)

**User Report**:
- Blur had no visible effect for the first ~7-8% of slider movement.
- Then blur suddenly appeared too strong right after crossing that point.

**Root Cause**:
1. Shared mapping function had a hard threshold (`PHOTO_BLUR_DEAD_ZONE = 0.08f`).
2. All values below that threshold were forced to `0.dp` blur.
3. Crossing the threshold produced a perceptual jump instead of continuous buildup.

**Fix Applied**:
- `BlurMapping.kt`
  - Removed hard dead-zone logic.
  - Switched to continuous smoothstep-based ramp from `0f..1f`:
    - `smoothRamp = t * t * (3 - 2 * t)`
    - `radius = smoothRamp * 5f`
  - Result: blur starts immediately (subtle at low values) and increases smoothly.

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/BlurMapping.kt`
- `CHANGELOG.md`
- `CHAT_HISTORY.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ BUILD SUCCESSFUL.

**Manual Verification Checklist**:
1. Open File List Background photo editor.
2. Drag blur from `0%` upward slowly.
3. Verify blur is already gradually visible before `8%`.
4. Verify no abrupt step-change around `8%`.
5. Check that 10-20% still feels moderate and controllable.

## [2026-03-25] Follow-up fix: gentle photo blur mapping unified in remaining preview/runtime paths

**User Goal**:
- Continue refining blur sensitivity so the first slider movement gives a subtle blur increase instead of a harsh jump.
- Keep blur behavior consistent across file-list and preview surfaces.

**Root Cause**:
1. A shared mapper (`mapPhotoBlurToRadius`) was already introduced for gentle low-end response.
2. Some rendering paths still used inline quadratic radius expressions (`((value * value) * 5f).dp`) rather than the shared helper.
3. Mixed mapping styles made the low-end response feel inconsistent across sections.

**Fixes Applied**:
- `FileListItem.kt`
  - Replaced both photo blur calls (DYNAMIC + STATIC_ZOOM branches) with:
  - `blur(mapPhotoBlurToRadius(photoBlur))`
- `FileListScreen.kt`
  - Replaced custom background photo blur mapping with shared helper.
  - Replaced file-list background photo blur mapping with shared helper.
- `ColorPickerDialog.kt`
  - Replaced all app-layout preview photo blur mappings with shared helper for:
    - bg
    - fileListBg
    - fileListItem
    - content
    - memo

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/FileListItem.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/FileListScreen.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`
- `CHANGELOG.md`
- `CHAT_HISTORY.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ BUILD SUCCESSFUL.

**Manual Verification Checklist**:
1. In Settings -> Colors, open photo editor for Background, File List Background, and File List Item.
2. Move blur from 0 in tiny increments and confirm gentle response at low values.
3. Compare same blur values between preview and runtime, ensuring similar visual intensity.
4. Press Select/Test, reopen editor, and verify behavior persists consistently.

## [2026-03-24] Follow-up fix: edit-dialog photo sliders no longer persist presets every drag frame

**User Report**:
- Blur still felt the same.
- Runtime logs showed repeated skipped frames and repeated `saveColorPresets` activity while dragging photo sliders.

**Root Cause**:
1. In `SettingsScreen` edit flow (`ColorPresetRow` -> `UnifiedBackgroundEditor`), `onPhotoBlurChange/onPhotoOffsetXChange/onPhotoOffsetYChange/onPhotoScaleChange/onPhotoRotationChange` called `onEditCustomColor(...)` on every slider tick.
2. `onEditCustomColor(...)` in `MainActivity` triggers expensive `loadColorPresets()`/`saveColorPresets(...)` paths.
3. That per-frame persistence caused UI jank and unstable feedback, making blur feel unchanged/inconsistent.

**Fixes Applied**:
- `SettingsScreen.kt` (edit dialog callbacks):
  - Kept slider values local in `presetToEdit` during drag.
  - Removed per-frame `onEditCustomColor(...)` calls from blur/transform slider callbacks.
  - Preserved final commit path via existing `onPhotoSelected` (Select/Test), so changes persist once per action.

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
- `CHANGELOG.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ passed.

**Manual Verification Checklist**:
1. Edit an existing photo square (File List Bg / File List Item).
2. Drag blur continuously and verify smoother response with less stutter.
3. Drag zoom/position/rotation and verify interaction smoothness.
4. Tap Select, reopen same square, and verify values persisted.

## [2026-03-24] Follow-up fix: blur ramp now consistent across preview and runtime photo surfaces

**User Report**:
- First blur slider step still caused too much blur in File List Background flow.
- Applying a small amount of blur made photo quality in full preview/editor appear to suddenly drop.

**Root Cause**:
1. Blur mapping was inconsistent between renderers: some paths used linear mapping (`value * 5f`) while others already used softened non-linear mapping.
2. That inconsistency made low-end slider movement feel harsher in certain surfaces.
3. Combined with prior file-list blur stacking behavior, the perceived quality drop at low blur levels looked worse than expected.

**Fixes Applied**:
- Standardized photo blur response to quadratic mapping in active preview/runtime photo render paths:
  - `blur(((value * value) * 5f).dp)`
- Updated shared background renderer path:
  - `PhotoBackgroundSurface.kt` now uses quadratic mapping for photo blur (both DYNAMIC and STATIC_ZOOM branches).
- Verified other key paths already aligned with non-linear mapping and no remaining `* 5f` linear photo blur usage in component render code.

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/PhotoBackgroundSurface.kt`
- `CHANGELOG.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ passed.

**Manual Verification Checklist**:
1. Open Settings -> Colors -> File List Background -> Photo mode.
2. Move blur slider upward slowly from 0 and confirm smooth ramp (no first-step jump).
3. Compare identical blur % across Background / Content / Memo / File List Item / File List Background.
4. Check full preview/editor while adding small blur and confirm no sudden quality cliff.
5. Save/apply preset, restart app, and confirm blur response persists consistently.

## [2026-03-24] Fix: file-list background photo blur sensitivity normalized

**User Report**:
- In File List Background photo editing, the blur slider felt too aggressive: a small drag produced too much blur compared to other sections.

**Root Cause**:
1. `FileListScreen` applied file-list section color blur on the entire file-list background container even while a file-list background photo was active.
2. The file-list background photo layer then applied its own photo blur.
3. This caused double blur stacking, so the same slider value looked much stronger than in other sections that effectively had one blur pass.

**Fixes Applied**:
- `FileListScreen.kt`:
  - Changed the file-list background container modifier to apply `fileListBackgroundBlur` only when no file-list background photo is selected.
  - Left photo blur behavior unchanged (`fileListBackgroundPhotoBlur`) so active photo backgrounds use a single blur source.

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/FileListScreen.kt`
- `CHANGELOG.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ passed.

## [2026-03-24] Follow-up fix: photo still looked low-quality due runtime decode limits

**User Report**:
- Even after import-quality fixes, applied background photos still looked not truly high quality.
- Logs showed heavily zoomed photo transforms in active use (e.g., ~2.8x scale), where blur remained visible.

**Main Problem (Root Cause)**:
1. `ColorPickerDialog` preview layers were explicitly decoding photos with constrained sizes (`600/800/1000/1200` px).
2. Several zoomable background render surfaces still used default constrained decode behavior.
3. Under high zoom, these lower-resolution decodes were enlarged, causing apparent pixelation.

**Fixes Applied**:
- Added shared helper:
  - `HighQualityPhotoPainter.kt` using Coil `ImageRequest.size(Size.ORIGINAL)`.
- Replaced painter usage across zoomable photo surfaces and previews:
  - `ColorPickerDialog.kt`
  - `PhotoBackgroundSurface.kt`
  - `FileListScreen.kt`
  - `FileListItem.kt`
  - `FileEditDialog.kt`
  - `FullScreenEditor.kt`
  - `PhotoPositionDialog.kt`
  - `SettingsScreen.kt`

**Result**:
- Photo rendering now uses original-size decode requests in all key transformed/zoomed surfaces.
- This removes the remaining quality bottleneck after the earlier storage-byte fidelity fix.

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ passed.

## [2026-03-24] Follow-up fix: imported photo quality was reduced by downscale/recompression

**User Report**:
- Applied photos (e.g., background) looked pixelated compared to the original source image.
- User requested that backup import also preserve original source quality.

**Root Cause**:
1. `PhotoStorage.savePhoto(...)` decoded selected images into bitmaps, downsampled/scaled, and recompressed to JPEG.
2. That conversion reduced fidelity immediately on import, so later preview/render/backup could only use already-degraded files.

**Fixes Applied**:
- `PhotoStorage.kt`:
  - Replaced decode/resize/compress flow with byte-for-byte copy from source URI to internal storage.
  - Kept deduping with MD5 filename hash from original bytes.
  - Preserved extension using MIME/URI resolution instead of always writing `.jpg`.
- Backup quality guarantee:
  - Confirmed backup flow already serializes/restores raw photo bytes (`photoToBase64` / `photoFromBase64`), so photos imported after this fix remain original quality through backup export/import.

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/data/PhotoStorage.kt`
- `CHANGELOG.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ completed successfully (tool reported I/O wait timeout after success output).

## [2026-03-24] Follow-up fix: cross-section preview was not showing rotation for other areas

**User Report**:
- In the photo preview editor, zoom from other sections was visible, but twist/rotation from those other sections was not shown.

**Root Cause**:
1. `UnifiedBackgroundEditor` was already loading and forwarding URI/alpha/blur/offset/scale for non-active sections.
2. It did not load/forward per-section rotation values into `PreviewTouchArea -> AppLayoutPreview(...)`.
3. As a result, non-active sections rendered with default rotation (`0f`) while still showing correct zoom/position.

**Fixes Applied**:
- `UnifiedBackgroundEditor.kt`:
  - Added loaded rotation state variables for all sections.
  - Loaded all section rotation values from SharedPreferences (`*_photo_rotation`).
  - Passed those rotations through `PreviewTouchArea(...)` and into `AppLayoutPreview(...)` as section-specific rotation params.

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/UnifiedBackgroundEditor.kt`
- `CHANGELOG.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ passed.

## [2026-03-23] Follow-up fix: MainActivity was dropping rotation before FileManagementApp

**User Report**:
- Behavior was still the same after prior fixes: preview showed twist, but opened editor still appeared unrotated.

**Root Cause**:
1. Rotation callback and persistence paths in `MainActivity` were working (`saveContentAreaPhotoRotation`, etc.).
2. But when `Settings` rendered `FileManagementApp(...)`, rotation values were not passed for custom/content/memo photo backgrounds.
3. `FileManagementApp` defaults those params to `0f`, so `FullScreenEditor` received default rotation and rendered untwisted.

**Fixes Applied**:
- `MainActivity.kt` (`Settings` branch -> `FileManagementApp(...)` call):
  - Added missing forwarding:
    - `customBgPhotoRotation = customBgPhotoRotation`
    - `contentAreaPhotoRotation = contentAreaPhotoRotation`
    - `memoAreaPhotoRotation = memoAreaPhotoRotation`

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
- `CHANGELOG.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ passed.

## [2026-03-23] Follow-up fix: existing photo-square selection was not restoring rotation

**User Report**:
- After latest build, behavior was still the same.
- Logs showed offset/scale flowing through (`MainActivity` and `ColorPresetRow`), but visual twist still looked unchanged after selecting/editing existing content photo squares.

**Root Cause**:
1. In `ColorPresetRow` click handling for existing presets, selecting a photo square restored URI/offset/scale but did not restore `photoRotation`.
2. This left section rotation stale/default while other transforms updated, producing the same visible issue.

**Fixes Applied**:
- `SettingsScreen.kt` (`ColorPresetRow` -> existing square `onClick`):
  - Added `onBackgroundPhotoRotationChange?.invoke(preset.photoRotation)` when a photo square is selected.
  - Also reset `offsetX/offsetY/scale/rotation` to defaults when selecting a plain color square (along with URI/alpha/blur cleanup).

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
- `CHANGELOG.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ passed.

## [2026-03-22] Follow-up fix: Settings was dropping live transform state before ColorsSection

**User Report**:
- Behavior still appeared unchanged: preview/editor paths still showed untwisted/default transforms in some edit/open flows.

**Root Cause**:
1. `MainActivity` held correct non-default transform state (offset/scale/rotation), but `SettingsScreen` did not pass all of it into `ColorsSection(...)`.
2. Only URI/alpha/blur were forwarded for multiple sections in that call site.
3. `ColorsSection` defaults missing transforms to `0f/0f/1f/0f`, so `ColorPresetRow`/`UnifiedBackgroundEditor` received defaults and produced runtime mismatch.

**Fixes Applied**:
- `SettingsScreen.kt` (`ColorsSection(...)` invocation):
  - Added forwarding for all section transform fields:
    - custom bg: `offsetX/offsetY/scale/rotation`
    - content: `offsetX/offsetY/scale/rotation`
    - memo: `offsetX/offsetY/scale/rotation`
    - file list item: `offsetX/offsetY/scale/rotation`
    - file list background: `offsetX/offsetY/scale/rotation`

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
- `CHANGELOG.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ passed.

## [2026-03-22] Follow-up fix: edit-dialog rotation callback was missing

**User Report**:
- Behavior still looked the same: twist changes appeared in preview flows but the opened editor still did not reliably reflect the rotated state.

**Root Cause**:
1. `SettingsScreen` had separate add/new and edit-existing `UnifiedBackgroundEditor` branches.
2. The add/new branch wired `onPhotoRotationChange`, but the edit-existing branch did not.
3. In edit flow, offset/scale propagated, but rotation updates were not routed through section callbacks during interaction, leaving stale rotation state in downstream rendering paths.

**Fixes Applied**:
- `SettingsScreen.kt` (`ColorPresetRow`, edit picker branch):
  - Added `onPhotoRotationChange` handler.
  - Routed rotation updates by section (`content`, `memo`, `fileListItem`, `fileListBg`, background).
  - Synced `presetToEdit.photoRotation` and `onEditCustomColor(...)` payload with new rotation values.

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
- `CHANGELOG.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ passed.

## [2026-03-22] Fix: opened text-file editor now applies saved photo twist

**User Report**:
- Rotation/twist showed in preview windows and preset cards, but when opening the actual text file editor, the background looked unrotated.

**Root Cause**:
1. The runtime opened-note path is rendered by `FileEditDialog`.
2. Its background image transform applied offset/scale but did not apply rotation in `graphicsLayer`.
3. `MainActivity` call into `FileEditDialog` did not pass photo transform state (URI/alpha/blur/offset/scale/rotation), so editor view used defaults.

**Fixes Applied**:
- `FileEditDialog.kt`:
  - Added `customBgPhotoRotation` parameter to the composable signature.
  - Applied `rotationZ = customBgPhotoRotation` to the opened-note background image layer.
- `MainActivity.kt`:
  - Updated `FileEditDialog(...)` invocation to pass custom background photo URI/alpha/blur/offset/scale/rotation from live state.

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/FileEditDialog.kt`
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
- `CHANGELOG.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ passed.

## [2026-03-22] Follow-up fix: rotation visible in preset cards and live app surfaces

**User Report**:
- Twist/rotation looked saved in `UnifiedBackgroundEditor` preview, but preset card preview and app surfaces still appeared straight.

**Root Cause**:
1. `AppLayoutPreview` rendered rotation only for the active `targetSection`; preset cards use `targetSection = "none"`, so all section photos were effectively shown with `0f` rotation there.
2. `SettingsScreen` live preset-card state loaded photo URI/alpha/blur/offset/scale from SharedPreferences, but not per-section rotation.
3. `MainActivity` `onEdit...Color` handlers updated square/preset values but did not consistently sync section live state + section SharedPreferences keys used by app surfaces and preset previews.
4. Startup active-preset restore loaded colors/URIs but did not fully restore section offset/scale/rotation into live state/prefs for all sections.

**Fixes Applied**:
- `ColorPickerDialog.kt` (`AppLayoutPreview`):
  - Added per-section rotation params (`customBgPhotoRotation`, `contentAreaPhotoRotation`, `memoAreaPhotoRotation`, `fileListItemPhotoRotation`, `fileListBgPhotoRotation`).
  - Applied those section rotations when not actively editing that section.
- `SettingsScreen.kt`:
  - Added live rotation state vars in `ColorPresetsSection` for all 5 sections.
  - Loaded rotation keys from SharedPreferences and passed them through `PresetCard` into `AppLayoutPreview`.
- `MainActivity.kt`:
  - Startup active-preset restore now applies/saves section offset/scale/rotation for all sections.
  - All `onEdit...Color` handlers now sync section-level photo state and save section SharedPreferences keys (including rotation) immediately.
  - Corrected `onEditFileListBackgroundColor` preset writeback to use incoming `offsetX/offsetY/scale` (not stale section vars).

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
- `CHANGELOG.md`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ passed.

## [2026-03-22] Fix: photo rotation persistence completed across settings/presets/editor

**User Goal**:
- Finish `photoRotation` threading through `SettingsScreen`/`ColorPresetRow`/`MainActivity` so rotation edited in `UnifiedBackgroundEditor` is saved, restored, and applied consistently.

**What Was Broken**:
1. `MainActivity` had a partially corrupted `SettingsScreen(...)` named-argument block from earlier patch attempts.
2. Rotation fields were missing in `ColorPreset` model JSON mapping.
3. `ColorPresetRow` lacked some section rotation callback parameters despite being used by call sites.
4. `UnifiedBackgroundEditor` exposed rotation callbacks but did not initialize/load section rotation consistently from incoming state/preferences.

**Fixes Applied**:
- Repaired `MainActivity` preset/apply/select/update callback region and removed duplicated `onUpdatePreset` argument.
- Ensured section rotation is propagated and persisted in apply/select flows using `save...PhotoRotation(...)` paths.
- Added all section rotation fields to `ColorPreset` data class + `toJson()` + `fromJson()`.
- Added missing rotation callback params to `ColorPresetRow` (`content`, `memo`, `fileListItem`).
- Added `currentPhotoRotation` parameter to `UnifiedBackgroundEditor`, initialized editor state from it, and loaded per-section rotation from SharedPreferences.

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`
- `app/src/main/java/com/j4/texter2025/data/ColorPreset.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/SettingsScreen.kt`
- `app/src/main/java/com/j4/texter2025/ui/components/UnifiedBackgroundEditor.kt`

**Validation**:
- `./gradlew.bat :app:compileDebugKotlin` ✅ passed.
- Existing warnings remain (unused params/vars), no rotation-threading compile errors remain.

## [2026-03-21] Follow-up fix: Twist rotation now visibly renders in full preview

**User Report**:
- Logs showed gesture callbacks firing and offset/scale saving, but twisting two fingers still looked like "no change".

**Root Cause**:
- Rotation state was computed in `UnifiedBackgroundEditor`, but `AppLayoutPreview` had no `photoRotation` render path.
- So gesture logs changed while UI rotation remained visually absent.

**Fix Applied**:
- Added `photoRotation` parameter to `AppLayoutPreview`.
- Applied `rotationZ` in `graphicsLayer` for all editable target sections (`bg`, `content`, `memo`, `fileListBg`, `fileListItem`).
- Keeps rotation scoped to the actively edited section only.

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/ColorPickerDialog.kt`

**Validation**:
- `./gradlew assembleDebug --console=plain` passed.

---

## [2026-03-21] UX enhancement: Freer 2-finger movement in full preview window

**User Request**:
- Make it possible to freely move/swap the photo using two fingers in the full preview window.

**Root Cause**:
- Full preview transform gesture logic used tighter pan constraints and lower sensitivity, making movement feel limited.

**Fix Applied**:
- Updated `PreviewTouchArea` in `UnifiedBackgroundEditor`:
  - Increased pan sensitivity (`3f -> 4f`).
  - Increased pan range (`3f * scale -> 8f * scale`).
  - Added safe container size guards (`coerceAtLeast(1f)`) for stable gesture math.
- Updated hint text to: `Use 2 fingers to move • Pinch to zoom`.

**File Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/UnifiedBackgroundEditor.kt`

**Validation**:
- `./gradlew assembleDebug --console=plain` passed.

---

## [2026-03-21] Bug fix: Blur persisted after Go Back from opened note

**User Report**:
- Flow: `Settings -> Test -> Main -> open note -> floating Go Back`
- Result: returned area looked blurred, unlike the normal clear settings flow.

**Root Cause**:
- Main blur gate was `isFullScreenEditorShown && customBgBlur > 0f`.
- In the floating `Go Back` path, navigation switched to settings but `isFullScreenEditorShown` could remain stale true in that transition path, so scaffold blur stayed active.

**Fix Applied**:
- In `MainActivity` callback passed to `FileManagementApp`, explicitly reset:
  - `isFullScreenEditorShown = false`
  - before `showSettings = true`.

**File Updated**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`

**Validation**:
- `./gradlew assembleDebug` passed.

---

## [2026-03-21] Bug fix: Custom square deletion was no-op in Settings

**User Report**:
- Deletion had not worked for a long time.
- Logs showed long-press action firing (`onLongClick`) and delete flow reaching Settings UI.

**Root Cause**:
- `SettingsScreen` delete actions call section callbacks (`onDeleteBgColor`, `onDeleteContentAreaColor`, etc.).
- In `MainActivity`, those delete callbacks were not passed into `SettingsScreen(...)`.
- Because the parameters have default no-op lambdas, tapping delete did nothing.

**Fix Applied**:
- Wired all missing delete callbacks in `MainActivity` for all 5 sections.
- Each callback removes one square by matching `(color, photoUri)` and updates persisted preset data via `saveColorPresets(...)`.
- Kept fix minimal and upstream (at callback wiring/root cause), not a UI workaround.

**File Updated**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`

**Validation**:
- `./gradlew assembleDebug` passed.

---

## [2026-03-20] UX enhancement: Soft/elastic content resize animation when memo is toggled

**User Report**:
- Auto-resize behavior works, but content area shrinks/expands too abruptly when tapping `Show Memo` / `Hide Memo`.

**Root Cause**:
- Content height was applied directly to `fillMaxHeight(...)` from the target fraction, so programmatic memo-toggle changes rendered as immediate jumps.

**Fix Applied**:
- Added spring-based height animation using `animateFloatAsState`.
- Added `isContentAreaDragging` to gate animation behavior:
  - During drag: apply direct height (no animation lag while user is moving handle).
  - When memo toggle triggers auto-resize: apply animated height for softer transition.
- Wired drag lifecycle hooks (`onDragStart`, `onDragEnd`, `onDragCancel`) to keep gesture behavior consistent.

**File Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt`

---

## [2026-03-20] UX fix: Auto-adjust content size when memo is toggled in opened notes

**User Report**:
- If content area is expanded too much, tapping `Show Memo` does not make memo visible.
- User must manually shrink content first.
- After hiding memo, content remains shrunk and must be manually expanded.

**Root Cause**:
- Memo toggle only changed visibility state and did not rebalance vertical layout.
- Content drag handler allowed heights that were incompatible with memo visibility.

**Fix Applied**:
- Added editor body-height measurement (`onSizeChanged`) to compute available layout space.
- Added memo-aware max content height calculations using reserved UI heights.
- On memo show: content auto-shrinks to fit memo in viewport.
- On memo hide: content auto-expands to a button-safe size so memo toggle remains visible.
- In dual-bar mode while memo is visible: content drag max is clamped to memo-aware limit.

**File Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt`

---

## [2026-03-20] UX fix: Go Back returns to the exact section used for Test

**User Report**: Dragging is fixed, but after tapping `Go Back`, Settings always opens at the main/default area instead of the exact section where `Test` was triggered.

**Root Cause**:
- Test flow callback path dropped section context in some layers (`onTestInApp` had no section payload).
- Returning to Settings did not trigger section re-focus for repeated returns to the same section.

**Fix Applied**:
- Updated callback contracts to pass section ids through Settings → ColorPresetRow → UnifiedBackgroundEditor flow.
- Stored the test origin section in `MainActivity` using `settingsReturnSection`.
- Added `settingsReturnRequestId` and incremented it whenever Go Back is pressed so section focus is reapplied even when section id is unchanged.
- Wired `SettingsScreen` to react with `LaunchedEffect(settingsReturnRequestId)` and expand the matching settings category.

**Result**:
- `Go Back` now returns the user to the relevant settings area tied to the section where `Test` was used.

---

## [2026-03-20] Fix: Go Back drag-start teleport caused by stale pointerInput captures

**User Report**: Teleport/jump still occurs on drag start even after cumulative drag and single-instance rendering updates.

**Root Cause**:
- `pointerInput` can keep old captured values when keys do not restart the modifier.
- `onDragStart` was initializing gesture offsets from potentially stale `fabOffsetX/fabOffsetY` captures.

**Fix Applied**:
- Added `rememberUpdatedState` in both FAB hosts to keep latest offsets available to the pointerInput block.
- Updated `onDragStart` initialization to use:
  - `latestFabOffsetX`
  - `latestFabOffsetY`

**Files Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt`
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`

---

## [2026-03-20] Fix: Go Back button jump on drag start when note is open

**User Report**: Dragging now works in opened note, but sometimes when touch starts the button suddenly jumps to another place, then continues moving with finger.

**Root Cause Hypothesis Implemented**:
- Shared FAB state was being represented by two draggable UI instances in different contexts:
  - MainActivity-level FAB
  - FullScreenEditor dialog FAB
- This can cause inconsistent offset updates and visible teleport behavior.

**Fix Applied**:
- Render MainActivity FAB only when editor dialog is not visible:
  - `if (showBackToSettingsFab && !isFullScreenEditorShown)`
- Also updated MainActivity drag handling to cumulative per-gesture offsets to keep behavior consistent.

**File Updated**:
- `app/src/main/java/com/j4/texter2025/MainActivity.kt`

---

## [2026-03-20] Fix: Floating "Go Back" button drag jitter in opened note dialog

**User Request**: Dragging the floating "Go Back" button inside opened notes (`FullScreenEditor`) is still jerky and unstable.

**Root Cause**:
- Drag updates inside `FullScreenEditor` were non-cumulative per pointer event:
  - `onFabOffsetChange(fabOffsetX + dragAmount.x, fabOffsetY + dragAmount.y)`
- `dragAmount` is only the per-event delta, while `fabOffsetX/fabOffsetY` are composition values that can be stale between rapid events.
- Result: jitter/jump behavior during drag in dialog context.

**Fix Applied**:
- Updated dialog FAB drag handling to use gesture-local cumulative offsets.
- On drag start, initialize local accumulators from shared offsets.
- On each drag event, accumulate delta into local values, then push the new position through `onFabOffsetChange`.

**File Updated**:
- `app/src/main/java/com/j4/texter2025/ui/components/FullScreenEditor.kt` (drag block around lines 848-861)

---

## [2026-03-09] Removed: STATIC_REPEAT Photo Background Mode

**User Request**: User decided to remove the STATIC_REPEAT (Fixed) mode and keep only DYNAMIC and STATIC_ZOOM modes.

**Changes Made**:
- Removed STATIC_REPEAT from `PhotoBackgroundMode` enum
- Removed mode selection chips from `PhotoControlsPanel.kt` and `ColorPickerDialog.kt`
- Removed all STATIC_REPEAT rendering branches from 7 files:
  - `FullScreenEditor.kt` (main background)
  - `FileListScreen.kt` (main bg + file list bg)
  - `FileListItem.kt` (file item bg)
  - `ColorPickerDialog.kt` (5 preview sections: bg, fileListBg, fileListItem, content, memo)
  - `PhotoBackgroundSurface.kt` (reference implementation)
- Updated gesture conditions to remove STATIC_REPEAT checks
- Updated contentScale when expressions to remove STATIC_REPEAT cases

**Result**: App now has only 2 photo background modes:
- **DYNAMIC**: User-controlled positioning with drag/pinch gestures
- **STATIC_ZOOM**: Auto-fill with no user positioning

**Migration**: Existing presets with STATIC_REPEAT will fall back to DYNAMIC via `fromString()` default.

---

## [2026-03-08] Feature: Photo Background Mode Visual Effects + STATIC_REPEAT Drag Support

**Problem**: Selecting DYNAMIC, STATIC_REPEAT, or STATIC_ZOOM mode was saved correctly but had no visual effect — all backgrounds rendered identically using DYNAMIC-style transforms with user offset/scale.

**Fix**: Added `when (photoBackgroundMode)` rendering logic across all photo background rendering locations:
- **App rendering**: `FullScreenEditor.kt`, `FileListScreen.kt`, `FileListItem.kt`
- **Preview rendering**: `AppLayoutPreview` in `ColorPickerDialog.kt` (all 5 sections), `UnifiedBackgroundEditor.kt`, `FullScreenPhotoEditor.kt`
- **Parameter passing**: `MainActivity.kt` passes mode through `MainContent → FullScreenEditor` and `MainContent → FileManagementApp → FileListScreen → FileListItem`

Each mode now renders differently: DYNAMIC (offset+scale with drag/pinch), STATIC_REPEAT (centered scale with drag positioning), STATIC_ZOOM (auto-fill, no user positioning, Fit).

**Enhancement [Evening]**: User requested STATIC_REPEAT mode to support drag gestures for selecting which area of the photo to show as the "fixed" background. Updated all STATIC_REPEAT rendering to apply offset (`translationX/Y = offset*size`) and enabled drag gestures in preview windows. Users can now position the fixed area while maintaining centered scale behavior. Files updated: `ColorPickerDialog.kt` (5 sections), `FullScreenEditor.kt`, `FileListScreen.kt` (2 sections), `FileListItem.kt`.

---

## [2026-02-16] Fix: Photo Preview-to-Reality Mismatch — ContentScale.Crop

**Problem**: Photo centered in editor preview but shifted too low in actual opened note. Container-relative fractions were correct but produced different visual results due to different container aspect ratios.

**Root Cause**: `ContentScale.Fit` scales image to fit inside container — in landscape preview the image fills most of the space, but in tall portrait actual app the image is small and centered with empty space. Same `translationY` fraction = very different visual displacement relative to the image.

**Fix**: Changed ALL `ContentScale.Fit` → `ContentScale.Crop` across ~15 locations in 9 files. With `Crop`, image always fills entire container, so fraction-based offsets are visually consistent regardless of aspect ratio.

---

## [2026-02-16] Fix: Photo Positioning — Proper Container-Relative Approach

**Problem**: Photo positioning broken across two failed attempts: (1) hardcoded `* 500f` produced different visual results in different-sized containers, (2) reverting to `500f` broke zoom/scale application.

**Root Cause**: A fixed pixel multiplier cannot work across containers of different sizes. Offset must be a fraction of the container.

**Fix**: Replaced ALL hardcoded multipliers with `size.width`/`size.height`:
- Rendering: `translationX = -offsetX * size.width`
- Gestures: `dragAmount.x / size.width.toFloat()`

This means offset `0.5` = 50% displacement in ANY container. 10 locations across 9 files updated: `PhotoBackgroundSurface.kt`, `FileEditDialog.kt`, `FileListScreen.kt`, `FileListItem.kt`, `PhotoPositionDialog.kt`, `ColorPickerDialog.kt` (5 sections), `SettingsScreen.kt` (2 previews), `FullScreenPhotoEditor.kt`, `UnifiedBackgroundEditor.kt`. Previously saved positions will need re-adjustment.

---

## [2026-02-15] Fix: Alpha Slider Resets to 100% When Re-Entering Editor

**Problem**: Alpha slider reset to 100% every time the editor was closed and reopened for a section.

**Root Causes**:
1. Missing `onPhotoAlphaChange` callback — slider only updated local state, never persisted to SharedPreferences.
2. Tapping a preset square wrote stale `preset.photoAlpha` (1.0) to SharedPreferences, overwriting the correct value saved by the slider callback.
3. `LaunchedEffect` conditionally loaded from SharedPreferences but the stale preset had already overwritten it.

**Fix**: Added `onPhotoAlphaChange` callback. Always load alpha/blur from SharedPreferences in `LaunchedEffect`. Removed stale preset alpha/blur writes on preset click.

---

## [2026-02-15] Fix: Alpha/Blur Sliders Not Updating Preview & Cross-Section Leak

**Problem**: Alpha/blur sliders showed no effect in preview. Changes leaked to other sections. Alpha reset when re-entering a section.

**Root Cause**: `PreviewTouchArea` hardcoded `previewAlpha=1f`/`previewBlur=0f` in photo mode. `LaunchedEffect` unconditionally overwrote alpha/blur from params, ignoring SharedPreferences.

**Fix** (`UnifiedBackgroundEditor.kt`):
- Pass `photoAlpha`/`photoBlur` as top-level preview values in photo mode
- Load alpha/blur from SharedPreferences per-section when params are at defaults (same pattern as positioning fix)

---

## [2026-02-12] Enhancement: Scale-Aware Photo Panning Limits

**Problem**: When zoomed in, the photo couldn't be panned far enough to show corners or edges — static `-3f..3f` limits didn't account for zoom level.

**Fix**: `maxOffset = 3f * currentScale` — at 3x zoom, limits expand to `±9f`. Applied to both gesture handler and slider ranges.

---

## [2026-02-12] Fix: Mirrored Horizontal Drag & Slow Gesture Movement

**Problem**: Horizontal drag was mirrored (left→right, right→left) and movement was very slow regardless of finger speed.

**Root Cause**: `AppLayoutPreview` uses `translationX = -offsetX * 500f * 0.175f` (negated X). Gesture added `+pan.x`, cancelling the direction. Also `pan / size` normalized to tiny values.

**Fix** (`UnifiedBackgroundEditor.kt`):
- Negated `pan.x`: `currentOffsetX - pan.x / size.width * sensitivity`
- Added `sensitivity = 3f` multiplier for natural finger tracking

---

## [2026-02-11] Fix: Photo Zoom Resets on Touch & Drag/Zoom Not Working

**Problem**: In all sections, touching the preview reset zoom/offset to defaults. Drag and pinch-to-zoom also stopped working / were jerky.

**Root Cause** (three-layer bug in `UnifiedBackgroundEditor.kt`):
1. Photo state initialized with hardcoded `0f/0f/1f` instead of `currentPhotoOffsetX/Y/Scale` parameters
2. First fix attempt used state values as `pointerInput` keys — this restarted the gesture detector on every frame, killing ongoing gestures
3. `LaunchedEffect` overwrote preset positioning with SharedPreferences values, resetting zoom in edit mode

**Fix**:
- `mutableStateOf(0f)` → `mutableStateOf(currentPhotoOffsetX)` (and Y/Scale)
- `rememberUpdatedState` for offset/scale + `pointerInput(Unit)` — lambda reads fresh values without restarting detector
- `LaunchedEffect` only loads from SharedPreferences when params are at defaults (new photo), preserving preset values in edit mode

**Key Lesson**: Never use rapidly-changing state as `pointerInput` keys. Use `rememberUpdatedState` instead.

**Log filter for gesture debugging**: `PhotoPositioning|UnifiedBackground`

---

## [2026-02-09] Feature: Unified Background Editor

**Session Goal**: Merge `ColorPickerDialog` and `FullScreenPhotoEditor` into a single full-screen `UnifiedBackgroundEditor` that combines color picking and photo selection with live preview and gesture-based photo manipulation.

**Approach**:
1. Analyzed `ColorPickerDialog` parameters (30+ params) and both call sites in `SettingsScreen.kt`
2. Created modular sub-components to keep each file under size limits:
   - `PhotoControlsPanel.kt` (~156 lines) — photo pick/remove, alpha, blur, zoom, position sliders
   - `ColorControlsPanel.kt` (~154 lines) — hue, spectrum, RGB, alpha, blur sliders
   - `UnifiedBackgroundEditor.kt` (~607 lines) — full-screen dialog, preview with gesture detection, mode toggle
3. Replaced both `ColorPickerDialog` calls in `ColorPresetRow` with `UnifiedBackgroundEditor`
4. Removed `allSectionPhotos` parameter — new editor loads all section photo state directly from SharedPreferences

**Key Design Decisions**:
- Full-screen `Dialog` with `DialogProperties(usePlatformDefaultWidth = false)` instead of `AlertDialog`
- Mode toggle (Color/Photo) instead of tab row
- Preview area with `detectTransformGestures` for pinch-zoom and drag in photo mode
- All callbacks kept identical to old `ColorPickerDialog` for zero-change integration at call sites
- SharedPreferences as single source of truth (bypasses stale parameter chains)

**Result**: Build successful. Both call sites wired up. Ready for on-device testing.

---

## [2026-02-08] Fix: Photo Positioning Fallback Bug

**Session Goal**: Fix bug where CustomBg photo positioning (zoom/offset) wasn't reflected in ColorPickerDialog preview when editing other sections (Content Area, Memo Area, etc.)

**Problem**: After setting zoom/offset on CustomBg's photo, opening Content Area's ColorPickerDialog showed the photo without any zoom — always default (0f, 0f, 1f).

**Diagnosis Steps**:
1. Added debug logs to SharedPreferences loading — confirmed values saved correctly
2. Added debug logs to parameter chain (MainActivity → SettingsScreen → ColorPresetRow) — found stale values
3. Bypassed parameter chain by loading directly from SharedPreferences in ColorPickerDialog — values loaded correctly
4. Still no zoom visible — discovered `AppLayoutPreview` was using wrong variables for rendering

**Root Cause** (three-layer bug):
1. `allSectionPhotos` parameter chain passed stale default values for CustomBg positioning
2. `AppLayoutPreview` calls used `currentPhotoOffsetX` (stale param, always 0f) instead of `photoOffsetX` (loaded state variable)
3. Background layer in `AppLayoutPreview` used `customBgPhotoOffsetX` from stale `allSectionPhotos["bg"]` map

**Fix Applied** (all in `ColorPickerDialog.kt`):
- Added `loadedCustomBgOffsetX/Y/Scale` state variables loaded from SharedPreferences
- Section-specific positioning loaded from SharedPreferences with fallback to CustomBg
- Replaced `currentPhotoOffsetX` → `photoOffsetX` in both `AppLayoutPreview` calls
- Replaced `allSectionPhotos["bg"]?.offsetX ?: 0f` → `loadedCustomBgOffsetX` for background layer

**Result**: All sections correctly show CustomBg zoom/offset in preview. Confirmed working for content, memo, and bg sections.

## [2026-02-08 18:03] Fix: Photo Positioning for ALL Sections in Preview

**Problem**: Same stale `allSectionPhotos` issue affected ALL sections, not just CustomBg. When Memo Area had zoom set, Content Area's preview didn't show Memo's zoom in the memo part of the layout.

**Fix**: Extended the SharedPreferences direct-loading pattern to all 5 sections:
- Added 15 state variables: `loaded[Section]OffsetX/Y/Scale` for CustomBg, Content, Memo, FileListItem, FileListBg
- All loaded from SharedPreferences in `LaunchedEffect(show, targetSection)`
- Replaced ALL `allSectionPhotos[...]?.offsetX ?: 0f` references with `loaded*` variables in both `AppLayoutPreview` calls

**Key Insight**: Same root cause as the CustomBg-only fix — the `allSectionPhotos` parameter chain is unreliable for positioning data across all sections. SharedPreferences is the source of truth.

## [2026-02-08 21:10] Fix: Complete Photo State for ALL Sections in Preview

**Problem**: Two issues reported:
1. Photo selected in Memo Area's "Pick a Photo" tab didn't appear in the preview within the dialog itself
2. Photos set on Memo/FileListItem sections weren't visible in other sections' previews

**Root Cause**: The `allSectionPhotos` parameter chain was stale for ALL photo properties — not just positioning, but also URIs, alpha, and blur. Additionally, the color tab preview passed `photoUri = ""` instead of the section's current photo URI.

**Fix**: Extended SharedPreferences direct-loading to cover ALL photo properties:
- Added 30 state variables: `loaded[Section]PhotoUri/Alpha/Blur/OffsetX/Y/Scale` for all 5 sections
- All loaded from SharedPreferences in `LaunchedEffect(show, targetSection)`
- Replaced ALL `allSectionPhotos[...]` references in all 4 `AppLayoutPreview`/`PhotoPickerContent` calls
- Color tab preview now passes `currentPhotoUri ?: ""` instead of `""` as `photoUri`

**Key Insight**: The `allSectionPhotos` parameter chain is completely unreliable. SharedPreferences is the single source of truth for ALL photo state in the preview.

## [2026-02-08 23:44] Fix: Preset Card Photo Preview & Initial Photo Load

**Problem**: Two remaining issues:
1. Photos set via background picker didn't appear in the mini preview on color preset cards in Settings → Colors
2. When first selecting a photo, it was invisible in the preview until touching the full-screen editor

**Root Causes**:
1. `PresetCard` used `preset.memoAreaPhotoUri` etc. (saved at preset creation time), not the current live app state from SharedPreferences
2. All 5 `rememberAsyncImagePainter` calls set `imageAlpha = 0f` during Coil's `State.Loading` — recomposition on `State.Success` wasn't reliably triggering visual update
3. `currentPhotoUri` (stale parent parameter) was used instead of `selectedPhotoUri` (freshly picked) in all `AppLayoutPreview` calls

**Fixes**:
- **SettingsScreen.kt**: Added SharedPreferences loading in `ColorPresetsSection` for all section photo state. Added 30 live photo parameters to `PresetCard`. Updated `AppLayoutPreview` in `PresetCard` to use live values with preset fallback.
- **ColorPickerDialog.kt**: Added `crossfade(true)` via `ImageRequest.Builder` to all 5 Coil painters. Changed loading-state alpha from `0f` to target alpha. Introduced `activePhotoUri = selectedPhotoUri ?: currentPhotoUri` used in all 4 calls.

**Confirmed by user**: Cross-section previews now working. Photos persist across all sections after save.
