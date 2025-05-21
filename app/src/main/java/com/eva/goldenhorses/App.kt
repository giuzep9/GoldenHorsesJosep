package com.eva.goldenhorses

import android.app.Application
import android.content.Intent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

class App : Application(), DefaultLifecycleObserver {

    override fun onCreate() {
        super<Application>.onCreate()
        // Registrar el observer de lifecycle
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        // App vuelve a primer plano
        val intent = Intent(this, MusicService::class.java).apply {
            action = MusicService.ACTION_UNMUTE
        }
        startService(intent)
    }

    override fun onStop(owner: LifecycleOwner) {
        // App pasa a segundo plano
        val intent = Intent(this, MusicService::class.java).apply {
            action = MusicService.ACTION_MUTE
        }
        startService(intent)
    }
}