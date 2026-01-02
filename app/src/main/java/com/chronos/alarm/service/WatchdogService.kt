package com.chronos.alarm.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.chronos.alarm.notification.NotificationChannelManager

class WatchdogService : Service() {
    
    private val binder = LocalBinder()
    private val handler = Handler(Looper.getMainLooper())
    
    inner class LocalBinder : Binder() {
        fun getService(): WatchdogService = this@WatchdogService
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        scheduleWatchdog()
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
    
    private fun scheduleWatchdog() {
        val runnable = object : Runnable {
            override fun run() {
                checkAlarmService()
                handler.postDelayed(this, CHECK_INTERVAL)
            }
        }
        handler.post(runnable)
    }
    
    private fun checkAlarmService() {
        // In production, would check if AlarmForegroundService is running
        // and restart if necessary
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, NotificationChannelManager.WATCHDOG_CHANNEL_ID)
            .setContentTitle("Chronos Watchdog")
            .setContentText("Monitoring alarm service")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
    
    companion object {
        private const val NOTIFICATION_ID = 1002
        private const val CHECK_INTERVAL = 30000L // 30 seconds
    }
}
