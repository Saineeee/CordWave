package com.example.data.scanner

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LocalMediaScanner(private val context: Context) {

    suspend fun scanLocalAudioFiles(): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Audio.Media.GENRE else MediaStore.Audio.Media._ID,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Audio.Media.YEAR else MediaStore.Audio.Media._ID,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Audio.Media.BITRATE else MediaStore.Audio.Media._ID,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Audio.Media.TRACK else MediaStore.Audio.Media._ID
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 10000"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val albumIdCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val sizeCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dateAddedCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val rawTitle = it.getString(titleCol) ?: "Unknown Title"
                    val rawArtist = it.getString(artistCol) ?: "Unknown Artist"
                    val rawAlbum = it.getString(albumCol) ?: "Unknown Album"
                    val duration = it.getLong(durationCol)
                    val filePath = it.getString(dataCol)
                    val albumId = it.getLong(albumIdCol)
                    val size = it.getLong(sizeCol)
                    val dateAdded = it.getLong(dateAddedCol) * 1000

                    val contentUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val artworkUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    ).toString()

                    // Custom tag parser handling edge cases like delimiters
                    val normalizedArtist = parseArtistTags(rawArtist)
                    val (bitrate, sampleRate) = extractAudioSpecs(filePath, it)

                    val song = Song(
                        id = "local_$id",
                        title = cleanTitle(rawTitle),
                        artist = normalizedArtist,
                        album = rawAlbum,
                        durationMs = duration,
                        albumArtUri = artworkUri,
                        mediaUri = contentUri.toString(),
                        source = MediaSource.LOCAL,
                        filePath = filePath,
                        sizeBytes = size,
                        dateAdded = dateAdded,
                        bitrate = bitrate,
                        sampleRate = sampleRate
                    )
                    songs.add(song)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        songs
    }

    private fun parseArtistTags(rawArtist: String): String {
        // Handles multi-artist delimiters: "\", "/", ";", "feat.", "ft."
        return rawArtist
            .replace("\\\\", ", ")
            .replace("\\", ", ")
            .replace(" / ", ", ")
            .replace("; ", ", ")
            .trim()
    }

    private fun cleanTitle(rawTitle: String): String {
        // Removes common file extension artifacts
        return rawTitle.removeSuffix(".mp3")
            .removeSuffix(".flac")
            .removeSuffix(".m4a")
            .removeSuffix(".ogg")
            .removeSuffix(".wav")
            .removeSuffix(".opus")
    }

    private fun extractAudioSpecs(filePath: String?, cursor: android.database.Cursor): Pair<String, String> {
        var bitrate = "320 kbps"
        var sampleRate = "44.1 kHz"

        if (filePath != null && File(filePath).exists()) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(filePath)
                val bitVal = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                if (bitVal != null) {
                    val kbps = bitVal.toLongOrNull()?.let { it / 1000 } ?: 320
                    bitrate = "$kbps kbps"
                }
                val sampleVal = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)
                if (sampleVal != null) {
                    val khz = sampleVal.toFloatOrNull()?.let { it / 1000f } ?: 44.1f
                    sampleRate = String.format("%.1f kHz", khz)
                }
                retriever.release()
            } catch (e: Exception) {
                // Ignore fallback
            }
        }
        return Pair(bitrate, sampleRate)
    }

    fun groupIntoAlbums(songs: List<Song>): List<Album> {
        return songs.groupBy { it.album to it.artist }.map { (pair, albumSongs) ->
            Album(
                id = "album_${pair.first.hashCode()}_${pair.second.hashCode()}",
                title = pair.first,
                artist = pair.second,
                year = albumSongs.firstOrNull()?.year,
                songCount = albumSongs.size,
                artworkUri = albumSongs.firstOrNull()?.albumArtUri,
                songs = albumSongs
            )
        }.sortedBy { it.title.lowercase() }
    }

    fun groupIntoArtists(songs: List<Song>): List<Artist> {
        val artistMap = mutableMapOf<String, MutableList<Song>>()
        songs.forEach { song ->
            val primaryArtist = song.artist.split(", ").firstOrNull()?.trim() ?: song.artist
            artistMap.getOrPut(primaryArtist) { mutableListOf() }.add(song)
        }

        return artistMap.map { (artistName, artistSongs) ->
            val albumCount = artistSongs.map { it.album }.distinct().size
            Artist(
                id = "artist_${artistName.hashCode()}",
                name = artistName,
                songCount = artistSongs.size,
                albumCount = albumCount,
                artworkUri = artistSongs.firstOrNull()?.albumArtUri,
                songs = artistSongs
            )
        }.sortedBy { it.name.lowercase() }
    }

    fun groupIntoFolders(songs: List<Song>): List<MediaFolder> {
        val folderMap = mutableMapOf<String, MutableList<Song>>()
        songs.forEach { song ->
            val path = song.filePath
            if (path != null) {
                val parent = File(path).parent ?: "Root"
                folderMap.getOrPut(parent) { mutableListOf() }.add(song)
            }
        }

        return folderMap.map { (folderPath, folderSongs) ->
            val folderName = File(folderPath).name.ifEmpty { folderPath }
            MediaFolder(
                path = folderPath,
                name = folderName,
                songCount = folderSongs.size,
                songs = folderSongs
            )
        }.sortedBy { it.name.lowercase() }
    }
}
