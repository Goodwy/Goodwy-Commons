package com.goodwy.commons.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.biometric.auth.AuthPromptHost
import com.goodwy.commons.R
import com.goodwy.commons.adapters.AppLockAdapter
import com.goodwy.commons.databinding.ActivityAppLockBinding
import com.goodwy.commons.extensions.appLockManager
import com.goodwy.commons.extensions.baseConfig
import com.goodwy.commons.extensions.getProperBackgroundColor
import com.goodwy.commons.extensions.getThemeId
import com.goodwy.commons.extensions.isBiometricAuthSupported
import com.goodwy.commons.extensions.onGlobalLayout
import com.goodwy.commons.extensions.overrideActivityTransition
import com.goodwy.commons.extensions.viewBinding
import com.goodwy.commons.helpers.PROTECTION_FINGERPRINT
import com.goodwy.commons.helpers.isRPlus
import com.goodwy.commons.interfaces.HashListener

class AppLockActivity : EdgeToEdgeActivity(), HashListener {

    private val binding by viewBinding(ActivityAppLockBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        overrideActivityTransition()
        setupTheme()

        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupEdgeToEdge(padBottomSystem = listOf(binding.viewPager))
        onBackPressedDispatcher.addCallback(owner = this) {
            appLockManager.lock()
            finishAffinity()
        }

        setupViewPager()
    }

    private fun setupViewPager() {
        val adapter = AppLockAdapter(
            context = binding.root.context,
            requiredHash = baseConfig.appPasswordHash,
            hashListener = this,
            viewPager = binding.viewPager,
            biometricPromptHost = AuthPromptHost(this),
            showBiometricIdTab = isBiometricAuthSupported(),
            showBiometricAuthentication = baseConfig.appProtectionType == PROTECTION_FINGERPRINT && isRPlus()
        )

        binding.viewPager.apply {
            this.adapter = adapter
            isUserInputEnabled = false
            setCurrentItem(baseConfig.appProtectionType, false)
            onGlobalLayout {
                for (i in 0..2) {
                    adapter.isTabVisible(i, binding.viewPager.currentItem == i)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (appLockManager.isLocked()) {
            setupTheme()
        } else {
            finish()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        overrideActivityTransition()
    }

    override fun finish() {
        super.finish()
        overrideActivityTransition(exiting = true)
    }

    private fun setupTheme() {
        setTheme(getThemeId())
        with(getProperBackgroundColor()) {
            window.decorView.setBackgroundColor(this)
        }
    }

    private fun overrideActivityTransition(exiting: Boolean = false) {
        overrideActivityTransition(R.anim.fadein, R.anim.fadeout, exiting)
    }

    override fun receivedHash(hash: String, type: Int) {
        appLockManager.unlock()
        setResult(RESULT_OK)
        finish()
    }
}

fun Activity.maybeLaunchAppUnlockActivity(requestCode: Int) {
    if (appLockManager.isLocked()) {
        Intent(this, AppLockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivityForResult(this, requestCode)
        }
    }
}
