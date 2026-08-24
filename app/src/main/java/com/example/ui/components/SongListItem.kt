package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.MediaSource
import com.example.model.Song

@Composable
fun SongListItem(
    song: Song,
    isPlaying: Boolean,
    isCurrentSong: Boolean,
    onClick: () -> Unit,
    onLikeToggle: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable(onClick = onClick)
            .testTag("song_item_${song.id}"),
        color = if (isCurrentSong) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artwork Squircle
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                if (!song.albumArtUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = song.albumArtUri,
                        contentDescription = song.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = if (song.source == MediaSource.LOCAL) Icons.Default.MusicNote else Icons.Default.CloudQueue,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                if (isCurrentSong) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                            contentDescription = "Playing",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Song Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isCurrentSong) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Source badge
                    Surface(
                        color = when (song.source) {
                            MediaSource.LOCAL -> MaterialTheme.colorScheme.secondaryContainer
                            MediaSource.YOUTUBE -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                        },
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = if (song.source == MediaSource.LOCAL) "LOCAL" else "ONLINE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (song.source == MediaSource.LOCAL) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "${song.artist} • ${formatDuration(song.durationMs)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Like action
            IconButton(
                onClick = onLikeToggle,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("like_button_${song.id}")
            ) {
                Icon(
                    imageVector = if (song.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (song.isLiked) "Unlike" else "Like",
                    tint = if (song.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // More Options Dropdown
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("more_button_${song.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    shape = RoundedCornerShape(18.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    DropdownMenuItem(
                        text = { Text("Play Next", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = { Icon(Icons.Default.QueuePlayNext, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        onClick = {
                            showMenu = false
                            onPlayNext()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add to Queue", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = { Icon(Icons.Default.AddToQueue, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        onClick = {
                            showMenu = false
                            onAddToQueue()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add to Playlist", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = { Icon(Icons.Default.PlaylistAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        onClick = {
                            showMenu = false
                            onAddToPlaylist()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Download Offline", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        onClick = {
                            showMenu = false
                            onDownload()
                        }
                    )
                }
            }
        }
    }
}

fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0) return "0:00"
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

