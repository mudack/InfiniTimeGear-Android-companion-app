package com.mandarinpirate.pinetimegear.ui.screens.device_control

interface DeviceControlEvent {
    object TryToConnect : DeviceControlEvent
    object TryToDisconnect : DeviceControlEvent
}