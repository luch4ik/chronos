package com.chronos.alarm

import android.app.Application
import com.chronos.alarm.notification.NotificationChannelManager

class ChronosApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Create notification channels
        NotificationChannelManager.createChannels(this)
    }
}
