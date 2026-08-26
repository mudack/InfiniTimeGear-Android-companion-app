package com.mandarinpirate.pinetimegear.ui.base.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mandarinpirate.pinetimegear.R
import com.mandarinpirate.pinetimegear.ui.entities.AlertDialogData
import com.mandarinpirate.pinetimegear.ui.entities.IssueMessageUI
import com.mandarinpirate.pinetimegear.ui.entities.IssuePermissionStatus
import com.mandarinpirate.pinetimegear.ui.entities.IssueType

@Composable
fun bluetoothIssueToAlertDialogMapper(
    issue: IssueMessageUI
): AlertDialogData = when (val type = issue.type) {
    IssueType.BluetoothScanIssue.BluetoothIsNotEnabled -> AlertDialogData(
        title = stringResource(R.string.alert_title_require_to_enable_bluetooth),
        message = stringResource(R.string.alert_message_require_to_enable_bluetooth),
        btnText = stringResource(R.string.alert_btn_require_to_enable_bluetooth)
    )

    IssueType.HardwareIssue.BLEIsNotAvailable -> AlertDialogData(
        title = stringResource(R.string.alert_title_ble_is_not_supported),
        message = stringResource(R.string.alert_message_ble_is_not_supported),
        btnText = stringResource(android.R.string.ok)
    )

    IssueType.HardwareIssue.BluetoothIsNotAvailable -> AlertDialogData(
        title = stringResource(R.string.alert_title_bluetooth_is_not_supported),
        message = stringResource(R.string.alert_message_bluetooth_is_not_supported),
        btnText = stringResource(android.R.string.ok)
    )

    is IssueType.Permissions.Bluetooth -> when (type.status) {
        IssuePermissionStatus.NOT_GRANTED -> AlertDialogData(
            title = stringResource(R.string.issue_title_bluetooth_permission_is_not_granted),
            message = stringResource(R.string.alert_message_bluetooth_permission_is_not_granted),
            btnText = stringResource(R.string.issue_btn_text_provide_permission)
        )
        IssuePermissionStatus.DENIED -> AlertDialogData(
            title = stringResource(R.string.alert_title_bluetooth_permission_denied),
            message = stringResource(R.string.alert_message_bluetooth_permission_denied),
            btnText = stringResource(R.string.alert_btn_bluetooth_permission_denied)
        )
    }
    is IssueType.Permissions.FineLocation -> when (type.status) {
        IssuePermissionStatus.NOT_GRANTED -> AlertDialogData(
            title = stringResource(R.string.alert_title_location_permission_is_not_granted),
            message = stringResource(R.string.alert_message_location_permission_is_not_granted),
            btnText = stringResource(R.string.issue_btn_text_provide_permission)
        )
        IssuePermissionStatus.DENIED -> AlertDialogData(
            title = stringResource(R.string.alert_title_location_permission_denied),
            message = stringResource(R.string.alert_message_location_permission_denied),
            btnText = stringResource(R.string.alert_btn_bluetooth_permission_denied)
        )
    }

    IssueType.BluetoothScanIssue.ApplicationRegistrationFailed -> AlertDialogData(
        title = stringResource(R.string.issue_title_bluetooth_application_registration_failed),
        message = stringResource(R.string.issue_message_application_registration_failed),
        btnText = stringResource(android.R.string.ok)
    )
    IssueType.BluetoothScanIssue.InternalError -> AlertDialogData(
        title = stringResource(R.string.issue_title_bluetooth_internal_error),
        message = stringResource(R.string.issue_message_bluetooth_internal_error),
        btnText = stringResource(android.R.string.ok)
    )
    IssueType.BluetoothScanIssue.OutOfHardwareResources -> AlertDialogData(
        title = stringResource(R.string.issue_title_bluetooth_out_of_hardware_resources),
        message = stringResource(R.string.issue_message_bluetooth_out_of_hardware_resources),
        btnText = stringResource(android.R.string.ok)
    )
    IssueType.BluetoothScanIssue.ScanWithSameSettingsIsAlreadyStarted -> AlertDialogData(
        title = stringResource(R.string.issue_title_bluetooth_scan_with_same_settings_is_already_started),
        message = stringResource(R.string.issue_message_bluetooth_scan_with_same_settings_is_already_started),
        btnText = stringResource(android.R.string.ok)
    )
    IssueType.BluetoothScanIssue.ThisAppTriesScanToFrequently -> AlertDialogData(
        title = stringResource(R.string.issue_title_bluetooth_this_app_tries_scan_to_frequently),
        message = stringResource(R.string.issue_message_bluetooth_this_app_tries_scan_to_frequently),
        btnText = stringResource(android.R.string.ok)
    )
    IssueType.HardwareIssue.PowerOptimizedScanUnsupported -> AlertDialogData(
        title = stringResource(R.string.issue_title_bluetooth_power_optimized_scan_unsupported),
        message = stringResource(R.string.issue_message_bluetooth_power_optimized_scan_unsupported),
        btnText = stringResource(android.R.string.ok)
    )
    is IssueType.BluetoothScanIssue.UndefinedError -> AlertDialogData(
        title = stringResource(R.string.issue_title_bluetooth_undefined_error, type.code),
        message = stringResource(R.string.issue_message_bluetooth_undefined_error, type.code),
        btnText = stringResource(android.R.string.ok)
    )
}

@Composable
fun bluetoothIssueTypeToIssueMessageMapper( //map bluetooth issue into label
    issueMessageType: IssueType
): IssueMessageUI = IssueMessageUI(
    message =
        when (issueMessageType) {
            IssueType.BluetoothScanIssue.BluetoothIsNotEnabled -> stringResource(R.string.issue_enable_bluetooth)
            IssueType.HardwareIssue.BLEIsNotAvailable -> stringResource(R.string.issue_ble_is_not_supported)
            IssueType.HardwareIssue.BluetoothIsNotAvailable -> stringResource(R.string.issue_bluetooth_is_not_supported)
            is IssueType.Permissions.Bluetooth -> when(issueMessageType.status){
                IssuePermissionStatus.NOT_GRANTED -> stringResource(R.string.issue_title_bluetooth_permission_is_not_granted)
                IssuePermissionStatus.DENIED -> stringResource(R.string.issue_title_bluetooth_permission_denied)
            }
            is IssueType.Permissions.FineLocation -> when(issueMessageType.status) {
                IssuePermissionStatus.NOT_GRANTED -> stringResource(R.string.issue_title_location_permission_is_not_granted)
                IssuePermissionStatus.DENIED -> stringResource(R.string.issue_title_location_permission_denied)
            }

            IssueType.BluetoothScanIssue.ApplicationRegistrationFailed -> stringResource(R.string.issue_title_bluetooth_application_registration_failed)
            IssueType.BluetoothScanIssue.InternalError -> stringResource(R.string.issue_title_bluetooth_internal_error)
            IssueType.BluetoothScanIssue.OutOfHardwareResources -> stringResource(R.string.issue_title_bluetooth_out_of_hardware_resources)
            IssueType.BluetoothScanIssue.ScanWithSameSettingsIsAlreadyStarted -> stringResource(R.string.issue_title_bluetooth_scan_with_same_settings_is_already_started)
            IssueType.BluetoothScanIssue.ThisAppTriesScanToFrequently -> stringResource(R.string.issue_title_bluetooth_this_app_tries_scan_to_frequently)
            IssueType.HardwareIssue.PowerOptimizedScanUnsupported -> stringResource(R.string.issue_title_bluetooth_power_optimized_scan_unsupported)
            is IssueType.BluetoothScanIssue.UndefinedError -> stringResource(R.string.issue_title_bluetooth_undefined_error, issueMessageType.code)
        },
    type = issueMessageType
)