package com.goodwy.commons.dialogs

import com.goodwy.commons.R
import com.goodwy.commons.activities.BaseSimpleActivity
import com.goodwy.commons.databinding.DialogOpenDeviceSettingsBinding
import com.goodwy.commons.extensions.getAlertDialogBuilder
import com.goodwy.commons.extensions.openDeviceSettings
import com.goodwy.commons.extensions.setupDialogStuff

class OpenDeviceSettingsDialog(val activity: BaseSimpleActivity, message: String) {

    init {
        activity.apply {
            val view = DialogOpenDeviceSettingsBinding.inflate(layoutInflater, null, false)
            view.openDeviceSettings.text = message
            getAlertDialogBuilder()
                .setNegativeButton(R.string.close, null)
                .setPositiveButton(R.string.go_to_settings) { _, _ ->
                    openDeviceSettings()
                }.apply {
                    setupDialogStuff(view.root, this)
                }
        }
    }
}
