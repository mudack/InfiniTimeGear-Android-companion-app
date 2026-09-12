package com.mandarinpirate.pinetimegear.domain.models

data class BleScanFilter(
    val deviceName: String? = null,
    val stringOfServiceUuid: String? = null,
    val deviceAddress: String? = null,
)