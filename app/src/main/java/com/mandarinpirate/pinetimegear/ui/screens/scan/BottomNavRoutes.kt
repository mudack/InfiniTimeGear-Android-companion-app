package com.mandarinpirate.pinetimegear.ui.screens.scan

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.mandarinpirate.pinetimegear.R

enum class BottomNavRoutes(@param:DrawableRes val icon: Int, @param:StringRes val label: Int) {
    PAIRED_DEVICES(
        R.drawable.paired_devices,
        R.string.scan_screen_paired_devices
    ),
    SCAN_NEW_DEVICES(
        R.drawable.bluetooth_scan,
        R.string.scan_screen_scan_new_devices
    )
}