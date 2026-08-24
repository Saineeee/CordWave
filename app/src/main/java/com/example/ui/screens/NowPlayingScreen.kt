package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Lyrics
import com.example.model.MediaSource
import com.example.model.RepeatMode
import com.example.model.Song
import com.example.ui.components.SyncedLyricsView
import com.example.ui.components.formatDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    song: Song?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    lyrics: Lyrics?,
    isLoadingLyrics: Boolean,
    onRefreshLyrics: (() -> Unit)? = null,
    onCollapse: () -> Unit,
    onPlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleLike: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenQueue: () -> Unit,
    onDownload: () -> Unit,
    onAddToPlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (song == null) return

    var showLyricsView by remember { mutableStateOf(false) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderTempPosition by remember { mutableStateOf(0f) }

    val progress = if (durationMs > 0) {
        if (isDraggingSlider) sliderTempPosition else (currentPositionMs.toFloat() / durationMs.toFloat())
    } else 0f

    val displayedPosition = if (isDraggingSlider) (sliderTempPosition * durationMs).toLong() else currentPositionMs

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("now_playing_screen")
    ) {
        // Pixel Atmospheric Ambient Tint
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = onCollapse,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("now_playing_collapse"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = CircleShape
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (song.source == MediaSource.LOCAL) "Device Storage" else "YouTube Music",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalIconButton(
                        onClick = onOpenEqualizer,
                        modifier = Modifier.size(42.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "Equalizer",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    FilledTonalIconButton(
                        onClick = onOpenSleepTimer,
                        modifier = Modifier.size(42.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = "Sleep Timer",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Center Content: Album Artwork or Synced Lyrics
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = showLyricsView,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "ArtworkLyricsTransition"
                ) { targetLyrics ->
                    if (targetLyrics) {
                        SyncedLyricsView(
                            lyrics = lyrics,
                            isLoading = isLoadingLyrics,
                            currentPositionMs = currentPositionMs,
                            onSeekTo = onSeekTo,
                            onRefresh = onRefreshLyrics,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Large Pixel Squircle Artwork Card
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth(0.92f)
                                    .aspectRatio(1f)
                                    .shadow(28.dp, RoundedCornerShape(28.dp))
                                    .clip(RoundedCornerShape(28.dp))
                                    .clickable { showLyricsView = true }
                                    .testTag("now_playing_art"),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
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
                                            modifier = Modifier.size(96.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Song Info & Favorite Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${song.artist} • ${song.album}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // Pixel Audio Spec pill
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${song.bitrate ?: "320 kbps"} • ${song.sampleRate ?: "44.1 kHz"} • Lossless Hi-Fi",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                FilledTonalIconButton(
                    onClick = onToggleLike,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("now_playing_like_button"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (song.isLiked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Icon(
                        imageVector = if (song.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (song.isLiked) "Unlike" else "Like",
                        tint = if (song.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pixel Seek Bar Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = progress.coerceIn(0f, 1f),
                    onValueChange = {
                        isDraggingSlider = true
                        sliderTempPosition = it
                    },
                    onValueChangeFinished = {
                        isDraggingSlider = false
                        onSeekTo((sliderTempPosition * durationMs).toLong())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .testTag("now_playing_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration(displayedPosition),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDuration(durationMs),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Playback Controls (Shuffle, Prev, Play/Pause, Next, Repeat)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("now_playing_shuffle")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous
                FilledTonalIconButton(
                    onClick = onSkipPrevious,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("now_playing_prev"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        modifier = Modifier.size(30.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Play / Pause FAB
                FilledIconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .size(76.dp)
                        .shadow(16.dp, CircleShape)
                        .testTag("now_playing_play_pause"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Next
                FilledTonalIconButton(
                    onClick = onSkipNext,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("now_playing_next"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        modifier = Modifier.size(30.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Repeat Mode
                IconButton(
                    onClick = onToggleRepeat,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("now_playing_repeat")
                ) {
                    Icon(
                        imageVector = when (repeatMode) {
                            RepeatMode.OFF -> Icons.Default.Repeat
                            RepeatMode.ALL -> Icons.Default.Repeat
                            RepeatMode.ONE -> Icons.Default.RepeatOne
                        },
                        contentDescription = "Repeat",
                        tint = if (repeatMode != RepeatMode.OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Toolbar (Pixel Pill Chips: Lyrics toggle, Queue, Add to Playlist, Download)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = showLyricsView,
                    onClick = { showLyricsView = !showLyricsView },
                    leadingIcon = { Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("Lyrics", style = MaterialTheme.typography.labelSmall) },
                    shape = CircleShape,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("toggle_lyrics_chip"),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    border = null
                )

                FilterChip(
                    selected = false,
                    onClick = onOpenQueue,
                    leadingIcon = { Icon(Icons.Default.QueueMusic, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("Queue", style = MaterialTheme.typography.labelSmall) },
                    shape = CircleShape,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_queue_chip"),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = null
                )

                FilterChip(
                    selected = false,
                    onClick = onAddToPlaylist,
                    leadingIcon = { Icon(Icons.Default.PlaylistAdd, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("Playlist", style = MaterialTheme.typography.labelSmall) },
                    shape = CircleShape,
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = null
                )

                FilterChip(
                    selected = false,
                    onClick = onDownload,
                    leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("Offline", style = MaterialTheme.typography.labelSmall) },
                    shape = CircleShape,
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = null
                )
            }
        }
    }
}

