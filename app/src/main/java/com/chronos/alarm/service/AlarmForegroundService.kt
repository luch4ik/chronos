package com.chronos.alarm.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.chronos.alarm.R
import com.chronos.alarm.data.local.database.ChronosDatabase
import com.chronos.alarm.data.local.preferences.SettingsDataStore
import com.chronos.alarm.data.repository.AlarmRepository
import com.chronos.alarm.notification.NotificationChannelManager
import com.chronos.alarm.ui.screens.alarm.AlarmActivity
import kotlinx.coroutines.*

class AlarmForegroundService : Service() {
    
    private val binder = LocalBinder()
    private var currentAlarmId: String? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private lateinit var audioEngine: AudioEngine
    private lateinit var volumeManager: VolumeManager
    private lateinit var wakeLockManager: WakeLockManager
    private lateinit var hapticManager: HapticManager
    
    inner class LocalBinder : Binder() {
        fun getService(): AlarmForegroundService = this@AlarmForegroundService
    }
    
    override fun onCreate() {
        super.onCreate()
        audioEngine = AudioEngine(this)
        volumeManager = VolumeManager(this)
        wakeLockManager = WakeLockManager(this)
        hapticManager = HapticManager(this)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getStringExtra("alarm_id")
        if (alarmId != null) {
            currentAlarmId = alarmId
            serviceScope.launch {
                startAlarm(alarmId)
            }
        }
        
        // START_STICKY: System will restart if killed
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
        serviceScope.cancel()
    }
    
    private suspend fun startAlarm(alarmId: String) {
        try {
            val database = ChronosDatabase.getDatabase(this)
            val settingsDataStore = SettingsDataStore(this)
            val repository = AlarmRepository(database.alarmDao(), settingsDataStore)
            
            val alarm = repository.getAlarmById(alarmId) ?: return
            
            // Acquire wake locks
            wakeLockManager.acquireFullWakeLock()
            
            // Override volume
            volumeManager.setMaxVolume()
            
            // Start volume monitoring
            volumeManager.startMonitoring(serviceScope)
            
            // Play alarm sound
            audioEngine.playAlarm(alarm.audio)
            
            // Haptic feedback
            hapticManager.vibrate(pattern = longArrayOf(0, 500, 200, 500))
            
            // Start foreground notification
            startForeground(NOTIFICATION_ID, createNotification(alarm.time, alarm.label))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun stopAlarm() {
        audioEngine.stopAlarm()
        volumeManager.stopMonitoring()
        wakeLockManager.releaseWakeLocks()
        hapticManager.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private fun createNotification(time: String, label: String): Notification {
        val intent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("alarm_id", currentAlarmId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        return NotificationCompat.Builder(this, NotificationChannelManager.ALARM_CHANNEL_ID)
            .setContentTitle("Alarm: $time")
            .setContentText("$label - Complete challenges to dismiss")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Using system icon for MVP
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setFullScreenIntent(pendingIntent, true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .build()
    }
    
    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}
