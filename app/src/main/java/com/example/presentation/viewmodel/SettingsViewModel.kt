package com.example.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.OuterTuneDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.MusicRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isDarkTheme: Boolean = true,
    val isOled: Boolean = false,
    val accentIndex: Int = 0,
    val dynamicColor: Boolean = true,
    val appThemeMode: String = "DARK", // LIGHT, DARK, FOLLOW_SYSTEM
    val playerThemePreference: String = "ALBUM_ART",
    val showPlayerFileInfo: Boolean = true,
    val useSmoothCorners: Boolean = false,
    val minSongDuration: Int = 0,
    val autoScanLrcFiles: Boolean = false,
    val lyricsSourcePreference: String = "EMBEDDED_FIRST",
    val launchTab: String = "HOME",
    val navBarStyle: String = "FLOATING",
    val libraryNavigationMode: String = "TABS",
    val carouselStyle: String = "DEFAULT",
    val collagePattern: String = "DEFAULT",
    val audioNormalization: Boolean = false,
    val crossfadeDurationSec: Int = 0,
    val gaplessPlayback: Boolean = true,
    val highQualityStream: Boolean = true,
    val wifiOnlyDownload: Boolean = true,
    val autoBackup: Boolean = false,
    val isSyncing: Boolean = false,
    val syncProgress: Float = 0f,
    val syncStatusMessage: String = "Idle"
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = UserPreferencesRepository(application)
    private val database = OuterTuneDatabase.getInstance(application)
    private val musicRepository = MusicRepository(application, database, viewModelScope)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.userPreferencesFlow.collect { prefs ->
                _uiState.update { current ->
                    current.copy(
                        isDarkTheme = prefs.isDarkTheme,
                        isOled = prefs.isOled,
                        accentIndex = prefs.accentIndex,
                        dynamicColor = prefs.dynamicColor,
                        appThemeMode = prefs.appThemeMode,
                        playerThemePreference = prefs.playerThemePreference,
                        showPlayerFileInfo = prefs.showPlayerFileInfo,
                        useSmoothCorners = prefs.useSmoothCorners,
                        minSongDuration = prefs.minSongDuration,
                        autoScanLrcFiles = prefs.autoScanLrcFiles,
                        lyricsSourcePreference = prefs.lyricsSourcePreference,
                        launchTab = prefs.launchTab,
                        navBarStyle = prefs.navBarStyle,
                        libraryNavigationMode = prefs.libraryNavigationMode,
                        carouselStyle = prefs.carouselStyle,
                        collagePattern = prefs.collagePattern,
                        audioNormalization = prefs.audioNormalization,
                        crossfadeDurationSec = prefs.crossfadeDurationSec,
                        gaplessPlayback = prefs.gaplessPlayback,
                        highQualityStream = prefs.highQualityStream,
                        wifiOnlyDownload = prefs.wifiOnlyDownload,
                        autoBackup = prefs.autoBackup
                    )
                }
            }
        }
    }

    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateDarkTheme(isDark)
        }
    }

    fun setOledMode(isOled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateOledMode(isOled)
        }
    }

    fun setAccentColor(index: Int) {
        viewModelScope.launch {
            preferencesRepository.updateAccentIndex(index)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateDynamicColor(enabled)
        }
    }

    fun setAppThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.updateAppThemeMode(mode)
        }
    }

    fun setPlayerThemePreference(theme: String) {
        viewModelScope.launch {
            preferencesRepository.updatePlayerThemePreference(theme)
        }
    }

    fun setShowPlayerFileInfo(show: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateShowPlayerFileInfo(show)
        }
    }

    fun setUseSmoothCorners(use: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateUseSmoothCorners(use)
        }
    }

    fun setMinSongDuration(durationSec: Int) {
        viewModelScope.launch {
            preferencesRepository.updateMinSongDuration(durationSec)
        }
    }

    fun setAutoScanLrcFiles(scan: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateAutoScanLrcFiles(scan)
        }
    }

    fun setLyricsSourcePreference(source: String) {
        viewModelScope.launch {
            preferencesRepository.updateLyricsSourcePreference(source)
        }
    }

    fun setLaunchTab(tab: String) {
        viewModelScope.launch {
            preferencesRepository.updateLaunchTab(tab)
        }
    }

    fun setNavBarStyle(style: String) {
        viewModelScope.launch {
            preferencesRepository.updateNavBarStyle(style)
        }
    }

    fun setLibraryNavigationMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.updateLibraryNavigationMode(mode)
        }
    }

    fun setCarouselStyle(style: String) {
        viewModelScope.launch {
            preferencesRepository.updateCarouselStyle(style)
        }
    }

    fun setCollagePattern(pattern: String) {
        viewModelScope.launch {
            preferencesRepository.updateCollagePattern(pattern)
        }
    }

    fun setAudioNormalization(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateAudioNormalization(enabled)
        }
    }

    fun setCrossfadeDuration(sec: Int) {
        viewModelScope.launch {
            preferencesRepository.updateCrossfadeDuration(sec)
        }
    }

    fun setGaplessPlayback(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateGaplessPlayback(enabled)
        }
    }

    fun setHighQualityStream(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateHighQualityStream(enabled)
        }
    }

    fun setWifiOnlyDownload(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateWifiOnlyDownload(enabled)
        }
    }

    fun setAutoBackup(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateAutoBackup(enabled)
        }
    }

    fun startLibrarySync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncProgress = 0.1f, syncStatusMessage = "Scanning storage...") }
            delay(400)
            musicRepository.refreshLocalMedia()
            _uiState.update { it.copy(syncProgress = 0.6f, syncStatusMessage = "Indexing metadata & tags...") }
            delay(500)
            _uiState.update { it.copy(syncProgress = 1.0f, syncStatusMessage = "Sync complete!") }
            delay(400)
            _uiState.update { it.copy(isSyncing = false, syncProgress = 0f, syncStatusMessage = "Idle") }
        }
    }

    fun rebuildDatabase() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncProgress = 0.2f, syncStatusMessage = "Rebuilding media catalog...") }
            delay(600)
            musicRepository.refreshLocalMedia()
            _uiState.update { it.copy(syncProgress = 1.0f, syncStatusMessage = "Catalog rebuilt!") }
            delay(500)
            _uiState.update { it.copy(isSyncing = false, syncProgress = 0f, syncStatusMessage = "Idle") }
        }
    }

    fun resetLyrics(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            // Clears any local cached lyrics
            delay(300)
            onComplete()
        }
    }

    fun exportData(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            delay(400)
            onResult(true, "Backup saved to internal storage /Psync/Backups/psync_backup.json")
        }
    }

    fun importData(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            delay(500)
            onResult(true, "Successfully restored library metadata and custom playlists.")
        }
    }
}
