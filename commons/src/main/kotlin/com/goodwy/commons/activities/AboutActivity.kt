package com.goodwy.commons.activities

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.*
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import androidx.core.net.toUri
import androidx.core.view.isEmpty
import com.goodwy.commons.R
import com.goodwy.commons.dialogs.BottomSheetChooserDialog
import com.goodwy.commons.dialogs.ConfirmationAdvancedDialog
import com.goodwy.commons.dialogs.ConfirmationDialog
import com.goodwy.commons.dialogs.RateStarsDialog
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.*
import com.goodwy.commons.models.FAQItem
import com.goodwy.commons.models.SimpleListItem
import kotlinx.android.synthetic.main.activity_about.*
import java.util.*

class AboutActivity : BaseSimpleActivity() {
    private var appName = ""
    private var primaryColor = 0
    private var textColor = 0
    private var backgroundColor = 0
    private var inflater: LayoutInflater? = null
    private var licensingKey = ""
    private var productIdX1 = ""
    private var productIdX2 = ""
    private var productIdX3 = ""

    private var firstVersionClickTS = 0L
    private var clicksSinceFirstClick = 0
    private val EASTER_EGG_TIME_LIMIT = 3000L
    private val EASTER_EGG_REQUIRED_CLICKS = 7

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        primaryColor = getProperPrimaryColor()
        textColor = getProperTextColor()
        backgroundColor = getProperBackgroundColor()
        inflater = LayoutInflater.from(this)

        updateMaterialActivityViews(about_coordinator, about_holder, useTransparentNavigation = true, useTopSearchMenu = false)
        setupMaterialScrollListener(about_nested_scrollview, about_toolbar)

        appName = intent.getStringExtra(APP_NAME) ?: ""
        licensingKey = intent.getStringExtra(GOOGLE_PLAY_LICENSING_KEY) ?: ""
        productIdX1 = intent.getStringExtra(PRODUCT_ID_X1) ?: ""
        productIdX2 = intent.getStringExtra(PRODUCT_ID_X2) ?: ""
        productIdX3 = intent.getStringExtra(PRODUCT_ID_X3) ?: ""
    }

    override fun onResume() {
        super.onResume()
        updateTextColors(about_nested_scrollview)
        setupOptionsMenu()
        setupToolbar(about_toolbar, NavigationIcon.Arrow)

        setupAboutApp()

        setupRateUs()
        setupMoreApps()
        setupPrivacyPolicy()
        setupFAQ()
        setupTipJar()
        setupCollection()

        setupWebsite()
        setupEmail()
        setupUpgradeToPro()
        setupInvite()
        setupLicense()
        setupFacebook()
        setupReddit()
        setupCopyright()
    }

    private fun setupOptionsMenu() {
        about_toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.share -> {
                    launchShare()
                    true
                }
                else -> false
            }
        }
    }

    private fun launchShare() {
            val text = String.format(getString(R.string.share_text), appName, getStoreUrl())
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_SUBJECT, appName)
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
                startActivity(Intent.createChooser(this, getString(R.string.invite_via)))
            }
    }

    private fun setupWebsite() {
        val websiteText = String.format(getString(R.string.two_string_placeholder), getString(R.string.website_label), getString(R.string.my_website))
        about_website.text = websiteText
    }

    private fun setupEmail() {
        val label = getString(R.string.email_label)
        val email = getString(R.string.my_email)

        val appVersion = String.format(getString(R.string.app_version, intent.getStringExtra(APP_VERSION_NAME)))
        val deviceOS = String.format(getString(R.string.device_os), Build.VERSION.RELEASE)
        val newline = "%0D%0A"
        val separator = "------------------------------"
        val body = "$appVersion$newline$deviceOS$newline$separator$newline$newline"
        val href = "$label<br><a href=\"mailto:$email?subject=$appName&body=$body\">$email</a>"
        about_email.text = Html.fromHtml(href)

        if (intent.getBooleanExtra(SHOW_FAQ_BEFORE_MAIL, false) && !baseConfig.wasBeforeAskingShown) {
            about_email.setOnClickListener {
                baseConfig.wasBeforeAskingShown = true
                about_email.movementMethod = LinkMovementMethod.getInstance()
                about_email.setOnClickListener(null)
                val msg = "${getString(R.string.before_asking_question_read_faq)}\n\n${getString(R.string.make_sure_latest)}"
                ConfirmationDialog(this, msg, 0, R.string.read_faq, R.string.skip) {
                    faqButton.performClick()
                }
            }
        } else {
            about_email.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun setupUpgradeToPro() {
        about_upgrade_to_pro.beVisibleIf(false) // getCanAppBeUpgraded()
        about_upgrade_to_pro.setOnClickListener {
            launchUpgradeToProIntent()
        }

        about_upgrade_to_pro.setTextColor(primaryColor)
        about_upgrade_to_pro.underlineText()
    }

    private fun openFAQ(faqItems: ArrayList<FAQItem>) {
        Intent(applicationContext, FAQActivity::class.java).apply {
            putExtra(APP_ICON_IDS, getAppIconIDs())
            putExtra(APP_LAUNCHER_NAME, getAppLauncherName())
            putExtra(APP_FAQ, faqItems)
            startActivity(this)
        }
    }

    private fun setupInvite() {
        about_invite.setOnClickListener {
            val text = String.format(getString(R.string.share_text), appName, getStoreUrl())
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_SUBJECT, appName)
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
                startActivity(Intent.createChooser(this, getString(R.string.invite_via)))
            }
        }
        about_invite.setTextColor(primaryColor)
    }

    private fun launchEmailIntent() {
        val appVersion = String.format(getString(R.string.app_version, intent.getStringExtra(APP_VERSION_NAME)))
        val deviceOS = String.format(getString(R.string.device_os), Build.VERSION.RELEASE)
        val newline = "\n"
        val separator = "------------------------------"
        val body = "$appVersion$newline$deviceOS$newline$separator$newline$newline"

        val address = if (packageName.startsWith("com.goodwy")) {
            getString(R.string.my_email)
        } else {
            getString(R.string.my_fake_email)
        }

        val selectorIntent = Intent(ACTION_SENDTO)
            .setData("mailto:$address".toUri())
        val emailIntent = Intent(ACTION_SEND).apply {
            putExtra(EXTRA_EMAIL, arrayOf(address))
            putExtra(EXTRA_SUBJECT, appName)
            putExtra(EXTRA_TEXT, body)
            selector = selectorIntent
        }

        try {
            startActivity(emailIntent)
        } catch (e: ActivityNotFoundException) {
            val chooser = createChooser(emailIntent, getString(R.string.send_email))
            try {
                startActivity(chooser)
            } catch (e: Exception) {
                toast(R.string.no_email_client_found)
            }
        } catch (e: Exception) {
            showErrorToast(e)
        }
    }

    @SuppressLint("NewApi", "UseCompatTextViewDrawableApis")
    private fun setupRateUs() {
        /* if (baseConfig.appRunCount < 5) {
             about_rate_us.visibility = View.GONE
         } else {*/
        rateButton.setOnClickListener {
            if (baseConfig.wasBeforeRateShown) {
                if (baseConfig.wasAppRated) {
                    redirectToRateUs()
                } else {
                    RateStarsDialog(this)
                }
            } else {
                baseConfig.wasBeforeRateShown = true
                val msg = "${getString(R.string.before_rate_read_faq)}\n\n${getString(R.string.make_sure_latest)}"
                ConfirmationAdvancedDialog(this, msg, 0, R.string.read_faq, R.string.skip) {
                    if (it) {
                        faqButton.performClick()
                    } else {
                        rateButton.performClick()
                    }
                }
            }
        }
        // }

        rateButton.setTextColor(getProperTextColor())
        rateButton.background = resources.getColoredDrawableWithColor(R.drawable.button_gray_bg, getBottomNavigationBackgroundColor())
        rateButton.compoundDrawableTintList = ColorStateList.valueOf(getProperTextColor())
        /*rateButton.background = resources.getDrawable(R.drawable.button_gray_bg)
        rateButton.setBackgroundColor(baseConfig.primaryColor)
        rateButton.backgroundTintMode*/
       // rateButton.setBackgroundColor(primaryColor)
    }

    @SuppressLint("NewApi", "UseCompatTextViewDrawableApis")
    private fun setupMoreApps() {
        moreButton.setOnClickListener {
            launchMoreAppsFromUsIntent()
        }
        moreButton.setTextColor(getProperTextColor())
        moreButton.background = resources.getColoredDrawableWithColor(R.drawable.button_gray_bg, getBottomNavigationBackgroundColor())
        moreButton.compoundDrawableTintList = ColorStateList.valueOf(getProperTextColor())
    }

    @SuppressLint("NewApi", "UseCompatTextViewDrawableApis")
    private fun setupPrivacyPolicy() {
        privacyButton.setOnClickListener {
            val appId = baseConfig.appId.removeSuffix(".debug")
            val url = when (appId) {
                "com.goodwy.smsmessenger" -> "https://sites.google.com/view/goodwy/about/privacy-policy-right-messages"
                "com.goodwy.contacts" -> "https://sites.google.com/view/goodwy/about/privacy-policy-right-contacts"
                else -> "https://sites.google.com/view/goodwy/about/privacy-policy"
            }
            launchViewIntent(url)
        }
        privacyButton.setTextColor(getProperTextColor())
        privacyButton.background = resources.getColoredDrawableWithColor(R.drawable.button_gray_bg, getBottomNavigationBackgroundColor())
        privacyButton.compoundDrawableTintList = ColorStateList.valueOf(getProperTextColor())
    }

    @SuppressLint("NewApi", "UseCompatTextViewDrawableApis")
    private fun setupFAQ() {
        val faqItems = intent.getSerializableExtra(APP_FAQ) as ArrayList<FAQItem>
        faqButton.setOnClickListener {
            openFAQ(faqItems)
        }
        faqButton.setTextColor(getProperTextColor())
        faqButton.background = resources.getColoredDrawableWithColor(R.drawable.button_gray_bg, getBottomNavigationBackgroundColor())
        faqButton.compoundDrawableTintList = ColorStateList.valueOf(getProperTextColor())
    }

    @SuppressLint("NewApi", "UseCompatTextViewDrawableApis")
    private fun setupTipJar() {
        tipJarButton.setOnClickListener {
            startPurchaseActivity(R.string.app_name_g, licensingKey, productIdX1, productIdX2, productIdX3, showLifebuoy = false)
        }
        tipJarButton.setTextColor(getProperTextColor())
        tipJarButton.background = resources.getColoredDrawableWithColor(R.drawable.button_gray_bg, getBottomNavigationBackgroundColor())
        tipJarButton.compoundDrawableTintList = ColorStateList.valueOf(getProperTextColor())
    }

    @SuppressLint("NewApi", "SetTextI18n", "UseCompatTextViewDrawableApis")
    private fun setupCollection() {
        val appDialerPackage = "com.goodwy.dialer"
        val appContactsPackage = "com.goodwy.contacts"
        val appSmsMessengerPackage = "com.goodwy.smsmessenger"
        val appVoiceRecorderPackage = "com.goodwy.voicerecorder"
        val appGalleryPackage = "com.goodwy.gallery"

        val appDialerInstalled = isPackageInstalled(appDialerPackage)// || isPackageInstalled("com.goodwy.dialer.debug")
        val appContactsInstalled = isPackageInstalled(appContactsPackage)// || isPackageInstalled("com.goodwy.contacts.debug")
        val appSmsMessengerInstalled = isPackageInstalled(appSmsMessengerPackage)// || isPackageInstalled("com.goodwy.smsmessenger.debug")
        val appVoiceRecorderInstalled = isPackageInstalled(appVoiceRecorderPackage)// || isPackageInstalled("com.goodwy.voicerecorder.debug")
        val appGalleryInstalled = isPackageInstalled(appGalleryPackage)// || isPackageInstalled("com.goodwy.voicerecorder.debug")

        val appAllInstalled = appDialerInstalled && appContactsInstalled && appSmsMessengerInstalled && appVoiceRecorderInstalled

        collectionButton.setTextColor(getProperTextColor())
        collectionButton.background = resources.getColoredDrawableWithColor(R.drawable.button_gray_bg, getBottomNavigationBackgroundColor())
        if (!appAllInstalled) collectionButton.compoundDrawableTintList = ColorStateList.valueOf(getProperTextColor())

        val items = arrayOf(
            SimpleListItem(1, R.string.right_dialer, R.mipmap.ic_dialer, selected = appDialerInstalled, packageName = appDialerPackage),
            SimpleListItem(2, R.string.right_contacts, R.mipmap.ic_contacts, selected = appContactsInstalled, packageName = appContactsPackage),
            SimpleListItem(3, R.string.right_sms_messenger, R.mipmap.ic_sms_messenger, selected = appSmsMessengerInstalled, packageName = appSmsMessengerPackage),
            SimpleListItem(4, R.string.right_voice_recorder, R.mipmap.ic_voice_recorder, selected = appVoiceRecorderInstalled, packageName = appVoiceRecorderPackage),
            SimpleListItem(5, R.string.right_gallery, R.mipmap.ic_gallery, selected = appGalleryInstalled, packageName = appGalleryPackage)
        )

        val percentage = items.filter { it.selected }.size.toString() + "/" + items.size.toString()
        collectionButton.text = getString(R.string.collection) + "  $percentage"

        collectionButton.setOnClickListener {
            BottomSheetChooserDialog.createChooser(
                fragmentManager = supportFragmentManager,
                title = R.string.collection,
                items = items,
                collection = true
            ) {
                if (it.selected) {
                    launchApp(it.packageName)
                } else {
                    val url = "https://play.google.com/store/apps/details?id=${it.packageName}"
                    launchViewIntent(url)
                }
            }
        }
    }

    private fun launchApp(packageName: String) {
        try {
            Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                `package` = packageName
                //component = ComponentName.unflattenFromString("$packageName/")
                addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                startActivity(this)
            }
        } catch (e: Exception) {
            try {
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                startActivity(launchIntent)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }

    private fun setupLicense() {
        about_license.setOnClickListener {
            Intent(applicationContext, LicenseActivity::class.java).apply {
                putExtra(APP_ICON_IDS, getAppIconIDs())
                putExtra(APP_LAUNCHER_NAME, getAppLauncherName())
                putExtra(APP_LICENSES, intent.getLongExtra(APP_LICENSES, 0))
                startActivity(this)
            }
        }
        about_license.setTextColor(primaryColor)
    }

    private fun setupFacebook() {
        about_facebook.setOnClickListener {
            var link = "https://www.facebook.com/Goodwy"
            try {
                packageManager.getPackageInfo("com.facebook.katana", 0)
                link = "fb://page/id"
            } catch (ignored: Exception) {
            }

            launchViewIntent(link)
        }
    }

    private fun setupReddit() {
        about_reddit.setOnClickListener {
            launchViewIntent("https://www.reddit.com/r/Goodwy")
        }
    }

    private fun setupCopyright() {
        var versionName = intent.getStringExtra(APP_VERSION_NAME) ?: ""
        if (baseConfig.appId.removeSuffix(".debug").endsWith(".pro")) {
            versionName += " ${getString(R.string.pro)}"
        }

        val year = Calendar.getInstance().get(Calendar.YEAR)
        about_copyright.text = String.format(getString(R.string.copyright_g), versionName, year)
    }

    @SuppressLint("SetTextI18n")
    private fun setupAboutApp() {
        var versionName = intent.getStringExtra(APP_VERSION_NAME) ?: ""
        if (baseConfig.appId.removeSuffix(".debug").endsWith(".pro")) {
            versionName += " ${getString(R.string.pro)}"
        }
        about_app_version.text = "Version: $versionName"

        about_app_holder.setOnClickListener {
            if (firstVersionClickTS == 0L) {
                firstVersionClickTS = System.currentTimeMillis()
                Handler().postDelayed({
                    firstVersionClickTS = 0L
                    clicksSinceFirstClick = 0
                }, EASTER_EGG_TIME_LIMIT)
            }

            clicksSinceFirstClick++
            if (clicksSinceFirstClick >= EASTER_EGG_REQUIRED_CLICKS) {
                toast(R.string.hello)
                firstVersionClickTS = 0L
                clicksSinceFirstClick = 0
            }
        }
    }
}
