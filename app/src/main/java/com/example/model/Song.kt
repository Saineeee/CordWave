package com.example.model

enum class MediaSource {
    LOCAL,
    YOUTUBE
}

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

enum class DownloadStatus {
    NOT_DOWNLOADED,
    DOWNLOADING,
    COMPLETED,
    FAILED,
    PAUSED
}

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "Unknown Album",
    val durationMs: Long = 0L,
    val albumArtUri: String? = null,
    val mediaUri: String = "",
    val source: MediaSource = MediaSource.LOCAL,
    val isLiked: Boolean = false,
    val year: Int? = null,
    val trackNumber: Int? = null,
    val genre: String? = null,
    val bitrate: String? = "320 kbps",
    val sampleRate: String? = "44.1 kHz",
    val filePath: String? = null,
    val sizeBytes: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis(),
    val playCount: Int = 0
)

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val year: Int? = null,
    val songCount: Int = 0,
    val artworkUri: String? = null,
    val songs: List<Song> = emptyList()
)

data class Artist(
    val id: String,
    val name: String,
    val songCount: Int = 0,
    val albumCount: Int = 0,
    val artworkUri: String? = null,
    val songs: List<Song> = emptyList()
)

data class MediaFolder(
    val path: String,
    val name: String,
    val songCount: Int,
    val songs: List<Song> = emptyList()
)

data class Playlist(
    val id: String,
    val title: String,
    val description: String = "",
    val artworkUri: String? = null,
    val songCount: Int = 0,
    val isLocalOnly: Boolean = true,
    val isEditable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val songs: List<Song> = emptyList()
)

data class LyricWord(
    val word: String,
    val startMs: Long,
    val endMs: Long
)

data class LyricLine(
    val timeMs: Long,
    val text: String,
    val words: List<LyricWord> = emptyList(),
    val isInstrumental: Boolean = false
)

data class Lyrics(
    val songId: String,
    val lines: List<LyricLine> = emptyList(),
    val isSynced: Boolean = false,
    val plainLyrics: String = "",
    val provider: String = "LrcLib / Musixmatch"
)

data class AudioEffectConfig(
    val isEnabled: Boolean = true,
    val presetIndex: Int = 0,
    val bassBoostStrength: Int = 300, // 0 - 1000
    val virtualizerStrength: Int = 200, // 0 - 1000
    val bands: List<Int> = listOf(0, 0, 0, 0, 0), // millibels (-1000 to +1000)
    val tempo: Float = 1.0f, // 0.5x to 2.0x
    val pitch: Float = 1.0f, // 0.5x to 2.0x
    val replayGainEnabled: Boolean = true,
    val crossfadeMs: Int = 0 // 0 to 8000 ms
)

data class DownloadItem(
    val songId: String,
    val song: Song,
    val status: DownloadStatus = DownloadStatus.NOT_DOWNLOADED,
    val progress: Float = 0f,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val localFilePath: String? = null
)

data class ListeningStats(
    val totalPlayCount: Int = 0,
    val totalListeningTimeMs: Long = 0L,
    val topSongs: List<Pair<Song, Int>> = emptyList(),
    val topArtists: List<Pair<String, Int>> = emptyList(),
    val recentPlaysCount: Int = 0
)

data class SearchResult(
    val query: String = "",
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList()
)
