package com.chronos.alarm.challenge

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chronos.alarm.domain.model.ChallengeConfig
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class MemoryGameState {
    SHOWING_SEQUENCE,
    WAITING_FOR_INPUT,
    CORRECT,
    WRONG
}

@Composable
fun MemoryChallenge(
    config: ChallengeConfig,
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rounds = config.params.rounds ?: 3
    var currentRound by remember { mutableIntStateOf(0) }
    var sequence by remember { mutableStateOf(generateSequence(currentRound + 2)) }
    var userSequence by remember { mutableStateOf(listOf<Int>()) }
    var gameState by remember { mutableStateOf(MemoryGameState.SHOWING_SEQUENCE) }
    var highlightedCell by remember { mutableIntStateOf(-1) }

    LaunchedEffect(currentRound) {
        if (currentRound >= rounds) {
            onCompleted()
            return@LaunchedEffect
        }

        // Generate and show sequence
        sequence = generateSequence(currentRound + 2)
        userSequence = emptyList()
        gameState = MemoryGameState.SHOWING_SEQUENCE

        // Show sequence with delays
        delay(1000)
        sequence.forEach { cell ->
            highlightedCell = cell
            delay(600)
            highlightedCell = -1
            delay(400)
        }

        gameState = MemoryGameState.WAITING_FOR_INPUT
    }

    LaunchedEffect(userSequence.size) {
        if (gameState != MemoryGameState.WAITING_FOR_INPUT) return@LaunchedEffect
        if (userSequence.isEmpty()) return@LaunchedEffect

        // Check if user's input matches so far
        if (userSequence.size <= sequence.size) {
            val isCorrectSoFar = userSequence.indices.all { i ->
                userSequence[i] == sequence[i]
            }

            if (!isCorrectSoFar) {
                // Wrong input
                gameState = MemoryGameState.WRONG
                delay(1000)
                // Reset round
                userSequence = emptyList()
                gameState = MemoryGameState.SHOWING_SEQUENCE
                delay(500)
            } else if (userSequence.size == sequence.size) {
                // Completed this round correctly
                gameState = MemoryGameState.CORRECT
                delay(1000)
                currentRound++
            }
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
            text = "REMEMBER THE PATTERN",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Round ${currentRound + 1} of $rounds",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Status text
        val statusText = when (gameState) {
            MemoryGameState.SHOWING_SEQUENCE -> "WATCH..."
            MemoryGameState.WAITING_FOR_INPUT -> "YOUR TURN"
            MemoryGameState.CORRECT -> "CORRECT!"
            MemoryGameState.WRONG -> "WRONG! TRY AGAIN"
        }

        val statusColor = when (gameState) {
            MemoryGameState.CORRECT -> Color.Green
            MemoryGameState.WRONG -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onBackground
        }

        Text(
            text = statusText,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = statusColor
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 3x3 Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.size(300.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            userScrollEnabled = false
        ) {
            items(9) { index ->
                val isHighlighted = highlightedCell == index
                val isInUserSequence = userSequence.contains(index)

                MemoryCell(
                    isHighlighted = isHighlighted,
                    isInUserSequence = isInUserSequence,
                    isClickable = gameState == MemoryGameState.WAITING_FOR_INPUT,
                    onClick = {
                        if (gameState == MemoryGameState.WAITING_FOR_INPUT) {
                            userSequence = userSequence + index
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Progress indicator
        Text(
            text = "${userSequence.size} / ${sequence.size}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MemoryCell(
    isHighlighted: Boolean,
    isInUserSequence: Boolean,
    isClickable: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isHighlighted -> MaterialTheme.colorScheme.primary
            isInUserSequence -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        label = "background_color"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .border(2.dp, Color.Black)
            .background(backgroundColor)
            .clickable(enabled = isClickable) { onClick() }
    )
}

private fun generateSequence(length: Int): List<Int> {
    val sequence = mutableListOf<Int>()
    repeat(length) {
        sequence.add(Random.nextInt(0, 9))
    }
    return sequence
}
