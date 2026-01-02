package com.chronos.alarm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chronos.alarm.data.local.database.ChronosDatabase
import com.chronos.alarm.data.local.preferences.SettingsDataStore
import com.chronos.alarm.data.repository.AlarmRepository
import com.chronos.alarm.domain.model.Alarm
import com.chronos.alarm.domain.model.AppSettings
import com.chronos.alarm.domain.scheduler.AlarmScheduler
import com.chronos.alarm.ui.screens.home.HomeScreen
import com.chronos.alarm.ui.screens.manager.AlarmManagerScreen
import com.chronos.alarm.ui.screens.manager.AlarmManagerViewModel
import com.chronos.alarm.ui.screens.manager.AlarmManagerViewModelFactory
import com.chronos.alarm.ui.screens.settings.SettingsScreen
import com.chronos.alarm.ui.theme.ChronosTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : ComponentActivity() {
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permission results
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check permissions on first launch
        if (savedInstanceState == null) {
            checkAndRequestPermissions()
        }
        
        setContent {
            ChronosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent()
                }
            }
        }
    }
    
    private fun checkAndRequestPermissions() {
        val requiredPermissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        
        val notGranted = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (notGranted.isNotEmpty()) {
            permissionLauncher.launch(notGranted.toTypedArray())
        }
    }
}

@Composable
fun MainContent() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(context)
    )
    
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val alarms by viewModel.alarms.collectAsState()
    val settings by viewModel.settings.collectAsState()
    
    when (val screen = currentScreen) {
        is Screen.Home -> {
            HomeScreen(
                alarms = alarms,
                settings = settings,
                onAddAlarm = { 
                    currentScreen = Screen.AlarmManager(null)
                },
                onEditAlarm = { alarmId ->
                    currentScreen = Screen.AlarmManager(alarmId)
                },
                onDeleteAlarm = { viewModel.deleteAlarm(it) },
                onToggleAlarm = { viewModel.toggleAlarm(it) },
                onSettingsClick = { currentScreen = Screen.Settings }
            )
        }
        is Screen.AlarmManager -> {
            val database = ChronosDatabase.getDatabase(context)
            val settingsDataStore = SettingsDataStore(context)
            val repository = AlarmRepository(database.alarmDao(), settingsDataStore)
            val scheduler = AlarmScheduler(context)
            
            val managerViewModel: AlarmManagerViewModel = viewModel(
                factory = AlarmManagerViewModelFactory(screen.alarmId, repository, scheduler),
                key = screen.alarmId ?: "new"
            )
            
            AlarmManagerScreen(
                viewModel = managerViewModel,
                onNavigateBack = { currentScreen = Screen.Home }
            )
        }
        is Screen.Settings -> {
            SettingsScreen(
                settings = settings,
                onUpdateSettings = { newSettings ->
                    viewModel.updateSettings(newSettings)
                },
                onNavigateBack = { currentScreen = Screen.Home }
            )
        }
    }
}

sealed class Screen {
    data object Home : Screen()
    data class AlarmManager(val alarmId: String?) : Screen()
    data object Settings : Screen()
}

class MainViewModel(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler
) : ViewModel() {
    
    val alarms: StateFlow<List<Alarm>> = repository.getAllAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val settings: StateFlow<AppSettings> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())
    
    fun addAlarm() {
        viewModelScope.launch {
            val newAlarm = Alarm(
                id = UUID.randomUUID().toString(),
                time = "07:00",
                label = "ALARM",
                isActive = true,
                days = emptyList(),
                challenges = emptyList(),
                wakeUpCheck = null,
                emergencyContact = null
            )
            repository.insertAlarm(newAlarm)
            scheduler.scheduleAlarm(newAlarm)
        }
    }
    
    fun deleteAlarm(id: String) {
        viewModelScope.launch {
            scheduler.cancelAlarm(id)
            repository.deleteAlarmById(id)
        }
    }
    
    fun toggleAlarm(id: String) {
        viewModelScope.launch {
            val alarm = repository.getAlarmById(id)
            alarm?.let {
                val updated = it.copy(isActive = !it.isActive)
                repository.updateAlarm(updated)
                if (updated.isActive) {
                    scheduler.scheduleAlarm(updated)
                } else {
                    scheduler.cancelAlarm(id)
                }
            }
        }
    }
    
    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            repository.updateSettings { newSettings }
        }
    }
}

class MainViewModelFactory(
    private val context: android.content.Context
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val database = ChronosDatabase.getDatabase(context)
        val settingsDataStore = SettingsDataStore(context)
        val repository = AlarmRepository(database.alarmDao(), settingsDataStore)
        val scheduler = AlarmScheduler(context)
        
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(repository, scheduler) as T
    }
}
