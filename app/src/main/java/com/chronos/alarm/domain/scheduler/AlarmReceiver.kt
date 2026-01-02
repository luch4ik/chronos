package com.chronos.alarm.domain.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.chronos.alarm.data.local.database.ChronosDatabase
import com.chronos.alarm.data.local.preferences.SettingsDataStore
import com.chronos.alarm.data.repository.AlarmRepository
import com.chronos.alarm.service.AlarmForegroundService
import com.chronos.alarm.service.WatchdogService
import com.chronos.alarm.ui.screens.alarm.AlarmActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> {
                handleBoot(context)
            }
            AlarmScheduler.ACTION_ALARM_TRIGGER -> {
                val alarmId = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID)
                if (alarmId != null) {
                    handleAlarmTrigger(context, alarmId)
                }
            }
        }
    }
    
    private fun handleBoot(context: Context) {
        scope.launch {
            try {
                // Initialize repository
                val database = ChronosDatabase.getDatabase(context)
                val settingsDataStore = SettingsDataStore(context)
                val repository = AlarmRepository(database.alarmDao(), settingsDataStore)
                
                // Reschedule all active alarms
                val alarms = repository.getActiveAlarms().first()
                val scheduler = AlarmScheduler(context)
                alarms.forEach { alarm ->
                    scheduler.scheduleAlarm(alarm)
                }
                
                // Start watchdog service
                val watchdogIntent = Intent(context, WatchdogService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(watchdogIntent)
                } else {
                    context.startService(watchdogIntent)
                }
            } catch (e: Exception) {
                // Log error - in production would use proper logging
                e.printStackTrace()
            }
        }
    }
    
    private fun handleAlarmTrigger(context: Context, alarmId: String) {
        scope.launch {
            try {
                // Initialize repository
                val database = ChronosDatabase.getDatabase(context)
                val settingsDataStore = SettingsDataStore(context)
                val repository = AlarmRepository(database.alarmDao(), settingsDataStore)
                
                val alarm = repository.getAlarmById(alarmId)
                
                if (alarm == null || !alarm.isActive) {
                    return@launch
                }
                
                // Check if alarm should fire today
                if (alarm.days.isNotEmpty()) {
                    val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
                    if (!alarm.days.contains(today)) {
                        return@launch
                    }
                }
                
                // Launch AlarmActivity
                val alarmIntent = AlarmActivity.createIntent(context, alarmId)
                context.startActivity(alarmIntent)
                
                // Start foreground service with alarm
                val serviceIntent = Intent(context, AlarmForegroundService::class.java).apply {
                    putExtra("alarm_id", alarmId)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                
                // Reschedule if recurring alarm
                if (alarm.days.isNotEmpty()) {
                    val scheduler = AlarmScheduler(context)
                    scheduler.scheduleAlarm(alarm)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
