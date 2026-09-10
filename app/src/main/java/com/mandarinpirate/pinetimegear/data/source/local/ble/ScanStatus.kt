package com.mandarinpirate.pinetimegear.data.source.local.ble

import com.mandarinpirate.pinetimegear.domain.models.BluetoothDevice

sealed interface ScanStatus {
    data class DeviceFound(val device: BluetoothDevice) : ScanStatus
    data class PermissionNotProvided(val notProvidedPermission: String) : ScanStatus

    sealed class ScanFailed(errorCode: Int) : ScanStatus {
//        object NoError : ScanFailed //error number = 0

        /** Fails to start scan as BLE scan with the same settings is already started by the app. */
        data object ScanWithSameSettingsIsAlreadyStarted : ScanFailed(1) //error number = 1

        /** Fails to start scan as app cannot be registered. */
        data object ApplicationRegistrationFailed : ScanFailed(2) //error number = 2

        /** Fails to start scan due an internal error */
        data object InternalError : ScanFailed(3)  //error number = 3

        /** Fails to start power optimized scan as this feature is not supported. */
        data object PowerOptimizedScanUnsupported : ScanFailed(4)  //error number = 4

        /** Fails to start scan as it is out of hardware resources. */
        data object OutOfHardwareResources : ScanFailed(5) //error number = 5

        /** Fails to start scan as application tries to scan too frequently. */
        data object ThisAppTriesScanToFrequently : ScanFailed(6)  //error number = 6

        data class UndefinedErrorCode(val errorCode: Int) : ScanFailed(errorCode)

        companion object {
            fun parseByCode(errorCode: Int): ScanFailed = when (errorCode) {
//                0 -> NoError
                1 -> ScanWithSameSettingsIsAlreadyStarted
                2 -> ApplicationRegistrationFailed
                3 -> InternalError
                4 -> PowerOptimizedScanUnsupported
                5 -> OutOfHardwareResources
                6 -> ThisAppTriesScanToFrequently
                else -> UndefinedErrorCode(errorCode)
            }

        }
    }
}