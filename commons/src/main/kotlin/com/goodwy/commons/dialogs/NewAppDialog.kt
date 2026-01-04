package com.goodwy.commons.dialogs

import android.app.Activity
import android.graphics.drawable.Drawable
import android.text.Html
import com.goodwy.commons.R
import com.goodwy.commons.databinding.DialogNewAppsBinding
import com.goodwy.commons.extensions.baseConfig
import com.goodwy.commons.extensions.beVisibleIf
import com.goodwy.commons.extensions.getAlertDialogBuilder
import com.goodwy.commons.extensions.getProperPrimaryColor
import com.goodwy.commons.extensions.getRoundedDrawable
import com.goodwy.commons.extensions.launchViewIntent
import com.goodwy.commons.extensions.setupDialogStuff

// run inside: runOnUiThread { }
class NewAppDialog(
    val activity: Activity,
    private val packageName: String,
    val title: String,
    val text: String,
    val drawable: Drawable?,
    val showSubtitle: Boolean = false,
    val callback: () -> Unit)
{
    init {
        val view = DialogNewAppsBinding.inflate(activity.layoutInflater, null, false).apply {
            newAppsTitle.text = Html.fromHtml(title)
            newAppsText.text = text
            newAppsIcon.setImageDrawable(drawable!!)
            newAppsHolder.setOnClickListener { dialogConfirmed() }
            newAppsSubtitle.beVisibleIf(showSubtitle)
            newAppsSubtitle.setTextColor(activity.getProperPrimaryColor())
            newAppsSubtitle.setOnClickListener { moreInfo() }
        }

        val builder = activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.later) { _, _ -> dialogDismissed(8) }
            .setNeutralButton(R.string.do_not_show_again) { _, _ -> dialogDismissed(1) }
            .setOnCancelListener { dialogDismissed(8) }

        builder.apply {
            activity.setupDialogStuff(view.root, this) { alertDialog ->
                val window = alertDialog.window
                window?.setBackgroundDrawable(activity.getRoundedDrawable())
            }
        }
    }

    private fun dialogDismissed(count: Int) {
        if (showSubtitle) {
            activity.baseConfig.newAppRecommendationDialogCount = count
        } else {
            activity.baseConfig.appRecommendationDialogCount = count
        }
        callback()
    }

    private fun moreInfo() {
        val url = "https://www.goodwy.dev/new-developer-account"
        activity.launchViewIntent(url)
        callback()
    }

    private fun dialogConfirmed() {
        val url = "https://play.google.com/store/apps/details?id=$packageName"
        activity.launchViewIntent(url)
        callback()
    }
}
