package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.model.*
import com.example.ui.components.AlbumCard
import com.example.ui.components.ArtistCard
import com.example.ui.components.CollapsibleCommonTopBar
import com.example.ui.components.SongListItem
import com.example.ui.components.rememberCollapsibleHeaderState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    query: String,
    searchResults: SearchResult,
    isSearching: Boolean,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    onQueryChange: (String) -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onSelectAlbum: (Album) -> Unit,
    onSelectArtist: (Artist) -> Unit,
    onLikeToggle: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onDownload: (Song) -> Unit,
    recentSearches: List<String> = listOf("Lo-fi Chill", "Acoustic Pop", "Synthwave", "Top Hits", "Piano Relaxation", "Electronic"),
    onSearchFocusChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Songs", "Albums", "Artists", "Playlists")
    var isSearchFocused by remember { mutableStateOf(false) }

    val headerHeightRange = 180.dp to 56.dp
    val headerState = rememberCollapsibleHeaderState(headerHeightRange)

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("search_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(headerState.nestedScrollConnection),
            contentPadding = PaddingValues(top = headerHeightRange.first + 8.dp, bottom = 120.dp)
        ) {
            // Search Bar & Filter Chips
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    // Full Pill Search Bar (surfaceContainerLow, 24dp corner shape)
                    TextField(
                        value = query,
                        onValueChange = onQueryChange,
                        placeholder = {
                            Text(
                                "Search songs, albums, artists...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search", modifier = Modifier.size(20.dp))
                                }
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                isSearchFocused = focusState.isFocused
                                onSearchFocusChange(focusState.isFocused)
                            }
                            .testTag("search_text_field")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Horizontal Categories Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            val isSelected = searchCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { searchCategory = category },
                                label = {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    )
                                },
                                shape = CircleShape,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = null,
                                modifier = Modifier.height(36.dp)
                            )
                        }
                    }
                }
            }

            // Searching indicator
            if (isSearching) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Empty query: Suggestions & Recent Searches
            if (query.isEmpty() && !isSearching) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Recent Searches",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        recentSearches.forEach { item ->
                            Surface(
                                onClick = { onQueryChange(item) },
                                shape = RoundedCornerShape(16.dp),
                                color = Color.Transparent,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = item,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Songs Section
            if ((searchCategory == "All" || searchCategory == "Songs") && searchResults.songs.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Songs (${searchResults.songs.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                items(searchResults.songs) { song ->
                    SongListItem(
                        song = song,
                        isPlaying = isPlaying,
                        isCurrentSong = currentPlayingSong?.id == song.id,
                        onClick = { onSongClick(song, searchResults.songs) },
                        onLikeToggle = { onLikeToggle(song) },
                        onAddToPlaylist = { onAddToPlaylist(song) },
                        onPlayNext = { onPlayNext(song) },
                        onAddToQueue = { onAddToQueue(song) },
                        onDownload = { onDownload(song) },
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }
            }

            // Albums Section
            if ((searchCategory == "All" || searchCategory == "Albums") && searchResults.albums.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Albums (${searchResults.albums.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(searchResults.albums) { album ->
                            Box(modifier = Modifier.width(150.dp)) {
                                AlbumCard(
                                    album = album,
                                    onClick = { onSelectAlbum(album) },
                                    onPlay = {
                                        val albumSongs = searchResults.songs.filter { it.album == album.title }
                                        if (albumSongs.isNotEmpty()) onSongClick(albumSongs.first(), albumSongs)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Artists Section
            if ((searchCategory == "All" || searchCategory == "Artists") && searchResults.artists.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Artists (${searchResults.artists.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(searchResults.artists) { artist ->
                            Box(modifier = Modifier.width(150.dp)) {
                                ArtistCard(
                                    artist = artist,
                                    onClick = { onSelectArtist(artist) }
                                )
                            }
                        }
                    }
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
                title = "Search",
                subtitle = if (query.isNotEmpty()) "Results for \"$query\"" else "Discover music",
                collapseFraction = headerState.collapseFraction,
                headerHeight = headerState.currentHeaderHeight,
                showBackButton = false,
                onBackClick = {}
            )
        }
    }
}
