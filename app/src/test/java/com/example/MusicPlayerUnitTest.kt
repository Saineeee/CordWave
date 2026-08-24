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
    fun testSongLikeToggle() {
        val song = Song(
            id = "s_1",
            title = "Test Song",
            artist = "Artist",
            isLiked = false
        )
        assertFalse(song.isLiked)
        val liked = song.copy(isLiked = true)
        assertTrue(liked.isLiked)
    }
}
