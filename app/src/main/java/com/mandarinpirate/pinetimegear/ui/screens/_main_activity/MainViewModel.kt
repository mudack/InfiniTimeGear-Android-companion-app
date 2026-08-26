package com.mandarinpirate.pinetimegear.ui.screens._main_activity

import android.bluetooth.BluetoothManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mandarinpirate.pinetimegear.R
import com.mandarinpirate.pinetimegear.data.source.local.ble.ScanStatus
import com.mandarinpirate.pinetimegear.data.source.local.string_provider.StringProvider
import com.mandarinpirate.pinetimegear.domain.repos.BleScannerRepo
import com.mandarinpirate.pinetimegear.ui.entities.AlertDialogData
import com.mandarinpirate.pinetimegear.ui.entities.BluetoothDeviceUi
import com.mandarinpirate.pinetimegear.ui.entities.IssueLevel
import com.mandarinpirate.pinetimegear.ui.entities.IssuePermissionStatus
import com.mandarinpirate.pinetimegear.ui.entities.IssueType
import com.mandarinpirate.pinetimegear.ui.getIssueTypeByPermission
import com.mandarinpirate.pinetimegear.ui.putUsingClassAsAKey
import com.mandarinpirate.pinetimegear.ui.removeUsingClassAsAKey
import com.mandarinpirate.pinetimegear.ui.screens._main_activity.MainScreenFabState
import com.mandarinpirate.pinetimegear.ui.screens._main_activity.MainState
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
    private val stringProvider: StringProvider,
    private val bluetoothManager: BluetoothManager,
    private val bleScannerRepo: BleScannerRepo
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainState())

    val uiState : StateFlow<MainState> = _uiState.stateIn(viewModelScope, WhileSubscribed(5_000),
        MainState()
    )

    init {
        viewModelScope.launch {
            uiState
                .map { state -> state.issues.any { it.value.level != IssueLevel.WARNING } }
                .distinctUntilChanged()
                .collect { shouldStop ->
                    if (shouldStop) stopBleScanCollect()
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
                            alertDialog = AlertDialogData( //todo make sharedFlow to make issue container blink to let user know that it should be fixed
                                title = stringProvider.getString(R.string.alert_title_issue_exists),
                                message = stringProvider.getString(R.string.alert_message_issue_exists),
                                btnText = stringProvider.getString(android.R.string.ok),
                                btnOnClick = {
                                    _uiState.update { currentState ->
                                        currentState.copy(
                                            alertDialog = null
                                        )
                                    }
                                }
                            )
                        )
                    }

                    MainScreenFabState.READY_FOR_SCANNING -> { //here an app should start scanning
                        startBleScanCollect()
                        _uiState.update {
                            it.copy(
                                fabState = MainScreenFabState.IS_SCANNING,
                                scannedDevices = emptyList()
                            )
                        }
                    }

                    MainScreenFabState.IS_SCANNING -> { //here an app should stop scanning
                        stopBleScanCollect()
                        _uiState.update { it.copy(fabState = MainScreenFabState.READY_FOR_SCANNING) }
                    }
                }
            }

            is MainEvent.InitBluetoothAvailability -> {
                val newIssues = uiState.value.issues.toMutableMap()
                if (!event.isBluetoothAvailable) newIssues.putUsingClassAsAKey(IssueType.HardwareIssue.BluetoothIsNotAvailable)
                if (!event.isBluetoothLEAvailable) newIssues.putUsingClassAsAKey(IssueType.HardwareIssue.BLEIsNotAvailable)

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
                    newIssues.putUsingClassAsAKey(IssueType.BluetoothScanIssue.BluetoothIsNotEnabled)
                else newIssues.removeUsingClassAsAKey(IssueType.BluetoothScanIssue.BluetoothIsNotEnabled)
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
                    newIssues.removeUsingClassAsAKey(IssueType.BluetoothScanIssue.BluetoothIsNotEnabled)
                else newIssues.putUsingClassAsAKey(IssueType.BluetoothScanIssue.BluetoothIsNotEnabled)

                _uiState.update {
                    it.copy(
                        isBluetoothEnabled = event.isSuccessful,
                        issues = newIssues,
                        fabState = getFabState(newIssues.isEmpty())
                    )
                }
            }

            is MainEvent.DismissAlertDialog -> _uiState.update { it.copy(alertDialog = null) }
            is MainEvent.ShowUpAlertDialog -> _uiState.update { it.copy(alertDialog = event.alertDialogData) } //todo btnOnClick set in another way, i don't like this way

            MainEvent.OnAllPermissionAreGranted -> {
                val updatedIssues = _uiState.value.issues
                    .filter { it.value !is IssueType.Permissions }
                    .toMutableMap()
                _uiState.update {
                    it.copy(
                        alertDialog = null,
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
                    alertDialog = AlertDialogData(
                        title = stringProvider.getString(R.string.alert_title_enable_bluetooth_exception),
                        message = stringProvider.getString(R.string.alert_message_enable_bluetooth_exception),
                        btnText = stringProvider.getString(android.R.string.ok),
                        btnOnClick = { _uiState.update { it.copy(alertDialog = null) } }
                    ),
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
                        alertDialog = AlertDialogData(
                            title = stringProvider.getString(R.string.alert_title_scan_error),
                            message = stringProvider.getString(
                                R.string.alert_message_scan_error,
                                event.errorCode
                            ),
                            btnText = stringProvider.getString(android.R.string.ok),
                            btnOnClick = { _uiState.update { it.copy(alertDialog = null) } }
                        ),
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
        return when {  //todo violante (Srp)OLID
            !isIssueListEmpty -> {
                if (isBleScanning) stopBleScanCollect()
                MainScreenFabState.ISSUE
            }

            isBleScanning -> MainScreenFabState.IS_SCANNING
            else -> MainScreenFabState.READY_FOR_SCANNING
        }
    }

    private fun startBleScanCollect() {
        if (bleScannerJob != null) return

        bleScannerJob = bleScannerRepo.observeBleScan(
            emptyList()
        )
            .onEach { value ->
                when (value) {
                    is ScanStatus.DeviceFound -> sendEvent(MainEvent.OnScanResult(value.device))

                    is ScanStatus.PermissionNotProvided -> addIssuesByPermissionsAndStatus(
                        listOf(value.notProvidedPermission),
                        permissionStatus = IssuePermissionStatus.NOT_GRANTED
                    )

                    ScanStatus.ScanFailed.PowerOptimizedScanUnsupported -> addIssue(IssueType.HardwareIssue.PowerOptimizedScanUnsupported)
                    ScanStatus.ScanFailed.ApplicationRegistrationFailed -> addIssue(IssueType.BluetoothScanIssue.ApplicationRegistrationFailed)
                    ScanStatus.ScanFailed.OutOfHardwareResources -> addIssue(IssueType.BluetoothScanIssue.OutOfHardwareResources)
                    ScanStatus.ScanFailed.InternalError -> addIssue(IssueType.BluetoothScanIssue.InternalError)
                    ScanStatus.ScanFailed.ThisAppTriesScanToFrequently -> addIssue(IssueType.BluetoothScanIssue.ThisAppTriesScanToFrequently)
                    ScanStatus.ScanFailed.ScanWithSameSettingsIsAlreadyStarted -> addIssue(IssueType.BluetoothScanIssue.ScanWithSameSettingsIsAlreadyStarted)
                    is ScanStatus.ScanFailed.UndefinedErrorCode -> addIssue(
                        IssueType.BluetoothScanIssue.UndefinedError(
                            code = value.errorCode
                        )
                    )
                }

            }.launchIn(viewModelScope)
    }

    private fun stopBleScanCollect() {
        bleScannerJob?.cancel()
        bleScannerJob = null
    }

    private fun addIssuesByPermissionsAndStatus(
        notGrantedPermissions: List<String>,
        permissionStatus: IssuePermissionStatus
    ) {
        val newIssueMessages = getIssueTypeByPermission(notGrantedPermissions, permissionStatus)
        val issue: Map<KClass<out IssueType>, IssueType> =
            _uiState.value.issues.plus(newIssueMessages)
        _uiState.update { it.copy(issues = issue, fabState = getFabState(issue.isEmpty())) }
    }

    private fun addIssue(issueType: IssueType) {
        val result = emptyMap<KClass<out IssueType>, IssueType>().toMutableMap()
        result.putUsingClassAsAKey(issueType)
        val issue: Map<KClass<out IssueType>, IssueType> =
            _uiState.value.issues.plus(result)
        _uiState.update {
            it.copy(
                issues = issue,
                fabState = getFabState(issue.isEmpty())
            )
        }
    }
}