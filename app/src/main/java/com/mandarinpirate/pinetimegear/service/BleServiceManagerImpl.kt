package com.mandarinpirate.pinetimegear.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.mandarinpirate.domain.BleServiceManager
import com.mandarinpirate.domain.models.BluetoothDevice
import javax.inject.Inject

class BleServiceManagerImpl @Inject constructor(
    private val context: Context
): BleServiceManager {
    override fun startService(device: BluetoothDevice) {
        ContextCompat.startForegroundService(
            context,
            BleForegroundService.createStartIntent(context, device)
        )
    }

    override fun stopService() {
        context.stopService(Intent(context, BleForegroundService::class.java))
    }
}
