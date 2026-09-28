package com.mandarinpirate.pinetimegear.ui.screens.device_control

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

    PineTimeGearCompanionAppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        ) { innerPadding ->
            DeviceControlContent(
                Modifier.padding(innerPadding),
                uiState = uiState,
                sendEvent = viewModel::sendEvent
            )
        }
    }
}

@Composable
fun DeviceControlContent(
    modifier: Modifier,
    uiState: DeviceControlState,
    sendEvent: (DeviceControlEvent) -> Unit
) {
    Column(modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Device")
        Text("name: " + uiState.device.name)
        Text("mac addr: " + uiState.device.macAddress)
        Button(onClick = {
            if (uiState.connection == ConnectionState.DISCONNECTED) {
                sendEvent(DeviceControlEvent.TryToConnect)
            } else if (uiState.connection == ConnectionState.CONNECTED) {
                sendEvent(DeviceControlEvent.TryToDisconnect)
            }
        }) {
            when (uiState.connection) {
                ConnectionState.CONNECTED -> ConnectedContent()
                ConnectionState.DISCONNECTED -> DisconnectedContent()
                ConnectionState.CONNECTING -> ConnectingContent()
                ConnectionState.UNDEFINED -> UndefinedContent()
                ConnectionState.DISCONNECTING -> DisconnectingContent()
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
fun DisconnectingContent() {
    Text("Disconnecting...")
}

@Composable
fun UndefinedContent() {
    Text("Undefined")
}