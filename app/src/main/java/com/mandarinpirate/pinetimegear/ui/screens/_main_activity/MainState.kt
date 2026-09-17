package com.mandarinpirate.pinetimegear.ui.screens._main_activity

import com.mandarinpirate.pinetimegear.ui.entities.AlertDialogData
import com.mandarinpirate.pinetimegear.ui.entities.BluetoothDeviceUi
import com.mandarinpirate.pinetimegear.ui.entities.BluetoothIssueType

data class MainState(
    val issues : Set<BluetoothIssueType> = emptySet(),
    val alertDialogData: AlertDialogData? = null,
    val isBluetoothEnabled: Boolean = false,
    val isItFirstOnResume: Boolean = true,
    val scannedDevices: List<BluetoothDeviceUi> = emptyList(),
    val fabState: MainScreenFabState = MainScreenFabState.ISSUE,
    val scrollToTheEnd : Boolean = false,
)

enum class MainScreenFabState{
    ISSUE, READY_FOR_SCANNING, IS_SCANNING
}