package com.example.data.playlistimport

import com.example.model.Song
import java.util.UUID

enum class PlaylistSource {
    SPOTIFY,
    APPLE_MUSIC,
    YOUTUBE_MUSIC,
    UNKNOWN
}

data class RemoteTrack(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val artist: String,
    val album: String = "",
    val durationMs: Long = 0L,
    val isMatched: Boolean = false,
    val matchedSong: Song? = null
)

data class PlaylistImportResult(
    val source: PlaylistSource,
    val playlistName: String,
    val description: String = "",
    val coverUrl: String? = null,
    val tracks: List<RemoteTrack> = emptyList(),
    val originalUrl: String = ""
)

data class ImportProgress(
    val current: Int = 0,
    val totalTracks: Int = 0,
    val matchedTracks: Int = 0,
    val isComplete: Boolean = false,
    val createdPlaylistId: String? = null
)
