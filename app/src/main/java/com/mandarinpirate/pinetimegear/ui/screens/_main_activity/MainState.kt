package com.mandarinpirate.pinetimegear.ui.screens._main_activity

import com.mandarinpirate.pinetimegear.ui.entities.BluetoothDeviceUi
import com.mandarinpirate.pinetimegear.ui.entities.BluetoothIssueType
import kotlin.reflect.KClass

data class MainState(
    val issues : Map<KClass<out BluetoothIssueType>, BluetoothIssueType> = emptyMap(), //todo change it onto List<IssueType>
    val alertDialogState: AlertDialogState = AlertDialogState.None,
    val isBluetoothEnabled: Boolean = false,
    val isItFirstOnResume: Boolean = true,
    val scannedDevices: List<BluetoothDeviceUi> = emptyList(),
    val fabState: MainScreenFabState = MainScreenFabState.ISSUE,
    val scrollToTheEnd : Boolean = false,
)

enum class MainScreenFabState{
    ISSUE, READY_FOR_SCANNING, IS_SCANNING
}

sealed interface AlertDialogState{
    data object None: AlertDialogState
    data object IssueExists: AlertDialogState
    data object EnableBluetoothError: AlertDialogState
    data class ScanFailed(val errorCode: Int): AlertDialogState
}