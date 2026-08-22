package com.example.data.lyrics

import com.example.model.LyricLine
import com.example.model.LyricWord
import com.example.model.Lyrics
import java.util.regex.Pattern

object LyricsParser {

    private val LRC_LINE_PATTERN = Pattern.compile("\\[(\\d{2}):(\\d{2})(?:[.:](\\d{2,3}))?\\](.*)")
    private val WORD_TIME_PATTERN = Pattern.compile("<(\\d{2}):(\\d{2})(?:[.:](\\d{2,3}))?>([^<]*)")

    fun parse(songId: String, rawContent: String, provider: String = "LrcLib"): Lyrics {
        if (rawContent.isBlank()) {
            return Lyrics(songId = songId, lines = emptyList(), isSynced = false, plainLyrics = "", provider = provider)
        }

        if (rawContent.contains("<tt") || rawContent.contains("<body")) {
            return parseTtml(songId, rawContent, provider)
        }

        return parseLrc(songId, rawContent, provider)
    }

    private fun parseLrc(songId: String, lrc: String, provider: String): Lyrics {
        val lines = mutableListOf<LyricLine>()
        val rawLines = lrc.lines()

        for (line in rawLines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("[ti:") || trimmed.startsWith("[ar:") ||
                trimmed.startsWith("[al:") || trimmed.startsWith("[by:") ||
                trimmed.startsWith("[offset:")
            ) {
                continue
            }

            val matcher = LRC_LINE_PATTERN.matcher(trimmed)
            if (matcher.find()) {
                val min = matcher.group(1)?.toLongOrNull() ?: 0L
                val sec = matcher.group(2)?.toLongOrNull() ?: 0L
                val milliStr = matcher.group(3) ?: "00"
                val milli = if (milliStr.length == 2) {
                    (milliStr.toLongOrNull() ?: 0L) * 10
                } else {
                    milliStr.toLongOrNull() ?: 0L
                }
                val timeMs = min * 60 * 1000 + sec * 1000 + milli
                val content = matcher.group(4)?.trim() ?: ""

                val words = extractKaraokeWords(content, timeMs)
                val cleanText = if (words.isNotEmpty()) {
                    words.joinToString(" ") { it.word }
                } else {
                    content.replace(Regex("<[^>]*>"), "").trim()
                }

                val isInstrumental = cleanText == "♪" || cleanText.equals("[Instrumental]", ignoreCase = true) || cleanText.isBlank()

                lines.add(
                    LyricLine(
                        timeMs = timeMs,
                        text = if (cleanText.isBlank()) "♪" else cleanText,
                        words = words,
                        isInstrumental = isInstrumental
                    )
                )
            }
        }

        val sortedLines = lines.sortedBy { it.timeMs }
        val isSynced = sortedLines.isNotEmpty()

        return Lyrics(
            songId = songId,
            lines = sortedLines,
            isSynced = isSynced,
            plainLyrics = if (isSynced) sortedLines.joinToString("\n") { it.text } else lrc,
            provider = provider
        )
    }

    private fun extractKaraokeWords(text: String, lineStartMs: Long): List<LyricWord> {
        val words = mutableListOf<LyricWord>()
        val matcher = WORD_TIME_PATTERN.matcher(text)
        var lastTime = lineStartMs

        while (matcher.find()) {
            val min = matcher.group(1)?.toLongOrNull() ?: 0L
            val sec = matcher.group(2)?.toLongOrNull() ?: 0L
            val milliStr = matcher.group(3) ?: "00"
            val milli = if (milliStr.length == 2) (milliStr.toLongOrNull() ?: 0L) * 10 else (milliStr.toLongOrNull() ?: 0L)
            val wordEndMs = min * 60 * 1000 + sec * 1000 + milli
            val wordText = matcher.group(4)?.trim() ?: ""

            if (wordText.isNotEmpty()) {
                words.add(LyricWord(word = wordText, startMs = lastTime, endMs = wordEndMs))
                lastTime = wordEndMs
            }
        }

        return words
    }

    private fun parseTtml(songId: String, ttml: String, provider: String): Lyrics {
        val lines = mutableListOf<LyricLine>()
        val pPattern = Pattern.compile("<p\\s+begin=\"([^\"]+)\"(?:\\s+end=\"([^\"]+)\")?[^>]*>(.*?)</p>", Pattern.DOTALL)
        val matcher = pPattern.matcher(ttml)

        while (matcher.find()) {
            val beginStr = matcher.group(1) ?: "00:00.00"
            val textContent = matcher.group(3)?.replace(Regex("<[^>]*>"), "")?.trim() ?: ""
            val timeMs = parseTtmlTimestamp(beginStr)

            lines.add(
                LyricLine(
                    timeMs = timeMs,
                    text = textContent,
                    isInstrumental = textContent.isBlank() || textContent == "♪"
                )
            )
        }

        val sorted = lines.sortedBy { it.timeMs }
        return Lyrics(
            songId = songId,
            lines = sorted,
            isSynced = sorted.isNotEmpty(),
            plainLyrics = sorted.joinToString("\n") { it.text },
            provider = provider
        )
    }

    private fun parseTtmlTimestamp(ts: String): Long {
        return try {
            val parts = ts.split(":")
            if (parts.size == 2) {
                val min = parts[0].toLong()
                val secParts = parts[1].split(".")
                val sec = secParts[0].toLong()
                val ms = if (secParts.size > 1) secParts[1].padEnd(3, '0').take(3).toLong() else 0L
                min * 60000 + sec * 1000 + ms
            } else if (parts.size == 3) {
                val hr = parts[0].toLong()
                val min = parts[1].toLong()
                val secParts = parts[2].split(".")
                val sec = secParts[0].toLong()
                val ms = if (secParts.size > 1) secParts[1].padEnd(3, '0').take(3).toLong() else 0L
                hr * 3600000 + min * 60000 + sec * 1000 + ms
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }
}
