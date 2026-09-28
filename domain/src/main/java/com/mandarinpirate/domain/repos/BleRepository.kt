package com.mandarinpirate.domain.repos

import com.mandarinpirate.domain.models.BluetoothDevice

interface BleRepository {
    fun connectTo(device: BluetoothDevice, autoConnectEnabled: Boolean, connectionStateCallback: (status: Int, newState: Int) -> Unit)
}