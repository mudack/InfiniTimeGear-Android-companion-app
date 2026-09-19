package com.mandarinpirate.pinetimegear.ui.entities

import com.mandarinpirate.domain.models.BluetoothDevice


data class BluetoothDeviceUi(
    val name: String,
    val macAddress: String
){
    fun toDomainBluetoothDevice(): BluetoothDevice = BluetoothDevice(
        name = this.name,
        macAddress = this.macAddress
    )
    companion object {
        fun from(device: BluetoothDevice) = BluetoothDeviceUi(
            name = device.name,
            macAddress = device.macAddress
        )
    }

    override fun equals(other: Any?): Boolean {
        if(other is BluetoothDeviceUi){
            return macAddress.equals(other.macAddress)
        }
        return false
    }

    override fun hashCode(): Int {
        return macAddress.hashCode()
    }
}
