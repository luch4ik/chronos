package com.chronos.alarm.service

// Phase 4: Advanced Features - Wake-Up Check Manager
// Schedules delayed notification to verify user is actually awake
// User must confirm within confirmWindow or alarm rings again

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.chronos.alarm.R
import com.chronos.alarm.domain.model.WakeUpCheckConfig
import com.chronos.alarm.ui.screens.alarm.AlarmActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

class WakeUpCheckManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Schedule a wake-up check notification
     * @param alarmId Unique ID for this alarm
     * @param config Wake-up check configuration
     */
    fun scheduleWakeUpCheck(alarmId: String, config: WakeUpCheckConfig) {
        if (!config.enabled) return

        scope.launch {
            // Wait for the check delay
            delay(config.checkDelay * 60 * 1000L) // Convert minutes to milliseconds

            // Show notification asking user to confirm
            showWakeUpCheckNotification(alarmId, config)

            // Schedule confirmation window expiration
            scheduleConfirmationWindowExpiration(alarmId, config.confirmWindow)
        }
    }

    /**
     * Show notification asking user to confirm they're awake
     */
    private fun showWakeUpCheckNotification(alarmId: String, config: WakeUpCheckConfig) {
        val confirmIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("ACTION", "CONFIRM_WAKE_UP")
            putExtra("ALARM_ID", alarmId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val confirmPendingIntent = PendingIntent.getActivity(
            context,
            alarmId.hashCode(),
            confirmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "wake_up_check_channel")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Are You Awake?")
            .setContentText("Tap to confirm you're actually awake!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setOngoing(true)
            .setContentIntent(confirmPendingIntent)
            .addAction(
                R.mipmap.ic_launcher,
                "I'm Awake!",
                confirmPendingIntent
            )
            .build()

        notificationManager.notify(
            WAKE_UP_CHECK_NOTIFICATION_ID_BASE + alarmId.hashCode(),
            notification
        )
    }

    /**
     * Schedule alarm to ring again if user doesn't confirm within window
     */
    private fun scheduleConfirmationWindowExpiration(alarmId: String, confirmWindow: Int) {
        scope.launch {
            // Wait for confirmation window
            delay(confirmWindow * 60 * 1000L)

            // Check if notification was dismissed (user confirmed)
            if (!wasNotificationDismissed(alarmId)) {
                // User didn't confirm - trigger alarm again
                triggerAlarmAgain(alarmId)
            }
        }
    }

    /**
     * Check if wake-up check notification was dismissed
     */
    private fun wasNotificationDismissed(alarmId: String): Boolean {
        // Check if notification is still active
        val activeNotifications = notificationManager.activeNotifications
        val notificationId = WAKE_UP_CHECK_NOTIFICATION_ID_BASE + alarmId.hashCode()

        return activeNotifications.none { it.id == notificationId }
    }

    /**
     * Trigger alarm again if user didn't confirm wake-up
     */
    private fun triggerAlarmAgain(alarmId: String) {
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("ACTION", "WAKE_UP_CHECK_FAILED")
            putExtra("ALARM_ID", alarmId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            alarmId.hashCode() + 1000,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + 1000 // Trigger in 1 second

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    /**
     * User confirmed they're awake - cancel any pending checks
     */
    fun confirmWakeUp(alarmId: String) {
        // Cancel notification
        val notificationId = WAKE_UP_CHECK_NOTIFICATION_ID_BASE + alarmId.hashCode()
        notificationManager.cancel(notificationId)

        // User confirmed successfully - no need to trigger alarm again
    }

    /**
     * Cancel all wake-up checks for an alarm
     */
    fun cancelWakeUpCheck(alarmId: String) {
        val notificationId = WAKE_UP_CHECK_NOTIFICATION_ID_BASE + alarmId.hashCode()
        notificationManager.cancel(notificationId)
    }

    companion object {
        private const val WAKE_UP_CHECK_NOTIFICATION_ID_BASE = 20000
    }
}
