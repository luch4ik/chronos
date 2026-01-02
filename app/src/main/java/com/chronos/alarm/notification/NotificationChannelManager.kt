package com.chronos.alarm.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannelManager {
    
    const val ALARM_CHANNEL_ID = "alarm_channel"
    const val WATCHDOG_CHANNEL_ID = "watchdog_channel"
    
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Alarm channel
            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for triggered alarms"
                enableVibration(true)
                enableLights(true)
                setBypassDnd(true) // Allow alarms even in Do Not Disturb mode
            }
            
            notificationManager.createNotificationChannel(alarmChannel)
            
            // Watchdog channel
            val watchdogChannel = NotificationChannel(
                WATCHDOG_CHANNEL_ID,
                "Watchdog Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Low priority notification for watchdog service"
                enableVibration(false)
                enableLights(false)
            }
            
            notificationManager.createNotificationChannel(watchdogChannel)
        }
    }
}
