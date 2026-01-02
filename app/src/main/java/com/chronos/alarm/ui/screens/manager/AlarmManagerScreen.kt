package com.chronos.alarm.ui.screens.manager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chronos.alarm.ui.components.ChallengeConfigurator
import com.chronos.alarm.ui.components.DaySelector
import com.chronos.alarm.ui.components.TimePicker
import com.chronos.alarm.ui.theme.BrutalistButton
import com.chronos.alarm.ui.theme.BrutalistTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmManagerScreen(
    viewModel: AlarmManagerViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.alarm?.id == null) "NEW ALARM" else "EDIT ALARM",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CANCEL")
                    }

                    BrutalistButton(
                        onClick = {
                            viewModel.saveAlarm(onSaved = onNavigateBack)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("SAVE")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ERROR",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = uiState.error ?: "Unknown error")
                    }
                }
            }

            uiState.alarm != null -> {
                AlarmManagerContent(
                    viewModel = viewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun AlarmManagerContent(
    viewModel: AlarmManagerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val alarm = uiState.alarm ?: return

    // Parse time
    val timeParts = alarm.time.split(":")
    val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 7
    val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Time Picker Section
        item {
            Column {
                Text(
                    text = "TIME",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                TimePicker(
                    hour = hour,
                    minute = minute,
                    onTimeChanged = { h, m -> viewModel.updateTime(h, m) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Label Section
        item {
            Column {
                Text(
                    text = "LABEL",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                BrutalistTextField(
                    value = alarm.label,
                    onValueChange = { viewModel.updateLabel(it) },
                    placeholder = { Text("Alarm label") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // Day Selector Section
        item {
            DaySelector(
                selectedDays = alarm.days,
                onDayToggled = { viewModel.toggleDay(it) }
            )
        }

        // Challenge Configurator Section
        item {
            ChallengeConfigurator(
                challenges = alarm.challenges,
                onAddChallenge = { viewModel.addChallenge(it) },
                onRemoveChallenge = { viewModel.removeChallenge(it) },
                onUpdateChallenge = { id, params -> viewModel.updateChallenge(id, params) }
            )
        }

        // Audio Configuration Section (Future)
        item {
            Column {
                Text(
                    text = "AUDIO",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Default system ringtone",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Custom audio coming soon",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Bottom spacer
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
