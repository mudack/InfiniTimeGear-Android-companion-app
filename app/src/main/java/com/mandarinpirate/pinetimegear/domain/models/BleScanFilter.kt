package com.mandarinpirate.pinetimegear.domain.models

data class BleScanFilter(
    val deviceName: String? = null,
    val serviceUuid: String? = null,
    val deviceAddress: String? = null,
)