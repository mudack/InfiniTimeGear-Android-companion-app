package com.mandarinpirate.data.local.ble

import android.Manifest
import android.content.BroadcastReceiver
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.mandarinpirate.data.utilConnectGatt
import com.mandarinpirate.domain.models.BleConnectionState
import com.mandarinpirate.domain.models.BluetoothDevice
import com.mandarinpirate.domain.repos.BleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BleRepoImpl(
    private val bluetoothAdapter: BluetoothAdapter,
    private val context: Context
) : BleRepository {
    private val connectionLock = Any()
    private val connectionScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _connectionState = MutableStateFlow<BleConnectionState>(BleConnectionState.Disconnected)

    override val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    private var activeDevice: BluetoothDevice? = null
    private var currentGatt: BluetoothGatt? = null
    private var reconnectJob: Job? = null
    private var reconnectEnabled = false
    private var reconnectAttempt = 0
    private var isBluetoothStateReceiverRegistered = false

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != BluetoothAdapter.ACTION_STATE_CHANGED) return

            when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                BluetoothAdapter.STATE_TURNING_OFF,
                BluetoothAdapter.STATE_OFF,
                BluetoothAdapter.STATE_TURNING_ON -> onBluetoothUnavailable()

                BluetoothAdapter.STATE_ON -> onBluetoothEnabled()
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun connect(device: BluetoothDevice, reconnectEnabled: Boolean) {
        synchronized(connectionLock) {
            cancelReconnectLocked()
            closeCurrentGattLocked()
            activeDevice = device
            this.reconnectEnabled = reconnectEnabled
            reconnectAttempt = 0
            registerBluetoothStateReceiverLocked()
        }
        connectToActiveDevice()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun disconnect() {
        synchronized(connectionLock) {
            activeDevice = null
            reconnectEnabled = false
            reconnectAttempt = 0
            cancelReconnectLocked()
            closeCurrentGattLocked()
            unregisterBluetoothStateReceiverLocked()
            _connectionState.value = BleConnectionState.Disconnected
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun connectToActiveDevice() {
        val device = synchronized(connectionLock) {
            activeDevice ?: return
        }

        if (!bluetoothAdapter.isEnabled) {
            _connectionState.value = BleConnectionState.WaitingForBluetooth
            return
        }

        _connectionState.value = BleConnectionState.Connecting
        val remoteDevice = runCatching {
            bluetoothAdapter.getRemoteDevice(device.macAddress)
        }.getOrElse {
            Log.e(TAG, "Unable to resolve the Bluetooth device", it)
            scheduleReconnect()
            return
        }

        val gatt = runCatching {
            remoteDevice.utilConnectGatt(
                context = context,
                autoConnect = false,
                bluetoothGattCallback = connectionCallback
            )
        }.getOrElse {
            Log.e(TAG, "Unable to start the GATT connection", it)
            scheduleReconnect()
            return
        }

        synchronized(connectionLock) {
            if (device != activeDevice) {
                gatt?.close()
                return
            }
            currentGatt = gatt
        }

        if (gatt == null) {
            scheduleReconnect()
        }
    }

    private val connectionCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> onConnected(gatt)
                BluetoothProfile.STATE_DISCONNECTED -> onDisconnected(gatt, status)
            }
        }
    }

    private fun onConnected(gatt: BluetoothGatt) {
        synchronized(connectionLock) {
            if (gatt != currentGatt) {
                gatt.close()
                return
            }
            cancelReconnectLocked()
            reconnectAttempt = 0
            _connectionState.value = BleConnectionState.Connected
        }
    }

    private fun onDisconnected(gatt: BluetoothGatt, status: Int) {
        synchronized(connectionLock) {
            if (gatt != currentGatt) {
                gatt.close()
                return
            }
            currentGatt = null
            gatt.close()
        }
        Log.w(TAG, "GATT disconnected with status=$status")
        scheduleReconnect()
    }

    private fun scheduleReconnect() {
        val delayMillis = synchronized(connectionLock) {
            if (!reconnectEnabled || activeDevice == null) {
                _connectionState.value = BleConnectionState.Disconnected
                return
            }

            reconnectAttempt += 1
            _connectionState.value = BleConnectionState.Reconnecting(reconnectAttempt)
            reconnectDelayMillis(reconnectAttempt)
        }

        reconnectJob?.cancel()
        reconnectJob = connectionScope.launch {
            delay(delayMillis)
            connectToActiveDevice()
        }
    }

    private fun cancelReconnectLocked() {
        reconnectJob?.cancel()
        reconnectJob = null
    }

    private fun onBluetoothUnavailable() {
        synchronized(connectionLock) {
            if (activeDevice == null) return

            cancelReconnectLocked()
            closeCurrentGattLocked()
            _connectionState.value = BleConnectionState.WaitingForBluetooth
        }
    }

    private fun onBluetoothEnabled() {
        val shouldReconnect = synchronized(connectionLock) {
            activeDevice != null && reconnectEnabled
        }
        if (shouldReconnect) {
            connectToActiveDevice()
        }
    }

    private fun registerBluetoothStateReceiverLocked() {
        if (isBluetoothStateReceiverRegistered) return

        ContextCompat.registerReceiver(
            context,
            bluetoothStateReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        isBluetoothStateReceiverRegistered = true
    }

    private fun unregisterBluetoothStateReceiverLocked() {
        if (!isBluetoothStateReceiverRegistered) return

        context.unregisterReceiver(bluetoothStateReceiver)
        isBluetoothStateReceiverRegistered = false
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun closeCurrentGattLocked() {
        val gatt = currentGatt
        currentGatt = null
        gatt?.run {
            disconnect()
            close()
        }
    }

    private fun reconnectDelayMillis(attempt: Int): Long = when (attempt) {
        1 -> 2_000L
        2 -> 5_000L
        3 -> 10_000L
        4 -> 30_000L
        else -> 60_000L
    }

    companion object {
        const val TAG = "BleRepoImpl"
    }
}
