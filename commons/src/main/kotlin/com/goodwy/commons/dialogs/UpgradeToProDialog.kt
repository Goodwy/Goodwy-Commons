package com.goodwy.commons.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.goodwy.commons.R
import com.goodwy.commons.databinding.DialogUpgradeToProBinding
import com.goodwy.commons.extensions.getAlertDialogBuilder
import com.goodwy.commons.extensions.launchMoreAppsFromUsIntent
import com.goodwy.commons.extensions.launchUpgradeToProIntent
import com.goodwy.commons.extensions.setupDialogStuff

class UpgradeToProDialog(val activity: Activity) {

    init {
        val view = DialogUpgradeToProBinding.inflate(activity.layoutInflater, null, false).apply {
            upgradeToPro.text = activity.getString(R.string.upgrade_to_pro_long)
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.upgrade) { _, _ -> upgradeApp() }
            .setNeutralButton(R.string.more_info, null)     // do not dismiss the dialog on pressing More Info
            .setNegativeButton(R.string.later, null)
            .apply {
                activity.setupDialogStuff(view.root, this, R.string.upgrade_to_pro, cancelOnTouchOutside = false) { alertDialog ->
                    alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                        moreInfo()
                    }
                }
            }
    }

    private fun upgradeApp() {
        activity.launchUpgradeToProIntent()
    }

    private fun moreInfo() {
        activity.launchMoreAppsFromUsIntent()
    }
}

