package com.mandarinpirate.data.local.data_store.saved_device

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mandarinpirate.domain.models.BluetoothDevice
import com.mandarinpirate.domain.repos.SavedDeviceRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SavedDeviceRepoImpl(
    private val preferencesDataStore: DataStore<Preferences>
) : SavedDeviceRepo {

    override fun observeSavedDevice(): Flow<BluetoothDevice?> =
        preferencesDataStore.data.map { preferences ->
            val address = preferences[KEY_ADDRESS] ?: return@map null
            BluetoothDevice(
                name = preferences[KEY_NAME].orEmpty(),
                macAddress = address
            )
        }

    override suspend fun saveDevice(device: BluetoothDevice) {
        preferencesDataStore.edit { preferences ->
            preferences[KEY_NAME] = device.name
            preferences[KEY_ADDRESS] = device.macAddress
        }
    }

    override suspend fun clearSavedDevice() {
        preferencesDataStore.edit { preferences ->
            preferences.remove(KEY_NAME)
            preferences.remove(KEY_ADDRESS)
        }
    }

    private companion object {
        val KEY_NAME = stringPreferencesKey("saved_device_name")
        val KEY_ADDRESS = stringPreferencesKey("saved_device_address")
    }
}
