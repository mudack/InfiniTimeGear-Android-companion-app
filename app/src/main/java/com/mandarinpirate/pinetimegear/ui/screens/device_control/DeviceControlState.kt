package com.mandarinpirate.pinetimegear.ui.screens.device_control

import com.mandarinpirate.domain.models.BleConnectionState
import com.mandarinpirate.pinetimegear.ui.entities.BluetoothDeviceUi

data class DeviceControlState(
    val batteryPercent: Int = 0,
    val connection: ConnectionState = ConnectionState.DISCONNECTED,
    val device: BluetoothDeviceUi = BluetoothDeviceUi.getNullObject(),
    val gattLog: String = ""
)

enum class ConnectionState {
    CONNECTED, DISCONNECTED, CONNECTING, WAITING_FOR_BLUETOOTH, RECONNECTING, ERROR
}

fun BleConnectionState.toUiConnectionState(): ConnectionState =
    when (this) {
        BleConnectionState.Connected -> ConnectionState.CONNECTED
        BleConnectionState.Connecting -> ConnectionState.CONNECTING
        BleConnectionState.Disconnected -> ConnectionState.DISCONNECTED
        BleConnectionState.WaitingForBluetooth -> ConnectionState.WAITING_FOR_BLUETOOTH
        is BleConnectionState.Reconnecting -> ConnectionState.RECONNECTING
        BleConnectionState.CantResolveTheDevice -> ConnectionState.ERROR
        BleConnectionState.UnableToStartGattConnection -> ConnectionState.ERROR
        is BleConnectionState.UndefinedBehavior -> ConnectionState.ERROR
    }
