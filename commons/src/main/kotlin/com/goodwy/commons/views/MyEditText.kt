package com.goodwy.commons.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.goodwy.commons.R
import com.goodwy.commons.extensions.adjustAlpha
import com.goodwy.commons.extensions.baseConfig
import com.goodwy.commons.extensions.getColoredDrawableWithColor
import com.goodwy.commons.helpers.MEDIUM_ALPHA
import com.goodwy.commons.helpers.isQPlus
import java.lang.reflect.Field


open class MyEditText : AppCompatEditText {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    @SuppressLint("DiscouragedPrivateApi")
    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {
        // TODO HIDE
       // background?.mutate()?.applyColorFilter(accentColor)

        // requires android:textCursorDrawable="@null" in xml to color the cursor too
        setTextColor(textColor)
        setHintTextColor(textColor.adjustAlpha(MEDIUM_ALPHA))
        setLinkTextColor(accentColor)
        if (backgroundColor != -2) {
            if (isQPlus()) {
                textCursorDrawable = resources.getColoredDrawableWithColor(context, R.drawable.cursor_text_vertical, backgroundColor)
                if (!context.baseConfig.isMiui) {
                    setTextSelectHandle(resources.getColoredDrawableWithColor(R.drawable.ic_drop_vector, backgroundColor))
                    setTextSelectHandleLeft(resources.getColoredDrawableWithColor(R.drawable.ic_drop_left_vector, backgroundColor))
                    setTextSelectHandleRight(resources.getColoredDrawableWithColor(R.drawable.ic_drop_right_vector, backgroundColor))
                }
            } else {
                try {
                    val fEditor: Field = TextView::class.java.getDeclaredField("mEditor")
                    fEditor.isAccessible = true
                    val editor: Any = fEditor.get(this) as Any
                    if (!context.baseConfig.isMiui) {
                        val fSelectHandleLeft: Field = editor.javaClass.getDeclaredField("mSelectHandleLeft")
                        val fSelectHandleRight: Field = editor.javaClass.getDeclaredField("mSelectHandleRight")
                        val fSelectHandleCenter: Field = editor.javaClass.getDeclaredField("mSelectHandleCenter")
                        fSelectHandleLeft.isAccessible = true
                        fSelectHandleRight.isAccessible = true
                        fSelectHandleCenter.isAccessible = true
                        fSelectHandleLeft.set(editor, resources.getColoredDrawableWithColor(context, R.drawable.ic_drop_left_vector, backgroundColor))
                        fSelectHandleRight.set(editor, resources.getColoredDrawableWithColor(context, R.drawable.ic_drop_right_vector, backgroundColor))
                        fSelectHandleCenter.set(editor, resources.getColoredDrawableWithColor(context, R.drawable.ic_drop_vector, backgroundColor))
                    }

                    // Get the cursor resource id
                    var field = TextView::class.java.getDeclaredField("mCursorDrawableRes")
                    field.isAccessible = true
                    val drawableResId = field.getInt(this)

                    // Get the editor
                    field = TextView::class.java.getDeclaredField("mEditor")
                    field.isAccessible = true
                    //val editor = field[this]

                    // Get the drawable and set a color filter
                    val drawable = ContextCompat.getDrawable(context, drawableResId)
                    DrawableCompat.setTint(drawable!!, backgroundColor)

                    // Set the drawables
                    val drawables = arrayOf<Drawable?>(drawable, drawable)
                    field = editor.javaClass.getDeclaredField("mCursorDrawable")
                    field.isAccessible = true
                    field[editor] = drawables
                } catch (ignored: Exception) {
                }
            }
        }
    }
}
