package com.mandarinpirate.pinetimegear.ui.screens._main_activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mandarinpirate.domain.repos.PreferencesStoreRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val preferencesStoreRepo: PreferencesStoreRepo
) : ViewModel() {

    private val _startDestination = MutableStateFlow<MainActivityStartDestination?>(null)
    val startDestination: StateFlow<MainActivityStartDestination?> =
        _startDestination.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val isFirstAppStart = preferencesStoreRepo.getIsItFirstAppStart()
            val savedDevice = preferencesStoreRepo.getSavedDevice()
            _startDestination.value = when {
                isFirstAppStart -> MainActivityStartDestination.ONBOARDING
                savedDevice != null -> MainActivityStartDestination.MAIN_MENU
                else -> MainActivityStartDestination.SCAN_DEVICE_MISSING
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesStoreRepo.setFirstAppStartFalse()
        }
    }
}

enum class MainActivityStartDestination {
    ONBOARDING,
    MAIN_MENU,
    SCAN_DEVICE_MISSING
}
