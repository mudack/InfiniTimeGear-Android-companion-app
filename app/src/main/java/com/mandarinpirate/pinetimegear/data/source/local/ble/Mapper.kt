package com.mandarinpirate.pinetimegear.data.source.local.ble

import android.bluetooth.le.ScanFilter
import android.os.ParcelUuid
import com.mandarinpirate.pinetimegear.domain.models.BleScanFilter

internal fun BleScanFilter.toAndroidScanFilter(): ScanFilter {
    return ScanFilter.Builder().apply {
        deviceName?.let { setDeviceName(it) }
        deviceAddress?.let { setDeviceAddress(it) }
        serviceUuid?.let {
            setServiceUuid(ParcelUuid.fromString(it))
        }
    }.build()
}

internal fun List<BleScanFilter>.toAndroidScanFilterList(): List<ScanFilter> {
    return this.map { it.toAndroidScanFilter() }
}