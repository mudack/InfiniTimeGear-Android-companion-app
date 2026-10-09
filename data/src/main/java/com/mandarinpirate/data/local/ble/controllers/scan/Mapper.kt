package com.mandarinpirate.data.local.ble.controllers.scan

import android.bluetooth.le.ScanFilter
import android.os.ParcelUuid
import com.mandarinpirate.domain.models.BleScanFilter

internal fun BleScanFilter.toAndroidScanFilter(): ScanFilter {
    return ScanFilter.Builder().apply {
        deviceName?.let { setDeviceName(it) }
        deviceAddress?.let { setDeviceAddress(it) }
        stringOfServiceUuid?.let {
            setServiceUuid(ParcelUuid.fromString(it))
        }
    }.build()
}

internal fun List<BleScanFilter>.toAndroidScanFilterList(): List<ScanFilter> {
    return this.map { it.toAndroidScanFilter() }
}