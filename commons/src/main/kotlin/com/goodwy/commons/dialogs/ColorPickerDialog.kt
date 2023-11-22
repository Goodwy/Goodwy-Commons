package com.goodwy.commons.dialogs

import android.annotation.SuppressLint
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
import com.goodwy.commons.databinding.DialogColorPickerBinding
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.isQPlus
import com.goodwy.commons.views.ColorPickerSquare
import java.util.LinkedList

private const val RECENT_COLORS_NUMBER = 5

// forked from https://github.com/yukuku/ambilwarna
@SuppressLint("ClickableViewAccessibility")
class ColorPickerDialog(
    val activity: Activity,
    color: Int,
    val removeDimmedBackground: Boolean = false,
    val addDefaultColorButton: Boolean = false,
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

        val view = DialogColorPickerBinding.inflate(activity.layoutInflater, null, false).apply {
            if (isQPlus()) {
                root.isForceDarkAllowed = false
            }

            colorPickerTitle.text = title
            colorPickerCancel.applyColorFilter(baseConfig.textColor)
            colorPickerCancel.setOnClickListener { dialog?.dismiss() }

            viewHue = colorPickerHue
            viewSatVal = colorPickerSquare
            viewCursor = colorPickerHueCursor

            viewNewColor = colorPickerNewColor
            viewTarget = colorPickerCursor
            viewContainer = colorPickerHolder
            newHexField = colorPickerNewHex

            viewSatVal.setHue(getHue())

            viewTarget.setFillWithStroke(getColor(), backgroundColor) //, cornerRadius)
            viewCursor.setFillWithStroke(getColor(), backgroundColor) //, cornerRadius)
            //viewNewColor.setFillWithStroke(getColor(), backgroundColor, cornerRadius)
            //colorPickerOldColor.setFillWithStroke(color, backgroundColor, cornerRadius)
            viewNewColor.setFillWithStrokeRight(color, backgroundColor)
            colorPickerOldColor.setCardBackgroundColor(color)

            val hexCode = getHexCode(color)
            colorPickerOldHex.text = "#$hexCode"
            colorPickerOldHex.setOnLongClickListener {
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
                viewNewColor.setFillWithStrokeRight(getColor(), backgroundColor) //setFillWithStroke(getColor(), backgroundColor, cornerRadius)
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
            .setPositiveButton(R.string.ok) { _, _ -> confirmNewColor() }
            .setNegativeButton(R.string.cancel) { _, _ -> dialogDismissed() }
            .setOnCancelListener { dialogDismissed() }
            .apply {
                if (addDefaultColorButton) {
                    setNeutralButton(R.string.default_color) { _, _ -> confirmDefaultColor(colorDefault) }
                }
            }

        builder.apply {
            activity.setupDialogStuff(view.root, this) { alertDialog ->
                dialog = alertDialog
                view.colorPickerHexArrow.applyColorFilter(textColor)
                viewCursor.applyColorFilter(textColor)
            }
        }

        view.root.onGlobalLayout {
            moveHuePicker()
            moveColorPicker()
        }
    }

    private fun DialogColorPickerBinding.setupRecentColors() {
        val colorPickerRecentColors = baseConfig.colorPickerRecentColors
        if (colorPickerRecentColors.isNotEmpty()) {
            recentColors.beVisible()
            val squareSize = root.context.resources.getDimensionPixelSize(R.dimen.colorpicker_hue_width)
            colorPickerRecentColors.take(RECENT_COLORS_NUMBER).forEach { recentColor ->
                val recentColorView = ImageView(root.context)
                recentColorView.id = View.generateViewId()
                recentColorView.layoutParams = ViewGroup.LayoutParams(squareSize, squareSize)
                recentColorView.setFillWithStroke(recentColor, backgroundColor)//, cornerRadius)
                recentColorView.setOnClickListener { newHexField.setText(getHexCode(recentColor)) }
                recentColors.addView(recentColorView)
                recentColorsFlow.addView(recentColorView)
            }
        }
    }

    private fun dialogDismissed() {
        callback(false, 0)
    }

    private fun confirmDefaultColor(colorDefault: Int) {
        val defaultColor = if (colorDefault == -1) baseConfig.primaryColor else colorDefault
        addRecentColor(defaultColor)

        callback(true, defaultColor)
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
        viewNewColor.setFillWithStrokeRight(getColor(), backgroundColor) //setFillWithStroke(getColor(), backgroundColor, cornerRadius)
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
