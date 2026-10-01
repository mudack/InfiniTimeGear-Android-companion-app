package com.mandarinpirate.domain

import com.mandarinpirate.domain.models.BluetoothDevice

interface BleServiceManager {
    fun startService(device: BluetoothDevice)
    fun stopService()
}