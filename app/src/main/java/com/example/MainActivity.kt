package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.ConnectionState
import com.example.ui.components.BluetoothDeviceDialog
import com.example.ui.components.DpadView
import com.example.ui.components.JoystickView
import com.example.ui.components.RequestBluetoothPermissions
import com.example.ui.components.StatusIndicator
import com.example.ui.theme.Ps5Background
import com.example.ui.theme.Ps5Blue
import com.example.ui.theme.Ps5Cyan
import com.example.ui.theme.Ps5Surface
import com.example.ui.theme.RobotArmTheme
import com.example.util.Constants
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RobotArmTheme {
                // Runtime Bluetooth Permission Request
                RequestBluetoothPermissions {
                    viewModel.refreshPairedDevices()
                }

                // Listen for Toast events
                LaunchedEffect(Unit) {
                    viewModel.toastEvent.collect { message ->
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                    }
                }

                val connectionState by viewModel.connectionState.collectAsState()
                val pairedDevices by viewModel.pairedDevices.collectAsState()
                val isDialogVisible by viewModel.isDeviceDialogVisible.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Ps5Background,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing),
                    ) {
                        // Main Centered Landscape Layout
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Top Bar Header
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                // Title and LED Status Dot (Centered)
                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "ROBOT ARM",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 4.sp
                                        ),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    StatusIndicator(connectionState = connectionState)
                                }

                                // Bluetooth Device Connection Button (Top Right)
                                IconButton(
                                    onClick = { viewModel.checkBluetoothAndShowDialog() },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(48.dp)
                                        .shadow(
                                            elevation = 6.dp,
                                            shape = CircleShape,
                                            ambientColor = Ps5Blue.copy(alpha = 0.5f),
                                            spotColor = Ps5Cyan.copy(alpha = 0.5f)
                                        )
                                        .background(
                                            color = Ps5Surface,
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bluetooth,
                                        contentDescription = "Mở danh sách Bluetooth",
                                        tint = if (connectionState is ConnectionState.Connected) Ps5Cyan else Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Control Surface: Left Joystick | D-Pad | Right Joystick
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left Joystick (JOY1)
                                JoystickView(
                                    size = 170.dp,
                                    cmdUp = Constants.CMD_JOY1_UP,
                                    cmdDown = Constants.CMD_JOY1_DOWN,
                                    cmdLeft = Constants.CMD_JOY1_LEFT,
                                    cmdRight = Constants.CMD_JOY1_RIGHT,
                                    cmdStop = Constants.CMD_JOY1_STOP
                                ) { cmd -> viewModel.onJoy1StateChanged(cmd) }

                                // Center D-Pad
                                DpadView(
                                    size = 170.dp
                                ) { cmd -> viewModel.onDpadStateChanged(cmd) }

                                // Right Joystick (JOY2)
                                JoystickView(
                                    size = 170.dp,
                                    cmdUp = Constants.CMD_JOY2_UP,
                                    cmdDown = Constants.CMD_JOY2_DOWN,
                                    cmdLeft = Constants.CMD_JOY2_LEFT,
                                    cmdRight = Constants.CMD_JOY2_RIGHT,
                                    cmdStop = Constants.CMD_JOY2_STOP
                                ) { cmd -> viewModel.onJoy2StateChanged(cmd) }
                            }

                            Spacer(modifier = Modifier.weight(1f))
                        }

                        // Bluetooth Paired Device Selector Dialog
                        if (isDialogVisible) {
                            BluetoothDeviceDialog(
                                devices = pairedDevices,
                                onSelectDevice = { device -> viewModel.connectToDevice(device) },
                                onRefreshDevices = { viewModel.refreshPairedDevices() },
                                onDismiss = { viewModel.dismissDeviceDialog() }
                            )
                        }
                    }
                }
            }
        }
    }
}
