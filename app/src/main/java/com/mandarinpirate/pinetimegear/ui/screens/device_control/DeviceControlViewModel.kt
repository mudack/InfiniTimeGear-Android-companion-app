package com.mandarinpirate.pinetimegear.ui.screens.device_control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mandarinpirate.domain.BleServiceManager
import com.mandarinpirate.domain.models.BluetoothDevice
import com.mandarinpirate.domain.repos.BleConnectionController
import com.mandarinpirate.domain.repos.PreferencesStoreRepo
import com.mandarinpirate.pinetimegear.ui.entities.BluetoothDeviceUi
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DeviceControlViewModel @Inject constructor(
    private val preferencesStoreRepo: PreferencesStoreRepo,
    private val bleRepo: BleConnectionController,
    private val bleServiceManager: BleServiceManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(DeviceControlState())

    val uiState: StateFlow<DeviceControlState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val device: BluetoothDevice = preferencesStoreRepo.getSavedDevice()
                ?: throw NullPointerException("The collecting device cannot be null ")
            _uiState.update { it.copy(device = BluetoothDeviceUi.fromDomain(device)) }
        }
        viewModelScope.launch {
            bleRepo.connectionState.collectLatest { connectionState ->
                _uiState.update {
                    it.copy(
                        connection = connectionState.toUiConnectionState(),
                        gattLog = "${it.gattLog}\nstate=$connectionState"
                    )
                }
            }
        }
    }

    fun sendEvent(event: DeviceControlEvent) {
        when (event) {
            DeviceControlEvent.TryToConnect -> {
                bleServiceManager.startService(uiState.value.device.toDomainBluetoothDevice())
            }

            DeviceControlEvent.TryToDisconnect -> {
                bleServiceManager.stopService()
            }
        }
    }

}
