package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.model.DownloadItem
import com.example.model.MediaSource
import com.example.model.Song
import com.example.ui.components.CollapsibleCommonTopBar
import com.example.ui.components.SongListItem
import com.example.ui.components.rememberCollapsibleHeaderState
import com.example.ui.components.scrollbar.ExpressiveScrollBar

enum class SongFilter {
    ALL,
    LOCAL,
    YOUTUBE,
    OFFLINE
}

enum class SongSort {
    DATE_ADDED,
    TITLE,
    ARTIST,
    DURATION
}

@Composable
fun SongsScreen(
    allSongs: List<Song>,
    downloads: List<DownloadItem>,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    onSongClick: (Song, List<Song>) -> Unit,
    onPlayAll: (List<Song>, Boolean) -> Unit,
    onLikeToggle: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onDownload: (Song) -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(SongFilter.ALL) }
    var selectedSort by remember { mutableStateOf(SongSort.DATE_ADDED) }
    var showSortMenu by remember { mutableStateOf(false) }

    val headerHeightRange = 180.dp to 56.dp
    val headerState = rememberCollapsibleHeaderState(headerHeightRange)
    val listState = rememberLazyListState()

    val filteredSongs = remember(allSongs, downloads, selectedFilter, selectedSort) {
        val list = when (selectedFilter) {
            SongFilter.ALL -> allSongs
            SongFilter.LOCAL -> allSongs.filter { it.source == MediaSource.LOCAL }
            SongFilter.YOUTUBE -> allSongs.filter { it.source == MediaSource.YOUTUBE }
            SongFilter.OFFLINE -> {
                val downloadedIds = downloads.map { it.songId }.toSet()
                allSongs.filter { it.source == MediaSource.LOCAL || downloadedIds.contains(it.id) }
            }
        }

        when (selectedSort) {
            SongSort.DATE_ADDED -> list.sortedByDescending { it.dateAdded }
            SongSort.TITLE -> list.sortedBy { it.title.lowercase() }
            SongSort.ARTIST -> list.sortedBy { it.artist.lowercase() }
            SongSort.DURATION -> list.sortedByDescending { it.durationMs }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("songs_screen")
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(headerState.nestedScrollConnection),
            contentPadding = PaddingValues(top = headerHeightRange.first + 8.dp, bottom = 120.dp)
        ) {
            // Filter Chips Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == SongFilter.ALL,
                        onClick = { selectedFilter = SongFilter.ALL },
                        shape = CircleShape,
                        label = { Text("All (${allSongs.size})", style = MaterialTheme.typography.labelMedium) }
                    )
                    FilterChip(
                        selected = selectedFilter == SongFilter.LOCAL,
                        onClick = { selectedFilter = SongFilter.LOCAL },
                        shape = CircleShape,
                        leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("Local", style = MaterialTheme.typography.labelMedium) }
                    )
                    FilterChip(
                        selected = selectedFilter == SongFilter.YOUTUBE,
                        onClick = { selectedFilter = SongFilter.YOUTUBE },
                        shape = CircleShape,
                        leadingIcon = { Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("YouTube", style = MaterialTheme.typography.labelMedium) }
                    )
                    FilterChip(
                        selected = selectedFilter == SongFilter.OFFLINE,
                        onClick = { selectedFilter = SongFilter.OFFLINE },
                        shape = CircleShape,
                        leadingIcon = { Icon(Icons.Default.DownloadDone, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("Offline", style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            // Play All / Shuffle Buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onPlayAll(filteredSongs, false) },
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        enabled = filteredSongs.isNotEmpty()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Play All", style = MaterialTheme.typography.labelLarge)
                    }

                    FilledTonalButton(
                        onClick = { onPlayAll(filteredSongs, true) },
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        enabled = filteredSongs.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Shuffle", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            if (filteredSongs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.MusicOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No songs found for this filter",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredSongs) { song ->
                    SongListItem(
                        song = song,
                        isPlaying = isPlaying,
                        isCurrentSong = currentPlayingSong?.id == song.id,
                        onClick = { onSongClick(song, filteredSongs) },
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

        // Expressive scrollbar on the end edge, above the list
        ExpressiveScrollBar(
            listState = listState,
            modifier = Modifier.align(Alignment.CenterEnd),
            dragLabelProvider = { index ->
                // Item 0 is the filter chip row, item 1 the play/shuffle buttons.
                filteredSongs.getOrNull(index - 2)?.title?.firstOrNull()?.uppercase()
            }
        )

        // Collapsible Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerState.currentHeaderHeight)
                .align(Alignment.TopCenter)
                .zIndex(1f)
        ) {
            CollapsibleCommonTopBar(
                title = "All Songs",
                subtitle = "${filteredSongs.size} tracks available",
                collapseFraction = headerState.collapseFraction,
                headerHeight = headerState.currentHeaderHeight,
                showBackButton = onBack != null,
                onBackClick = { onBack?.invoke() },
                actions = {
                    Box {
                        FilledTonalIconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.size(42.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Icon(
                                Icons.Default.Sort,
                                contentDescription = "Sort Songs",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Date Added") },
                                onClick = {
                                    selectedSort = SongSort.DATE_ADDED
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Title (A-Z)") },
                                onClick = {
                                    selectedSort = SongSort.TITLE
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Artist (A-Z)") },
                                onClick = {
                                    selectedSort = SongSort.ARTIST
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Duration") },
                                onClick = {
                                    selectedSort = SongSort.DURATION
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            )
        }
    }
}
