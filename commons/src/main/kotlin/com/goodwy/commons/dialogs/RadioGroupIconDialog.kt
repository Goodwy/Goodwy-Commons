package com.goodwy.commons.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.goodwy.commons.R
import com.goodwy.commons.databinding.DialogRadioGroupBinding
import com.goodwy.commons.databinding.RadioButtonIconBinding
import com.goodwy.commons.extensions.*
import com.goodwy.commons.models.RadioItem

class RadioGroupIconDialog(
    val activity: Activity,
    val items: ArrayList<RadioItem>,
    val checkedItemId: Int = -1,
    val titleId: Int = 0,
    showOKButton: Boolean = false,
    val defaultItemId: Int? = null,
    val cancelCallback: (() -> Unit)? = null,
    val callback: (newValue: Any) -> Unit
) {
    private var dialog: AlertDialog? = null
    private var wasInit = false
    private var selectedItemId = -1

    init {
        val view = DialogRadioGroupBinding.inflate(activity.layoutInflater, null, false)
        view.dialogRadioGroup.apply {
            for (i in 0 until items.size) {
                RadioButtonIconBinding.inflate(activity.layoutInflater, this, false).apply {
                    dialogRadioButton.apply {
                        text = items[i].title
                        isChecked = items[i].id == checkedItemId
                        id = i
                        setOnClickListener { itemSelected(i) }
                    }
                    dialogRadioButtonIcon.apply {
                        val drawable = items[i].drawable
                        val icon = items[i].icon
                        if (drawable != null) {
                            setImageDrawable(drawable)
                        } else if (icon != null) {
                            setImageResource(icon)
                            setColorFilter(activity.getProperTextColor())
                        }
                    }

                    if (items[i].id == checkedItemId) {
                        selectedItemId = i
                    }
                    addView(root)
                }
            }
        }

        val builder = activity.getAlertDialogBuilder()
                .setOnCancelListener { cancelCallback?.invoke() }

        if (selectedItemId != -1 && showOKButton) {
            builder.setPositiveButton(R.string.ok) { dialog, which -> itemSelected(selectedItemId) }
        }

        builder.apply {
            if (defaultItemId != null) {
                setNeutralButton(R.string.default_color) { _, _ ->
                    val checkedId = items.indexOfFirst {it.id == defaultItemId}
                    itemSelected(checkedId)
                }
            }
            activity.setupDialogStuff(view.root, this, titleId) { alertDialog ->
                dialog = alertDialog
            }
        }

        if (selectedItemId != -1) {
            view.dialogRadioHolder.apply {
                onGlobalLayout {
                    scrollY = view.dialogRadioGroup.bottom - height
                }
            }
        }

        wasInit = true
    }

    private fun itemSelected(checkedId: Int) {
        if (wasInit) {
            callback(items[checkedId].value)
            dialog?.dismiss()
        }
    }
}
