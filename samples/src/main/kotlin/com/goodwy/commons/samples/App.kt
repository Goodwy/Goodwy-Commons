package com.goodwy.commons.samples

import android.app.Application
import com.github.ajalt.reprint.core.Reprint
import com.goodwy.commons.helpers.rustore.RuStoreModule

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Reprint.initialize(this)
        RuStoreModule.install(this, "309929407")
    }
}
