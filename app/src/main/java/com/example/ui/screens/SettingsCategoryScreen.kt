package com.example.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.presentation.model.SettingsCategory
import com.example.presentation.viewmodel.SettingsUiState
import com.example.presentation.viewmodel.SettingsViewModel
import com.example.ui.components.CollapsibleCommonTopBar
import com.example.ui.components.rememberCollapsibleHeaderState
import com.example.ui.components.scrollbar.ExpressiveScrollBar
import com.example.ui.components.settings.*
import com.example.ui.theme.MyApplicationTheme

@Composable
fun SettingsCategoryScreen(
    categoryId: String,
    navController: NavController? = null,
    onBackClick: () -> Unit,
    settingsViewModel: SettingsViewModel? = null,
    uiStateOverride: SettingsUiState? = null,
    onOpenEqualizer: () -> Unit = {},
    onOpenSleepTimer: () -> Unit = {},
    onNavigateToRoute: (String) -> Unit = { route -> navController?.navigate(route) },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val category = SettingsCategory.fromId(categoryId) ?: SettingsCategory.APPEARANCE
    val state = uiStateOverride ?: settingsViewModel?.uiState?.collectAsState()?.value ?: SettingsUiState()

    val headerHeightRange = (if (category.title.length > 13) 200.dp else 180.dp) to 56.dp
    val headerState = rememberCollapsibleHeaderState(headerHeightRange)
    val listState = rememberLazyListState()

    var showResetLyricsDialog by remember { mutableStateOf(false) }
    var showExcludedFoldersDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_category_screen_${category.id}")
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(headerState.nestedScrollConnection),
            contentPadding = PaddingValues(top = headerHeightRange.first + 8.dp, bottom = 48.dp, start = 16.dp, end = 16.dp)
        ) {
            when (category) {
                SettingsCategory.APPEARANCE -> {
                    item {
                        SettingsSubsection(title = "THEME & COLOR") {
                            ThemeSelectorItem(
                                label = "App Theme",
                                description = "Choose overall light, dark, or system look",
                                options = mapOf(
                                    "DARK" to "Dark",
                                    "LIGHT" to "Light",
                                    "FOLLOW_SYSTEM" to "System"
                                ),
                                selectedKey = state.appThemeMode,
                                onSelectionChanged = { settingsViewModel?.setAppThemeMode(it) },
                                leadingIcon = Icons.Default.Brightness4
                            )

                            SwitchSettingItem(
                                title = "Pure OLED Black",
                                subtitle = "Pitch black background for high contrast & battery saving",
                                checked = state.isOled,
                                onCheckedChange = { settingsViewModel?.setOledMode(it) },
                                leadingIcon = Icons.Default.Contrast
                            )

                            val isDynamicSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                            SwitchSettingItem(
                                title = "Dynamic Wallpaper Colors",
                                subtitle = if (isDynamicSupported) "Extract palette dynamically from device wallpaper" else "Requires Android 12+",
                                checked = state.dynamicColor && isDynamicSupported,
                                enabled = isDynamicSupported,
                                onCheckedChange = { settingsViewModel?.setDynamicColor(it) },
                                leadingIcon = Icons.Default.ColorLens
                            )

                            ThemeSelectorItem(
                                label = "Accent Color Tone",
                                description = if (state.dynamicColor && isDynamicSupported) "Used as fallback when dynamic color is disabled" else "Active system accent color",
                                options = mapOf(
                                    "0" to "Pixel Violet",
                                    "1" to "Amber Sunset",
                                    "2" to "Sage Emerald",
                                    "3" to "Berry Coral",
                                    "4" to "Sky Blue"
                                ),
                                selectedKey = state.accentIndex.toString(),
                                onSelectionChanged = { it.toIntOrNull()?.let { idx -> settingsViewModel?.setAccentColor(idx) } },
                                leadingIcon = Icons.Default.Palette
                            )
                        }
                    }

                    item {
                        SettingsSubsection(title = "PLAYER & ARTWORK") {
                            ThemeSelectorItem(
                                label = "Now Playing Player Theme",
                                description = "Color style for full now playing background",
                                options = mapOf(
                                    "ALBUM_ART" to "Album Art Dynamic",
                                    "SYSTEM" to "System Dynamic"
                                ),
                                selectedKey = state.playerThemePreference,
                                onSelectionChanged = { settingsViewModel?.setPlayerThemePreference(it) },
                                leadingIcon = Icons.Default.Wallpaper
                            )

                            SwitchSettingItem(
                                title = "Show Audio File Info",
                                subtitle = "Display bitrate, sample rate, and format badges",
                                checked = state.showPlayerFileInfo,
                                onCheckedChange = { settingsViewModel?.setShowPlayerFileInfo(it) },
                                leadingIcon = Icons.Default.Info
                            )

                            SettingsItem(
                                title = "Album Art Palette Style",
                                subtitle = "Fine-tune color extraction intensity and vibrance",
                                leadingIcon = Icons.Default.Gradient,
                                onClick = { onNavigateToRoute("palette_style") }
                            )

                            SwitchSettingItem(
                                title = "Smooth Rounded Corners",
                                subtitle = "Use continuous curvature corner styling",
                                checked = state.useSmoothCorners,
                                onCheckedChange = { settingsViewModel?.setUseSmoothCorners(it) },
                                leadingIcon = Icons.Default.RoundedCorner
                            )
                        }
                    }
                }

                SettingsCategory.PLAYBACK -> {
                    item {
                        SettingsSubsection(title = "AUDIO ENGINE") {
                            SettingsItem(
                                title = "Audio Equalizer & Sound FX",
                                subtitle = "5-band graphic EQ, dynamic bass boost, and 3D virtualizer",
                                leadingIcon = Icons.Default.Equalizer,
                                onClick = onOpenEqualizer
                            )

                            SettingsItem(
                                title = "Sleep Timer",
                                subtitle = "Automatically pause playback after scheduled duration",
                                leadingIcon = Icons.Default.Timer,
                                onClick = onOpenSleepTimer
                            )

                            SwitchSettingItem(
                                title = "Audio Normalization",
                                subtitle = "ReplayGain volume leveling across tracks",
                                checked = state.audioNormalization,
                                onCheckedChange = { settingsViewModel?.setAudioNormalization(it) },
                                leadingIcon = Icons.Default.VolumeUp
                            )

                            SwitchSettingItem(
                                title = "Gapless Audio Playback",
                                subtitle = "Seamless track transitions with zero pause gap",
                                checked = state.gaplessPlayback,
                                onCheckedChange = { settingsViewModel?.setGaplessPlayback(it) },
                                leadingIcon = Icons.Default.GraphicEq
                            )

                            SliderSettingsItem(
                                label = "Crossfade Duration",
                                value = state.crossfadeDurationSec.toFloat(),
                                valueRange = 0f..5f,
                                steps = 4,
                                valueText = if (state.crossfadeDurationSec == 0) "Off" else "${state.crossfadeDurationSec}s",
                                onValueChange = { settingsViewModel?.setCrossfadeDuration(it.toInt()) },
                                leadingIcon = Icons.Default.Shuffle
                            )
                        }
                    }

                    item {
                        SettingsSubsection(title = "STREAMING QUALITY") {
                            SwitchSettingItem(
                                title = "High-Fidelity Audio Stream",
                                subtitle = "256kbps Opus / 320kbps Lossless MP3 streaming",
                                checked = state.highQualityStream,
                                onCheckedChange = { settingsViewModel?.setHighQualityStream(it) },
                                leadingIcon = Icons.Default.HighQuality
                            )

                            SwitchSettingItem(
                                title = "Download over Wi-Fi Only",
                                subtitle = "Conserve cellular mobile data usage",
                                checked = state.wifiOnlyDownload,
                                onCheckedChange = { settingsViewModel?.setWifiOnlyDownload(it) },
                                leadingIcon = Icons.Default.Wifi
                            )
                        }
                    }
                }

                SettingsCategory.LIBRARY -> {
                    item {
                        SettingsSubsection(title = "LIBRARY SCANNER & SYNC") {
                            RefreshLibraryItem(
                                isSyncing = state.isSyncing,
                                syncProgress = state.syncProgress,
                                statusMessage = state.syncStatusMessage,
                                onSync = { settingsViewModel?.startLibrarySync() },
                                onRebuild = { settingsViewModel?.rebuildDatabase() }
                            )

                            SliderSettingsItem(
                                label = "Minimum Song Duration",
                                value = state.minSongDuration.toFloat(),
                                valueRange = 0f..120f,
                                steps = 23,
                                valueText = if (state.minSongDuration == 0) "All files" else "${state.minSongDuration}s",
                                onValueChange = { settingsViewModel?.setMinSongDuration(it.toInt()) },
                                leadingIcon = Icons.Default.HourglassBottom
                            )

                            SettingsItem(
                                title = "Excluded Directories",
                                subtitle = "Choose storage folders to ignore during media scans",
                                leadingIcon = Icons.Default.FolderOff,
                                onClick = { showExcludedFoldersDialog = true }
                            )

                            SettingsItem(
                                title = "Artist Settings",
                                subtitle = "Portraits, release grouping, and compilation filters",
                                leadingIcon = Icons.Default.Person,
                                onClick = { onNavigateToRoute("artist_settings") }
                            )
                        }
                    }

                    item {
                        SettingsSubsection(title = "LYRICS") {
                            SwitchSettingItem(
                                title = "Auto-scan .LRC Files",
                                subtitle = "Automatically load synchronized lyrics from audio folders",
                                checked = state.autoScanLrcFiles,
                                onCheckedChange = { settingsViewModel?.setAutoScanLrcFiles(it) },
                                leadingIcon = Icons.Default.Lyrics
                            )

                            ThemeSelectorItem(
                                label = "Lyrics Source Priority",
                                description = "Order of preferred lyrics discovery",
                                options = mapOf(
                                    "EMBEDDED_FIRST" to "Embedded First",
                                    "ONLINE_FIRST" to "Online First",
                                    "LOCAL_FIRST" to "Local .LRC"
                                ),
                                selectedKey = state.lyricsSourcePreference,
                                onSelectionChanged = { settingsViewModel?.setLyricsSourcePreference(it) },
                                leadingIcon = Icons.Default.Sort
                            )

                            SettingsItem(
                                title = "Reset Cached Lyrics",
                                subtitle = "Clear local cached synchronized lyrics",
                                leadingIcon = Icons.Default.DeleteOutline,
                                onClick = { showResetLyricsDialog = true }
                            )
                        }
                    }
                }

                SettingsCategory.BEHAVIOR -> {
                    item {
                        SettingsSubsection(title = "NAVIGATION & LAUNCH") {
                            ThemeSelectorItem(
                                label = "Default Launch Tab",
                                description = "Screen displayed upon opening the application",
                                options = mapOf(
                                    "HOME" to "Home",
                                    "LIBRARY" to "Library",
                                    "SEARCH" to "Search"
                                ),
                                selectedKey = state.launchTab,
                                onSelectionChanged = { settingsViewModel?.setLaunchTab(it) },
                                leadingIcon = Icons.Default.Home
                            )

                            ThemeSelectorItem(
                                label = "Bottom Navigation Style",
                                description = "Appearance of the persistent bottom navigation bar",
                                options = mapOf(
                                    "FLOATING" to "Floating Pill",
                                    "FIXED" to "Fixed Edge-to-Edge"
                                ),
                                selectedKey = state.navBarStyle,
                                onSelectionChanged = { settingsViewModel?.setNavBarStyle(it) },
                                leadingIcon = Icons.Default.ViewAgenda
                            )

                            ThemeSelectorItem(
                                label = "Library Layout Mode",
                                description = "Navigation hierarchy in your music library",
                                options = mapOf(
                                    "TABS" to "Swipeable Tabs",
                                    "DRAWER" to "Side Navigation Drawer"
                                ),
                                selectedKey = state.libraryNavigationMode,
                                onSelectionChanged = { settingsViewModel?.setLibraryNavigationMode(it) },
                                leadingIcon = Icons.Default.ViewCarousel
                            )
                        }
                    }

                    item {
                        SettingsSubsection(title = "VISUAL CAROUSELS & ART") {
                            ThemeSelectorItem(
                                label = "Card Carousel Style",
                                description = "Transition physics and animation for home carousels",
                                options = mapOf(
                                    "DEFAULT" to "Standard Cards",
                                    "COVERFLOW" to "Cover Flow 3D",
                                    "MINIMAL" to "Minimalist Strip"
                                ),
                                selectedKey = state.carouselStyle,
                                onSelectionChanged = { settingsViewModel?.setCarouselStyle(it) },
                                leadingIcon = Icons.Default.Collections
                            )

                            ThemeSelectorItem(
                                label = "Playlist Art Collage Pattern",
                                description = "Artwork mosaic style for custom multi-track playlists",
                                options = mapOf(
                                    "DEFAULT" to "2x2 Quad Grid",
                                    "DIAGONAL" to "Diagonal Slice",
                                    "MOSAIC" to "Dynamic Mosaic"
                                ),
                                selectedKey = state.collagePattern,
                                onSelectionChanged = { settingsViewModel?.setCollagePattern(it) },
                                leadingIcon = Icons.Default.GridOn
                            )
                        }
                    }
                }

                SettingsCategory.BACKUP_RESTORE -> {
                    item {
                        SettingsSubsection(title = "BACKUP & RESTORE") {
                            SettingsItem(
                                title = "Export Backup File",
                                subtitle = "Save custom playlists, playback history, and preferences",
                                leadingIcon = Icons.Default.FileDownload,
                                onClick = {
                                    settingsViewModel?.exportData { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                }
                            )

                            SettingsItem(
                                title = "Import Backup File",
                                subtitle = "Restore playlists and metadata from JSON backup",
                                leadingIcon = Icons.Default.FileUpload,
                                onClick = {
                                    settingsViewModel?.importData { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                }
                            )

                            SwitchSettingItem(
                                title = "Automatic Snapshot Backups",
                                subtitle = "Create scheduled local backups when playlists change",
                                checked = state.autoBackup,
                                onCheckedChange = { settingsViewModel?.setAutoBackup(it) },
                                leadingIcon = Icons.Default.Backup
                            )
                        }
                    }
                }

                SettingsCategory.IMPORT_PLAYLIST -> {
                    item {
                        ImportPlaylistCategoryContent(
                            onImportComplete = { playlistId ->
                                onNavigateToRoute("playlist_detail/$playlistId")
                            }
                        )
                    }
                }

                SettingsCategory.ABOUT -> {
                    item {
                        AboutScreen(onBack = onBackClick)
                    }
                }

                SettingsCategory.EQUALIZER -> {
                    item {
                        SettingsSubsection(title = "EQUALIZER") {
                            SettingsItem(
                                title = "Open Equalizer Dialog",
                                subtitle = "Access audio equalizer and effect controls",
                                leadingIcon = Icons.Default.Equalizer,
                                onClick = onOpenEqualizer
                            )
                        }
                    }
                }

                SettingsCategory.DEVICE_CAPABILITIES -> {
                    item {
                        DeviceCapabilitiesScreen(onBack = onBackClick)
                    }
                }

                SettingsCategory.ACCOUNTS -> {
                    item {
                        AccountsScreen(onBack = onBackClick)
                    }
                }
            }
        }

        // Expressive scrollbar on the end edge, above the list. Settings rows
        // have no natural section characters, so no drag label is provided.
        ExpressiveScrollBar(
            listState = listState,
            modifier = Modifier.align(Alignment.CenterEnd)
        )

        // Collapsible Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerState.currentHeaderHeight)
                .align(Alignment.TopCenter)
                .zIndex(1f)
        ) {
            CollapsibleCommonTopBar(
                title = category.title,
                subtitle = category.subtitle,
                collapseFraction = headerState.collapseFraction,
                headerHeight = headerState.currentHeaderHeight,
                showBackButton = true,
                onBackClick = onBackClick
            )
        }
    }

    if (showResetLyricsDialog) {
        AlertDialog(
            onDismissRequest = { showResetLyricsDialog = false },
            title = { Text("Reset Cached Lyrics?") },
            text = { Text("This will clear all downloaded and parsed lyrics cache. Fresh lyrics will be fetched on playback.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        settingsViewModel?.resetLyrics {
                            Toast.makeText(context, "Lyrics cache cleared", Toast.LENGTH_SHORT).show()
                        }
                        showResetLyricsDialog = false
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetLyricsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showExcludedFoldersDialog) {
        AlertDialog(
            onDismissRequest = { showExcludedFoldersDialog = false },
            title = { Text("Excluded Folders") },
            text = {
                Column {
                    Text("Excluded folders are ignored during automatic library scans:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("• /storage/emulated/0/WhatsApp/Media", style = MaterialTheme.typography.bodySmall)
                            Text("• /storage/emulated/0/Telegram/Audio", style = MaterialTheme.typography.bodySmall)
                            Text("• /storage/emulated/0/Recordings", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExcludedFoldersDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}

@Preview
@Composable
fun SettingsCategoryScreenPreview() {
    MyApplicationTheme(darkTheme = true) {
        SettingsCategoryScreen(
            categoryId = "appearance",
            onBackClick = {}
        )
    }
}
