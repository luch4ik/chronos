package com.chronos.alarm.protection

// Permission Request Helper - Guided Permission Setup
// Helps users grant all critical permissions for alarm reliability

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

class PermissionRequestHelper(private val context: Context) {

    /**
     * Get list of all required permissions for full alarm functionality
     */
    fun getAllRequiredPermissions(): List<PermissionInfo> {
        val permissions = mutableListOf<PermissionInfo>()

        // 1. Exact Alarm Permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(
                PermissionInfo(
                    permission = Manifest.permission.SCHEDULE_EXACT_ALARM,
                    title = "Exact Alarm Scheduling",
                    description = "Required for alarms to ring at precise times",
                    criticality = PermissionCriticality.CRITICAL,
                    settingsAction = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    isSpecialPermission = true
                )
            )
        }

        // 2. Post Notifications (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(
                PermissionInfo(
                    permission = Manifest.permission.POST_NOTIFICATIONS,
                    title = "Notifications",
                    description = "Required to show alarm notifications",
                    criticality = PermissionCriticality.CRITICAL,
                    settingsAction = null,
                    isSpecialPermission = false
                )
            )
        }

        // 3. Battery Optimization Exemption (Android 6+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissions.add(
                PermissionInfo(
                    permission = "BATTERY_OPTIMIZATION_EXEMPT",
                    title = "Battery Optimization Exemption",
                    description = "Prevents Android from killing alarm in battery saver mode",
                    criticality = PermissionCriticality.CRITICAL,
                    settingsAction = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    isSpecialPermission = true,
                    settingsDataUri = "package:${context.packageName}"
                )
            )
        }

        // 4. Do Not Disturb Override (Android 6+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissions.add(
                PermissionInfo(
                    permission = "DND_OVERRIDE",
                    title = "Do Not Disturb Override",
                    description = "Allows alarm to ring even when DND is enabled",
                    criticality = PermissionCriticality.HIGH,
                    settingsAction = Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS,
                    isSpecialPermission = true
                )
            )
        }

        // 5. Location (for Velocity Challenge)
        permissions.add(
            PermissionInfo(
                permission = Manifest.permission.ACCESS_FINE_LOCATION,
                title = "Location Access",
                description = "Required for Velocity Challenge (GPS speed tracking)",
                criticality = PermissionCriticality.MEDIUM,
                settingsAction = null,
                isSpecialPermission = false
            )
        )

        // 6. Bluetooth (for Bluetooth Challenge)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(
                PermissionInfo(
                    permission = Manifest.permission.BLUETOOTH_CONNECT,
                    title = "Bluetooth",
                    description = "Required for Bluetooth Challenge",
                    criticality = PermissionCriticality.MEDIUM,
                    settingsAction = null,
                    isSpecialPermission = false
                )
            )
        }

        // 7. SMS (for Emergency Contact)
        permissions.add(
            PermissionInfo(
                permission = Manifest.permission.SEND_SMS,
                title = "Send SMS",
                description = "Required for Emergency Contact SMS feature",
                criticality = PermissionCriticality.LOW,
                settingsAction = null,
                isSpecialPermission = false
            )
        )

        // 8. Phone Call (for Emergency Contact)
        permissions.add(
            PermissionInfo(
                permission = Manifest.permission.CALL_PHONE,
                title = "Make Phone Calls",
                description = "Required for Emergency Contact call feature",
                criticality = PermissionCriticality.LOW,
                settingsAction = null,
                isSpecialPermission = false
            )
        )

        return permissions
    }

    /**
     * Check which permissions are granted
     */
    fun checkPermissionStatus(): PermissionCheckResult {
        val all = getAllRequiredPermissions()
        val granted = all.filter { isPermissionGranted(it) }
        val denied = all.filter { !isPermissionGranted(it) }

        val criticalDenied = denied.filter { it.criticality == PermissionCriticality.CRITICAL }
        val highDenied = denied.filter { it.criticality == PermissionCriticality.HIGH }

        return PermissionCheckResult(
            totalPermissions = all.size,
            grantedPermissions = granted.size,
            deniedPermissions = denied.size,
            granted = granted,
            denied = denied,
            hasAllCriticalPermissions = criticalDenied.isEmpty(),
            criticalDenied = criticalDenied,
            highDenied = highDenied
        )
    }

    /**
     * Check if a specific permission is granted
     */
    private fun isPermissionGranted(permissionInfo: PermissionInfo): Boolean {
        return when (permissionInfo.permission) {
            "BATTERY_OPTIMIZATION_EXEMPT" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
                    powerManager.isIgnoringBatteryOptimizations(context.packageName)
                } else {
                    true
                }
            }
            "DND_OVERRIDE" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                        as android.app.NotificationManager
                    notificationManager.isNotificationPolicyAccessGranted
                } else {
                    true
                }
            }
            Manifest.permission.SCHEDULE_EXACT_ALARM -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                    alarmManager.canScheduleExactAlarms()
                } else {
                    true
                }
            }
            else -> {
                ContextCompat.checkSelfPermission(
                    context,
                    permissionInfo.permission
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    /**
     * Request a specific permission
     */
    fun requestPermission(permissionInfo: PermissionInfo, activity: Activity) {
        if (permissionInfo.isSpecialPermission) {
            // Special permissions need to be requested via Settings
            val intent = Intent(permissionInfo.settingsAction).apply {
                if (permissionInfo.settingsDataUri != null) {
                    data = Uri.parse(permissionInfo.settingsDataUri)
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            try {
                activity.startActivity(intent)
            } catch (e: Exception) {
                // Fallback to app settings
                openAppSettings(activity)
            }
        } else {
            // Regular runtime permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(arrayOf(permissionInfo.permission), 1000)
            }
        }
    }

    /**
     * Open app settings page
     */
    fun openAppSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            activity.startActivity(intent)
        } catch (e: Exception) {
            // Cannot open settings
        }
    }

    /**
     * Get permission setup guide text
     */
    fun getSetupGuide(): String {
        val status = checkPermissionStatus()

        if (status.hasAllCriticalPermissions) {
            return "✅ All critical permissions granted! Your alarm is protected."
        }

        val builder = StringBuilder()
        builder.appendLine("⚠️ PERMISSION SETUP REQUIRED")
        builder.appendLine()
        builder.appendLine("${status.deniedPermissions.size} permissions need to be granted for full alarm protection:")
        builder.appendLine()

        status.criticalDenied.forEach { permission ->
            builder.appendLine("🔴 CRITICAL: ${permission.title}")
            builder.appendLine("   ${permission.description}")
            builder.appendLine()
        }

        status.highDenied.forEach { permission ->
            builder.appendLine("🟠 HIGH: ${permission.title}")
            builder.appendLine("   ${permission.description}")
            builder.appendLine()
        }

        val mediumLowDenied = status.denied.filter {
            it.criticality != PermissionCriticality.CRITICAL &&
            it.criticality != PermissionCriticality.HIGH
        }

        if (mediumLowDenied.isNotEmpty()) {
            builder.appendLine("Optional permissions:")
            mediumLowDenied.forEach { permission ->
                builder.appendLine("  - ${permission.title}")
            }
        }

        return builder.toString()
    }
}

/**
 * Information about a permission
 */
data class PermissionInfo(
    val permission: String,
    val title: String,
    val description: String,
    val criticality: PermissionCriticality,
    val settingsAction: String?,
    val isSpecialPermission: Boolean,
    val settingsDataUri: String? = null
)

/**
 * Permission check result
 */
data class PermissionCheckResult(
    val totalPermissions: Int,
    val grantedPermissions: Int,
    val deniedPermissions: Int,
    val granted: List<PermissionInfo>,
    val denied: List<PermissionInfo>,
    val hasAllCriticalPermissions: Boolean,
    val criticalDenied: List<PermissionInfo>,
    val highDenied: List<PermissionInfo>
)

/**
 * Permission criticality levels
 */
enum class PermissionCriticality {
    CRITICAL,   // Alarm won't work without this
    HIGH,       // Alarm may not work reliably
    MEDIUM,     // Some features won't work
    LOW         // Optional feature
}
