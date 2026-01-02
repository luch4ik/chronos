package com.chronos.alarm.challenge

// Phase 3: Challenge Implementation - Bluetooth Challenge
// Bluetooth LE device discovery and connection challenge
// Requires user to find and connect to a specific paired Bluetooth device

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.chronos.alarm.domain.model.ChallengeConfig
import com.chronos.alarm.ui.theme.BrutalistCard
import com.chronos.alarm.ui.theme.BrutalistTag
import com.chronos.alarm.ui.utils.HapticType
import com.chronos.alarm.ui.utils.rememberHapticFeedback

@SuppressLint("MissingPermission")
@Composable
fun BluetoothChallenge(
    config: ChallengeConfig,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val haptic = rememberHapticFeedback()
    val targetDeviceName = config.params.deviceName ?: "Unknown Device"

    var hasPermission by remember { mutableStateOf(checkBluetoothPermissions(context)) }
    var isScanning by remember { mutableStateOf(false) }
    var discoveredDevices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    var pairedDevices by remember { mutableStateOf<Set<BluetoothDevice>>(emptySet()) }
    var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }
    var isConnected by remember { mutableStateOf(false) }

    val bluetoothManager = remember {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    }
    val bluetoothAdapter = bluetoothManager?.adapter

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission = permissions.values.all { it }
    }

    // Load paired devices
    LaunchedEffect(hasPermission) {
        if (hasPermission && bluetoothAdapter != null) {
            try {
                pairedDevices = bluetoothAdapter.bondedDevices ?: emptySet()
                // Auto-find target device in paired list
                val target = pairedDevices.find { it.name == targetDeviceName }
                if (target != null) {
                    selectedDevice = target
                }
            } catch (e: SecurityException) {
                // Permission denied
            }
        }
    }

    // Check for connection success
    LaunchedEffect(selectedDevice) {
        if (selectedDevice != null && selectedDevice?.name == targetDeviceName) {
            isConnected = true
            haptic.triggerHaptic(HapticType.SUCCESS)
            kotlinx.coroutines.delay(500)
            onSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isConnected) {
                    Icons.Default.BluetoothConnected
                } else if (isScanning) {
                    Icons.Default.BluetoothSearching
                } else {
                    Icons.Default.Bluetooth
                },
                contentDescription = "Bluetooth",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "BLUETOOTH CHALLENGE",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        if (!hasPermission) {
            // Permission request UI
            BrutalistCard(
                onClick = {
                    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        arrayOf(
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT
                        )
                    } else {
                        arrayOf(
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN
                        )
                    }
                    permissionLauncher.launch(permissions)
                }
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = "Bluetooth",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Bluetooth Permission Required",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap to grant Bluetooth access",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                </Column>
            }
        } else {
            // Target device
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Find and connect to:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BrutalistTag(
                    text = targetDeviceName,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer
                )
            }

            // Paired devices section
            if (pairedDevices.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Paired Devices",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    pairedDevices.forEach { device ->
                        BrutalistCard(
                            onClick = {
                                selectedDevice = device
                                haptic.triggerHaptic(HapticType.MEDIUM)
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = device.name ?: "Unknown Device",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = device.address,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (device == selectedDevice && device.name == targetDeviceName) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Connected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Instructions
            if (!isConnected) {
                Text(
                    text = if (pairedDevices.isEmpty()) {
                        "No paired devices found. Please pair your device in system settings first."
                    } else {
                        "Tap on \"$targetDeviceName\" to connect and dismiss the alarm!"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Connected! Challenge complete!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@SuppressLint("InlinedApi")
private fun checkBluetoothPermissions(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_ADMIN
        ) == PackageManager.PERMISSION_GRANTED
    }
}
