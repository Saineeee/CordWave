package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Song
import com.example.ui.components.AlbumCard
import com.example.ui.components.ArtistCard

enum class AlbumArtistTab {
    ALBUMS,
    ARTISTS
}

@Composable
fun AlbumsArtistsScreen(
    allSongs: List<Song>,
    onSelectAlbum: (Album) -> Unit,
    onSelectArtist: (Artist) -> Unit,
    onPlayAlbum: (Album) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentSubTab by remember { mutableStateOf(AlbumArtistTab.ALBUMS) }

    val albums = remember(allSongs) {
        allSongs.groupBy { it.album }
            .map { (albumTitle, songList) ->
                val first = songList.first()
                Album(
                    id = "album_${albumTitle.hashCode()}",
                    title = albumTitle,
                    artist = first.artist,
                    artworkUri = first.albumArtUri,
                    year = first.year,
                    songCount = songList.size
                )
            }.sortedBy { it.title.lowercase() }
    }

    val artists = remember(allSongs) {
        allSongs.groupBy { it.artist }
            .map { (artistName, songList) ->
                val first = songList.first()
                val albumCount = songList.map { it.album }.distinct().size
                Artist(
                    id = "artist_${artistName.hashCode()}",
                    name = artistName,
                    artworkUri = first.albumArtUri,
                    songCount = songList.size,
                    albumCount = albumCount
                )
            }.sortedBy { it.name.lowercase() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("albums_artists_screen")
    ) {
        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)
        ) {
            Text(
                text = "Artists & Albums",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Pixel-style Pill Segmented Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = currentSubTab == AlbumArtistTab.ALBUMS,
                    onClick = { currentSubTab = AlbumArtistTab.ALBUMS },
                    shape = CircleShape,
                    leadingIcon = { Icon(Icons.Default.Album, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("Albums (${albums.size})", style = MaterialTheme.typography.labelMedium) }
                )
                FilterChip(
                    selected = currentSubTab == AlbumArtistTab.ARTISTS,
                    onClick = { currentSubTab = AlbumArtistTab.ARTISTS },
                    shape = CircleShape,
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("Artists (${artists.size})", style = MaterialTheme.typography.labelMedium) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (currentSubTab == AlbumArtistTab.ALBUMS) {
            if (albums.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No albums available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(albums) { album ->
                        AlbumCard(
                            album = album,
                            onClick = { onSelectAlbum(album) },
                            onPlay = { onPlayAlbum(album) }
                        )
                    }
                }
            }
        } else {
            if (artists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No artists available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(artists) { artist ->
                        ArtistCard(
                            artist = artist,
                            onClick = { onSelectArtist(artist) }
                        )
                    }
                }
            }
        }
    }
}

