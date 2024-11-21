package com.goodwy.commons.extensions

import android.app.Application
import android.content.Context
import com.goodwy.commons.helpers.isNougatPlus
import java.util.Locale

fun Application.checkUseEnglish() {
    if (baseConfig.useEnglish && !isNougatPlus()) {
        val conf = resources.configuration
        conf.locale = Locale.ENGLISH
        resources.updateConfiguration(conf, resources.displayMetrics)
    }
}

fun Application.isRuStoreInstalled(): Boolean {
    return isPackageInstalled("ru.vk.store")
}
