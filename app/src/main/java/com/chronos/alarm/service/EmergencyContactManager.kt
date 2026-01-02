package com.chronos.alarm.service

// Phase 4: Advanced Features - Emergency Contact Manager
// Sends SMS or makes call to emergency contact if user doesn't dismiss alarm
// Triggers after configurable delay

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.chronos.alarm.domain.model.EmergencyContactConfig
import kotlinx.coroutines.*

class EmergencyContactManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var triggerJob: Job? = null

    /**
     * Schedule emergency contact notification
     * @param alarmId Unique ID for this alarm
     * @param config Emergency contact configuration
     */
    fun scheduleEmergencyContact(alarmId: String, config: EmergencyContactConfig) {
        if (!config.enabled) return

        // Cancel any existing job for this alarm
        cancelEmergencyContact()

        triggerJob = scope.launch {
            // Wait for trigger delay
            delay(config.triggerDelay * 60 * 1000L) // Convert minutes to milliseconds

            // Execute emergency action
            when (config.method) {
                "SMS" -> sendEmergencySMS(config)
                "CALL" -> makeEmergencyCall(config)
                else -> sendEmergencySMS(config) // Default to SMS
            }
        }
    }

    /**
     * Send emergency SMS to configured contact
     */
    private fun sendEmergencySMS(config: EmergencyContactConfig) {
        // Check SMS permission
        if (!hasSMSPermission()) {
            showPermissionError("SMS")
            return
        }

        try {
            val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val message = config.message ?: "I am not waking up to my alarm. Please help."

            smsManager.sendTextMessage(
                config.contactNumber,
                null,
                message,
                null,
                null
            )

            showSuccessMessage("Emergency SMS sent to ${config.contactName}")
        } catch (e: Exception) {
            showErrorMessage("Failed to send SMS: ${e.message}")
        }
    }

    /**
     * Make emergency call to configured contact
     */
    private fun makeEmergencyCall(config: EmergencyContactConfig) {
        // Check CALL permission
        if (!hasCallPermission()) {
            showPermissionError("Call")
            return
        }

        try {
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:${config.contactNumber}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(callIntent)

            showSuccessMessage("Calling ${config.contactName}...")
        } catch (e: Exception) {
            showErrorMessage("Failed to make call: ${e.message}")
        }
    }

    /**
     * Cancel emergency contact trigger (user dismissed alarm in time)
     */
    fun cancelEmergencyContact() {
        triggerJob?.cancel()
        triggerJob = null
    }

    /**
     * Check if app has SMS permission
     */
    private fun hasSMSPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if app has CALL permission
     */
    private fun hasCallPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Show permission error message
     */
    private fun showPermissionError(permissionType: String) {
        scope.launch(Dispatchers.Main) {
            Toast.makeText(
                context,
                "Permission required: $permissionType permission not granted",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Show success message
     */
    private fun showSuccessMessage(message: String) {
        scope.launch(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Show error message
     */
    private fun showErrorMessage(message: String) {
        scope.launch(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Cleanup - cancel all pending jobs
     */
    fun cleanup() {
        triggerJob?.cancel()
        scope.cancel()
    }
}
