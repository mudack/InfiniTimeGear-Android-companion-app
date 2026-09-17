package com.mandarinpirate.data.local.string_provider

import android.content.Context

class StringProviderImpl(val context: Context): StringProvider {
    override fun getString(id: Int): String = context.getString(id)
    override fun getString(id: Int, vararg formatArgs:Any): String = context.getString(id, formatArgs)
}