package com.goodwy.commons.interfaces

import androidx.biometric.auth.AuthPromptHost
import com.goodwy.commons.views.MyScrollView

interface SecurityTab {
    fun initTab(
        requiredHash: String,
        listener: HashListener,
        scrollView: MyScrollView?,
        biometricPromptHost: AuthPromptHost,
        showBiometricAuthentication: Boolean
    )

    fun visibilityChanged(isVisible: Boolean)
}
