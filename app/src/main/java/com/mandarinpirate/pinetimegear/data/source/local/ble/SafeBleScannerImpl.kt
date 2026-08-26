package com.mandarinpirate.pinetimegear.data.source.local.ble

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
import androidx.annotation.IntDef
import androidx.annotation.RequiresPermission
import com.mandarinpirate.pinetimegear.domain.BleScanner
import com.mandarinpirate.pinetimegear.domain.models.BleScanFilter
import com.mandarinpirate.pinetimegear.domain.models.BluetoothDevice
import com.mandarinpirate.pinetimegear.ui.getPermissionRequiredForBleScan

class SafeBleScannerImpl(
    bluetoothManager: BluetoothManager,
    private val context: Context
) : BleScanner {
    override var isScanning = false
        private set
    private var scanCallback: ScanCallback? = null
    private val bluetoothAdapter = bluetoothManager.adapter
    private var scanner: BluetoothLeScanner? = bluetoothAdapter.bluetoothLeScanner

    private var scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
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
            bluetoothAdapter.bluetoothLeScanner //bluetoothAdapter can be null in init so scanner can be null as well, for this case i double check it here
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
                    address = result?.device?.address.toString()
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

    @IntDef(
        ScanSettings.SCAN_MODE_LOW_POWER,
        ScanSettings.SCAN_MODE_BALANCED,
        ScanSettings.SCAN_MODE_LOW_LATENCY
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class BleScanMode
}