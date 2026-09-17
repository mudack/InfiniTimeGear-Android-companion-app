package com.mandarinpirate.pinetimegear.ui.screens.scan

import android.bluetooth.BluetoothManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mandarinpirate.domain.ScanStatus
import com.mandarinpirate.domain.models.BleScanFilter
import com.mandarinpirate.domain.repos.BleScannerRepo
import com.mandarinpirate.pinetimegear.Const.PINETIME_UUID_SERVICE
import com.mandarinpirate.pinetimegear.ui.entities.BluetoothDeviceUi
import com.mandarinpirate.pinetimegear.ui.entities.IssuePermissionStatus
import com.mandarinpirate.pinetimegear.ui.entities.BluetoothIssueType
import com.mandarinpirate.pinetimegear.ui.getIssueTypeByPermission
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val bluetoothManager: BluetoothManager,
    private val bleScannerRepo: BleScannerRepo
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScanState())

    val uiState: StateFlow<ScanState> = _uiState.stateIn(
        viewModelScope, WhileSubscribed(5_000),
        ScanState()
    )

    init {
        viewModelScope.launch {
            uiState
                .map { state -> state.issues.isNotEmpty() }
                .distinctUntilChanged()
                .collect { isIssueListNotEmpty -> if (isIssueListNotEmpty) stopBleScanning() }
        }
    }

    private var bleScannerJob: Job? = null

    fun sendEvent(event: ScanEvent) {
        when (event) {
            is ScanEvent.FirstOnResume -> _uiState.update { it.copy(isItFirstOnResume = false) }
            is ScanEvent.ScrollDownChipClicked -> _uiState.update {
                it.copy(
                    scrollToTheEnd = !it.scrollToTheEnd
                )
            }

            is ScanEvent.OnFabPressed -> {
                when (uiState.value.fabState) {
                    ScanScreenFabState.ISSUE -> _uiState.update {
                        it.copy(
                            alertDialogData = event.issueExistsDialogData
                        )
                    }

                    ScanScreenFabState.READY_FOR_SCANNING -> {
                        startBleScanCollect()
                        _uiState.update {
                            it.copy(
                                fabState = ScanScreenFabState.IS_SCANNING,
                                scannedDevices = emptyList()
                            )
                        }
                    }

                    ScanScreenFabState.IS_SCANNING -> {
                        stopBleScanCollect()
                        _uiState.update { it.copy(fabState = ScanScreenFabState.READY_FOR_SCANNING) }
                    }
                }
            }

            is ScanEvent.InitBluetoothAvailability -> {
                val newIssues = uiState.value.issues.toMutableSet()
                if (!event.isBluetoothAvailable) newIssues.add(BluetoothIssueType.HardwareIssue.BluetoothIsNotAvailable)
                if (!event.isBluetoothLEAvailable) newIssues.add(BluetoothIssueType.HardwareIssue.BLEIsNotAvailable)

                _uiState.update {
                    it.copy(
                        issues = newIssues,
                        fabState = getFabState(
                            isIssueListEmpty = newIssues.isEmpty()
                        )
                    )
                }
            }

            ScanEvent.RequireToCheckIsBluetoothEnabled -> {
                val newIssues = _uiState.value.issues.toMutableSet()
                if (!bluetoothManager.adapter.isEnabled)
                    newIssues.add(BluetoothIssueType.BluetoothIsNotEnabled)
                else newIssues.add(BluetoothIssueType.BluetoothIsNotEnabled)
                _uiState.update {
                    it.copy(
                        issues = newIssues,
                        fabState = getFabState(newIssues.isEmpty())
                    )
                }
            }

            is ScanEvent.TryToEnableBluetooth -> {
                val newIssues = _uiState.value.issues.toMutableSet()
                if (event.isSuccessful)
                    newIssues.add(BluetoothIssueType.BluetoothIsNotEnabled)
                else newIssues.add(BluetoothIssueType.BluetoothIsNotEnabled)

                _uiState.update {
                    it.copy(
                        isBluetoothEnabled = event.isSuccessful,
                        issues = newIssues,
                        fabState = getFabState(newIssues.isEmpty())
                    )
                }
            }

            is ScanEvent.DismissAlertDialog -> _uiState.update { it.copy(alertDialogData = null) }
            is ScanEvent.ShowUpAlertDialog -> _uiState.update { it.copy(alertDialogData = event.alertDialogData) }

            ScanEvent.OnAllPermissionAreGranted -> {
                val updatedIssues = _uiState.value.issues
                    .filter { it !is BluetoothIssueType.Permissions }
                    .toMutableSet()
                _uiState.update {
                    it.copy(
                        alertDialogData = null,
                        issues = updatedIssues,
                        fabState = getFabState(updatedIssues.isEmpty())
                    )
                }
            }

            is ScanEvent.OnNotGrantedPermissions -> addIssuesByPermissionsAndStatus(
                event.notGrantedPermission,
                permissionStatus = IssuePermissionStatus.NOT_GRANTED
            )

            is ScanEvent.OnDeniedPermissions -> addIssuesByPermissionsAndStatus(
                event.deniedPermission,
                permissionStatus = IssuePermissionStatus.DENIED,
            )

            ScanEvent.EnablingBluetoothNoPermissionException -> _uiState.update { state ->
                state.copy(
                    fabState = getFabState(state.issues.isEmpty())
                )
            }

            is ScanEvent.OnScanResult -> {
                val device = BluetoothDeviceUi.from(event.scannedDevice)
                if (_uiState.value.scannedDevices.find { (_, address) -> address == device.address } == null) {
                    _uiState.update {
                        it.copy(scannedDevices = it.scannedDevices + device)
                    }
                }
            }

            is ScanEvent.OnScanFailed -> {
                _uiState.update { state ->
                    state.copy(
                        alertDialogData = event.alertDialogData,
                        fabState = getFabState(state.issues.isEmpty())
                    )
                }
            }
        }
    }

    private fun getFabState(
        isIssueListEmpty: Boolean,
    ): ScanScreenFabState {
        val isBleScanning = bleScannerJob != null
        return when {
            !isIssueListEmpty -> {
                ScanScreenFabState.ISSUE
            }
            isBleScanning -> ScanScreenFabState.IS_SCANNING
            else -> ScanScreenFabState.READY_FOR_SCANNING
        }
    }

    private fun startBleScanCollect() {
        if (bleScannerJob != null) return

        bleScannerJob = bleScannerRepo.observeBleScan(listOf(BleScanFilter(stringOfServiceUuid = PINETIME_UUID_SERVICE)))
            .onEach { value ->
                when (value) {
                    is ScanStatus.DeviceFound -> sendEvent(ScanEvent.OnScanResult(value.device))

                    is ScanStatus.PermissionNotProvided -> addIssuesByPermissionsAndStatus(
                        listOf(value.notProvidedPermission),
                        permissionStatus = IssuePermissionStatus.NOT_GRANTED
                    )

                    ScanStatus.ScanFailed.PowerOptimizedScanUnsupported -> addIssue(BluetoothIssueType.BluetoothScanIssue.PowerOptimizedScanUnsupported)
                    ScanStatus.ScanFailed.ApplicationRegistrationFailed -> addIssue(BluetoothIssueType.BluetoothScanIssue.ApplicationRegistrationFailed)
                    ScanStatus.ScanFailed.OutOfHardwareResources -> addIssue(BluetoothIssueType.BluetoothScanIssue.OutOfHardwareResources)
                    ScanStatus.ScanFailed.InternalError -> addIssue(BluetoothIssueType.BluetoothScanIssue.InternalError)
                    ScanStatus.ScanFailed.ThisAppTriesScanToFrequently -> addIssue(BluetoothIssueType.BluetoothScanIssue.ThisAppTriesScanToFrequently)
                    ScanStatus.ScanFailed.ScanWithSameSettingsIsAlreadyStarted -> addIssue(BluetoothIssueType.BluetoothScanIssue.ScanWithSameSettingsIsAlreadyStarted)
                    is ScanStatus.ScanFailed.UndefinedErrorCode -> addIssue(
                        BluetoothIssueType.BluetoothScanIssue.UndefinedError(value.errorCode)
                    )
                }

            }.launchIn(viewModelScope)
    }

    private fun stopBleScanCollect() {
        bleScannerJob?.cancel()
        bleScannerJob = null
    }
    private fun stopBleScanning(){
        stopBleScanCollect()
        _uiState.update { it.copy(fabState = getFabState(uiState.value.issues.isEmpty())) }
    }

    private fun addIssuesByPermissionsAndStatus(
        notGrantedPermissions: List<String>,
        permissionStatus: IssuePermissionStatus
    ) {
        val newIssueMessages = getIssueTypeByPermission(notGrantedPermissions, permissionStatus)
        val issue: Set<BluetoothIssueType> = _uiState.value.issues.plus(newIssueMessages)
        _uiState.update { it.copy(issues = issue, fabState = getFabState(issue.isEmpty())) }
    }

    private fun addIssue(issueType: BluetoothIssueType) {
        val issue: Set<BluetoothIssueType> =
            _uiState.value.issues.plus(issueType)
        _uiState.update {
            it.copy(
                issues = issue,
                fabState = getFabState(issue.isEmpty())
            )
        }
    }

    override fun onCleared() {
        stopBleScanCollect()
    }
}