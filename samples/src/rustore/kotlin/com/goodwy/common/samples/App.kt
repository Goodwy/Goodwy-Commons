package com.goodwy.commons.samples

import com.github.ajalt.reprint.core.Reprint
import com.goodwy.commons.RightApp
import com.goodwy.commons.extensions.isRuStoreInstalled
import com.goodwy.commons.helpers.rustore.RuStoreModule

class App : RightApp() {
    override fun onCreate() {
        super.onCreate()
        Reprint.initialize(this)
        if (isRuStoreInstalled()) RuStoreModule.install(this, "309929407")
    }
}
