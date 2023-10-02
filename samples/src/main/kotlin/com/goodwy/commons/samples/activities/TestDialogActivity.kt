package com.goodwy.commons.samples.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.goodwy.commons.compose.alert_dialog.AlertDialogState
import com.goodwy.commons.compose.alert_dialog.rememberAlertDialogState
import com.goodwy.commons.compose.theme.AppThemeSurface
import com.goodwy.commons.dialogs.*
import com.goodwy.commons.extensions.baseConfig
import com.goodwy.commons.extensions.toHex

class TestDialogActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppThemeSurface {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.padding(top = 16.dp))
                    ShowButton(getAppSideLoadedDialogState(), text = "App side loaded dialog")
                    ShowButton(getAddBlockedNumberDialogState(), text = "Add blocked number")
                    ShowButton(getConfirmationAlertDialogState(), text = "Confirmation normal")
                    ShowButton(getConfirmationAdvancedAlertDialogState(), text = "Confirmation advanced")
                    ShowButton(getDonateAlertDialogState(), text = "Donate")
                    ShowButton(getFeatureLockedAlertDialogState(), text = "Feature Locked")
                    ShowButton(getPurchaseThankYouAlertDialogState(), text = "Purchase thank you")
                    ShowButton(getLineColorPickerAlertDialogState(), text = "Line color picker")
                    ShowButton(getCallConfirmationAlertDialogState(), text = "Call confirmation")
                    ShowButton(getGridColorPickerAlertDialogState(), text = "Grid color picker")
                    Spacer(modifier = Modifier.padding(bottom = 16.dp))
                }
            }
        }
    }

    @Composable
    private fun getLineColorPickerAlertDialogState() = rememberAlertDialogState().apply {
        DialogMember {
            LineColorPickerAlertDialog(
                alertDialogState = this,
                color = baseConfig.customPrimaryColor,
                isPrimaryColorPicker = true,
                onButtonPressed = { wasPositivePressed, color ->
                    Log.d("getLineColorPickerAlertDialogState", "wasPositivePressed=$wasPositivePressed color=${color.toHex()}")
                }, onActiveColorChange = { color ->
                    Log.d("getLineColorPickerAlertDialogState", "onActiveColorChange=${color.toHex()}")
                })
        }
    }

    @Composable
    private fun getPurchaseThankYouAlertDialogState() = rememberAlertDialogState().apply {
        DialogMember {
            PurchaseThankYouAlertDialog(alertDialogState = this)
        }
    }

    @Composable
    private fun getCallConfirmationAlertDialogState() = rememberAlertDialogState().apply {
        DialogMember {
            CallConfirmationAlertDialog(alertDialogState = this, callee = "Simple Mobile Tools",
                color = baseConfig.customAccentColor, callback = {})
        }
    }

    @Composable
    private fun getFeatureLockedAlertDialogState() = rememberAlertDialogState().apply {
        DialogMember {
            FeatureLockedAlertDialog(alertDialogState = this, callback = {})
        }
    }

    @Composable
    private fun getDonateAlertDialogState() =
        rememberAlertDialogState().apply {
            DialogMember {
                DonateAlertDialog(alertDialogState = this)
            }
        }


    @Composable
    private fun getConfirmationAlertDialogState() = rememberAlertDialogState().apply {
        DialogMember {
            ConfirmationAlertDialog(alertDialogState = this, callback = {}, dialogTitle = "Some fancy title")
        }
    }

    @Composable
    private fun getAppSideLoadedDialogState() =
        rememberAlertDialogState().apply {
            DialogMember {
                AppSideLoadedAlertDialog(onDownloadClick = {}, onCancelClick = {}, alertDialogState = this)
            }
        }


    @Composable
    private fun getAddBlockedNumberDialogState() =
        rememberAlertDialogState().apply {
            DialogMember {
                AddOrEditBlockedNumberAlertDialog(blockedNumber = null, deleteBlockedNumber = {}, addBlockedNumber = {}, alertDialogState = this)
            }
        }


    @Composable
    private fun getConfirmationAdvancedAlertDialogState() =
        rememberAlertDialogState().apply {
            DialogMember {
                ConfirmationAdvancedAlertDialog(alertDialogState = this, callback = {})
            }
        }


    @Composable
    private fun ShowButton(appSideLoadedDialogState: AlertDialogState, text: String) {
        Button(onClick = appSideLoadedDialogState::show) {
            Text(
                text = text,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    private fun getGridColorPickerAlertDialogState() = rememberAlertDialogState().apply {
        DialogMember {
            GridColorPickerAlertDialog(
                alertDialogState = this,
                color = baseConfig.customPrimaryColor,
                isPrimaryColorPicker = true,
                onButtonPressed = { wasPositivePressed, color ->
                    Log.d("getGridColorPickerAlertDialogState", "wasPositivePressed=$wasPositivePressed color=${color.toHex()}")
                }, onActiveColorChange = { color ->
                    Log.d("getGridColorPickerAlertDialogState", "onActiveColorChange=${color.toHex()}")
                })
        }
    }
}
