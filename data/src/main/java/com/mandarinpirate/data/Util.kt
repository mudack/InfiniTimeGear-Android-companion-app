package com.mandarinpirate.data

import android.Manifest
import android.os.Build


fun getPermissionRequiredForBleScan() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    Manifest.permission.BLUETOOTH_SCAN
} else {
    Manifest.permission.ACCESS_FINE_LOCATION
}