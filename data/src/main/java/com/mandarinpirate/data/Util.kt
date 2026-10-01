package com.mandarinpirate.data

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattConnectionSettings
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import java.util.concurrent.Executor


fun getPermissionRequiredForBleScan() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    Manifest.permission.BLUETOOTH_SCAN
} else {
    Manifest.permission.ACCESS_FINE_LOCATION
}

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun BluetoothDevice.utilConnectGatt(
    context: Context,
    autoConnect: Boolean,
    bluetoothGattCallback: BluetoothGattCallback,
): BluetoothGatt? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
        val connectionSettings = BluetoothGattConnectionSettings.Builder()
            .setAutoConnectEnabled(autoConnect)
            .build()
        this.connectGatt(
            connectionSettings,
            context.mainExecutor,
            bluetoothGattCallback
        )
    } else {
        @Suppress("DEPRECATION")
        this.connectGatt(context, autoConnect, bluetoothGattCallback)
    }
}