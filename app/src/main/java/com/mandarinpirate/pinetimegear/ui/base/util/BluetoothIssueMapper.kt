package com.mandarinpirate.pinetimegear.ui.base.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mandarinpirate.pinetimegear.R
import com.mandarinpirate.pinetimegear.ui.entities.AlertDialogData
import com.mandarinpirate.pinetimegear.ui.entities.IssueMessageUI
import com.mandarinpirate.pinetimegear.ui.entities.IssuePermissionStatus
import com.mandarinpirate.pinetimegear.ui.entities.IssueType

fun bluetoothIssueToAlertDialogMapper(
    context: Context,
    issue: IssueMessageUI
): AlertDialogData = when (val type = issue.type) {
    IssueType.BluetoothScanIssue.BluetoothIsNotEnabled -> AlertDialogData(
        title = context.getString(R.string.alert_title_require_to_enable_bluetooth),
        message = context.getString(R.string.alert_message_require_to_enable_bluetooth),
        btnText = context.getString(R.string.alert_btn_require_to_enable_bluetooth)
    )

    IssueType.HardwareIssue.BLEIsNotAvailable -> AlertDialogData(
        title = context.getString(R.string.alert_title_ble_is_not_supported),
        message = context.getString(R.string.alert_message_ble_is_not_supported),
        btnText = context.getString(android.R.string.ok)
    )

    IssueType.HardwareIssue.BluetoothIsNotAvailable -> AlertDialogData(
        title = context.getString(R.string.alert_title_bluetooth_is_not_supported),
        message = context.getString(R.string.alert_message_bluetooth_is_not_supported),
        btnText = context.getString(android.R.string.ok)
    )

    is IssueType.Permissions.Bluetooth -> when (type.status) {
        IssuePermissionStatus.NOT_GRANTED -> AlertDialogData(
            title = context.getString(R.string.issue_title_bluetooth_permission_is_not_granted),
            message = context.getString(R.string.alert_message_bluetooth_permission_is_not_granted),
            btnText = context.getString(R.string.issue_btn_text_provide_permission)
        )
        IssuePermissionStatus.DENIED -> AlertDialogData(
            title = context.getString(R.string.alert_title_bluetooth_permission_denied),
            message = context.getString(R.string.alert_message_bluetooth_permission_denied),
            btnText = context.getString(R.string.alert_btn_bluetooth_permission_denied)
        )
    }
    is IssueType.Permissions.FineLocation -> when (type.status) {
        IssuePermissionStatus.NOT_GRANTED -> AlertDialogData(
            title = context.getString(R.string.alert_title_location_permission_is_not_granted),
            message = context.getString(R.string.alert_message_location_permission_is_not_granted),
            btnText = context.getString(R.string.issue_btn_text_provide_permission)
        )
        IssuePermissionStatus.DENIED -> AlertDialogData(
            title = context.getString(R.string.alert_title_location_permission_denied),
            message = context.getString(R.string.alert_message_location_permission_denied),
            btnText = context.getString(R.string.alert_btn_bluetooth_permission_denied)
        )
    }

    IssueType.BluetoothScanIssue.ApplicationRegistrationFailed -> AlertDialogData(
        title = context.getString(R.string.issue_title_bluetooth_application_registration_failed),
        message = context.getString(R.string.issue_message_application_registration_failed),
        btnText = context.getString(android.R.string.ok)
    )
    IssueType.BluetoothScanIssue.InternalError -> AlertDialogData(
        title = context.getString(R.string.issue_title_bluetooth_internal_error),
        message = context.getString(R.string.issue_message_bluetooth_internal_error),
        btnText = context.getString(android.R.string.ok)
    )
    IssueType.BluetoothScanIssue.OutOfHardwareResources -> AlertDialogData(
        title = context.getString(R.string.issue_title_bluetooth_out_of_hardware_resources),
        message = context.getString(R.string.issue_message_bluetooth_out_of_hardware_resources),
        btnText = context.getString(android.R.string.ok)
    )
    IssueType.BluetoothScanIssue.ScanWithSameSettingsIsAlreadyStarted -> AlertDialogData(
        title = context.getString(R.string.issue_title_bluetooth_scan_with_same_settings_is_already_started),
        message = context.getString(R.string.issue_message_bluetooth_scan_with_same_settings_is_already_started),
        btnText = context.getString(android.R.string.ok)
    )
    IssueType.BluetoothScanIssue.ThisAppTriesScanToFrequently -> AlertDialogData(
        title = context.getString(R.string.issue_title_bluetooth_this_app_tries_scan_to_frequently),
        message = context.getString(R.string.issue_message_bluetooth_this_app_tries_scan_to_frequently),
        btnText = context.getString(android.R.string.ok)
    )
    IssueType.HardwareIssue.PowerOptimizedScanUnsupported -> AlertDialogData(
        title = context.getString(R.string.issue_title_bluetooth_power_optimized_scan_unsupported),
        message = context.getString(R.string.issue_message_bluetooth_power_optimized_scan_unsupported),
        btnText = context.getString(android.R.string.ok)
    )
    is IssueType.BluetoothScanIssue.UndefinedError -> AlertDialogData(
        title = context.getString(R.string.issue_title_bluetooth_undefined_error, type.code),
        message = context.getString(R.string.issue_message_bluetooth_undefined_error, type.code),
        btnText = context.getString(android.R.string.ok)
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