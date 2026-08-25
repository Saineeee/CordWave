package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.example.ui.components.settings.*

@Composable
fun AccountsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val minHeightDp = 64f
    val maxHeightDp = 180f
    val topBarHeight = remember { Animatable(maxHeightDp) }
    val nestedScrollConnection = rememberCollapsibleTopBarConnection(topBarHeight, minHeightDp, maxHeightDp)

    var enableLastFm by remember { mutableStateOf(false) }
    var enableYouTubeSync by remember { mutableStateOf(true) }
    var enableDriveBackup by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CollapsibleSettingsTopBar(
                title = "Accounts",
                onBackClick = onBack,
                showBackButton = true,
                topBarHeight = topBarHeight,
                minHeightDp = minHeightDp,
                maxHeightDp = maxHeightDp
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                SettingsSubsection(title = "MUSIC SERVICES") {
                    SwitchSettingItem(
                        title = "YouTube Music Client",
                        subtitle = "Stream online tracks, charts, and curated playlists",
                        checked = enableYouTubeSync,
                        onCheckedChange = { enableYouTubeSync = it },
                        leadingIcon = Icons.Default.PlayCircle
                    )
                    SwitchSettingItem(
                        title = "Last.fm Scrobbling",
                        subtitle = "Track listening stats and sync scrobbles",
                        checked = enableLastFm,
                        onCheckedChange = { enableLastFm = it },
                        leadingIcon = Icons.Default.Radio
                    )
                }
            }

            item {
                SettingsSubsection(title = "CLOUD BACKUP") {
                    SwitchSettingItem(
                        title = "Google Drive Sync",
                        subtitle = "Sync favorite playlists and library history",
                        checked = enableDriveBackup,
                        onCheckedChange = { enableDriveBackup = it },
                        leadingIcon = Icons.Default.CloudSync
                    )
                }
            }
        }
    }
}
