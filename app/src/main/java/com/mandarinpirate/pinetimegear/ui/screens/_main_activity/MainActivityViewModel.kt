package com.mandarinpirate.pinetimegear.ui.screens._main_activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mandarinpirate.domain.repos.PreferencesStoreRepo
import com.mandarinpirate.domain.repos.SavedDeviceRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val preferencesStoreRepo: PreferencesStoreRepo,
    private val savedDeviceRepo: SavedDeviceRepo
) : ViewModel() {

    private val _startDestination = MutableStateFlow<MainActivityStartDestination?>(null)
    val startDestination: StateFlow<MainActivityStartDestination?> =
        _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            _startDestination.value = combine(
                preferencesStoreRepo.getIsItFirstAppStart(),
                savedDeviceRepo.observeSavedDevice()
            ) { isFirstAppStart, savedDevice ->
                when {
                    isFirstAppStart -> MainActivityStartDestination.ONBOARDING
                    savedDevice != null -> MainActivityStartDestination.MAIN_MENU
                    else -> MainActivityStartDestination.SCAN_DEVICE_MISSING
                }
            }.first()
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesStoreRepo.setIsItFirstAppStartFalse()
        }
    }
}

enum class MainActivityStartDestination {
    ONBOARDING,
    MAIN_MENU,
    SCAN_DEVICE_MISSING
}
