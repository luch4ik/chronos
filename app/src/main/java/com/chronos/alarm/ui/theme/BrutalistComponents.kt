package com.chronos.alarm.ui.theme

// Phase 2: Design System - Brutalist Components
// Implements reusable brutalist UI components with hover/press states and haptic feedback
// BrutalistButton: Button with spring-based shadow animation
// BrutalistCard: Card with spring-based shadow animation
// BrutalistTextField: Text input with brutalist styling
// BrutalistSwitch: Toggle switch with animation
// BrutalistTag: Badge component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chronos.alarm.ui.utils.HapticType
import com.chronos.alarm.ui.utils.rememberHapticFeedback

// Helper function for hard brutalist shadow - draws shadow BEHIND the component
fun Modifier.hardShadow(
    offsetX: Dp = 4.dp,
    offsetY: Dp = 4.dp,
    color: Color = Color.Black,
    cornerRadius: Dp = 8.dp
) = this.then(
    Modifier.drawBehind {
        val shadowOffset = Offset(offsetX.toPx(), offsetY.toPx())
        // Draw shadow behind everything
        drawRoundRect(
            color = color,
            topLeft = shadowOffset,
            size = size,
            cornerRadius = CornerRadius(cornerRadius.toPx())
        )
    }
)

@Composable
fun BrutalistCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(8.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val haptic = rememberHapticFeedback()

    val offsetX by animateFloatAsState(
        targetValue = when {
            isPressed -> 4f
            isHovered -> -1f
            else -> 0f
        },
        label = "card_offsetX",
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.8f)
    )

    val offsetY by animateFloatAsState(
        targetValue = when {
            isPressed -> 4f
            isHovered -> -1f
            else -> 0f
        },
        label = "card_offsetY",
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.8f)
    )

    Box(modifier = modifier) {
        // Shadow layer (stays in place)
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(Color.Black, shape)
        )
        
        // Card (moves on press)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = offsetX.dp, y = offsetY.dp)
                .border(2.dp, Color.Black, shape)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            haptic.triggerHaptic(HapticType.LIGHT)
                            onClick()
                        }
                    } else Modifier
                ),
            shape = shape,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                content = content
            )
        }
    }
}

@Composable
fun BrutalistButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White
) {
    BrutalistButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        containerColor = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BrutalistButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    onPressStart: () -> Unit = {},
    onPressEnd: () -> Unit = {},
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val haptic = rememberHapticFeedback()

    val offsetX by animateFloatAsState(
        targetValue = when {
            isPressed -> 4f
            isHovered -> -1f
            else -> 0f
        },
        label = "button_offsetX",
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.8f)
    )

    val offsetY by animateFloatAsState(
        targetValue = when {
            isPressed -> 4f
            isHovered -> -1f
            else -> 0f
        },
        label = "button_offsetY",
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.8f)
    )

    Box(modifier = modifier) {
        // Shadow layer (stays in place)
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(Color.Black, RoundedCornerShape(8.dp))
        )
        
        // Button (moves on press)
        Button(
            onClick = {
                haptic.triggerHaptic(HapticType.MEDIUM)
                onClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = offsetX.dp, y = offsetY.dp)
                .border(2.dp, Color.Black, RoundedCornerShape(8.dp)),
            enabled = enabled,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.White
            ),
            interactionSource = interactionSource,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp
            ),
            content = content
        )
    }
}

@Composable
fun BrutalistTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .border(
                2.dp,
                if (isError) Color.Red else Color.Black,
                RoundedCornerShape(8.dp)
            )
            .shadow(
                elevation = 2.dp,
                spotColor = Color.Black,
                ambientColor = Color.Black,
                shape = RoundedCornerShape(8.dp)
            ),
        placeholder = placeholder,
        singleLine = singleLine,
        maxLines = maxLines,
        isError = isError,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            errorContainerColor = Color(0xFFFFEBEE),
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent
        )
    )
}

@Composable
fun BrutalistSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()
    
    Switch(
        checked = checked,
        onCheckedChange = {
            haptic.triggerHaptic(HapticType.MEDIUM)
            onCheckedChange(it)
        },
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.primary,
            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            checkedBorderColor = Color.Black,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = Color.LightGray,
            uncheckedBorderColor = Color.Black
        )
    )
}

@Composable
fun BrutalistTag(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = Color.White,
    rotation: Float = 0f
) {
    Box(modifier = modifier.wrapContentSize()) {
        // Shadow layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 2.dp, y = 2.dp)
                .background(Color.Black, RoundedCornerShape(4.dp))
        )
        
        // Tag content
        Surface(
            modifier = Modifier
                .border(2.dp, Color.Black, RoundedCornerShape(4.dp)),
            color = backgroundColor,
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = text.uppercase(),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
