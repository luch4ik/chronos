package com.chronos.alarm.domain.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.chronos.alarm.domain.model.Alarm
import java.util.*

class AlarmScheduler(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    fun scheduleAlarm(alarm: Alarm) {
        if (!alarm.isActive) return
        
        val triggerTime = calculateNextTriggerTime(alarm)
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
            putExtra(EXTRA_ALARM_ID, alarm.id)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Use exact alarm for precise timing
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ requires canScheduleExactAlarms permission
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    // Fallback to inexact alarm
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback for older Android versions or restricted devices
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
    
    fun cancelAlarm(alarmId: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        alarmManager.cancel(pendingIntent)
    }
    
    private fun calculateNextTriggerTime(alarm: Alarm): Long {
        val now = Calendar.getInstance()
        val timeParts = alarm.time.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()
        
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        return if (alarm.days.isEmpty()) {
            // One-time alarm
            if (target <= now) {
                target.add(Calendar.DAY_OF_MONTH, 1)
            }
            target.timeInMillis
        } else {
            // Recurring alarm
            val currentDay = now.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday
            
            if (alarm.days.contains(currentDay) && target > now) {
                // Today
                target.timeInMillis
            } else {
                // Find next valid day
                val sortedDays = alarm.days.sorted()
                var nextDay = sortedDays.find { it > currentDay }
                
                if (nextDay == null) {
                    // Wrap around to next week
                    nextDay = sortedDays[0]
                    val daysToAdd = 7 - currentDay + nextDay
                    target.add(Calendar.DAY_OF_MONTH, daysToAdd)
                } else {
                    // This week
                    val daysToAdd = nextDay - currentDay
                    target.add(Calendar.DAY_OF_MONTH, daysToAdd)
                }
                
                target.timeInMillis
            }
        }
    }
    
    companion object {
        const val ACTION_ALARM_TRIGGER = "com.chronos.alarm.ACTION_ALARM_TRIGGER"
        const val EXTRA_ALARM_ID = "alarm_id"
    }
}
