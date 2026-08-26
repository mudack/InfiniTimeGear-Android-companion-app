package com.mandarinpirate.pinetimegear.ui.screens._main_activity

sealed interface MainEffect {
    object StartBleScanning: MainEffect
    object StopBleScanning: MainEffect
}