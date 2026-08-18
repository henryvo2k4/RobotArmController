package com.example.util

import java.util.UUID

object Constants {
    // Bluetooth Classic SPP Standard Serial Port Profile UUID
    val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // Default ESP32 Device Name
    const val DEFAULT_ESP32_NAME = "RobotArm_BT"

    // Protocol Commands (UTF-8 terminated by '\n')
    // Left Joystick (JOY1)
    const val CMD_JOY1_LEFT = "JOY1_L"
    const val CMD_JOY1_RIGHT = "JOY1_R"
    const val CMD_JOY1_UP = "JOY1_U"
    const val CMD_JOY1_DOWN = "JOY1_D"
    const val CMD_JOY1_STOP = "JOY1_S"

    // Right Joystick (JOY2)
    const val CMD_JOY2_LEFT = "JOY2_L"
    const val CMD_JOY2_RIGHT = "JOY2_R"
    const val CMD_JOY2_UP = "JOY2_U"
    const val CMD_JOY2_DOWN = "JOY2_D"
    const val CMD_JOY2_STOP = "JOY2_S"

    // D-Pad Commands
    const val CMD_DPAD_UP = "DPAD_U"
    const val CMD_DPAD_DOWN = "DPAD_D"
    const val CMD_DPAD_LEFT = "DPAD_L"
    const val CMD_DPAD_RIGHT = "DPAD_R"
    const val CMD_DPAD_STOP = "DPAD_S"
}
