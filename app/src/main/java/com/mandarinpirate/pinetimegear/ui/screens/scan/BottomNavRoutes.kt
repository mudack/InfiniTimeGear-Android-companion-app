package com.mandarinpirate.pinetimegear.ui.screens.scan

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.mandarinpirate.pinetimegear.R
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

sealed interface BottomNavRoute {
    @Serializable
    data object PairedDevicesRoute : BottomNavRoute

    @Serializable
    data object ScanNewDevicesRoute : BottomNavRoute
}

enum class BottomNavRoutes(
    @param:DrawableRes
    @field:DrawableRes
    @get:DrawableRes
    val icon: Int,
    @param:StringRes
    @field:StringRes
    @get:StringRes
    val label: Int,
    val routeClass: KClass<out BottomNavRoute>
) {
    PAIRED_DEVICES(
        R.drawable.paired_devices,
        R.string.scan_screen_paired_devices,
        BottomNavRoute.PairedDevicesRoute::class
    ),
    SCAN_NEW_DEVICES(
        R.drawable.bluetooth_scan,
        R.string.scan_screen_scan_new_devices,
        BottomNavRoute.ScanNewDevicesRoute::class
    )
}
