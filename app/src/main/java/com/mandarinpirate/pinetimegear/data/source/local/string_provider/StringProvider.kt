package com.mandarinpirate.pinetimegear.data.source.local.string_provider

import androidx.annotation.StringRes

interface StringProvider {
    fun getString(@StringRes id: Int): String
    fun getString(@StringRes id: Int, vararg formatArgs:Any): String
}