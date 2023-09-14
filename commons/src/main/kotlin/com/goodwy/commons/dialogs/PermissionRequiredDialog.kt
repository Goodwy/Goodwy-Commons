package com.goodwy.commons.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.goodwy.commons.R
import com.goodwy.commons.extensions.getAlertDialogBuilder
import com.goodwy.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_message.view.*

class PermissionRequiredDialog(
    val activity: Activity,
    textId: Int,
    private val positiveActionCallback: () -> Unit,
    private val negativeActionCallback: (() -> Unit)? = null
) {
    private var dialog: AlertDialog? = null

    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_message, null)
        view.message.text = activity.getString(textId)

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.grant_permission) { _, _ -> positiveActionCallback() }
            .setNegativeButton(R.string.cancel) { _, _ -> negativeActionCallback?.invoke() }.apply {
                val title = activity.getString(R.string.permission_required)
                activity.setupDialogStuff(view, this, titleText = title) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }
}
