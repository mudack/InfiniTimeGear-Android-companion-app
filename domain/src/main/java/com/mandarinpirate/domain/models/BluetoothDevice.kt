package com.mandarinpirate.domain.models

data class BluetoothDevice(
    val name: String,
    val macAddress: String
) {

    override fun equals(other: Any?): Boolean {
        if (other is BluetoothDevice) {
            return macAddress.equals(other.macAddress)
        }
        return false
    }

    override fun hashCode(): Int {
        return macAddress.hashCode()
    }
}