package com.chronos.alarm.ui.components

/* PENDING CLAUDE REVIEW */
// Phase 2: Design System - AlarmItem
// Displays alarm with all badges: schedule, challenges, wake-up check, SOS, audio
// Matches React AlarmItem.tsx component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chronos.alarm.domain.model.Alarm
import com.chronos.alarm.domain.model.ChallengeType
import com.chronos.alarm.ui.theme.BrutalistCard
import com.chronos.alarm.ui.theme.BrutalistSwitch
import com.chronos.alarm.ui.theme.BrutalistTag
import com.chronos.alarm.ui.theme.enterAnimation
import com.chronos.alarm.ui.utils.rememberHapticFeedback
import com.chronos.alarm.ui.utils.HapticType

@Composable
fun AlarmItem(
    alarm: Alarm,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit
) {
    val animation = enterAnimation()
    val haptic = rememberHapticFeedback()
    
    BrutalistCard(
        onClick = onEdit,
        modifier = animation(Modifier.fillMaxWidth())
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = alarm.time,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    if (!alarm.isActive) {
                        Text(
                            text = "(OFF)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    ScheduleTag(alarm)
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BrutalistSwitch(
                        checked = alarm.isActive,
                        onCheckedChange = { onToggle() }
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(onClick = {
                        haptic.triggerHaptic(HapticType.HEAVY)
                        onDelete()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChallengeIcons(alarm)
                WakeUpCheckBadge(alarm)
                SOSBadge(alarm)
                AudioIcon(alarm)
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = alarm.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ScheduleTag(alarm: Alarm) {
    val tag = when {
        alarm.days.isEmpty() -> "ONCE"
        alarm.days.size == 7 -> "DAILY"
        alarm.days.size == 5 && alarm.days == listOf(1, 2, 3, 4, 5) -> "WEEKDAYS"
        alarm.days.size == 2 && alarm.days == listOf(0, 6) -> "WEEKENDS"
        else -> {
            val dayNames = listOf("S", "M", "T", "W", "T", "F", "S")
            alarm.days.sorted().joinToString(" ") { dayNames[it] }
        }
    }
    
    BrutalistTag(
        text = tag,
        backgroundColor = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ChallengeIcons(alarm: Alarm) {
    if (alarm.challenges.isEmpty()) return
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        alarm.challenges.take(3).forEach { challenge ->
            ChallengeIconBadge(challenge.type)
        }
        
        if (alarm.challenges.size > 3) {
            Text(
                text = "+${alarm.challenges.size - 3}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ChallengeIconBadge(type: ChallengeType) {
    val (icon, label) = when (type) {
        ChallengeType.MATH -> Icons.Default.Calculate to "MATH"
        ChallengeType.BURST -> Icons.Default.TouchApp to "BURST"
        ChallengeType.MEMORY -> Icons.Default.Memory to "MEMORY"
        ChallengeType.TYPING -> Icons.Default.Keyboard to "TYPING"
        ChallengeType.BLUETOOTH -> Icons.Default.Bluetooth to "BLUETOOTH"
        ChallengeType.VELOCITY -> Icons.Default.DirectionsRun to "VELOCITY"
    }
    
    Surface(
        modifier = Modifier.size(28.dp),
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.primary,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun WakeUpCheckBadge(alarm: Alarm) {
    if (alarm.wakeUpCheck?.enabled != true) return
    
    Surface(
        modifier = Modifier.size(28.dp),
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.primary,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = "Wake up check",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SOSBadge(alarm: Alarm) {
    if (alarm.emergencyContact?.enabled != true) return
    
    Surface(
        modifier = Modifier.size(28.dp),
        shape = RoundedCornerShape(4.dp),
        color = Color.Red,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationImportant,
                contentDescription = "SOS",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun AudioIcon(alarm: Alarm) {
    val icon = when (alarm.audio.source) {
        "GENERATED" -> Icons.Default.MusicNote
        "SYSTEM" -> Icons.Default.Notifications
        "URL" -> Icons.Default.Cloud
        "FILE" -> Icons.Default.AudioFile
        else -> Icons.Default.MusicNote
    }
    
    Surface(
        modifier = Modifier.size(28.dp),
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Audio source",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
