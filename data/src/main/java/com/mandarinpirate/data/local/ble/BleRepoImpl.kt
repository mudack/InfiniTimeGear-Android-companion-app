package com.mandarinpirate.data.local.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattConnectionSettings
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import com.mandarinpirate.data.utilConnectGatt
import com.mandarinpirate.domain.models.BluetoothDevice
import com.mandarinpirate.domain.repos.BleRepository
import java.util.concurrent.Executor

class BleRepoImpl(private val bluetoothAdapter: BluetoothAdapter, private val context: Context) : BleRepository {
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun connectTo(
        device: BluetoothDevice,
        autoConnectEnabled: Boolean,
        connectionStateCallback: (status: Int, newState: Int) -> Unit
    ) {
        val device = bluetoothAdapter.getRemoteDevice(device.macAddress)
        device.utilConnectGatt(
            context,
            autoConnectEnabled,
            object : BluetoothGattCallback(){
                override fun onConnectionStateChange(
                    gatt: BluetoothGatt?,
                    status: Int,
                    newState: Int
                ) {
                    super.onConnectionStateChange(gatt, status, newState)
                    connectionStateCallback(status, newState)
                }
            }
        )
    }

    companion object{
        const val TAG = "BleRepoImpl onConnectionStateChange"
    }
}