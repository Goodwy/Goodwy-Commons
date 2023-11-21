package com.goodwy.commons.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.goodwy.commons.R
import com.goodwy.commons.databinding.DialogOverflowIconBinding
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.OVERFLOW_ICON_HORIZONTAL
import com.goodwy.commons.helpers.OVERFLOW_ICON_HORIZONTAL_ROUND
import com.goodwy.commons.helpers.OVERFLOW_ICON_VERTICAL

class OverflowIconDialog(val activity: Activity, val callback: (newValue: Any) -> Unit) {

    private var dialog: AlertDialog? = null
    private var wasInit = false

    init {
        val view = DialogOverflowIconBinding.inflate(activity.layoutInflater, null, false).apply {
            val primaryColor = activity.getProperPrimaryColor()

            arrayOf(icon0, icon1, icon2).forEach {
                it.applyColorFilter(activity.getProperTextColor())
            }

            when (activity.baseConfig.overflowIcon) {
                OVERFLOW_ICON_HORIZONTAL -> icon0.applyColorFilter(primaryColor)
                OVERFLOW_ICON_VERTICAL -> icon1.applyColorFilter(primaryColor)
                OVERFLOW_ICON_HORIZONTAL_ROUND -> icon2.applyColorFilter(primaryColor)
            }

            icon0.setOnClickListener { itemSelected(OVERFLOW_ICON_HORIZONTAL) }
            icon1.setOnClickListener { itemSelected(OVERFLOW_ICON_VERTICAL) }
            icon2.setOnClickListener { itemSelected(OVERFLOW_ICON_HORIZONTAL_ROUND) }
        }

        val builder = activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)

        builder.apply {
            activity.setupDialogStuff(view.root, this, R.string.overflow_icon, cancelOnTouchOutside = true) { alertDialog ->
                dialog = alertDialog
            }
        }

        wasInit = true
    }

    private fun itemSelected(checkedId: Int) {
        if (wasInit) {
            activity.baseConfig.overflowIcon = checkedId
            callback(checkedId)
            dialog?.dismiss()
        }
    }
}
