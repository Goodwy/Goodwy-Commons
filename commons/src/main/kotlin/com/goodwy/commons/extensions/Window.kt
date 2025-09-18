package com.goodwy.commons.extensions

import android.view.View
import android.view.Window
import com.goodwy.commons.helpers.DARK_GREY

fun Window.updateStatusBarColors(backgroundColor: Int) {
    if (statusBarColor == backgroundColor) {
        return
    }
    statusBarColor = backgroundColor
    updateStatusBarForegroundColor(backgroundColor)
}

fun Window.updateStatusBarForegroundColor(backgroundColor: Int) {
    val shouldBeLight = backgroundColor.getContrastColor() == DARK_GREY

    decorView.post {
        if (shouldBeLight) {
            decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            decorView.systemUiVisibility = decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }
}

fun Window.updateNavigationBarColors(backgroundColor: Int) {
    if (navigationBarColor == backgroundColor) {
        return
    }
    navigationBarColor = backgroundColor
    updateNavigationBarForegroundColor(backgroundColor)
}

fun Window.updateNavigationBarForegroundColor(backgroundColor: Int) {
    val shouldBeLight = backgroundColor.getContrastColor() == DARK_GREY

    decorView.post {
        if (shouldBeLight) {
            decorView.systemUiVisibility = decorView.systemUiVisibility.addBit(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
        } else {
            decorView.systemUiVisibility = decorView.systemUiVisibility.removeBit(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
        }
    }
}
