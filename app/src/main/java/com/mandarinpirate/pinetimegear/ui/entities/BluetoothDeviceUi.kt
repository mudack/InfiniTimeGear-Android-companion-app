package com.mandarinpirate.pinetimegear.ui.entities

import com.mandarinpirate.domain.models.BluetoothDevice


data class BluetoothDeviceUi(
    val name: String,
    val address: String
){
    fun toDomainBluetoothDevice(): BluetoothDevice = BluetoothDevice(
        name = this.name,
        address = this.address
    )
    companion object {
        fun from(device: BluetoothDevice) = BluetoothDeviceUi(
            name = device.name,
            address = device.address
        )
    }
}
