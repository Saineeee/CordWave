package com.example.presentation.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class SettingsCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector?,
    val iconRes: Int? = null
) {
    LIBRARY(
        id = "library",
        title = "Library",
        subtitle = "Folders, lyrics, scanner, and filters",
        icon = Icons.Default.FolderSpecial,
        iconRes = null
    ),
    APPEARANCE(
        id = "appearance",
        title = "Appearance",
        subtitle = "Theme, OLED mode, dynamic color, and accents",
        icon = Icons.Default.Palette,
        iconRes = null
    ),
    PLAYBACK(
        id = "playback",
        title = "Playback",
        subtitle = "Equalizer, sleep timer, gapless, and crossfade",
        icon = Icons.Default.PlayCircle,
        iconRes = null
    ),
    BEHAVIOR(
        id = "behavior",
        title = "Behavior",
        subtitle = "Launch tab, navigation styles, and carousels",
        icon = Icons.Default.Tune,
        iconRes = null
    ),
    BACKUP_RESTORE(
        id = "backup_restore",
        title = "Backup & Restore",
        subtitle = "Export or import playlists and preferences",
        icon = Icons.Default.CloudSync,
        iconRes = null
    ),
    IMPORT_PLAYLIST(
        id = "import_playlist",
        title = "Import Playlist",
        subtitle = "Import from Spotify, Apple Music, or YouTube Music links",
        icon = Icons.Default.Link,
        iconRes = null
    ),
    ABOUT(
        id = "about",
        title = "About",
        subtitle = "Version, open source licenses, and links",
        icon = Icons.Default.Info,
        iconRes = null
    ),
    EQUALIZER(
        id = "equalizer",
        title = "Equalizer",
        subtitle = "5-band EQ, bass boost, and audio effects",
        icon = Icons.Default.Equalizer,
        iconRes = null
    ),
    DEVICE_CAPABILITIES(
        id = "device_capabilities",
        title = "Device Capabilities",
        subtitle = "Hardware decoding, codecs, and audio output",
        icon = Icons.Default.Smartphone,
        iconRes = null
    ),
    ACCOUNTS(
        id = "accounts",
        title = "Accounts",
        subtitle = "YouTube Music, Last.fm, and cloud sync",
        icon = Icons.Default.AccountCircle,
        iconRes = null
    );

    companion object {
        fun fromId(id: String): SettingsCategory? {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) }
        }
    }
}
