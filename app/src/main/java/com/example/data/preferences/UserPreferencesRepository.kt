package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "psync_preferences")

data class UserPreferences(
    val isDarkTheme: Boolean = true,
    val isOled: Boolean = false,
    val accentIndex: Int = 0,
    val dynamicColor: Boolean = true,
    val appThemeMode: String = "DARK",
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
    val autoBackup: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val IS_OLED = booleanPreferencesKey("is_oled")
        val ACCENT_INDEX = intPreferencesKey("accent_index")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val APP_THEME_MODE = stringPreferencesKey("app_theme_mode")
        val PLAYER_THEME_PREFERENCE = stringPreferencesKey("player_theme_preference")
        val SHOW_PLAYER_FILE_INFO = booleanPreferencesKey("show_player_file_info")
        val USE_SMOOTH_CORNERS = booleanPreferencesKey("use_smooth_corners")
        val MIN_SONG_DURATION = intPreferencesKey("min_song_duration")
        val AUTO_SCAN_LRC_FILES = booleanPreferencesKey("auto_scan_lrc_files")
        val LYRICS_SOURCE_PREFERENCE = stringPreferencesKey("lyrics_source_preference")
        val LAUNCH_TAB = stringPreferencesKey("launch_tab")
        val NAV_BAR_STYLE = stringPreferencesKey("nav_bar_style")
        val LIBRARY_NAVIGATION_MODE = stringPreferencesKey("library_navigation_mode")
        val CAROUSEL_STYLE = stringPreferencesKey("carousel_style")
        val COLLAGE_PATTERN = stringPreferencesKey("collage_pattern")
        val AUDIO_NORMALIZATION = booleanPreferencesKey("audio_normalization")
        val CROSSFADE_DURATION_SEC = intPreferencesKey("crossfade_duration_sec")
        val GAPLESS_PLAYBACK = booleanPreferencesKey("gapless_playback")
        val HIGH_QUALITY_STREAM = booleanPreferencesKey("high_quality_stream")
        val WIFI_ONLY_DOWNLOAD = booleanPreferencesKey("wifi_only_download")
        val AUTO_BACKUP = booleanPreferencesKey("auto_backup")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                isDarkTheme = preferences[PreferencesKeys.IS_DARK_THEME] ?: true,
                isOled = preferences[PreferencesKeys.IS_OLED] ?: false,
                accentIndex = preferences[PreferencesKeys.ACCENT_INDEX] ?: 0,
                dynamicColor = preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true,
                appThemeMode = preferences[PreferencesKeys.APP_THEME_MODE] ?: "DARK",
                playerThemePreference = preferences[PreferencesKeys.PLAYER_THEME_PREFERENCE] ?: "ALBUM_ART",
                showPlayerFileInfo = preferences[PreferencesKeys.SHOW_PLAYER_FILE_INFO] ?: true,
                useSmoothCorners = preferences[PreferencesKeys.USE_SMOOTH_CORNERS] ?: false,
                minSongDuration = preferences[PreferencesKeys.MIN_SONG_DURATION] ?: 0,
                autoScanLrcFiles = preferences[PreferencesKeys.AUTO_SCAN_LRC_FILES] ?: false,
                lyricsSourcePreference = preferences[PreferencesKeys.LYRICS_SOURCE_PREFERENCE] ?: "EMBEDDED_FIRST",
                launchTab = preferences[PreferencesKeys.LAUNCH_TAB] ?: "HOME",
                navBarStyle = preferences[PreferencesKeys.NAV_BAR_STYLE] ?: "FLOATING",
                libraryNavigationMode = preferences[PreferencesKeys.LIBRARY_NAVIGATION_MODE] ?: "TABS",
                carouselStyle = preferences[PreferencesKeys.CAROUSEL_STYLE] ?: "DEFAULT",
                collagePattern = preferences[PreferencesKeys.COLLAGE_PATTERN] ?: "DEFAULT",
                audioNormalization = preferences[PreferencesKeys.AUDIO_NORMALIZATION] ?: false,
                crossfadeDurationSec = preferences[PreferencesKeys.CROSSFADE_DURATION_SEC] ?: 0,
                gaplessPlayback = preferences[PreferencesKeys.GAPLESS_PLAYBACK] ?: true,
                highQualityStream = preferences[PreferencesKeys.HIGH_QUALITY_STREAM] ?: true,
                wifiOnlyDownload = preferences[PreferencesKeys.WIFI_ONLY_DOWNLOAD] ?: true,
                autoBackup = preferences[PreferencesKeys.AUTO_BACKUP] ?: false
            )
        }

    suspend fun updateDarkTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_THEME] = isDark
        }
    }

    suspend fun updateOledMode(isOled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_OLED] = isOled
        }
    }

    suspend fun updateAccentIndex(accentIndex: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCENT_INDEX] = accentIndex
        }
    }

    suspend fun updateDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun updateAppThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_THEME_MODE] = mode
            preferences[PreferencesKeys.IS_DARK_THEME] = (mode != "LIGHT")
        }
    }

    suspend fun updatePlayerThemePreference(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PLAYER_THEME_PREFERENCE] = theme
        }
    }

    suspend fun updateShowPlayerFileInfo(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_PLAYER_FILE_INFO] = show
        }
    }

    suspend fun updateUseSmoothCorners(use: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_SMOOTH_CORNERS] = use
        }
    }

    suspend fun updateMinSongDuration(durationSec: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MIN_SONG_DURATION] = durationSec
        }
    }

    suspend fun updateAutoScanLrcFiles(scan: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SCAN_LRC_FILES] = scan
        }
    }

    suspend fun updateLyricsSourcePreference(source: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LYRICS_SOURCE_PREFERENCE] = source
        }
    }

    suspend fun updateLaunchTab(tab: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAUNCH_TAB] = tab
        }
    }

    suspend fun updateNavBarStyle(style: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NAV_BAR_STYLE] = style
        }
    }

    suspend fun updateLibraryNavigationMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LIBRARY_NAVIGATION_MODE] = mode
        }
    }

    suspend fun updateCarouselStyle(style: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CAROUSEL_STYLE] = style
        }
    }

    suspend fun updateCollagePattern(pattern: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COLLAGE_PATTERN] = pattern
        }
    }

    suspend fun updateAudioNormalization(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUDIO_NORMALIZATION] = enabled
        }
    }

    suspend fun updateCrossfadeDuration(sec: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CROSSFADE_DURATION_SEC] = sec
        }
    }

    suspend fun updateGaplessPlayback(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GAPLESS_PLAYBACK] = enabled
        }
    }

    suspend fun updateHighQualityStream(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIGH_QUALITY_STREAM] = enabled
        }
    }

    suspend fun updateWifiOnlyDownload(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WIFI_ONLY_DOWNLOAD] = enabled
        }
    }

    suspend fun updateAutoBackup(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_BACKUP] = enabled
        }
    }
}
