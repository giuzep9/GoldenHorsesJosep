package com.eva.goldenhorses

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder

class MusicService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var isMuted = false

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music) // Replace with your MP3 file name
        mediaPlayer?.isLooping = true // Loop the music
        mediaPlayer?.setVolume(1.0f, 1.0f) // Set initial volume to max
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "MUTE" -> {
                isMuted = true
                mediaPlayer?.setVolume(0f, 0f) // Mute music
            }
            "UNMUTE" -> {
                isMuted = false
                mediaPlayer?.setVolume(1.0f, 1.0f) // Restore volume
            }
            else -> {
                if (mediaPlayer?.isPlaying == false) {
                    mediaPlayer?.start() // Start playing if not already playing
                }
            }
        }
        return START_STICKY // Keep service running
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }
}