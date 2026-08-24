package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
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
fun MiniPlayer(
    song: Song?,
    isPlaying: Boolean,
    progress: Float,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = song != null,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = modifier
    ) {
        if (song != null) {
            var totalDrag by remember { mutableStateOf(0f) }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .height(68.dp)
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .clickable(onClick = onClick)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (totalDrag > 80f) {
                                    onSkipPrevious()
                                } else if (totalDrag < -80f) {
                                    onSkipNext()
                                }
                                totalDrag = 0f
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                totalDrag += dragAmount
                            }
                        )
                    }
                    .testTag("mini_player_container"),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 8.dp
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 10.dp, end = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Artwork Squircle
                        Box(
                            modifier = Modifier
                                .size(48.dp)
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
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Title & Artist
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${song.artist} • ${if (song.source == MediaSource.LOCAL) "On Device" else "Online"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Play/Pause Pill Button
                        FilledIconButton(
                            onClick = onPlayPause,
                            modifier = Modifier
                                .size(42.dp)
                                .testTag("mini_player_play_pause"),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Skip Next
                        IconButton(
                            onClick = onSkipNext,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("mini_player_skip_next")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Skip Next",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Bottom progress indicator line with rounded track
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .align(Alignment.BottomCenter),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

