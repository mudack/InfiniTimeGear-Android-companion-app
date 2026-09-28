package com.mandarinpirate.pinetimegear.ui.screens.device_control

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothProfile
import com.mandarinpirate.pinetimegear.ui.entities.BluetoothDeviceUi

data class DeviceControlState(
    val batteryPercent: Int = 0,
    val connection: ConnectionState = ConnectionState.DISCONNECTED,
    val disconnectReason: BleDisconnectReason = BleDisconnectReason.Normal,
    val device: BluetoothDeviceUi = BluetoothDeviceUi.getNullObject(),
    val gattLog: String = ""
)

enum class ConnectionState {
    CONNECTED, DISCONNECTED, CONNECTING, UNDEFINED, DISCONNECTING
}

fun Int.toBleConnectionState(): ConnectionState =
    when (this) {
        BluetoothProfile.STATE_DISCONNECTED -> ConnectionState.DISCONNECTED
        BluetoothProfile.STATE_CONNECTING -> ConnectionState.CONNECTING
        BluetoothProfile.STATE_CONNECTED -> ConnectionState.CONNECTED
        BluetoothProfile.STATE_DISCONNECTING -> ConnectionState.DISCONNECTING
        else -> ConnectionState.UNDEFINED
    }

sealed interface BleDisconnectReason {
    data object Timeout : BleDisconnectReason
    data object RemoteDevice : BleDisconnectReason
    data object LocalHost : BleDisconnectReason
    data object Normal : BleDisconnectReason
    data class Unknown(val status: Int) : BleDisconnectReason
}

fun Int.parseDisconnectReason(): BleDisconnectReason =
    when (this) {
        BluetoothGatt.GATT_SUCCESS -> BleDisconnectReason.Normal
        8 -> BleDisconnectReason.Timeout
        19 -> BleDisconnectReason.RemoteDevice
        22 -> BleDisconnectReason.LocalHost
        else -> BleDisconnectReason.Unknown(this)
    }