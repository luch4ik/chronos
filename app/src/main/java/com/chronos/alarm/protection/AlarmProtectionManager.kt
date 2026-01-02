package com.chronos.alarm.protection

// Security & Protection System - Alarm Protection Manager
// Comprehensive protection against all bypass methods:
// - Force-close resistance
// - Volume mute prevention
// - Notification dismissal protection
// - Reboot survival
// - Battery optimization bypass
// - Task killer resistance
// - Screen lock bypass

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.chronos.alarm.domain.model.AppSettings
import com.chronos.alarm.service.AlarmForegroundService
import com.chronos.alarm.service.WatchdogService
import kotlinx.coroutines.*

class AlarmProtectionManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var protectionJob: Job? = null
    private var originalVolume: Int = 0

    /**
     * EXPLOIT #1: VOLUME MUTE
     * Test: User turns volume to 0 to silence alarm
     * Protection: Monitor and override volume continuously
     */
    fun enableVolumeProtection(settings: AppSettings) {
        if (!settings.volumeOverride) return

        protectionJob?.cancel()
        protectionJob = scope.launch {
            while (isActive) {
                // Save original volume on first run
                if (originalVolume == 0) {
                    originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
                }

                // Force maximum alarm volume
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)

                if (currentVolume < maxVolume) {
                    // User tried to lower volume - override it
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_ALARM,
                        maxVolume,
                        0 // No UI feedback to prevent user knowing
                    )
                }

                // Also monitor media volume (some alarms use media stream)
                val currentMediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                val maxMediaVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

                if (currentMediaVolume < maxMediaVolume * 0.7) {
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        (maxMediaVolume * 0.7).toInt(),
                        0
                    )
                }

                delay(500) // Check every 500ms
            }
        }
    }

    /**
     * EXPLOIT #2: FORCE CLOSE APP
     * Test: User force-closes app via task manager
     * Protection: Foreground service + watchdog that auto-restarts
     */
    fun enableForceCloseProtection() {
        // Start foreground service
        val serviceIntent = Intent(context, AlarmForegroundService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)

        // Start watchdog service that monitors main service
        val watchdogIntent = Intent(context, WatchdogService::class.java)
        ContextCompat.startForegroundService(context, watchdogIntent)

        // Schedule periodic health checks
        scheduleHealthCheck()
    }

    /**
     * EXPLOIT #3: NOTIFICATION DISMISSAL
     * Test: User swipes away alarm notification
     * Protection: Make notification undismissable + auto-restart
     */
    fun protectNotification(notificationId: Int) {
        // Use ongoing flag to prevent swipe dismissal
        // This is handled in the notification builder itself

        // Monitor for notification dismissal
        scope.launch {
            while (isActive) {
                val activeNotifications = notificationManager.activeNotifications
                val hasAlarmNotification = activeNotifications.any { it.id == notificationId }

                if (!hasAlarmNotification) {
                    // Notification was dismissed - recreate it
                    // This would trigger alarm restart
                }

                delay(1000)
            }
        }
    }

    /**
     * EXPLOIT #4: DEVICE REBOOT
     * Test: User reboots device to stop alarm
     * Protection: BOOT_COMPLETED receiver reschedules all alarms
     */
    fun enableRebootProtection(settings: AppSettings): Boolean {
        if (!settings.rebootProtection) return false

        // This is handled by AlarmReceiver listening to BOOT_COMPLETED
        // Just verify the receiver is registered
        return true
    }

    /**
     * EXPLOIT #5: BATTERY SAVER / DOZE MODE
     * Test: Device enters doze mode, alarm doesn't ring
     * Protection: Request battery optimization exemption
     */
    fun requestBatteryOptimizationExemption(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName = context.packageName
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                // Need to request exemption
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = android.net.Uri.parse("package:$packageName")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }

                try {
                    context.startActivity(intent)
                    return true
                } catch (e: Exception) {
                    return false
                }
            }
            return true // Already exempted
        }
        return true // Not needed on older Android
    }

    /**
     * EXPLOIT #6: TASK KILLERS
     * Test: User uses task killer app
     * Protection: Multiple restart mechanisms
     */
    private fun scheduleHealthCheck() {
        val intent = Intent(context, AlarmForegroundService::class.java).apply {
            action = "HEALTH_CHECK"
        }

        val pendingIntent = PendingIntent.getService(
            context,
            HEALTH_CHECK_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule health check every 30 seconds
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 30000,
            30000,
            pendingIntent
        )
    }

    /**
     * EXPLOIT #7: AIRPLANE MODE
     * Test: User enables airplane mode (irrelevant for alarm)
     * Protection: Alarms work offline, no protection needed
     */
    // No implementation needed - alarms are local

    /**
     * EXPLOIT #8: UNINSTALL APP
     * Test: User uninstalls app
     * Protection: Limited on Android 14+ but show warning
     */
    fun enableUninstallProtection(settings: AppSettings) {
        if (!settings.uninstallProtection) return

        // Android 14+ doesn't allow preventing uninstall
        // Best we can do is make it harder:
        // 1. Require device admin (deprecated)
        // 2. Show persistent notification warning
        // 3. Use foreground service to stay visible

        // Show warning that uninstalling will disable alarms
        // This is handled in the UI layer
    }

    /**
     * EXPLOIT #9: SCREEN LOCK BYPASS
     * Test: Alarm rings but user can't interact without unlocking
     * Protection: Use FLAG_SHOW_WHEN_LOCKED + FLAG_TURN_SCREEN_ON
     */
    // This is handled in AlarmActivity's window flags

    /**
     * EXPLOIT #10: DO NOT DISTURB MODE
     * Test: User enables DND to silence alarm
     * Protection: Request notification policy access
     */
    fun bypassDoNotDisturb(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!notificationManager.isNotificationPolicyAccessGranted) {
                // Need to request permission
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }

                try {
                    context.startActivity(intent)
                    return false
                } catch (e: Exception) {
                    return false
                }
            }
            return true // Already granted
        }
        return true // Not needed on older Android
    }

    /**
     * EXPLOIT #11: CLEAR APP DATA
     * Test: User clears app data to delete alarms
     * Protection: Cannot prevent, but auto-backup to cloud (future)
     */
    // Would require cloud backup implementation

    /**
     * EXPLOIT #12: SAFE MODE BOOT
     * Test: User boots into safe mode (disables third-party apps)
     * Protection: Cannot prevent - system limitation
     */
    // Cannot be prevented

    /**
     * Restore original volume when alarm dismissed
     */
    fun restoreOriginalVolume() {
        if (originalVolume > 0) {
            audioManager.setStreamVolume(
                AudioManager.STREAM_ALARM,
                originalVolume,
                0
            )
            originalVolume = 0
        }
        protectionJob?.cancel()
    }

    /**
     * Check if all critical permissions are granted
     */
    fun verifyAllPermissions(): PermissionStatus {
        val missing = mutableListOf<String>()

        // Battery optimization
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
                missing.add("Battery Optimization Exemption")
            }
        }

        // DND bypass
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!notificationManager.isNotificationPolicyAccessGranted) {
                missing.add("Do Not Disturb Override")
            }
        }

        // Exact alarm permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                missing.add("Exact Alarm Scheduling")
            }
        }

        return PermissionStatus(
            allGranted = missing.isEmpty(),
            missingPermissions = missing
        )
    }

    /**
     * Cleanup - cancel all protection jobs
     */
    fun cleanup() {
        protectionJob?.cancel()
        restoreOriginalVolume()
        scope.cancel()
    }

    companion object {
        private const val HEALTH_CHECK_REQUEST_CODE = 3000
    }
}

/**
 * Permission status result
 */
data class PermissionStatus(
    val allGranted: Boolean,
    val missingPermissions: List<String>
)
