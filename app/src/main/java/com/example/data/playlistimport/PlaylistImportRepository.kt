package com.example.data.playlistimport

import android.content.Context
import com.example.data.local.OuterTuneDatabase
import com.example.data.repository.MusicRepository
import com.example.model.Playlist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class PlaylistImportRepository(
    private val context: Context,
    private val musicRepository: MusicRepository? = null,
    private val urlParser: PlaylistUrlParser = PlaylistUrlParser(),
    private val spotifyClient: SpotifyApiClient = SpotifyApiClient(),
    private val appleMusicClient: AppleMusicApiClient = AppleMusicApiClient(),
    private val ytMusicClient: YouTubeMusicPlaylistClient = YouTubeMusicPlaylistClient(),
    private val songMatcher: SongMatcher = SongMatcher(musicRepository)
) {

    suspend fun parsePlaylist(url: String): PlaylistImportResult = withContext(Dispatchers.IO) {
        val trimmed = url.trim()
        val source = urlParser.detectSource(trimmed)
        val id = urlParser.extractId(trimmed) ?: "playlist"

        val rawResult = when (source) {
            PlaylistSource.SPOTIFY -> spotifyClient.fetchPlaylist(trimmed, id)
            PlaylistSource.APPLE_MUSIC -> appleMusicClient.fetchPlaylist(trimmed, id)
            PlaylistSource.YOUTUBE_MUSIC -> ytMusicClient.fetchPlaylist(trimmed, id)
            PlaylistSource.UNKNOWN -> {
                // If unknown source, try to detect or provide default
                spotifyClient.fetchPlaylist(trimmed, id)
            }
        }

        // Run matcher on tracks to pre-evaluate match status
        val matchedTracks = songMatcher.matchAll(rawResult.tracks)
        rawResult.copy(tracks = matchedTracks)
    }

    fun importPlaylist(result: PlaylistImportResult): Flow<ImportProgress> = flow {
        val total = result.tracks.size
        emit(ImportProgress(current = 0, totalTracks = total, matchedTracks = 0, isComplete = false))

        // Create target playlist in MusicRepository
        val playlistTitle = result.playlistName.ifBlank { "Imported Playlist" }
        val playlistDesc = result.description.ifBlank { "Imported from ${result.source.name}" }

        val createdPlaylist: Playlist? = if (musicRepository != null) {
            musicRepository.createPlaylist(playlistTitle, playlistDesc)
        } else {
            val db = OuterTuneDatabase.getInstance(context)
            val id = "pl_" + java.util.UUID.randomUUID().toString().take(8)
            db.playlistDao().insertPlaylist(
                com.example.data.local.PlaylistEntity(
                    id = id,
                    title = playlistTitle,
                    description = playlistDesc,
                    artworkUri = result.coverUrl,
                    createdAt = System.currentTimeMillis()
                )
            )
            Playlist(id = id, title = playlistTitle, description = playlistDesc, artworkUri = result.coverUrl)
        }

        var matchedCount = 0

        result.tracks.forEachIndexed { index, track ->
            delay(120) // Give smooth visual feedback to progress UI
            val matched = if (track.isMatched && track.matchedSong != null) {
                track
            } else {
                songMatcher.matchTrack(track)
            }

            if (matched.isMatched && matched.matchedSong != null && createdPlaylist != null) {
                matchedCount++
                musicRepository?.addSongToPlaylist(createdPlaylist.id, matched.matchedSong)
            }

            emit(
                ImportProgress(
                    current = index + 1,
                    totalTracks = total,
                    matchedTracks = matchedCount,
                    isComplete = false,
                    createdPlaylistId = createdPlaylist?.id
                )
            )
        }

        // Final completion event
        emit(
            ImportProgress(
                current = total,
                totalTracks = total,
                matchedTracks = matchedCount,
                isComplete = true,
                createdPlaylistId = createdPlaylist?.id
            )
        )
    }.flowOn(Dispatchers.IO)
}
