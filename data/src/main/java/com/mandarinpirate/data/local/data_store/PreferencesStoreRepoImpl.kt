package com.mandarinpirate.data.local.data_store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mandarinpirate.domain.models.BluetoothDevice
import com.mandarinpirate.domain.repos.PreferencesStoreRepo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PreferencesStoreRepoImpl(
    private val prefDataStore: DataStore<Preferences>
) : PreferencesStoreRepo {

    //  IsItFirstStart
    private val keyIsItFirstAppStart: Preferences.Key<Boolean> =
        booleanPreferencesKey(IS_FIRST_APP_START)

    override suspend fun getIsItFirstAppStart(): Boolean =
        prefDataStore.data.map { it[keyIsItFirstAppStart] ?: true }.first()

    override suspend fun setFirstAppStartFalse() {
        prefDataStore.edit {
            it[keyIsItFirstAppStart] = false
        }
    }

    //  SavedDevices
    override suspend fun getSavedDevice(): BluetoothDevice? =
        prefDataStore.data.map { preferences ->
            val address = preferences[KEY_ADDRESS] ?: return@map null
            BluetoothDevice(
                name = preferences[KEY_NAME].orEmpty(),
                macAddress = address
            )
        }.first()

    override suspend fun clearSavedDevice() {
        prefDataStore.edit { preferences ->
            preferences.remove(KEY_NAME)
            preferences.remove(KEY_ADDRESS)
        }
    }

    override suspend fun saveDevice(device: BluetoothDevice) {
        prefDataStore.edit { preferences ->
            preferences[KEY_NAME] = device.name
            preferences[KEY_ADDRESS] = device.macAddress
        }
    }


    companion object {

        val KEY_NAME = stringPreferencesKey("saved_device_name")
        val KEY_ADDRESS = stringPreferencesKey("saved_device_address")

        const val IS_FIRST_APP_START =
            "is_first_app_start"
    }
}