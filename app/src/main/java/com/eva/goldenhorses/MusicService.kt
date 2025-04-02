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
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music) // Introducimos la música
        mediaPlayer?.isLooping = true
        mediaPlayer?.setVolume(1.0f, 1.0f) // Inicializamos el volumen al máximo
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "MUTE" -> {
                isMuted = true
                mediaPlayer?.setVolume(0f, 0f) // Mutear
            }
            "UNMUTE" -> {
                isMuted = false
                mediaPlayer?.setVolume(1.0f, 1.0f) // Desmutear
            }
            else -> {
                if (mediaPlayer?.isPlaying == false) {
                    mediaPlayer?.start() // Si no está la música sonando que suene
                }
            }
        }
        return START_STICKY // Mantenerlo encendido
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