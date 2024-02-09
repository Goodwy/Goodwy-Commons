package com.goodwy.commons.dialogs

import android.view.View
import com.goodwy.commons.R
import com.goodwy.commons.activities.BaseSimpleActivity
import com.goodwy.commons.databinding.DialogChangeViewTypeBinding
import com.goodwy.commons.extensions.baseConfig
import com.goodwy.commons.extensions.getAlertDialogBuilder
import com.goodwy.commons.extensions.setupDialogStuff
import com.goodwy.commons.helpers.VIEW_TYPE_GRID
import com.goodwy.commons.helpers.VIEW_TYPE_LIST

class ChangeViewTypeDialog(val activity: BaseSimpleActivity, val path: String = "", val callback: () -> Unit) {
    private var view: DialogChangeViewTypeBinding
    private var config = activity.baseConfig

    init {
        view = DialogChangeViewTypeBinding.inflate(activity.layoutInflater, null, false).apply {
            val viewToCheck = when (config.viewType) {
                VIEW_TYPE_GRID -> changeViewTypeDialogRadioGrid.id
                else -> changeViewTypeDialogRadioList.id
            }

            changeViewTypeDialogRadio.check(viewToCheck)
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view.root, this, R.string.change_view_type)
            }
    }

    private fun dialogConfirmed() {
        val viewType = if (view.changeViewTypeDialogRadioGrid.isChecked) {
            VIEW_TYPE_GRID
        } else {
            VIEW_TYPE_LIST
        }
        config.viewType = viewType
        callback()
    }
}
