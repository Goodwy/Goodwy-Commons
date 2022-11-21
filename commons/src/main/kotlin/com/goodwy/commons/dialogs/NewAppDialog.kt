package com.goodwy.commons.dialogs

import android.app.Activity
import android.graphics.drawable.Drawable
import android.text.Html
import com.goodwy.commons.R
import com.goodwy.commons.extensions.baseConfig
import com.goodwy.commons.extensions.getAlertDialogBuilder
import com.goodwy.commons.extensions.launchViewIntent
import com.goodwy.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_new_apps.view.*
import kotlinx.android.synthetic.main.dialog_new_apps.view.new_apps_text

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
        val view = activity.layoutInflater.inflate(R.layout.dialog_new_apps, null).apply {
            new_apps_title.text = Html.fromHtml(title)
            new_apps_text.text = text
            new_apps_icon.setImageDrawable(drawable!!)
            new_apps_holder.setOnClickListener { dialogConfirmed() }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.later) { dialog, which -> dialogDismissed(8) }
            .setNeutralButton(R.string.do_not_show_again) { dialog, which -> dialogDismissed(1) }
            .setOnCancelListener { dialogDismissed(8) }
            .apply {
                activity.setupDialogStuff(view, this)
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
