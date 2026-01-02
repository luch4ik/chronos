package com.chronos.alarm.challenge

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chronos.alarm.domain.model.ChallengeConfig
import com.chronos.alarm.ui.theme.BrutalistButton

@Composable
fun BurstChallenge(
    config: ChallengeConfig,
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetTaps = config.params.count ?: 50
    var currentTaps by remember { mutableIntStateOf(0) }
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "button_scale"
    )

    LaunchedEffect(currentTaps) {
        if (currentTaps >= targetTaps) {
            onCompleted()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "TAP THE BUTTON",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "$currentTaps / $targetTaps",
            style = MaterialTheme.typography.displayLarge,
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(48.dp))

        BrutalistButton(
            onClick = {
                currentTaps++
                isPressed = false
            },
            modifier = Modifier
                .size(200.dp)
                .scale(scale),
            onPressStart = { isPressed = true },
            onPressEnd = { isPressed = false }
        ) {
            Text(
                text = "TAP",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        val progress = (currentTaps.toFloat() / targetTaps.toFloat() * 100).toInt()
        Text(
            text = "$progress%",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
