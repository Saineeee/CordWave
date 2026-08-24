package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Album
import com.example.model.Song
import com.example.ui.components.SongListItem

@Composable
fun AlbumDetailScreen(
    album: Album,
    songs: List<Song>,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onPlayAll: (List<Song>, Boolean) -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onLikeToggle: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onDownload: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("album_detail_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = onBack,
                    modifier = Modifier.size(42.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Album Details",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Cover and Album info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    if (!album.artworkUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = album.artworkUri,
                            contentDescription = album.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Album,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = album.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${album.artist} • ${album.year ?: "Album"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${songs.size} tracks",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onPlayAll(songs, false) },
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        enabled = songs.isNotEmpty()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Play All", style = MaterialTheme.typography.labelLarge)
                    }

                    FilledTonalButton(
                        onClick = { onPlayAll(songs, true) },
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        enabled = songs.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Shuffle", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        items(songs) { song ->
            SongListItem(
                song = song,
                isPlaying = isPlaying,
                isCurrentSong = currentPlayingSong?.id == song.id,
                onClick = { onSongClick(song, songs) },
                onLikeToggle = { onLikeToggle(song) },
                onAddToPlaylist = { onAddToPlaylist(song) },
                onPlayNext = { onPlayNext(song) },
                onAddToQueue = { onAddToQueue(song) },
                onDownload = { onDownload(song) },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

