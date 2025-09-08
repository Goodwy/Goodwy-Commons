package com.goodwy.commons.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.goodwy.commons.R
import com.goodwy.commons.extensions.beGoneIf
import com.goodwy.commons.extensions.findActivity
import com.goodwy.commons.extensions.isOrWasThankYouInstalled
import com.goodwy.commons.extensions.launchViewIntent
import com.goodwy.commons.extensions.toast

class PurchaseThankYouItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RelativeLayout(context, attrs, defStyle) {

    private var lifecycleOwner: LifecycleOwner? = null
    private val hideGoogleRelations = resources.getBoolean(R.bool.hide_google_relations)
    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            updateVisibility()
        }
    }

    init {
        LayoutInflater
            .from(context)
            .inflate(R.layout.purchase_thank_you_view, this, true)

        val title = context.getString(R.string.purchase_simple_thank_you)
        val subtitle = context.getText(R.string.enables_more_customization)
        findViewById<MyTextView>(R.id.purchase_thank_you_label).text = title
        findViewById<MyTextView>(R.id.purchase_thank_you_value).text = subtitle
        updateVisibility()

        if (!context.isOrWasThankYouInstalled(allowPretend = false)) {
            setOnClickListener {
                val activity = context.findActivity()
                if (activity != null) {
                    activity.launchViewIntent(activity.getString(R.string.donate_url))
                } else {
                    context.toast(R.string.unknown_error_occurred)
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        lifecycleOwner = findViewTreeLifecycleOwner()
        lifecycleOwner?.lifecycle?.addObserver(lifecycleObserver)
    }

    override fun onDetachedFromWindow() {
        lifecycleOwner?.lifecycle?.removeObserver(lifecycleObserver)
        lifecycleOwner = null
        super.onDetachedFromWindow()
    }

    fun updateVisibility() {
        beGoneIf(
            context.isOrWasThankYouInstalled(allowPretend = false) || hideGoogleRelations
        )
    }
}
