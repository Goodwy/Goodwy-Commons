package com.goodwy.commons.samples

import com.github.ajalt.reprint.core.Reprint
import com.goodwy.commons.RightApp

class App : RightApp() {
    override fun onCreate() {
        super.onCreate()
        Reprint.initialize(this)
    }
}
