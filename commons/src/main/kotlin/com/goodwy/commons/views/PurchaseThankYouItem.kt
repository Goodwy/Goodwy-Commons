package com.goodwy.commons.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.goodwy.commons.R
import com.goodwy.commons.extensions.getColoredDrawableWithColor
import com.goodwy.commons.extensions.getContrastColor
import com.goodwy.commons.extensions.getProperPrimaryColor
import com.goodwy.commons.extensions.getProperTextColor
import com.goodwy.commons.extensions.getSurfaceColor

class PurchaseThankYouItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RelativeLayout(context, attrs, defStyle) {

    private var lifecycleOwner: LifecycleOwner? = null
//    private val hideGoogleRelations = resources.getBoolean(R.bool.hide_google_relations)
    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            updateVisibility()
        }
    }
    var onClick: (() -> Unit)? = null

    init {
        LayoutInflater
            .from(context)
            .inflate(R.layout.purchase_thank_you_view, this, true)

//        val activity = context.findActivity()
        updateVisibility()

        setOnClickListener {
            onClick?.invoke()
        }

        findViewById<AppCompatButton>(R.id.purchase_thank_you_more).setOnClickListener {
            onClick?.invoke()
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
        val primaryColor = context.getProperPrimaryColor()
        val textColor = context.getProperTextColor()

        val appDrawable =
            context.resources.getColoredDrawableWithColor(context, R.drawable.ic_plus_support, primaryColor)
        val appBg =
            context.resources.getColoredDrawableWithColor(context, R.drawable.button_white_bg_24dp, context.getSurfaceColor())
        findViewById<ImageView>(R.id.purchase_logo).apply {
            setImageDrawable(appDrawable)
            val isDev = context.packageName.startsWith("dev.")
            if (isDev) background = appBg
        }

        val drawable =
            context.resources.getColoredDrawableWithColor(context, R.drawable.button_gray_bg, primaryColor)
        findViewById<AppCompatButton>(R.id.purchase_thank_you_more).apply {
            background = drawable
            setTextColor(primaryColor.getContrastColor())
            setPadding(2, 2, 2, 2)
        }

        findViewById<MyTextView>(R.id.purchase_thank_you_title).setTextColor(textColor)
        findViewById<MyTextView>(R.id.purchase_thank_you_label).setTextColor(textColor)
    }
}
