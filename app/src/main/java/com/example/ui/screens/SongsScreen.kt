package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DownloadItem
import com.example.model.MediaSource
import com.example.model.Song
import com.example.ui.components.SongListItem

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
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(SongFilter.ALL) }
    var selectedSort by remember { mutableStateOf(SongSort.DATE_ADDED) }
    var showSortMenu by remember { mutableStateOf(false) }

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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("songs_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All Songs",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )

                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort Songs")
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

                Text(
                    text = "${filteredSongs.size} tracks available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Filter Chips Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == SongFilter.ALL,
                    onClick = { selectedFilter = SongFilter.ALL },
                    label = { Text("All (${allSongs.size})") }
                )
                FilterChip(
                    selected = selectedFilter == SongFilter.LOCAL,
                    onClick = { selectedFilter = SongFilter.LOCAL },
                    leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("Local Only") }
                )
                FilterChip(
                    selected = selectedFilter == SongFilter.YOUTUBE,
                    onClick = { selectedFilter = SongFilter.YOUTUBE },
                    leadingIcon = { Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("YouTube Music") }
                )
                FilterChip(
                    selected = selectedFilter == SongFilter.OFFLINE,
                    onClick = { selectedFilter = SongFilter.OFFLINE },
                    leadingIcon = { Icon(Icons.Default.DownloadDone, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("Offline Ready") }
                )
            }
        }

        // Play All / Shuffle Buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onPlayAll(filteredSongs, false) },
                    modifier = Modifier.weight(1f),
                    enabled = filteredSongs.isNotEmpty()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play All")
                }

                FilledTonalButton(
                    onClick = { onPlayAll(filteredSongs, true) },
                    modifier = Modifier.weight(1f),
                    enabled = filteredSongs.isNotEmpty()
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Shuffle")
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
}
