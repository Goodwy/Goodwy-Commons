package com.goodwy.commons.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.goodwy.commons.R
import com.goodwy.commons.extensions.*
import kotlinx.android.synthetic.main.dialog_rate_stars.view.*

class RateStarsDialog(val activity: Activity) {
    private var dialog: AlertDialog

    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_rate_stars, null).apply {
            val textColor = activity.baseConfig.textColor
            arrayOf(rate_star_1, rate_star_2, rate_star_3, rate_star_4, rate_star_5).forEach {
                it.applyColorFilter(textColor)
            }

            rate_star_1.setOnClickListener { dialogCancelled(true) }
            rate_star_2.setOnClickListener { dialogCancelled(true) }
            rate_star_3.setOnClickListener { dialogCancelled(true) }
            rate_star_4.setOnClickListener { dialogCancelled(true) }
            rate_star_5.setOnClickListener {
                activity.redirectToRateUs()
                dialogCancelled(true)
            }
        }

        dialog = AlertDialog.Builder(activity)
            .setNegativeButton(R.string.cancel) { dialog, which -> dialogCancelled(false) }
            .setOnCancelListener { dialogCancelled(false) }
            .create().apply {
                activity.setupDialogStuff(view, this, cancelOnTouchOutside = false)
            }
    }

    private fun dialogCancelled(showThankYou: Boolean) {
        dialog.dismiss()
        if (showThankYou) {
            activity.toast(R.string.thank_you)
            activity.baseConfig.wasAppRated = true
        }
    }
}
