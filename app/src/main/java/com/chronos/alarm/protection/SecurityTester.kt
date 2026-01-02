package com.chronos.alarm.protection

// Security Testing Suite - Comprehensive Exploit Testing
// Tests all known methods users might use to bypass the alarm

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.chronos.alarm.service.AlarmForegroundService
import kotlinx.coroutines.*

class SecurityTester(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val notificationManager = NotificationManagerCompat.from(context)

    /**
     * Run all security tests and return detailed report
     */
    suspend fun runFullSecurityAudit(): SecurityAuditReport = withContext(Dispatchers.Default) {
        val tests = mutableListOf<SecurityTestResult>()

        // Test 1: Volume Mute Exploit
        tests.add(testVolumeMuteExploit())

        // Test 2: Force Close Exploit
        tests.add(testForceCloseResistance())

        // Test 3: Notification Dismissal
        tests.add(testNotificationProtection())

        // Test 4: Battery Optimization
        tests.add(testBatteryOptimization())

        // Test 5: Do Not Disturb
        tests.add(testDoNotDisturbBypass())

        // Test 6: Exact Alarm Permission
        tests.add(testExactAlarmPermission())

        // Test 7: Permission Completeness
        tests.add(testAllPermissions())

        val passed = tests.count { it.passed }
        val failed = tests.count { !it.passed }

        SecurityAuditReport(
            totalTests = tests.size,
            passed = passed,
            failed = failed,
            tests = tests,
            overallSecurityScore = (passed.toFloat() / tests.size * 100).toInt()
        )
    }

    /**
     * TEST #1: Volume Mute Exploit
     * Simulates user lowering volume to 0
     */
    private suspend fun testVolumeMuteExploit(): SecurityTestResult {
        val testName = "Volume Mute Protection"
        val originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)

        return try {
            // Simulate user muting volume
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0)
            delay(100)

            val mutedVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)

            // With protection enabled, volume should be restored
            // For testing, we check if we CAN mute (vulnerability exists if true)
            val canMute = (mutedVolume == 0)

            // Restore original volume
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0)

            if (canMute) {
                SecurityTestResult(
                    testName = testName,
                    passed = false,
                    vulnerability = "Volume can be muted to 0 - alarm will be silent",
                    recommendation = "Enable VolumeProtection in AlarmProtectionManager",
                    severity = SecuritySeverity.CRITICAL
                )
            } else {
                SecurityTestResult(
                    testName = testName,
                    passed = true,
                    vulnerability = null,
                    recommendation = null,
                    severity = SecuritySeverity.SAFE
                )
            }
        } catch (e: Exception) {
            SecurityTestResult(
                testName = testName,
                passed = false,
                vulnerability = "Test failed: ${e.message}",
                recommendation = "Fix volume permission handling",
                severity = SecuritySeverity.CRITICAL
            )
        }
    }

    /**
     * TEST #2: Force Close Resistance
     * Checks if foreground service is running
     */
    private fun testForceCloseResistance(): SecurityTestResult {
        val testName = "Force Close Resistance"

        val serviceRunning = isServiceRunning(AlarmForegroundService::class.java)

        return if (serviceRunning) {
            SecurityTestResult(
                testName = testName,
                passed = true,
                vulnerability = null,
                recommendation = null,
                severity = SecuritySeverity.SAFE
            )
        } else {
            SecurityTestResult(
                testName = testName,
                passed = false,
                vulnerability = "Foreground service not running - app can be easily killed",
                recommendation = "Start AlarmForegroundService when alarm is set",
                severity = SecuritySeverity.HIGH
            )
        }
    }

    /**
     * TEST #3: Notification Protection
     * Checks if notifications are properly configured
     */
    private fun testNotificationProtection(): SecurityTestResult {
        val testName = "Notification Protection"

        val notificationsEnabled = notificationManager.areNotificationsEnabled()

        return if (notificationsEnabled) {
            SecurityTestResult(
                testName = testName,
                passed = true,
                vulnerability = null,
                recommendation = null,
                severity = SecuritySeverity.SAFE
            )
        } else {
            SecurityTestResult(
                testName = testName,
                passed = false,
                vulnerability = "Notifications are disabled - alarm won't show",
                recommendation = "Request notification permission in settings",
                severity = SecuritySeverity.CRITICAL
            )
        }
    }

    /**
     * TEST #4: Battery Optimization
     * Checks if app is exempt from battery optimization
     */
    private fun testBatteryOptimization(): SecurityTestResult {
        val testName = "Battery Optimization Exemption"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val isExempt = powerManager.isIgnoringBatteryOptimizations(context.packageName)

            if (isExempt) {
                SecurityTestResult(
                    testName = testName,
                    passed = true,
                    vulnerability = null,
                    recommendation = null,
                    severity = SecuritySeverity.SAFE
                )
            } else {
                SecurityTestResult(
                    testName = testName,
                    passed = false,
                    vulnerability = "Battery optimization enabled - alarm may not ring in doze mode",
                    recommendation = "Request battery optimization exemption",
                    severity = SecuritySeverity.HIGH
                )
            }
        } else {
            SecurityTestResult(
                testName = testName,
                passed = true,
                vulnerability = null,
                recommendation = "Not applicable on Android < M",
                severity = SecuritySeverity.SAFE
            )
        }
    }

    /**
     * TEST #5: Do Not Disturb Bypass
     * Checks if app can bypass DND
     */
    private fun testDoNotDisturbBypass(): SecurityTestResult {
        val testName = "Do Not Disturb Bypass"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as android.app.NotificationManager
            val canBypass = notificationManager.isNotificationPolicyAccessGranted

            if (canBypass) {
                SecurityTestResult(
                    testName = testName,
                    passed = true,
                    vulnerability = null,
                    recommendation = null,
                    severity = SecuritySeverity.SAFE
                )
            } else {
                SecurityTestResult(
                    testName = testName,
                    passed = false,
                    vulnerability = "Cannot bypass DND - alarm may be silenced",
                    recommendation = "Request notification policy access",
                    severity = SecuritySeverity.MEDIUM
                )
            }
        } else {
            SecurityTestResult(
                testName = testName,
                passed = true,
                vulnerability = null,
                recommendation = "Not applicable on Android < M",
                severity = SecuritySeverity.SAFE
            )
        }
    }

    /**
     * TEST #6: Exact Alarm Permission
     * Checks if app can schedule exact alarms
     */
    private fun testExactAlarmPermission(): SecurityTestResult {
        val testName = "Exact Alarm Permission"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val canSchedule = alarmManager.canScheduleExactAlarms()

            if (canSchedule) {
                SecurityTestResult(
                    testName = testName,
                    passed = true,
                    vulnerability = null,
                    recommendation = null,
                    severity = SecuritySeverity.SAFE
                )
            } else {
                SecurityTestResult(
                    testName = testName,
                    passed = false,
                    vulnerability = "Cannot schedule exact alarms - timing will be inaccurate",
                    recommendation = "Request SCHEDULE_EXACT_ALARM permission",
                    severity = SecuritySeverity.CRITICAL
                )
            }
        } else {
            SecurityTestResult(
                testName = testName,
                passed = true,
                vulnerability = null,
                recommendation = "Not applicable on Android < S",
                severity = SecuritySeverity.SAFE
            )
        }
    }

    /**
     * TEST #7: All Required Permissions
     * Comprehensive permission check
     */
    private fun testAllPermissions(): SecurityTestResult {
        val testName = "Permission Completeness"
        val missingPermissions = mutableListOf<String>()

        // Check notification permission
        if (!notificationManager.areNotificationsEnabled()) {
            missingPermissions.add("POST_NOTIFICATIONS")
        }

        // Check battery optimization
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
                missingPermissions.add("IGNORE_BATTERY_OPTIMIZATIONS")
            }
        }

        // Check exact alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                missingPermissions.add("SCHEDULE_EXACT_ALARM")
            }
        }

        // Check DND access
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as android.app.NotificationManager
            if (!notificationManager.isNotificationPolicyAccessGranted) {
                missingPermissions.add("NOTIFICATION_POLICY_ACCESS")
            }
        }

        return if (missingPermissions.isEmpty()) {
            SecurityTestResult(
                testName = testName,
                passed = true,
                vulnerability = null,
                recommendation = null,
                severity = SecuritySeverity.SAFE
            )
        } else {
            SecurityTestResult(
                testName = testName,
                passed = false,
                vulnerability = "Missing ${missingPermissions.size} critical permissions: ${missingPermissions.joinToString()}",
                recommendation = "Request all missing permissions via Settings screen",
                severity = SecuritySeverity.CRITICAL
            )
        }
    }

    /**
     * Check if a service is currently running
     */
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        @Suppress("DEPRECATION")
        for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}

/**
 * Security test result for a single test
 */
data class SecurityTestResult(
    val testName: String,
    val passed: Boolean,
    val vulnerability: String?,
    val recommendation: String?,
    val severity: SecuritySeverity
)

/**
 * Overall security audit report
 */
data class SecurityAuditReport(
    val totalTests: Int,
    val passed: Int,
    val failed: Int,
    val tests: List<SecurityTestResult>,
    val overallSecurityScore: Int
) {
    fun toReadableReport(): String {
        val builder = StringBuilder()
        builder.appendLine("=== SECURITY AUDIT REPORT ===")
        builder.appendLine("Overall Score: $overallSecurityScore/100")
        builder.appendLine("Tests Passed: $passed/$totalTests")
        builder.appendLine("Tests Failed: $failed/$totalTests")
        builder.appendLine()

        val criticalIssues = tests.filter { it.severity == SecuritySeverity.CRITICAL && !it.passed }
        val highIssues = tests.filter { it.severity == SecuritySeverity.HIGH && !it.passed }
        val mediumIssues = tests.filter { it.severity == SecuritySeverity.MEDIUM && !it.passed }

        if (criticalIssues.isNotEmpty()) {
            builder.appendLine("🔴 CRITICAL ISSUES (${criticalIssues.size}):")
            criticalIssues.forEach { test ->
                builder.appendLine("  - ${test.testName}")
                builder.appendLine("    Vulnerability: ${test.vulnerability}")
                builder.appendLine("    Fix: ${test.recommendation}")
                builder.appendLine()
            }
        }

        if (highIssues.isNotEmpty()) {
            builder.appendLine("🟠 HIGH PRIORITY (${highIssues.size}):")
            highIssues.forEach { test ->
                builder.appendLine("  - ${test.testName}")
                builder.appendLine("    Vulnerability: ${test.vulnerability}")
                builder.appendLine("    Fix: ${test.recommendation}")
                builder.appendLine()
            }
        }

        if (mediumIssues.isNotEmpty()) {
            builder.appendLine("🟡 MEDIUM PRIORITY (${mediumIssues.size}):")
            mediumIssues.forEach { test ->
                builder.appendLine("  - ${test.testName}")
                builder.appendLine("    Vulnerability: ${test.vulnerability}")
                builder.appendLine("    Fix: ${test.recommendation}")
                builder.appendLine()
            }
        }

        val passedTests = tests.filter { it.passed }
        if (passedTests.isNotEmpty()) {
            builder.appendLine("✅ PASSED (${passedTests.size}):")
            passedTests.forEach { test ->
                builder.appendLine("  - ${test.testName}")
            }
        }

        builder.appendLine()
        builder.appendLine("=== END REPORT ===")

        return builder.toString()
    }
}

/**
 * Security severity levels
 */
enum class SecuritySeverity {
    SAFE,       // No issues
    LOW,        // Minor inconvenience
    MEDIUM,     // May affect some users
    HIGH,       // Significant bypass possible
    CRITICAL    // Alarm can be easily disabled
}
