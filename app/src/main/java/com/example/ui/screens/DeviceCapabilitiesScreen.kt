package com.example.ui.screens

import android.os.Build
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
fun DeviceCapabilitiesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val minHeightDp = 64f
    val maxHeightDp = 180f
    val topBarHeight = remember { Animatable(maxHeightDp) }
    val nestedScrollConnection = rememberCollapsibleTopBarConnection(topBarHeight, minHeightDp, maxHeightDp)

    Scaffold(
        topBar = {
            CollapsibleSettingsTopBar(
                title = "Device Capabilities",
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
                SettingsSubsection(title = "AUDIO HARDWARE & ENGINE") {
                    SettingsItem(
                        title = "Android Version",
                        subtitle = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                        leadingIcon = Icons.Default.Android
                    )
                    SettingsItem(
                        title = "Device Model",
                        subtitle = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
                        leadingIcon = Icons.Default.PhoneAndroid
                    )
                    SettingsItem(
                        title = "Audio Output Engine",
                        subtitle = "AndroidX Media3 ExoPlayer AudioTrack (24-bit float PCM)",
                        leadingIcon = Icons.Default.GraphicEq
                    )
                    SettingsItem(
                        title = "Hardware Offload Decoding",
                        subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "Supported (Direct DSP Path)" else "Software Emulation",
                        leadingIcon = Icons.Default.Memory
                    )
                }
            }

            item {
                SettingsSubsection(title = "SUPPORTED CODECS") {
                    SettingsItem(
                        title = "FLAC Lossless",
                        subtitle = "Up to 24-bit / 192 kHz Hi-Res decoding supported",
                        leadingIcon = Icons.Default.MusicNote
                    )
                    SettingsItem(
                        title = "Opus & Vorbis",
                        subtitle = "Hardware & Low latency native decoder",
                        leadingIcon = Icons.Default.AudioFile
                    )
                    SettingsItem(
                        title = "AAC / MP3 / WAV",
                        subtitle = "Full hardware acceleration enabled",
                        leadingIcon = Icons.Default.FormatListBulleted
                    )
                }
            }
        }
    }
}
