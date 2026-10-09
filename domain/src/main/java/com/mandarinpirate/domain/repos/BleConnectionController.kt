package com.mandarinpirate.domain.repos

import com.mandarinpirate.domain.models.BleConnectionState
import com.mandarinpirate.domain.models.BluetoothDevice
import kotlinx.coroutines.flow.StateFlow

interface BleConnectionController {
    val connectionState: StateFlow<BleConnectionState>

    suspend fun connect(device: BluetoothDevice, reconnectEnabled: Boolean)

    suspend fun disconnect()
}
