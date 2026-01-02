package com.chronos.alarm.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chronos.alarm.domain.model.ChallengeConfig
import com.chronos.alarm.ui.theme.BrutalistTextField

val DEFAULT_TYPING_PHRASES = listOf(
    "I am wide awake and ready for the day",
    "No more sleeping, time to get moving",
    "Today is going to be productive",
    "I will not hit the snooze button",
    "Coffee is calling my name",
    "Rise and shine, the world awaits",
    "Another day, another opportunity",
    "Sleep is over, adventure begins",
    "Time to conquer this beautiful day",
    "I am grateful for this new morning"
)

@Composable
fun TypingChallenge(
    config: ChallengeConfig,
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetPhrase = config.params.text ?: DEFAULT_TYPING_PHRASES.random()
    var userInput by remember { mutableStateOf("") }
    
    LaunchedEffect(userInput) {
        if (userInput == targetPhrase) {
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
            text = "TYPE THIS PHRASE",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Target phrase with highlighting
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color.Black)
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    targetPhrase.forEachIndexed { index, char ->
                        when {
                            index < userInput.length && userInput[index] == char -> {
                                // Correct character typed
                                withStyle(
                                    SpanStyle(
                                        color = Color.Green,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(char)
                                }
                            }
                            index < userInput.length && userInput[index] != char -> {
                                // Wrong character typed
                                withStyle(
                                    SpanStyle(
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(char)
                                }
                            }
                            else -> {
                                // Not yet typed
                                append(char)
                            }
                        }
                    }
                },
                style = MaterialTheme.typography.titleLarge,
                fontSize = 20.sp,
                lineHeight = 32.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // User input field
        BrutalistTextField(
            value = userInput,
            onValueChange = { newValue ->
                // Only allow input that could potentially match the target
                // Allow backspace and typing
                if (newValue.length <= targetPhrase.length) {
                    userInput = newValue
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Start typing...") },
            singleLine = false,
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Progress indicator
        val correctChars = userInput.indices.count { index ->
            index < targetPhrase.length && userInput[index] == targetPhrase[index]
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color.Black)
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Correct: $correctChars / ${targetPhrase.length}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            val progress = if (targetPhrase.isNotEmpty()) {
                (correctChars.toFloat() / targetPhrase.length * 100).toInt()
            } else 0
            
            Text(
                text = "$progress%",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        if (userInput.isNotEmpty() && userInput.length <= targetPhrase.length) {
            val hasErrors = userInput.indices.any { index ->
                userInput[index] != targetPhrase[index]
            }
            
            if (hasErrors) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Fix the errors to continue",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
