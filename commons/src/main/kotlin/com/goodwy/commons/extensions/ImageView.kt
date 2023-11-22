package com.goodwy.commons.extensions

import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.widget.ImageView
import com.goodwy.commons.R
import androidx.annotation.DrawableRes

fun ImageView.setFillWithStroke(fillColor: Int, backgroundColor: Int, drawRectangle: Boolean = false) {
    GradientDrawable().apply {
        shape = if (drawRectangle) GradientDrawable.RECTANGLE else GradientDrawable.OVAL
        setColor(fillColor)
        background = this

        if (backgroundColor == fillColor || fillColor == -2 && backgroundColor == -1) {
            val strokeColor = backgroundColor.getContrastColor().adjustAlpha(0.5f)
            setStroke(2, strokeColor)
        }
    }
}

/* old
fun ImageView.setFillWithStroke(fillColor: Int, backgroundColor: Int, cornerRadiusSize: Float = 0f) {
    val strokeColor = backgroundColor.getContrastColor()
    GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(fillColor)
        setStroke(2, strokeColor)
        background = this

        if (cornerRadiusSize != 0f) {
            cornerRadius = cornerRadiusSize
        }
    }
}*/

fun ImageView.applyColorFilter(color: Int) = setColorFilter(color, PorterDuff.Mode.SRC_IN)

fun ImageView.setImageResourceOrBeGone(@DrawableRes imageRes: Int?) {
    if (imageRes != null) {
        beVisible()
        setImageResource(imageRes)
    } else {
        beGone()
    }
}

fun ImageView.setFillWithStrokeRight(fillColor: Int, backgroundColor: Int, cornerRadiusSize: Float = 0f) {
    val strokeColor = backgroundColor.getContrastColor()
    GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(fillColor)
        setStroke(2, strokeColor)
        val bgDrawable = resources.getColoredDrawableWithColor(context, R.drawable.ic_delta, fillColor)
        background = bgDrawable

        if (cornerRadiusSize != 0f) {
            cornerRadius = cornerRadiusSize
        }
    }
}
