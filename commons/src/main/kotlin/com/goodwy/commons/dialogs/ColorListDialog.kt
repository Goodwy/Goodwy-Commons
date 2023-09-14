package com.goodwy.commons.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.goodwy.commons.R
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.LBC_ANDROID
import com.goodwy.commons.helpers.LBC_ARC
import com.goodwy.commons.helpers.LBC_IOS
import com.goodwy.commons.helpers.LBC_ORIGINAL
import kotlinx.android.synthetic.main.dialog_color_list.view.*

class ColorListDialog(val activity: Activity, val callback: (newValue: Any) -> Unit) {

    private var dialog: AlertDialog? = null
    private var wasInit = false

    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_color_list, null).apply {
            arrayOf(icon1_check, icon2_check, icon3_check, icon4_check).forEach {
                it.applyColorFilter(activity.getProperPrimaryColor())
            }

            when (activity.baseConfig.contactColorList) {
                LBC_ORIGINAL -> icon1_check.beVisible()
                LBC_ANDROID -> icon2_check.beVisible()
                LBC_IOS -> icon3_check.beVisible()
                LBC_ARC -> icon4_check.beVisible()
            }

            icon1.setOnClickListener { itemSelected(LBC_ORIGINAL) }
            icon2.setOnClickListener { itemSelected(LBC_ANDROID) }
            icon3.setOnClickListener { itemSelected(LBC_IOS) }
            icon4.setOnClickListener { itemSelected(LBC_ARC) }
        }

        val builder = activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)

        builder.apply {
            activity.setupDialogStuff(view, this, R.string.contact_color_list, cancelOnTouchOutside = true) { alertDialog ->
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
