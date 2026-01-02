package com.chronos.alarm.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chronos.alarm.ui.theme.BrutalistButton

private val DAYS = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

@Composable
fun DaySelector(
    selectedDays: List<Int>,
    onDayToggled: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "REPEAT",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DAYS.forEachIndexed { index, day ->
                DayButton(
                    day = day,
                    isSelected = selectedDays.contains(index),
                    onClick = { onDayToggled(index) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val description = when {
            selectedDays.isEmpty() -> "One-time alarm"
            selectedDays.size == 7 -> "Every day"
            selectedDays.containsAll(listOf(1, 2, 3, 4, 5)) && selectedDays.size == 5 -> "Weekdays"
            selectedDays.containsAll(listOf(0, 6)) && selectedDays.size == 2 -> "Weekends"
            else -> "${selectedDays.size} days selected"
        }
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DayButton(
    day: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BrutalistButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        enabled = true
    ) {
        Text(
            text = day,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}
