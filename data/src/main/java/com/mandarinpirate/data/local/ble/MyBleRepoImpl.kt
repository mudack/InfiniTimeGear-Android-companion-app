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

class MyBleRepoImpl(
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
    private var activeDevice: DBluetoothDevice? = null
    private var currentGatt: BluetoothGatt? = null

    private val connectionMutex = Mutex()

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> onConnected(gatt)
                BluetoothProfile.STATE_DISCONNECTED -> onDisconnected(gatt, status)
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun connect(
        device: DBluetoothDevice,
        reconnectEnabled: Boolean
    ) {
        connectionScope.launch {
            activeDevice = device
            val remoteDevice = getRemoteDevice(BleConnectionState.Connecting)
                ?: throw IllegalStateException("remoteDevice cannot be null")

            val gatt = runCatching {
                remoteDevice.utilConnectGatt(
                    context = context,
                    autoConnect = false,
                    bluetoothGattCallback = bluetoothGattCallback
                )
            }.getOrElse {
                Log.e(TAG, "Unable to start the GATT connection", it)
//            scheduleReconnect()
                return@launch
            }

            connectionMutex.withLock {
                if (activeDevice?.macAddress != remoteDevice.address) {
                    gatt?.close()
                    return@launch
                }
                currentGatt = gatt
            }
        }
    }

    private suspend fun getRemoteDevice(processingState: BleConnectionState): BluetoothDevice? {
        val device = connectionMutex.withLock {
            activeDevice
                ?: return null
        }

        if (!bluetoothAdapter.isEnabled) {
            _connectionState.value = BleConnectionState.WaitingForBluetooth
            throw IllegalStateException("In this app version bluetooth always should be turned on")//TODO impl bluetooth state checking
        }

        _connectionState.value = processingState

        val remoteDevice: BluetoothDevice = runCatching {
            bluetoothAdapter.getRemoteDevice(device.macAddress)
        }.getOrElse {
            Log.e(TAG, "Unable to resolve the Bluetooth device", it)
//            scheduleReconnect()
            return null
        }

        val currentActiveDevice = connectionMutex.withLock { activeDevice }

        if (device == currentActiveDevice) return remoteDevice
        else throw IllegalStateException("race condition $device != $currentActiveDevice ")
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun disconnect() {
        connectionScope.launch {
            connectionMutex.withLock {
                closeCurrentGattLocked()
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
    private fun onConnected(gatt: BluetoothGatt) {
        connectionScope.launch {
            connectionMutex.withLock {
                if (gatt != currentGatt) {
                    closeCurrentGattLocked()
                }
                currentGatt = gatt
            }
            _connectionState.value = BleConnectionState.Connected
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun onDisconnected(gatt: BluetoothGatt, status: Int) {
        connectionScope.launch {
            connectionMutex.withLock {
                if (gatt === currentGatt) {
                    currentGatt = null
                }
            }
            gatt.close()

            _connectionState.value = BleConnectionState.Disconnected
            //in the future i could parse status and show it in state
        }
    }

    companion object {
        const val TAG = "MyBleRepoImpl"
    }
}