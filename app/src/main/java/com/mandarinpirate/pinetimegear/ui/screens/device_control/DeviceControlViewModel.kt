package com.mandarinpirate.pinetimegear.ui.screens.device_control

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mandarinpirate.domain.models.BluetoothDevice
import com.mandarinpirate.domain.repos.BleRepository
import com.mandarinpirate.domain.repos.SavedDeviceRepo
import com.mandarinpirate.pinetimegear.ui.entities.BluetoothDeviceUi
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DeviceControlViewModel @Inject constructor(
    private val savedDeviceRepo: SavedDeviceRepo,
    private val bleRepo: BleRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DeviceControlState())

    val uiState: StateFlow<DeviceControlState> = _uiState.stateIn(
        viewModelScope, WhileSubscribed(5_000),
        DeviceControlState()
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val device: BluetoothDevice = savedDeviceRepo.observeSavedDevice().first()
                ?: throw NullPointerException("The collecting device cannot be null ")
            _uiState.update { it.copy(device = BluetoothDeviceUi.fromDomain(device)) }
        }
    }

    fun sendEvent(event: DeviceControlEvent) {
        when (event) {
            DeviceControlEvent.TryToConnect -> {
                _uiState.update { it.copy(connection = ConnectionState.CONNECTING) }
                bleRepo.connectTo(
                    uiState.value.device.toDomainBluetoothDevice(),
                    connectionStateCallback = { status, newState ->
                        val connectionState = newState.toBleConnectionState()
                        val disconnectReason = status.parseDisconnectReason()
                        val newGattLog =
                            uiState.value.gattLog + "\nstate=$connectionState and newState=$disconnectReason"

                        _uiState.update {
                            it.copy(
                                connection = connectionState,
                                disconnectReason = disconnectReason,
                                gattLog = newGattLog
                            )
                        }
                    },
                    autoConnectEnabled = true
                )
            }

            DeviceControlEvent.TryToDisconnect -> {
                bleRepo.disconnect
            }
        }
    }

}