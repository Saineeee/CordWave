package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.settings.CollapsibleSettingsTopBar
import com.example.ui.components.settings.SettingsItem
import com.example.ui.components.settings.SettingsSubsection
import com.example.ui.components.settings.rememberCollapsibleTopBarConnection

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val minHeightDp = 64f
    val maxHeightDp = 180f
    val topBarHeight = remember { Animatable(maxHeightDp) }
    val nestedScrollConnection = rememberCollapsibleTopBarConnection(topBarHeight, minHeightDp, maxHeightDp)

    var showLicenseDialog by remember { mutableStateOf(false) }
    var showChangelogDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CollapsibleSettingsTopBar(
                title = "About",
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
            .testTag("about_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // App Branding Card
            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Psync Music Player",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Version 1.0.0 (Build 100)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        ) {
                            Text(
                                text = "PixelTune Expressive Design",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Info Section
            item {
                SettingsSubsection(title = "APPLICATION") {
                    SettingsItem(
                        title = "What's New in v1.0",
                        subtitle = "Changelog, bug fixes, and latest features",
                        leadingIcon = Icons.Default.NewReleases,
                        onClick = { showChangelogDialog = true }
                    )
                    SettingsItem(
                        title = "Source Code & GitHub",
                        subtitle = "View repository, issues, and contributing guidelines",
                        leadingIcon = Icons.Default.Code,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com"))
                            runCatching { context.startActivity(intent) }
                        }
                    )
                    SettingsItem(
                        title = "Open Source Licenses",
                        subtitle = "Third-party libraries and dependencies",
                        leadingIcon = Icons.Default.Description,
                        onClick = { showLicenseDialog = true }
                    )
                }
            }

            item {
                SettingsSubsection(title = "LEGAL & SUPPORT") {
                    SettingsItem(
                        title = "Privacy Policy",
                        subtitle = "No telemetry, offline-first local playback",
                        leadingIcon = Icons.Default.Security,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"))
                            runCatching { context.startActivity(intent) }
                        }
                    )
                    SettingsItem(
                        title = "Rate & Review",
                        subtitle = "Support development on app store",
                        leadingIcon = Icons.Default.StarRate,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com"))
                            runCatching { context.startActivity(intent) }
                        }
                    )
                }
            }
        }
    }

    if (showLicenseDialog) {
        AlertDialog(
            onDismissRequest = { showLicenseDialog = false },
            title = { Text("Open Source Licenses") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "• Android Jetpack Compose (Apache 2.0)\n• AndroidX Media3 ExoPlayer (Apache 2.0)\n• Coil-kt Image Loader (Apache 2.0)\n• Material 3 Components (Apache 2.0)\n• Kotlin Coroutines & Flow (Apache 2.0)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLicenseDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showChangelogDialog) {
        AlertDialog(
            onDismissRequest = { showChangelogDialog = false },
            title = { Text("What's New in v1.0") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "• PixelTune Expressive 2-Level Settings Architecture\n• Collapsible Animated Top Bar with Spring Dynamics\n• Pure OLED Mode & Wallpaper Dynamic Palette Extraction\n• 5-Band Graphic Equalizer with Bass Boost & Virtualizer\n• Synced LRC Lyrics Engine & Offline Download Support",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showChangelogDialog = false }) {
                    Text("Awesome")
                }
            }
        )
    }
}
