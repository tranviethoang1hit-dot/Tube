package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class BackgroundMediaService : Service() {

    companion object {
        const val CHANNEL_ID = "tube_premium_playback_channel"
        const val NOTIFICATION_ID = 2026

        const val ACTION_START = "com.example.service.ACTION_START"
        const val ACTION_UPDATE = "com.example.service.ACTION_UPDATE"
        const val ACTION_PLAY = "com.example.service.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.service.ACTION_PAUSE"
        const val ACTION_NEXT = "com.example.service.ACTION_NEXT"
        const val ACTION_PREV = "com.example.service.ACTION_PREV"
        const val ACTION_SEEK_FWD = "com.example.service.ACTION_SEEK_FWD"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
    }

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_PLAY -> MediaPlaybackManager.resume()
            ACTION_PAUSE -> MediaPlaybackManager.pause()
            ACTION_NEXT -> MediaPlaybackManager.playNext()
            ACTION_PREV -> MediaPlaybackManager.playPrevious()
            ACTION_SEEK_FWD -> MediaPlaybackManager.seekForward(10000L)
            ACTION_STOP -> {
                MediaPlaybackManager.dismissMiniPlayer()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START, ACTION_UPDATE -> {
                // Refresh notification
            }
        }

        val notification = buildMediaNotification()
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "YouTube Premium Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Phát video và âm thanh trong nền không quảng cáo"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildMediaNotification(): Notification {
        val video = MediaPlaybackManager.currentVideo.value
        val isPlaying = MediaPlaybackManager.isPlaying.value

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Play / Pause Action
        val playPauseActionIntent = Intent(this, BackgroundMediaService::class.java).apply {
            action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        }
        val playPausePendingIntent = PendingIntent.getService(
            this, 1, playPauseActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Prev Action
        val prevActionIntent = Intent(this, BackgroundMediaService::class.java).apply {
            action = ACTION_PREV
        }
        val prevPendingIntent = PendingIntent.getService(
            this, 2, prevActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Next Action
        val nextActionIntent = Intent(this, BackgroundMediaService::class.java).apply {
            action = ACTION_NEXT
        }
        val nextPendingIntent = PendingIntent.getService(
            this, 3, nextActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Seek +10s Action
        val seekFwdIntent = Intent(this, BackgroundMediaService::class.java).apply {
            action = ACTION_SEEK_FWD
        }
        val seekFwdPendingIntent = PendingIntent.getService(
            this, 4, seekFwdIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Close Action
        val stopIntent = Intent(this, BackgroundMediaService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 5, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = video?.title ?: "YouTube Premium"
        val subtitle = "${video?.channelName ?: "Ad-Free"} • VIP Background Play"

        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseTitle = if (isPlaying) "Tạm dừng" else "Phát"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setSubText("YouTube Premium • 0 Ads")
            .setContentIntent(openAppPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .addAction(android.R.drawable.ic_media_previous, "Trước", prevPendingIntent)
            .addAction(playPauseIcon, playPauseTitle, playPausePendingIntent)
            .addAction(android.R.drawable.ic_media_next, "Tiếp", nextPendingIntent)
            .addAction(android.R.drawable.ic_menu_rotate, "+10s", seekFwdPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Đóng", stopPendingIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$subtitle\nPhát trong nền chất lượng cao và không quảng cáo")
            )
            .build()
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "TubePremium::MediaPlaybackWakeLock"
            ).apply {
                acquire(10 * 60 * 1000L /* 10 minutes */)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
        wakeLock = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
