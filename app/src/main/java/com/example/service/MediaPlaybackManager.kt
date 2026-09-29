package com.example.service

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.SurfaceHolder
import com.example.data.model.VideoItem
import com.example.data.remote.SampleVideoData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PlaybackState {
    IDLE,
    PREPARING,
    PLAYING,
    PAUSED,
    COMPLETED,
    ERROR
}

object MediaPlaybackManager {

    private const val TAG = "MediaPlaybackManager"

    private var mediaPlayer: MediaPlayer? = null
    private var surfaceHolder: SurfaceHolder? = null
    private var appContext: Context? = null

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentVideo = MutableStateFlow<VideoItem?>(null)
    val currentVideo: StateFlow<VideoItem?> = _currentVideo.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _selectedQuality = MutableStateFlow("1080p60 HDR")
    val selectedQuality: StateFlow<String> = _selectedQuality.asStateFlow()

    private val _isAudioOnly = MutableStateFlow(false)
    val isAudioOnly: StateFlow<Boolean> = _isAudioOnly.asStateFlow()

    private val _isLooping = MutableStateFlow(false)
    val isLooping: StateFlow<Boolean> = _isLooping.asStateFlow()

    private val _isMiniPlayerVisible = MutableStateFlow(false)
    val isMiniPlayerVisible: StateFlow<Boolean> = _isMiniPlayerVisible.asStateFlow()

    private val _sleepTimerSeconds = MutableStateFlow(0)
    val sleepTimerSeconds: StateFlow<Int> = _sleepTimerSeconds.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())

    private val progressRunnable = object : Runnable {
        override fun run() {
            try {
                if (_playbackState.value == PlaybackState.PLAYING) {
                    mediaPlayer?.let { player ->
                        if (player.isPlaying) {
                            _currentPositionMs.value = player.currentPosition.toLong()
                            _durationMs.value = player.duration.toLong().coerceAtLeast(0L)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error updating progress: ${e.message}")
            }
            if (_playbackState.value == PlaybackState.PLAYING) {
                handler.postDelayed(this, 500)
            }
        }
    }

    private val sleepTimerRunnable = object : Runnable {
        override fun run() {
            if (_sleepTimerSeconds.value > 0) {
                _sleepTimerSeconds.value -= 1
                if (_sleepTimerSeconds.value <= 0) {
                    pause()
                } else {
                    handler.postDelayed(this, 1000)
                }
            }
        }
    }

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    @Synchronized
    fun playVideo(video: VideoItem, startPosMs: Long = 0L, forceRestart: Boolean = false) {
        // If already playing the same video and not forceRestarting, do not recreate
        if (!forceRestart && _currentVideo.value?.id == video.id &&
            (_playbackState.value == PlaybackState.PLAYING || _playbackState.value == PlaybackState.PAUSED)
        ) {
            if (_playbackState.value == PlaybackState.PAUSED) {
                resume()
            }
            return
        }

        _currentVideo.value = video
        _isMiniPlayerVisible.value = true
        _playbackState.value = PlaybackState.PREPARING
        _isPlaying.value = false
        handler.removeCallbacks(progressRunnable)

        try {
            releasePlayerInternal()

            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(video.videoUrl)

                surfaceHolder?.let { holder ->
                    if (holder.surface?.isValid == true) {
                        runCatching { setDisplay(holder) }
                    }
                }

                isLooping = _isLooping.value

                setOnPreparedListener { mp ->
                    if (_playbackState.value == PlaybackState.PREPARING) {
                        try {
                            if (startPosMs > 0) {
                                mp.seekTo(startPosMs.toInt())
                            }
                            mp.start()
                            applySpeedInternal(mp, _playbackSpeed.value)
                            _playbackState.value = PlaybackState.PLAYING
                            _isPlaying.value = true
                            _durationMs.value = mp.duration.toLong().coerceAtLeast(0L)
                            startProgressTracker()
                            startForegroundPlaybackService()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error starting MediaPlayer onPrepared: ${e.message}")
                            _playbackState.value = PlaybackState.ERROR
                            _isPlaying.value = false
                        }
                    }
                }

                setOnCompletionListener {
                    _playbackState.value = PlaybackState.COMPLETED
                    _isPlaying.value = false
                    if (!_isLooping.value) {
                        playNext()
                    }
                }

                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer onError: what=$what, extra=$extra")
                    _playbackState.value = PlaybackState.ERROR
                    _isPlaying.value = false
                    handler.removeCallbacks(progressRunnable)
                    true // Handled gracefully
                }

                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up MediaPlayer: ${e.message}")
            _playbackState.value = PlaybackState.ERROR
            _isPlaying.value = false
        }
    }

    @Synchronized
    fun attachSurface(holder: SurfaceHolder) {
        surfaceHolder = holder
        try {
            if (holder.surface?.isValid == true) {
                mediaPlayer?.setDisplay(holder)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not attach surface to MediaPlayer: ${e.message}")
        }
    }

    @Synchronized
    fun detachSurface() {
        surfaceHolder = null
        try {
            mediaPlayer?.setDisplay(null)
        } catch (e: Exception) {
            Log.w(TAG, "Could not detach surface from MediaPlayer: ${e.message}")
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            resume()
        }
    }

    @Synchronized
    fun resume() {
        try {
            mediaPlayer?.let { player ->
                if (_playbackState.value == PlaybackState.PAUSED || _playbackState.value == PlaybackState.COMPLETED) {
                    player.start()
                    _playbackState.value = PlaybackState.PLAYING
                    _isPlaying.value = true
                    startProgressTracker()
                    startForegroundPlaybackService()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in resume(): ${e.message}")
            _isPlaying.value = false
        }
    }

    @Synchronized
    fun pause() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error in pause(): ${e.message}")
        }
        _playbackState.value = PlaybackState.PAUSED
        _isPlaying.value = false
        handler.removeCallbacks(progressRunnable)
        updateServiceNotification()
    }

    @Synchronized
    fun seekTo(positionMs: Long) {
        try {
            mediaPlayer?.let { player ->
                if (_playbackState.value == PlaybackState.PLAYING || _playbackState.value == PlaybackState.PAUSED) {
                    player.seekTo(positionMs.toInt())
                    _currentPositionMs.value = positionMs
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error seeking: ${e.message}")
        }
    }

    fun seekForward(deltaMs: Long = 10000L) {
        val target = (_currentPositionMs.value + deltaMs).coerceAtMost(_durationMs.value)
        seekTo(target)
    }

    fun seekBackward(deltaMs: Long = 10000L) {
        val target = (_currentPositionMs.value - deltaMs).coerceAtLeast(0L)
        seekTo(target)
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        mediaPlayer?.let { applySpeedInternal(it, speed) }
    }

    private fun applySpeedInternal(player: MediaPlayer, speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if (_playbackState.value == PlaybackState.PLAYING) {
                    val params = player.playbackParams
                    params.speed = speed
                    player.playbackParams = params
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error applying playback speed: ${e.message}")
            }
        }
    }

    fun setQuality(quality: String) {
        _selectedQuality.value = quality
    }

    fun toggleAudioOnly() {
        _isAudioOnly.value = !_isAudioOnly.value
    }

    fun toggleLooping() {
        _isLooping.value = !_isLooping.value
        try {
            mediaPlayer?.isLooping = _isLooping.value
        } catch (_: Exception) {}
    }

    fun setSleepTimer(minutes: Int) {
        _sleepTimerSeconds.value = minutes * 60
        handler.removeCallbacks(sleepTimerRunnable)
        if (minutes > 0) {
            handler.post(sleepTimerRunnable)
        }
    }

    fun playNext() {
        val all = SampleVideoData.sampleVideos
        val cur = _currentVideo.value ?: return
        val idx = all.indexOfFirst { it.id == cur.id }
        val nextIdx = if (idx >= 0) (idx + 1) % all.size else 0
        playVideo(all[nextIdx], forceRestart = true)
    }

    fun playPrevious() {
        val all = SampleVideoData.sampleVideos
        val cur = _currentVideo.value ?: return
        val idx = all.indexOfFirst { it.id == cur.id }
        val prevIdx = if (idx > 0) idx - 1 else all.size - 1
        playVideo(all[prevIdx], forceRestart = true)
    }

    fun dismissMiniPlayer() {
        pause()
        _isMiniPlayerVisible.value = false
        stopForegroundPlaybackService()
    }

    private fun startProgressTracker() {
        handler.removeCallbacks(progressRunnable)
        handler.post(progressRunnable)
    }

    private fun startForegroundPlaybackService() {
        appContext?.let { ctx ->
            try {
                val intent = Intent(ctx, BackgroundMediaService::class.java).apply {
                    action = BackgroundMediaService.ACTION_START
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ctx.startForegroundService(intent)
                } else {
                    ctx.startService(intent)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not start foreground media service: ${e.message}")
            }
        }
    }

    private fun updateServiceNotification() {
        appContext?.let { ctx ->
            try {
                val intent = Intent(ctx, BackgroundMediaService::class.java).apply {
                    action = BackgroundMediaService.ACTION_UPDATE
                }
                ctx.startService(intent)
            } catch (_: Exception) {}
        }
    }

    private fun stopForegroundPlaybackService() {
        appContext?.let { ctx ->
            try {
                val intent = Intent(ctx, BackgroundMediaService::class.java).apply {
                    action = BackgroundMediaService.ACTION_STOP
                }
                ctx.startService(intent)
            } catch (_: Exception) {}
        }
    }

    @Synchronized
    private fun releasePlayerInternal() {
        handler.removeCallbacks(progressRunnable)
        val player = mediaPlayer
        mediaPlayer = null
        if (player != null) {
            try {
                player.setOnPreparedListener(null)
                player.setOnCompletionListener(null)
                player.setOnErrorListener(null)
                player.reset()
                player.release()
            } catch (e: Exception) {
                Log.w(TAG, "Error releasing MediaPlayer: ${e.message}")
            }
        }
    }

    fun releasePlayer() {
        releasePlayerInternal()
        _playbackState.value = PlaybackState.IDLE
        _isPlaying.value = false
    }
}
