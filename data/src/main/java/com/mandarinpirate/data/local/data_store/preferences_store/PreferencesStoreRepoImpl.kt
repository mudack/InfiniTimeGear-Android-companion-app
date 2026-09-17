package com.mandarinpirate.data.local.data_store.preferences_store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.mandarinpirate.domain.repos.PreferencesStoreRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesStoreRepoImpl(
    private val prefDataStore: DataStore<Preferences>
) : PreferencesStoreRepo {

    private val keyIsItFirstAppStart: Preferences.Key<Boolean> =
        booleanPreferencesKey(IS_FIRST_APP_START)

    override fun getIsItFirstAppStart(): Flow<Boolean> =
        prefDataStore.data.map { it[keyIsItFirstAppStart] ?: true }


    override suspend fun setIsItFirstAppStartFalse() {
        prefDataStore.edit {
            it[keyIsItFirstAppStart] = false
        }
    }

    companion object {
        const val IS_FIRST_APP_START =
            "is_first_app_start"
    }
}