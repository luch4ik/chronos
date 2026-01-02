package com.chronos.alarm.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Alarm(
    val id: String,
    val time: String, // HH:MM format (24h)
    val label: String,
    val isActive: Boolean,
    val days: List<Int>, // 0-6, Sunday to Saturday. Empty = one-time
    val challenges: List<ChallengeConfig> = emptyList(),
    val wakeUpCheck: WakeUpCheckConfig? = null,
    val emergencyContact: EmergencyContactConfig? = null,
    val audio: AlarmAudioConfig = AlarmAudioConfig("GENERATED", generatedType = "CLASSIC")
)

@Serializable
data class ChallengeConfig(
    val id: String,
    val type: ChallengeType,
    val params: ChallengeParams = ChallengeParams()
)

@Serializable
enum class ChallengeType {
    MATH, BURST, MEMORY, TYPING, BLUETOOTH, VELOCITY
}

@Serializable
data class ChallengeParams(
    val count: Int? = null,
    val rounds: Int? = null,
    val difficulty: String? = null, // NORMAL or HARD
    val text: String? = null,
    val deviceName: String? = null,
    val targetSpeed: Int? = null // km/h
)

@Serializable
data class WakeUpCheckConfig(
    val enabled: Boolean,
    val checkDelay: Int, // minutes to wait before asking
    val confirmWindow: Int // minutes user has to confirm
)

@Serializable
data class EmergencyContactConfig(
    val enabled: Boolean,
    val contactName: String,
    val contactNumber: String,
    val method: String, // SMS or CALL
    val message: String? = null,
    val triggerDelay: Int // minutes after alarm starts
)

@Serializable
data class AlarmAudioConfig(
    val source: String, // GENERATED, SYSTEM, URL, FILE
    val generatedType: String? = null, // CLASSIC, DIGITAL, ZEN, HAZARD
    val systemType: String? = null, // MARIMBA, COSMIC, RIPPLE, CIRCUIT
    val url: String? = null,
    val fileData: String? = null, // Base64
    val fileName: String? = null
)

@Serializable
data class AppSettings(
    val timeFormat: String = "24h", // 12h or 24h
    val uninstallProtection: Boolean = false,
    val volumeOverride: Boolean = false,
    val rebootProtection: Boolean = false,
    val theme: String = "system" // light, dark, system
)
