package com.goodwy.commons.helpers

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale

// language forcing used at "Use english language", taken from https://stackoverflow.com/a/40704077/1967672
class MyContextWrapper(context: Context) : ContextWrapper(context) {

    fun wrap(context: Context, language: String): ContextWrapper {
        var newContext = context
        val config = newContext.resources.configuration

        val sysLocale = getSystemLocale(config)

        if (language != "" && sysLocale!!.language != language) {
            val locale = Locale(language)
            Locale.setDefault(locale)
            setSystemLocale(config, locale)
        }

        newContext = newContext.createConfigurationContext(config)
        return MyContextWrapper(newContext)
    }

    private fun getSystemLocale(config: Configuration) = config.locales.get(0)

    private fun setSystemLocale(config: Configuration, locale: Locale) {
        config.setLocale(locale)
    }
}
