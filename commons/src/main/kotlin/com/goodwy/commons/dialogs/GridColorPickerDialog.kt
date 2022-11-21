package com.goodwy.commons.dialogs

import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.google.android.material.appbar.MaterialToolbar
import com.goodwy.commons.R
import com.goodwy.commons.activities.BaseSimpleActivity
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.NavigationIcon
import com.goodwy.commons.interfaces.LineColorPickerListener
import kotlinx.android.synthetic.main.dialog_color_picker.view.*
import kotlinx.android.synthetic.main.dialog_grid_color_picker.view.*

// Material colors
/*class GridColorPickerDialog(val activity: BaseSimpleActivity, val color: Int, val isPrimaryColorPicker: Boolean, val primaryColors: Int = R.array.line_bw,
                            val primaryColors50: Int = R.array.md_line_50, val primaryColors100: Int = R.array.md_line_100, val primaryColors200: Int = R.array.md_line_200,
                            val primaryColors300: Int = R.array.md_line_300, val primaryColors400: Int = R.array.md_line_400, val primaryColors500: Int = R.array.md_line_500,
                            val primaryColors600: Int = R.array.md_line_600, val primaryColors700: Int = R.array.md_line_700, val primaryColors800: Int = R.array.md_line_800,
                            val primaryColors900: Int = R.array.md_line_900,
                            val appIconIDs: ArrayList<Int>? = null, val menu: Menu? = null, val callback: (wasPositivePressed: Boolean, color: Int) -> Unit) {*/
// iOS colors
class GridColorPickerDialog(val activity: BaseSimpleActivity, val color: Int, val isPrimaryColorPicker: Boolean, val primaryColors: Int = R.array.line_00,
                            val primaryColors50: Int = R.array.line_10, val primaryColors100: Int = R.array.line_20, val primaryColors200: Int = R.array.line_30,
                            val primaryColors300: Int = R.array.line_40, val primaryColors400: Int = R.array.line_50, val primaryColors500: Int = R.array.line_60,
                            val primaryColors600: Int = R.array.line_70, val primaryColors700: Int = R.array.line_80, val primaryColors800: Int = R.array.line_90,
                            val appIconIDs: ArrayList<Int>? = null, val toolbar: MaterialToolbar? = null, val title: String = activity.resources.getString(R.string.color_title), val removeDimmedBackground: Boolean = false,
                            showUseDefaultButton: Boolean = false, val callback: (wasPositivePressed: Boolean, color: Int) -> Unit) {

    private val PRIMARY_COLORS_COUNT = 12
    private val DEFAULT_PRIMARY_COLOR_INDEX = 5 //TODO DEFAULT PRIMARY COLOR CURSOR
    private val DEFAULT_SECONDARY_COLOR_INDEX = 5
    private val DEFAULT_COLOR_VALUE = activity.resources.getColor(R.color.color_primary)
    private val backgroundColor = activity.baseConfig.backgroundColor
    private var LINE_COLOR_INDEX = 2

    private var wasDimmedBackgroundRemoved = false
    private var dialog: AlertDialog? = null
    private var view: View = activity.layoutInflater.inflate(R.layout.dialog_grid_color_picker, null)

    init {
        view.apply {
            hex_code.text = color.toHex().substring(1)
            hex_code.setOnClickListener {
                activity.copyToClipboard(hex_code.value)
                true
            }

            grid_color_title.text = title
            grid_color_cancel.applyColorFilter(activity.baseConfig.textColor)
            grid_color_cancel.setOnClickListener { dialog?.dismiss() }

            line_color_picker_icon.beGoneIf(isPrimaryColorPicker)
            val indexes = getColorIndexes(color)

            val primaryColorIndex = indexes.first
            primaryColorChanged(primaryColorIndex)
            primary_line_color_picker.updateColors(getColors(primaryColors), primaryColorIndex)
            primary_line_color_picker.listener = object : LineColorPickerListener {
                override fun colorChanged(index: Int, color: Int) {
                    val secondaryColors = getColorsForIndex(index)
                    secondary_line_color_picker.updateColors(secondaryColors)

                    val newColor = if (!isPrimaryColorPicker) secondary_line_color_picker.getCurrentColor() else color
                    colorUpdated(newColor)
                    LINE_COLOR_INDEX = 1

                    if (!isPrimaryColorPicker) {
                        primaryColorChanged(index)
                    }
                }
            }
            val primaryColor50Index = indexes.first
            primaryColorChanged(primaryColor50Index)
            line_50_color_picker.updateColors(getColors(primaryColors50), primaryColor50Index)
            line_50_color_picker.listener = object : LineColorPickerListener {
                override fun colorChanged(index: Int, color: Int) {
                    val secondaryColors = getColorsForIndex(index)
                    secondary_line_color_picker.updateColors(secondaryColors)

                    val newColor = if (!isPrimaryColorPicker) secondary_line_color_picker.getCurrentColor() else color
                    colorUpdated(newColor)
                    LINE_COLOR_INDEX = 2

                    if (!isPrimaryColorPicker) {
                        primaryColorChanged(index)
                    }
                }
            }
            val primaryColor100Index = indexes.first
            primaryColorChanged(primaryColor100Index)
            line_100_color_picker.updateColors(getColors(primaryColors100), primaryColor100Index)
            line_100_color_picker.listener = object : LineColorPickerListener {
                override fun colorChanged(index: Int, color: Int) {
                    val secondaryColors = getColorsForIndex(index)
                    secondary_line_color_picker.updateColors(secondaryColors)

                    val newColor = if (!isPrimaryColorPicker) secondary_line_color_picker.getCurrentColor() else color
                    colorUpdated(newColor)
                    LINE_COLOR_INDEX = 3

                    if (!isPrimaryColorPicker) {
                        primaryColorChanged(index)
                    }
                }
            }
            val primaryColor200Index = indexes.first
            primaryColorChanged(primaryColor200Index)
            line_200_color_picker.updateColors(getColors(primaryColors200), primaryColor200Index)
            line_200_color_picker.listener = object : LineColorPickerListener {
                override fun colorChanged(index: Int, color: Int) {
                    val secondaryColors = getColorsForIndex(index)
                    secondary_line_color_picker.updateColors(secondaryColors)

                    val newColor = if (!isPrimaryColorPicker) secondary_line_color_picker.getCurrentColor() else color
                    colorUpdated(newColor)
                    LINE_COLOR_INDEX = 4

                    if (!isPrimaryColorPicker) {
                        primaryColorChanged(index)
                    }
                }
            }
            val primaryColor300Index = indexes.first
            primaryColorChanged(primaryColor300Index)
            line_300_color_picker.updateColors(getColors(primaryColors300), primaryColor300Index)
            line_300_color_picker.listener = object : LineColorPickerListener {
                override fun colorChanged(index: Int, color: Int) {
                    val secondaryColors = getColorsForIndex(index)
                    secondary_line_color_picker.updateColors(secondaryColors)

                    val newColor = if (!isPrimaryColorPicker) secondary_line_color_picker.getCurrentColor() else color
                    colorUpdated(newColor)
                    LINE_COLOR_INDEX = 5

                    if (!isPrimaryColorPicker) {
                        primaryColorChanged(index)
                    }
                }
            }
            val primaryColor400Index = indexes.first
            primaryColorChanged(primaryColor400Index)
            line_400_color_picker.updateColors(getColors(primaryColors400), primaryColor400Index)
            line_400_color_picker.listener = object : LineColorPickerListener {
                override fun colorChanged(index: Int, color: Int) {
                    val secondaryColors = getColorsForIndex(index)
                    secondary_line_color_picker.updateColors(secondaryColors)

                    val newColor = if (!isPrimaryColorPicker) secondary_line_color_picker.getCurrentColor() else color
                    colorUpdated(newColor)
                    LINE_COLOR_INDEX = 6

                    if (!isPrimaryColorPicker) {
                        primaryColorChanged(index)
                    }
                }
            }
            val primaryColor500Index = indexes.first
            primaryColorChanged(primaryColor500Index)
            line_500_color_picker.updateColors(getColors(primaryColors500), primaryColor500Index)
            line_500_color_picker.listener = object : LineColorPickerListener {
                override fun colorChanged(index: Int, color: Int) {
                    val secondaryColors = getColorsForIndex(index)
                    secondary_line_color_picker.updateColors(secondaryColors)

                    val newColor = if (!isPrimaryColorPicker) secondary_line_color_picker.getCurrentColor() else color
                    colorUpdated(newColor)
                    LINE_COLOR_INDEX = 7

                    if (!isPrimaryColorPicker) {
                        primaryColorChanged(index)
                    }
                }
            }
            val primaryColor600Index = indexes.first
            primaryColorChanged(primaryColor600Index)
            line_600_color_picker.updateColors(getColors(primaryColors600), primaryColor600Index)
            line_600_color_picker.listener = object : LineColorPickerListener {
                override fun colorChanged(index: Int, color: Int) {
                    val secondaryColors = getColorsForIndex(index)
                    secondary_line_color_picker.updateColors(secondaryColors)

                    val newColor = if (!isPrimaryColorPicker) secondary_line_color_picker.getCurrentColor() else color
                    colorUpdated(newColor)
                    LINE_COLOR_INDEX = 8

                    if (!isPrimaryColorPicker) {
                        primaryColorChanged(index)
                    }
                }
            }
            val primaryColor700Index = indexes.first
            primaryColorChanged(primaryColor700Index)
            line_700_color_picker.updateColors(getColors(primaryColors700), primaryColor700Index)
            line_700_color_picker.listener = object : LineColorPickerListener {
                override fun colorChanged(index: Int, color: Int) {
                    val secondaryColors = getColorsForIndex(index)
                    secondary_line_color_picker.updateColors(secondaryColors)

                    val newColor = if (!isPrimaryColorPicker) secondary_line_color_picker.getCurrentColor() else color
                    colorUpdated(newColor)
                    LINE_COLOR_INDEX = 9

                    if (!isPrimaryColorPicker) {
                        primaryColorChanged(index)
                    }
                }
            }
            val primaryColor800Index = indexes.first
            primaryColorChanged(primaryColor800Index)
            line_800_color_picker.updateColors(getColors(primaryColors800), primaryColor800Index)
            line_800_color_picker.listener = object : LineColorPickerListener {
                override fun colorChanged(index: Int, color: Int) {
                    val secondaryColors = getColorsForIndex(index)
                    secondary_line_color_picker.updateColors(secondaryColors)

                    val newColor = if (!isPrimaryColorPicker) secondary_line_color_picker.getCurrentColor() else color
                    colorUpdated(newColor)
                    LINE_COLOR_INDEX = 10

                    if (!isPrimaryColorPicker) {
                        primaryColorChanged(index)
                    }
                }
            }

            secondary_line_color_picker.beVisibleIf(false) //secondary_line_color_picker.beVisibleIf(isPrimaryColorPicker)
            secondary_line_color_picker.updateColors(getColorsForIndex(primaryColorIndex), indexes.second)
            secondary_line_color_picker.listener = object : LineColorPickerListener {
                override fun colorChanged(index: Int, color: Int) {
                    colorUpdated(color)
                }
            }
            grid_color_new_color.setFillWithStrokeRigth(color, backgroundColor)
            grid_color_old_color.setCardBackgroundColor(color)
        }

        val builder = activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { dialog, which -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel) { dialog, which -> dialogDismissed() }
            .setOnCancelListener { dialogDismissed() }
            .apply {
                if (showUseDefaultButton) {
                    setNeutralButton(R.string.use_default) { dialog, which -> useDefault() }
                }
                activity.setupDialogStuff(view, this) { alertDialog ->
                    dialog = alertDialog
                }
            }

        /*dialog = AlertDialog.Builder(activity, R.style.MyDialog)
            .setPositiveButton(R.string.ok) { dialog, which -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel) { dialog, which -> dialogDismissed() }
            .setOnCancelListener { dialogDismissed() }
            .create().apply {
                activity.setupDialogStuff(view, this)
            }*/
    }

    fun getSpecificColor() = view.secondary_line_color_picker.getCurrentColor()

    private fun colorUpdated(color: Int) {
        view.hex_code.text = color.toHex().substring(1)
        view.grid_color_new_color.setFillWithStrokeRigth(color, backgroundColor)
        if (isPrimaryColorPicker) {
           // activity.updateActionbarColor(color)
            activity.setTheme(activity.getThemeId(color))

            if (toolbar != null) {
                //activity.setupToolbar(toolbar, NavigationIcon.Cross, color)
            }

            if (removeDimmedBackground && !wasDimmedBackgroundRemoved) {
                dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                wasDimmedBackgroundRemoved = true
            }
        }
    }

    private fun getColorIndexes(color: Int): Pair<Int, Int> {
        if (color == DEFAULT_COLOR_VALUE) {
            return getDefaultColorPair()
        }

        for (i in 0 until PRIMARY_COLORS_COUNT) {
            getColorsForIndex(i).indexOfFirst { color == it }.apply {
                if (this != -1) {
                    return Pair(i, this)
                }
            }
        }

        return getDefaultColorPair()
    }

    private fun primaryColorChanged(index: Int) {
        view.line_color_picker_icon.setImageResource(appIconIDs?.getOrNull(index) ?: 0)
    }

    private fun getDefaultColorPair() = Pair(DEFAULT_PRIMARY_COLOR_INDEX, DEFAULT_SECONDARY_COLOR_INDEX)

    private fun dialogDismissed() {
        callback(false, 0)
    }

   /* private fun dialogConfirmed() {
        val targetView = if (isPrimaryColorPicker) view.secondary_line_color_picker else view.primary_line_color_picker
        val color = targetView.getCurrentColor()
        callback(true, color)
    }*/

    private fun dialogConfirmed() {
        if (LINE_COLOR_INDEX == 1) {
            val targetView = view.primary_line_color_picker
            val color = targetView.getCurrentColor()
            callback(true, color)
        } else if  (LINE_COLOR_INDEX == 2) {
            val targetView = view.line_50_color_picker
            val color = targetView.getCurrentColor()
            callback(true, color)
        } else if  (LINE_COLOR_INDEX == 3) {
            val targetView = view.line_100_color_picker
            val color = targetView.getCurrentColor()
            callback(true, color)
        } else if  (LINE_COLOR_INDEX == 4) {
            val targetView = view.line_200_color_picker
            val color = targetView.getCurrentColor()
            callback(true, color)
        } else if  (LINE_COLOR_INDEX == 5) {
            val targetView = view.line_300_color_picker
            val color = targetView.getCurrentColor()
            callback(true, color)
        } else if  (LINE_COLOR_INDEX == 6) {
            val targetView = view.line_400_color_picker
            val color = targetView.getCurrentColor()
            callback(true, color)
        } else if  (LINE_COLOR_INDEX == 7) {
            val targetView = view.line_500_color_picker
            val color = targetView.getCurrentColor()
            callback(true, color)
        } else if  (LINE_COLOR_INDEX == 8) {
            val targetView = view.line_600_color_picker
            val color = targetView.getCurrentColor()
            callback(true, color)
        } else if  (LINE_COLOR_INDEX == 9) {
            val targetView = view.line_700_color_picker
            val color = targetView.getCurrentColor()
            callback(true, color)
        } else if  (LINE_COLOR_INDEX == 10) {
            val targetView = view.line_800_color_picker
            val color = targetView.getCurrentColor()
            callback(true, color)
        }
    }

    private fun getColorsForIndex(index: Int) = when (index) {
        0 -> getColors(R.array.md_reds)
        1 -> getColors(R.array.md_pinks)
        2 -> getColors(R.array.md_purples)
        3 -> getColors(R.array.md_deep_purples)
        4 -> getColors(R.array.md_indigos)
        5 -> getColors(R.array.md_blues)
        6 -> getColors(R.array.md_light_blues)
        7 -> getColors(R.array.md_cyans)
        8 -> getColors(R.array.md_teals)
        9 -> getColors(R.array.md_greens)
        10 -> getColors(R.array.md_light_greens)
        11 -> getColors(R.array.md_limes)
        12 -> getColors(R.array.md_yellows)
        13 -> getColors(R.array.md_ambers)
        14 -> getColors(R.array.md_oranges)
        15 -> getColors(R.array.md_deep_oranges)
        16 -> getColors(R.array.md_browns)
        17 -> getColors(R.array.md_greys)
        18 -> getColors(R.array.md_blue_greys)
        else -> throw RuntimeException("Invalid color id $index")
    }

    private fun getColors50ForIndex(index: Int) = when (index) {
        0 -> getColors(R.array.md_line_50)
        1 -> getColors(R.array.md_pinks)
        2 -> getColors(R.array.md_purples)
        3 -> getColors(R.array.md_deep_purples)
        4 -> getColors(R.array.md_indigos)
        5 -> getColors(R.array.md_blues)
        6 -> getColors(R.array.md_light_blues)
        7 -> getColors(R.array.md_cyans)
        8 -> getColors(R.array.md_teals)
        9 -> getColors(R.array.md_greens)
        10 -> getColors(R.array.md_light_greens)
        11 -> getColors(R.array.md_limes)
        12 -> getColors(R.array.md_yellows)
        13 -> getColors(R.array.md_ambers)
        14 -> getColors(R.array.md_oranges)
        15 -> getColors(R.array.md_deep_oranges)
        16 -> getColors(R.array.md_browns)
        17 -> getColors(R.array.md_greys)
        18 -> getColors(R.array.md_blue_greys)
        else -> throw RuntimeException("Invalid color id $index")
    }

    private fun getColors(id: Int) = activity.resources.getIntArray(id).toCollection(ArrayList())

    private fun useDefault() {
        callback(true, activity.resources.getColor(R.color.color_primary))
    }
}
