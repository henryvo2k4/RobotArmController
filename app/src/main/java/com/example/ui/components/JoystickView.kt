package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JoystickBaseColor
import com.example.ui.theme.JoystickBorderColor
import com.example.ui.theme.JoystickHandleColor
import com.example.ui.theme.Ps5Cyan
import com.example.ui.theme.Ps5Glow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun JoystickView(
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    cmdUp: String,
    cmdDown: String,
    cmdLeft: String,
    cmdRight: String,
    cmdStop: String,
    onCommandStateChanged: (String) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val handleOffsetX = remember { Animatable(0f) }
    val handleOffsetY = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .size(size)
            .pointerInput(Unit) {
                val maxRadius = size.toPx() / 2f
                val handleMaxRadius = maxRadius * 0.55f
                val deadZone = maxRadius * 0.25f

                detectDragGestures(
                    onDragStart = { offset ->
                        val dx = offset.x - maxRadius
                        val dy = offset.y - maxRadius
                        val distance = sqrt((dx * dx) + (dy * dy))
                        val angle = atan2(dy, dx)

                        val clampedDistance = distance.coerceAtMost(handleMaxRadius)
                        val newX = clampedDistance * cos(angle)
                        val newY = clampedDistance * sin(angle)

                        coroutineScope.launch {
                            handleOffsetX.snapTo(newX)
                            handleOffsetY.snapTo(newY)
                        }

                        updateDirectionCommand(
                            dx = newX,
                            dy = newY,
                            deadZone = deadZone,
                            cmdUp = cmdUp,
                            cmdDown = cmdDown,
                            cmdLeft = cmdLeft,
                            cmdRight = cmdRight,
                            cmdStop = cmdStop,
                            onCommandStateChanged = onCommandStateChanged
                        )
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val currentX = handleOffsetX.value + dragAmount.x
                        val currentY = handleOffsetY.value + dragAmount.y

                        val distance = sqrt((currentX * currentX) + (currentY * currentY))
                        val angle = atan2(currentY, currentX)

                        val clampedDistance = distance.coerceAtMost(handleMaxRadius)
                        val newX = clampedDistance * cos(angle)
                        val newY = clampedDistance * sin(angle)

                        coroutineScope.launch {
                            handleOffsetX.snapTo(newX)
                            handleOffsetY.snapTo(newY)
                        }

                        updateDirectionCommand(
                            dx = newX,
                            dy = newY,
                            deadZone = deadZone,
                            cmdUp = cmdUp,
                            cmdDown = cmdDown,
                            cmdLeft = cmdLeft,
                            cmdRight = cmdRight,
                            cmdStop = cmdStop,
                            onCommandStateChanged = onCommandStateChanged
                        )
                    },
                    onDragEnd = {
                        // Smoothly animate handle back to center
                        coroutineScope.launch {
                            launch {
                                handleOffsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                            launch {
                                handleOffsetY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                        }
                        onCommandStateChanged(cmdStop)
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            launch { handleOffsetX.animateTo(0f) }
                            launch { handleOffsetY.animateTo(0f) }
                        }
                        onCommandStateChanged(cmdStop)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val outerRadius = size.toPx() / 2f - 8f
            val handleRadius = outerRadius * 0.38f

            // 1. Draw Outer Base Background with Gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        JoystickBaseColor,
                        Color(0xFF0D121B)
                    ),
                    center = center,
                    radius = outerRadius
                ),
                center = center,
                radius = outerRadius
            )

            // 2. Draw Outer Glowing Ring Border
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(JoystickBorderColor, Ps5Glow.copy(alpha = 0.4f))
                ),
                center = center,
                radius = outerRadius,
                style = Stroke(width = 4.dp.toPx())
            )

            // 3. Draw Directional Guide Dots/Arrows
            val guideOffset = outerRadius * 0.72f
            val dotRadius = 3.dp.toPx()
            // Top, Bottom, Left, Right guide indicators
            drawCircle(color = Ps5Cyan.copy(alpha = 0.6f), radius = dotRadius, center = Offset(center.x, center.y - guideOffset))
            drawCircle(color = Ps5Cyan.copy(alpha = 0.6f), radius = dotRadius, center = Offset(center.x, center.y + guideOffset))
            drawCircle(color = Ps5Cyan.copy(alpha = 0.6f), radius = dotRadius, center = Offset(center.x - guideOffset, center.y))
            drawCircle(color = Ps5Cyan.copy(alpha = 0.6f), radius = dotRadius, center = Offset(center.x + guideOffset, center.y))

            // 4. Draw Animated Joystick Thumb Handle
            val handleCenter = Offset(
                x = center.x + handleOffsetX.value,
                y = center.y + handleOffsetY.value
            )

            // Handle Glow Shadow
            drawCircle(
                color = Ps5Glow.copy(alpha = 0.25f),
                center = handleCenter,
                radius = handleRadius + 6.dp.toPx()
            )

            // Handle Base Gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        JoystickHandleColor,
                        Color(0xFF00439C)
                    ),
                    center = handleCenter,
                    radius = handleRadius
                ),
                center = handleCenter,
                radius = handleRadius
            )

            // Handle Outer Accent Ring
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                center = handleCenter,
                radius = handleRadius,
                style = Stroke(width = 2.dp.toPx())
            )

            // Inner Thumb Grip Pattern (Center Ring + Cross)
            drawCircle(
                color = Color.White.copy(alpha = 0.6f),
                center = handleCenter,
                radius = handleRadius * 0.35f,
                style = Stroke(width = 1.5.dp.toPx())
            )

            val crossLength = handleRadius * 0.25f
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(handleCenter.x - crossLength, handleCenter.y),
                end = Offset(handleCenter.x + crossLength, handleCenter.y),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(handleCenter.x, handleCenter.y - crossLength),
                end = Offset(handleCenter.x, handleCenter.y + crossLength),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

private fun updateDirectionCommand(
    dx: Float,
    dy: Float,
    deadZone: Float,
    cmdUp: String,
    cmdDown: String,
    cmdLeft: String,
    cmdRight: String,
    cmdStop: String,
    onCommandStateChanged: (String) -> Unit
) {
    val distance = sqrt((dx * dx) + (dy * dy))
    if (distance < deadZone) {
        onCommandStateChanged(cmdStop)
        return
    }

    if (abs(dx) > abs(dy)) {
        if (dx > 0) {
            onCommandStateChanged(cmdRight)
        } else {
            onCommandStateChanged(cmdLeft)
        }
    } else {
        if (dy > 0) {
            onCommandStateChanged(cmdDown)
        } else {
            onCommandStateChanged(cmdUp)
        }
    }
}
