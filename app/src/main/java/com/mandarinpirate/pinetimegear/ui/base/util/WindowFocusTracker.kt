package com.mandarinpirate.pinetimegear.ui.base.util

import android.util.Log
import android.view.ViewTreeObserver
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun WindowFocusTracker(
    onFocusLost: () -> Unit = {},
    onFocusGained: () -> Unit = {}
) {
    val activity = LocalActivity.current
    DisposableEffect(activity) {
        val windowFocusChangeListener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (hasFocus) onFocusGained()
            else onFocusLost()
        }

        activity?.window?.decorView?.viewTreeObserver?.addOnWindowFocusChangeListener(
            windowFocusChangeListener
        )
        onDispose {
            activity?.window?.decorView?.viewTreeObserver?.removeOnWindowFocusChangeListener(
                windowFocusChangeListener
            )
        }
    }
}

@Preview
@Composable
fun WindowFocusTrackerPreview(){
    val tag = "WindowFocusTrackerPreview"
    WindowFocusTracker(
        onFocusLost = {Log.i(tag, "onFocusLost")},
        onFocusGained = {Log.i(tag, "onFocusGained")}
    )
}