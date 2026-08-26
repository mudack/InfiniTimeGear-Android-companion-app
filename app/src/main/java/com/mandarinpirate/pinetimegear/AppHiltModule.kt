package com.mandarinpirate.pinetimegear

import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.mandarinpirate.pinetimegear.data.repo.ble.BleScannerRepoImpl
import com.mandarinpirate.pinetimegear.data.source.local.ble.SafeBleScannerImpl
import com.mandarinpirate.pinetimegear.data.source.local.data_store.preferences_store.PreferencesStoreRepoImpl
import com.mandarinpirate.pinetimegear.data.source.local.string_provider.StringProvider
import com.mandarinpirate.pinetimegear.data.source.local.string_provider.StringProviderImpl
import com.mandarinpirate.pinetimegear.domain.repos.BleScannerRepo
import com.mandarinpirate.pinetimegear.domain.repos.PreferencesStoreRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppHiltModule {

    @Provides
    @Singleton
    fun provideStringProvider(
        @ApplicationContext context: Context
    ): StringProvider = StringProviderImpl(context)

    @Provides
    @Singleton
    fun provideBluetoothManager(
        @ApplicationContext context: Context
    ): BluetoothManager = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)

    @Provides
    @Singleton
    fun providePreferencesStoreRepo(
        @ApplicationContext context: Context
    ): PreferencesStoreRepo {
        val dataStore = PreferenceDataStoreFactory.create(
            corruptionHandler = null,
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        ) { context.preferencesDataStoreFile(Const.PREFERENCES_DATA_STORE_FILE_NAME) }
        return PreferencesStoreRepoImpl(dataStore)
    }

    @Provides
    @Singleton
    fun provideBleScannerRepo(
        @ApplicationContext context: Context,
        bluetoothManager:BluetoothManager
    ): BleScannerRepo = BleScannerRepoImpl(SafeBleScannerImpl(
        bluetoothManager = bluetoothManager,
        context = context
    ))
}