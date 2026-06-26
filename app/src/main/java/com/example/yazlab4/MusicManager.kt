package com.example.yazlab4

import android.content.Context
import android.media.MediaPlayer

object MusicManager {
    private var mediaPlayer: MediaPlayer? = null

    fun startMusic(context: Context) {
        if (mediaPlayer == null) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    val descriptor = context.assets.openFd("cc_soundtrack1.mp3")
                    setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                    descriptor.close()
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (e: Exception) {
                android.util.Log.e("MusicManager", "Error playing music: ${e.message}")
            }
        } else if (!mediaPlayer!!.isPlaying) {
            mediaPlayer?.start()
        }
    }

    fun pauseMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    fun onActivityStop() {
        // Optional: Add logic to stop music when app is in background
    }
}