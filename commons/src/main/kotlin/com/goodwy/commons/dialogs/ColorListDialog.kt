package com.goodwy.commons.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.goodwy.commons.R
import com.goodwy.commons.databinding.DialogColorListBinding
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.LBC_ANDROID
import com.goodwy.commons.helpers.LBC_ARC
import com.goodwy.commons.helpers.LBC_IOS
import com.goodwy.commons.helpers.LBC_ORIGINAL
import com.goodwy.strings.R as stringsR

class ColorListDialog(val activity: Activity, val callback: (newValue: Any) -> Unit) {

    private var dialog: AlertDialog? = null
    private var wasInit = false

    init {
        val view = DialogColorListBinding.inflate(activity.layoutInflater, null, false).apply {
            arrayOf(icon1Check, icon2Check, icon3Check, icon4Check).forEach {
                it.applyColorFilter(activity.getProperPrimaryColor())
            }

//            arrayOf(icon1, icon2, icon3, icon4).forEach {
//                it.background.applyColorFilter(activity.getBottomNavigationBackgroundColor())
//            }

            when (activity.baseConfig.contactColorList) {
                LBC_ORIGINAL -> icon1Check.beVisible()
                LBC_ANDROID -> icon2Check.beVisible()
                LBC_IOS -> icon3Check.beVisible()
                LBC_ARC -> icon4Check.beVisible()
            }

            icon1.setOnClickListener { itemSelected(LBC_ORIGINAL) }
            icon2.setOnClickListener { itemSelected(LBC_ANDROID) }
            icon3.setOnClickListener { itemSelected(LBC_IOS) }
            icon4.setOnClickListener { itemSelected(LBC_ARC) }
        }

        val builder = activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)

        builder.apply {
            activity.setupDialogStuff(view.root, this, stringsR.string.contact_color_list, cancelOnTouchOutside = true) { alertDialog ->
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
