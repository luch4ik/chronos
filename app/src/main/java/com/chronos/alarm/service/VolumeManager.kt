package com.chronos.alarm.service

import android.content.Context
import android.media.AudioManager
import android.os.Build
import kotlinx.coroutines.*

class VolumeManager(private val context: Context) {
    
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var monitoringJob: Job? = null
    
    fun setMaxVolume() {
        // Set alarm stream to maximum
        val maxAlarmVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        audioManager.setStreamVolume(
            AudioManager.STREAM_ALARM,
            maxAlarmVolume,
            0 // No UI feedback
        )
        
        // Set system stream to maximum
        val maxSystemVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM)
        audioManager.setStreamVolume(
            AudioManager.STREAM_SYSTEM,
            maxSystemVolume,
            0
        )
        
        // Ensure alarm stream is not muted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_ALARM,
                    AudioManager.ADJUST_UNMUTE,
                    0
                )
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_SYSTEM,
                    AudioManager.ADJUST_UNMUTE,
                    0
                )
            } catch (e: Exception) {
                // Ignore - some devices may not support this
            }
        }
    }
    
    fun startMonitoring(scope: CoroutineScope) {
        monitoringJob = scope.launch {
            while (isActive) {
                monitorVolume()
                delay(5000) // Check every 5 seconds
            }
        }
    }
    
    fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
    }
    
    private fun monitorVolume() {
        try {
            val maxAlarmVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            val currentAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
            
            // Reset if user lowered volume
            if (currentAlarmVolume < maxAlarmVolume) {
                audioManager.setStreamVolume(
                    AudioManager.STREAM_ALARM,
                    maxAlarmVolume,
                    0
                )
            }
        } catch (e: Exception) {
            // Ignore errors
        }
    }
}
