package com.goodwy.commons.helpers

import android.app.Activity
import android.graphics.Color
import android.view.View
import androidx.core.view.ViewCompat

object InsetUtil {
// Edge-to-edge
    fun removeSystemInsets(view: View, listener: OnSystemInsetsChangedListener, statusBar: Boolean = false) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->

            /*val desiredBottomInset = calculateDesiredBottomInset(
                view,
                insets.systemWindowInsetTop,
                insets.systemWindowInsetBottom,
                listener
            )*/

            val top = if (statusBar) 0 else insets.systemWindowInsetTop
            listener.invoke(
                top,
                insets.systemWindowInsetBottom,
                insets.systemWindowInsetLeft,
                insets.systemWindowInsetRight
            )
            //Remove system indents
            ViewCompat.onApplyWindowInsets(view, insets.replaceSystemWindowInsets(0, top, 0, 0))
        }
    }

    fun calculateDesiredBottomInset(
        view: View,
        topInset: Int,
        bottomInset: Int,
        listener: OnSystemInsetsChangedListener,
        statusBar: Boolean = false
    ): Int {
        val hasKeyboard = view.isKeyboardAppeared(bottomInset)
        val desiredBottomInset = if (hasKeyboard) bottomInset else 0
        listener(if (statusBar) topInset else 0, if (hasKeyboard) 0 else bottomInset, 0, 0)
        return desiredBottomInset
    }
}

private fun View.isKeyboardAppeared(bottomInset: Int) =
    bottomInset / resources.displayMetrics.heightPixels.toDouble() > .25

typealias OnSystemBarsSizeChangedListener =
        (statusBarSize: Int, navigationBarSize: Int) -> Unit

typealias OnSystemInsetsChangedListener = (
    statusBarSize: Int,
    bottomNavigationBarSize: Int,
    leftNavigationBarSize: Int,
    rightNavigationBarSize: Int
) -> Unit

fun Activity.setWindowTransparency(
    statusBar: Boolean = false,
    listener: OnSystemInsetsChangedListener = { _, _, _, _ -> }
) {
    InsetUtil.removeSystemInsets(window.decorView, listener, statusBar)
    window.navigationBarColor = Color.TRANSPARENT
    if (!statusBar) window.statusBarColor = Color.TRANSPARENT
}
