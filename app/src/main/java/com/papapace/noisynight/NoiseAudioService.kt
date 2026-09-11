package com.papapace.noisynight

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoiseAudioService : Service() {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    // Audio Playback state
    private var audioTrack: AudioTrack? = null
    private var volumeFaderJob: Job? = null

    private val whiteGenerator = WhiteNoiseGenerator()
    private val pinkGenerator = PinkNoiseGenerator()
    private val brownGenerator = BrownNoiseGenerator()
    private val greenGenerator = GreenNoiseGenerator()
    private val blueGenerator = BlueNoiseGenerator()
    private val purpleGenerator = PurpleNoiseGenerator()
    private val deepBrownGenerator = DeepBrownNoiseGenerator()
    private val amberGenerator = AmberNoiseGenerator()
    private val oceanWavesGenerator = WindStormGenerator()
    private val windStormGenerator = OceanWavesGenerator()

    // State flows for Compose UI
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentNoise = MutableStateFlow(NoiseType.WHITE)
    val currentNoise = _currentNoise.asStateFlow()

    private val _timerRemainingMs = MutableStateFlow(0L)
    val timerRemainingMs = _timerRemainingMs.asStateFlow()

    private val _timerTotalMs = MutableStateFlow(0L)
    val timerTotalMs = _timerTotalMs.asStateFlow()

    // Sleep Timer state
    private var timerEndTimeMs = 0L
    private var isTimerActive = false
    private var timerJob: Job? = null

    // System integrations
    private lateinit var mediaSession: MediaSession
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        private const val TAG = "NoiseAudioService"
        private const val CHANNEL_ID = "noisy_night_playback"
        private const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY = "com.papapace.noisynight.ACTION_PLAY"
        const val ACTION_PAUSE = "com.papapace.noisynight.ACTION_PAUSE"
        const val ACTION_STOP = "com.papapace.noisynight.ACTION_STOP"
        const val ACTION_TOGGLE = "com.papapace.noisynight.ACTION_TOGGLE"

        const val SAMPLE_RATE = 44100
        const val FADE_DURATION_MS = 60_000L // 1 minute fade-out
    }

    enum class NoiseType(val displayName: String) {
        HEAVY_RAIN("Heavy Rain"),
        SOFT_RAIN("Soft Rain"),
        OCEAN_WAVES("Ocean Waves"),
        WIND_STORM("Wind Storm"),
        WHITE("White Noise"),
        GREEN("Green Noise"),
        PINK("Pink Noise"),
        BROWN("Brown Noise"),
        BLUE("Blue Noise"),
        PURPLE("Purple Noise"),
        DEEP_BROWN("Deep Brown Noise"),
        AMBER("Amber Noise")
    }

    inner class LocalBinder : Binder() {
        fun getService(): NoiseAudioService = this@NoiseAudioService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate service")
        createNotificationChannel()
        setupMediaSession()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> playNoise()
            ACTION_PAUSE -> pauseNoise()
            ACTION_STOP -> stopServicePlayback()
            ACTION_TOGGLE -> {
                if (_isPlaying.value) pauseNoise() else playNoise()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    // Playback control
    fun selectNoise(type: NoiseType) {
        _currentNoise.value = type
        if (_isPlaying.value) {
            startAudio()
        }
        updateNotification()
    }

    fun playNoise() {
        if (_isPlaying.value) return
        Log.d(TAG, "playNoise starting")
        acquireWakeLock()
        _isPlaying.value = true
        startForeground(NOTIFICATION_ID, buildNotification())
        startAudio()
        updateMediaSessionState()
        startTimerJob()
    }

    fun pauseNoise() {
        if (!_isPlaying.value) return
        Log.d(TAG, "pauseNoise")
        _isPlaying.value = false
        stopAudio()
        releaseWakeLock()
        updateMediaSessionState()
        updateNotification()
        // Stop foreground state (keep notification but make it dismissible)
        stopForeground(STOP_FOREGROUND_DETACH)
    }

    fun startTimer(durationMinutes: Int) {
        if (durationMinutes <= 0) {
            cancelTimer()
            return
        }
        val durationMs = durationMinutes * 60 * 1000L
        _timerTotalMs.value = durationMs
        timerEndTimeMs = System.currentTimeMillis() + durationMs
        isTimerActive = true
        _timerRemainingMs.value = durationMs
        startTimerJob()
    }

    fun cancelTimer() {
        isTimerActive = false
        timerEndTimeMs = 0L
        _timerRemainingMs.value = 0L
        _timerTotalMs.value = 0L
        timerJob?.cancel()
    }

    private fun startTimerJob() {
        timerJob?.cancel()
        if (!isTimerActive || !_isPlaying.value) return

        timerJob = serviceScope.launch {
            while (isTimerActive && _isPlaying.value) {
                val now = System.currentTimeMillis()
                val remaining = timerEndTimeMs - now
                if (remaining <= 0) {
                    _timerRemainingMs.value = 0L
                    cancelTimer()
                    pauseNoise()
                    break
                }
                _timerRemainingMs.value = remaining
                delay(1.seconds)
            }
        }
    }

    private fun stopServicePlayback() {
        pauseNoise()
        cancelTimer()
        stopSelf()
    }

    // Audio stream synthesis and playback
    private fun startAudio() {
        stopAudio()
        
        val type = _currentNoise.value
        
        if (type == NoiseType.HEAVY_RAIN || type == NoiseType.SOFT_RAIN) {
            startStaticAudioFromResource(when (type) {
                NoiseType.HEAVY_RAIN -> R.raw.heavy_rain
                else -> R.raw.soft_rain
            })
        } else {
            startStaticAudio()
        }
    }

    /**
     * Loads a PCM WAV from res/raw into an AudioTrack MODE_STATIC buffer and
     * calls setLoopPoints so the hardware loops it with zero gap — identical to
     * how the procedural generators work.
     */
    private fun startStaticAudioFromResource(resId: Int) {
        try {
            // Open the raw resource as a stream and skip the 44-byte WAV header
            val inputStream = resources.openRawResource(resId)
            val headerBytes = ByteArray(44)
            inputStream.read(headerBytes)

            // Parse sample rate and num channels from WAV header
            val fileSampleRate = (headerBytes[24].toInt() and 0xFF) or
                ((headerBytes[25].toInt() and 0xFF) shl 8) or
                ((headerBytes[26].toInt() and 0xFF) shl 16) or
                ((headerBytes[27].toInt() and 0xFF) shl 24)
            val fileChannels = (headerBytes[22].toInt() and 0xFF) or
                ((headerBytes[23].toInt() and 0xFF) shl 8)

            val pcmBytes = inputStream.readBytes()
            inputStream.close()

            // Convert 16-bit signed PCM → float samples, downmix to mono if needed
            val numFrames = pcmBytes.size / (2 * fileChannels)
            val floatBuffer = FloatArray(numFrames)
            var byteIdx = 0
            for (f in 0 until numFrames) {
                var sum = 0f
                for (c in 0 until fileChannels) {
                    val lo = pcmBytes[byteIdx++].toInt() and 0xFF
                    val hi = pcmBytes[byteIdx++].toInt()
                    val sample = (hi shl 8 or lo).toShort().toInt()
                    sum += sample / 32768f
                }
                floatBuffer[f] = (sum / fileChannels).coerceIn(-1f, 1f)
            }

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            val audioFormat = AudioFormat.Builder()
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                .setSampleRate(fileSampleRate)
                .build()

            val localTrack = AudioTrack(
                audioAttributes,
                audioFormat,
                floatBuffer.size * 4,
                AudioTrack.MODE_STATIC,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )
            audioTrack = localTrack
            localTrack.write(floatBuffer, 0, floatBuffer.size, AudioTrack.WRITE_BLOCKING)
            localTrack.setLoopPoints(0, numFrames, -1)
            localTrack.play()
            startVolumeFaderJob()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading WAV resource", e)
            startStaticAudio() // fallback to procedural white noise
        }
    }
    
    private fun startStaticAudio() {
        val loopSeconds = 11
        val totalFrames = SAMPLE_RATE * loopSeconds
        
        // Size in bytes (float is 4 bytes)
        val bufferSizeBytes = totalFrames * 4

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val audioFormat = AudioFormat.Builder()
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
            .setSampleRate(SAMPLE_RATE)
            .build()

        val localTrack = AudioTrack(
            audioAttributes,
            audioFormat,
            bufferSizeBytes,
            AudioTrack.MODE_STATIC,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )
        audioTrack = localTrack

        val generator: NoiseGenerator = when (_currentNoise.value) {
            NoiseType.WHITE -> whiteGenerator
            NoiseType.PINK -> pinkGenerator
            NoiseType.BROWN -> brownGenerator
            NoiseType.GREEN -> greenGenerator
            NoiseType.BLUE -> blueGenerator
            NoiseType.PURPLE -> purpleGenerator
            NoiseType.DEEP_BROWN -> deepBrownGenerator
            NoiseType.AMBER -> amberGenerator
            NoiseType.OCEAN_WAVES -> oceanWavesGenerator
            NoiseType.WIND_STORM -> windStormGenerator
            else -> whiteGenerator // Fallback for Rain types, though they won't reach here
        }

        // Generate the 11 seconds of noise mathematically once
        val audioBuffer = FloatArray(totalFrames)
        generator.fillBuffer(audioBuffer)



        // Write the full buffer to the audio track memory
        localTrack.write(audioBuffer, 0, audioBuffer.size, AudioTrack.WRITE_BLOCKING)
        
        // Tell Android to loop this static memory infinitely
        localTrack.setLoopPoints(0, totalFrames, -1)
        localTrack.play()

        startVolumeFaderJob()
    }

    private fun startVolumeFaderJob() {
        volumeFaderJob?.cancel()
        volumeFaderJob = serviceScope.launch {
            while (true) {
                var fadeFactor = 1.0f
                if (isTimerActive) {
                    val remaining = timerEndTimeMs - System.currentTimeMillis()
                    if (remaining <= 0) {
                        cancelTimer()
                        pauseNoise()
                        break
                    } else if (remaining < FADE_DURATION_MS) {
                        fadeFactor = remaining.toFloat() / FADE_DURATION_MS.toFloat()
                    }
                }
                audioTrack?.setVolume(fadeFactor)
                delay(100.milliseconds) // Update volume smoothly 10 times a second
            }
        }
    }

    private fun stopAudio() {
        volumeFaderJob?.cancel()
        try {
            audioTrack?.apply {
                if (state == AudioTrack.STATE_INITIALIZED) {
                    stop()
                    release()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing AudioTrack", e)
        }
        audioTrack = null
    }

    // Media Session
    private fun setupMediaSession() {
        mediaSession = MediaSession(this, TAG).apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() {
                    playNoise()
                }

                override fun onPause() {
                    pauseNoise()
                }

                override fun onStop() {
                    stopServicePlayback()
                }
            })
            isActive = true
        }
        updateMediaSessionState()
    }

    private fun updateMediaSessionState() {
        val state = if (_isPlaying.value) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED
        val stateBuilder = PlaybackState.Builder()
            .setActions(PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_STOP)
            .setState(state, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1.0f)
        mediaSession.setPlaybackState(stateBuilder.build())
    }

    // Notifications
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Playback Control"
            val descriptionText = "Displays lockscreen media actions for noise generation"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val playPauseAction = if (_isPlaying.value) {
            val pauseIntent = Intent(this, NoiseAudioService::class.java).apply { action = ACTION_PAUSE }
            val pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_IMMUTABLE)
            Notification.Action.Builder(
                android.R.drawable.ic_media_pause,
                "Pause",
                pausePendingIntent
            ).build()
        } else {
            val playIntent = Intent(this, NoiseAudioService::class.java).apply { action = ACTION_PLAY }
            val playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_IMMUTABLE)
            Notification.Action.Builder(
                android.R.drawable.ic_media_play,
                "Play",
                playPendingIntent
            ).build()
        }

        val stopIntent = Intent(this, NoiseAudioService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE)
        val stopAction = Notification.Action.Builder(
            android.R.drawable.ic_menu_close_clear_cancel,
            "Stop",
            stopPendingIntent
        ).build()

        val openIntent = Intent(this, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(this, 2, openIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        return builder
            .setContentTitle("Noisy Night")
            .setContentText("Playing ${_currentNoise.value.displayName}")
            .setSmallIcon(android.R.drawable.ic_media_play) // Use standard simple platform play icon
            .setContentIntent(openPendingIntent)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .addAction(playPauseAction)
            .addAction(stopAction)
            .setStyle(
                Notification.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1)
            )
            .build()
    }

    private fun updateNotification() {
        if (_isPlaying.value) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, buildNotification())
        }
    }

    // Wake Lock
    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NoisyNight::PlaybackWakeLock")
        }
        if (wakeLock?.isHeld == false) {
            wakeLock?.acquire(12 * 60 * 60 * 1000L) // 12 hours safeguard max
            Log.d(TAG, "WakeLock acquired")
        }
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
            Log.d(TAG, "WakeLock released")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy service")
        stopAudio()
        releaseWakeLock()
        mediaSession.release()
        timerJob?.cancel()
    }
}
