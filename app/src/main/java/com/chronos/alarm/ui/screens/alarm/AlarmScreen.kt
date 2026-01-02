package com.chronos.alarm.ui.screens.alarm

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chronos.alarm.challenge.BurstChallenge
import com.chronos.alarm.challenge.MathChallenge
import com.chronos.alarm.challenge.MemoryChallenge
import com.chronos.alarm.challenge.TypingChallenge
import com.chronos.alarm.domain.model.ChallengeType
import com.chronos.alarm.ui.theme.BrutalistButton
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AlarmScreen(
    viewModel: AlarmViewModel,
    onDismissAlarm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ERROR",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.error ?: "Unknown error",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            uiState.isCompleted -> {
                LaunchedEffect(Unit) {
                    onDismissAlarm()
                }
            }

            else -> {
                AlarmContent(
                    alarm = uiState.alarm,
                    currentChallengeIndex = uiState.currentChallengeIndex,
                    onChallengeCompleted = { viewModel.onChallengeCompleted() },
                    onDismissAlarm = onDismissAlarm
                )
            }
        }
    }
}

@Composable
private fun AlarmContent(
    alarm: com.chronos.alarm.domain.model.Alarm?,
    currentChallengeIndex: Int,
    onChallengeCompleted: () -> Unit,
    onDismissAlarm: () -> Unit
) {
    if (alarm == null) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with time and label
        AlarmHeader(
            label = alarm.label,
            currentTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Challenge content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (alarm.challenges.isEmpty()) {
                // No challenges - just show dismiss button
                NoChallengeScreen(onDismiss = onDismissAlarm)
            } else {
                // Show current challenge
                val currentChallenge = alarm.challenges.getOrNull(currentChallengeIndex)
                if (currentChallenge != null) {
                    AnimatedContent(
                        targetState = currentChallengeIndex,
                        transitionSpec = {
                            slideInHorizontally { it } + fadeIn() togetherWith
                                    slideOutHorizontally { -it } + fadeOut()
                        },
                        label = "challenge_transition"
                    ) { index ->
                        when (currentChallenge.type) {
                            ChallengeType.BURST -> BurstChallenge(
                                config = currentChallenge,
                                onCompleted = onChallengeCompleted
                            )
                            ChallengeType.MATH -> MathChallenge(
                                config = currentChallenge,
                                onCompleted = onChallengeCompleted
                            )
                            ChallengeType.MEMORY -> MemoryChallenge(
                                config = currentChallenge,
                                onCompleted = onChallengeCompleted
                            )
                            ChallengeType.TYPING -> TypingChallenge(
                                config = currentChallenge,
                                onCompleted = onChallengeCompleted
                            )
                            ChallengeType.VELOCITY -> {
                                // TODO: Implement VelocityChallenge
                                PlaceholderChallenge(
                                    type = "VELOCITY",
                                    onCompleted = onChallengeCompleted
                                )
                            }
                            ChallengeType.BLUETOOTH -> {
                                // TODO: Implement BluetoothChallenge
                                PlaceholderChallenge(
                                    type = "BLUETOOTH",
                                    onCompleted = onChallengeCompleted
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress indicator
        if (alarm.challenges.isNotEmpty()) {
            ChallengeProgress(
                currentChallenge = currentChallengeIndex + 1,
                totalChallenges = alarm.challenges.size
            )
        }
    }
}

@Composable
private fun AlarmHeader(
    label: String,
    currentTime: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color.Black)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = currentTime,
            style = MaterialTheme.typography.displayLarge,
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold
        )
        if (label.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoChallengeScreen(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "WAKE UP!",
            style = MaterialTheme.typography.displayLarge,
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(48.dp))

        BrutalistButton(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(80.dp)
        ) {
            Text(
                text = "I'M AWAKE",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PlaceholderChallenge(
    type: String,
    onCompleted: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "$type CHALLENGE",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Not yet implemented",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        BrutalistButton(
            onClick = onCompleted,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(60.dp)
        ) {
            Text(
                text = "SKIP",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ChallengeProgress(
    currentChallenge: Int,
    totalChallenges: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color.Black)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Challenge $currentChallenge of $totalChallenges",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(totalChallenges) { index ->
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .border(
                            2.dp,
                            if (index < currentChallenge) MaterialTheme.colorScheme.primary
                            else Color.Black
                        )
                        .background(
                            if (index < currentChallenge) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                )
            }
        }
    }
}
