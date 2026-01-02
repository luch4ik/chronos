package com.chronos.alarm.service

import android.content.Context
import android.os.PowerManager

class WakeLockManager(private val context: Context) {
    
    private var wakeLock: PowerManager.WakeLock? = null
    private var screenWakeLock: PowerManager.WakeLock? = null
    
    @Suppress("DEPRECATION")
    fun acquireFullWakeLock() {
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            
            // Partial wake lock: Keep CPU running
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "chronos:alarm_wakelock"
            ).apply {
                acquire(10 * 60 * 1000L) // 10 minutes
            }
            
            // Screen wake lock: Keep screen on and wake device
            screenWakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "chronos:screen_wakelock"
            ).apply {
                acquire(10 * 60 * 1000L)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun releaseWakeLocks() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            screenWakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            wakeLock = null
            screenWakeLock = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
