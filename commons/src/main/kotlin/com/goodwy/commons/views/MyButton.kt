package com.goodwy.commons.views

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import androidx.appcompat.widget.AppCompatButton

open class MyButton : AppCompatButton {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {
        setTextColor(textColor)
    }
}
