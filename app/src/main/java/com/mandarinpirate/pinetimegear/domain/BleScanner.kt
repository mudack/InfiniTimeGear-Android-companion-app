package com.mandarinpirate.pinetimegear.domain

import com.mandarinpirate.pinetimegear.domain.models.BleScanFilter
import com.mandarinpirate.pinetimegear.domain.models.BluetoothDevice

interface BleScanner {
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