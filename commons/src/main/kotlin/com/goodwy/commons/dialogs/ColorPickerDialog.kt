package com.goodwy.commons.dialogs

import android.app.Activity
import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.goodwy.commons.R
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.isQPlus
import com.goodwy.commons.views.ColorPickerSquare
import kotlinx.android.synthetic.main.dialog_color_picker.view.*
import kotlinx.android.synthetic.main.dialog_color_picker.view.color_picker_holder
import java.util.*

private const val RECENT_COLORS_NUMBER = 5

// forked from https://github.com/yukuku/ambilwarna
class ColorPickerDialog(
    val activity: Activity,
    color: Int,
    val removeDimmedBackground: Boolean = false,
    showUseDefaultButton: Boolean = false,
    colorDefault: Int = -1,
    val currentColorCallback: ((color: Int) -> Unit)? = null,
    val title: String = activity.resources.getString(R.string.color_title),
    val callback: (wasPositivePressed: Boolean, color: Int) -> Unit
) {
    var viewHue: View
    var viewSatVal: ColorPickerSquare
    var viewCursor: ImageView
    var viewNewColor: ImageView
    var viewTarget: ImageView
    var newHexField: EditText
    var viewContainer: ViewGroup
    private val baseConfig = activity.baseConfig
    private val currentColorHsv = FloatArray(3)
    private val backgroundColor = baseConfig.backgroundColor
    //private val cornerRadius = activity.getCornerRadius()
    private var isHueBeingDragged = false
    private var wasDimmedBackgroundRemoved = false
    private var dialog: AlertDialog? = null

    init {
        Color.colorToHSV(color, currentColorHsv)

        /*var recentColors = baseConfig.colorPickerRecentColors
        if (recentColors.size == 0) {
            recentColors.add(-1356483)
            recentColors.add(-537018)
            recentColors.add(-10173338)
            recentColors.add(-14131212)
            recentColors.add(-16777216)
            baseConfig.colorPickerRecentColors = recentColors
        }*/

        val view = activity.layoutInflater.inflate(R.layout.dialog_color_picker, null).apply {
            if (isQPlus()) {
                isForceDarkAllowed = false
            }

            color_picker_title.text = title
            color_picker_cancel.applyColorFilter(baseConfig.textColor)
            color_picker_cancel.setOnClickListener { dialog?.dismiss() }

            viewHue = color_picker_hue
            viewSatVal = color_picker_square
            viewCursor = color_picker_hue_cursor

            viewNewColor = color_picker_new_color
            viewTarget = color_picker_cursor
            viewContainer = color_picker_holder
            newHexField = color_picker_new_hex

            viewSatVal.setHue(getHue())

            viewTarget.setFillWithStroke(getColor(), backgroundColor) //, cornerRadius)
            viewCursor.setFillWithStroke(getColor(), backgroundColor) //, cornerRadius)
            //viewNewColor.setFillWithStroke(getColor(), backgroundColor, cornerRadius)
            //color_picker_old_color.setFillWithStroke(color, backgroundColor, cornerRadius)
            viewNewColor.setFillWithStrokeRigth(color, backgroundColor)
            color_picker_old_color.setCardBackgroundColor(color)

            val hexCode = getHexCode(color)
            color_picker_old_hex.text = "#$hexCode"
            color_picker_old_hex.setOnLongClickListener {
                activity.copyToClipboard(hexCode)
                true
            }
            newHexField.setText(hexCode)
            setupRecentColors()
        }

        viewHue.setOnTouchListener(OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                isHueBeingDragged = true
            }

            if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_UP) {
                var y = event.y
                if (y < 0f)
                    y = 0f

                if (y > viewHue.measuredHeight) {
                    y = viewHue.measuredHeight - 0.001f // to avoid jumping the cursor from bottom to top.
                }
                var hue = 360f - 360f / viewHue.measuredHeight * y
                if (hue == 360f)
                    hue = 0f

                currentColorHsv[0] = hue
                updateHue()
                newHexField.setText(getHexCode(getColor()))

                if (event.action == MotionEvent.ACTION_UP) {
                    isHueBeingDragged = false
                }
                viewTarget.setFillWithStroke(getColor(), backgroundColor)//, cornerRadius)
                viewCursor.setFillWithStroke(getColor(), backgroundColor)//, cornerRadius)
                return@OnTouchListener true
            }
            false
        })

        viewSatVal.setOnTouchListener(OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_UP) {
                var x = event.x
                var y = event.y

                if (x < 0f)
                    x = 0f
                if (x > viewSatVal.measuredWidth)
                    x = viewSatVal.measuredWidth.toFloat()
                if (y < 0f)
                    y = 0f
                if (y > viewSatVal.measuredHeight)
                    y = viewSatVal.measuredHeight.toFloat()

                currentColorHsv[1] = 1f / viewSatVal.measuredWidth * x
                currentColorHsv[2] = 1f - 1f / viewSatVal.measuredHeight * y

                moveColorPicker()
                viewNewColor.setFillWithStrokeRigth(getColor(), backgroundColor) //setFillWithStroke(getColor(), backgroundColor, cornerRadius)
                viewTarget.setFillWithStroke(getColor(), backgroundColor)//, cornerRadius)
                viewCursor.setFillWithStroke(getColor(), backgroundColor)//, cornerRadius)
                newHexField.setText(getHexCode(getColor()))
                return@OnTouchListener true
            }
            false
        })

        newHexField.onTextChangeListener {
            if (it.length == 6 && !isHueBeingDragged) {
                try {
                    val newColor = Color.parseColor("#$it")
                    Color.colorToHSV(newColor, currentColorHsv)
                    updateHue()
                    moveColorPicker()
                } catch (ignored: Exception) {
                }
            }
        }

        val textColor = activity.getProperTextColor()
        val builder = activity.getAlertDialogBuilder() //val builder = AlertDialog.Builder(activity, R.style.MyDialog)
                .setPositiveButton(R.string.ok) { dialog, which -> confirmNewColor() }
                .setNegativeButton(R.string.cancel) { dialog, which -> dialogDismissed() }
                .setOnCancelListener { dialogDismissed() }

        if (showUseDefaultButton) {
            builder.setNeutralButton(R.string.use_default) { dialog, which -> useDefault(colorDefault) }
        }

        builder.apply {
            activity.setupDialogStuff(view, this) { alertDialog ->
                dialog = alertDialog
                view.color_picker_hex_arrow.applyColorFilter(textColor)
                viewCursor.applyColorFilter(textColor)
            }
        }

        view.onGlobalLayout {
            moveHuePicker()
            moveColorPicker()
        }
    }

    private fun View.setupRecentColors() {
        val recentColors = baseConfig.colorPickerRecentColors
        if (recentColors.isNotEmpty()) {
            recent_colors.beVisible()
            val squareSize = context.resources.getDimensionPixelSize(R.dimen.colorpicker_hue_width)
            recentColors.take(RECENT_COLORS_NUMBER).reversed().forEach { recentColor ->
                val recentColorView = ImageView(context)
                recentColorView.id = View.generateViewId()
                recentColorView.layoutParams = ViewGroup.LayoutParams(squareSize, squareSize)
                recentColorView.setFillWithStroke(recentColor, backgroundColor)//, cornerRadius)
                recentColorView.setOnClickListener { newHexField.setText(getHexCode(recentColor)) }
                recent_colors.addView(recentColorView)
                recent_colors_flow.addView(recentColorView)
            }
        }
    }

    private fun dialogDismissed() {
        callback(false, 0)
    }

    private fun confirmNewColor() {
        val hexValue = newHexField.value
        val newColor = if (hexValue.length == 6) {
            Color.parseColor("#$hexValue")
        } else {
            getColor()
        }

        addRecentColor(newColor)
        callback(true, newColor)
    }

    private fun useDefault(colorDefault: Int) {
        val defaultColor = if (colorDefault == -1) baseConfig.primaryColor else colorDefault
        addRecentColor(defaultColor)

        callback(true, defaultColor)
    }

    private fun addRecentColor(color: Int) {
        var recentColors = baseConfig.colorPickerRecentColors

        recentColors.remove(color)
        if (recentColors.size >= RECENT_COLORS_NUMBER) {
            val numberOfColorsToDrop = recentColors.size - RECENT_COLORS_NUMBER + 1
            recentColors = LinkedList(recentColors.dropLast(numberOfColorsToDrop))
        }
        recentColors.addFirst(color)

        baseConfig.colorPickerRecentColors = recentColors
    }

    private fun getHexCode(color: Int) = color.toHex().substring(1)

    private fun updateHue() {
        viewSatVal.setHue(getHue())
        moveHuePicker()
        viewNewColor.setFillWithStrokeRigth(getColor(), backgroundColor) //setFillWithStroke(getColor(), backgroundColor, cornerRadius)
        if (removeDimmedBackground && !wasDimmedBackgroundRemoved) {
            dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            wasDimmedBackgroundRemoved = true
        }

        currentColorCallback?.invoke(getColor())
    }

    private fun moveHuePicker() {
        var y = viewHue.measuredHeight - getHue() * viewHue.measuredHeight / 360f
        if (y == viewHue.measuredHeight.toFloat())
            y = 0f

        viewCursor.x = (viewHue.left - ((viewCursor.width - viewHue.width) / 2)).toFloat() //(viewHue.left - viewCursor.width).toFloat()
        viewCursor.y = viewHue.top + y - viewCursor.height / 2
    }

    private fun moveColorPicker() {
        val x = getSat() * viewSatVal.measuredWidth
        val y = (1f - getVal()) * viewSatVal.measuredHeight
        viewTarget.x = viewSatVal.left + x - viewTarget.width / 2
        viewTarget.y = viewSatVal.top + y - viewTarget.height / 2
    }

    private fun getColor() = Color.HSVToColor(currentColorHsv)
    private fun getHue() = currentColorHsv[0]
    private fun getSat() = currentColorHsv[1]
    private fun getVal() = currentColorHsv[2]
}
