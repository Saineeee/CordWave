package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.components.SongListItem
import java.util.Calendar

@Composable
fun HomeScreen(
    quickPicks: List<Song>,
    trendingSongs: List<Song>,
    recentHistory: List<Song>,
    localSongs: List<Song>,
    moodPlaylists: Map<String, List<Song>>,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    onSongClick: (Song, List<Song>) -> Unit,
    onPlayAll: (List<Song>, Boolean) -> Unit,
    onLikeToggle: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onDownload: (Song) -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
    onRescanLocal: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMood by remember { mutableStateOf("All") }
    val moods = listOf("All", "Workout & Energy", "Chill & Lo-Fi", "Deep Focus", "Party & Drive")

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..22 -> "Good evening"
            else -> "Good night"
        }
    }

    val displayedSongs = if (selectedMood == "All") {
        trendingSongs
    } else {
        moodPlaylists[selectedMood] ?: trendingSongs
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // App Bar & Brand Header (Pixel Style)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "OuterTune",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = onOpenStats,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("home_stats_button"),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Insights,
                                contentDescription = "Stats",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        FilledTonalIconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("home_settings_button"),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pixel Mood Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    moods.forEach { mood ->
                        FilterChip(
                            selected = selectedMood == mood,
                            onClick = { selectedMood = mood },
                            label = { Text(mood, style = MaterialTheme.typography.labelMedium) },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = null
                        )
                    }
                }
            }
        }

        // Quick Picks Carousel (YouTube Music style on Pixel)
        if (quickPicks.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick Picks",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    FilledTonalButton(
                        onClick = { onPlayAll(quickPicks, true) },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Shuffle All", style = MaterialTheme.typography.labelMedium)
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    items(quickPicks) { song ->
                        Card(
                            modifier = Modifier
                                .width(160.dp)
                                .clickable { onSongClick(song, quickPicks) }
                                .testTag("quick_pick_${song.id}"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(16.dp))
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
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }

                                    // Play icon overlay
                                    FilledIconButton(
                                        onClick = { onSongClick(song, quickPicks) },
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(6.dp)
                                            .size(36.dp),
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Local Storage Banner
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderSpecial,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Local Library",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${localSongs.size} songs scanned on device storage",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = onRescanLocal,
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Rescan", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // Trending & Categorized Tracks Section
        item {
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedMood == "All") "Trending & Recommended" else selectedMood,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                FilledTonalButton(
                    onClick = { onPlayAll(displayedSongs, false) },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play All", style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(displayedSongs) { song ->
            SongListItem(
                song = song,
                isPlaying = isPlaying,
                isCurrentSong = currentPlayingSong?.id == song.id,
                onClick = { onSongClick(song, displayedSongs) },
                onLikeToggle = { onLikeToggle(song) },
                onAddToPlaylist = { onAddToPlaylist(song) },
                onPlayNext = { onPlayNext(song) },
                onAddToQueue = { onAddToQueue(song) },
                onDownload = { onDownload(song) },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        // Recently Played
        if (recentHistory.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = "Recently Played",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(recentHistory.take(6)) { song ->
                SongListItem(
                    song = song,
                    isPlaying = isPlaying,
                    isCurrentSong = currentPlayingSong?.id == song.id,
                    onClick = { onSongClick(song, recentHistory) },
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

