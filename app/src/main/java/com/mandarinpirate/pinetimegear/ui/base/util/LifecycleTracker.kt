package com.mandarinpirate.pinetimegear.ui.base.util

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner


@Composable
fun LifecycleTracker(
    onCreate: () -> Unit = {},
    onStart: () -> Unit = {},
    onResume: () -> Unit = {},
    onPause: () -> Unit = {},
    onStop: () -> Unit = {},
    onDestroy: () -> Unit = {}
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val lifecycleObserver = object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                super.onCreate(owner)
                onCreate()
            }

            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                onStart()
            }

            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                onResume()
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                onPause()
            }


            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                onStop()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                onDestroy()
            }
        }

        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }
}

@Preview
@Composable
fun LifecycleTrackerPreview(){
    val tag = "LifecycleTrackerPreview"
    LifecycleTracker(
        onCreate = { Log.i(tag, "onCreate") },
        onStart =  { Log.i(tag, "onStart") },
        onResume =  { Log.i(tag, "onResume") },
        onPause =  { Log.i(tag, "onPause") },
        onStop =  { Log.i(tag, "onStop") },
        onDestroy =  { Log.i(tag,"onDestroy") },
    )
}