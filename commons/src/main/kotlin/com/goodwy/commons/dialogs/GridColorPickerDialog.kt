package com.goodwy.commons.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.goodwy.commons.R
import com.goodwy.commons.activities.BaseSimpleActivity
import com.goodwy.commons.compose.alert_dialog.AlertDialogState
import com.goodwy.commons.compose.alert_dialog.DialogSurface
import com.goodwy.commons.compose.alert_dialog.dialogTextColor
import com.goodwy.commons.compose.alert_dialog.rememberAlertDialogState
import com.goodwy.commons.compose.extensions.MyDevices
import com.goodwy.commons.compose.theme.AppThemeSurface
import com.goodwy.commons.compose.theme.SimpleTheme
import com.goodwy.commons.databinding.DialogGridColorPickerBinding
import com.goodwy.commons.extensions.*
import com.goodwy.commons.interfaces.LineColorPickerListener
import com.goodwy.commons.views.MyAppBarLayout

class GridColorPickerDialog(
    val activity: BaseSimpleActivity,
    val color: Int,
    val colorBackground: Int,
    val isPrimaryColorPicker: Boolean,
    val primaryColors: Int = R.array.line_00,
    val primaryColors50: Int = R.array.line_10,
    val primaryColors100: Int = R.array.line_20,
    val primaryColors200: Int = R.array.line_30,
    val primaryColors300: Int = R.array.line_40,
    val primaryColors400: Int = R.array.line_50,
    val primaryColors500: Int = R.array.line_60,
    val primaryColors600: Int = R.array.line_70,
    val primaryColors700: Int = R.array.line_80,
    val primaryColors800: Int = R.array.line_90,
    val appIconIDs: ArrayList<Int>? = null,
    val appBar: MyAppBarLayout? = null,
    val callback: (wasPositivePressed: Boolean, color: Int) -> Unit
) {
    companion object {
        private val PRIMARY_COLORS_COUNT = 12
        private val DEFAULT_PRIMARY_COLOR_INDEX = 5 //TODO DEFAULT PRIMARY COLOR CURSOR
        private val DEFAULT_SECONDARY_COLOR_INDEX = 5
        private var LINE_COLOR_INDEX = 2
    }

    private var wasDimmedBackgroundRemoved = false
    private var dialog: AlertDialog? = null
    private var view = DialogGridColorPickerBinding.inflate(activity.layoutInflater, null, false)
    private val defaultColorValue = activity.resources.getColor(R.color.color_primary)
    private val backgroundColor = activity.baseConfig.backgroundColor

    init {
        view.apply {
            hexCode.text = color.toHex().substring(1)
            hexCode.setOnClickListener {
                activity.copyToClipboard(hexCode.value)
                true
            }

            gridColorTitle.text = activity.resources.getString(R.string.primary_color)
            gridColorCancel.applyColorFilter(activity.baseConfig.textColor)
            gridColorCancel.setOnClickListener { dialog?.dismiss() }

            lineColorPickerIcon.beGoneIf(isPrimaryColorPicker)
            val indexes = getColorIndexes(color)

            val primaryColorIndex = indexes.first
            primaryColorChanged(primaryColorIndex)
            primaryLineColorPicker.updateColors(getColors(primaryColors), primaryColorIndex)
            primaryLineColorPicker.listener = LineColorPickerListener { index, color ->
                val secondaryColors = getColorsForIndex(index)
                secondaryLineColorPicker.updateColors(secondaryColors)

                val newColor = if (!isPrimaryColorPicker) secondaryLineColorPicker.getCurrentColor() else color
                colorUpdated(newColor)
                LINE_COLOR_INDEX = 1

                if (!isPrimaryColorPicker) {
                    primaryColorChanged(index)
                }
            }
            val primaryColor50Index = indexes.first
            primaryColorChanged(primaryColor50Index)
            line50ColorPicker.updateColors(getColors(primaryColors50), primaryColor50Index)
            line50ColorPicker.listener = LineColorPickerListener { index, color ->
                val secondaryColors = getColorsForIndex(index)
                secondaryLineColorPicker.updateColors(secondaryColors)

                val newColor = if (!isPrimaryColorPicker) secondaryLineColorPicker.getCurrentColor() else color
                colorUpdated(newColor)
                LINE_COLOR_INDEX = 2

                if (!isPrimaryColorPicker) {
                    primaryColorChanged(index)
                }
            }
            val primaryColor100Index = indexes.first
            primaryColorChanged(primaryColor100Index)
            line100ColorPicker.updateColors(getColors(primaryColors100), primaryColor100Index)
            line100ColorPicker.listener = LineColorPickerListener { index, color ->
                val secondaryColors = getColorsForIndex(index)
                secondaryLineColorPicker.updateColors(secondaryColors)

                val newColor = if (!isPrimaryColorPicker) secondaryLineColorPicker.getCurrentColor() else color
                colorUpdated(newColor)
                LINE_COLOR_INDEX = 3

                if (!isPrimaryColorPicker) {
                    primaryColorChanged(index)
                }
            }
            val primaryColor200Index = indexes.first
            primaryColorChanged(primaryColor200Index)
            line200ColorPicker.updateColors(getColors(primaryColors200), primaryColor200Index)
            line200ColorPicker.listener = LineColorPickerListener { index, color ->
                val secondaryColors = getColorsForIndex(index)
                secondaryLineColorPicker.updateColors(secondaryColors)

                val newColor = if (!isPrimaryColorPicker) secondaryLineColorPicker.getCurrentColor() else color
                colorUpdated(newColor)
                LINE_COLOR_INDEX = 4

                if (!isPrimaryColorPicker) {
                    primaryColorChanged(index)
                }
            }
            val primaryColor300Index = indexes.first
            primaryColorChanged(primaryColor300Index)
            line300ColorPicker.updateColors(getColors(primaryColors300), primaryColor300Index)
            line300ColorPicker.listener = LineColorPickerListener { index, color ->
                val secondaryColors = getColorsForIndex(index)
                secondaryLineColorPicker.updateColors(secondaryColors)

                val newColor = if (!isPrimaryColorPicker) secondaryLineColorPicker.getCurrentColor() else color
                colorUpdated(newColor)
                LINE_COLOR_INDEX = 5

                if (!isPrimaryColorPicker) {
                    primaryColorChanged(index)
                }
            }
            val primaryColor400Index = indexes.first
            primaryColorChanged(primaryColor400Index)
            line400ColorPicker.updateColors(getColors(primaryColors400), primaryColor400Index)
            line400ColorPicker.listener = LineColorPickerListener { index, color ->
                val secondaryColors = getColorsForIndex(index)
                secondaryLineColorPicker.updateColors(secondaryColors)

                val newColor = if (!isPrimaryColorPicker) secondaryLineColorPicker.getCurrentColor() else color
                colorUpdated(newColor)
                LINE_COLOR_INDEX = 6

                if (!isPrimaryColorPicker) {
                    primaryColorChanged(index)
                }
            }
            val primaryColor500Index = indexes.first
            primaryColorChanged(primaryColor500Index)
            line500ColorPicker.updateColors(getColors(primaryColors500), primaryColor500Index)
            line500ColorPicker.listener = LineColorPickerListener { index, color ->
                val secondaryColors = getColorsForIndex(index)
                secondaryLineColorPicker.updateColors(secondaryColors)

                val newColor = if (!isPrimaryColorPicker) secondaryLineColorPicker.getCurrentColor() else color
                colorUpdated(newColor)
                LINE_COLOR_INDEX = 7

                if (!isPrimaryColorPicker) {
                    primaryColorChanged(index)
                }
            }
            val primaryColor600Index = indexes.first
            primaryColorChanged(primaryColor600Index)
            line600ColorPicker.updateColors(getColors(primaryColors600), primaryColor600Index)
            line600ColorPicker.listener = LineColorPickerListener { index, color ->
                val secondaryColors = getColorsForIndex(index)
                secondaryLineColorPicker.updateColors(secondaryColors)

                val newColor = if (!isPrimaryColorPicker) secondaryLineColorPicker.getCurrentColor() else color
                colorUpdated(newColor)
                LINE_COLOR_INDEX = 8

                if (!isPrimaryColorPicker) {
                    primaryColorChanged(index)
                }
            }
            val primaryColor700Index = indexes.first
            primaryColorChanged(primaryColor700Index)
            line700ColorPicker.updateColors(getColors(primaryColors700), primaryColor700Index)
            line700ColorPicker.listener = LineColorPickerListener { index, color ->
                val secondaryColors = getColorsForIndex(index)
                secondaryLineColorPicker.updateColors(secondaryColors)

                val newColor = if (!isPrimaryColorPicker) secondaryLineColorPicker.getCurrentColor() else color
                colorUpdated(newColor)
                LINE_COLOR_INDEX = 9

                if (!isPrimaryColorPicker) {
                    primaryColorChanged(index)
                }
            }
            val primaryColor800Index = indexes.first
            primaryColorChanged(primaryColor800Index)
            line800ColorPicker.updateColors(getColors(primaryColors800), primaryColor800Index)
            line800ColorPicker.listener = LineColorPickerListener { index, color ->
                val secondaryColors = getColorsForIndex(index)
                secondaryLineColorPicker.updateColors(secondaryColors)

                val newColor = if (!isPrimaryColorPicker) secondaryLineColorPicker.getCurrentColor() else color
                colorUpdated(newColor)
                LINE_COLOR_INDEX = 10

                if (!isPrimaryColorPicker) {
                    primaryColorChanged(index)
                }
            }

            secondaryLineColorPicker.beVisibleIf(false) //secondaryLineColorPicker.beVisibleIf(isPrimaryColorPicker)
            secondaryLineColorPicker.updateColors(getColorsForIndex(primaryColorIndex), indexes.second)
            secondaryLineColorPicker.listener = LineColorPickerListener { _, color -> colorUpdated(color) }
            gridColorNewColor.setFillWithStrokeRight(color, backgroundColor)
            gridColorOldColor.setCardBackgroundColor(color)
        }

//        val builder = activity.getAlertDialogBuilder()
//            .setPositiveButton(R.string.ok) { dialog, which -> dialogConfirmed() }
//            .setNegativeButton(R.string.cancel) { dialog, which -> dialogDismissed() }
//            .setOnCancelListener { dialogDismissed() }
//            .apply {
//                setNeutralButton(R.string.use_default) { dialog, which -> useDefault() }
//                activity.setupDialogStuff(view.root, this) { alertDialog ->
//                    dialog = alertDialog
//                }
//            }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { dialog, which -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel) { dialog, which -> dialogDismissed() }
            .setOnCancelListener { dialogDismissed() }
            .apply {
                setNeutralButton(R.string.default_color) { _, _ -> useDefault() }
                activity.setupDialogStuff(view.root, this) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }

    fun getSpecificColor() = view.secondaryLineColorPicker.getCurrentColor()

    private fun colorUpdated(color: Int) {
        view.hexCode.text = color.toHex().substring(1)
        view.gridColorNewColor.setFillWithStrokeRight(color, backgroundColor)
        if (isPrimaryColorPicker) {

            if (appBar != null) {
                activity.updateTopBarColors(appBar, colorBackground, color)
            }

            if (!wasDimmedBackgroundRemoved) {
                dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                wasDimmedBackgroundRemoved = true
            }
        }
    }

    private fun getColorIndexes(color: Int): Pair<Int, Int> {
        if (color == defaultColorValue) {
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
        view.lineColorPickerIcon.setImageResource(appIconIDs?.getOrNull(index) ?: 0)
    }

    private fun getDefaultColorPair() = Pair(DEFAULT_PRIMARY_COLOR_INDEX, DEFAULT_SECONDARY_COLOR_INDEX)

    private fun dialogDismissed() {
        callback(false, 0)
    }

   /* private fun dialogConfirmed() {
        val targetView = if (isPrimaryColorPicker) view.secondaryLineColorPicker else view.primary_line_color_picker
        val color = targetView.getCurrentColor()
        callback(true, color)
    }*/

    private fun dialogConfirmed() {
        if (LINE_COLOR_INDEX == 1) {
            val targetView = view.primaryLineColorPicker
            val color = targetView.getCurrentColor()
            callback(true, color)
        } else if  (LINE_COLOR_INDEX == 2) {
            val targetView = view.line50ColorPicker
            val color = targetView.getCurrentColor()
            callback(true, color)
        } else if  (LINE_COLOR_INDEX == 3) {
            val targetView = view.line100ColorPicker
            val color = targetView.getCurrentColor()
            callback(true, color)
        } else if  (LINE_COLOR_INDEX == 4) {
            val targetView = view.line200ColorPicker
            val color = targetView.getCurrentColor()
            callback(true, color)
        } else if  (LINE_COLOR_INDEX == 5) {
            val targetView = view.line300ColorPicker
            val color = targetView.getCurrentColor()
            callback(true, color)
        } else if  (LINE_COLOR_INDEX == 6) {
            val targetView = view.line400ColorPicker
            val color = targetView.getCurrentColor()
            callback(true, color)
        } else if  (LINE_COLOR_INDEX == 7) {
            val targetView = view.line500ColorPicker
            val color = targetView.getCurrentColor()
            callback(true, color)
        } else if  (LINE_COLOR_INDEX == 8) {
            val targetView = view.line600ColorPicker
            val color = targetView.getCurrentColor()
            callback(true, color)
        } else if  (LINE_COLOR_INDEX == 9) {
            val targetView = view.line700ColorPicker
            val color = targetView.getCurrentColor()
            callback(true, color)
        } else if  (LINE_COLOR_INDEX == 10) {
            val targetView = view.line800ColorPicker
            val color = targetView.getCurrentColor()
            callback(true, color)
        } else {
            callback(false, color)
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

@Composable
fun GridColorPickerAlertDialog(
    alertDialogState: AlertDialogState,
    @ColorInt color: Int,
    isPrimaryColorPicker: Boolean,
    modifier: Modifier = Modifier,
    primaryColors: Int = R.array.line_00,
    appIconIDs: ArrayList<Int>? = null,
    onActiveColorChange: (color: Int) -> Unit,
    onButtonPressed: (wasPositivePressed: Boolean, color: Int) -> Unit
) {
    val view = LocalView.current
    val context = LocalContext.current
    var wasDimmedBackgroundRemoved by remember { mutableStateOf(false) }

    val defaultColor = remember {
        ContextCompat.getColor(context, R.color.color_primary)
    }
    AlertDialog(
        modifier = modifier,
       onDismissRequest = alertDialogState::hide,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        DialogSurface {
            Column(
                Modifier
                    .fillMaxWidth(0.95f)
                    .padding(SimpleTheme.dimens.padding.extraLarge)
            ) {
                val dialogTextColor = dialogTextColor
                var dialogGridColorPickerBinding by remember { mutableStateOf<DialogGridColorPickerBinding?>(null) }
                AndroidViewBinding(
                    DialogGridColorPickerBinding::inflate, onRelease = {
                        dialogGridColorPickerBinding = null
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    root.updateLayoutParams<FrameLayout.LayoutParams> {
                        height = FrameLayout.LayoutParams.WRAP_CONTENT
                    }
                    dialogGridColorPickerBinding = this
                    fun colorUpdated(color: Int) {
                        hexCode.text = color.toHex()
                        onActiveColorChange(color)
                        if (isPrimaryColorPicker) {
                            if (!wasDimmedBackgroundRemoved) {
                                (view.parent as? DialogWindowProvider)?.window?.setDimAmount(0f)
                                wasDimmedBackgroundRemoved = true
                            }
                        }
                    }

                    hexCode.setTextColor(dialogTextColor.toArgb())
                    hexCode.text = color.toHex()
                    hexCode.setOnLongClickListener {
                        context.copyToClipboard(hexCode.value.substring(1))
                        true
                    }

                    lineColorPickerIcon.beGoneIf(isPrimaryColorPicker)
                    val indexes = context.getColorIndexes(color, defaultColor)

                    val primaryColorIndex = indexes.first
                    lineColorPickerIcon.setImageResource(appIconIDs?.getOrNull(primaryColorIndex) ?: 0)
                    primaryLineColorPicker.updateColors(context.getColors(primaryColors), primaryColorIndex)
                    primaryLineColorPicker.listener = LineColorPickerListener { index, color ->
                        val secondaryColors = context.getColorsForIndex(index)
                        secondaryLineColorPicker.updateColors(secondaryColors)

                        val newColor = if (isPrimaryColorPicker) secondaryLineColorPicker.getCurrentColor() else color
                        colorUpdated(newColor)

                        if (!isPrimaryColorPicker) {
                            lineColorPickerIcon.setImageResource(appIconIDs?.getOrNull(index) ?: 0)
                        }
                    }

                    secondaryLineColorPicker.beVisibleIf(isPrimaryColorPicker)
                    secondaryLineColorPicker.updateColors(context.getColorsForIndex(primaryColorIndex), indexes.second)
                    secondaryLineColorPicker.listener = LineColorPickerListener { _, color -> colorUpdated(color) }
                }

                Row(
                    Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        alertDialogState.hide()
                        onButtonPressed(false, 0)
                    }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                    TextButton(onClick = {
                        if (dialogGridColorPickerBinding != null) {
                            val targetView =
                                if (isPrimaryColorPicker) dialogGridColorPickerBinding!!.secondaryLineColorPicker else dialogGridColorPickerBinding!!.primaryLineColorPicker
                            onButtonPressed(true, targetView.getCurrentColor())
                        }
                        alertDialogState.hide()

                    }) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            }
        }
    }
}

private const val PRIMARY_COLORS_COUNT = 19
private const val DEFAULT_PRIMARY_COLOR_INDEX = 14
private const val DEFAULT_SECONDARY_COLOR_INDEX = 6

private fun Context.getColorIndexes(color: Int, defaultColor: Int): Pair<Int, Int> {
    if (color == defaultColor) {
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

private fun getDefaultColorPair() = Pair(DEFAULT_PRIMARY_COLOR_INDEX, DEFAULT_SECONDARY_COLOR_INDEX)


private fun Context.getColorsForIndex(index: Int) = when (index) {
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
    17 -> getColors(R.array.md_blue_greys)
    18 -> getColors(R.array.md_greys)
    else -> throw RuntimeException("Invalid color id $index")
}

private fun Context.getColors(id: Int) = resources.getIntArray(id).toCollection(ArrayList())

@SuppressLint("ResourceAsColor")
@Composable
@MyDevices
private fun GridColorPickerAlertDialogPreview() {
    AppThemeSurface {
        GridColorPickerAlertDialog(alertDialogState = rememberAlertDialogState(),
            color = R.color.color_primary,
            isPrimaryColorPicker = true,
            onActiveColorChange = {}
        ) { _, _ -> }
    }
}
