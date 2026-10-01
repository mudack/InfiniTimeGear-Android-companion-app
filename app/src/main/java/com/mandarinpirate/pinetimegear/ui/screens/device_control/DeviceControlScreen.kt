package com.mandarinpirate.pinetimegear.ui.screens.device_control

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mandarinpirate.pinetimegear.R
import com.mandarinpirate.pinetimegear.ui.theme.PineTimeGearCompanionAppTheme

@Composable
fun DeviceControlScreen(onWorkingHours: () -> Unit, onChooseAnotherDevice: () -> Unit) {
    val viewModel: DeviceControlViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {}

    PineTimeGearCompanionAppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        ) { innerPadding ->
            DeviceControlContent(
                Modifier.padding(innerPadding),
                uiState = uiState,
                sendEvent = viewModel::sendEvent,
                enableBluetooth = {
                    enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                }
            )
        }
    }
}

@Composable
fun DeviceControlContent(
    modifier: Modifier,
    uiState: DeviceControlState,
    sendEvent: (DeviceControlEvent) -> Unit,
    enableBluetooth: () -> Unit
) {
    // TODO: Move temporary UI strings to string resources.
    Column(modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Device")
        Text("name: " + uiState.device.name)
        Text("mac addr: " + uiState.device.macAddress)
        Button(onClick = {
            when (uiState.connection) {
                ConnectionState.DISCONNECTED -> sendEvent(DeviceControlEvent.TryToConnect)
                ConnectionState.WAITING_FOR_BLUETOOTH -> enableBluetooth()
                else -> sendEvent(DeviceControlEvent.TryToDisconnect)
            }
        }) {
            when (uiState.connection) {
                ConnectionState.CONNECTED -> ConnectedContent()
                ConnectionState.DISCONNECTED -> DisconnectedContent()
                ConnectionState.CONNECTING -> ConnectingContent()
                ConnectionState.WAITING_FOR_BLUETOOTH -> WaitingForBluetoothContent()
                ConnectionState.RECONNECTING -> ReconnectingContent()
            }
        }
        Text(uiState.gattLog)
    }
}

@Composable
fun ConnectedContent() {
    Text("Connected")
}

@Composable
fun DisconnectedContent() {
    Text("Disconnected")
}

@Composable
fun ConnectingContent() {
    val fabIconDimension = dimensionResource(R.dimen.fab_icon_size)
    Box(
        Modifier.size(fabIconDimension),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            Modifier.size(fabIconDimension),
            color = Color.Blue
        )
        Icon(
            painterResource(android.R.drawable.ic_media_pause),
            contentDescription = stringResource(R.string.fab_icon_pause_description)
        )
    }
}

@Composable
fun ReconnectingContent() {
    Text("Reconnecting...")
}

@Composable
fun WaitingForBluetoothContent() {
    Text(stringResource(R.string.device_control_enable_bluetooth))
}
