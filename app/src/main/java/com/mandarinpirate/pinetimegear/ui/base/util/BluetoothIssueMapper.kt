package com.mandarinpirate.pinetimegear.ui.base.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mandarinpirate.pinetimegear.R
import com.mandarinpirate.pinetimegear.ui.entities.AlertDialogData
import com.mandarinpirate.pinetimegear.ui.entities.IssueMessageUI
import com.mandarinpirate.pinetimegear.ui.entities.IssuePermissionStatus
import com.mandarinpirate.pinetimegear.ui.entities.BluetoothIssueType
import com.mandarinpirate.pinetimegear.ui.screens._main_activity.AlertDialogState

fun mapIssueToAlertDialog(
    context: Context,
    issueType: BluetoothIssueType
): AlertDialogData = when (issueType) {
    BluetoothIssueType.BluetoothIsNotEnabled -> AlertDialogData(
        title = context.getString(R.string.alert_title_require_to_enable_bluetooth),
        message = context.getString(R.string.alert_message_require_to_enable_bluetooth),
        btnText = context.getString(R.string.alert_btn_require_to_enable_bluetooth)
    )

    BluetoothIssueType.HardwareIssue.BLEIsNotAvailable -> AlertDialogData(
        title = context.getString(R.string.alert_title_ble_is_not_supported),
        message = context.getString(R.string.alert_message_ble_is_not_supported),
        btnText = context.getString(android.R.string.ok)
    )

    BluetoothIssueType.HardwareIssue.BluetoothIsNotAvailable -> AlertDialogData(
        title = context.getString(R.string.alert_title_bluetooth_is_not_supported),
        message = context.getString(R.string.alert_message_bluetooth_is_not_supported),
        btnText = context.getString(android.R.string.ok)
    )

    is BluetoothIssueType.Permissions.Bluetooth -> when (issueType.status) {
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

    is BluetoothIssueType.Permissions.FineLocation -> when (issueType.status) {
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

    BluetoothIssueType.BluetoothScanIssue.ApplicationRegistrationFailed -> AlertDialogData(
        title = context.getString(R.string.issue_title_bluetooth_application_registration_failed),
        message = context.getString(R.string.issue_message_application_registration_failed),
        btnText = context.getString(android.R.string.ok)
    )

    BluetoothIssueType.BluetoothScanIssue.InternalError -> AlertDialogData(
        title = context.getString(R.string.issue_title_bluetooth_internal_error),
        message = context.getString(R.string.issue_message_bluetooth_internal_error),
        btnText = context.getString(android.R.string.ok)
    )

    BluetoothIssueType.BluetoothScanIssue.OutOfHardwareResources -> AlertDialogData(
        title = context.getString(R.string.issue_title_bluetooth_out_of_hardware_resources),
        message = context.getString(R.string.issue_message_bluetooth_out_of_hardware_resources),
        btnText = context.getString(android.R.string.ok)
    )

    BluetoothIssueType.BluetoothScanIssue.ScanWithSameSettingsIsAlreadyStarted -> AlertDialogData(
        title = context.getString(R.string.issue_title_bluetooth_scan_with_same_settings_is_already_started),
        message = context.getString(R.string.issue_message_bluetooth_scan_with_same_settings_is_already_started),
        btnText = context.getString(android.R.string.ok)
    )

    BluetoothIssueType.BluetoothScanIssue.ThisAppTriesScanToFrequently -> AlertDialogData(
        title = context.getString(R.string.issue_title_bluetooth_this_app_tries_scan_to_frequently),
        message = context.getString(R.string.issue_message_bluetooth_this_app_tries_scan_to_frequently),
        btnText = context.getString(android.R.string.ok)
    )

    BluetoothIssueType.BluetoothScanIssue.PowerOptimizedScanUnsupported -> AlertDialogData(
        title = context.getString(R.string.issue_title_bluetooth_power_optimized_scan_unsupported),
        message = context.getString(R.string.issue_message_bluetooth_power_optimized_scan_unsupported),
        btnText = context.getString(android.R.string.ok)
    )

    is BluetoothIssueType.BluetoothScanIssue.UndefinedError -> AlertDialogData(
        title = context.getString(
            R.string.issue_title_bluetooth_undefined_error,
            issueType.errorCode
        ),
        message = context.getString(
            R.string.issue_message_bluetooth_undefined_error,
            issueType.errorCode
        ),
        btnText = context.getString(android.R.string.ok)
    )
}

@Composable
fun mapIssueTypeToIssueMessageUI( //map bluetooth issue into label
    issueType: BluetoothIssueType
): IssueMessageUI = IssueMessageUI(
    message =
        when (issueType) {
            BluetoothIssueType.BluetoothIsNotEnabled -> stringResource(R.string.issue_enable_bluetooth)
            BluetoothIssueType.HardwareIssue.BLEIsNotAvailable -> stringResource(R.string.issue_ble_is_not_supported)
            BluetoothIssueType.HardwareIssue.BluetoothIsNotAvailable -> stringResource(R.string.issue_bluetooth_is_not_supported)
            is BluetoothIssueType.Permissions.Bluetooth -> when (issueType.status) {
                IssuePermissionStatus.NOT_GRANTED -> stringResource(R.string.issue_title_bluetooth_permission_is_not_granted)
                IssuePermissionStatus.DENIED -> stringResource(R.string.issue_title_bluetooth_permission_denied)
            }

            is BluetoothIssueType.Permissions.FineLocation -> when (issueType.status) {
                IssuePermissionStatus.NOT_GRANTED -> stringResource(R.string.issue_title_location_permission_is_not_granted)
                IssuePermissionStatus.DENIED -> stringResource(R.string.issue_title_location_permission_denied)
            }

            BluetoothIssueType.BluetoothScanIssue.ApplicationRegistrationFailed -> stringResource(R.string.issue_title_bluetooth_application_registration_failed)
            BluetoothIssueType.BluetoothScanIssue.InternalError -> stringResource(R.string.issue_title_bluetooth_internal_error)
            BluetoothIssueType.BluetoothScanIssue.OutOfHardwareResources -> stringResource(R.string.issue_title_bluetooth_out_of_hardware_resources)
            BluetoothIssueType.BluetoothScanIssue.ScanWithSameSettingsIsAlreadyStarted -> stringResource(R.string.issue_title_bluetooth_scan_with_same_settings_is_already_started)
            BluetoothIssueType.BluetoothScanIssue.ThisAppTriesScanToFrequently -> stringResource(R.string.issue_title_bluetooth_this_app_tries_scan_to_frequently)
            BluetoothIssueType.BluetoothScanIssue.PowerOptimizedScanUnsupported -> stringResource(R.string.issue_title_bluetooth_power_optimized_scan_unsupported)
            is BluetoothIssueType.BluetoothScanIssue.UndefinedError -> stringResource(
                R.string.issue_title_bluetooth_undefined_error,
                issueType.errorCode
            )
        },
    type = issueType
)

fun mapIssueToAlertDialogState(issueType: BluetoothIssueType): AlertDialogState = when (issueType) {
    is BluetoothIssueType.BluetoothScanIssue -> AlertDialogState.ScanFailed(issueType.errorCode)
    is BluetoothIssueType.Permissions -> AlertDialogState.EnableBluetoothError
    else -> AlertDialogState.IssueExists
}