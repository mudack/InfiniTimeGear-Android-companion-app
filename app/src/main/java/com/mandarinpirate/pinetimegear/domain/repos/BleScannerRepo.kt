package com.mandarinpirate.pinetimegear.domain.repos

import com.mandarinpirate.pinetimegear.data.source.local.ble.ScanStatus
import com.mandarinpirate.pinetimegear.domain.models.BleScanFilter
import kotlinx.coroutines.flow.Flow

interface BleScannerRepo {
    fun observeBleScan(filters: List<BleScanFilter>): Flow<ScanStatus>
}