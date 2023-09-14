package com.goodwy.commons.dialogs

import android.view.animation.AnimationUtils
import com.goodwy.commons.R
import com.goodwy.commons.activities.BaseSimpleActivity
import com.goodwy.commons.extensions.*
import kotlinx.android.synthetic.main.dialog_call_confirmation.view.*

class CallConfirmationDialog(val activity: BaseSimpleActivity, private val callee: String, private val callback: () -> Unit) {
    private var view = activity.layoutInflater.inflate(R.layout.dialog_call_confirmation, null)

    init {
        view.call_confirm_phone.applyColorFilter(activity.getProperTextColor())
        activity.getAlertDialogBuilder()
//            .setNegativeButton(R.string.cancel, null)
//            .setPositiveButton(R.string.call)  { dialog, _ ->
//                callback.invoke()
//                dialog.dismiss() }
            .apply {
                val title = String.format(activity.getString(R.string.confirm_calling_person), callee)
                activity.setupDialogStuff(view, this, titleText = title) { alertDialog ->
                    view.call_confirm_phone.apply {
                        startAnimation(AnimationUtils.loadAnimation(activity, R.anim.shake_pulse_animation))
                        setOnClickListener {
                            callback.invoke()
                            alertDialog.dismiss()
                        }
                    }
                    view.cancel_button.apply {
                        val drawable = resources.getColoredDrawableWithColor(R.drawable.button_gray_bg, 0xFFEB5545.toInt())
                        background = drawable
                        setPadding(2,2,2,2)
                        setOnClickListener {
                            alertDialog.dismiss()
                        }
                    }
                    view.call_button.apply {
                        val drawable = resources.getColoredDrawableWithColor(R.drawable.button_gray_bg, activity.baseConfig.accentColor)
                        background = drawable
                        setPadding(2,2,2,2)
                        setOnClickListener {
                            callback.invoke()
                            alertDialog.dismiss()
                        }
                    }
                }
            }
    }
}
