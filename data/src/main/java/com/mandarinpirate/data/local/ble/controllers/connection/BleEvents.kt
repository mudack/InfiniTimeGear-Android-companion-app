package com.mandarinpirate.data.local.ble.controllers.connection

import android.bluetooth.BluetoothGatt

sealed interface BleEvents {
    data class Connected(val gatt: BluetoothGatt) : BleEvents
    data class Disconnected(
        val gatt: BluetoothGatt,
        val status: Int
    ) : BleEvents

    data object BluetoothEnabled : BleEvents
    data object BluetoothDisabled : BleEvents
}