package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.BluetoothDeviceDomain
import com.example.bluetooth.ConnectionState
import com.example.bluetooth.RobotBluetoothManager
import com.example.util.Constants
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val bluetoothManager = RobotBluetoothManager(application)

    val connectionState: StateFlow<ConnectionState> = bluetoothManager.connectionState

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDeviceDomain>> = _pairedDevices.asStateFlow()

    private val _isDeviceDialogVisible = MutableStateFlow(value = false)
    val isDeviceDialogVisible: StateFlow<Boolean> = _isDeviceDialogVisible.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Command state-change tracking to ensure commands are sent ONLY ONCE when state changes
    private var lastJoy1Command: String = Constants.CMD_JOY1_STOP
    private var lastJoy2Command: String = Constants.CMD_JOY2_STOP
    private var lastDpadCommand: String = Constants.CMD_DPAD_STOP

    fun checkBluetoothAndShowDialog() {
        if (!bluetoothManager.isBluetoothEnabled) {
            emitToast("Vui lòng bật Bluetooth trên thiết bị")
            return
        }
        refreshPairedDevices()
        _isDeviceDialogVisible.value = true
    }

    fun dismissDeviceDialog() {
        _isDeviceDialogVisible.value = false
    }

    fun refreshPairedDevices() {
        val devices = bluetoothManager.getPairedDevices()
        _pairedDevices.value = devices
        if (devices.isEmpty()) {
            emitToast("Không tìm thấy thiết bị Bluetooth đã ghép đôi")
        }
    }

    fun connectToDevice(device: BluetoothDeviceDomain) {
        _isDeviceDialogVisible.value = false
        bluetoothManager.connectToDevice(device.address, device.name)
    }

    // Left Joystick Command Control
    fun onJoy1StateChanged(command: String) {
        if (command != lastJoy1Command) {
            lastJoy1Command = command
            sendCommandInternal(command)
        }
    }

    // Right Joystick Command Control
    fun onJoy2StateChanged(command: String) {
        if (command != lastJoy2Command) {
            lastJoy2Command = command
            sendCommandInternal(command)
        }
    }

    // D-Pad Command Control
    fun onDpadStateChanged(command: String) {
        if (command != lastDpadCommand) {
            lastDpadCommand = command
            sendCommandInternal(command)
        }
    }

    private fun sendCommandInternal(command: String) {
        val success = bluetoothManager.sendCommand(command)
        if (!success && (connectionState.value is ConnectionState.Disconnected)) {
            emitToast("Chưa kết nối Bluetooth")
        }
    }

    private fun emitToast(message: String) {
        viewModelScope.launch {
            _toastEvent.emit(message)
        }
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothManager.disconnect()
    }
}
