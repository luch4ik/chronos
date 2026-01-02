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
    // Number of padding items above and below to allow centering
    val paddingItems = 2

    // Calculate initial scroll position to center the selected item
    val hourListState = rememberLazyListState(initialFirstVisibleItemIndex = hour + paddingItems)
    val minuteListState = rememberLazyListState(initialFirstVisibleItemIndex = minute + paddingItems)
    val coroutineScope = rememberCoroutineScope()
    val haptic = rememberHapticFeedback()

    // Track last reported values to avoid duplicate callbacks
    var lastReportedHour by remember { mutableStateOf(hour) }
    var lastReportedMinute by remember { mutableStateOf(minute) }

    // Sync picker position when hour prop changes externally
    LaunchedEffect(hour) {
        if (hour != lastReportedHour && !hourListState.isScrollInProgress) {
            hourListState.animateScrollToItem(hour + paddingItems)
            lastReportedHour = hour
        }
    }

    // Sync picker position when minute prop changes externally
    LaunchedEffect(minute) {
        if (minute != lastReportedMinute && !minuteListState.isScrollInProgress) {
            minuteListState.animateScrollToItem(minute + paddingItems)
            lastReportedMinute = minute
        }
    }

    // Detect hour changes from user scrolling
    LaunchedEffect(hourListState.firstVisibleItemIndex, hourListState.isScrollInProgress) {
        if (!hourListState.isScrollInProgress) {
            // The selected item is the one at the center, accounting for padding
            val selectedIndex = hourListState.firstVisibleItemIndex - paddingItems
            val newHour = selectedIndex.coerceIn(0, 23)
            if (newHour != lastReportedHour) {
                lastReportedHour = newHour
                haptic.triggerHaptic(HapticType.LIGHT)
                onTimeChanged(newHour, minute)
            }
        }
    }

    // Detect minute changes from user scrolling
    LaunchedEffect(minuteListState.firstVisibleItemIndex, minuteListState.isScrollInProgress) {
        if (!minuteListState.isScrollInProgress) {
            // The selected item is the one at the center, accounting for padding
            val selectedIndex = minuteListState.firstVisibleItemIndex - paddingItems
            val newMinute = selectedIndex.coerceIn(0, 59)
            if (newMinute != lastReportedMinute) {
                lastReportedMinute = newMinute
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
                // Add padding items to allow first and last items to be centered
                items(paddingItems) {
                    TimePickerItem(value = "", isSelected = false)
                }

                items(24) { index ->
                    val isSelected = (index + paddingItems) == hourListState.firstVisibleItemIndex
                    TimePickerItem(
                        value = String.format("%02d", index),
                        isSelected = isSelected
                    )
                }

                // Add padding items at the bottom
                items(paddingItems) {
                    TimePickerItem(value = "", isSelected = false)
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
                // Add padding items to allow first and last items to be centered
                items(paddingItems) {
                    TimePickerItem(value = "", isSelected = false)
                }

                items(60) { index ->
                    val isSelected = (index + paddingItems) == minuteListState.firstVisibleItemIndex
                    TimePickerItem(
                        value = String.format("%02d", index),
                        isSelected = isSelected
                    )
                }

                // Add padding items at the bottom
                items(paddingItems) {
                    TimePickerItem(value = "", isSelected = false)
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
