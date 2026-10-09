package com.mandarinpirate.data.local.ble.controllers.connection

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import com.mandarinpirate.data.utilConnectGatt
import com.mandarinpirate.domain.models.BleConnectionState
import com.mandarinpirate.domain.models.BluetoothDevice
import com.mandarinpirate.domain.repos.BleConnectionController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException

@SuppressLint("MissingPermission")
class SingleDeviceBleConnectionController(
    private val bluetoothAdapter: BluetoothAdapter,
    private val context: Context
) : BleConnectionController {

    /** Hints for my self
     * do not lock the coroutine with a long operation
     * always check a state in locked operation if there is long operation
     */
    private val _connectionState =
        MutableStateFlow<BleConnectionState>(BleConnectionState.Disconnected)

    override val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    private val connectionScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectionMutex = Mutex()

    private var activeDevice: BluetoothDevice? = null
    private var currentGatt: BluetoothGatt? = null
    private var reconnectEnabled = false

    private val events = Channel<BleEvents>(Channel.UNLIMITED)

    init {
        connectionScope.launch {
            for (event in events) {
                try {
                    handleEvent(event)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to handle BLE event: $event", e)
                }
            }
        }
    }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            // TODO: Handle failed event delivery if the channel is closed or trySend() fails.
            when (newState) {
                BluetoothProfile.STATE_CONNECTED ->
                    events.trySend(BleEvents.Connected(gatt))

                BluetoothProfile.STATE_DISCONNECTED ->
                    events.trySend(BleEvents.Disconnected(gatt, status))
            }
        }
    }


    private suspend fun handleEvent(event: BleEvents) {
        when (event) {
            BleEvents.BluetoothEnabled -> onBluetoothEnabled()
            BleEvents.BluetoothDisabled -> onBluetoothDisabled()
            is BleEvents.Connected -> onConnected(event.gatt)
            is BleEvents.Disconnected -> onDisconnected(event.gatt, event.status)
        }
    }


    private var isBluetoothStateReceiverRegistered: Boolean = false
    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != BluetoothAdapter.ACTION_STATE_CHANGED) return

            // TODO: Handle failed event delivery if the channel is closed or trySend() fails.
            when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                BluetoothAdapter.STATE_TURNING_OFF,
                BluetoothAdapter.STATE_OFF,
                BluetoothAdapter.STATE_TURNING_ON -> events.trySend(BleEvents.BluetoothDisabled)

                BluetoothAdapter.STATE_ON -> events.trySend(BleEvents.BluetoothEnabled)
            }
        }
    }

    override suspend fun connect(
        device: BluetoothDevice,
        reconnectEnabled: Boolean
    ) {
        connectionMutex.withLock {
            closeCurrentGattLocked()
            activeDevice = device
            this@SingleDeviceBleConnectionController.reconnectEnabled = reconnectEnabled
            registerBluetoothStateReceiverLocked()
        }
        connectToActiveDevice()
    }

    private suspend fun connectToActiveDevice() {
        val device: BluetoothDevice = connectionMutex.withLock {
            if (activeDevice == null) {
                _connectionState.value =
                    BleConnectionState.UndefinedBehavior("somehow active device is null")
            }
            activeDevice ?: return
        }

        if (!bluetoothAdapter.isEnabled) {
            _connectionState.value = BleConnectionState.WaitingForBluetooth
            return
        }

        _connectionState.value = BleConnectionState.Connecting

        val remoteDevice: android.bluetooth.BluetoothDevice = runCatching {
            bluetoothAdapter.getRemoteDevice(device.macAddress)
        }.getOrElse {
            Log.e(TAG, "Unable to resolve the Bluetooth device", it)
            _connectionState.value = BleConnectionState.CantResolveTheDevice
            return
        }

        val currentActiveDevice = connectionMutex.withLock { activeDevice }

        if (device.macAddress != currentActiveDevice?.macAddress) {
            val msg =
                "Connection attempt is no longer current, race condition $device != $currentActiveDevice"
            Log.e(TAG, msg)
            return
        }

        val gatt = runCatching {
            remoteDevice.utilConnectGatt(
                context = context,
                autoConnect = false,
                bluetoothGattCallback = bluetoothGattCallback
            )
        }.getOrElse {
            val msg = "Unable to start the GATT connection"
            Log.e(TAG, msg, it)
            _connectionState.value = BleConnectionState.UnableToStartGattConnection
            return
        }
// TODO: Handle the race where a GATT callback arrives before currentGatt is assigned.
        connectionMutex.withLock {
            if (activeDevice?.macAddress != remoteDevice.address) {
                gatt?.close()
                val msg =
                    "addresses of remote device and active device are not the same, race condition"
                Log.e(TAG, msg)
                return
            }
            currentGatt = gatt
        }
    }


    override suspend fun disconnect() {
        connectionMutex.withLock {
            activeDevice = null
            reconnectEnabled = false
            closeCurrentGattLocked()
            unregisterBluetoothStateReceiverLocked()
        }
        _connectionState.value = BleConnectionState.Disconnected

    }

    private fun closeCurrentGattLocked() { //make sure this func is called into locked blocks by mutex
        currentGatt?.run {
            disconnect()//todo move it out of mutex lock properly
            close()//todo move it out of mutex lock properly
        }
        currentGatt = null
    }

    private suspend fun onConnected(gatt: BluetoothGatt) {
        val isCurrent = connectionMutex.withLock {
            gatt === currentGatt
        }

        if (!isCurrent) {
            gatt.close()
            return
        }

        _connectionState.value = BleConnectionState.Connected
    }

    private suspend fun onDisconnected(gatt: BluetoothGatt, status: Int) {
        val wasCurrent = connectionMutex.withLock {
            if (gatt !== currentGatt) false
            else {
                currentGatt = null
                true
            }
        }

        gatt.close()
        if (wasCurrent)
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

    private suspend fun onBluetoothDisabled() {
        connectionMutex.withLock {
            if (activeDevice == null) return
            currentGatt?.close()
            currentGatt = null
            _connectionState.value = BleConnectionState.WaitingForBluetooth
        }
    }

    private suspend fun onBluetoothEnabled() {
        val shouldReconnect = connectionMutex.withLock {
            activeDevice != null && reconnectEnabled
        }
        if (shouldReconnect) {
            _connectionState.value = BleConnectionState.Reconnecting
            connectToActiveDevice()
        }
    }


    companion object {
        const val TAG = "MyBleRepoImpl"
    }
}