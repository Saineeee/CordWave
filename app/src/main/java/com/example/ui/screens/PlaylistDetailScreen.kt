package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Playlist
import com.example.model.Song
import com.example.ui.components.SongListItem

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    songs: List<Song>,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onPlayAll: (List<Song>, Boolean) -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onLikeToggle: (Song) -> Unit,
    onRemoveSongFromPlaylist: (String) -> Unit,
    onDeletePlaylist: () -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onDownload: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("playlist_detail_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            // Header
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
                    text = "Playlist",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (playlist.isEditable) {
                    FilledTonalIconButton(
                        onClick = onDeletePlaylist,
                        modifier = Modifier.size(42.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete Playlist",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Playlist Info Banner
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(68.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = playlist.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (playlist.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = playlist.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${songs.size} tracks",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Play / Shuffle Buttons
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

        if (songs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No songs in this playlist",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
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
}

