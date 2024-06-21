package com.goodwy.commons.compose.extensions

import android.app.Activity
import android.content.Context
import com.goodwy.commons.R
import com.goodwy.commons.extensions.baseConfig
import com.goodwy.commons.extensions.redirectToRateUs
import com.goodwy.commons.extensions.toast
import com.goodwy.commons.helpers.BaseConfig

val Context.config: BaseConfig get() = BaseConfig.newInstance(applicationContext)

fun Activity.rateStarsRedirectAndThankYou(stars: Int) {
    if (stars == 5) {
        redirectToRateUs()
    }
    toast(R.string.thank_you)
    baseConfig.wasAppRated = true
}
