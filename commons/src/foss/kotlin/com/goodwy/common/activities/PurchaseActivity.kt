package com.goodwy.commons.activities

import android.content.Intent
import android.os.Bundle
import android.text.Html
import com.goodwy.commons.R
import com.goodwy.commons.databinding.ActivityPurchaseBinding
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.*
import com.goodwy.strings.R as stringsR

class PurchaseActivity : BaseSimpleActivity() {

    private var appName = ""
    private var primaryColor = 0

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun getRepositoryName() = null

    private val binding by viewBinding(ActivityPurchaseBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = false
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        appName = intent.getStringExtra(APP_NAME) ?: ""
        primaryColor = getProperPrimaryColor()

        // TODO TRANSPARENT Navigation Bar
        setWindowTransparency(true) { _, _, leftNavigationBarSize, rightNavigationBarSize ->
            binding.purchaseCoordinator.setPadding(leftNavigationBarSize, 0, rightNavigationBarSize, 0)
            updateNavigationBarColor(getProperBackgroundColor())
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        updateTextColors(binding.purchaseCoordinator)
        setupToolbar(binding.purchaseToolbar, NavigationIcon.Arrow)
        val backgroundColor = getProperBackgroundColor()
        binding.collapsingToolbar.setBackgroundColor(backgroundColor)
        updateTopBarColors(binding.purchaseToolbar, backgroundColor, useOverflowIcon = false)
        setupNoPlayStoreInstalled()
    }

    @Suppress("DEPRECATION")
    private fun setupNoPlayStoreInstalled() {
        val isProApp = resources.getBoolean(R.bool.is_pro_app)
        binding.proDonateText.text = Html.fromHtml(getString(stringsR.string.donate_text_g))
        binding.proDonateButton.apply {
            setOnClickListener {
                launchViewIntent("https://www.goodwy.dev/support-project")
            }
            background.setTint(primaryColor)
        }
        if (isProApp) {
            binding.proUnlockText.beGone()
            binding.proSwitchHolder.beGone()
        } else {
            binding.proSwitch.isChecked = baseConfig.isProNoGP
            binding.proSwitchHolder.setOnClickListener {
                binding.proSwitch.toggle()
                baseConfig.isProNoGP = binding.proSwitch.isChecked
            }
        }
    }
}
