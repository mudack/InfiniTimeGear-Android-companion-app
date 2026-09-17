package com.mandarinpirate.domain.repos

import com.mandarinpirate.domain.models.BluetoothDevice
import kotlinx.coroutines.flow.Flow

interface SavedDeviceRepo {
    fun observeSavedDevice(): Flow<BluetoothDevice?>

    suspend fun saveDevice(device: BluetoothDevice)

    suspend fun clearSavedDevice()
}
