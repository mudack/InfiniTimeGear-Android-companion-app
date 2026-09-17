package com.mandarinpirate.pinetimegear.ui.screens._main_activity

import kotlinx.serialization.Serializable

sealed interface MainActivityRoute {

    @Serializable
    data object OnboardingRoute : MainActivityRoute

    @Serializable
    data object ScanRoute : MainActivityRoute

    @Serializable
    data object MainMenuRoute : MainActivityRoute

    @Serializable
    data object WorkingHoursRoute : MainActivityRoute
}