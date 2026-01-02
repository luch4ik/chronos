package com.chronos.alarm.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chronos.alarm.domain.model.AppSettings
import com.chronos.alarm.ui.theme.BrutalistSwitch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onUpdateSettings: (AppSettings) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SETTINGS",
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
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Time Format
            item {
                SettingSection(title = "TIME FORMAT") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SettingButton(
                            text = "12H",
                            isSelected = settings.timeFormat == "12h",
                            onClick = {
                                onUpdateSettings(settings.copy(timeFormat = "12h"))
                            },
                            modifier = Modifier.weight(1f)
                        )
                        SettingButton(
                            text = "24H",
                            isSelected = settings.timeFormat == "24h",
                            onClick = {
                                onUpdateSettings(settings.copy(timeFormat = "24h"))
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Theme
            item {
                SettingSection(title = "THEME") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SettingButton(
                            text = "LIGHT",
                            isSelected = settings.theme == "light",
                            onClick = {
                                onUpdateSettings(settings.copy(theme = "light"))
                            },
                            modifier = Modifier.weight(1f)
                        )
                        SettingButton(
                            text = "DARK",
                            isSelected = settings.theme == "dark",
                            onClick = {
                                onUpdateSettings(settings.copy(theme = "dark"))
                            },
                            modifier = Modifier.weight(1f)
                        )
                        SettingButton(
                            text = "SYSTEM",
                            isSelected = settings.theme == "system",
                            onClick = {
                                onUpdateSettings(settings.copy(theme = "system"))
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Protection Features
            item {
                SettingSection(title = "PROTECTION") {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingToggle(
                            title = "Volume Override",
                            description = "Automatically set volume to maximum when alarm rings",
                            checked = settings.volumeOverride,
                            onCheckedChange = {
                                onUpdateSettings(settings.copy(volumeOverride = it))
                            }
                        )

                        SettingToggle(
                            title = "Reboot Protection",
                            description = "Reschedule alarms after device restart",
                            checked = settings.rebootProtection,
                            onCheckedChange = {
                                onUpdateSettings(settings.copy(rebootProtection = it))
                            }
                        )

                        SettingToggle(
                            title = "Uninstall Protection",
                            description = "Warn before uninstalling (limited on Android 14+)",
                            checked = settings.uninstallProtection,
                            onCheckedChange = {
                                onUpdateSettings(settings.copy(uninstallProtection = it))
                            }
                        )
                    }
                }
            }

            // About
            item {
                SettingSection(title = "ABOUT") {
                    Column {
                        Text(
                            text = "Chronos Alarm",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Version 1.0.0",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "A brutalist alarm clock that prevents oversleeping with wake-up challenges.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color.Black)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun SettingButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .border(2.dp, Color.Black),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surface
            },
            contentColor = if (isSelected) {
                Color.White
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    ) {
        Text(
            text = text,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun SettingToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        BrutalistSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
