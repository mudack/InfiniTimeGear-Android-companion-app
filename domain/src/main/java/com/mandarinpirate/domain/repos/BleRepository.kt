package com.mandarinpirate.domain.repos

import com.mandarinpirate.domain.models.BleConnectionState
import com.mandarinpirate.domain.models.BluetoothDevice
import kotlinx.coroutines.flow.StateFlow

interface BleRepository {
    val connectionState: StateFlow<BleConnectionState>

    fun connect(device: BluetoothDevice, reconnectEnabled: Boolean)

    fun disconnect()
}
