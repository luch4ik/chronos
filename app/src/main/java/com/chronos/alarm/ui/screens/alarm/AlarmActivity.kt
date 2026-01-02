package com.chronos.alarm.ui.screens.alarm

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chronos.alarm.data.local.database.ChronosDatabase
import com.chronos.alarm.data.repository.AlarmRepository
import com.chronos.alarm.service.AlarmForegroundService
import com.chronos.alarm.ui.theme.ChronosTheme

class AlarmActivity : ComponentActivity() {

    companion object {
        const val EXTRA_ALARM_ID = "extra_alarm_id"

        fun createIntent(context: Context, alarmId: String): Intent {
            return Intent(context, AlarmActivity::class.java).apply {
                putExtra(EXTRA_ALARM_ID, alarmId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_NO_USER_ACTION
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show on lock screen and turn screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        // Disable back button and recent apps
        @Suppress("DEPRECATION")
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)

        val alarmId = intent.getStringExtra(EXTRA_ALARM_ID)
        if (alarmId == null) {
            finish()
            return
        }

        // Initialize repository
        val database = ChronosDatabase.getDatabase(applicationContext)
        val settingsDataStore = com.chronos.alarm.data.local.preferences.SettingsDataStore(applicationContext)
        val repository = AlarmRepository(database.alarmDao(), settingsDataStore)

        setContent {
            ChronosTheme {
                val viewModel: AlarmViewModel = viewModel(
                    factory = AlarmViewModelFactory(alarmId, repository)
                )

                AlarmScreen(
                    viewModel = viewModel,
                    onDismissAlarm = {
                        dismissAlarm()
                    }
                )
            }
        }
    }

    private fun dismissAlarm() {
        // Stop the alarm service
        val serviceIntent = Intent(this, AlarmForegroundService::class.java)
        stopService(serviceIntent)
        
        // Close the activity
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Prevent back button from dismissing alarm
        // User must complete challenges
    }
}
