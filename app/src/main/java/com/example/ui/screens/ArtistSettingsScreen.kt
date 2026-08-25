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
fun ArtistSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val minHeightDp = 64f
    val maxHeightDp = 180f
    val topBarHeight = remember { Animatable(maxHeightDp) }
    val nestedScrollConnection = rememberCollapsibleTopBarConnection(topBarHeight, minHeightDp, maxHeightDp)

    var autoFetchArtistImages by remember { mutableStateOf(true) }
    var groupAlbumsByType by remember { mutableStateOf(true) }
    var hideCompilationArtists by remember { mutableStateOf(false) }
    var artistImageQuality by remember { mutableStateOf("HIGH") }

    Scaffold(
        topBar = {
            CollapsibleSettingsTopBar(
                title = "Artist Settings",
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
                SettingsSubsection(title = "ARTIST IMAGERY") {
                    SwitchSettingItem(
                        title = "Auto-fetch Artist Avatars",
                        subtitle = "Retrieve high-resolution artist portraits online",
                        checked = autoFetchArtistImages,
                        onCheckedChange = { autoFetchArtistImages = it },
                        leadingIcon = Icons.Default.PersonSearch
                    )
                    ThemeSelectorItem(
                        label = "Image Quality",
                        description = "Resolution for downloaded artist header imagery",
                        options = mapOf("LOW" to "Low (Fast)", "HIGH" to "High Res (HD)", "ORIGINAL" to "Ultra Lossless"),
                        selectedKey = artistImageQuality,
                        onSelectionChanged = { artistImageQuality = it },
                        leadingIcon = Icons.Default.HighQuality
                    )
                }
            }

            item {
                SettingsSubsection(title = "ORGANIZATION & DISPLAY") {
                    SwitchSettingItem(
                        title = "Group Albums by Release Type",
                        subtitle = "Separate into Albums, Singles, EPs, and Live sets",
                        checked = groupAlbumsByType,
                        onCheckedChange = { groupAlbumsByType = it },
                        leadingIcon = Icons.Default.FolderOpen
                    )
                    SwitchSettingItem(
                        title = "Filter Compilation Artists",
                        subtitle = "Hide various artists from the main artists list",
                        checked = hideCompilationArtists,
                        onCheckedChange = { hideCompilationArtists = it },
                        leadingIcon = Icons.Default.FilterAlt
                    )
                }
            }
        }
    }
}
