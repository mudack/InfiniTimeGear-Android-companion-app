package com.mandarinpirate.pinetimegear

import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.mandarinpirate.data.local.ble.BleRepoImpl
import com.mandarinpirate.data.local.ble.BleScannerRepoImpl
import com.mandarinpirate.data.local.ble.SafeBleScannerImpl
import com.mandarinpirate.data.local.data_store.preferences_store.PreferencesStoreRepoImpl
import com.mandarinpirate.data.local.data_store.saved_device.SavedDeviceRepoImpl
import com.mandarinpirate.data.local.string_provider.StringProvider
import com.mandarinpirate.data.local.string_provider.StringProviderImpl
import com.mandarinpirate.domain.repos.BleRepository
import com.mandarinpirate.domain.repos.BleScannerRepo
import com.mandarinpirate.domain.repos.PreferencesStoreRepo
import com.mandarinpirate.domain.repos.SavedDeviceRepo
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
    fun provideBleRepo(
        @ApplicationContext context: Context,
        bluetoothManager: BluetoothManager
    ): BleRepository = BleRepoImpl(bluetoothManager.adapter, context)

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
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        corruptionHandler = null,
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    ) { context.preferencesDataStoreFile(Const.PREFERENCES_DATA_STORE_FILE_NAME) }

    @Provides
    @Singleton
    fun providePreferencesStoreRepo(
        dataStore: DataStore<Preferences>
    ): PreferencesStoreRepo = PreferencesStoreRepoImpl(dataStore)

    @Provides
    @Singleton
    fun provideSavedDeviceRepo(
        dataStore: DataStore<Preferences>
    ): SavedDeviceRepo = SavedDeviceRepoImpl(dataStore)

    @Provides
    @Singleton
    fun provideBleScannerRepo(
        @ApplicationContext context: Context,
        bluetoothManager: BluetoothManager
    ): BleScannerRepo = BleScannerRepoImpl(
        SafeBleScannerImpl(
            bluetoothManager = bluetoothManager,
            context = context
        )
    )
}
