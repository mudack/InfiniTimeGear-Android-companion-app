package com.mandarinpirate.pinetimegear.ui.screens._main_activity

import com.mandarinpirate.pinetimegear.domain.models.BluetoothDevice
import com.mandarinpirate.pinetimegear.ui.entities.AlertDialogData

sealed interface MainEvent {
    data object FirstOnResume : MainEvent
    data object OnFabPressed : MainEvent
    data object ScrollDownChipClicked : MainEvent
    data object DismissAlertDialog : MainEvent
    data object OnAllPermissionAreGranted : MainEvent
    data object EnablingBluetoothNoPermissionException : MainEvent
    data object RequireToCheckIsBluetoothEnabled: MainEvent //actually it trigger UI to check bluetooth to be enabled and if android ver. is <=11 will also trigger to check location

    data class OnDeniedPermissions(val deniedPermission: List<String>): MainEvent //send when app tried to get prem access but it was denied by user

    data class OnNotGrantedPermissions(val notGrantedPermission: List<String>): MainEvent //send when app have spotted that perm is not granted
    data class ShowUpAlertDialog(val alertDialogData: AlertDialogData) : MainEvent
    data class TryToEnableBluetooth(val isSuccessful: Boolean): MainEvent
    data class InitBluetoothAvailability(
        val isBluetoothAvailable: Boolean,
        val isBluetoothLEAvailable: Boolean
    ): MainEvent

    data class OnScanResult(val scannedDevice: BluetoothDevice): MainEvent
    data class OnScanFailed(val errorCode: Int): MainEvent
}