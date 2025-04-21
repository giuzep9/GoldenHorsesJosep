package com.eva.goldenhorses

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.widget.Toast

class MusicService : Service() {
    companion object {
        const val ACTION_PLAY_CUSTOM = "ACTION_PLAY_CUSTOM"
        const val ACTION_MUTE = "MUTE"
        const val ACTION_UNMUTE = "UNMUTE"
        const val ACTION_CHANGE_MUSIC = "CHANGE_MUSIC"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var isMuted = false
    private var currentUri: Uri? = null

    override fun onCreate() {
        super.onCreate()
        playDefaultMusic()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "MUTE" -> {
                isMuted = true
                mediaPlayer?.setVolume(0f, 0f)
            }

            "UNMUTE" -> {
                isMuted = false
                mediaPlayer?.setVolume(1f, 1f)
            }

            "CHANGE_MUSIC" -> {
                val musicUri = intent.getStringExtra("MUSIC_URI")
                if (musicUri == "DEFAULT") {
                    playDefaultMusic()
                    Toast.makeText(this, "Música por defecto activada", Toast.LENGTH_SHORT).show()
                } else {
                    changeMusic(Uri.parse(musicUri))
                    Toast.makeText(this, "Música personalizada cargada", Toast.LENGTH_SHORT).show()
                }
            }

            else -> {
                if (mediaPlayer?.isPlaying == false) {
                    mediaPlayer?.start()
                }
            }
        }
        return START_STICKY
    }

    private fun playDefaultMusic() {
        currentUri = null
        stopMusic()
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music)
        startMusic()
    }

    private fun changeMusic(uri: Uri) {
        currentUri = uri
        stopMusic()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@MusicService, uri)
                prepare()
            }
            startMusic()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al reproducir el archivo seleccionado", Toast.LENGTH_SHORT).show()
            playDefaultMusic()
        }
    }

    private fun startMusic() {
        mediaPlayer?.apply {
            isLooping = true
            setVolume(if (isMuted) 0f else 1f, if (isMuted) 0f else 1f)
            start()
        }
    }

    private fun stopMusic() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
    }

    override fun onDestroy() {
        stopMusic()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}