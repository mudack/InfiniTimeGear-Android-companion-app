package com.mandarinpirate.data.local.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import com.mandarinpirate.data.utilConnectGatt
import com.mandarinpirate.domain.models.BleConnectionState
import com.mandarinpirate.domain.models.BluetoothDevice as DBluetoothDevice
import com.mandarinpirate.domain.repos.BleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat

class MyBleRepoImpl(  //todo check all edge cases and consider right behavior every place where throws are
    private val bluetoothAdapter: BluetoothAdapter,
    private val context: Context
) : BleRepository {

    /** Hints for my self
     * do not lock the coroutine with a long operation
     * always check a state in locked operation if there is long operation
     */
    private val _connectionState =
        MutableStateFlow<BleConnectionState>(BleConnectionState.Disconnected)

    override val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    private val connectionScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectionMutex = Mutex()

    private var activeDevice: DBluetoothDevice? = null
    private var currentGatt: BluetoothGatt? = null
    private var reconnectEnabled = false


    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> onConnected(gatt)
                BluetoothProfile.STATE_DISCONNECTED -> onDisconnected(gatt, status)
            }
        }
    }

    private var isBluetoothStateReceiverRegistered: Boolean = false
    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
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
    override fun connect(
        device: DBluetoothDevice,
        reconnectEnabled: Boolean
    ) {
        connectionScope.launch {
            connectionMutex.withLock {
                activeDevice = device
                this@MyBleRepoImpl.reconnectEnabled = reconnectEnabled
            }
            registerBluetoothStateReceiverLocked()
            connectToActiveDeviceLocked()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun connectToActiveDeviceLocked() {
        val device = connectionMutex.withLock {
            activeDevice
                ?: throw IllegalStateException("activeDevice cannot be null here")
        }

        if (!bluetoothAdapter.isEnabled) {
            _connectionState.value = BleConnectionState.WaitingForBluetooth
            throw IllegalStateException("Bluetooth is disabled enable it please")
        }

        _connectionState.value = BleConnectionState.Connecting

        val remoteDevice: BluetoothDevice = runCatching {
            bluetoothAdapter.getRemoteDevice(device.macAddress)
        }.getOrElse {
            Log.e(TAG, "Unable to resolve the Bluetooth device", it)
            throw it
        }

        val currentActiveDevice = connectionMutex.withLock { activeDevice }

        if (device !== currentActiveDevice) throw IllegalStateException("race condition $device != $currentActiveDevice ")

        val gatt = runCatching {
            remoteDevice.utilConnectGatt(
                context = context,
                autoConnect = false,
                bluetoothGattCallback = bluetoothGattCallback
            )
        }.getOrElse {
            Log.e(TAG, "Unable to start the GATT connection", it)
            throw it
        }

        connectionMutex.withLock {
            if (activeDevice?.macAddress != remoteDevice.address) {
                gatt?.close()
                throw IllegalStateException("addresses of remote device and active device are not the same, race condition")
            }
            currentGatt = gatt
        }
    }


    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun disconnect() {
        connectionScope.launch {
            connectionMutex.withLock {
                activeDevice = null
                reconnectEnabled = false
                closeCurrentGattLocked()
                unregisterBluetoothStateReceiverLocked()
            }
            _connectionState.value = BleConnectionState.Disconnected
        }
    }


    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun closeCurrentGattLocked() { //make sure this func is called into locked blocks by mutex
        currentGatt?.run {
            disconnect()//todo move it out of mutex lock properly
            close()//todo move it out of mutex lock properly
        }
        currentGatt = null
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun onConnected(gatt: BluetoothGatt) = connectionScope.launch {
        connectionMutex.withLock {
            if (gatt != currentGatt) {
                closeCurrentGattLocked()
            }
            currentGatt = gatt
        }
        _connectionState.value = BleConnectionState.Connected

    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun onDisconnected(gatt: BluetoothGatt, status: Int) = connectionScope.launch {
        connectionMutex.withLock {
            if (gatt === currentGatt) {
                currentGatt = null
            }
        }
        gatt.close()

        _connectionState.value = BleConnectionState.Disconnected
        //in the future i could parse status and show it in the state
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

    @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    private fun onBluetoothUnavailable() = connectionScope.launch {
        connectionMutex.withLock {
            if (activeDevice == null) return@launch

            closeCurrentGattLocked()
            _connectionState.value = BleConnectionState.WaitingForBluetooth
        }
    }

    @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    private fun onBluetoothEnabled() = connectionScope.launch {
        val shouldReconnect = connectionMutex.withLock {
            activeDevice != null && reconnectEnabled
        }
        if (shouldReconnect) connectToActiveDeviceLocked()
    }


    companion object {
        const val TAG = "MyBleRepoImpl"
    }
}