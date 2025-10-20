package com.goodwy.commons.extensions

import android.graphics.Point
import kotlin.math.roundToInt

fun Point.formatAsResolution() = "${getMPx()}  •  $x × $y"

fun Point.getMPx(): String {
    val px = x * y / 1000000f
    val rounded = (px * 10).roundToInt() / 10f
    return "$rounded MP"
}
