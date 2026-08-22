package com.example.playback

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.ExoPlayer
import com.example.model.AudioEffectConfig

class AudioEffectsManager(private val player: ExoPlayer) {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var lastAudioSessionId: Int = 0

    var currentConfig = AudioEffectConfig()
        private set

    fun initAudioSession(audioSessionId: Int) {
        if (audioSessionId <= 0) return
        lastAudioSessionId = audioSessionId
        if (!currentConfig.isEnabled) {
            release()
            return
        }
        try {
            release()
            try {
                equalizer = Equalizer(0, audioSessionId).apply {
                    enabled = currentConfig.isEnabled
                }
            } catch (_: Throwable) {
                equalizer = null
            }

            try {
                bassBoost = BassBoost(0, audioSessionId).apply {
                    enabled = currentConfig.isEnabled
                    setStrength(currentConfig.bassBoostStrength.toShort())
                }
            } catch (_: Throwable) {
                bassBoost = null
            }

            try {
                virtualizer = Virtualizer(0, audioSessionId).apply {
                    enabled = currentConfig.isEnabled
                    setStrength(currentConfig.virtualizerStrength.toShort())
                }
            } catch (_: Throwable) {
                virtualizer = null
            }

            applyConfig(currentConfig)
        } catch (_: Throwable) {
            // Fail gracefully if system audio effects are unsupported on the current device
        }
    }

    fun applyConfig(config: AudioEffectConfig) {
        val wasEnabled = currentConfig.isEnabled
        currentConfig = config

        if (config.isEnabled && !wasEnabled && lastAudioSessionId > 0) {
            initAudioSession(lastAudioSessionId)
            return
        }

        try {
            if (!config.isEnabled) {
                release()
            } else {
                equalizer?.enabled = true
                bassBoost?.enabled = true
                virtualizer?.enabled = true

                bassBoost?.setStrength(config.bassBoostStrength.coerceIn(0, 1000).toShort())
                virtualizer?.setStrength(config.virtualizerStrength.coerceIn(0, 1000).toShort())

                equalizer?.let { eq ->
                    val numBands = eq.numberOfBands.toInt()
                    val range = eq.bandLevelRange
                    val min = range.getOrNull(0) ?: -1000
                    val max = range.getOrNull(1) ?: 1000

                    config.bands.forEachIndexed { index, level ->
                        if (index < numBands) {
                            val clampedLevel = level.coerceIn(min.toInt(), max.toInt()).toShort()
                            eq.setBandLevel(index.toShort(), clampedLevel)
                        }
                    }
                }
            }

            // Apply Pitch & Tempo/Speed to ExoPlayer
            player.playbackParameters = PlaybackParameters(config.tempo, config.pitch)

            // ReplayGain software volume normalization
            if (config.replayGainEnabled) {
                player.volume = 0.95f
            } else {
                player.volume = 1.0f
            }
        } catch (_: Throwable) {
            // Safe fallback
        }
    }

    fun release() {
        try {
            equalizer?.release()
        } catch (_: Throwable) {}
        try {
            bassBoost?.release()
        } catch (_: Throwable) {}
        try {
            virtualizer?.release()
        } catch (_: Throwable) {}
        equalizer = null
        bassBoost = null
        virtualizer = null
    }
}
