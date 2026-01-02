# Chronos Android 14 Port - Implementation Plan

**Version:** 1.0  
**Target API:** 34 (Android 14)  
**Language:** Kotlin  
**UI Framework:** Jetpack Compose  
**Last Updated:** January 1, 2026

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Technical Stack](#technical-stack)
3. [Project Structure](#project-structure)
4. [Phase 1: Foundation](#phase-1-foundation)
5. [Phase 2: Data Layer](#phase-2-data-layer)
6. [Phase 3: UI Theme](#phase-3-ui-theme)
7. [Phase 4: Alarm Scheduling](#phase-4-alarm-scheduling)
8. [Phase 5: Foreground Service](#phase-5-foreground-service)
9. [Phase 6: Audio Engine](#phase-6-audio-engine)
10. [Phase 7: Basic UI](#phase-7-basic-ui)
11. [Phase 8: Challenge Games](#phase-8-challenge-games)
12. [Phase 9: Advanced Features](#phase-9-advanced-features)
13. [Phase 10: Testing](#phase-10-testing)
14. [Technical Decisions](#technical-decisions)
15. [Implementation Checklist](#implementation-checklist)

---

## Project Overview

**Purpose:** Port luch4ik/chronos React webapp to native Android 14  
**Core Features:**
- Alarm clock with exact scheduling
- 6 challenge types to prevent oversleeping
- Wake-up verification system
- Emergency contact (SMS/Call) triggers
- Volume override protection
- Force-close recovery
- Boot persistence
- Brutalist UI design

**Design Philosophy:**
- Match React version's brutalist aesthetic exactly
- Use native Android APIs (no PWA wrappers)
- Minimal battery impact (GPS only during active challenges)
- No cloud dependencies (local storage only)

**Constraints:**
- Device Admin API deprecated in API 34+ → Use alternative protection methods
- Cannot prevent phone shutdown (Android security limitation)
- Cannot prevent uninstall entirely (user always has control)

---

## Technical Stack

### Core Dependencies

**Gradle Module (`build.gradle.kts`):**
```kotlin
dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.02.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // ExoPlayer (Audio)
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.media3:media3-ui:1.2.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    
    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // Location (Velocity Challenge)
    implementation("com.google.android.gms:play-services-location:21.1.0")
    
    // Icons
    implementation("androidx.compose.material:material-icons-extended")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

### Required Permissions

**AndroidManifest.xml:**
```xml
<!-- Alarm Scheduling -->
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />

<!-- Wake Lock & Boot -->
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<!-- Audio -->
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />

<!-- Full Screen -->
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- Location (Velocity) -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<!-- Contacts & Emergency -->
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.CALL_PHONE" />

<!-- Bluetooth Challenge -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
```

---

## Project Structure

```
com.chronos.alarm/
├── ChronosApplication.kt
├── MainActivity.kt
│
├── data/
│   ├── local/
│   │   ├── database/
│   │   │   ├── ChronosDatabase.kt
│   │   │   ├── AlarmEntity.kt
│   │   │   └── AlarmDao.kt
│   │   ├── preferences/
│   │   │   └── SettingsDataStore.kt
│   │   └── Converters.kt
│   └── repository/
│       ├── AlarmRepository.kt
│       └── SettingsRepository.kt
│
├── domain/
│   ├── model/
│   │   ├── Alarm.kt
│   │   ├── Challenge.kt
│   │   ├── AudioConfig.kt
│   │   ├── WakeUpCheckConfig.kt
│   │   ├── EmergencyContactConfig.kt
│   │   └── AppSettings.kt
│   ├── scheduler/
│   │   ├── AlarmScheduler.kt
│   │   └── AlarmReceiver.kt
│   └── usecase/
│       ├── AddAlarmUseCase.kt
│       ├── UpdateAlarmUseCase.kt
│       ├── DeleteAlarmUseCase.kt
│       └── ToggleAlarmUseCase.kt
│
├── ui/
│   ├── ChronosApp.kt
│   ├── navigation/
│   │   └── ChronosNavigation.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   ├── Type.kt
│   │   └── BrutalistComponents.kt
│   ├── screens/
│   │   ├── home/
│   │   │   ├── HomeScreen.kt
│   │   │   └── HomeViewModel.kt
│   │   ├── alarm/
│   │   │   ├── AlarmScreen.kt
│   │   │   └── AlarmViewModel.kt
│   │   └── settings/
│   │       ├── SettingsScreen.kt
│   │       └── SettingsViewModel.kt
│   └── components/
│       ├── ClockDisplay.kt
│       ├── AlarmItem.kt
│       ├── TimePicker.kt
│       ├── DaySelector.kt
│       └── ChallengeComponents.kt
│
├── challenge/
│   ├── BurstChallenge.kt
│   ├── MathChallenge.kt
│   ├── MemoryChallenge.kt
│   ├── TypingChallenge.kt
│   ├── VelocityChallenge.kt
│   └── BluetoothChallenge.kt
│
├── service/
│   ├── AlarmForegroundService.kt
│   ├── WatchdogService.kt
│   ├── AudioEngine.kt
│   ├── VolumeManager.kt
│   ├── WakeLockManager.kt
│   └── HapticManager.kt
│
├── protection/
│   ├── BootReceiver.kt
│   └── WakeUpCheckManager.kt
│
├── notification/
│   ├── NotificationChannelManager.kt
│   └── AlarmNotificationManager.kt
│
└── utils/
    ├── Extensions.kt
    ├── DateUtils.kt
    └── Constants.kt
```

---

## Phase 1: Foundation

### Step 1.1: Create Android Project Structure

**Directory Setup:**
```bash
mkdir -p android-chronos/app/src/main/java/com/chronos/alarm
mkdir -p android-chronos/app/src/main/res
mkdir -p android-chronos/gradle
```

### Step 1.2: Create Gradle Configuration Files

**File: `build.gradle.kts` (Project Level)**
```kotlin
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22" apply false
}
```

**File: `build.gradle.kts` (Module Level)**
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.chronos.alarm"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.chronos.alarm"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.02.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Media3 ExoPlayer (Audio)
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.media3:media3-ui:1.2.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    
    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // Location (Velocity Challenge)
    implementation("com.google.android.gms:play-services-location:21.1.0")
    
    // Icons
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

### Step 1.3: Create AndroidManifest.xml

**File: `AndroidManifest.xml`**
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Alarm Scheduling -->
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.USE_EXACT_ALARM" />

    <!-- Wake Lock & Boot -->
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <!-- Audio -->
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />

    <!-- Full Screen -->
    <uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <!-- Location (Velocity) -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

    <!-- Contacts & Emergency -->
    <uses-permission android:name="android.permission.READ_CONTACTS" />
    <uses-permission android:name="android.permission.SEND_SMS" />
    <uses-permission android:name="android.permission.CALL_PHONE" />

    <!-- Bluetooth Challenge -->
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

    <!-- Vibration -->
    <uses-permission android:name="android.permission.VIBRATE" />

    <application
        android:name=".ChronosApplication"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Chronos"
        tools:targetApi="31">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.Chronos">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity
            android:name=".ui.screens.alarm.AlarmScreen"
            android:exported="false"
            android:showOnLockScreen="true"
            android:turnScreenOn="true"
            android:launchMode="singleInstance"
            android:excludeFromRecents="true"
            android:theme="@style/Theme.Chronos" />

        <service
            android:name=".service.AlarmForegroundService"
            android:exported="false"
            android:foregroundServiceType="mediaPlayback" />

        <service
            android:name=".service.WatchdogService"
            android:exported="false" />

        <receiver
            android:name=".domain.scheduler.AlarmReceiver"
            android:exported="true"
            android:enabled="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.LOCKED_BOOT_COMPLETED" />
                <action android:name="com.chronos.alarm.ACTION_ALARM_TRIGGER" />
            </intent-filter>
        </receiver>

    </application>

</manifest>
```

### Step 1.4: Create Resource Files

**File: `res/values/strings.xml`**
```xml
<resources>
    <string name="app_name">Chronos</string>
</resources>
```

**File: `res/values/themes.xml`**
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.Chronos" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```

---

## Phase 2: Data Layer

### Step 2.1: Domain Models

**File: `domain/model/Alarm.kt`**
```kotlin
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
```

### Step 2.2: Room Database

**File: `data/local/database/AlarmEntity.kt`**
```kotlin
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
```

**File: `data/local/database/AlarmDao.kt`**
```kotlin
package com.chronos.alarm.data.local.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY time ASC")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: String): AlarmEntity?

    @Query("SELECT * FROM alarms WHERE isActive = 1 ORDER BY time ASC")
    fun getActiveAlarms(): Flow<List<AlarmEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity)

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)

    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteAlarmById(id: String)
}
```

**File: `data/local/Converters.kt`**
```kotlin
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
```

**File: `data/local/database/ChronosDatabase.kt`**
```kotlin
package com.chronos.alarm.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.chronos.alarm.data.local.Converters

@Database(
    entities = [AlarmEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ChronosDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    companion object {
        @Volatile
        private var INSTANCE: ChronosDatabase? = null

        fun getDatabase(context: Context): ChronosDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChronosDatabase::class.java,
                    "chronos_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

### Step 2.3: DataStore for Settings

**File: `data/local/preferences/SettingsDataStore.kt`**
```kotlin
package com.chronos.alarm.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.chronos.alarm.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    
    private object PreferencesKeys {
        val TIME_FORMAT = stringPreferencesKey("time_format")
        val UNINSTALL_PROTECTION = booleanPreferencesKey("uninstall_protection")
        val VOLUME_OVERRIDE = booleanPreferencesKey("volume_override")
        val REBOOT_PROTECTION = booleanPreferencesKey("reboot_protection")
        val THEME = stringPreferencesKey("theme")
    }

    val settingsFlow: Flow<AppSettings> = context.settingsDataStore.data.map { preferences ->
        AppSettings(
            timeFormat = preferences[PreferencesKeys.TIME_FORMAT] ?: "24h",
            uninstallProtection = preferences[PreferencesKeys.UNINSTALL_PROTECTION] ?: false,
            volumeOverride = preferences[PreferencesKeys.VOLUME_OVERRIDE] ?: false,
            rebootProtection = preferences[PreferencesKeys.REBOOT_PROTECTION] ?: false,
            theme = preferences[PreferencesKeys.THEME] ?: "system"
        )
    }

    suspend fun updateSettings(updater: (AppSettings) -> AppSettings) {
        context.settingsDataStore.edit { preferences ->
            val current = AppSettings(
                timeFormat = preferences[PreferencesKeys.TIME_FORMAT] ?: "24h",
                uninstallProtection = preferences[PreferencesKeys.UNINSTALL_PROTECTION] ?: false,
                volumeOverride = preferences[PreferencesKeys.VOLUME_OVERRIDE] ?: false,
                rebootProtection = preferences[PreferencesKeys.REBOOT_PROTECTION] ?: false,
                theme = preferences[PreferencesKeys.THEME] ?: "system"
            )
            val updated = updater(current)
            
            preferences[PreferencesKeys.TIME_FORMAT] = updated.timeFormat
            preferences[PreferencesKeys.UNINSTALL_PROTECTION] = updated.uninstallProtection
            preferences[PreferencesKeys.VOLUME_OVERRIDE] = updated.volumeOverride
            preferences[PreferencesKeys.REBOOT_PROTECTION] = updated.rebootProtection
            preferences[PreferencesKeys.THEME] = updated.theme
        }
    }
}
```

### Step 2.4: Repository

**File: `data/repository/AlarmRepository.kt`**
```kotlin
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
```

---

## Implementation Checklist

### Phase 1: Foundation ✓
- [ ] Create project structure
- [ ] Configure Gradle files
- [ ] Create AndroidManifest.xml
- [ ] Create resource files
- [ ] Verify project builds

### Phase 2: Data Layer
- [ ] Create domain models
- [ ] Create Room entities
- [ ] Create DAOs
- [ ] Create database
- [ ] Create type converters
- [ ] Create DataStore
- [ ] Create repository
- [ ] Test database operations

### Phase 3-10: Remaining Phases
- See full checklist in complete plan

---

**End of Implementation Plan**
