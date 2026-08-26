package com.mandarinpirate.pinetimegear.ui.entities

sealed class IssueType(val level: IssueLevel) {

    sealed class HardwareIssue : IssueType(IssueLevel.NOT_FIXABLE) {
        object BluetoothIsNotAvailable : HardwareIssue()
        object BLEIsNotAvailable : HardwareIssue()
        object PowerOptimizedScanUnsupported : HardwareIssue()
    }

    sealed class Permissions(open val status: IssuePermissionStatus) : IssueType(IssueLevel.FIXABLE) {
        data class Bluetooth(override val status: IssuePermissionStatus) : Permissions(status)
        data class FineLocation(override val status: IssuePermissionStatus) : Permissions(status)
    }
    sealed class BluetoothScanIssue(level: IssueLevel): IssueType(level){
        object ScanWithSameSettingsIsAlreadyStarted : BluetoothScanIssue(IssueLevel.WARNING)
        object ApplicationRegistrationFailed : BluetoothScanIssue(IssueLevel.WARNING)
        object InternalError : BluetoothScanIssue(IssueLevel.WARNING)
        object OutOfHardwareResources : BluetoothScanIssue(IssueLevel.WARNING)
        object BluetoothIsNotEnabled : BluetoothScanIssue(IssueLevel.FIXABLE)
        object ThisAppTriesScanToFrequently : BluetoothScanIssue(IssueLevel.WARNING)
        class UndefinedError(val code: Int): BluetoothScanIssue(IssueLevel.WARNING)
    }
}

enum class IssuePermissionStatus {
    NOT_GRANTED, //not granted means it means it's first time app to be launched and user have never asked to provide permission
    DENIED //denied means user was asked to provide permission but he/she denied it and permission was not provided
}
enum class IssueLevel {
    NOT_FIXABLE,
    FIXABLE,
    WARNING
}