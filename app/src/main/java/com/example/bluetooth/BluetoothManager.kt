package com.example.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.example.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class RobotBluetoothManager(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(BluetoothManager::class.java)
        manager?.adapter
    }

    private var socket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private var readJob: Job? = null

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDeviceDomain> {
        val adapter = bluetoothAdapter ?: return emptyList()
        if (!adapter.isEnabled) return emptyList()

        return try {
            val pairedDevices: Set<BluetoothDevice>? = adapter.bondedDevices
            pairedDevices?.asSequence()?.map { device ->
                val deviceName = device.name ?: "Unknown Device"
                val deviceAddress = device.address
                BluetoothDeviceDomain(
                    name = deviceName,
                    address = deviceAddress,
                    isTargetEsp32 = deviceName.equals(Constants.DEFAULT_ESP32_NAME, ignoreCase = true),
                )
            }?.sortedByDescending { it.isTargetEsp32 }?.toList() ?: emptyList()
        } catch (_: SecurityException) {
            emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(deviceAddress: String, deviceName: String) {
        val adapter = bluetoothAdapter
        if ((adapter == null) || (!adapter.isEnabled)) {
            _connectionState.value = ConnectionState.Error("Bluetooth chưa được bật")
            return
        }

        coroutineScope.launch {
            _connectionState.value = ConnectionState.Connecting
            closeConnectionInternal()

            try {
                // Cancel discovery as it slows down connection
                adapter.cancelDiscovery()

                val device: BluetoothDevice = adapter.getRemoteDevice(deviceAddress)
                val newSocket = device.createRfcommSocketToServiceRecord(Constants.SPP_UUID)
                newSocket.connect()

                socket = newSocket
                outputStream = newSocket.outputStream
                inputStream = newSocket.inputStream

                _connectionState.value = ConnectionState.Connected(
                    deviceName = deviceName.ifEmpty { Constants.DEFAULT_ESP32_NAME },
                    address = deviceAddress,
                )

                // Start listening for unexpected disconnects
                startReadLoop()

            } catch (e: Exception) {
                closeConnectionInternal()
                _connectionState.value = ConnectionState.Error(
                    e.localizedMessage ?: "Kết nối thất bại. Vui lòng thử lại."
                )
            }
        }
    }

    fun sendCommand(command: String): Boolean {
        if (_connectionState.value !is ConnectionState.Connected) {
            return false
        }

        val stream = outputStream ?: return false

        return try {
            val payload = (command.trim() + "\n").toByteArray(Charsets.UTF_8)
            stream.write(payload)
            stream.flush()
            true
        } catch (_: IOException) {
            // Socket write failed, connection lost
            handleDisconnect()
            false
        }
    }

    private fun startReadLoop() {
        readJob?.cancel()
        readJob = coroutineScope.launch {
            val buffer = ByteArray(1024)
            while (socket?.isConnected == true) {
                try {
                    val stream = inputStream ?: break
                    val bytesRead = stream.read(buffer)
                    if (bytesRead == -1) {
                        handleDisconnect()
                        break
                    }
                } catch (_: IOException) {
                    handleDisconnect()
                    break
                }
            }
        }
    }

    private fun handleDisconnect() {
        coroutineScope.launch {
            closeConnectionInternal()
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    fun disconnect() {
        coroutineScope.launch {
            closeConnectionInternal()
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    private suspend fun closeConnectionInternal() = withContext(Dispatchers.IO) {
        readJob?.cancel()
        readJob = null

        try {
            inputStream?.close()
        } catch (_: IOException) {}
        inputStream = null

        try {
            outputStream?.close()
        } catch (_: IOException) {}
        outputStream = null

        try {
            socket?.close()
        } catch (_: IOException) {}
        socket = null
    }
}
