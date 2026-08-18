package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DpadBaseColor
import com.example.ui.theme.DpadButtonNormalColor
import com.example.ui.theme.DpadButtonPressedColor
import com.example.ui.theme.Ps5Blue
import com.example.ui.theme.Ps5CardBorder
import com.example.ui.theme.Ps5Cyan
import com.example.util.Constants

@Composable
fun DpadView(
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    onCommandStateChanged: (String) -> Unit,
) {
    val buttonSize = size / 3.2f

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = Ps5Blue.copy(alpha = 0.3f),
                spotColor = Ps5Cyan.copy(alpha = 0.3f)
            )
            .background(
                color = DpadBaseColor,
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                color = Ps5CardBorder,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Up Button
            DpadButton(
                command = Constants.CMD_DPAD_UP,
                label = "▲",
                size = buttonSize,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                onCommandStateChanged = onCommandStateChanged
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Button
                DpadButton(
                    command = Constants.CMD_DPAD_LEFT,
                    label = "◄",
                    size = buttonSize,
                    shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp, topEnd = 4.dp, bottomEnd = 4.dp),
                    onCommandStateChanged = onCommandStateChanged
                )

                // Center Spacer / Core Accent
                Box(
                    modifier = Modifier
                        .size(buttonSize)
                        .background(
                            color = Color(0xFF0A0D14),
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = Ps5Cyan.copy(alpha = 0.3f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Ps5Cyan, CircleShape)
                    )
                }

                // Right Button
                DpadButton(
                    command = Constants.CMD_DPAD_RIGHT,
                    label = "►",
                    size = buttonSize,
                    shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp, topStart = 4.dp, bottomStart = 4.dp),
                    onCommandStateChanged = onCommandStateChanged
                )
            }

            // Down Button
            DpadButton(
                command = Constants.CMD_DPAD_DOWN,
                label = "▼",
                size = buttonSize,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp, topStart = 4.dp, topEnd = 4.dp),
                onCommandStateChanged = onCommandStateChanged
            )
        }
    }
}

@Composable
private fun DpadButton(
    command: String,
    label: String,
    size: Dp,
    shape: RoundedCornerShape,
    onCommandStateChanged: (String) -> Unit,
) {
    var isPressed by remember { mutableStateOf(value = false) }

    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) DpadButtonPressedColor else DpadButtonNormalColor,
        label = "DpadButtonBgAnim"
    )

    val textColor by animateColorAsState(
        targetValue = if (isPressed) Color.White else Ps5Cyan,
        label = "DpadButtonTextAnim"
    )

    Box(
        modifier = Modifier
            .size(size)
            .padding(2.dp)
            .clip(shape)
            .background(backgroundColor, shape)
            .border(width = 1.dp, color = Ps5CardBorder, shape = shape)
            .pointerInput(command) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onCommandStateChanged(command)
                        tryAwaitRelease()
                        isPressed = false
                        onCommandStateChanged(Constants.CMD_DPAD_STOP)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = label,
            color = textColor,
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge
        )
    }
}
