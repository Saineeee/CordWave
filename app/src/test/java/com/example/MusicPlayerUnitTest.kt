package com.example

import com.example.data.lyrics.LyricsParser
import com.example.model.*
import com.example.ui.components.formatDuration
import org.junit.Assert.*
import org.junit.Test

class MusicPlayerUnitTest {

    @Test
    fun testFormatDuration() {
        assertEquals("0:00", formatDuration(0L))
        assertEquals("3:45", formatDuration(225000L))
        assertEquals("1:05", formatDuration(65000L))
    }

    @Test
    fun testLrcLyricsParsing() {
        val lrcContent = """
            [ti:Test Song]
            [ar:Test Artist]
            [00:12.30]First line of test lyrics
            [00:25.50]Second line of test lyrics
            [01:05.00]Chorus line of test lyrics
        """.trimIndent()

        val parsed = LyricsParser.parse("test_song_1", lrcContent)
        assertEquals(3, parsed.lines.size)
        assertEquals("First line of test lyrics", parsed.lines[0].text)
        assertEquals(12300L, parsed.lines[0].timeMs)
        assertEquals("Second line of test lyrics", parsed.lines[1].text)
        assertEquals(25500L, parsed.lines[1].timeMs)
        assertEquals(65000L, parsed.lines[2].timeMs)
    }

    @Test
    fun testSongModelProperties() {
        val song = Song(
            id = "yt_123",
            title = "Midnight City",
            artist = "M83",
            album = "Hurry Up, We're Dreaming",
            durationMs = 243000L,
            source = MediaSource.YOUTUBE,
            bitrate = "320 kbps",
            isLiked = true
        )

        assertEquals("Midnight City", song.title)
        assertEquals(MediaSource.YOUTUBE, song.source)
        assertTrue(song.isLiked)
    }

    @Test
    fun testAudioEffectConfigDefault() {
        val config = AudioEffectConfig()
        assertTrue(config.isEnabled)
        assertEquals(5, config.bands.size)
        assertEquals(1.0f, config.tempo, 0.001f)
        assertEquals(1.0f, config.pitch, 0.001f)
    }

    @Test
    fun testSettingsCategoryResolution() {
        val appearance = com.example.presentation.model.SettingsCategory.fromId("appearance")
        assertEquals(com.example.presentation.model.SettingsCategory.APPEARANCE, appearance)
        assertEquals("Appearance", appearance?.title)

        val library = com.example.presentation.model.SettingsCategory.fromId("library")
        assertEquals(com.example.presentation.model.SettingsCategory.LIBRARY, library)

        val importPlaylist = com.example.presentation.model.SettingsCategory.fromId("import_playlist")
        assertEquals(com.example.presentation.model.SettingsCategory.IMPORT_PLAYLIST, importPlaylist)
        assertEquals("Import Playlist", importPlaylist?.title)

        val unknown = com.example.presentation.model.SettingsCategory.fromId("unknown_id")
        assertNull(unknown)
    }

    @Test
    fun testPlaylistUrlParser() {
        val parser = com.example.data.playlistimport.PlaylistUrlParser()

        val spotifyUrl = "https://open.spotify.com/playlist/37i9dQZF1DXcBWIGoYBM5M?si=123"
        assertEquals(com.example.data.playlistimport.PlaylistSource.SPOTIFY, parser.detectSource(spotifyUrl))
        assertEquals("37i9dQZF1DXcBWIGoYBM5M", parser.extractId(spotifyUrl))

        val appleUrl = "https://music.apple.com/us/playlist/todays-hits/pl.f4d106fed2bd41149aaacabb233eb5eb"
        assertEquals(com.example.data.playlistimport.PlaylistSource.APPLE_MUSIC, parser.detectSource(appleUrl))
        assertEquals("pl.f4d106fed2bd41149aaacabb233eb5eb", parser.extractId(appleUrl))

        val ytUrl = "https://music.youtube.com/playlist?list=PL4fGSI1pDJn6jXS_5NWD36m_R4Bq92330"
        assertEquals(com.example.data.playlistimport.PlaylistSource.YOUTUBE_MUSIC, parser.detectSource(ytUrl))
        assertEquals("PL4fGSI1pDJn6jXS_5NWD36m_R4Bq92330", parser.extractId(ytUrl))
    }

    @Test
    fun testSongMatcherFallback() = kotlinx.coroutines.runBlocking {
        val matcher = com.example.data.playlistimport.SongMatcher()
        val track = com.example.data.playlistimport.RemoteTrack(title = "Starboy", artist = "The Weeknd")
        val matched = matcher.matchTrack(track)
        assertTrue(matched.isMatched)
        assertNotNull(matched.matchedSong)
        assertEquals("Starboy", matched.matchedSong?.title)
    }

    @Test
    fun testUserPreferencesDefaults() {
        val prefs = com.example.data.preferences.UserPreferences()
        assertTrue(prefs.isDarkTheme)
        assertFalse(prefs.isOled)
        assertTrue(prefs.dynamicColor)
        assertEquals(0, prefs.accentIndex)
        assertEquals("DARK", prefs.appThemeMode)
        assertEquals("ALBUM_ART", prefs.playerThemePreference)
        assertEquals("FLOATING", prefs.navBarStyle)
    }
}
