package com.mandarinpirate.data.local.data_store

import com.mandarinpirate.domain.BleScanController
import com.mandarinpirate.domain.ScanStatus
import com.mandarinpirate.domain.models.BleScanFilter
import com.mandarinpirate.domain.models.BluetoothDevice
import com.mandarinpirate.domain.repos.BleScanDataSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class BleScanDataSourceImpl(private val bleScanner: BleScanController) : BleScanDataSource {
    override fun observeBleScan(filters: List<BleScanFilter>): Flow<ScanStatus> = callbackFlow {
        bleScanner.startScan(
            onScanResult = { _: Int, device: BluetoothDevice ->
                trySend(ScanStatus.DeviceFound(device))
            },
            onScanFailed = { errorCode ->
                val errorState = ScanStatus.ScanFailed.parseByCode(errorCode)
                trySend(errorState)
            },
            filters = filters,
            onPermissionNotProvided = { notProvidedPermission: String ->
                trySend(ScanStatus.PermissionNotProvided(notProvidedPermission))
            }
        )
        awaitClose {
            bleScanner.stopScan { notProvidedPermission: String ->
                trySend(ScanStatus.PermissionNotProvided(notProvidedPermission))
            }
        }
    }
}