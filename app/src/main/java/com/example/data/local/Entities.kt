package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.DownloadStatus
import com.example.model.MediaSource
import com.example.model.Song

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val albumArtUri: String?,
    val mediaUri: String,
    val source: String, // LOCAL or YOUTUBE
    val isLiked: Boolean = false,
    val year: Int? = null,
    val trackNumber: Int? = null,
    val genre: String? = null,
    val bitrate: String? = null,
    val sampleRate: String? = null,
    val filePath: String? = null,
    val sizeBytes: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis(),
    val playCount: Int = 0,
    val lastPlayedTime: Long = 0L
) {
    fun toSong(): Song = Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        albumArtUri = albumArtUri,
        mediaUri = mediaUri,
        source = if (source == "LOCAL") MediaSource.LOCAL else MediaSource.YOUTUBE,
        isLiked = isLiked,
        year = year,
        trackNumber = trackNumber,
        genre = genre,
        bitrate = bitrate ?: "320 kbps",
        sampleRate = sampleRate ?: "44.1 kHz",
        filePath = filePath,
        sizeBytes = sizeBytes,
        dateAdded = dateAdded,
        playCount = playCount
    )

    companion object {
        fun fromSong(song: Song, playCount: Int = 0, lastPlayedTime: Long = 0L): SongEntity = SongEntity(
            id = song.id,
            title = song.title,
            artist = song.artist,
            album = song.album,
            durationMs = song.durationMs,
            albumArtUri = song.albumArtUri,
            mediaUri = song.mediaUri,
            source = song.source.name,
            isLiked = song.isLiked,
            year = song.year,
            trackNumber = song.trackNumber,
            genre = song.genre,
            bitrate = song.bitrate,
            sampleRate = song.sampleRate,
            filePath = song.filePath,
            sizeBytes = song.sizeBytes,
            dateAdded = song.dateAdded,
            playCount = playCount,
            lastPlayedTime = lastPlayedTime
        )
    }
}

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val artworkUri: String? = null,
    val isLocalOnly: Boolean = true,
    val isEditable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"],
    indices = [Index("songId"), Index("playlistId")]
)
data class PlaylistSongCrossRef(
    val playlistId: String,
    val songId: String,
    val position: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playback_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val historyId: Long = 0L,
    val songId: String,
    val playedAt: Long = System.currentTimeMillis(),
    val listenedDurationMs: Long = 0L
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val songId: String,
    val status: String = DownloadStatus.NOT_DOWNLOADED.name,
    val progress: Float = 0f,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val localFilePath: String? = null,
    val downloadedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "queue_state")
data class QueueStateEntity(
    @PrimaryKey val id: Int = 1,
    val currentSongId: String? = null,
    val queueSongIds: String = "", // Comma-separated or JSON list of IDs
    val queueIndex: Int = 0,
    val playbackPositionMs: Long = 0L,
    val isShuffle: Boolean = false,
    val repeatMode: String = "OFF"
)
