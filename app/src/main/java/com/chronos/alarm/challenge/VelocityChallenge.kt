package com.chronos.alarm.challenge

// Phase 3: Challenge Implementation - Velocity Challenge
// GPS-based challenge requiring user to reach target speed (km/h)
// Uses FusedLocationProviderClient for accurate speed tracking

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.chronos.alarm.domain.model.ChallengeConfig
import com.chronos.alarm.ui.theme.BrutalistCard
import com.chronos.alarm.ui.theme.BrutalistTag
import com.google.android.gms.location.*
import kotlinx.coroutines.delay

@SuppressLint("MissingPermission")
@Composable
fun VelocityChallenge(
    config: ChallengeConfig,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val targetSpeed = config.params.targetSpeed ?: 10 // Default 10 km/h

    var hasPermission by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) }

    var currentSpeed by remember { mutableStateOf(0f) }
    var accuracy by remember { mutableStateOf(0f) }
    var isTracking by remember { mutableStateOf(false) }
    var successTime by remember { mutableStateOf(0) }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            isTracking = true
        }
    }

    // Location callback
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation
                if (location != null) {
                    // Speed is in m/s, convert to km/h
                    currentSpeed = location.speed * 3.6f
                    accuracy = location.accuracy
                }
            }
        }
    }

    // Track speed and check for success
    LaunchedEffect(currentSpeed) {
        if (currentSpeed >= targetSpeed && accuracy < 20f) {
            // Require sustained speed for 3 seconds
            successTime++
            delay(1000)
            if (successTime >= 3) {
                onSuccess()
            }
        } else {
            successTime = 0
        }
    }

    // Start location updates when permission is granted
    LaunchedEffect(hasPermission, isTracking) {
        if (hasPermission && isTracking) {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                1000L // Update every second
            ).build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
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
                imageVector = Icons.Default.DirectionsRun,
                contentDescription = "Velocity",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "VELOCITY CHALLENGE",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        if (!hasPermission) {
            // Permission request UI
            BrutalistCard(
                onClick = {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Location Permission Required",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap to grant location access",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                </Column>
            }
        } else {
            // Speed display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Current Speed",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = String.format("%.1f", currentSpeed),
                    style = MaterialTheme.typography.displayLarge,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentSpeed >= targetSpeed) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = "km/h",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Target speed
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Target:",
                    style = MaterialTheme.typography.bodyLarge
                )
                BrutalistTag(text = "$targetSpeed km/h")
            }

            // Progress bar
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { (currentSpeed / targetSpeed).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp),
                )

                if (currentSpeed >= targetSpeed) {
                    Text(
                        text = "Hold for ${3 - successTime} more seconds...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // GPS accuracy indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "GPS",
                    modifier = Modifier.size(16.dp),
                    tint = if (accuracy < 20f) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                Text(
                    text = "GPS Accuracy: ${accuracy.toInt()}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Instructions
            Text(
                text = "Start moving to reach the target speed!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
