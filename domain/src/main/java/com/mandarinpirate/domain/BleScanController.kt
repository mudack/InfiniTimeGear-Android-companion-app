package com.mandarinpirate.domain

import com.mandarinpirate.domain.models.BleScanFilter
import com.mandarinpirate.domain.models.BluetoothDevice

interface BleScanController {
    val isScanning: Boolean
    fun startScan(
        onScanResult: (callbackType: Int, device: BluetoothDevice) -> Unit,
        onScanFailed: (errorCode: Int) -> Unit,
        filters: List<BleScanFilter> = listOf(),
        onPermissionNotProvided: (notProvidedPermission: String)->Unit,
    )

    fun stopScan(
        onPermissionNotProvided: (notProvidedPermission: String) -> Unit
    )
}