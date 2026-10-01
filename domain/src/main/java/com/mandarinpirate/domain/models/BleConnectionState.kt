package com.mandarinpirate.domain.models

sealed interface BleConnectionState {
    data object Disconnected : BleConnectionState
    data object Connecting : BleConnectionState
    data object Connected : BleConnectionState
    data object WaitingForBluetooth : BleConnectionState
    data class Reconnecting(val attempt: Int) : BleConnectionState
}
