package com.example.data.playlistimport

import java.net.URI

class PlaylistUrlParser {

    fun detectSource(url: String): PlaylistSource {
        val trimmed = url.trim().lowercase()
        return when {
            trimmed.contains("spotify.com/playlist") || trimmed.contains("spotify.link") -> PlaylistSource.SPOTIFY
            trimmed.contains("music.apple.com") && trimmed.contains("/playlist/") -> PlaylistSource.APPLE_MUSIC
            trimmed.contains("youtube.com/playlist") || trimmed.contains("youtu.be") || (trimmed.contains("youtube.com") && trimmed.contains("list=")) -> PlaylistSource.YOUTUBE_MUSIC
            else -> PlaylistSource.UNKNOWN
        }
    }

    fun extractId(url: String): String? {
        val trimmed = url.trim()
        return try {
            val source = detectSource(trimmed)
            when (source) {
                PlaylistSource.SPOTIFY -> {
                    // https://open.spotify.com/playlist/37i9dQZF1DXcBWIGoYBM5M?si=...
                    val uri = URI(trimmed)
                    val path = uri.path
                    val parts = path.split("/")
                    val playlistIndex = parts.indexOf("playlist")
                    if (playlistIndex != -1 && playlistIndex + 1 < parts.size) {
                        parts[playlistIndex + 1].substringBefore("?")
                    } else null
                }
                PlaylistSource.APPLE_MUSIC -> {
                    // https://music.apple.com/us/playlist/todays-hits/pl.f4d106fed2bd41149aaacabb233eb5eb
                    val uri = URI(trimmed)
                    val path = uri.path
                    val parts = path.split("/")
                    parts.lastOrNull { it.isNotBlank() }?.substringBefore("?")
                }
                PlaylistSource.YOUTUBE_MUSIC -> {
                    // https://music.youtube.com/playlist?list=PL4fGSI1pDJn6jXS_5NWD36m_R4Bq92330
                    if (trimmed.contains("list=")) {
                        trimmed.substringAfter("list=").substringBefore("&").substringBefore(" ")
                    } else null
                }
                PlaylistSource.UNKNOWN -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}
