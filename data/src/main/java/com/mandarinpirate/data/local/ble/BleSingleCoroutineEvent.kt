package com.mandarinpirate.data.local.ble

import android.bluetooth.BluetoothGatt

sealed interface BleSingleCoroutineEvent {
    data class Connected(val gatt: BluetoothGatt) : BleSingleCoroutineEvent
    data class Disconnected(
        val gatt: BluetoothGatt,
        val status: Int
    ) : BleSingleCoroutineEvent

    data object BluetoothEnabled : BleSingleCoroutineEvent
    data object BluetoothDisabled : BleSingleCoroutineEvent
}