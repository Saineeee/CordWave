package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.OuterTuneDatabase
import com.example.data.repository.MusicRepository
import com.example.model.*
import com.example.playback.MusicPlayerController
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class MainNavTab {
    HOME,
    SONGS,
    LIBRARY,
    ALBUMS_ARTISTS,
    FOLDERS,
    SEARCH
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = OuterTuneDatabase.getInstance(application)
    val repository = MusicRepository(application, database, viewModelScope)
    val playerController = MusicPlayerController(application, repository, viewModelScope)

    // Navigation state
    private val _currentTab = MutableStateFlow(MainNavTab.HOME)
    val currentTab: StateFlow<MainNavTab> = _currentTab.asStateFlow()

    // Sub-navigation / details
    private val _selectedAlbum = MutableStateFlow<Album?>(null)
    val selectedAlbum: StateFlow<Album?> = _selectedAlbum.asStateFlow()

    private val _selectedArtist = MutableStateFlow<Artist?>(null)
    val selectedArtist: StateFlow<Artist?> = _selectedArtist.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    private val _selectedFolder = MutableStateFlow<MediaFolder?>(null)
    val selectedFolder: StateFlow<MediaFolder?> = _selectedFolder.asStateFlow()

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow(SearchResult())
    val searchResults: StateFlow<SearchResult> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Dialogs & Sheets
    private val _isNowPlayingExpanded = MutableStateFlow(false)
    val isNowPlayingExpanded: StateFlow<Boolean> = _isNowPlayingExpanded.asStateFlow()

    private val _showQueueSheet = MutableStateFlow(false)
    val showQueueSheet: StateFlow<Boolean> = _showQueueSheet.asStateFlow()

    private val _showLyrics = MutableStateFlow(false)
    val showLyrics: StateFlow<Boolean> = _showLyrics.asStateFlow()

    private val _showEqualizerDialog = MutableStateFlow(false)
    val showEqualizerDialog: StateFlow<Boolean> = _showEqualizerDialog.asStateFlow()

    private val _showSleepTimerDialog = MutableStateFlow(false)
    val showSleepTimerDialog: StateFlow<Boolean> = _showSleepTimerDialog.asStateFlow()

    private val _songToAddToPlaylist = MutableStateFlow<Song?>(null)
    val songToAddToPlaylist: StateFlow<Song?> = _songToAddToPlaylist.asStateFlow()

    private val _showStatsScreen = MutableStateFlow(false)
    val showStatsScreen: StateFlow<Boolean> = _showStatsScreen.asStateFlow()

    private val _showSettingsScreen = MutableStateFlow(false)
    val showSettingsScreen: StateFlow<Boolean> = _showSettingsScreen.asStateFlow()

    // Synced lyrics state
    private val _currentLyrics = MutableStateFlow<Lyrics?>(null)
    val currentLyrics: StateFlow<Lyrics?> = _currentLyrics.asStateFlow()

    private val _isLoadingLyrics = MutableStateFlow(false)
    val isLoadingLyrics: StateFlow<Boolean> = _isLoadingLyrics.asStateFlow()

    // Theme & Options
    private val _isOledBlack = MutableStateFlow(false)
    val isOledBlack: StateFlow<Boolean> = _isOledBlack.asStateFlow()

    private val _useDynamicColor = MutableStateFlow(true)
    val useDynamicColor: StateFlow<Boolean> = _useDynamicColor.asStateFlow()

    private val _accentColorIndex = MutableStateFlow(0)
    val accentColorIndex: StateFlow<Int> = _accentColorIndex.asStateFlow()

    init {
        // Observe current song changes to auto fetch synced lyrics
        viewModelScope.launch {
            playerController.currentSong.collect { song ->
                if (song != null) {
                    loadLyricsForSong(song)
                } else {
                    _currentLyrics.value = null
                }
            }
        }
    }

    fun setTab(tab: MainNavTab) {
        _currentTab.value = tab
        // Clear sub-details on main tab switch
        _selectedAlbum.value = null
        _selectedArtist.value = null
        _selectedPlaylist.value = null
        _selectedFolder.value = null
        _showStatsScreen.value = false
        _showSettingsScreen.value = false
    }

    fun selectAlbum(album: Album?) {
        _selectedAlbum.value = album
    }

    fun selectArtist(artist: Artist?) {
        _selectedArtist.value = artist
    }

    fun selectPlaylist(playlist: Playlist?) {
        _selectedPlaylist.value = playlist
    }

    fun selectFolder(folder: MediaFolder?) {
        _selectedFolder.value = folder
    }

    fun openStats() {
        _showStatsScreen.value = true
    }

    fun closeStats() {
        _showStatsScreen.value = false
    }

    fun openSettings() {
        _showSettingsScreen.value = true
    }

    fun closeSettings() {
        _showSettingsScreen.value = false
    }

    fun setNowPlayingExpanded(expanded: Boolean) {
        _isNowPlayingExpanded.value = expanded
    }

    fun setShowQueueSheet(show: Boolean) {
        _showQueueSheet.value = show
    }

    fun setShowLyrics(show: Boolean) {
        _showLyrics.value = show
    }

    fun setShowEqualizer(show: Boolean) {
        _showEqualizerDialog.value = show
    }

    fun setShowSleepTimer(show: Boolean) {
        _showSleepTimerDialog.value = show
    }

    fun setSongForPlaylist(song: Song?) {
        _songToAddToPlaylist.value = song
    }

    fun toggleOledBlack() {
        _isOledBlack.value = !_isOledBlack.value
    }

    fun toggleDynamicColor() {
        _useDynamicColor.value = !_useDynamicColor.value
    }

    fun setDynamicColor(enabled: Boolean) {
        _useDynamicColor.value = enabled
    }

    fun setAccentColor(index: Int) {
        _accentColorIndex.value = index
    }

    fun search(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = SearchResult()
            _isSearching.value = false
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            val results = repository.search(query)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    fun playSong(song: Song, queue: List<Song> = emptyList()) {
        playerController.playSong(song, queue)
    }

    fun playAll(songs: List<Song>, shuffle: Boolean = false) {
        if (songs.isEmpty()) return
        val list = if (shuffle) songs.shuffled() else songs
        playerController.playSong(list.first(), list)
    }

    fun toggleLike(song: Song) {
        viewModelScope.launch {
            val liked = repository.toggleLike(song)
            // If the current playing song is the one being toggled, update it
            if (playerController.currentSong.value?.id == song.id) {
                // Controller will stay in sync via reactive flow
            }
        }
    }

    fun createPlaylist(title: String, description: String = "") {
        viewModelScope.launch {
            repository.createPlaylist(title, description)
        }
    }

    fun addSongToPlaylist(playlistId: String, song: Song) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, song)
            _songToAddToPlaylist.value = null
        }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            if (_selectedPlaylist.value?.id == playlistId) {
                _selectedPlaylist.value = null
            }
        }
    }

    fun downloadSong(song: Song) {
        viewModelScope.launch {
            repository.startDownload(song)
        }
    }

    fun removeDownload(songId: String) {
        viewModelScope.launch {
            repository.removeDownload(songId)
        }
    }

    fun rescanLocalLibrary() {
        viewModelScope.launch {
            repository.refreshLocalMedia()
        }
    }

    fun refreshLyrics(song: Song? = null) {
        val target = song ?: playerController.currentSong.value ?: return
        loadLyricsForSong(target)
    }

    private fun loadLyricsForSong(song: Song) {
        viewModelScope.launch {
            _isLoadingLyrics.value = true
            val lyrics = repository.fetchLyrics(song)
            _currentLyrics.value = lyrics
            _isLoadingLyrics.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerController.release()
    }
}
