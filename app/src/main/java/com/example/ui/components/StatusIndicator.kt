package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bluetooth.ConnectionState
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenGlow
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedGlow

@Composable
fun StatusIndicator(
    connectionState: ConnectionState,
    modifier: Modifier = Modifier,
) {
    val isConnected = connectionState is ConnectionState.Connected

    val ledColor by animateColorAsState(
        targetValue = if (isConnected) StatusGreen else StatusRed,
        animationSpec = tween(durationMillis = 500),
        label = "LedColorAnimation"
    )

    val glowColor by animateColorAsState(
        targetValue = if (isConnected) StatusGreenGlow else StatusRedGlow,
        animationSpec = tween(durationMillis = 500),
        label = "LedGlowAnimation"
    )

    Box(
        modifier = modifier
            .size(20.dp)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = glowColor,
                spotColor = glowColor
            )
            .background(color = ledColor, shape = CircleShape)
            .border(
                width = 2.dp,
                color = Color.White.copy(alpha = 0.4f),
                shape = CircleShape
            )
    )
}
