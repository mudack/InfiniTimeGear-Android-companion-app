package com.mandarinpirate.pinetimegear.ui.screens._main_activity

import android.bluetooth.BluetoothManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mandarinpirate.pinetimegear.data.source.local.ble.ScanStatus
import com.mandarinpirate.pinetimegear.domain.models.BleScanFilter
import com.mandarinpirate.pinetimegear.domain.repos.BleScannerRepo
import com.mandarinpirate.pinetimegear.ui.entities.BluetoothDeviceUi
import com.mandarinpirate.pinetimegear.ui.entities.IssuePermissionStatus
import com.mandarinpirate.pinetimegear.ui.entities.BluetoothIssueType
import com.mandarinpirate.pinetimegear.ui.getIssueTypeByPermission
import com.mandarinpirate.pinetimegear.ui.putUsingClassAsAKey
import com.mandarinpirate.pinetimegear.ui.removeUsingClassAsAKey
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
import kotlin.reflect.KClass

@HiltViewModel
class MainViewModel @Inject constructor(
    private val bluetoothManager: BluetoothManager,
    private val bleScannerRepo: BleScannerRepo
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainState())

    val uiState: StateFlow<MainState> = _uiState.stateIn(
        viewModelScope, WhileSubscribed(5_000),
        MainState()
    )

    init {
        viewModelScope.launch {
            uiState
                .map { state -> state.issues.isNotEmpty() }
                .distinctUntilChanged()
                .collect { shouldStop ->
                    if (shouldStop) stopBleScanning()
                }
        }
    }

    private var bleScannerJob: Job? = null

    fun sendEvent(event: MainEvent) {
        when (event) {
            is MainEvent.FirstOnResume -> _uiState.update { it.copy(isItFirstOnResume = false) }
            is MainEvent.ScrollDownChipClicked -> _uiState.update {
                it.copy(
                    scrollToTheEnd = !it.scrollToTheEnd
                )
            }

            is MainEvent.OnFabPressed -> {
                when (uiState.value.fabState) {
                    MainScreenFabState.ISSUE -> _uiState.update {
                        it.copy(
                            alertDialogData = event.issueExistsDialogData
                        )
                    }

                    MainScreenFabState.READY_FOR_SCANNING -> {
                        startBleScanCollect()
                        _uiState.update {
                            it.copy(
                                fabState = MainScreenFabState.IS_SCANNING,
                                scannedDevices = emptyList()
                            )
                        }
                    }

                    MainScreenFabState.IS_SCANNING -> {
                        stopBleScanCollect()
                        _uiState.update { it.copy(fabState = MainScreenFabState.READY_FOR_SCANNING) }
                    }
                }
            }

            is MainEvent.InitBluetoothAvailability -> {
                val newIssues = uiState.value.issues.toMutableMap()
                if (!event.isBluetoothAvailable) newIssues.putUsingClassAsAKey(BluetoothIssueType.HardwareIssue.BluetoothIsNotAvailable)
                if (!event.isBluetoothLEAvailable) newIssues.putUsingClassAsAKey(BluetoothIssueType.HardwareIssue.BLEIsNotAvailable)

                _uiState.update {
                    it.copy(
                        issues = newIssues,
                        fabState = getFabState(
                            isIssueListEmpty = newIssues.isEmpty()
                        )
                    )
                }
            }

            MainEvent.RequireToCheckIsBluetoothEnabled -> {
                val newIssues = _uiState.value.issues.toMutableMap()
                if (!bluetoothManager.adapter.isEnabled)
                    newIssues.putUsingClassAsAKey(BluetoothIssueType.BluetoothIsNotEnabled)
                else newIssues.removeUsingClassAsAKey(BluetoothIssueType.BluetoothIsNotEnabled)
                _uiState.update {
                    it.copy(
                        issues = newIssues,
                        fabState = getFabState(newIssues.isEmpty())
                    )
                }
            }

            is MainEvent.TryToEnableBluetooth -> {
                val newIssues = _uiState.value.issues.toMutableMap()
                if (event.isSuccessful)
                    newIssues.removeUsingClassAsAKey(BluetoothIssueType.BluetoothIsNotEnabled)
                else newIssues.putUsingClassAsAKey(BluetoothIssueType.BluetoothIsNotEnabled)

                _uiState.update {
                    it.copy(
                        isBluetoothEnabled = event.isSuccessful,
                        issues = newIssues,
                        fabState = getFabState(newIssues.isEmpty())
                    )
                }
            }

            is MainEvent.DismissAlertDialog -> _uiState.update { it.copy(alertDialogData = null) }
            is MainEvent.ShowUpAlertDialog -> _uiState.update { it.copy(alertDialogData = event.alertDialogData) }

            MainEvent.OnAllPermissionAreGranted -> {
                val updatedIssues = _uiState.value.issues
                    .filter { it.value !is BluetoothIssueType.Permissions }
                    .toMutableMap()
                _uiState.update {
                    it.copy(
                        alertDialogData = null,
                        issues = updatedIssues,
                        fabState = getFabState(updatedIssues.isEmpty())
                    )
                }
            }

            is MainEvent.OnNotGrantedPermissions -> addIssuesByPermissionsAndStatus(
                event.notGrantedPermission,
                permissionStatus = IssuePermissionStatus.NOT_GRANTED
            )

            is MainEvent.OnDeniedPermissions -> addIssuesByPermissionsAndStatus(
                event.deniedPermission,
                permissionStatus = IssuePermissionStatus.DENIED,
            )

            MainEvent.EnablingBluetoothNoPermissionException -> _uiState.update { state ->
                state.copy(
                    fabState = getFabState(state.issues.isEmpty())
                )
            }

            is MainEvent.OnScanResult -> {
                val device = BluetoothDeviceUi.from(event.scannedDevice)
                if (_uiState.value.scannedDevices.find { (_, address) -> address == device.address } == null) {
                    _uiState.update {
                        it.copy(scannedDevices = it.scannedDevices + device)
                    }
                }
            }

            is MainEvent.OnScanFailed -> {
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
    ): MainScreenFabState {
        val isBleScanning = bleScannerJob != null
        return when {
            !isIssueListEmpty -> {
                MainScreenFabState.ISSUE
            }
            isBleScanning -> MainScreenFabState.IS_SCANNING
            else -> MainScreenFabState.READY_FOR_SCANNING
        }
    }

    private fun startBleScanCollect() {
        if (bleScannerJob != null) return

        bleScannerJob = bleScannerRepo.observeBleScan(listOf(BleScanFilter(stringOfServiceUuid = "00001530-1212-efde-1523-785feabcd123")))
            .onEach { value ->
                when (value) {
                    is ScanStatus.DeviceFound -> sendEvent(MainEvent.OnScanResult(value.device))

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
        getFabState(uiState.value.issues.isEmpty())
    }

    private fun addIssuesByPermissionsAndStatus(
        notGrantedPermissions: List<String>,
        permissionStatus: IssuePermissionStatus
    ) {
        val newIssueMessages = getIssueTypeByPermission(notGrantedPermissions, permissionStatus)
        val issue: Map<KClass<out BluetoothIssueType>, BluetoothIssueType> =
            _uiState.value.issues.plus(newIssueMessages)
        _uiState.update { it.copy(issues = issue, fabState = getFabState(issue.isEmpty())) }
    }

    private fun addIssue(issueType: BluetoothIssueType) {
        val result = emptyMap<KClass<out BluetoothIssueType>, BluetoothIssueType>().toMutableMap()
        result.putUsingClassAsAKey(issueType)
        val issue: Map<KClass<out BluetoothIssueType>, BluetoothIssueType> =
            _uiState.value.issues.plus(result)
        _uiState.update {
            it.copy(
                issues = issue,
                fabState = getFabState(issue.isEmpty())
            )
        }
    }
}