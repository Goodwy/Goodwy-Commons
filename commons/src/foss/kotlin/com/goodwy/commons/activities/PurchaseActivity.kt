package com.goodwy.commons.activities

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
    private var showLifebuoy = true

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun getRepositoryName() = null

    private val binding by viewBinding(ActivityPurchaseBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

//        setupEdgeToEdge(
//            padTopSystem = listOf(binding.proHolder)
//        )

        appName = intent.getStringExtra(APP_NAME) ?: ""
        primaryColor = getProperPrimaryColor()
        showLifebuoy = intent.getBooleanExtra(SHOW_LIFEBUOY, true)
    }

    override fun onResume() {
        super.onResume()
        updateTextColors(binding.purchaseCoordinator)

        val backgroundColor = getProperBackgroundColor()
        setupToolbar(binding.purchaseToolbar, NavigationIcon.Arrow)
        updateToolbarColors(binding.purchaseToolbar, backgroundColor, useOverflowIcon = false)

        val appDrawable = resources.getColoredDrawableWithColor(this, R.drawable.ic_plus_support, primaryColor)
        binding.appLogo.setImageDrawable(appDrawable)

        setupNoPlayStoreInstalled()
    }

    @Suppress("DEPRECATION")
    private fun setupNoPlayStoreInstalled() {
        val isProApp = resources.getBoolean(R.bool.is_pro_app)
        binding.proDonateText.text = Html.fromHtml(getString(stringsR.string.donate_text_no_gp_g))
        binding.proDonateButton.apply {
            setOnClickListener {
                launchViewIntent(getString(R.string.donate_url))
            }
            background.setTint(primaryColor)
        }
        binding.proSwitch.isChecked = baseConfig.isProNoGP
        binding.proSwitchHolder.setOnClickListener {
            binding.proSwitch.toggle()
            baseConfig.isProNoGP = binding.proSwitch.isChecked
        }
    }
}
