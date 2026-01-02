package com.chronos.alarm.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import com.chronos.alarm.domain.model.AlarmAudioConfig

class AudioEngine(private val context: Context) {
    
    private var mediaPlayer: MediaPlayer? = null
    
    fun warmup() {
        // Pre-initialize audio on first user interaction
        // Will be called from MainActivity
    }
    
    fun playAlarm(audioConfig: AlarmAudioConfig) {
        stopAlarm() // Stop any existing alarm
        
        when (audioConfig.source) {
            "GENERATED" -> playGeneratedSound(audioConfig.generatedType ?: "CLASSIC")
            "SYSTEM" -> playSystemSound()
            "URL" -> playUrlAudio(audioConfig.url ?: "")
            "FILE" -> playFileAudio(audioConfig.fileData ?: "")
            else -> playGeneratedSound("CLASSIC") // Fallback
        }
    }
    
    private fun playGeneratedSound(type: String) {
        // For MVP, use default system alarm sound
        // In production, generate actual sounds based on type
        playSystemSound()
    }
    
    private fun playSystemSound() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                isLooping = true
                setAudioStreamType(AudioManager.STREAM_ALARM)
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun playUrlAudio(url: String) {
        if (url.isEmpty()) {
            playSystemSound()
            return
        }
        
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                isLooping = true
                setAudioStreamType(AudioManager.STREAM_ALARM)
                setOnErrorListener { _, _, _ ->
                    // Fallback to system sound
                    playSystemSound()
                    true
                }
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            playSystemSound()
        }
    }
    
    private fun playFileAudio(base64Data: String) {
        if (base64Data.isEmpty()) {
            playSystemSound()
            return
        }
        
        // For MVP, fallback to system sound
        // In production, decode base64 and play file
        playSystemSound()
    }
    
    fun stopAlarm() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
