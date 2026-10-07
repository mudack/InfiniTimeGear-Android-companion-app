package com.mandarinpirate.pinetimegear.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.mandarinpirate.domain.models.BleConnectionState
import com.mandarinpirate.domain.models.BluetoothDevice
import com.mandarinpirate.domain.repos.BleRepository
import com.mandarinpirate.pinetimegear.R
import com.mandarinpirate.pinetimegear.ui.screens._main_activity.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BleForegroundService : Service() {

    @Inject
    lateinit var bleRepository: BleRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val bleScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isForegroundStarted = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        serviceScope.launch {
            bleRepository.connectionState.collectLatest { connectionState ->
                if (isForegroundStarted) {
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        createNotification(connectionState)
                    )
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val device = intent?.toBluetoothDevice()
        if (intent?.action != ACTION_CONNECT || device == null) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        val notification = createNotification(BleConnectionState.Connecting)
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            foregroundServiceTypeFlags
        )
        isForegroundStarted = true
        bleScope.launch { bleRepository.connect(device, reconnectEnabled = true) }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        bleScope.launch {
            bleRepository.disconnect()
            bleScope.cancel()
        }
        isForegroundStarted = false
        serviceScope.cancel()
        super.onDestroy()
    }

    private val notificationManager: NotificationManager
        get() = getSystemService(NotificationManager::class.java)

    private val foregroundServiceTypeFlags: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
        } else {
            0
        }

    private fun createNotification(connectionState: BleConnectionState): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(Icon.createWithResource(this, R.mipmap.ic_launcher))
            .setContentTitle(getString(R.string.app_name))
            .setContentText(connectionState.notificationMessage())
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun BleConnectionState.notificationMessage(): String = when (this) {
        BleConnectionState.Connected -> getString(R.string.ble_notification_connected)
        BleConnectionState.Connecting -> getString(R.string.ble_notification_connecting)
        BleConnectionState.Disconnected -> getString(R.string.ble_notification_disconnected)
        BleConnectionState.WaitingForBluetooth -> {
            getString(R.string.ble_notification_waiting_for_bluetooth)
        }
        is BleConnectionState.Reconnecting -> {
            getString(R.string.ble_notification_reconnecting)
        }

        BleConnectionState.CantResolveTheDevice ->
            getString(R.string.ble_notification_cant_resolve_the_device)
        BleConnectionState.UnableToStartGattConnection ->
            getString(R.string.ble_notification_unable_to_start_gatt_connection)
        is BleConnectionState.UndefinedBehavior -> this.message //todo <release> replace with abstract error or consult with designer how to do it better
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.ble_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.ble_notification_channel_description)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun Intent.toBluetoothDevice(): BluetoothDevice? {
        val name = getStringExtra(EXTRA_DEVICE_NAME) ?: return null
        val macAddress = getStringExtra(EXTRA_DEVICE_MAC_ADDRESS) ?: return null
        return BluetoothDevice(name = name, macAddress = macAddress)
    }

    companion object {
        private const val ACTION_CONNECT = "com.mandarinpirate.pinetimegear.action.CONNECT"
        private const val EXTRA_DEVICE_NAME = "extra_device_name"
        private const val EXTRA_DEVICE_MAC_ADDRESS = "extra_device_mac_address"
        private const val NOTIFICATION_CHANNEL_ID = "ble_connection"
        private const val NOTIFICATION_ID = 5910

        fun createStartIntent(context: Context, device: BluetoothDevice): Intent =
            Intent(context, BleForegroundService::class.java).apply {
                action = ACTION_CONNECT
                putExtra(EXTRA_DEVICE_NAME, device.name)
                putExtra(EXTRA_DEVICE_MAC_ADDRESS, device.macAddress)
            }
    }
}
