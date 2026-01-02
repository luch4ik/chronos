package com.chronos.alarm.ui.components

/* PENDING CLAUDE REVIEW */
// Phase 2: Design System - TimePicker
// iOS-style wheel picker with haptic feedback on scroll
// Matches React TimePickerWheel.tsx component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.chronos.alarm.ui.utils.rememberHapticFeedback
import com.chronos.alarm.ui.utils.HapticType

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimePicker(
    hour: Int,
    minute: Int,
    onTimeChanged: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val hourListState = rememberLazyListState(initialFirstVisibleItemIndex = hour)
    val minuteListState = rememberLazyListState(initialFirstVisibleItemIndex = minute)
    val coroutineScope = rememberCoroutineScope()
    val haptic = rememberHapticFeedback()
    
    LaunchedEffect(hourListState.firstVisibleItemIndex) {
        if (!hourListState.isScrollInProgress) {
            val newHour = hourListState.firstVisibleItemIndex
            if (newHour != hour) {
                haptic.triggerHaptic(HapticType.LIGHT)
                onTimeChanged(newHour, minute)
            }
        }
    }
    
    LaunchedEffect(minuteListState.firstVisibleItemIndex) {
        if (!minuteListState.isScrollInProgress) {
            val newMinute = minuteListState.firstVisibleItemIndex
            if (newMinute != minute) {
                haptic.triggerHaptic(HapticType.LIGHT)
                onTimeChanged(hour, newMinute)
            }
        }
    }

    Row(
        modifier = modifier
            .border(2.dp, Color.Black)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hour picker
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(200.dp)
        ) {
            LazyColumn(
                state = hourListState,
                flingBehavior = rememberSnapFlingBehavior(lazyListState = hourListState),
                modifier = Modifier.fillMaxSize()
            ) {
                items(24) { index ->
                    val isSelected = index == hourListState.firstVisibleItemIndex
                    TimePickerItem(
                        value = String.format("%02d", index),
                        isSelected = isSelected
                    )
                }
            }
        }

        Text(
            text = ":",
            style = MaterialTheme.typography.displayLarge,
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Minute picker
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(200.dp)
        ) {
            LazyColumn(
                state = minuteListState,
                flingBehavior = rememberSnapFlingBehavior(lazyListState = minuteListState),
                modifier = Modifier.fillMaxSize()
            ) {
                items(60) { index ->
                    val isSelected = index == minuteListState.firstVisibleItemIndex
                    TimePickerItem(
                        value = String.format("%02d", index),
                        isSelected = isSelected
                    )
                }
            }
        }
    }
}

@Composable
private fun TimePickerItem(
    value: String,
    isSelected: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.displayMedium,
            fontSize = if (isSelected) 48.sp else 32.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
