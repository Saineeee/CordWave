package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LyricLine
import com.example.model.Lyrics

@Composable
fun SyncedLyricsView(
    lyrics: Lyrics?,
    isLoading: Boolean = false,
    currentPositionMs: Long,
    onSeekTo: (Long) -> Unit,
    onRefresh: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var syncOffsetMs by remember { mutableStateOf(0L) }
    val effectiveTimeMs = currentPositionMs + syncOffsetMs

    val listState = rememberLazyListState()
    val lines = lyrics?.lines ?: emptyList()
    val hasPlainLyrics = lyrics?.plainLyrics?.isNotBlank() == true

    val currentLineIndex = remember(effectiveTimeMs, lines) {
        if (lines.isEmpty()) -1
        else {
            val idx = lines.indexOfLast { it.timeMs <= effectiveTimeMs }
            if (idx == -1) 0 else idx
        }
    }

    // Auto scroll to active line for synced lyrics
    LaunchedEffect(currentLineIndex) {
        if (currentLineIndex in lines.indices) {
            val targetScroll = (currentLineIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(targetScroll)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("synced_lyrics_view"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Finding lyrics...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (lines.isNotEmpty()) {
            // 1. Synced LRC Lyrics View
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(vertical = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(lines) { index, line ->
                    val isCurrent = (index == currentLineIndex)
                    val isPast = (index < currentLineIndex)

                    val textColor by animateColorAsState(
                        targetValue = when {
                            isCurrent -> MaterialTheme.colorScheme.primary
                            isPast -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                        },
                        animationSpec = spring(),
                        label = "lyricsColor"
                    )

                    val lineScale by animateFloatAsState(
                        targetValue = if (isCurrent) 1.08f else 1.0f,
                        animationSpec = spring(),
                        label = "lyricsScale"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                            .scale(lineScale)
                            .clickable { onSeekTo(line.timeMs) }
                            .testTag("lyric_line_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = line.text,
                            style = if (isCurrent) {
                                MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    lineHeight = 32.sp
                                )
                            } else {
                                MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 26.sp
                                )
                            },
                            color = textColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Sync tuning bar at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { syncOffsetMs -= 500 }) {
                            Text("-0.5s", style = MaterialTheme.typography.labelMedium)
                        }
                        Text(
                            text = if (syncOffsetMs == 0L) "Synced" else "${if (syncOffsetMs > 0) "+" else ""}${syncOffsetMs / 1000f}s",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        TextButton(onClick = { syncOffsetMs += 500 }) {
                            Text("+0.5s", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        } else if (hasPlainLyrics) {
            // 2. Plain Text Lyrics (Scrollable format)
            val plainLines = (lyrics?.plainLyrics ?: "").lines()
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        Text(
                            text = "Plain Lyrics • ${lyrics?.provider ?: "Web"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
                itemsIndexed(plainLines) { _, lineText ->
                    if (lineText.isBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                    } else {
                        Text(
                            text = lineText,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Normal,
                                lineHeight = 28.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        } else {
            // 3. No lyrics found state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No lyrics found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "We couldn't find lyrics for this song",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    if (onRefresh != null) {
                        Spacer(modifier = Modifier.height(20.dp))
                        FilledTonalButton(
                            onClick = onRefresh,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry Search")
                        }
                    }
                }
            }
        }
    }
}

