package com.chronos.alarm.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey val id: String,
    val time: String,
    val label: String,
    val isActive: Boolean,
    val days: String, // JSON: [0,1,2,3,4,5,6]
    val challenges: String, // JSON array of ChallengeConfig
    val wakeUpCheck: String?, // JSON of WakeUpCheckConfig
    val emergencyContact: String?, // JSON of EmergencyContactConfig
    val audio: String // JSON of AudioConfig
)
