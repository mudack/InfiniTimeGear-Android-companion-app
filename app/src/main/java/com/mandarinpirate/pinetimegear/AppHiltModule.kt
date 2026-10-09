package com.mandarinpirate.pinetimegear

import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.mandarinpirate.data.local.ble.controllers.connection.SingleDeviceBleConnectionController
import com.mandarinpirate.data.local.ble.controllers.scan.SafeBleScanControllerImpl
import com.mandarinpirate.data.local.data_store.BleScanDataSourceImpl
import com.mandarinpirate.data.local.data_store.PreferencesStoreRepoImpl
import com.mandarinpirate.data.local.string_provider.StringProvider
import com.mandarinpirate.data.local.string_provider.StringProviderImpl
import com.mandarinpirate.domain.BleServiceManager
import com.mandarinpirate.domain.repos.BleConnectionController
import com.mandarinpirate.domain.repos.PreferencesStoreRepo
import com.mandarinpirate.pinetimegear.service.BleServiceManagerImpl
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
    ): BleConnectionController = SingleDeviceBleConnectionController(bluetoothManager.adapter, context)

    @Provides
    @Singleton
    fun provideStringProvider(
        @ApplicationContext context: Context
    ): StringProvider = StringProviderImpl(context)

    @Provides
    @Singleton
    fun provideBleServiceManager(
        @ApplicationContext context: Context
    ): BleServiceManager = BleServiceManagerImpl(context)

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
    fun provideBleScannerRepo(
        @ApplicationContext context: Context,
        bluetoothManager: BluetoothManager
    ): com.mandarinpirate.domain.repos.BleScanDataSource = BleScanDataSourceImpl(
        SafeBleScanControllerImpl(
            bluetoothManager = bluetoothManager,
            context = context
        )
    )
}
