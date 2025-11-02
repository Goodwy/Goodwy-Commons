package com.goodwy.commons.dialogs

import android.app.Activity
import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.goodwy.commons.R
import com.goodwy.commons.compose.alert_dialog.*
import com.goodwy.commons.compose.extensions.MyDevices
import com.goodwy.commons.compose.theme.AppThemeSurface
import com.goodwy.commons.databinding.DialogMessageBinding
import com.goodwy.commons.extensions.getAlertDialogBuilder
import com.goodwy.commons.extensions.setupDialogStuff

// similar fo ConfirmationDialog, but has a callback for negative button too
class ConfirmationAdvancedDialog(
    activity: Activity, message: String = "", messageId: Int = R.string.proceed_with_deletion, positive: Int = R.string.yes,
    negative: Int = R.string.no, val cancelOnTouchOutside: Boolean = true, val fromHtml: Boolean = false, val callback: (result: Boolean) -> Unit
) {
    private var dialog: AlertDialog? = null

    init {
        val view = DialogMessageBinding.inflate(activity.layoutInflater, null, false)
        val text = message.ifEmpty { activity.resources.getString(messageId) }
        view.message.text = if (fromHtml) Html.fromHtml(text) else text
        if (fromHtml) view.message.movementMethod = LinkMovementMethod.getInstance()

        val builder = activity.getAlertDialogBuilder()
            .setPositiveButton(positive) { _, _ -> positivePressed() }

        if (negative != 0) {
            builder.setNegativeButton(negative) { _, _ -> negativePressed() }
        }

        if (!cancelOnTouchOutside) {
            builder.setOnCancelListener { negativePressed() }
        }

        builder.apply {
            activity.setupDialogStuff(view.root, this, cancelOnTouchOutside = cancelOnTouchOutside) { alertDialog ->
                dialog = alertDialog
            }
        }
    }

    private fun positivePressed() {
        callback(true)
        dialog?.dismiss()
    }

    private fun negativePressed() {
        callback(false)
        dialog?.dismiss()
    }
}

@Composable
fun ConfirmationAdvancedAlertDialog(
    alertDialogState: AlertDialogState,
    modifier: Modifier = Modifier,
    message: String = "",
    messageId: Int? = R.string.proceed_with_deletion,
    positive: Int? = R.string.yes,
    negative: Int? = R.string.no,
    cancelOnTouchOutside: Boolean = true,
    callback: (result: Boolean) -> Unit
) {

    androidx.compose.material3.AlertDialog(
        containerColor = dialogContainerColor,
        modifier = modifier
            .dialogBorder(),
        properties = DialogProperties(dismissOnClickOutside = cancelOnTouchOutside),
        onDismissRequest = {
            alertDialogState.hide()
            callback(false)
        },
        shape = dialogShape,
        tonalElevation = dialogElevation,
        dismissButton = {
            if (negative != null) {
                TextButton(onClick = {
                    alertDialogState.hide()
                    callback(false)
                }) {
                    Text(text = stringResource(id = negative))
                }
            }
        },
        confirmButton = {
            if (positive != null) {
                TextButton(onClick = {
                    alertDialogState.hide()
                    callback(true)
                }) {
                    Text(text = stringResource(id = positive))
                }
            }
        },
        text = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = message.ifEmpty { messageId?.let { stringResource(id = it) }.orEmpty() },
                fontSize = 16.sp,
                color = dialogTextColor,
            )
        }
    )
}

@Composable
@MyDevices
private fun ConfirmationAdvancedAlertDialogPreview() {
    AppThemeSurface {
        ConfirmationAdvancedAlertDialog(
            alertDialogState = rememberAlertDialogState()
        ) {}
    }
}
