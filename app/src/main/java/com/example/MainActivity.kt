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
import com.example.model.Album
import com.example.model.Artist
import com.example.model.MediaFolder
import com.example.model.Playlist
import com.example.model.Song
import com.example.presentation.viewmodel.SettingsViewModel
import com.example.ui.animation.enterTransition
import com.example.ui.animation.exitTransition
import com.example.ui.animation.popEnterTransition
import com.example.ui.animation.popExitTransition
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

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
            val settingsUiState by settingsViewModel.uiState.collectAsState()
            val isDark = when (settingsUiState.appThemeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> true
            }

            MyApplicationTheme(
                darkTheme = isDark,
                dynamicColor = settingsUiState.dynamicColor,
                isOled = settingsUiState.isOled,
                accentIndex = settingsUiState.accentIndex
            ) {
                OuterTuneMainApp(
                    viewModel = viewModel,
                    settingsViewModel = settingsViewModel
                )
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

private sealed interface AppScreen {
    val depth: Int

    data object Home : AppScreen { override val depth = 0 }
    data object Search : AppScreen { override val depth = 0 }
    data object Library : AppScreen { override val depth = 0 }
    data object Songs : AppScreen { override val depth = 1 }
    data object AlbumsArtists : AppScreen { override val depth = 1 }
    data object Folders : AppScreen { override val depth = 1 }
    data object Stats : AppScreen { override val depth = 2 }
    data object Settings : AppScreen { override val depth = 2 }
    data class SettingsCategory(val categoryId: String) : AppScreen { override val depth = 3 }
    data object PaletteStyle : AppScreen { override val depth = 4 }
    data object ArtistSettings : AppScreen { override val depth = 4 }
    data object Accounts : AppScreen { override val depth = 4 }
    data object DeviceCapabilities : AppScreen { override val depth = 4 }
    data object About : AppScreen { override val depth = 4 }
    data class AlbumDetail(val album: Album) : AppScreen { override val depth = 2 }
    data class ArtistDetail(val artist: Artist) : AppScreen { override val depth = 2 }
    data class PlaylistDetail(val playlist: Playlist) : AppScreen { override val depth = 2 }
    data class FolderDetail(val folder: MediaFolder) : AppScreen { override val depth = 2 }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OuterTuneMainApp(
    viewModel: MainViewModel,
    settingsViewModel: SettingsViewModel? = null
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isNowPlayingExpanded by viewModel.isNowPlayingExpanded.collectAsState()
    val showQueueSheet by viewModel.showQueueSheet.collectAsState()
    val showEqualizerDialog by viewModel.showEqualizerDialog.collectAsState()
    val showSleepTimerDialog by viewModel.showSleepTimerDialog.collectAsState()
    val songToAddToPlaylist by viewModel.songToAddToPlaylist.collectAsState()
    val showStatsScreen by viewModel.showStatsScreen.collectAsState()
    val showSettingsScreen by viewModel.showSettingsScreen.collectAsState()

    var settingsSubRoute by remember { mutableStateOf<String?>(null) }

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

    val currentScreen: AppScreen = when {
        showStatsScreen -> AppScreen.Stats
        showSettingsScreen -> {
            when {
                settingsSubRoute == null -> AppScreen.Settings
                settingsSubRoute?.startsWith("settings_category/") == true -> {
                    AppScreen.SettingsCategory(settingsSubRoute!!.removePrefix("settings_category/"))
                }
                settingsSubRoute?.startsWith("playlist_detail/") == true -> {
                    val playlistId = settingsSubRoute!!.removePrefix("playlist_detail/")
                    val playlist = customPlaylists.firstOrNull { it.id == playlistId }
                    if (playlist != null) AppScreen.PlaylistDetail(playlist) else AppScreen.Settings
                }
                settingsSubRoute == "about" -> AppScreen.About
                settingsSubRoute == "palette_style" -> AppScreen.PaletteStyle
                settingsSubRoute == "artist_settings" -> AppScreen.ArtistSettings
                settingsSubRoute == "accounts" -> AppScreen.Accounts
                settingsSubRoute == "device_capabilities" -> AppScreen.DeviceCapabilities
                else -> AppScreen.Settings
            }
        }
        selectedAlbum != null -> AppScreen.AlbumDetail(selectedAlbum!!)
        selectedArtist != null -> AppScreen.ArtistDetail(selectedArtist!!)
        selectedPlaylist != null -> AppScreen.PlaylistDetail(selectedPlaylist!!)
        selectedFolder != null -> AppScreen.FolderDetail(selectedFolder!!)
        else -> {
            when (currentTab) {
                MainNavTab.HOME -> AppScreen.Home
                MainNavTab.SONGS -> AppScreen.Songs
                MainNavTab.LIBRARY -> AppScreen.Library
                MainNavTab.ALBUMS_ARTISTS -> AppScreen.AlbumsArtists
                MainNavTab.FOLDERS -> AppScreen.Folders
                MainNavTab.SEARCH -> AppScreen.Search
            }
        }
    }

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
            showSettingsScreen -> {
                if (settingsSubRoute != null) {
                    if (settingsSubRoute == "palette_style") {
                        settingsSubRoute = "settings_category/appearance"
                    } else if (settingsSubRoute == "artist_settings") {
                        settingsSubRoute = "settings_category/library"
                    } else {
                        settingsSubRoute = null
                    }
                } else {
                    viewModel.closeSettings()
                }
            }
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
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    if (targetState.depth >= initialState.depth) {
                        enterTransition() togetherWith exitTransition()
                    } else {
                        popEnterTransition() togetherWith popExitTransition()
                    }
                },
                label = "AppNavigationTransition",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { targetScreen ->
                when (targetScreen) {
                    is AppScreen.Stats -> {
                        StatsScreen(
                            topPlayedSongs = topPlayed,
                            onBack = { viewModel.closeStats() }
                        )
                    }
                    is AppScreen.Settings -> {
                        SettingsScreen(
                            onNavigateToCategory = { categoryId ->
                                settingsSubRoute = "settings_category/$categoryId"
                            },
                            onOpenEqualizer = { viewModel.setShowEqualizer(true) },
                            onOpenDeviceCapabilities = { settingsSubRoute = "device_capabilities" },
                            onOpenAccounts = { settingsSubRoute = "accounts" },
                            onOpenAbout = { settingsSubRoute = "about" },
                            onBack = { viewModel.closeSettings() }
                        )
                    }
                    is AppScreen.SettingsCategory -> {
                        SettingsCategoryScreen(
                            categoryId = targetScreen.categoryId,
                            onBackClick = { settingsSubRoute = null },
                            settingsViewModel = settingsViewModel,
                            onOpenEqualizer = { viewModel.setShowEqualizer(true) },
                            onOpenSleepTimer = { viewModel.setShowSleepTimer(true) },
                            onNavigateToRoute = { route -> settingsSubRoute = route }
                        )
                    }
                    is AppScreen.About -> {
                        AboutScreen(onBack = { settingsSubRoute = null })
                    }
                    is AppScreen.PaletteStyle -> {
                        PaletteStyleScreen(onBack = { settingsSubRoute = "settings_category/appearance" })
                    }
                    is AppScreen.ArtistSettings -> {
                        ArtistSettingsScreen(onBack = { settingsSubRoute = "settings_category/library" })
                    }
                    is AppScreen.Accounts -> {
                        AccountsScreen(onBack = { settingsSubRoute = null })
                    }
                    is AppScreen.DeviceCapabilities -> {
                        DeviceCapabilitiesScreen(onBack = { settingsSubRoute = null })
                    }
                    is AppScreen.AlbumDetail -> {
                        val album = targetScreen.album
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
                    is AppScreen.ArtistDetail -> {
                        val artist = targetScreen.artist
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
                    is AppScreen.PlaylistDetail -> {
                        val playlist = targetScreen.playlist
                        val playlistSongs by viewModel.repository.getSongsForPlaylist(playlist.id).collectAsState(initial = emptyList())
                        PlaylistDetailScreen(
                            playlist = playlist,
                            songs = playlistSongs,
                            currentPlayingSong = currentSong,
                            isPlaying = isPlaying,
                            onBack = {
                                if (settingsSubRoute != null) {
                                    settingsSubRoute = null
                                }
                                viewModel.selectPlaylist(null)
                            },
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
                    is AppScreen.FolderDetail -> {
                        val folder = targetScreen.folder
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
                    is AppScreen.Home -> {
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
                    is AppScreen.Songs -> {
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
                    is AppScreen.Library -> {
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
                    is AppScreen.AlbumsArtists -> {
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
                    is AppScreen.Folders -> {
                        FoldersScreen(
                            localSongs = localSongs,
                            onSelectFolder = { viewModel.selectFolder(it) },
                            onPlayFolder = { folder ->
                                viewModel.playAll(folder.songs, false)
                            }
                        )
                    }
                    is AppScreen.Search -> {
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

        // Full Now Playing overlay screen
        AnimatedVisibility(
            visible = isNowPlayingExpanded && currentSong != null,
            enter = enterTransition(),
            exit = popExitTransition(),
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

