package com.goodwy.commons.compose.extensions

import android.content.Context
import com.goodwy.commons.helpers.BaseConfig

val Context.config: BaseConfig get() = BaseConfig.newInstance(applicationContext)
