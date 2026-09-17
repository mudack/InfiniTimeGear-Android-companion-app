package com.mandarinpirate.data.ble

import com.mandarinpirate.domain.models.BluetoothDevice
import com.mandarinpirate.domain.ScanStatus
import com.mandarinpirate.domain.BleScanner
import com.mandarinpirate.domain.models.BleScanFilter
import com.mandarinpirate.domain.repos.BleScannerRepo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class BleScannerRepoImpl(private val bleScanner: BleScanner) : BleScannerRepo {
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