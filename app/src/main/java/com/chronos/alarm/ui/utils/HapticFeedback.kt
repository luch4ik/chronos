package com.chronos.alarm.ui.utils

/* PENDING CLAUDE REVIEW */
// Phase 2: Design System - Haptic Feedback Utility
// Provides vibration feedback for user interactions
// Created by: general agent (Task tool session ses_484e60b33ffei4JvgOUr88S0mo)

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberHapticFeedback(): HapticFeedback {
    val context = LocalContext.current
    return remember(context) { HapticFeedback(context) }
}

class HapticFeedback(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun triggerHaptic(type: HapticType = HapticType.LIGHT) {
        if (vibrator == null || !vibrator.hasVibrator()) return

        when (type) {
            HapticType.LIGHT -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(10)
                }
            }
            HapticType.MEDIUM -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(25)
                }
            }
            HapticType.HEAVY -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50)
                }
            }
            HapticType.SUCCESS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createWaveform(
                            longArrayOf(0, 30, 30, 30),
                            intArrayOf(0, 255, 0, 255),
                            -1
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 30, 30, 30), -1)
                }
            }
            HapticType.ERROR -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createWaveform(
                            longArrayOf(0, 50, 50, 50),
                            intArrayOf(0, 255, 0, 255),
                            -1
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 50, 50, 50), -1)
                }
            }
        }
    }
}

enum class HapticType {
    LIGHT,
    MEDIUM,
    HEAVY,
    SUCCESS,
    ERROR
}