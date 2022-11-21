package com.goodwy.commons.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.appcompat.widget.SwitchCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.goodwy.commons.R
import com.goodwy.commons.extensions.adjustAlpha
import com.google.android.material.materialswitch.MaterialSwitch

class MySwitchCompat : MaterialSwitch {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {
        setTextColor(textColor)
        val states = arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked))
        val thumbCheckedColors = if (accentColor == resources.getColor(R.color.white)) backgroundColor else resources.getColor(R.color.white)
        val thumbColors = intArrayOf(resources.getColor(R.color.thumb_deactivated), thumbCheckedColors)
        val trackColors = intArrayOf(resources.getColor(R.color.track_deactivated), accentColor) //accentColor.adjustAlpha(0.3f)
        //DrawableCompat.setTintList(DrawableCompat.wrap(thumbDrawable!!), ColorStateList(states, thumbColors))
        //DrawableCompat.setTintList(DrawableCompat.wrap(trackDrawable!!), ColorStateList(states, trackColors))
        thumbTintList = ColorStateList(states, thumbColors)
        trackTintList = ColorStateList(states, trackColors)
    }
}
