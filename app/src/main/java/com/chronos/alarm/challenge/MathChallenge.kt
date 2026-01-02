package com.chronos.alarm.challenge

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chronos.alarm.domain.model.ChallengeConfig
import com.chronos.alarm.ui.theme.BrutalistButton
import kotlin.random.Random

data class MathProblem(
    val num1: Int,
    val num2: Int,
    val operator: String,
    val answer: Int
)

@Composable
fun MathChallenge(
    config: ChallengeConfig,
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val problemCount = config.params.count ?: 5
    val difficulty = config.params.difficulty?.lowercase() ?: "medium"

    var currentProblemIndex by remember { mutableIntStateOf(0) }
    var currentAnswer by remember { mutableStateOf("") }
    var problems by remember { 
        mutableStateOf(generateProblems(problemCount, difficulty))
    }

    val currentProblem = problems.getOrNull(currentProblemIndex)

    LaunchedEffect(currentProblemIndex) {
        if (currentProblemIndex >= problems.size) {
            onCompleted()
        }
    }

    if (currentProblem == null) {
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SOLVE THE PROBLEM",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Problem ${currentProblemIndex + 1} of ${problems.size}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "${currentProblem.num1} ${currentProblem.operator} ${currentProblem.num2} = ?",
            style = MaterialTheme.typography.displayLarge,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = currentAnswer.ifEmpty { "_" },
            style = MaterialTheme.typography.displayMedium,
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Number pad
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.width(300.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items((1..9).toList()) { number ->
                BrutalistButton(
                    onClick = {
                        if (currentAnswer.length < 6) {
                            currentAnswer += number.toString()
                        }
                    },
                    modifier = Modifier
                        .aspectRatio(1f)
                        .height(70.dp)
                ) {
                    Text(
                        text = number.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Bottom row: Clear, 0, Submit
            item {
                BrutalistButton(
                    onClick = { currentAnswer = "" },
                    modifier = Modifier
                        .aspectRatio(1f)
                        .height(70.dp)
                ) {
                    Text(
                        text = "C",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                BrutalistButton(
                    onClick = {
                        if (currentAnswer.length < 6) {
                            currentAnswer += "0"
                        }
                    },
                    modifier = Modifier
                        .aspectRatio(1f)
                        .height(70.dp)
                ) {
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                BrutalistButton(
                    onClick = {
                        val userAnswer = currentAnswer.toIntOrNull()
                        if (userAnswer == currentProblem.answer) {
                            currentProblemIndex++
                            currentAnswer = ""
                        } else {
                            // Wrong answer, shake or show error
                            currentAnswer = ""
                        }
                    },
                    modifier = Modifier
                        .aspectRatio(1f)
                        .height(70.dp)
                ) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Negative number support
        Spacer(modifier = Modifier.height(8.dp))
        BrutalistButton(
            onClick = {
                currentAnswer = if (currentAnswer.startsWith("-")) {
                    currentAnswer.removePrefix("-")
                } else {
                    "-$currentAnswer"
                }
            },
            modifier = Modifier.width(300.dp)
        ) {
            Text(
                text = "+/−",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun generateProblems(count: Int, difficulty: String): List<MathProblem> {
    val problems = mutableListOf<MathProblem>()
    
    val range = when (difficulty) {
        "easy" -> 1..20
        "hard" -> 10..100
        else -> 1..50 // medium
    }

    repeat(count) {
        val num1 = Random.nextInt(range.first, range.last)
        val num2 = Random.nextInt(range.first, range.last)
        val operator = listOf("+", "-", "×").random()

        val answer = when (operator) {
            "+" -> num1 + num2
            "-" -> num1 - num2
            "×" -> num1 * num2
            else -> num1 + num2
        }

        problems.add(MathProblem(num1, num2, operator, answer))
    }

    return problems
}
