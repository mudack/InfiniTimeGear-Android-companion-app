package com.mandarinpirate.pinetimegear.ui.screens._main_activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mandarinpirate.pinetimegear.R
import com.mandarinpirate.pinetimegear.ui.screens.scan.ScanScreen
import com.mandarinpirate.pinetimegear.ui.theme.PineTimeGearCompanionAppTheme
import dagger.hilt.android.AndroidEntryPoint
import com.mandarinpirate.pinetimegear.ui.screens._main_activity.MainActivityRoute.*
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PineTimeGearCompanionAppTheme {
                NavGraph()
            }
        }
    }
}

@Composable
private fun NavGraph(
    viewModel: MainActivityViewModel = hiltViewModel()
) {
    val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()
    val destination = startDestination ?: return
    val navController = rememberNavController()
    val context = LocalContext.current

    val prepStringNoSavedDeviceMessage = stringResource(R.string.navigation_no_saved_device_message)
    LaunchedEffect(destination) {
        if (destination == MainActivityStartDestination.SCAN_DEVICE_MISSING) {
            Toast.makeText(
                context,
                prepStringNoSavedDeviceMessage,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    NavHost(
        navController = navController,
        startDestination = destination.route
    ) {
        composable<OnboardingRoute> {
            OnboardingPlaceholder(
                onFinished = {
                    viewModel.completeOnboarding()
                    navController.navigate(ScanRoute) {
                        popUpTo<OnboardingRoute> {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<ScanRoute> {
            ScanScreen()
        }

        composable<MainMenuRoute> {
            MainMenuPlaceholder(
                onWorkingHoursClick = {
                    navController.navigate(WorkingHoursRoute)
                },
                onChooseDeviceClick = {
                    navController.navigate(ScanRoute)
                }
            )
        }

        composable<WorkingHoursRoute> {
            WorkingHoursPlaceholder(
                onBackClick = navController::popBackStack
            )
        }
    }
}

private val MainActivityStartDestination.route: MainActivityRoute
    get() = when (this) {
        MainActivityStartDestination.ONBOARDING -> OnboardingRoute
        MainActivityStartDestination.MAIN_MENU -> MainMenuRoute
        MainActivityStartDestination.SCAN_DEVICE_MISSING -> ScanRoute
    }

@Composable
private fun OnboardingPlaceholder(onFinished: () -> Unit) {
    PlaceholderScreen(
        title = stringResource(R.string.onboarding_title),
        buttonText = stringResource(R.string.onboarding_continue_button),
        onButtonClick = onFinished
    )
}

@Composable
private fun MainMenuPlaceholder(
    onWorkingHoursClick: () -> Unit,
    onChooseDeviceClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.main_menu_title))
        Button(onClick = onWorkingHoursClick) {
            Text(stringResource(R.string.main_menu_working_hours_button))
        }
        Button(onClick = onChooseDeviceClick) {
            Text(stringResource(R.string.main_menu_select_another_device_button))
        }
    }
}

@Composable
private fun WorkingHoursPlaceholder(onBackClick: () -> Unit) {
    PlaceholderScreen(
        title = stringResource(R.string.working_hours_title),
        buttonText = stringResource(R.string.navigation_back_button),
        onButtonClick = onBackClick
    )
}

@Composable
private fun PlaceholderScreen(
    title: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title)
        Button(onClick = onButtonClick) {
            Text(buttonText)
        }
    }
}
