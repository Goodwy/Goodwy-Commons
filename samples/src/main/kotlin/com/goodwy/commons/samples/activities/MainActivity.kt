package com.goodwy.commons.samples.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goodwy.commons.activities.BaseSimpleActivity
import com.goodwy.commons.activities.ManageBlockedNumbersActivity
import com.goodwy.commons.compose.alert_dialog.AlertDialogState
import com.goodwy.commons.compose.alert_dialog.rememberAlertDialogState
import com.goodwy.commons.compose.extensions.*
import com.goodwy.commons.compose.theme.AppThemeSurface
import com.goodwy.commons.dialogs.ChangeDateTimeFormatDialog
import com.goodwy.commons.dialogs.RateStarsAlertDialog
import com.goodwy.commons.dialogs.SecurityDialog
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.LICENSE_AUTOFITTEXTVIEW
import com.goodwy.commons.helpers.SHOW_ALL_TABS
import com.goodwy.commons.helpers.TIME_FORMAT_12
import com.goodwy.commons.helpers.TIME_FORMAT_24
import com.goodwy.commons.models.FAQItem
import com.goodwy.commons.samples.BuildConfig
import com.goodwy.commons.samples.R
import com.goodwy.commons.samples.screens.MainScreen

class MainActivity : BaseSimpleActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appLaunched(BuildConfig.APPLICATION_ID)
        enableEdgeToEdgeSimple()
        setContent {
            val isTopAppBarColorIcon by config.isTopAppBarColorIcon.collectAsStateWithLifecycle(initialValue = config.topAppBarColorIcon)
            val isDateFormat by config.isDateFormat.collectAsStateWithLifecycle(initialValue = config.dateFormat)
            val isUse24HourFormat by config.isUse24HourFormat.collectAsStateWithLifecycle(initialValue = config.use24HourFormat)
            val isTimeFormat = if (isUse24HourFormat) TIME_FORMAT_24 else TIME_FORMAT_12
            val useShamsi by config.isUseShamsi.collectAsStateWithLifecycle(initialValue = config.useShamsi)
            AppThemeSurface {
                val showMoreApps = onEventValue { !resources.getBoolean(com.goodwy.commons.R.bool.hide_google_relations) }

                MainScreen(
                    openColorCustomization = ::startCustomizationActivity,
                    manageBlockedNumbers = {
                        startActivity(Intent(this@MainActivity, ManageBlockedNumbersActivity::class.java))
                    },
                    showComposeDialogs = {
                        startActivity(Intent(this@MainActivity, TestDialogActivity::class.java))
                    },
                    openTestButton = ::securityDialog,//::setupStartDate,
                    showMoreApps = showMoreApps,
                    openAbout = ::launchAbout,
                    moreAppsFromUs = ::launchMoreAppsFromUs,
                    startPurchaseActivity = ::launchPurchase,
                    startTestActivity = {
                        startActivity(Intent(this@MainActivity, TestActivity::class.java))
                    },
                    isTopAppBarColorIcon = isTopAppBarColorIcon,
                    openDateButton = ::setupDateButton,
                    isDateFormat = isDateFormat,
                    isTimeFormat = isTimeFormat,
                    useShamsi = useShamsi,
                )
                AppLaunched()
            }
        }
    }

    @Composable
    private fun AppLaunched(
        rateStarsAlertDialogState: AlertDialogState = getRateStarsAlertDialogState(),
    ) {
        LaunchedEffect(Unit) {
            appLaunchedCompose(
                appId = BuildConfig.APPLICATION_ID,
                showRateUsDialog = rateStarsAlertDialogState::show,
            )
        }
    }

    @Composable
    private fun getRateStarsAlertDialogState() = rememberAlertDialogState().apply {
        DialogMember {
            RateStarsAlertDialog(alertDialogState = this, onRating = ::rateStarsRedirectAndThankYou)
        }
    }

    private fun startCustomizationActivity() {
        startCustomizationActivity(
            showAccentColor = true,
            isCollection = false,
            productIdList = arrayListOf("", "", ""),
            productIdListRu = arrayListOf("", "", ""),
            subscriptionIdList = arrayListOf("", "", ""),
            subscriptionIdListRu = arrayListOf("", "", ""),
            subscriptionYearIdList = arrayListOf("", "", ""),
            subscriptionYearIdListRu = arrayListOf("", "", ""),
            showAppIconColor = true
        )
    }

    private fun launchPurchase() {
        startPurchaseActivity(
            R.string.app_name_g,
            productIdList = arrayListOf("", "", ""),
            productIdListRu = arrayListOf("", "", ""),
            subscriptionIdList = arrayListOf("", "", ""),
            subscriptionIdListRu = arrayListOf("", "", ""),
            subscriptionYearIdList = arrayListOf("", "", ""),
            subscriptionYearIdListRu = arrayListOf("", "", ""),
            showLifebuoy = false,
            showCollection = true
        )
    }

    private fun launchAbout() {
        val licenses = LICENSE_AUTOFITTEXTVIEW

        val faqItems = arrayListOf(
            FAQItem(com.goodwy.commons.R.string.faq_1_title_commons, com.goodwy.commons.R.string.faq_1_text_commons),
            FAQItem(com.goodwy.commons.R.string.faq_4_title_commons, com.goodwy.commons.R.string.faq_4_text_commons)
        )

        if (!resources.getBoolean(com.goodwy.commons.R.bool.hide_google_relations)) {
            faqItems.add(FAQItem(com.goodwy.commons.R.string.faq_2_title_commons, com.goodwy.commons.R.string.faq_2_text_commons))
            faqItems.add(FAQItem(com.goodwy.commons.R.string.faq_6_title_commons, com.goodwy.commons.R.string.faq_6_text_commons))
        }

        val flavorName = BuildConfig.FLAVOR
        val storeDisplayName = when (flavorName) {
            "gplay" -> "Google Play"
            "foss" -> "FOSS"
            "rustore" -> "RuStore"
            else -> ""
        }
        val versionName = BuildConfig.VERSION_NAME
        val fullVersionText = "$versionName ($storeDisplayName)"
        startAboutActivity(
            R.string.app_name_g,
            licenses,
            fullVersionText,
            faqItems,
            true,
            arrayListOf("", "", ""), arrayListOf("", "", ""),
            arrayListOf("", "", ""), arrayListOf("", "", ""),
            arrayListOf("", "", ""), arrayListOf("", "", ""),
            flavorName,
            )
    }

    fun launchMoreAppsFromUs() {
        launchMoreAppsFromUsIntent(BuildConfig.FLAVOR)
    }

    private fun securityDialog() {
        val tabToShow = if (config.isAppPasswordProtectionOn) config.appProtectionType else SHOW_ALL_TABS
        SecurityDialog(this@MainActivity, config.appPasswordHash, tabToShow) { hash, type, success ->
            if (success) {
                val hasPasswordProtection = config.isAppPasswordProtectionOn
                config.isAppPasswordProtectionOn = !hasPasswordProtection
                config.appPasswordHash = if (hasPasswordProtection) "" else hash
                config.appProtectionType = type
            }
        }
    }

    private fun setupStartDate() {
        hideKeyboard()
        val datePicker = DatePickerDialog(
            this, getDatePickerDialogTheme(), startDateSetListener, 2024, 12, 30
        )

        datePicker.show()
    }

    private fun setupDateButton() {
        hideKeyboard()
        ChangeDateTimeFormatDialog(this, true) {}
    }

    private val startDateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
    }

    override fun getAppLauncherName() = getString(R.string.commons_app_name)

    override fun getAppIconIDs() = arrayListOf(
        R.mipmap.ic_launcher,
        R.mipmap.ic_launcher_one,
        R.mipmap.ic_launcher_two,
        R.mipmap.ic_launcher_three,
        R.mipmap.ic_launcher_four,
        R.mipmap.ic_launcher_five,
        R.mipmap.ic_launcher_six,
        R.mipmap.ic_launcher_seven,
        R.mipmap.ic_launcher_eight,
        R.mipmap.ic_launcher_nine,
        R.mipmap.ic_launcher_ten,
        R.mipmap.ic_launcher_eleven
    )

    override fun getRepositoryName() = "Gallery"
}
