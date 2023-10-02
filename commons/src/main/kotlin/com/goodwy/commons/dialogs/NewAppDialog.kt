package com.goodwy.commons.dialogs

import android.app.Activity
import android.graphics.drawable.Drawable
import android.text.Html
import com.goodwy.commons.R
import com.goodwy.commons.databinding.DialogNewAppsBinding
import com.goodwy.commons.extensions.baseConfig
import com.goodwy.commons.extensions.getAlertDialogBuilder
import com.goodwy.commons.extensions.launchViewIntent
import com.goodwy.commons.extensions.setupDialogStuff

// run inside: runOnUiThread { }
class NewAppDialog(
    val activity: Activity,
    private val packageName: String,
    val title: String,
    val text: String,
    val drawable: Drawable?,
    val callback: () -> Unit)
{
    init {
        val view = DialogNewAppsBinding.inflate(activity.layoutInflater, null, false).apply {
            newAppsTitle.text = Html.fromHtml(title)
            newAppsText.text = text
            newAppsIcon.setImageDrawable(drawable!!)
            newAppsHolder.setOnClickListener { dialogConfirmed() }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.later) { _, _ -> dialogDismissed(8) }
            .setNeutralButton(R.string.do_not_show_again) { _, _ -> dialogDismissed(1) }
            .setOnCancelListener { dialogDismissed(8) }
            .apply {
                activity.setupDialogStuff(view.root, this)
            }
    }

    private fun dialogDismissed(count: Int) {
        activity.baseConfig.appRecommendationDialogCount = count
        callback()
    }

    private fun dialogConfirmed() {
        val url = "https://play.google.com/store/apps/details?id=$packageName"
        activity.launchViewIntent(url)
        callback()
    }
}
