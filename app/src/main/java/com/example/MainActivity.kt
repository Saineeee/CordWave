package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.model.MediaFolder
import com.example.model.Song
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.any { it.value }
        if (granted) {
            viewModel.rescanLocalLibrary()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        checkAndRequestPermissions()

        setContent {
            val useDynamicColor by viewModel.useDynamicColor.collectAsState()

            MyApplicationTheme(
                darkTheme = true,
                dynamicColor = useDynamicColor
            ) {
                OuterTuneMainApp(viewModel = viewModel)
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OuterTuneMainApp(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isNowPlayingExpanded by viewModel.isNowPlayingExpanded.collectAsState()
    val showQueueSheet by viewModel.showQueueSheet.collectAsState()
    val showEqualizerDialog by viewModel.showEqualizerDialog.collectAsState()
    val showSleepTimerDialog by viewModel.showSleepTimerDialog.collectAsState()
    val songToAddToPlaylist by viewModel.songToAddToPlaylist.collectAsState()
    val showStatsScreen by viewModel.showStatsScreen.collectAsState()
    val showSettingsScreen by viewModel.showSettingsScreen.collectAsState()

    // Sub-screens
    val selectedAlbum by viewModel.selectedAlbum.collectAsState()
    val selectedArtist by viewModel.selectedArtist.collectAsState()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()

    // Controller states
    val currentSong by viewModel.playerController.currentSong.collectAsState()
    val isPlaying by viewModel.playerController.isPlaying.collectAsState()
    val currentPos by viewModel.playerController.currentPositionMs.collectAsState()
    val duration by viewModel.playerController.durationMs.collectAsState()
    val queue by viewModel.playerController.playbackQueue.collectAsState()
    val queueIndex by viewModel.playerController.currentQueueIndex.collectAsState()
    val isShuffle by viewModel.playerController.isShuffle.collectAsState()
    val repeatMode by viewModel.playerController.repeatMode.collectAsState()
    val audioEffects by viewModel.playerController.audioEffects.collectAsState()
    val sleepTimerSec by viewModel.playerController.sleepTimerRemainingSec.collectAsState()

    // Repository flows
    val allSongs by viewModel.repository.allSongs.collectAsState(initial = emptyList())
    val localSongs by viewModel.repository.localSongs.collectAsState()
    val trendingSongs by viewModel.repository.onlineTrendingSongs.collectAsState()
    val quickPicks by viewModel.repository.quickPicks.collectAsState()
    val moodPlaylists by viewModel.repository.moodPlaylists.collectAsState()
    val likedSongs by viewModel.repository.likedSongs.collectAsState(initial = emptyList())
    val customPlaylists by viewModel.repository.customPlaylists.collectAsState(initial = emptyList())
    val downloads by viewModel.repository.downloads.collectAsState()
    val recentHistory by viewModel.repository.recentHistory.collectAsState(initial = emptyList())
    val topPlayed by viewModel.repository.topPlayedSongs.collectAsState(initial = emptyList())

    // Synced lyrics
    val currentLyrics by viewModel.currentLyrics.collectAsState()
    val isLoadingLyrics by viewModel.isLoadingLyrics.collectAsState()

    // Search
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    val isOled by viewModel.isOledBlack.collectAsState()
    val accentIndex by viewModel.accentColorIndex.collectAsState()
    val useDynamicColor by viewModel.useDynamicColor.collectAsState()

    val progress = if (duration > 0) (currentPos.toFloat() / duration.toFloat()) else 0f
    val isAnySubScreenOpen = selectedAlbum != null || selectedArtist != null || selectedPlaylist != null || selectedFolder != null || showStatsScreen || showSettingsScreen

    val currentRoute = when {
        isAnySubScreenOpen -> ""
        currentTab == MainNavTab.HOME -> "home"
        currentTab == MainNavTab.SEARCH -> "search"
        else -> "library"
    }

    // System Back Button Handling
    val canGoBack = isNowPlayingExpanded || showStatsScreen || showSettingsScreen ||
            selectedAlbum != null || selectedArtist != null || selectedPlaylist != null ||
            selectedFolder != null || currentTab != MainNavTab.HOME

    BackHandler(enabled = canGoBack) {
        when {
            isNowPlayingExpanded -> viewModel.setNowPlayingExpanded(false)
            showStatsScreen -> viewModel.closeStats()
            showSettingsScreen -> viewModel.closeSettings()
            selectedAlbum != null -> viewModel.selectAlbum(null)
            selectedArtist != null -> viewModel.selectArtist(null)
            selectedPlaylist != null -> viewModel.selectPlaylist(null)
            selectedFolder != null -> viewModel.selectFolder(null)
            currentTab != MainNavTab.HOME -> viewModel.setTab(MainNavTab.HOME)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (!isNowPlayingExpanded) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Mini Player
                        if (currentSong != null) {
                            MiniPlayer(
                                song = currentSong,
                                isPlaying = isPlaying,
                                progress = progress,
                                onPlayPause = { viewModel.playerController.togglePlayPause() },
                                onSkipNext = { viewModel.playerController.skipToNext() },
                                onSkipPrevious = { viewModel.playerController.skipToPrevious() },
                                onClick = { viewModel.setNowPlayingExpanded(true) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                            )
                        }

                        // Floating Bottom Navigation Bar
                        FloatingBottomNav(
                            currentRoute = currentRoute,
                            onNavigate = { route ->
                                // Dismiss active sub-screens on main tab change
                                viewModel.selectAlbum(null)
                                viewModel.selectArtist(null)
                                viewModel.selectPlaylist(null)
                                viewModel.selectFolder(null)
                                viewModel.closeStats()
                                viewModel.closeSettings()

                                when (route) {
                                    "home" -> viewModel.setTab(MainNavTab.HOME)
                                    "search" -> viewModel.setTab(MainNavTab.SEARCH)
                                    "library" -> viewModel.setTab(MainNavTab.LIBRARY)
                                }
                            }
                        )
                    }
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when {
                    showStatsScreen -> {
                        StatsScreen(
                            topPlayedSongs = topPlayed,
                            onBack = { viewModel.closeStats() }
                        )
                    }
                    showSettingsScreen -> {
                        SettingsScreen(
                            isOledBlack = isOled,
                            useDynamicColor = useDynamicColor,
                            accentIndex = accentIndex,
                            onToggleOled = { viewModel.toggleOledBlack() },
                            onToggleDynamicColor = { viewModel.toggleDynamicColor() },
                            onSelectAccent = { viewModel.setAccentColor(it) },
                            onOpenEqualizer = { viewModel.setShowEqualizer(true) },
                            onRescanLibrary = { viewModel.rescanLocalLibrary() },
                            onBack = { viewModel.closeSettings() }
                        )
                    }
                    selectedAlbum != null -> {
                        val album = selectedAlbum!!
                        val albumSongs = allSongs.filter { it.album == album.title }
                        AlbumDetailScreen(
                            album = album,
                            songs = albumSongs,
                            currentPlayingSong = currentSong,
                            isPlaying = isPlaying,
                            onBack = { viewModel.selectAlbum(null) },
                            onPlayAll = { songs, shuffle -> viewModel.playAll(songs, shuffle) },
                            onSongClick = { song, list -> viewModel.playSong(song, list) },
                            onLikeToggle = { viewModel.toggleLike(it) },
                            onAddToPlaylist = { viewModel.setSongForPlaylist(it) },
                            onPlayNext = { viewModel.playerController.addToQueueNext(it) },
                            onAddToQueue = { viewModel.playerController.addToQueueEnd(it) },
                            onDownload = { viewModel.downloadSong(it) }
                        )
                    }
                    selectedArtist != null -> {
                        val artist = selectedArtist!!
                        val artistSongs = allSongs.filter { it.artist == artist.name }
                        ArtistDetailScreen(
                            artist = artist,
                            songs = artistSongs,
                            currentPlayingSong = currentSong,
                            isPlaying = isPlaying,
                            onBack = { viewModel.selectArtist(null) },
                            onPlayAll = { songs, shuffle -> viewModel.playAll(songs, shuffle) },
                            onSongClick = { song, list -> viewModel.playSong(song, list) },
                            onLikeToggle = { viewModel.toggleLike(it) },
                            onAddToPlaylist = { viewModel.setSongForPlaylist(it) },
                            onPlayNext = { viewModel.playerController.addToQueueNext(it) },
                            onAddToQueue = { viewModel.playerController.addToQueueEnd(it) },
                            onDownload = { viewModel.downloadSong(it) }
                        )
                    }
                    selectedPlaylist != null -> {
                        val playlist = selectedPlaylist!!
                        val playlistSongs by viewModel.repository.getSongsForPlaylist(playlist.id).collectAsState(initial = emptyList())
                        PlaylistDetailScreen(
                            playlist = playlist,
                            songs = playlistSongs,
                            currentPlayingSong = currentSong,
                            isPlaying = isPlaying,
                            onBack = { viewModel.selectPlaylist(null) },
                            onPlayAll = { songs, shuffle -> viewModel.playAll(songs, shuffle) },
                            onSongClick = { song, list -> viewModel.playSong(song, list) },
                            onLikeToggle = { viewModel.toggleLike(it) },
                            onRemoveSongFromPlaylist = { viewModel.removeSongFromPlaylist(playlist.id, it) },
                            onDeletePlaylist = { viewModel.deletePlaylist(playlist.id) },
                            onAddToPlaylist = { viewModel.setSongForPlaylist(it) },
                            onPlayNext = { viewModel.playerController.addToQueueNext(it) },
                            onAddToQueue = { viewModel.playerController.addToQueueEnd(it) },
                            onDownload = { viewModel.downloadSong(it) }
                        )
                    }
                    selectedFolder != null -> {
                        val folder = selectedFolder!!
                        FolderDetailScreen(
                            folder = folder,
                            currentPlayingSong = currentSong,
                            isPlaying = isPlaying,
                            onBack = { viewModel.selectFolder(null) },
                            onPlayAll = { songs, shuffle -> viewModel.playAll(songs, shuffle) },
                            onSongClick = { song, list -> viewModel.playSong(song, list) },
                            onLikeToggle = { viewModel.toggleLike(it) },
                            onAddToPlaylist = { viewModel.setSongForPlaylist(it) },
                            onPlayNext = { viewModel.playerController.addToQueueNext(it) },
                            onAddToQueue = { viewModel.playerController.addToQueueEnd(it) },
                            onDownload = { viewModel.downloadSong(it) }
                        )
                    }
                    else -> {
                        when (currentTab) {
                            MainNavTab.HOME -> {
                                HomeScreen(
                                    quickPicks = quickPicks,
                                    trendingSongs = trendingSongs,
                                    recentHistory = recentHistory,
                                    localSongs = localSongs,
                                    moodPlaylists = moodPlaylists,
                                    currentPlayingSong = currentSong,
                                    isPlaying = isPlaying,
                                    onSongClick = { song, list -> viewModel.playSong(song, list) },
                                    onPlayAll = { songs, shuffle -> viewModel.playAll(songs, shuffle) },
                                    onLikeToggle = { viewModel.toggleLike(it) },
                                    onAddToPlaylist = { viewModel.setSongForPlaylist(it) },
                                    onPlayNext = { viewModel.playerController.addToQueueNext(it) },
                                    onAddToQueue = { viewModel.playerController.addToQueueEnd(it) },
                                    onDownload = { viewModel.downloadSong(it) },
                                    onOpenStats = { viewModel.openStats() },
                                    onOpenSettings = { viewModel.openSettings() },
                                    onRescanLocal = { viewModel.rescanLocalLibrary() }
                                )
                            }
                            MainNavTab.SONGS -> {
                                SongsScreen(
                                    allSongs = allSongs,
                                    downloads = downloads,
                                    currentPlayingSong = currentSong,
                                    isPlaying = isPlaying,
                                    onSongClick = { song, list -> viewModel.playSong(song, list) },
                                    onPlayAll = { songs, shuffle -> viewModel.playAll(songs, shuffle) },
                                    onLikeToggle = { viewModel.toggleLike(it) },
                                    onAddToPlaylist = { viewModel.setSongForPlaylist(it) },
                                    onPlayNext = { viewModel.playerController.addToQueueNext(it) },
                                    onAddToQueue = { viewModel.playerController.addToQueueEnd(it) },
                                    onDownload = { viewModel.downloadSong(it) }
                                )
                            }
                            MainNavTab.LIBRARY -> {
                                LibraryScreen(
                                    likedSongs = likedSongs,
                                    downloads = downloads,
                                    recentHistory = recentHistory,
                                    playlists = customPlaylists,
                                    allSongs = allSongs,
                                    currentPlayingSong = currentSong,
                                    isPlaying = isPlaying,
                                    onSongClick = { song, list -> viewModel.playSong(song, list) },
                                    onPlayAll = { songs, shuffle -> viewModel.playAll(songs, shuffle) },
                                    onLikeToggle = { viewModel.toggleLike(it) },
                                    onAddToPlaylist = { viewModel.setSongForPlaylist(it) },
                                    onPlayNext = { viewModel.playerController.addToQueueNext(it) },
                                    onAddToQueue = { viewModel.playerController.addToQueueEnd(it) },
                                    onDownload = { viewModel.downloadSong(it) },
                                    onOpenLikedSongs = {
                                        viewModel.selectPlaylist(
                                            com.example.model.Playlist(
                                                id = "liked_songs",
                                                title = "Liked Songs",
                                                description = "Your favorite tracks",
                                                isEditable = false
                                            )
                                        )
                                    },
                                    onOpenDownloads = { viewModel.setTab(MainNavTab.SONGS) },
                                    onOpenHistory = { viewModel.openStats() },
                                    onOpenFolders = { viewModel.setTab(MainNavTab.FOLDERS) },
                                    onSelectPlaylist = { viewModel.selectPlaylist(it) },
                                    onCreatePlaylist = { viewModel.createPlaylist(it) },
                                    onDeletePlaylist = { viewModel.deletePlaylist(it) },
                                    onSelectAlbum = { viewModel.selectAlbum(it) },
                                    onSelectArtist = { viewModel.selectArtist(it) },
                                    onSelectFolder = { viewModel.selectFolder(it) },
                                    onOpenSettings = { viewModel.openSettings() }
                                )
                            }
                            MainNavTab.ALBUMS_ARTISTS -> {
                                AlbumsArtistsScreen(
                                    allSongs = allSongs,
                                    onSelectAlbum = { viewModel.selectAlbum(it) },
                                    onSelectArtist = { viewModel.selectArtist(it) },
                                    onPlayAlbum = { album ->
                                        val albumSongs = allSongs.filter { it.album == album.title }
                                        viewModel.playAll(albumSongs, false)
                                    }
                                )
                            }
                            MainNavTab.FOLDERS -> {
                                FoldersScreen(
                                    localSongs = localSongs,
                                    onSelectFolder = { viewModel.selectFolder(it) },
                                    onPlayFolder = { folder ->
                                        viewModel.playAll(folder.songs, false)
                                    }
                                )
                            }
                            MainNavTab.SEARCH -> {
                                SearchScreen(
                                    query = searchQuery,
                                    searchResults = searchResults,
                                    isSearching = isSearching,
                                    currentPlayingSong = currentSong,
                                    isPlaying = isPlaying,
                                    onQueryChange = { viewModel.search(it) },
                                    onSongClick = { song, list -> viewModel.playSong(song, list) },
                                    onSelectAlbum = { viewModel.selectAlbum(it) },
                                    onSelectArtist = { viewModel.selectArtist(it) },
                                    onLikeToggle = { viewModel.toggleLike(it) },
                                    onAddToPlaylist = { viewModel.setSongForPlaylist(it) },
                                    onPlayNext = { viewModel.playerController.addToQueueNext(it) },
                                    onAddToQueue = { viewModel.playerController.addToQueueEnd(it) },
                                    onDownload = { viewModel.downloadSong(it) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Full Now Playing overlay screen
        AnimatedVisibility(
            visible = isNowPlayingExpanded && currentSong != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            NowPlayingScreen(
                song = currentSong,
                isPlaying = isPlaying,
                currentPositionMs = currentPos,
                durationMs = duration,
                isShuffle = isShuffle,
                repeatMode = repeatMode,
                lyrics = currentLyrics,
                isLoadingLyrics = isLoadingLyrics,
                onRefreshLyrics = { viewModel.refreshLyrics() },
                onCollapse = { viewModel.setNowPlayingExpanded(false) },
                onPlayPause = { viewModel.playerController.togglePlayPause() },
                onSeekTo = { viewModel.playerController.seekTo(it) },
                onSkipNext = { viewModel.playerController.skipToNext() },
                onSkipPrevious = { viewModel.playerController.skipToPrevious() },
                onToggleShuffle = { viewModel.playerController.toggleShuffle() },
                onToggleRepeat = { viewModel.playerController.toggleRepeat() },
                onToggleLike = { currentSong?.let { viewModel.toggleLike(it) } },
                onOpenEqualizer = { viewModel.setShowEqualizer(true) },
                onOpenSleepTimer = { viewModel.setShowSleepTimer(true) },
                onOpenQueue = { viewModel.setShowQueueSheet(true) },
                onDownload = { currentSong?.let { viewModel.downloadSong(it) } },
                onAddToPlaylist = { currentSong?.let { viewModel.setSongForPlaylist(it) } }
            )
        }
    }

    // Modal Sheets & Dialogs
    if (showQueueSheet) {
        QueueBottomSheet(
            queue = queue,
            currentIndex = queueIndex,
            onSongClick = { viewModel.playerController.playQueueIndex(it) },
            onRemoveSong = { viewModel.playerController.removeFromQueue(it) },
            onMoveUp = { if (it > 0) viewModel.playerController.reorderQueue(it, it - 1) },
            onMoveDown = { if (it < queue.size - 1) viewModel.playerController.reorderQueue(it, it + 1) },
            onClearQueue = { viewModel.playerController.clearQueue() },
            onSaveAsPlaylist = {
                viewModel.createPlaylist("Queue Playlist ${System.currentTimeMillis() % 10000}")
                viewModel.setShowQueueSheet(false)
            },
            onDismiss = { viewModel.setShowQueueSheet(false) }
        )
    }

    if (showEqualizerDialog) {
        EqualizerDialog(
            config = audioEffects,
            onConfigChange = { viewModel.playerController.setAudioEffects(it) },
            onDismiss = { viewModel.setShowEqualizer(false) }
        )
    }

    if (showSleepTimerDialog) {
        SleepTimerDialog(
            activeRemainingSec = sleepTimerSec,
            onSetTimer = { viewModel.playerController.startSleepTimer(it) },
            onCancelTimer = { viewModel.playerController.cancelSleepTimer() },
            onDismiss = { viewModel.setShowSleepTimer(false) }
        )
    }

    if (songToAddToPlaylist != null) {
        AddToPlaylistDialog(
            song = songToAddToPlaylist,
            playlists = customPlaylists,
            onAddToPlaylist = { playlistId ->
                songToAddToPlaylist?.let { song ->
                    viewModel.addSongToPlaylist(playlistId, song)
                }
            },
            onCreatePlaylist = { title ->
                viewModel.createPlaylist(title)
            },
            onDismiss = { viewModel.setSongForPlaylist(null) }
        )
    }
}

