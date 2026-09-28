package com.mandarinpirate.pinetimegear.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.mandarinpirate.pinetimegear.R
import com.mandarinpirate.pinetimegear.ui.theme.PineTimeGearCompanionAppTheme

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    PineTimeGearCompanionAppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                val fabDimension = dimensionResource(R.dimen.fab_size)
                val fabIconDimension = dimensionResource(R.dimen.fab_icon_size)
                FilledIconButton(
                    modifier = Modifier.size(fabDimension),
                    onClick = onFinished
                ){
                    Icon(
                        modifier = Modifier.size(fabIconDimension),
                        painter = painterResource(R.drawable.arrow_right),
                        contentDescription = stringResource(R.string.fab_icon_onboarding_next_description)
                    )
                }
            }
        ){ innerPadding ->
            OnboardingContent(Modifier.padding(innerPadding))
        }
    }
}

@Composable
fun OnboardingContent(
    modifier: Modifier = Modifier
){
    Column(modifier, verticalArrangement = Arrangement.Center) {
        Text(text = stringResource(R.string.onboarding_text))
    }
}