package com.example.bluetooth

data class BluetoothDeviceDomain(
    val name: String,
    val address: String,
    val isTargetEsp32: Boolean = false,
)
