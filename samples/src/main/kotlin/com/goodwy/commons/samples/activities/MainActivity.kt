package com.goodwy.commons.samples.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.goodwy.commons.activities.BaseSimpleActivity
import com.goodwy.commons.activities.ManageBlockedNumbersActivity
import com.goodwy.commons.compose.alert_dialog.AlertDialogState
import com.goodwy.commons.compose.alert_dialog.rememberAlertDialogState
import com.goodwy.commons.compose.extensions.*
import com.goodwy.commons.compose.theme.AppThemeSurface
import com.goodwy.commons.dialogs.ConfirmationDialog
import com.goodwy.commons.dialogs.DonateAlertDialog
import com.goodwy.commons.dialogs.RateStarsAlertDialog
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.LICENSE_AUTOFITTEXTVIEW
import com.goodwy.commons.models.FAQItem
import com.goodwy.commons.samples.BuildConfig
import com.goodwy.commons.samples.R
import com.goodwy.commons.samples.screens.MainScreen

class MainActivity : BaseSimpleActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        appLaunched(BuildConfig.APPLICATION_ID)
        enableEdgeToEdgeSimple()
        setContent {
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
                    openTestButton = {
                        ConfirmationDialog(
                            this@MainActivity,
                            FAKE_VERSION_APP_LABEL,
                            positive = com.goodwy.commons.R.string.ok,
                            negative = 0
                        ) {
                            launchViewIntent(DEVELOPER_PLAY_STORE_URL)
                        }
                    },
                    showMoreApps = showMoreApps,
                    openAbout = ::launchAbout,
                    moreAppsFromUs = ::launchMoreAppsFromUsIntent,
                    startPurchaseActivity = ::launchPurchase,
                )
                AppLaunched()
            }
        }
    }

    @Composable
    private fun AppLaunched(
        donateAlertDialogState: AlertDialogState = getDonateAlertDialogState(),
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
    private fun getDonateAlertDialogState() =
        rememberAlertDialogState().apply {
            DialogMember {
                DonateAlertDialog(alertDialogState = this)
            }
        }

    @Composable
    private fun getRateStarsAlertDialogState() = rememberAlertDialogState().apply {
        DialogMember {
            RateStarsAlertDialog(alertDialogState = this, onRating = ::rateStarsRedirectAndThankYou)
        }
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
            playStoreInstalled = isPlayStoreInstalled(),
            ruStoreInstalled = isRuStoreInstalled(),
            showCollection = true
        )
    }

    private fun launchAbout() {
        val licenses = LICENSE_AUTOFITTEXTVIEW

        val faqItems = arrayListOf(
            FAQItem(com.goodwy.commons.R.string.faq_1_title_commons, com.goodwy.commons.R.string.faq_1_text_commons),
            FAQItem(com.goodwy.commons.R.string.faq_1_title_commons, com.goodwy.commons.R.string.faq_1_text_commons),
            FAQItem(com.goodwy.commons.R.string.faq_4_title_commons, com.goodwy.commons.R.string.faq_4_text_commons)
        )

        if (!resources.getBoolean(com.goodwy.commons.R.bool.hide_google_relations)) {
            faqItems.add(FAQItem(com.goodwy.commons.R.string.faq_2_title_commons, com.goodwy.commons.R.string.faq_2_text_commons))
            faqItems.add(FAQItem(com.goodwy.commons.R.string.faq_6_title_commons, com.goodwy.commons.R.string.faq_6_text_commons))
        }

        startAboutActivity(
            R.string.app_name_g,
            licenses,
            BuildConfig.VERSION_NAME,
            faqItems,
            true,
            arrayListOf("", "", ""), arrayListOf("", "", ""),
            arrayListOf("", "", ""), arrayListOf("", "", ""),
            arrayListOf("", "", ""), arrayListOf("", "", ""),
            playStoreInstalled = isPlayStoreInstalled(),
            ruStoreInstalled = isRuStoreInstalled())
    }

    override fun getAppLauncherName() = getString(R.string.commons_app_name)

    override fun getAppIconIDs() = arrayListOf(
        R.mipmap.ic_launcher,
    )
}
