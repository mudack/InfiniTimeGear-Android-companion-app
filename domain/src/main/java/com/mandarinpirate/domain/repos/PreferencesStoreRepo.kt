package com.mandarinpirate.domain.repos

import kotlinx.coroutines.flow.Flow

interface PreferencesStoreRepo {
    fun getIsItFirstAppStart(): Flow<Boolean>
    suspend fun setIsItFirstAppStartFalse()
}