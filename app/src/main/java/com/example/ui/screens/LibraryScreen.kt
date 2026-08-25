package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.ui.components.scrollbar.ExpressiveScrollBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    likedSongs: List<Song> = emptyList(),
    downloads: List<DownloadItem> = emptyList(),
    recentHistory: List<Song> = emptyList(),
    playlists: List<Playlist> = emptyList(),
    allSongs: List<Song> = emptyList(),
    albums: List<Album> = emptyList(),
    artists: List<Artist> = emptyList(),
    folders: List<MediaFolder> = emptyList(),
    currentPlayingSong: Song? = null,
    isPlaying: Boolean = false,
    onOpenLikedSongs: () -> Unit = {},
    onOpenDownloads: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onOpenFolders: () -> Unit = {},
    onSelectPlaylist: (Playlist) -> Unit = {},
    onCreatePlaylist: (String) -> Unit = {},
    onDeletePlaylist: (String) -> Unit = {},
    onSongClick: (Song, List<Song>) -> Unit = { _, _ -> },
    onPlayAll: (List<Song>, Boolean) -> Unit = { _, _ -> },
    onLikeToggle: (Song) -> Unit = {},
    onAddToPlaylist: (Song) -> Unit = {},
    onPlayNext: (Song) -> Unit = {},
    onAddToQueue: (Song) -> Unit = {},
    onDownload: (Song) -> Unit = {},
    onSelectAlbum: (Album) -> Unit = {},
    onSelectArtist: (Artist) -> Unit = {},
    onSelectFolder: (MediaFolder) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("SONGS") }
    val categories = listOf("SONGS", "FAVOURITE", "ALBUMS", "ARTISTS", "PLAYLISTS", "FOLDERS")

    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistTitle by remember { mutableStateOf("") }
    var isSortAscending by remember { mutableStateOf(true) }

    // Effective songs list
    val effectiveSongs = remember(allSongs, likedSongs, isSortAscending) {
        val base = if (allSongs.isNotEmpty()) allSongs else likedSongs
        if (isSortAscending) base.sortedBy { it.title } else base.sortedByDescending { it.title }
    }

    // Effective favourite songs list
    val effectiveLikedSongs = remember(likedSongs, isSortAscending) {
        if (isSortAscending) likedSongs.sortedBy { it.title } else likedSongs.sortedByDescending { it.title }
    }

    // Effective albums
    val effectiveAlbums = remember(albums, effectiveSongs, isSortAscending) {
        if (albums.isNotEmpty()) {
            if (isSortAscending) albums.sortedBy { it.title } else albums.sortedByDescending { it.title }
        } else {
            effectiveSongs.groupBy { it.album to it.artist }.map { (key, songs) ->
                Album(
                    id = "${key.first}_${key.second}".hashCode().toString(),
                    title = key.first,
                    artist = key.second,
                    artworkUri = songs.firstOrNull()?.albumArtUri,
                    songCount = songs.size
                )
            }
        }
    }

    // Effective artists
    val effectiveArtists = remember(artists, effectiveSongs, isSortAscending) {
        if (artists.isNotEmpty()) {
            if (isSortAscending) artists.sortedBy { it.name } else artists.sortedByDescending { it.name }
        } else {
            effectiveSongs.groupBy { it.artist }.map { (artistName, songs) ->
                Artist(
                    id = artistName.hashCode().toString(),
                    name = artistName,
                    songCount = songs.size,
                    albumCount = songs.map { it.album }.distinct().size,
                    artworkUri = songs.firstOrNull()?.albumArtUri
                )
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = {
                Text(
                    "New Playlist",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                OutlinedTextField(
                    value = newPlaylistTitle,
                    onValueChange = { newPlaylistTitle = it },
                    label = { Text("Playlist Name") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistTitle.isNotBlank()) {
                            onCreatePlaylist(newPlaylistTitle)
                            newPlaylistTitle = ""
                            showCreateDialog = false
                        }
                    },
                    shape = CircleShape,
                    enabled = newPlaylistTitle.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val headerHeightRange = 180.dp to 56.dp
    val headerState = rememberCollapsibleHeaderState(headerHeightRange)

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("library_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(headerState.nestedScrollConnection)
                .padding(top = headerState.currentHeaderHeight)
        ) {
            // Filter Chips Row (Horizontal Scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
            categories.forEach { category ->
                val isSelected = selectedCategory == category
                Surface(
                    onClick = { selectedCategory = category },
                    shape = CircleShape,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    },
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("filter_chip_$category")
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }

        // Action Bar (Shuffle pill button on left, Sort icon on right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle Button
            Button(
                onClick = {
                    val songsToPlay = when (selectedCategory) {
                        "SONGS" -> effectiveSongs
                        "FAVOURITE" -> effectiveLikedSongs
                        "PLAYLISTS" -> likedSongs
                        else -> effectiveSongs
                    }
                    if (songsToPlay.isNotEmpty()) {
                        onPlayAll(songsToPlay, true)
                    }
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("library_shuffle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Shuffle",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Right side: New playlist if on PLAYLISTS or Sort icon
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (selectedCategory == "PLAYLISTS") {
                    IconButton(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f), CircleShape)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                                shape = CircleShape
                            )
                            .testTag("create_playlist_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Playlist",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { isSortAscending = !isSortAscending },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f), CircleShape)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                        .testTag("library_sort_button")
                ) {
                    Icon(
                        imageVector = if (isSortAscending) Icons.Default.SortByAlpha else Icons.Default.Sort,
                        contentDescription = "Sort",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Content Area with AnimatedContent
        AnimatedContent(
            targetState = selectedCategory,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "LibraryTabTransition",
            modifier = Modifier.weight(1f)
        ) { category ->
            when (category) {
                "SONGS" -> {
                    if (effectiveSongs.isEmpty()) {
                        EmptyStateView(message = "No songs found")
                    } else {
                        val songsListState = rememberLazyListState()
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                state = songsListState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 120.dp)
                            ) {
                                items(effectiveSongs, key = { it.id }) { song ->
                                    SongListItem(
                                        song = song,
                                        isPlaying = isPlaying,
                                        isCurrentSong = currentPlayingSong?.id == song.id,
                                        onClick = { onSongClick(song, effectiveSongs) },
                                        onLikeToggle = { onLikeToggle(song) },
                                        onAddToPlaylist = { onAddToPlaylist(song) },
                                        onPlayNext = { onPlayNext(song) },
                                        onAddToQueue = { onAddToQueue(song) },
                                        onDownload = { onDownload(song) }
                                    )
                                }
                            }
                            ExpressiveScrollBar(
                                listState = songsListState,
                                modifier = Modifier.align(Alignment.CenterEnd),
                                dragLabelProvider = { index ->
                                    effectiveSongs.getOrNull(index)?.title?.firstOrNull()?.uppercase()
                                }
                            )
                        }
                    }
                }
                "FAVOURITE" -> {
                    if (effectiveLikedSongs.isEmpty()) {
                        EmptyStateView(message = "No favourite songs yet")
                    } else {
                        val favouriteListState = rememberLazyListState()
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                state = favouriteListState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 120.dp)
                            ) {
                                items(effectiveLikedSongs, key = { it.id }) { song ->
                                    SongListItem(
                                        song = song.copy(isLiked = true),
                                        isPlaying = isPlaying,
                                        isCurrentSong = currentPlayingSong?.id == song.id,
                                        onClick = { onSongClick(song, effectiveLikedSongs) },
                                        onLikeToggle = { onLikeToggle(song) },
                                        onAddToPlaylist = { onAddToPlaylist(song) },
                                        onPlayNext = { onPlayNext(song) },
                                        onAddToQueue = { onAddToQueue(song) },
                                        onDownload = { onDownload(song) }
                                    )
                                }
                            }
                            ExpressiveScrollBar(
                                listState = favouriteListState,
                                modifier = Modifier.align(Alignment.CenterEnd),
                                dragLabelProvider = { index ->
                                    effectiveLikedSongs.getOrNull(index)?.title?.firstOrNull()?.uppercase()
                                }
                            )
                        }
                    }
                }
                "ALBUMS" -> {
                    if (effectiveAlbums.isEmpty()) {
                        EmptyStateView(message = "No albums found")
                    } else {
                        val albumsGridState = rememberLazyGridState()
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyVerticalGrid(
                                state = albumsGridState,
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(effectiveAlbums) { album ->
                                    AlbumCard(
                                        album = album,
                                        onClick = { onSelectAlbum(album) },
                                        onPlay = {
                                            val albumTracks = effectiveSongs.filter { it.album == album.title }
                                            if (albumTracks.isNotEmpty()) onPlayAll(albumTracks, false)
                                        }
                                    )
                                }
                            }
                            ExpressiveScrollBar(
                                gridState = albumsGridState,
                                modifier = Modifier.align(Alignment.CenterEnd),
                                dragLabelProvider = { index ->
                                    effectiveAlbums.getOrNull(index)?.title?.firstOrNull()?.uppercase()
                                }
                            )
                        }
                    }
                }
                "ARTISTS" -> {
                    if (effectiveArtists.isEmpty()) {
                        EmptyStateView(message = "No artists found")
                    } else {
                        val artistsGridState = rememberLazyGridState()
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyVerticalGrid(
                                state = artistsGridState,
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(effectiveArtists) { artist ->
                                    ArtistCard(
                                        artist = artist,
                                        onClick = { onSelectArtist(artist) }
                                    )
                                }
                            }
                            ExpressiveScrollBar(
                                gridState = artistsGridState,
                                modifier = Modifier.align(Alignment.CenterEnd),
                                dragLabelProvider = { index ->
                                    effectiveArtists.getOrNull(index)?.name?.firstOrNull()?.uppercase()
                                }
                            )
                        }
                    }
                }
                "PLAYLISTS" -> {
                    val allPlaylists = remember(playlists, likedSongs, downloads) {
                        val builtIns = mutableListOf<Playlist>()
                        if (likedSongs.isNotEmpty()) {
                            builtIns.add(Playlist(id = "liked_songs", title = "Liked Songs", songCount = likedSongs.size, isEditable = false))
                        }
                        if (downloads.isNotEmpty()) {
                            builtIns.add(Playlist(id = "downloads", title = "Downloads", songCount = downloads.size, isEditable = false))
                        }
                        builtIns + playlists
                    }

                    if (allPlaylists.isEmpty()) {
                        EmptyStateView(message = "No playlists found")
                    } else {
                        val playlistsListState = rememberLazyListState()
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                state = playlistsListState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 120.dp)
                            ) {
                                items(allPlaylists) { playlist ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                        .clickable {
                                            if (playlist.id == "liked_songs") onOpenLikedSongs()
                                            else if (playlist.id == "downloads") onOpenDownloads()
                                            else onSelectPlaylist(playlist)
                                        }
                                        .testTag("playlist_item_${playlist.id}"),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(
                                                    if (playlist.id == "liked_songs") MaterialTheme.colorScheme.primaryContainer
                                                    else MaterialTheme.colorScheme.secondaryContainer
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (playlist.id == "liked_songs") Icons.Default.Favorite else Icons.Default.QueueMusic,
                                                contentDescription = null,
                                                tint = if (playlist.id == "liked_songs") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = playlist.title,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${playlist.songCount} songs • Playlist",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        if (playlist.isEditable) {
                                            IconButton(
                                                onClick = { onDeletePlaylist(playlist.id) },
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f), CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "Delete Playlist",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                }
                            }
                            ExpressiveScrollBar(
                                listState = playlistsListState,
                                modifier = Modifier.align(Alignment.CenterEnd),
                                dragLabelProvider = { index ->
                                    allPlaylists.getOrNull(index)?.title?.firstOrNull()?.uppercase()
                                }
                            )
                        }
                    }
                }
                "FOLDERS" -> {
                    if (folders.isEmpty()) {
                        // Fallback click into folder viewer or empty state
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clickable { onOpenFolders() },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Device Storage Folders",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Browse audio by directories",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        val foldersListState = rememberLazyListState()
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                state = foldersListState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 120.dp)
                            ) {
                            items(folders) { folder ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                        .clickable { onSelectFolder(folder) },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Folder,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = folder.name,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${folder.songCount} songs • ${folder.path}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                }
                            }
                            ExpressiveScrollBar(
                                listState = foldersListState,
                                modifier = Modifier.align(Alignment.CenterEnd),
                                dragLabelProvider = { index ->
                                    folders.getOrNull(index)?.name?.firstOrNull()?.uppercase()
                                }
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
                title = "Library",
                subtitle = "${effectiveSongs.size} tracks",
                collapseFraction = headerState.collapseFraction,
                headerHeight = headerState.currentHeaderHeight,
                showBackButton = false,
                onBackClick = {},
                actions = {
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
                            .testTag("library_settings_button")
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

@Composable
private fun EmptyStateView(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
