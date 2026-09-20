package com.mandarinpirate.pinetimegear.ui.entities

sealed class AppIssueType(val level: IssueLevel) {

    data object BluetoothIsNotEnabled : AppIssueType(IssueLevel.FIXABLE)

    sealed class HardwareIssue : AppIssueType(IssueLevel.NOT_FIXABLE) {
        data object BluetoothIsNotAvailable : HardwareIssue()
        data object BLEIsNotAvailable : HardwareIssue()
    }

    sealed class Permissions(open val status: IssuePermissionStatus) : AppIssueType(IssueLevel.FIXABLE) {
        data class Bluetooth(override val status: IssuePermissionStatus) : Permissions(status)
        data class FineLocation(override val status: IssuePermissionStatus) : Permissions(status)
        data class PostNotification(override val status: IssuePermissionStatus) : Permissions(status)
    }
    sealed class BluetoothScanIssue(
        level: IssueLevel,
        open val errorCode: Int
    ) : AppIssueType(level){
        data object ScanWithSameSettingsIsAlreadyStarted : BluetoothScanIssue(IssueLevel.WARNING, errorCode = 1)
        data object ApplicationRegistrationFailed : BluetoothScanIssue(IssueLevel.WARNING, errorCode = 2)
        data object InternalError : BluetoothScanIssue(IssueLevel.WARNING, errorCode = 3)

        data object PowerOptimizedScanUnsupported : BluetoothScanIssue(IssueLevel.NOT_FIXABLE, errorCode = 4)
        data object OutOfHardwareResources : BluetoothScanIssue(IssueLevel.WARNING, errorCode = 5)
        data object ThisAppTriesScanToFrequently : BluetoothScanIssue(IssueLevel.WARNING, errorCode = 6)
        data class UndefinedError(override val errorCode: Int): BluetoothScanIssue(IssueLevel.WARNING, errorCode = errorCode)
    }
}

enum class IssuePermissionStatus {
    NOT_GRANTED, //not granted means it's first time app to be launched and user have never asked to provide permission
    DENIED //denied means user was asked to provide permission but he/she denied it and permission was not provided
}
enum class IssueLevel {
    NOT_FIXABLE,
    FIXABLE,
    WARNING
}