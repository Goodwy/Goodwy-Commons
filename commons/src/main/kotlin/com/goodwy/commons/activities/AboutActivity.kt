package com.goodwy.commons.activities

import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_SUBJECT
import android.content.Intent.EXTRA_TEXT
import android.content.Intent.createChooser
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goodwy.commons.R
import com.goodwy.commons.compose.alert_dialog.rememberAlertDialogState
import com.goodwy.commons.compose.extensions.config
import com.goodwy.commons.compose.extensions.enableEdgeToEdgeSimple
import com.goodwy.commons.compose.extensions.rateStarsRedirectAndThankYou
import com.goodwy.commons.compose.screens.*
import com.goodwy.commons.compose.theme.AppThemeSurface
import com.goodwy.commons.dialogs.ConfirmationAdvancedAlertDialog
import com.goodwy.commons.dialogs.RateStarsAlertDialog
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.*
import com.goodwy.commons.models.FAQItem

class AboutActivity : BaseComposeActivity() {
    private val appName get() = intent.getStringExtra(APP_NAME) ?: ""

    private var firstVersionClickTS = 0L
    private var clicksSinceFirstClick = 0

    companion object {
        private const val EASTER_EGG_TIME_LIMIT = 8000L
        private const val EASTER_EGG_REQUIRED_CLICKS = 7
        private const val EASTER_EGG_REQUIRED_CLICKS_NEXT = 10
    }

    fun getFlavorName() = intent.getStringExtra(APP_FLAVOR_NAME) ?: "gplay"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeSimple()
        setContent {
            val isTopAppBarColorIcon by config.isTopAppBarColorIcon.collectAsStateWithLifecycle(initialValue = config.topAppBarColorIcon)
            val isTopAppBarColorTitle by config.isTopAppBarColorTitle.collectAsStateWithLifecycle(initialValue = config.topAppBarColorTitle)
            AppThemeSurface {
                val rateStarsAlertDialogState = getRateStarsAlertDialogState()
                val onRateUsClickAlertDialogState = getOnRateUsClickAlertDialogState(rateStarsAlertDialogState::show)
                AboutScreen(
                    goBack = ::finish,
                    aboutSection = {
                        AboutNewSection(
                            appName = appName,
                            appVersion = intent.getStringExtra(APP_VERSION_NAME) ?: "",
                            appFlavor = getFlavorName(),
                            onRateUsClick = {
                                onRateUsClick(
                                    showConfirmationAdvancedDialog = onRateUsClickAlertDialogState::show,
                                    showRateStarsDialog = rateStarsAlertDialogState::show
                                )
                            },
                            onMoreAppsClick = ::launchMoreAppsFromUs,
                            onPrivacyPolicyClick = ::onPrivacyPolicyClick,
                            onFAQClick = ::launchFAQActivity,
                            onTipJarClick = ::onTipJarClick,
                            onGithubClick = ::onGithubClick,
                            onPatreonClick = ::onPatreonClick,
                            onBuyMeaCoffeeClick = ::onBuyMeaCoffeeClick,
                            onWebsiteClick = ::onWebsiteClick,
                            showGithub = showGithub(),
                            onLicenseClick = ::onLicenseClick,
                            onContributorsClick = ::onContributorsClick,
                            onVersionClick = ::onVersionClick,
                        )
                    },
                    onInviteClick = ::onInviteClick,
                    onKnownIssuesClick = ::launchIssueTracker,
                    isTopAppBarColorIcon = isTopAppBarColorIcon,
                    isTopAppBarColorTitle = isTopAppBarColorTitle,
                )
            }
        }
    }

    @Composable
    private fun getRateStarsAlertDialogState() =
        rememberAlertDialogState().apply {
            DialogMember {
                RateStarsAlertDialog(
                    alertDialogState = this,
                    onRating = ::rateStarsRedirectAndThankYou
                )
            }
        }

    @Composable
    private fun getOnRateUsClickAlertDialogState(showRateStarsDialog: () -> Unit) =
        rememberAlertDialogState().apply {
            DialogMember {
                ConfirmationAdvancedAlertDialog(
                    alertDialogState = this,
                    message = "${getString(R.string.before_asking_question_read_faq)}\n\n${getString(R.string.make_sure_latest)}",
                    messageId = null,
                    positive = R.string.read_faq,
                    negative = R.string.skip
                ) { success ->
                    if (success) {
                        launchFAQActivity()
                    } else {
                        launchRateUsPrompt(showRateStarsDialog)
                    }
                }
            }
        }

    private fun launchFAQActivity() {
        val faqItems = intent.getSerializableExtra(APP_FAQ) as ArrayList<FAQItem>
        Intent(applicationContext, FAQActivity::class.java).apply {
            putExtra(
                APP_ICON_IDS,
                intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList<String>()
            )
            putExtra(APP_LAUNCHER_NAME, intent.getStringExtra(APP_LAUNCHER_NAME) ?: "")
            putExtra(APP_FAQ, faqItems)
            startActivity(this)
        }
    }

    fun launchMoreAppsFromUs() {
        launchMoreAppsFromUsIntent(getFlavorName())
    }

    private fun onRateUsClick(
        showConfirmationAdvancedDialog: () -> Unit,
        showRateStarsDialog: () -> Unit,
    ) {
        if (baseConfig.wasBeforeRateShown) {
            launchRateUsPrompt(showRateStarsDialog)
        } else {
            baseConfig.wasBeforeRateShown = true
            showConfirmationAdvancedDialog()
        }
    }

    private fun launchRateUsPrompt(
        showRateStarsDialog: () -> Unit,
    ) {
        if (baseConfig.wasAppRated) {
            launchAppRatingPage()
        } else {
            showRateStarsDialog()
        }
    }

    private fun onPrivacyPolicyClick() {
        val appId = baseConfig.appId.removePrefix("com.").removePrefix("dev.").removeSuffix(".debug")
        val url = when (appId) {
            "goodwy.dialer" -> "https://sites.google.com/view/goodwy/about/privacy-policy-right-dialer"
            "goodwy.smsmessenger" -> "https://sites.google.com/view/goodwy/about/privacy-policy-right-messages"
            "goodwy.contacts" -> "https://sites.google.com/view/goodwy/about/privacy-policy-right-contacts"
            "goodwy.gallery" -> "https://sites.google.com/view/goodwy/about/privacy-policy-right-gallery"
            "goodwy.filemanager" -> "https://sites.google.com/view/goodwy/about/privacy-policy-right-files"
            "goodwy.voicerecorder", "goodwy.voicerecorderfree" -> "https://sites.google.com/view/goodwy/about/privacy-policy-right-voice-recorder"
            "goodwy.calendar" -> "https://sites.google.com/view/goodwy/about/privacy-policy-right-calendar"
            else -> "https://sites.google.com/view/goodwy/about/privacy-policy"
        }
        launchViewIntent(url)
    }

    private fun onTipJarClick() {
        Intent(applicationContext, PurchaseActivity::class.java).apply {
            putExtra(APP_ICON_IDS, intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList<String>())
            putExtra(APP_LAUNCHER_NAME, intent.getStringExtra(APP_LAUNCHER_NAME) ?: "")
            putExtra(APP_NAME, intent.getStringExtra(APP_NAME) ?: "")
            putExtra(PRODUCT_ID_LIST, intent.getStringArrayListExtra(PRODUCT_ID_LIST) ?: arrayListOf("", "", ""))
            putExtra(PRODUCT_ID_LIST_RU, intent.getStringArrayListExtra(PRODUCT_ID_LIST_RU) ?: arrayListOf("", "", ""))
            putExtra(SUBSCRIPTION_ID_LIST, intent.getStringArrayListExtra(SUBSCRIPTION_ID_LIST) ?: arrayListOf("", "", ""))
            putExtra(SUBSCRIPTION_ID_LIST_RU, intent.getStringArrayListExtra(SUBSCRIPTION_ID_LIST_RU) ?: arrayListOf("", "", ""))
            putExtra(SUBSCRIPTION_YEAR_ID_LIST, intent.getStringArrayListExtra(SUBSCRIPTION_YEAR_ID_LIST) ?: arrayListOf("", "", ""))
            putExtra(SUBSCRIPTION_YEAR_ID_LIST_RU, intent.getStringArrayListExtra(SUBSCRIPTION_YEAR_ID_LIST_RU) ?: arrayListOf("", "", ""))
            putExtra(SHOW_LIFEBUOY, resources.getBoolean(R.bool.show_lifebuoy))
            putExtra(SHOW_COLLECTION, resources.getBoolean(R.bool.show_collection))
            startActivity(this)
        }
    }

    private fun onPatreonClick() {
        launchViewIntent("https://www.patreon.com/cw/Goodwy")
    }

    private fun onBuyMeaCoffeeClick() {
        launchViewIntent("https://buymeacoffee.com/goodwy")
    }

    private fun onWebsiteClick() {
        launchViewIntent(getString(R.string.my_website))
    }

    private fun onGithubClick() {
        launchViewIntent(getGithubUrl())
    }

    @Composable
    private fun showGithub() =
        remember { !intent.getStringExtra(APP_REPOSITORY_NAME).isNullOrEmpty() }

    private fun getGithubUrl(): String {
        val repositoryName = intent.getStringExtra(APP_REPOSITORY_NAME)
        return "https://github.com/Goodwy/$repositoryName"
    }

    private fun launchIssueTracker() {
        launchViewIntent(
            "${getGithubUrl()}/issues?q=is:open+is:issue+label:bug"
        )
    }

    private fun onInviteClick() {
        val storeUrl = when(getFlavorName()) {
            "foss" -> getGithubUrl()
            "rustore" -> getRuStoreUrl()
            else -> getStoreUrl()
        }

        val text = String.format(getString(R.string.share_text), appName, storeUrl)
        Intent().apply {
            action = ACTION_SEND
            putExtra(EXTRA_SUBJECT, appName)
            putExtra(EXTRA_TEXT, text)
            type = "text/plain"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(createChooser(this, getString(R.string.invite_via)))
        }
    }

    private fun onContributorsClick() {
        val intent = Intent(applicationContext, ContributorsActivity::class.java)
        startActivity(intent)
    }

    private fun onLicenseClick() {
        Intent(applicationContext, LicenseActivity::class.java).apply {
            putExtra(
                APP_ICON_IDS,
                intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList<String>()
            )
            putExtra(APP_LAUNCHER_NAME, intent.getStringExtra(APP_LAUNCHER_NAME) ?: "")
            putExtra(APP_LICENSES, intent.getLongExtra(APP_LICENSES, 0))
            startActivity(this)
        }
    }

    private fun onVersionClick() {
        if (firstVersionClickTS == 0L) {
            firstVersionClickTS = System.currentTimeMillis()
            Handler(Looper.getMainLooper()).postDelayed({
                firstVersionClickTS = 0L
                clicksSinceFirstClick = 0
            }, EASTER_EGG_TIME_LIMIT)
        }

        clicksSinceFirstClick++
        if (clicksSinceFirstClick == EASTER_EGG_REQUIRED_CLICKS) {
            toast(R.string.hello)
        } else if (clicksSinceFirstClick >= EASTER_EGG_REQUIRED_CLICKS_NEXT) {
            val appVersion = intent.getStringExtra(APP_VERSION_NAME) ?: ""
            toast("Version: $appVersion")
            firstVersionClickTS = 0L
            clicksSinceFirstClick = 0
        }
    }
}
