package com.mandarinpirate.pinetimegear.ui.screens.scan

import com.mandarinpirate.domain.models.BluetoothDevice
import com.mandarinpirate.pinetimegear.ui.entities.AlertDialogData

sealed interface ScanEvent {
    data object FirstOnResume : ScanEvent
    data class OnFabPressed(val issueExistsDialogData: AlertDialogData) : ScanEvent
    data object ScrollDownChipClicked : ScanEvent
    data object DismissAlertDialog : ScanEvent
    data object OnAllPermissionAreGranted : ScanEvent
    data object EnablingBluetoothNoPermissionException : ScanEvent
    data object RequireToCheckIsBluetoothEnabled: ScanEvent //actually it trigger UI to check bluetooth to be enabled and if android ver. is <=11 will also trigger to check location

    data class OnDeniedPermissions(val deniedPermission: List<String>): ScanEvent //send when app tried to get prem access but it was denied by user

    data class OnNotGrantedPermissions(val notGrantedPermission: List<String>): ScanEvent //send when app have spotted that perm is not granted
    data class ShowUpAlertDialog(val alertDialogData: AlertDialogData) : ScanEvent
    data class TryToEnableBluetooth(val isSuccessful: Boolean): ScanEvent
    data class InitBluetoothAvailability(
        val isBluetoothAvailable: Boolean,
        val isBluetoothLEAvailable: Boolean
    ): ScanEvent

    data class OnScanResult(val scannedDevice: BluetoothDevice): ScanEvent
    data class OnScanFailed(val errorCode: Int, val alertDialogData: AlertDialogData): ScanEvent
}