package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.model.Song
import com.example.ui.components.AlbumArtCollage
import com.example.ui.components.CollapsibleCommonTopBar
import com.example.ui.components.SongListItem
import com.example.ui.components.rememberCollapsibleHeaderState
import com.example.ui.theme.TextSecondary

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
    val recentLazyListState = rememberLazyListState()
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = recentLazyListState)
    val headerHeightRange = 180.dp to 56.dp
    val headerState = rememberCollapsibleHeaderState(headerHeightRange)

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(headerState.nestedScrollConnection),
            contentPadding = PaddingValues(top = headerHeightRange.first + 8.dp, bottom = 120.dp)
        ) {
            // Hero Collage Section
            item {
                val collageSongs = quickPicks.ifEmpty { trendingSongs.ifEmpty { localSongs } }
                if (collageSongs.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        AlbumArtCollage(
                            songs = collageSongs,
                            onPlayAll = {
                                onPlayAll(collageSongs, false)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }

            // Quick Picks Section (Card Style with 30dp corners)
            if (quickPicks.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Quick Picks",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }

                val displayedQuickPicks = quickPicks.take(5)
                items(displayedQuickPicks) { song ->
                    SongListItem(
                        song = song,
                        isPlaying = isPlaying,
                        isCurrentSong = currentPlayingSong?.id == song.id,
                        onClick = { onSongClick(song, quickPicks) },
                        onLikeToggle = { onLikeToggle(song) },
                        onAddToPlaylist = { onAddToPlaylist(song) },
                        onPlayNext = { onPlayNext(song) },
                        onAddToQueue = { onAddToQueue(song) },
                        onDownload = { onDownload(song) },
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }
            }

            // Recent History Section (Horizontal Snap Carousel)
            if (recentHistory.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(28.dp))
                    Text(
                        text = "Recently Played",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )

                    LazyRow(
                        state = recentLazyListState,
                        flingBehavior = snapFlingBehavior,
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        items(recentHistory.take(10)) { song ->
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                modifier = Modifier
                                    .width(150.dp)
                                    .clickable { onSongClick(song, recentHistory) }
                                    .testTag("recent_song_${song.id}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(130.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!song.albumArtUri.isNullOrEmpty()) {
                                            AsyncImage(
                                                model = song.albumArtUri,
                                                contentDescription = song.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.MusicNote,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
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

            // Trending Section
            item {
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = "Trending",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }

            val displayedTrending = trendingSongs.take(10)
            items(displayedTrending) { song ->
                SongListItem(
                    song = song,
                    isPlaying = isPlaying,
                    isCurrentSong = currentPlayingSong?.id == song.id,
                    onClick = { onSongClick(song, trendingSongs) },
                    onLikeToggle = { onLikeToggle(song) },
                    onAddToPlaylist = { onAddToPlaylist(song) },
                    onPlayNext = { onPlayNext(song) },
                    onAddToQueue = { onAddToQueue(song) },
                    onDownload = { onDownload(song) },
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
            }
        }

        // Collapsible Animated Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerState.currentHeaderHeight)
                .align(Alignment.TopCenter)
                .zIndex(1f)
        ) {
            CollapsibleCommonTopBar(
                title = "Your Mix",
                subtitle = "Today's mix for you",
                collapseFraction = headerState.collapseFraction,
                headerHeight = headerState.currentHeaderHeight,
                showBackButton = false,
                onBackClick = {},
                actions = {
                    IconButton(
                        onClick = onOpenStats,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f), CircleShape)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                                shape = CircleShape
                            )
                            .testTag("home_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "History & Stats",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f), CircleShape)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                                shape = CircleShape
                            )
                            .testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            )
        }
    }
}
