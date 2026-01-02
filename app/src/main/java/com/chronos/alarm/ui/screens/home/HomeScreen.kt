package com.chronos.alarm.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chronos.alarm.domain.model.Alarm
import com.chronos.alarm.domain.model.AppSettings
import com.chronos.alarm.ui.components.AlarmItem
import com.chronos.alarm.ui.components.ClockDisplay
import com.chronos.alarm.ui.components.DotPatternBackground
import com.chronos.alarm.ui.theme.BrutalistButton
import kotlinx.coroutines.delay
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    alarms: List<Alarm>,
    settings: AppSettings,
    onAddAlarm: () -> Unit,
    onEditAlarm: (String) -> Unit,
    onDeleteAlarm: (String) -> Unit,
    onToggleAlarm: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val currentTime = remember { mutableStateOf(Date()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime.value = Date()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DotPatternBackground()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Chronos",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                ClockDisplay(
                    date = currentTime.value,
                    timeFormat = settings.timeFormat,
                    modifier = Modifier.padding(vertical = 24.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (alarms.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No alarms yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = alarms,
                            key = { it.id }
                        ) { alarm ->
                            AlarmItem(
                                alarm = alarm,
                                onEdit = { onEditAlarm(alarm.id) },
                                onDelete = { onDeleteAlarm(alarm.id) },
                                onToggle = { onToggleAlarm(alarm.id) }
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BrutalistButton(
                        text = "Add Alarm",
                        onClick = onAddAlarm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    )
                }
            }
        }
    }
}
