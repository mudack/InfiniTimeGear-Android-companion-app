package com.mandarinpirate.domain.repos

import com.mandarinpirate.domain.ScanStatus
import com.mandarinpirate.domain.models.BleScanFilter
import kotlinx.coroutines.flow.Flow

interface BleScannerRepo {
    fun observeBleScan(filters: List<BleScanFilter>): Flow<ScanStatus>
}