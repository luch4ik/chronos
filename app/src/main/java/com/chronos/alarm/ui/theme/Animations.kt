package com.chronos.alarm.ui.theme

/* PENDING CLAUDE REVIEW */
// Phase 2: Design System - Animation System
// Spring-based animations matching Framer Motion patterns
// Created by: general agent (Task tool session ses_484e60b33ffei4JvgOUr88S0mo)

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun enterAnimation(
    visible: Boolean = true
): (Modifier) -> Modifier {
    val transition = updateTransition(visible, label = "enter")

    val alpha by transition.animateFloat(
        transitionSpec = { tween(400, easing = FastOutSlowInEasing) },
        label = "alpha"
    ) { if (it) 1f else 0f }

    val scale by transition.animateFloat(
        transitionSpec = { spring(stiffness = 300f, dampingRatio = 20f / (2 * kotlin.math.sqrt(300f))) },
        label = "scale"
    ) { if (it) 1f else 0.95f }

    val offsetY by transition.animateFloat(
        transitionSpec = { spring(stiffness = 300f, dampingRatio = 20f / (2 * kotlin.math.sqrt(300f))) },
        label = "offsetY"
    ) { if (it) 0f else 20f }

    return { modifier ->
        modifier.graphicsLayer {
            this.alpha = alpha
            this.scaleX = scale
            this.scaleY = scale
            this.translationY = offsetY
        }
    }
}