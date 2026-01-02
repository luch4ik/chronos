package com.chronos.alarm.service

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticManager(private val context: Context) {
    
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
    
    fun vibrate(pattern: LongArray) {
        vibrator?.let {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(pattern, -1)
                    it.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(pattern, -1)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun vibrateLight() {
        vibrate(longArrayOf(0, 10))
    }
    
    fun vibrateMedium() {
        vibrate(longArrayOf(0, 40))
    }
    
    fun vibrateHeavy() {
        vibrate(longArrayOf(0, 70))
    }
    
    fun vibrateSuccess() {
        vibrate(longArrayOf(0, 50, 50, 100))
    }
    
    fun vibrateError() {
        vibrate(longArrayOf(0, 50, 100, 50, 100))
    }
    
    fun cancel() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
