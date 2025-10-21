package com.goodwy.commons.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.TextView
import androidx.biometric.auth.AuthPromptHost
import androidx.core.os.postDelayed
import androidx.core.widget.TextViewCompat
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.listener.PatternLockViewListener
import com.andrognito.patternlockview.utils.PatternLockUtils
import com.goodwy.commons.R
import com.goodwy.commons.databinding.TabPatternBinding
import com.goodwy.commons.extensions.getProperPrimaryColor
import com.goodwy.commons.extensions.getProperTextColor
import com.goodwy.commons.extensions.performHapticFeedback
import com.goodwy.commons.extensions.updateTextColors
import com.goodwy.commons.helpers.PROTECTION_PATTERN
import com.goodwy.commons.interfaces.BaseSecurityTab
import com.goodwy.commons.interfaces.HashListener

class PatternTab(context: Context, attrs: AttributeSet) : BaseSecurityTab(context, attrs) {
    private var scrollView: MyScrollView? = null

    private lateinit var binding: TabPatternBinding

    override val protectionType = PROTECTION_PATTERN
    override val defaultTextRes = R.string.insert_pattern
    override val wrongTextRes = R.string.wrong_pattern
    override val titleTextView: TextView
        get() = binding.patternLockTitle

    @SuppressLint("ClickableViewAccessibility")
    override fun onFinishInflate() {
        super.onFinishInflate()
        binding = TabPatternBinding.bind(this)

        val textColor = context.getProperTextColor()
        context.updateTextColors(binding.patternLockHolder)

        binding.patternLockView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> scrollView?.isScrollable = false
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> scrollView?.isScrollable = true
            }
            false
        }

        binding.patternLockView.correctStateColor = context.getProperPrimaryColor()
        binding.patternLockView.normalStateColor = textColor
        binding.patternLockView.addPatternLockListener(object : PatternLockViewListener {
            override fun onComplete(pattern: MutableList<PatternLockView.Dot>?) {
                receivedHash(PatternLockUtils.patternToSha1(binding.patternLockView, pattern))
            }

            override fun onCleared() {}

            override fun onStarted() {}

            override fun onProgress(progressPattern: MutableList<PatternLockView.Dot>?) {}
        })

        TextViewCompat.setCompoundDrawableTintList(binding.patternLockTitle, ColorStateList.valueOf(textColor))
        maybeShowCountdown()
    }

    override fun initTab(
        requiredHash: String,
        listener: HashListener,
        scrollView: MyScrollView?,
        biometricPromptHost: AuthPromptHost,
        showBiometricAuthentication: Boolean
    ) {
        this.requiredHash = requiredHash
        this.scrollView = scrollView
        computedHash = requiredHash
        hashListener = listener
        if (computedHash != "") binding.patternLockTitle.setText(com.goodwy.strings.R.string.enter_pattern)
    }

    override fun onLockedOutChange(lockedOut: Boolean) {
        binding.patternLockView.isInputEnabled = !lockedOut
    }

    private fun receivedHash(newHash: String) {
        if (isLockedOut()) {
            performHapticFeedback()
            return
        }

        when {
            computedHash.isEmpty() -> {
                computedHash = newHash
                binding.patternLockView.clearPattern()
                binding.patternLockTitle.setText(R.string.repeat_pattern)
            }

            computedHash == newHash -> {
                binding.patternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT)
                onCorrectPassword()
            }

            else -> {
                onIncorrectPassword()
                binding.patternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG)
                binding.patternLockView.isInputEnabled = false
                Handler(Looper.getMainLooper()).postDelayed(WRONG_PATTERN_CLEAR_DELAY) {
                    binding.patternLockView.clearPattern()
                    binding.patternLockView.isInputEnabled = !isLockedOut()
                    if (requiredHash.isEmpty()) {
                        computedHash = ""
                    }
                }
            }
        }
    }

    companion object {
        private const val WRONG_PATTERN_CLEAR_DELAY = 300L
    }
}
