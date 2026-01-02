package com.chronos.alarm.ui.components

// Phase 2: Design System - ClockDisplay
// Implements brutalist clock with date tag, PM/AM badge, and blinking colon
// Matches React ClockDisplay.tsx behavior

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chronos.alarm.ui.theme.BrutalistTag
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ClockDisplay(
    date: Date,
    timeFormat: String,
    modifier: Modifier = Modifier
) {
    val calendar = Calendar.getInstance().apply { time = date }
    val hours = calendar.get(Calendar.HOUR_OF_DAY)
    val minutes = calendar.get(Calendar.MINUTE)
    val dayString = formatDate(date)
    
    val (displayHours, period) = if (timeFormat == "12h") {
        val h12 = if (hours % 12 == 0) 12 else hours % 12
        Pair(h12, if (hours >= 12) "PM" else "AM")
    } else {
        Pair(hours, "")
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "colon_blink")
    val colonVisible by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colon_blink"
    )
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Date tag
        BrutalistTag(
            text = dayString,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .rotate(-2f)
        )
        
        // Main clock
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = displayHours.toString().padStart(2, '0'),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Bold,
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black,
                        offset = Offset(4f, 4f),
                        blurRadius = 0f
                    )
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = ":",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Bold,
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black,
                        offset = Offset(4f, 4f),
                        blurRadius = 0f
                    )
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = colonVisible)
            )
            
            Text(
                text = minutes.toString().padStart(2, '0'),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Bold,
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black,
                        offset = Offset(4f, 4f),
                        blurRadius = 0f
                    )
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            
            if (timeFormat == "12h" && period.isNotEmpty()) {
                BrutalistTag(
                    text = period,
                    modifier = Modifier
                        .padding(start = 16.dp, bottom = 16.dp)
                        .rotate(6f)
                )
            }
        }
    }
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("EEEE, MMM d", Locale.US)
    return formatter.format(date)
}
