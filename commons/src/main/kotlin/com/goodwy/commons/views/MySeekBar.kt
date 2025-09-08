package com.goodwy.commons.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSeekBar
import com.goodwy.commons.extensions.applyColorFilter

open class MySeekBar : AppCompatSeekBar {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int = Color.WHITE) {
        progressDrawable?.applyColorFilter(accentColor)
        background?.applyColorFilter(textColor)
        thumb?.applyColorFilter(backgroundColor)
    }
}
