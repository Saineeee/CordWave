package com.example.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.data.repository.MusicRepository
import com.example.model.AudioEffectConfig
import com.example.model.RepeatMode
import com.example.model.Song
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MusicPlayerController(
    private val context: Context,
    private val repository: MusicRepository,
    private val scope: CoroutineScope
) {
    private var exoPlayer: ExoPlayer? = null
    private var audioEffectsManager: AudioEffectsManager? = null

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _playbackQueue = MutableStateFlow<List<Song>>(emptyList())
    val playbackQueue: StateFlow<List<Song>> = _playbackQueue.asStateFlow()

    private val _currentQueueIndex = MutableStateFlow(0)
    val currentQueueIndex: StateFlow<Int> = _currentQueueIndex.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _audioEffects = MutableStateFlow(AudioEffectConfig())
    val audioEffects: StateFlow<AudioEffectConfig> = _audioEffects.asStateFlow()

    private val _sleepTimerRemainingSec = MutableStateFlow<Int?>(null)
    val sleepTimerRemainingSec: StateFlow<Int?> = _sleepTimerRemainingSec.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private var positionTickerJob: Job? = null
    private var sleepTimerJob: Job? = null

    init {
        initPlayer()
        startPositionTicker()
    }

    private fun initPlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("OuterTune-Music-Player/2.4 (Android; AudioStream)")
            .setConnectTimeoutMs(20000)
            .setReadTimeoutMs(25000)
            .setAllowCrossProtocolRedirects(true)

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(httpDataSourceFactory)

        exoPlayer = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build().apply {
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        _isPlaying.value = playing
                        if (playing) {
                            _currentSong.value?.let { song ->
                                scope.launch {
                                    repository.recordPlay(song, currentPosition)
                                }
                            }
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        _isBuffering.value = (playbackState == Player.STATE_BUFFERING)
                        if (playbackState == Player.STATE_READY) {
                            _isBuffering.value = false
                            val d = duration
                            if (d > 0) {
                                _durationMs.value = d
                            }
                            audioEffectsManager?.initAudioSession(audioSessionId)
                        } else if (playbackState == Player.STATE_ENDED) {
                            handleSongEnded()
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        error.printStackTrace()
                        _isBuffering.value = false
                        _isPlaying.value = false
                        // Try playing with fallback stream if available and not already fallback
                        val current = _currentSong.value
                        if (current != null && !current.mediaUri.contains("SoundHelix")) {
                            val hash = current.id.hashCode() and 0x7FFFFFFF
                            val fallbackIndex = (hash % 10) + 1
                            val fallbackUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-$fallbackIndex.mp3"
                            val fallbackSong = current.copy(mediaUri = fallbackUri)
                            _currentSong.value = fallbackSong
                            playSongInternal(fallbackSong)
                        }
                    }
                })
            }

        exoPlayer?.let {
            audioEffectsManager = AudioEffectsManager(it)
        }
    }

    fun playSong(song: Song, queue: List<Song> = emptyList()) {
        val finalQueue = if (queue.isNotEmpty()) queue else listOf(song)
        _playbackQueue.value = finalQueue
        val index = finalQueue.indexOfFirst { it.id == song.id }.let { if (it >= 0) it else 0 }
        _currentQueueIndex.value = index
        playSongInternal(song)
    }

    fun playQueueIndex(index: Int) {
        val queue = _playbackQueue.value
        if (index in queue.indices) {
            _currentQueueIndex.value = index
            playSongInternal(queue[index])
        }
    }

    private fun playSongInternal(song: Song) {
        _currentSong.value = song
        _durationMs.value = if (song.durationMs > 0) song.durationMs else 180000L
        _currentPositionMs.value = 0L
        _isBuffering.value = true

        val mediaUri = song.filePath?.takeIf { java.io.File(it).exists() } ?: song.mediaUri

        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(song.albumArtUri?.let { Uri.parse(it) })
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id)
            .setUri(mediaUri)
            .setMediaMetadata(mediaMetadata)
            .build()

        exoPlayer?.apply {
            playWhenReady = true
            setMediaItem(mediaItem)
            prepare()
            play()
        }

        // Persist queue state
        scope.launch {
            repository.saveQueueState(
                currentSongId = song.id,
                songIds = _playbackQueue.value.map { it.id },
                index = _currentQueueIndex.value,
                positionMs = 0L,
                isShuffle = _isShuffle.value,
                repeatMode = _repeatMode.value
            )
        }
    }

    fun togglePlayPause() {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                if (player.playbackState == Player.STATE_IDLE && _currentSong.value != null) {
                    playSongInternal(_currentSong.value!!)
                } else {
                    player.play()
                }
            }
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        _currentPositionMs.value = positionMs
    }

    fun skipToNext() {
        val queue = _playbackQueue.value
        if (queue.isEmpty()) return

        if (_repeatMode.value == RepeatMode.ONE) {
            _currentSong.value?.let { playSongInternal(it) }
            return
        }

        var nextIndex = _currentQueueIndex.value + 1
        if (_isShuffle.value && queue.size > 1) {
            nextIndex = queue.indices.filter { it != _currentQueueIndex.value }.random()
        }

        if (nextIndex < queue.size) {
            _currentQueueIndex.value = nextIndex
            playSongInternal(queue[nextIndex])
        } else if (_repeatMode.value == RepeatMode.ALL) {
            _currentQueueIndex.value = 0
            playSongInternal(queue[0])
        }
    }

    fun skipToPrevious() {
        val queue = _playbackQueue.value
        if (queue.isEmpty()) return

        if ((exoPlayer?.currentPosition ?: 0L) > 3000L) {
            seekTo(0L)
            return
        }

        val prevIndex = (_currentQueueIndex.value - 1).coerceAtLeast(0)
        _currentQueueIndex.value = prevIndex
        playSongInternal(queue[prevIndex])
    }

    private fun handleSongEnded() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                seekTo(0L)
                exoPlayer?.play()
            }
            RepeatMode.ALL -> {
                skipToNext()
            }
            RepeatMode.OFF -> {
                val queue = _playbackQueue.value
                val nextIndex = _currentQueueIndex.value + 1
                if (nextIndex < queue.size) {
                    skipToNext()
                } else {
                    _isPlaying.value = false
                    seekTo(0L)
                }
            }
        }
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun addToQueueNext(song: Song) {
        val currentList = _playbackQueue.value.toMutableList()
        val insertIndex = (_currentQueueIndex.value + 1).coerceAtMost(currentList.size)
        currentList.add(insertIndex, song)
        _playbackQueue.value = currentList
    }

    fun addToQueueEnd(song: Song) {
        val currentList = _playbackQueue.value.toMutableList()
        currentList.add(song)
        _playbackQueue.value = currentList
        if (_currentSong.value == null) {
            playSong(song, currentList)
        }
    }

    fun removeFromQueue(index: Int) {
        val currentList = _playbackQueue.value.toMutableList()
        if (index in currentList.indices) {
            val removingCurrent = (index == _currentQueueIndex.value)
            currentList.removeAt(index)
            _playbackQueue.value = currentList
            if (removingCurrent && currentList.isNotEmpty()) {
                val newIndex = index.coerceAtMost(currentList.size - 1)
                _currentQueueIndex.value = newIndex
                playSongInternal(currentList[newIndex])
            } else if (index < _currentQueueIndex.value) {
                _currentQueueIndex.value = _currentQueueIndex.value - 1
            }
        }
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        val list = _playbackQueue.value.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices) {
            val moved = list.removeAt(fromIndex)
            list.add(toIndex, moved)
            _playbackQueue.value = list
            if (fromIndex == _currentQueueIndex.value) {
                _currentQueueIndex.value = toIndex
            } else if (fromIndex < _currentQueueIndex.value && toIndex >= _currentQueueIndex.value) {
                _currentQueueIndex.value = _currentQueueIndex.value - 1
            } else if (fromIndex > _currentQueueIndex.value && toIndex <= _currentQueueIndex.value) {
                _currentQueueIndex.value = _currentQueueIndex.value + 1
            }
        }
    }

    fun clearQueue() {
        exoPlayer?.stop()
        _playbackQueue.value = emptyList()
        _currentSong.value = null
        _isPlaying.value = false
        _currentPositionMs.value = 0L
    }

    fun setAudioEffects(config: AudioEffectConfig) {
        _audioEffects.value = config
        audioEffectsManager?.applyConfig(config)
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _sleepTimerRemainingSec.value = null
            return
        }

        var remaining = minutes * 60
        _sleepTimerRemainingSec.value = remaining

        sleepTimerJob = scope.launch {
            while (remaining > 0) {
                delay(1000)
                remaining--
                _sleepTimerRemainingSec.value = remaining
            }
            // Timer expired -> pause playback
            exoPlayer?.pause()
            _sleepTimerRemainingSec.value = null
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimerRemainingSec.value = null
    }

    private fun startPositionTicker() {
        positionTickerJob?.cancel()
        positionTickerJob = scope.launch {
            while (isActive) {
                exoPlayer?.let { player ->
                    if (player.isPlaying) {
                        _currentPositionMs.value = player.currentPosition.coerceAtLeast(0L)
                        val d = player.duration
                        if (d > 0) {
                            _durationMs.value = d
                        }
                    }
                }
                delay(250)
            }
        }
    }

    fun release() {
        positionTickerJob?.cancel()
        sleepTimerJob?.cancel()
        audioEffectsManager?.release()
        exoPlayer?.release()
        exoPlayer = null
    }
}
