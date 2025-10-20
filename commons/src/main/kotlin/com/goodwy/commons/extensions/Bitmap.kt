package com.goodwy.commons.extensions

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import kotlin.math.min

fun Bitmap.getByteArray(): ByteArray {
    var baos: ByteArrayOutputStream? = null
    try {
        baos = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, baos)
        return baos.toByteArray()
    } finally {
        baos?.close()
    }
}

fun Bitmap.cropCenter(
    newWidth:Int = min(width,height),
    newHeight:Int = min(width,height)
):Bitmap? {
    // calculate x and y offset
    val xOffset = (width - newWidth)/2
    val yOffset = (height-newHeight)/2

    return try {
        Bitmap.createBitmap(
            this, // source bitmap
            xOffset, // x coordinate of the first pixel in source
            yOffset, // y coordinate of the first pixel in source
            newWidth, // new width
            newHeight // new height
        )

    } catch (_:IllegalArgumentException){
        null
    }
}
