package com.goodwy.commons.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.TextView
import android.widget.Toast
import androidx.biometric.auth.AuthPromptHost
import androidx.core.widget.TextViewCompat
import com.goodwy.commons.R
import com.goodwy.commons.databinding.TabPinBinding
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.MINIMUM_PIN_LENGTH
import com.goodwy.commons.helpers.PROTECTION_PIN
import com.goodwy.commons.interfaces.BaseSecurityTab
import com.goodwy.commons.interfaces.HashListener
import java.math.BigInteger
import java.security.MessageDigest
import java.util.Locale

class PinTab(context: Context, attrs: AttributeSet) : BaseSecurityTab(context, attrs) {
    private var pin = ""
    private var autoConfirm = true
    private var correctPassword = false

    private lateinit var binding: TabPinBinding

    override val protectionType = PROTECTION_PIN
    override val defaultTextRes = R.string.enter_pin
    override val wrongTextRes = R.string.wrong_pin
    override val titleTextView: TextView
        get() = binding.pinLockTitle

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding = TabPinBinding.bind(this)

        val textColor = context.getProperTextColor()
        context.updateTextColors(binding.pinLockHolder)

        binding.pin0.setOnClickListener { addNumber("0") }
        binding.pin1.setOnClickListener { addNumber("1") }
        binding.pin2.setOnClickListener { addNumber("2") }
        binding.pin3.setOnClickListener { addNumber("3") }
        binding.pin4.setOnClickListener { addNumber("4") }
        binding.pin5.setOnClickListener { addNumber("5") }
        binding.pin6.setOnClickListener { addNumber("6") }
        binding.pin7.setOnClickListener { addNumber("7") }
        binding.pin8.setOnClickListener { addNumber("8") }
        binding.pin9.setOnClickListener { addNumber("9") }
        binding.pinC.setOnClickListener { clear() }
        binding.pinC.setOnLongClickListener { resetPin(); true }
        binding.pinC.applyColorFilter(textColor)
        binding.pinOk.setOnClickListener { confirmPIN() }
        binding.pinOk.applyColorFilter(textColor)

        TextViewCompat.setCompoundDrawableTintList(binding.pinLockTitle, ColorStateList.valueOf(textColor))
        maybeShowCountdown()

        arrayOf(
            binding.pin1, binding.pin2, binding.pin3,
            binding.pin4, binding.pin5, binding.pin6,
            binding.pin7, binding.pin8, binding.pin9,
            binding.pin0//, binding.pinC, binding.pinOk
        ).forEach {
            if (context.isDynamicTheme()) {
                if (context.isSystemInDarkMode()) it.background.applyColorFilter(context.getProperBackgroundColor())
                else it.background.applyColorFilter(context.getSurfaceColor())
            } else if (context.isBlackTheme()) {
                it.background.applyColorFilter(context.getProperBackgroundColor())
            } else {
                it.background.applyColorFilter(context.getSurfaceColor())
            }
        }
    }

    override fun initTab(
        requiredHash: String,
        listener: HashListener,
        scrollView: MyScrollView?,
        biometricPromptHost: AuthPromptHost,
        showBiometricAuthentication: Boolean
    ) {
        this.requiredHash = requiredHash
        computedHash = requiredHash
        hashListener = listener
    }

    private fun addNumber(number: String) {
        if (!isLockedOut() && !correctPassword) {
            if (pin.length < 10) {
                pin += number
                updatePinCode()
            }
        }

        performHapticFeedback()
    }

    private fun clear() {
        if (pin.isNotEmpty()) {
            pin = pin.substring(0, pin.length - 1)
            updatePinCode()
        }
        performHapticFeedback()
    }

    private fun confirmPIN() {
        if (!isLockedOut()) {
            val newHash = getHashedPin()
            when {
                pin.isEmpty() -> {
                    context.toast(id = R.string.please_enter_pin, length = Toast.LENGTH_LONG)
                }

                computedHash.isEmpty() && pin.length < MINIMUM_PIN_LENGTH -> {
                    resetPin()
                    context.toast(id = R.string.pin_must_be_4_digits_long, length = Toast.LENGTH_LONG)
                }

                computedHash.isEmpty() -> {
                    autoConfirm = false
                    computedHash = newHash
                    resetPin()
                    binding.pinLockTitle.setText(R.string.repeat_pin)
                }

                computedHash == newHash -> {
                    onCorrectPassword()
                }

                else -> {
                    resetPin()
                    onIncorrectPassword()
                    if (requiredHash.isEmpty()) {
                        computedHash = ""
                    }
                }
            }
        }

        performHapticFeedback()
    }

    private fun resetPin() {
        pin = ""
        binding.pinLockCurrentPin.text = ""
        updateButton()
    }

    private fun updatePinCode() {
        binding.pinLockCurrentPin.text = "*".repeat(pin.length)
        updateButton()

        if (autoConfirm && !isLockedOut() && computedHash.isNotEmpty()) {
            val newHash = getHashedPin()
            if (computedHash == newHash) {
                correctPassword = true
                val getProperPrimaryColor = context.getProperPrimaryColor()
                binding.pinLockCurrentPin.setColors(getProperPrimaryColor, getProperPrimaryColor, getProperPrimaryColor)
                onCorrectPassword()
            }
        }
    }

    private fun updateButton() {
        if (pin.isNotEmpty()) {
            binding.pinOk.beVisible()
            binding.pinC.beVisible()
        } else {
            binding.pinOk.beInvisible()
            binding.pinC.beInvisible()
        }
    }

    private fun getHashedPin(): String {
        val messageDigest = MessageDigest.getInstance("SHA-1")
        messageDigest.update(pin.toByteArray(charset("UTF-8")))
        val digest = messageDigest.digest()
        val bigInteger = BigInteger(1, digest)
        return String.format(Locale.getDefault(), "%0${digest.size * 2}x", bigInteger).lowercase(Locale.getDefault())
    }
}
