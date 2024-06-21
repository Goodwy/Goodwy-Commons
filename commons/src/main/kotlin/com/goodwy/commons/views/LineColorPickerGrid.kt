package com.goodwy.commons.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.LinearLayout
import com.goodwy.commons.R
import com.goodwy.commons.extensions.isRTLLayout
import com.goodwy.commons.extensions.onGlobalLayout
import com.goodwy.commons.interfaces.LineColorPickerListener
import java.util.*


class LineColorPickerGrid @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {
    private var colorsCount = 0
    private var pickerWidth = 0
    private var stripeWidth = 0
    private var unselectedMargin = 0
    private var lastColorIndex = -1
    private var wasInit = false
    private var colors = ArrayList<Int>()

    var listener: LineColorPickerListener? = null

    init {
        unselectedMargin = context.resources.getDimension(R.dimen.grid_color_picker_margin).toInt()
        onGlobalLayout {
            if (pickerWidth == 0) {
                pickerWidth = width

                if (colorsCount != 0)
                    stripeWidth = width / colorsCount
            }

            if (!wasInit) {
                wasInit = true
                initColorPicker()
                updateItemMargin(lastColorIndex, false)
            }
        }
        orientation = HORIZONTAL

        setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                    if (pickerWidth != 0 && stripeWidth != 0) {
                        touchAt(motionEvent.x.toInt())
                    }
                }
            }
            true
        }
    }

    fun updateColors(colors: ArrayList<Int>, selectColorIndex: Int = -1) {
        this.colors = colors
        colorsCount = colors.size
        if (pickerWidth != 0) {
            stripeWidth = pickerWidth / colorsCount
        }

        if (selectColorIndex != -1) {
            lastColorIndex = selectColorIndex
        }

        initColorPicker()
        updateItemMargin(lastColorIndex, false)
    }

    // do not remove ": Int", it causes "NoSuchMethodError" for some reason
    fun getCurrentColor(): Int = colors[lastColorIndex]

    private fun initColorPicker() {
        removeAllViews()
        val inflater = LayoutInflater.from(context)
        colors.forEach {
            inflater.inflate(R.layout.empty_image_view_grid, this, false).apply {
                setBackgroundColor(it)
                addView(this)
            }
        }
    }

    private fun touchAt(touchX: Int) {
        var colorIndex = touchX / stripeWidth
        if (context.isRTLLayout) {
            colorIndex = colors.size - colorIndex - 1
        }
        val index = Math.max(0, Math.min(colorIndex, colorsCount - 1))
      //  if (lastColorIndex != index) { // если включить то курсор не переводится на предыдущую позицию в другой строке
            updateItemMargin(lastColorIndex, true)
            lastColorIndex = index
            updateItemMargin(index, false)
            listener?.colorChanged(index, colors[index])
       // }
    }

    private fun updateItemMargin(index: Int, addMargin: Boolean) {
        getChildAt(index)?.apply {
//            (layoutParams as LayoutParams).apply {
//                topMargin = if (addMargin) 0 else unselectedMargin
//                bottomMargin = if (addMargin) 0 else unselectedMargin
//                if (addMargin) setPadding(unselectedMargin,unselectedMargin,unselectedMargin,unselectedMargin) else setPadding(0,0,0,0)
//            }
            requestLayout()
        }
    }
}
