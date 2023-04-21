package com.goodwy.commons.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.goodwy.commons.R
import com.goodwy.commons.extensions.*
import kotlinx.android.synthetic.main.dialog_settings_icon.view.*

class SettingsIconDialog(val activity: Activity, val callback: (newValue: Any) -> Unit) {

    private var dialog: AlertDialog? = null
    private var wasInit = false

    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_settings_icon, null).apply {
            arrayOf(icon1, icon2, icon3, icon4, icon5, icon6).forEach {
                it.applyColorFilter(activity.getProperTextColor())
            }

            when (activity.baseConfig.settingsIcon) {
                0 -> icon1.applyColorFilter(activity.getProperPrimaryColor())
                1 -> icon2.applyColorFilter(activity.getProperPrimaryColor())
                2 -> icon3.applyColorFilter(activity.getProperPrimaryColor())
                3 -> icon4.applyColorFilter(activity.getProperPrimaryColor())
                4 -> icon5.applyColorFilter(activity.getProperPrimaryColor())
                5 -> icon6.applyColorFilter(activity.getProperPrimaryColor())
            }

            icon1.setOnClickListener { itemSelected(0) }
            icon2.setOnClickListener { itemSelected(1) }
            icon3.setOnClickListener { itemSelected(2) }
            icon4.setOnClickListener { itemSelected(3) }
            icon5.setOnClickListener { itemSelected(4) }
            icon6.setOnClickListener { itemSelected(5) }
        }

        val builder = activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)

        builder.apply {
            activity.setupDialogStuff(view, this, R.string.settings_icon, cancelOnTouchOutside = true) { alertDialog ->
                dialog = alertDialog
            }
        }

        wasInit = true
    }

    private fun itemSelected(checkedId: Int) {
        if (wasInit) {
            callback(checkedId)
            dialog?.dismiss()
        }
    }
}
