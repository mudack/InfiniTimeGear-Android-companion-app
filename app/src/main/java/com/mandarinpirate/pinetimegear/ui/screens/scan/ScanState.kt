package com.mandarinpirate.pinetimegear.ui.screens.scan

import com.mandarinpirate.pinetimegear.ui.entities.AlertDialogData
import com.mandarinpirate.pinetimegear.ui.entities.BluetoothDeviceUi
import com.mandarinpirate.pinetimegear.ui.entities.BluetoothIssueType

data class ScanState(
    val issues : Set<BluetoothIssueType> = emptySet(),
    val alertDialogData: AlertDialogData? = null,
    val isBluetoothEnabled: Boolean = false,
    val isItFirstOnResume: Boolean = true,
    val scannedDevices: List<BluetoothDeviceUi> = emptyList(),
    val fabState: ScanScreenFabState = ScanScreenFabState.ISSUE,
    val scrollToTheEnd : Boolean = false,
)

enum class ScanScreenFabState{
    ISSUE, READY_FOR_SCANNING, IS_SCANNING
}