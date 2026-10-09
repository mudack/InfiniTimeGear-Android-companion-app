package com.mandarinpirate.domain.repos

import com.mandarinpirate.domain.models.BluetoothDevice

interface PreferencesStoreRepo {
    suspend fun getIsItFirstAppStart(): Boolean
    suspend fun setFirstAppStartFalse()

    suspend fun getSavedDevice(): BluetoothDevice?
    suspend fun clearSavedDevice()
    suspend fun saveDevice(device: BluetoothDevice)
}