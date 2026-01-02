package com.chronos.alarm.data.local

import androidx.room.TypeConverter
import com.chronos.alarm.domain.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @TypeConverter
    fun fromDaysList(days: List<Int>): String {
        return json.encodeToString(days)
    }

    @TypeConverter
    fun toDaysList(days: String): List<Int> {
        return json.decodeFromString(days)
    }

    @TypeConverter
    fun fromChallengeList(challenges: List<ChallengeConfig>): String {
        return json.encodeToString(challenges)
    }

    @TypeConverter
    fun toChallengeList(challenges: String): List<ChallengeConfig> {
        return json.decodeFromString(challenges)
    }

    @TypeConverter
    fun fromWakeUpCheckConfig(config: WakeUpCheckConfig?): String? {
        return config?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toWakeUpCheckConfig(jsonString: String?): WakeUpCheckConfig? {
        return jsonString?.let { json.decodeFromString(it) }
    }

    @TypeConverter
    fun fromEmergencyContactConfig(config: EmergencyContactConfig?): String? {
        return config?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toEmergencyContactConfig(jsonString: String?): EmergencyContactConfig? {
        return jsonString?.let { json.decodeFromString(it) }
    }

    @TypeConverter
    fun fromAudioConfig(config: AlarmAudioConfig): String {
        return json.encodeToString(config)
    }

    @TypeConverter
    fun toAudioConfig(jsonString: String): AlarmAudioConfig {
        return json.decodeFromString(jsonString)
    }
}
