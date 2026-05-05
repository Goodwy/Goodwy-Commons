package com.goodwy.commons.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.goodwy.commons.R
import com.goodwy.commons.compose.alert_dialog.*
import com.goodwy.commons.compose.extensions.MyDevices
import com.goodwy.commons.compose.theme.AppThemeSurface
import com.goodwy.commons.databinding.DialogQrCodeBinding
import com.goodwy.commons.extensions.beVisibleIf
import com.goodwy.commons.extensions.getAlertDialogBuilder
import com.goodwy.commons.extensions.setupDialogStuff
import com.goodwy.commons.helpers.QrCodeHelper

/**
 * Dialog box for displaying a QR code
 *
 * @param activity has to be activity context to avoid some Theme.AppCompat issues
 * @param message the dialogs message, can be any String. If empty, messageId is used
 * @param content content for generating QR codes
 * @param positive positive buttons text ID
 * @param callback an anonymous function
 */
class QrCodeDialog(
    activity: Activity,
    message: String = "",
    content: String,
    positive: Int = R.string.close,
    dialogTitle: String = "",
    val callback: () -> Unit
) {
    private var dialog: AlertDialog? = null

    init {
        val view = DialogQrCodeBinding.inflate(activity.layoutInflater, null, false)
        view.title.text = dialogTitle
        view.title.beVisibleIf(dialogTitle.isNotBlank())

        view.message.text = message
        view.message.beVisibleIf(message.isNotBlank())

        val qrBitmap = QrCodeHelper.generateQrCode(activity, content, 640)
        if (qrBitmap != null) view.qrCode.setImageBitmap(qrBitmap)
        else {
            view.qrCode.setImageResource(R.drawable.ic_question_round)
            view.qrCode.contentDescription = activity.resources.getString(R.string.unknown_error_occurred)
        }

        val builder = activity.getAlertDialogBuilder()
            .setPositiveButton(positive) { _, _ -> dialogConfirmed() }

        builder.apply {
            activity.setupDialogStuff(
                view.root, this,
//                titleText = dialogTitle,
                cancelOnTouchOutside = true
            ) { alertDialog ->
                dialog = alertDialog
            }
        }
    }

    private fun dialogConfirmed() {
        callback()
        dialog?.dismiss()
    }
}

@Composable
fun QrCodeDialog(
    alertDialogState: AlertDialogState,
    modifier: Modifier = Modifier,
    message: String = "",
    content: String,
    positive: Int? = R.string.close,
    dialogTitle: String = "",
    callback: () -> Unit
) {
    val context = LocalContext.current
    val qrBitmap = remember(content) {
        QrCodeHelper.generateQrCode(context, content, 640)
    }

    AlertDialog(
        containerColor = dialogContainerColor,
        modifier = modifier
            .dialogBorder(),
        properties = DialogProperties(dismissOnClickOutside = true),
        onDismissRequest = {
            alertDialogState.hide()
            callback()
        },
        shape = dialogShape,
        tonalElevation = dialogElevation,
        confirmButton = {
            if (positive != null) {
                TextButton(onClick = {
                    alertDialogState.hide()
                    callback()
                }) {
                    Text(text = stringResource(id = positive))
                }
            }
        },
        title = {
            if (dialogTitle.isNotBlank() || dialogTitle.isNotEmpty()) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = dialogTitle,
                    textAlign = TextAlign.Center,
                    color = dialogTextColor,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = stringResource(id = R.string.qr_code),
                        modifier = Modifier
                            .size(240.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_question_round),
                        contentDescription = stringResource(id = R.string.unknown_error_occurred),
                        modifier = Modifier
                            .size(240.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    )
                }
                if (message.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        text = message,
                        color = dialogTextColor,
                    )
                }
            }
        }
    )
}

@Composable
@MyDevices
private fun QrCodeAlertDialogPreview() {
    AppThemeSurface {
        QrCodeDialog(
            alertDialogState = rememberAlertDialogState(),
            content = "QR code",
            message = "Contact",
            dialogTitle = "QR code",
        ) {}
    }
}
