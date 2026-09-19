package com.mandarinpirate.pinetimegear.ui.screens

import kotlinx.serialization.Serializable

sealed interface ScreenRoute {

    @Serializable
    data object OnboardingRoute : ScreenRoute

    @Serializable
    data object ScanRoute : ScreenRoute

    @Serializable
    data object MainMenuRoute : ScreenRoute

    @Serializable
    data object WorkingHoursRoute : ScreenRoute
}