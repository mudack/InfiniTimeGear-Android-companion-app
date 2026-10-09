package com.mandarinpirate.data.local.ble.controllers.scan

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import com.mandarinpirate.data.getPermissionRequiredForBleScan
import com.mandarinpirate.domain.BleScanController
import com.mandarinpirate.domain.models.BleScanFilter
import com.mandarinpirate.domain.models.BluetoothDevice

class SafeBleScanControllerImpl(
    bluetoothManager: BluetoothManager,
    private val context: Context
) : BleScanController {
    override var isScanning = false
        private set
    private var scanCallback: ScanCallback? = null
    private val bluetoothAdapter = bluetoothManager.adapter
    private var scanner: BluetoothLeScanner? = bluetoothAdapter.bluetoothLeScanner

    private var scanSettings = ScanSettings.Builder()
        .setScanMode(BleScanMode.LOW_LATENCY.value)
        .build()

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun performStartScan(
        onScanResult: (callbackType: Int, result: ScanResult?) -> Unit,
        onScanFailed: (errorCode: Int) -> Unit,
        filters: List<ScanFilter> = listOf()
    ) {
        if (bluetoothAdapter == null) throw BleScannerException.BluetoothAdapterIsNotInitializedException()
        if (!bluetoothAdapter.isEnabled) throw BleScannerException.BluetoothIsNotEnabled()
        if (scanner == null) scanner =
            bluetoothAdapter.bluetoothLeScanner //bluetoothAdapter can be null in init so scanner can be null as well, for this case I double-check it here
        this.scanCallback = object : ScanCallback() {
            override fun onScanResult(
                callbackType: Int,
                result: ScanResult?
            ) {
                super.onScanResult(callbackType, result)
                onScanResult(callbackType, result)
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                onScanFailed(errorCode)
            }
        }
        isScanning = true
        scanner?.startScan(filters, scanSettings, this.scanCallback)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun performStopScan() {
        if (isScanning) {
            val callback = checkNotNull(scanCallback) {
                "Inconsistent State: isScanning is true, but scanCallback is null!"
            }
            scanner?.stopScan(callback)
            isScanning = false
            scanCallback = null
        }
    }

    /**
     * its implementation must guarantee that permission are provided or call onPermissionNotProvided
     * */
    @SuppressLint("MissingPermission")
    override fun startScan(
        onScanResult: (callbackType: Int, device: BluetoothDevice) -> Unit,
        onScanFailed: (errorCode: Int) -> Unit,
        filters: List<BleScanFilter>,
        onPermissionNotProvided: (notProvidedPermission: String) -> Unit,
    ) {
        val permRequiredForScan = getPermissionRequiredForBleScan()
        val isBluetoothScanPermissionGranted =
            context.checkSelfPermission(permRequiredForScan) == PackageManager.PERMISSION_GRANTED
        if (isBluetoothScanPermissionGranted) {
            performStartScan(onScanResult = { callbackType, result ->
                val device = BluetoothDevice(
                    name = result?.device?.name.toString(),
                    macAddress = result?.device?.address.toString()
                )
                onScanResult.invoke(callbackType, device)
            }, onScanFailed = onScanFailed, filters.toAndroidScanFilterList())
        } else {
            onPermissionNotProvided(permRequiredForScan)
        }
    }

    /**
     * its implementation must guarantee that permission are provided or call onPermissionNotProvided
     * */
    @SuppressLint("MissingPermission")
    override fun stopScan(
        onPermissionNotProvided: (notProvidedPermission: String) -> Unit
    ) {
        val permRequiredForScan = getPermissionRequiredForBleScan()
        val isBluetoothScanPermissionGranted =
            context.checkSelfPermission(permRequiredForScan) == PackageManager.PERMISSION_GRANTED
        if (isBluetoothScanPermissionGranted) {
            performStopScan()
        } else {
            onPermissionNotProvided(permRequiredForScan)
        }
    }

    sealed class BleScannerException : RuntimeException() {
        class BluetoothAdapterIsNotInitializedException : BleScannerException()
        class BluetoothIsNotEnabled : BleScannerException()
    }

    enum class BleScanMode(val value: Int) {
        LOW_POWER(ScanSettings.SCAN_MODE_LOW_POWER),
        BALANCED(ScanSettings.SCAN_MODE_BALANCED),
        LOW_LATENCY(ScanSettings.SCAN_MODE_LOW_LATENCY);

        companion object {
            fun fromValue(value: Int): BleScanMode? =
                entries.find { it.value == value }
        }
    }
}