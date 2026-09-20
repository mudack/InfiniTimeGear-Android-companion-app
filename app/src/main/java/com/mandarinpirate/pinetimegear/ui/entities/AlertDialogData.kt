package com.mandarinpirate.pinetimegear.ui.entities

data class AlertDialogData(
    val title: String,
    val message: String,
    val btnText: String,
    val btnOnClick: (() -> Unit)
)
