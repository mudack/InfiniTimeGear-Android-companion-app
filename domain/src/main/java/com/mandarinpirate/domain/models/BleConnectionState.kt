package com.mandarinpirate.domain.models

sealed interface BleConnectionState {
    data object Disconnected : BleConnectionState
    data object Connecting : BleConnectionState
    data object Connected : BleConnectionState
    data object WaitingForBluetooth : BleConnectionState
    data object Reconnecting : BleConnectionState
    data object CantResolveTheDevice: BleConnectionState
    data object UnableToStartGattConnection: BleConnectionState
    data class UndefinedBehavior(val message: String) : BleConnectionState
}
