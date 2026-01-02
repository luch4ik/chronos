package com.chronos.alarm.data.repository

import com.chronos.alarm.data.local.database.AlarmDao
import com.chronos.alarm.data.local.database.AlarmEntity
import com.chronos.alarm.data.local.preferences.SettingsDataStore
import com.chronos.alarm.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class AlarmRepository(
    private val alarmDao: AlarmDao,
    private val settingsDataStore: SettingsDataStore
) {
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun getAllAlarms(): Flow<List<Alarm>> {
        return alarmDao.getAllAlarms().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getActiveAlarms(): Flow<List<Alarm>> {
        return alarmDao.getActiveAlarms().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getAlarmById(id: String): Alarm? {
        return alarmDao.getAlarmById(id)?.toDomain()
    }

    suspend fun insertAlarm(alarm: Alarm) {
        alarmDao.insertAlarm(alarm.toEntity())
    }

    suspend fun updateAlarm(alarm: Alarm) {
        alarmDao.updateAlarm(alarm.toEntity())
    }

    suspend fun deleteAlarm(alarm: Alarm) {
        alarmDao.deleteAlarm(alarm.toEntity())
    }

    suspend fun deleteAlarmById(id: String) {
        alarmDao.deleteAlarmById(id)
    }

    // Settings
    val settingsFlow: Flow<AppSettings> = settingsDataStore.settingsFlow

    suspend fun updateSettings(updater: (AppSettings) -> AppSettings) {
        settingsDataStore.updateSettings(updater)
    }

    // Extension functions for Entity ↔ Domain conversion
    private fun AlarmEntity.toDomain(): Alarm {
        return Alarm(
            id = id,
            time = time,
            label = label,
            isActive = isActive,
            days = json.decodeFromString<List<Int>>(days),
            challenges = json.decodeFromString<List<ChallengeConfig>>(challenges),
            wakeUpCheck = wakeUpCheck?.let { json.decodeFromString(it) },
            emergencyContact = emergencyContact?.let { json.decodeFromString(it) },
            audio = json.decodeFromString<AlarmAudioConfig>(audio)
        )
    }

    private fun Alarm.toEntity(): AlarmEntity {
        return AlarmEntity(
            id = id,
            time = time,
            label = label,
            isActive = isActive,
            days = json.encodeToString(days),
            challenges = json.encodeToString(challenges),
            wakeUpCheck = wakeUpCheck?.let { json.encodeToString(it) },
            emergencyContact = emergencyContact?.let { json.encodeToString(it) },
            audio = json.encodeToString(audio)
        )
    }
}
