package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.presentation.model.SettingsCategory
import com.example.ui.components.CollapsibleCommonTopBar
import com.example.ui.components.rememberCollapsibleHeaderState
import com.example.ui.components.settings.*
import com.example.ui.theme.MyApplicationTheme

/**
 * PixelTune Settings Hub (Level 1)
 */
@Composable
fun SettingsScreen(
    navController: NavController? = null,
    onNavigateToCategory: (String) -> Unit = { categoryId ->
        navController?.navigate("settings_category/$categoryId")
    },
    onOpenEqualizer: () -> Unit = {
        navController?.navigate("equalizer") ?: onNavigateToCategory("equalizer")
    },
    onOpenDeviceCapabilities: () -> Unit = {
        navController?.navigate("device_capabilities") ?: onNavigateToCategory("device_capabilities")
    },
    onOpenAccounts: () -> Unit = {
        navController?.navigate("accounts") ?: onNavigateToCategory("accounts")
    },
    onOpenAbout: () -> Unit = {
        navController?.navigate("about") ?: onNavigateToCategory("about")
    },
    onBack: () -> Unit = { navController?.popBackStack() },
    // Backward compatibility parameters for existing caller
    isOledBlack: Boolean = false,
    useDynamicColor: Boolean = true,
    accentIndex: Int = 0,
    onToggleOled: () -> Unit = {},
    onToggleDynamicColor: () -> Unit = {},
    onSelectAccent: (Int) -> Unit = {},
    onRescanLibrary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val headerHeightRange = 180.dp to 56.dp
    val headerState = rememberCollapsibleHeaderState(headerHeightRange)

    val isDark = MaterialTheme.colorScheme.background.let {
        androidx.core.graphics.ColorUtils.calculateLuminance(it.hashCode()) < 0.5
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(headerState.nestedScrollConnection),
            contentPadding = PaddingValues(top = headerHeightRange.first + 8.dp, bottom = 120.dp, start = 16.dp, end = 16.dp)
        ) {
            // Group 1: Core Music Experience (Library, Appearance, Playback, Behavior)
            item {
                Text(
                    text = "EXPERIENCE",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 4.dp)
                )

                ExpressiveSettingsGroup {
                    // Library item (Top rounded: 24dp top, 4dp bottom)
                    ExpressiveCategoryItem(
                        category = SettingsCategory.LIBRARY,
                        onClick = { onNavigateToCategory(SettingsCategory.LIBRARY.id) },
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                    )

                    // Appearance item (Inner rounded: 4dp all)
                    ExpressiveCategoryItem(
                        category = SettingsCategory.APPEARANCE,
                        onClick = { onNavigateToCategory(SettingsCategory.APPEARANCE.id) },
                        shape = RoundedCornerShape(4.dp)
                    )

                    // Playback item (Inner rounded: 4dp all)
                    ExpressiveCategoryItem(
                        category = SettingsCategory.PLAYBACK,
                        onClick = { onNavigateToCategory(SettingsCategory.PLAYBACK.id) },
                        shape = RoundedCornerShape(4.dp)
                    )

                    // Behavior item (Inner rounded: 4dp all)
                    ExpressiveCategoryItem(
                        category = SettingsCategory.BEHAVIOR,
                        onClick = { onNavigateToCategory(SettingsCategory.BEHAVIOR.id) },
                        shape = RoundedCornerShape(4.dp)
                    )

                    // Import Playlist item (Bottom rounded: 4dp top, 24dp bottom)
                    ExpressiveCategoryItem(
                        category = SettingsCategory.IMPORT_PLAYLIST,
                        onClick = { onNavigateToCategory(SettingsCategory.IMPORT_PLAYLIST.id) },
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Standalone Direct Nav Items: Equalizer, Device Capabilities, Accounts
            item {
                Text(
                    text = "AUDIO & HARDWARE",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Equalizer Standalone Card
                    ExpressiveNavigationItem(
                        title = "Equalizer & Sound Effects",
                        subtitle = "5-band graphic EQ, bass boost, and audio visualizer",
                        icon = Icons.Default.Equalizer,
                        colors = getCategoryColors(SettingsCategory.EQUALIZER, isDark),
                        onClick = onOpenEqualizer,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.testTag("settings_item_equalizer")
                    )

                    // Device Capabilities Standalone Card
                    ExpressiveNavigationItem(
                        title = "Device Capabilities",
                        subtitle = "Hardware decoders, audio output, and supported codecs",
                        icon = Icons.Default.Smartphone,
                        colors = getCategoryColors(SettingsCategory.DEVICE_CAPABILITIES, isDark),
                        onClick = onOpenDeviceCapabilities,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.testTag("settings_item_device_capabilities")
                    )

                    // Accounts Standalone Card
                    ExpressiveNavigationItem(
                        title = "Accounts & Services",
                        subtitle = "YouTube Music, Last.fm scrobbling, and cloud sync",
                        icon = Icons.Default.AccountCircle,
                        colors = getCategoryColors(SettingsCategory.ACCOUNTS, isDark),
                        onClick = onOpenAccounts,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.testTag("settings_item_accounts")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Group 2: Backup & System (Backup & Restore, About)
            item {
                Text(
                    text = "SYSTEM & DATA",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )

                ExpressiveSettingsGroup {
                    // Backup & Restore (Top rounded: 24dp top, 4dp bottom)
                    ExpressiveCategoryItem(
                        category = SettingsCategory.BACKUP_RESTORE,
                        onClick = { onNavigateToCategory(SettingsCategory.BACKUP_RESTORE.id) },
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                    )

                    // About (Bottom rounded: 4dp top, 24dp bottom)
                    ExpressiveCategoryItem(
                        category = SettingsCategory.ABOUT,
                        onClick = onOpenAbout,
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Collapsible Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerState.currentHeaderHeight)
                .align(Alignment.TopCenter)
                .zIndex(1f)
        ) {
            CollapsibleCommonTopBar(
                title = "Settings",
                subtitle = "App preferences & customizations",
                collapseFraction = headerState.collapseFraction,
                headerHeight = headerState.currentHeaderHeight,
                showBackButton = true,
                onBackClick = onBack
            )
        }
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    MyApplicationTheme(darkTheme = true) {
        SettingsScreen(
            onNavigateToCategory = {},
            onOpenEqualizer = {},
            onOpenDeviceCapabilities = {},
            onOpenAccounts = {},
            onOpenAbout = {},
            onBack = {}
        )
    }
}
