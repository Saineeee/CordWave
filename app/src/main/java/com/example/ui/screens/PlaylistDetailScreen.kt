package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.model.Playlist
import com.example.model.Song
import com.example.ui.components.CollapsibleCommonTopBar
import com.example.ui.components.SongListItem
import com.example.ui.components.rememberCollapsibleHeaderState

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
    val headerHeightRange = 180.dp to 56.dp
    val headerState = rememberCollapsibleHeaderState(headerHeightRange)

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("playlist_detail_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(headerState.nestedScrollConnection),
            contentPadding = PaddingValues(top = headerHeightRange.first + 8.dp, bottom = 120.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
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

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "${songs.size} tracks",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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

        // Collapsible Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerState.currentHeaderHeight)
                .align(Alignment.TopCenter)
                .zIndex(1f)
        ) {
            CollapsibleCommonTopBar(
                title = playlist.title,
                subtitle = if (playlist.description.isNotBlank()) playlist.description else "${songs.size} tracks • Playlist",
                collapseFraction = headerState.collapseFraction,
                headerHeight = headerState.currentHeaderHeight,
                showBackButton = true,
                onBackClick = onBack,
                actions = {
                    if (playlist.isEditable) {
                        IconButton(
                            onClick = onDeletePlaylist,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f), CircleShape)
                                .testTag("playlist_delete_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Playlist",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            )
        }
    }
}
