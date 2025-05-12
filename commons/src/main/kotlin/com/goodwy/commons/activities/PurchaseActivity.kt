package com.goodwy.commons.activities

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.*
import android.os.Bundle
import android.text.Html
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toUri
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.goodwy.commons.R
import com.goodwy.commons.databinding.ActivityPurchaseBinding
import com.goodwy.commons.dialogs.BottomSheetChooserDialog
import com.goodwy.commons.dialogs.ConfirmationAdvancedDialog
import com.goodwy.commons.dialogs.ConfirmationDialog
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.*
import com.goodwy.commons.helpers.rustore.RuStoreHelper
import com.goodwy.commons.helpers.rustore.RuStoreModule
import com.goodwy.commons.helpers.rustore.model.BillingEvent
import com.goodwy.commons.helpers.rustore.model.BillingState
import com.goodwy.commons.helpers.rustore.model.StartPurchasesEvent
import com.goodwy.commons.models.SimpleListItem
import com.goodwy.strings.R as stringsR
import kotlinx.coroutines.*
import ru.rustore.sdk.billingclient.RuStoreBillingClient
import ru.rustore.sdk.billingclient.utils.resolveForBilling
import ru.rustore.sdk.core.exception.RuStoreException
import ru.rustore.sdk.core.feature.model.FeatureAvailabilityResult

class PurchaseActivity : BaseSimpleActivity() {

    private var appName = ""
    private var primaryColor = 0
    private var productIdList: ArrayList<String> = ArrayList()
    private var productIdListRu: ArrayList<String> = ArrayList()
    private var subscriptionIdList: ArrayList<String> = ArrayList()
    private var subscriptionIdListRu: ArrayList<String> = ArrayList()
    private var subscriptionYearIdList: ArrayList<String> = ArrayList()
    private var subscriptionYearIdListRu: ArrayList<String> = ArrayList()
    private var showLifebuoy = true
    private var playStoreInstalled = true
    private var ruStoreInstalled = false
    private var showCollection = false

    private var ruStoreIsConnected = false

    private val purchaseHelper = PurchaseHelper(this)
    private var ruStoreHelper: RuStoreHelper? = null
    private var ruStoreBillingClient: RuStoreBillingClient? = null

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun getRepositoryName() = null

    private val binding by viewBinding(ActivityPurchaseBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = false
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        appName = intent.getStringExtra(APP_NAME) ?: ""
        productIdList = intent.getStringArrayListExtra(PRODUCT_ID_LIST) ?: arrayListOf("", "", "")
        productIdListRu = intent.getStringArrayListExtra(PRODUCT_ID_LIST_RU) ?: arrayListOf("", "", "")
        subscriptionIdList = intent.getStringArrayListExtra(SUBSCRIPTION_ID_LIST) ?: arrayListOf("", "", "")
        subscriptionIdListRu = intent.getStringArrayListExtra(SUBSCRIPTION_ID_LIST_RU) ?: arrayListOf("", "", "")
        subscriptionYearIdList = intent.getStringArrayListExtra(SUBSCRIPTION_YEAR_ID_LIST) ?: arrayListOf("", "", "")
        subscriptionYearIdListRu = intent.getStringArrayListExtra(SUBSCRIPTION_YEAR_ID_LIST_RU) ?: arrayListOf("", "", "")
        primaryColor = getProperPrimaryColor()
        showLifebuoy = intent.getBooleanExtra(SHOW_LIFEBUOY, true)
        playStoreInstalled = intent.getBooleanExtra(PLAY_STORE_INSTALLED, true)
        ruStoreInstalled = intent.getBooleanExtra(RU_STORE, false)
        showCollection = intent.getBooleanExtra(SHOW_COLLECTION, false)


        if (ruStoreInstalled) {
            ruStoreHelper = RuStoreHelper()
            ruStoreBillingClient = RuStoreModule.provideRuStoreBillingClient()
        }
        if (savedInstanceState == null && ruStoreInstalled) {
            ruStoreBillingClient!!.onNewIntent(intent)
        }

        // TODO TRANSPARENT Navigation Bar
        setWindowTransparency(true) { _, _, leftNavigationBarSize, rightNavigationBarSize ->
            binding.purchaseCoordinator.setPadding(leftNavigationBarSize, 0, rightNavigationBarSize, 0)
            updateNavigationBarColor(getProperBackgroundColor())
        }

        arrayOf(
            binding.purchaseNestedScrollview,
            binding.topDetails.root
        ).forEach {
            it.beInvisibleIf(resources.getBoolean(R.bool.using_no_gp) || (!playStoreInstalled && !ruStoreInstalled))
//            it.beInvisibleIf(!ruStoreInstalled)
        }

        arrayOf(
            binding.proHolder,
            binding.proDonateText,
            binding.proDonateButton
        ).forEach {
            it.beGoneIf((!resources.getBoolean(R.bool.using_no_gp) && playStoreInstalled) || ruStoreInstalled)
        }

        if ((playStoreInstalled && !ruStoreInstalled) || (playStoreInstalled && ruStoreInstalled && baseConfig.useGooglePlay)) {
            //PlayStore
            purchaseHelper.initBillingClient()
            val subscriptionIdListAll: ArrayList<String> = subscriptionIdList
            subscriptionIdListAll.addAll(subscriptionYearIdList)
            purchaseHelper.retrieveDonation(productIdList, subscriptionIdListAll)

            purchaseHelper.iapSkuDetailsInitialized.observe(this) {
                if (it) setupButtonIapPurchased()
            }
            purchaseHelper.subSkuDetailsInitialized.observe(this) {
                if (it) setupButtonSupPurchased()
            }

            purchaseHelper.isIapPurchased.observe(this) {
                when (it) {
                    is Tipping.Succeeded -> {
                        baseConfig.isPro = true
                    }
                    is Tipping.NoTips -> {
                        baseConfig.isPro = false
                    }
                    is Tipping.FailedToLoad -> {
                    }
                    else -> {
                    }
                }
            }
            purchaseHelper.isSupPurchased.observe(this) {
                when (it) {
                    is Tipping.Succeeded -> {
                        baseConfig.isProSubs = true
                    }
                    is Tipping.NoTips -> {
                        baseConfig.isProSubs = false
                    }
                    is Tipping.FailedToLoad -> {
                    }
                    else -> {
                    }
                }
            }

            purchaseHelper.isIapPurchasedList.observe(this) {
                setupButtonIapChecked()
            }
            purchaseHelper.isSupPurchasedList.observe(this) {
                setupButtonSupChecked()
            }
        } else if ((!playStoreInstalled && ruStoreInstalled) || (playStoreInstalled && ruStoreInstalled && !baseConfig.useGooglePlay)) {
            //RuStore
            ruStoreHelper!!.checkPurchasesAvailability(this)

//            lifecycleScope.launch {
//                ruStoreHelper!!.stateStart
//                    .flowWithLifecycle(lifecycle)
//                    .collect { state ->
//                        // update button
//                    }
//            }
            lifecycleScope.launch {
                ruStoreHelper!!.eventStart
                    .flowWithLifecycle(lifecycle)
                    .collect { event ->
                        handleEventStart(event)
                    }
            }

            lifecycleScope.launch {
                ruStoreHelper!!.stateBilling
                    .flowWithLifecycle(lifecycle)
                    .collect { state ->
                        if (!state.isLoading) {
                            //price update
                            setupButtonRuStore(state)
                        }
                    }
            }
            lifecycleScope.launch {
                ruStoreHelper!!.eventBilling
                    .flowWithLifecycle(lifecycle)
                    .collect { event ->
                        handleEventBilling(event)
                    }
            }

            lifecycleScope.launch {
                ruStoreHelper!!.statePurchased
                    .flowWithLifecycle(lifecycle)
                    .collect { state ->
                        if (!state.isLoading && ruStoreIsConnected) {
                            //update of purchased
                            setupButtonCheckedRuStore(state.purchases)
                            //update pro version
                            baseConfig.isProRuStore = state.purchases.isNotEmpty()
                        }
                    }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (ruStoreInstalled) {
            ruStoreBillingClient!!.onNewIntent(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateTextColors(binding.purchaseCoordinator)
        setupOptionsMenu()
        setupToolbar(binding.purchaseToolbar, NavigationIcon.Arrow)
        val backgroundColor = getProperBackgroundColor()
        binding.collapsingToolbar.setBackgroundColor(backgroundColor)
        updateTopBarColors(binding.purchaseToolbar, backgroundColor, useOverflowIcon = false)

        setupChangeStoreMenu()
        setupEmail()
        if (showCollection) setupCollection()
        if ((!resources.getBoolean(R.bool.using_no_gp) && playStoreInstalled) || ruStoreInstalled) {
            setupIcon()
        } else {
            setupNoPlayStoreInstalled()
        }
//        if (ruStoreInstalled) {
//            setupIcon()
//        } else {
//            setupNoPlayStoreInstalled()
//        }

        val isProApp = resources.getBoolean(R.bool.is_pro_app)
        binding.themeHolder.beVisibleIf(!isProApp)
        binding.colorHolder.beVisibleIf(!isProApp)
    }

    private fun setupOptionsMenu() {
        val visible = (!resources.getBoolean(R.bool.using_no_gp) && playStoreInstalled) || ruStoreInstalled
        binding.purchaseToolbar.menu.apply {
            findItem(R.id.restorePurchases).isVisible = visible
            findItem(R.id.openSubscriptions).isVisible = visible
        }
        binding.purchaseToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.restorePurchases -> {
                    setupButtonReset()
                    if (ruStoreInstalled && !baseConfig.useGooglePlay) updateProducts()
                    else {
                        val subscriptionIdListAll: ArrayList<String> = subscriptionIdList
                        subscriptionIdListAll.addAll(subscriptionYearIdList)
                        purchaseHelper.retrieveDonation(productIdList, subscriptionIdListAll)
                    }
                    true
                }
                R.id.openSubscriptions -> {
                    val url = if (ruStoreInstalled && !baseConfig.useGooglePlay) "rustore://profile/subscriptions" else "https://play.google.com/store/account/subscriptions"
                    launchViewIntent(url)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupChangeStoreMenu() {
        binding.purchaseToolbar.menu.findItem(R.id.changeStore).apply {
            isVisible = !resources.getBoolean(R.bool.using_no_gp) && playStoreInstalled && ruStoreInstalled
            title = if (baseConfig.useGooglePlay) getString(stringsR.string.billing_change_to_ru_store) else getString(stringsR.string.billing_change_to_google_play)
            icon = if (baseConfig.useGooglePlay) AppCompatResources.getDrawable(this@PurchaseActivity, R.drawable.ic_google_play_vector)
            else AppCompatResources.getDrawable(this@PurchaseActivity, R.drawable.ic_rustore)
            icon?.setTint(getProperTextColor())
            setOnMenuItemClickListener {
                if (baseConfig.useGooglePlay) {
                    baseConfig.useGooglePlay = false
                    recreate()
                } else {
                    baseConfig.useGooglePlay = true
                    recreate()
                }
                true
            }
        }
    }

    private fun setupEmail() {
        binding.lifebuoyHolder.beVisibleIf(showLifebuoy && playStoreInstalled)
        val lifebuoyButtonDrawable = resources.getColoredDrawableWithColor(this, R.drawable.ic_mail_vector, getProperTextColor())
        binding.lifebuoyButton.setImageDrawable(lifebuoyButtonDrawable)
        binding.lifebuoyButton.setOnClickListener {
            ConfirmationDialog(this, getString(R.string.send_email)) {
                val body = "$appName : Lifebuoy"
                val address = getString(R.string.my_email)
                val selectorIntent = Intent(ACTION_SENDTO)
                    .setData("mailto:$address".toUri())
                val emailIntent = Intent(ACTION_SEND).apply {
                    putExtra(EXTRA_EMAIL, arrayOf(address))
                    putExtra(EXTRA_SUBJECT, body)
                    selector = selectorIntent
                }

                try {
                    startActivity(emailIntent)
                } catch (e: ActivityNotFoundException) {
                    toast(R.string.no_app_found)
                } catch (e: Exception) {
                    showErrorToast(e)
                }
            }
        }
    }

    private fun setupButtonIapPurchased() {
        binding.appOneButton.apply {
            val price = purchaseHelper.getPriceDonation(productIdList[0])
            isEnabled = price != getString(stringsR.string.no_connection)
            text = price
            setOnClickListener {
                purchaseHelper.getDonation(productIdList[0])
            }
            background.setTint(primaryColor)
        }

        binding.appTwoButton.apply {
            val price = purchaseHelper.getPriceDonation(productIdList[1])
            isEnabled = price != getString(stringsR.string.no_connection)
            text = price
            setOnClickListener {
                purchaseHelper.getDonation(productIdList[1])
            }
            background.setTint(primaryColor)
        }

        binding.appThreeButton.apply {
            val price = purchaseHelper.getPriceDonation(productIdList[2])
            isEnabled = price != getString(stringsR.string.no_connection)
            text = price
            setOnClickListener {
                purchaseHelper.getDonation(productIdList[2])
            }
            background.setTint(primaryColor)
        }
    }

    private fun setupButtonIapChecked() {
        val check = AppCompatResources.getDrawable(this@PurchaseActivity, R.drawable.ic_check_circle_mini)
        if (purchaseHelper.isIapPurchased(productIdList[0])) {
            binding.appOneButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appOneButton.isEnabled = false
        }
        if (purchaseHelper.isIapPurchased(productIdList[1])) {
            binding.appTwoButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appTwoButton.isEnabled = false
        }
        if (purchaseHelper.isIapPurchased(productIdList[2])) {
            binding.appThreeButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appThreeButton.isEnabled = false
        }
    }

    private fun setupButtonSupPurchased() {
        binding.appOneSubButton.apply {
            val price = purchaseHelper.getPriceSubscription(subscriptionIdList[0])
            if (price != getString(stringsR.string.no_connection)) {
                isEnabled = true
                val textPrice = String.format(getString(stringsR.string.per_month), price)
                text = textPrice
                setOnClickListener {
                    purchaseHelper.getSubscription(subscriptionIdList[0])
                }
            } else {
                text = price
            }
            background.setTint(primaryColor)
        }

        binding.appTwoSubButton.apply {
            val price = purchaseHelper.getPriceSubscription(subscriptionIdList[1])
            if (price != getString(stringsR.string.no_connection)) {
                isEnabled = true
                val textPrice = String.format(getString(stringsR.string.per_month), price)
                text = textPrice
                setOnClickListener {
                    purchaseHelper.getSubscription(subscriptionIdList[1])
                }
            } else {
                text = price
            }
            background.setTint(primaryColor)
        }

        binding.appThreeSubButton.apply {
            val price = purchaseHelper.getPriceSubscription(subscriptionIdList[2])
            if (price != getString(stringsR.string.no_connection)) {
                isEnabled = true
                val textPrice = String.format(getString(stringsR.string.per_month), price)
                text = textPrice
                setOnClickListener {
                    purchaseHelper.getSubscription(subscriptionIdList[2])
                }
            } else {
                text = price
            }
            background.setTint(primaryColor)
        }

        binding.appOneSubYearButton.apply {
            val price = purchaseHelper.getPriceSubscription(subscriptionYearIdList[0])
            if (price != getString(stringsR.string.no_connection)) {
                isEnabled = true
                val textPrice = String.format(getString(stringsR.string.per_year), price)
                text = textPrice
                setOnClickListener {
                    purchaseHelper.getSubscription(subscriptionYearIdList[0])
                }
            } else {
                text = price
            }
            background.setTint(primaryColor)
        }

        binding.appTwoSubYearButton.apply {
            val price = purchaseHelper.getPriceSubscription(subscriptionYearIdList[1])
            if (price != getString(stringsR.string.no_connection)) {
                isEnabled = true
                val textPrice = String.format(getString(stringsR.string.per_year), price)
                text = textPrice
                setOnClickListener {
                    purchaseHelper.getSubscription(subscriptionYearIdList[1])
                }
            } else {
                text = price
            }
            background.setTint(primaryColor)
        }

        binding.appThreeSubYearButton.apply {
            val price = purchaseHelper.getPriceSubscription(subscriptionYearIdList[2])
            if (price != getString(stringsR.string.no_connection)) {
                isEnabled = true
                val textPrice = String.format(getString(stringsR.string.per_year), price)
                text = textPrice
                setOnClickListener {
                    purchaseHelper.getSubscription(subscriptionYearIdList[2])
                }
            } else {
                text = price
            }
            background.setTint(primaryColor)
        }
    }

    private fun setupButtonSupChecked() {
        val check = AppCompatResources.getDrawable(this@PurchaseActivity, R.drawable.ic_check_circle_mini)
        if (purchaseHelper.isSubPurchased(subscriptionIdList[0])) {
            binding.appOneSubButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appOneSubButton.isEnabled = false
        }
        if (purchaseHelper.isSubPurchased(subscriptionIdList[1])) {
            binding.appTwoSubButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appTwoSubButton.isEnabled = false
        }
        if (purchaseHelper.isSubPurchased(subscriptionIdList[2])) {
            binding.appThreeSubButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appThreeSubButton.isEnabled = false
        }
        if (purchaseHelper.isSubPurchased(subscriptionYearIdList[0])) {
            binding.appOneSubYearButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appOneSubYearButton.isEnabled = false
        }
        if (purchaseHelper.isSubPurchased(subscriptionYearIdList[1])) {
            binding.appTwoSubYearButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appTwoSubYearButton.isEnabled = false
        }
        if (purchaseHelper.isSubPurchased(subscriptionYearIdList[2])) {
            binding.appThreeSubYearButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appThreeSubYearButton.isEnabled = false
        }
    }

    private fun setupButtonReset() {
        binding.appOneButton.apply {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            text = "..."
            isEnabled = false
        }
        binding.appOneSubButton.apply {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            text = "..."
            isEnabled = false
        }
        binding.appOneSubYearButton.apply {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            text = "..."
            isEnabled = false
        }
        binding.appTwoButton.apply {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            text = "..."
            isEnabled = false
        }
        binding.appTwoSubButton.apply {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            text = "..."
            isEnabled = false
        }
        binding.appTwoSubYearButton.apply {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            text = "..."
            isEnabled = false
        }
        binding.appThreeButton.apply {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            text = "..."
            isEnabled = false
        }
        binding.appThreeSubButton.apply {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            text = "..."
            isEnabled = false
        }
        binding.appThreeSubYearButton.apply {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            text = "..."
            isEnabled = false
        }
    }

    private fun setupIcon() {
        val appDrawable = resources.getColoredDrawableWithColor(this, R.drawable.ic_plus_support, primaryColor)
        binding.topDetails.appLogo.setImageDrawable(appDrawable)
        val themeDrawable = resources.getColoredDrawableWithColor(this, R.drawable.ic_invert_colors, primaryColor)
        binding.themeLogo.setImageDrawable(themeDrawable)
        val colorDrawable = resources.getColoredDrawableWithColor(this, R.drawable.ic_palette, primaryColor)
        binding.colorLogo.setImageDrawable(colorDrawable)
        val plusDrawable = resources.getColoredDrawableWithColor(this, R.drawable.ic_plus_round, primaryColor)
        binding.plusLogo.setImageDrawable(plusDrawable)
        val lifebuoyDrawable = resources.getColoredDrawableWithColor(this, R.drawable.ic_lifebuoy, primaryColor)
        binding.lifebuoyLogo.setImageDrawable(lifebuoyDrawable)
    }

    @Suppress("DEPRECATION")
    private fun setupNoPlayStoreInstalled() {
        val isProApp = resources.getBoolean(R.bool.is_pro_app)
        binding.proDonateText.text =
            if (isProApp) Html.fromHtml(getString(stringsR.string.plus_summary))
            else if (resources.getBoolean(R.bool.using_no_gp) && playStoreInstalled) Html.fromHtml(getString(stringsR.string.donate_text_no_gp_g))
            else Html.fromHtml(getString(stringsR.string.donate_text_g))
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
            binding.proSwitch.isChecked = if (resources.getBoolean(R.bool.using_no_gp) && playStoreInstalled) baseConfig.isProNoGP else baseConfig.isPro
            binding.proSwitchHolder.setOnClickListener {
                binding.proSwitch.toggle()
                if (resources.getBoolean(R.bool.using_no_gp) && playStoreInstalled) baseConfig.isProNoGP = binding.proSwitch.isChecked
                else baseConfig.isPro = binding.proSwitch.isChecked
            }
        }
    }

    @SuppressLint("NewApi", "SetTextI18n", "UseCompatTextViewDrawableApis")
    private fun setupCollection() {
        binding.collectionHolder.beVisible()
        val appDialerPackage = "com.goodwy.dialer"
        val appContactsPackage = "com.goodwy.contacts"
        val appSmsMessengerPackage = "com.goodwy.smsmessenger"
        val appGalleryPackage = "com.goodwy.gallery"
        val appAudiobookLitePackage = "com.goodwy.audiobooklite"
        val appFilesPackage = "com.goodwy.filemanager"
        val appKeyboardPackage = "com.goodwy.keyboard"
        val appCalendarPackage = "com.goodwy.calendar"
        val appVoiceRecorderPackage = "com.goodwy.voicerecorderfree"

        val appDialerInstalled = isPackageInstalled(appDialerPackage)
        val appContactsInstalled = isPackageInstalled(appContactsPackage)
        val appSmsMessengerInstalled = isPackageInstalled(appSmsMessengerPackage)
        val appGalleryInstalled = isPackageInstalled(appGalleryPackage)
        val appAudiobookLiteInstalled = isPackageInstalled(appAudiobookLitePackage)
        val appFilesInstalled = isPackageInstalled(appFilesPackage)
        val appKeyboardInstalled = isPackageInstalled(appKeyboardPackage)
        val appCalendarInstalled = isPackageInstalled(appCalendarPackage)
        val appVoiceRecorderInstalled = isPackageInstalled(appVoiceRecorderPackage)

        val appAllInstalled = appDialerInstalled && appContactsInstalled && appSmsMessengerInstalled && appGalleryInstalled &&
            appAudiobookLiteInstalled && appFilesInstalled && appKeyboardInstalled && appCalendarInstalled && appVoiceRecorderInstalled

        if (!appAllInstalled) binding.collectionLogo.applyColorFilter(primaryColor)
        binding.collectionChevron.applyColorFilter(getProperTextColor())
        binding.collectionSubtitle.background.applyColorFilter(getBottomNavigationBackgroundColor())

        val items = arrayOf(
            SimpleListItem(1, R.string.right_dialer, imageRes = R.drawable.ic_dialer, selected = appDialerInstalled, packageName = appDialerPackage),
            SimpleListItem(2, R.string.right_contacts, imageRes = R.drawable.ic_contacts, selected = appContactsInstalled, packageName = appContactsPackage),
            SimpleListItem(3, R.string.right_sms_messenger, imageRes = R.drawable.ic_sms_messenger, selected = appSmsMessengerInstalled, packageName = appSmsMessengerPackage),
            SimpleListItem(4, R.string.right_gallery, imageRes = R.drawable.ic_gallery, selected = appGalleryInstalled, packageName = appGalleryPackage),
            SimpleListItem(5, R.string.right_files, imageRes = R.drawable.ic_files, selected = appFilesInstalled, packageName = appFilesPackage),
            SimpleListItem(6, R.string.playbook, imageRes = R.drawable.ic_playbook, selected = appAudiobookLiteInstalled, packageName = appAudiobookLitePackage),
            SimpleListItem(7, R.string.right_keyboard, imageRes = R.drawable.ic_inkwell, selected = appKeyboardInstalled, packageName = appKeyboardPackage),
            SimpleListItem(8, R.string.right_calendar, imageRes = R.drawable.ic_calendar_app, selected = appCalendarInstalled, packageName = appCalendarPackage),
            SimpleListItem(8, R.string.right_voice_recorder, imageRes = R.drawable.ic_voice_recorder, selected = appVoiceRecorderInstalled, packageName = appVoiceRecorderPackage)
        )

        val percentage = items.filter { it.selected }.size.toString() + "/" + items.size.toString()
        binding.collectionTitle.text = getString(stringsR.string.collection) + "  $percentage"

        binding.collectionHolder.setOnClickListener {
            BottomSheetChooserDialog.createChooser(
                fragmentManager = supportFragmentManager,
                title = stringsR.string.collection,
                items = items,
                collection = true
            ) {
                if (it.selected) {
                    launchApp(it.packageName)
                } else {
                    if (ruStoreInstalled && !baseConfig.useGooglePlay) {
                        val urlRS = "https://www.rustore.ru/catalog/app/${it.packageName}"
                        launchViewIntent(urlRS)
                    } else {
                        val urlGP = "https://play.google.com/store/apps/details?id=${it.packageName}"
                        launchViewIntent(urlGP)
                    }
                }
            }
        }
    }

    private fun launchApp(packageName: String) {
        try {
            Intent(ACTION_MAIN).apply {
                addCategory(CATEGORY_LAUNCHER)
                `package` = packageName
                //component = ComponentName.unflattenFromString("$packageName/")
                addFlags(FLAG_RECEIVER_FOREGROUND)
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

    //RuStore
    private fun setupButtonRuStore(state: BillingState) {
        binding.appOneButton.apply {
            val product = state.products.firstOrNull {  it.productId == productIdListRu[0]  }
            val price = product?.priceLabel ?: getString(stringsR.string.no_connection)
            isEnabled = price != getString(stringsR.string.no_connection)
            val resultPrice = price.replace(".00","",true)
            text = resultPrice
            setOnClickListener {
                if (product != null) {
                    ruStoreHelper!!.purchaseProduct(product)
                }
            }
            background.setTint(primaryColor)
        }

        binding.appTwoButton.apply {
            val product = state.products.firstOrNull {  it.productId == productIdListRu[1]  }
            val price = product?.priceLabel ?: getString(stringsR.string.no_connection)
            isEnabled = price != getString(stringsR.string.no_connection)
            val resultPrice = price.replace(".00","",true)
            text = resultPrice
            setOnClickListener {
                if (product != null) {
                    ruStoreHelper!!.purchaseProduct(product)
                }
            }
            background.setTint(primaryColor)
        }

        binding.appThreeButton.apply {
            val product = state.products.firstOrNull {  it.productId == productIdListRu[2]  }
            val price = product?.priceLabel ?: getString(stringsR.string.no_connection)
            isEnabled = price != getString(stringsR.string.no_connection)
            val resultPrice = price.replace(".00","",true)
            text = resultPrice
            setOnClickListener {
                if (product != null) {
                    ruStoreHelper!!.purchaseProduct(product)
                }
            }
            background.setTint(primaryColor)
        }

        binding.appOneSubButton.apply {
            val product = state.products.firstOrNull {  it.productId == subscriptionIdListRu[0]  }
            val price = product?.priceLabel ?: getString(stringsR.string.no_connection)
            if (price != getString(stringsR.string.no_connection)) {
                isEnabled = true
                val resultPrice = price.replace(".00","",true)
                val textPrice = String.format(getString(stringsR.string.per_month), resultPrice)
                text = textPrice
                setOnClickListener {
                    if (product != null) {
                        ruStoreHelper!!.purchaseProduct(product)
                    }
                }
            } else {
                text = price
            }
            background.setTint(primaryColor)
        }

        binding.appTwoSubButton.apply {
            val product = state.products.firstOrNull {  it.productId == subscriptionIdListRu[1]  }
            val price = product?.priceLabel ?: getString(stringsR.string.no_connection)
            if (price != getString(stringsR.string.no_connection)) {
                isEnabled = true
                val resultPrice = price.replace(".00","",true)
                val textPrice = String.format(getString(stringsR.string.per_month), resultPrice)
                text = textPrice
                setOnClickListener {
                    if (product != null) {
                        ruStoreHelper!!.purchaseProduct(product)
                    }
                }
            } else {
                text = price
            }
            background.setTint(primaryColor)
        }

        binding.appThreeSubButton.apply {
            val product = state.products.firstOrNull {  it.productId == subscriptionIdListRu[2]  }
            val price = product?.priceLabel ?: getString(stringsR.string.no_connection)
            if (price != getString(stringsR.string.no_connection)) {
                isEnabled = true
                val resultPrice = price.replace(".00","",true)
                val textPrice = String.format(getString(stringsR.string.per_month), resultPrice)
                text = textPrice
                setOnClickListener {
                    if (product != null) {
                        ruStoreHelper!!.purchaseProduct(product)
                    }
                }
            } else {
                text = price
            }
            background.setTint(primaryColor)
        }


        binding.appOneSubYearButton.apply {
            val product = state.products.firstOrNull {  it.productId == subscriptionYearIdListRu[0]  }
            val price = product?.priceLabel ?: getString(stringsR.string.no_connection)
            if (price != getString(stringsR.string.no_connection)) {
                isEnabled = true
                val resultPrice = price.replace(".00","",true)
                val textPrice = String.format(getString(stringsR.string.per_year), resultPrice)
                text = textPrice
                setOnClickListener {
                    if (product != null) {
                        ruStoreHelper!!.purchaseProduct(product)
                    }
                }
            } else {
                text = price
            }
            background.setTint(primaryColor)
        }

        binding.appTwoSubYearButton.apply {
            val product = state.products.firstOrNull {  it.productId == subscriptionYearIdListRu[1]  }
            val price = product?.priceLabel ?: getString(stringsR.string.no_connection)
            if (price != getString(stringsR.string.no_connection)) {
                isEnabled = true
                val resultPrice = price.replace(".00","",true)
                val textPrice = String.format(getString(stringsR.string.per_year), resultPrice)
                text = textPrice
                setOnClickListener {
                    if (product != null) {
                        ruStoreHelper!!.purchaseProduct(product)
                    }
                }
            } else {
                text = price
            }
            background.setTint(primaryColor)
        }

        binding.appThreeSubYearButton.apply {
            val product = state.products.firstOrNull {  it.productId == subscriptionYearIdListRu[2]  }
            val price = product?.priceLabel ?: getString(stringsR.string.no_connection)
            if (price != getString(stringsR.string.no_connection)) {
                isEnabled = true
                val resultPrice = price.replace(".00","",true)
                val textPrice = String.format(getString(stringsR.string.per_year), resultPrice)
                text = textPrice
                setOnClickListener {
                    if (product != null) {
                        ruStoreHelper!!.purchaseProduct(product)
                    }
                }
            } else {
                text = price
            }
            background.setTint(primaryColor)
        }
    }

    private fun setupButtonCheckedRuStore(state: List<ru.rustore.sdk.billingclient.model.purchase.Purchase>) {
        val check = AppCompatResources.getDrawable(this@PurchaseActivity, R.drawable.ic_check_circle_mini)
        if (state.firstOrNull {  it.productId == productIdListRu[0]  } != null) {
            binding.appOneButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appOneButton.isEnabled = false
        }
        if (state.firstOrNull {  it.productId == productIdListRu[1]  } != null) {
            binding.appTwoButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appTwoButton.isEnabled = false
        }
        if (state.firstOrNull {  it.productId == productIdListRu[2]  } != null) {
            binding.appThreeButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appThreeButton.isEnabled = false
        }
        if (state.firstOrNull {  it.productId == subscriptionIdListRu[0]  } != null) {
            binding.appOneSubButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appOneSubButton.isEnabled = false
        }
        if (state.firstOrNull {  it.productId == subscriptionIdListRu[1]  } != null) {
            binding.appTwoSubButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appTwoSubButton.isEnabled = false
        }
        if (state.firstOrNull {  it.productId == subscriptionIdListRu[2]  } != null) {
            binding.appThreeSubButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appThreeSubButton.isEnabled = false
        }
        if (state.firstOrNull {  it.productId == subscriptionYearIdListRu[0]  } != null) {
            binding.appOneSubYearButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appOneSubYearButton.isEnabled = false
        }
        if (state.firstOrNull {  it.productId == subscriptionYearIdListRu[1]  } != null) {
            binding.appTwoSubYearButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appTwoSubYearButton.isEnabled = false
        }
        if (state.firstOrNull {  it.productId == subscriptionYearIdListRu[2]  } != null) {
            binding.appThreeSubYearButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appThreeSubYearButton.isEnabled = false
        }
    }

    private fun updateProducts() {
        val productList: ArrayList<String> = productIdListRu
        productList.addAll(subscriptionIdListRu)
        productList.addAll(subscriptionYearIdListRu)
        ruStoreHelper!!.getProducts(productList)
    }

    private fun handleEventStart(event: StartPurchasesEvent) {
        when (event) {
            is StartPurchasesEvent.PurchasesAvailability -> {
                when (event.availability) {
                    is FeatureAvailabilityResult.Available -> {
                        //Process purchases available
                        updateProducts()
                        ruStoreIsConnected = true
                    }

                    is FeatureAvailabilityResult.Unavailable -> {
                        val error = event.availability.cause.message ?: "Process purchases unavailable"
                        if (error == "Application signature not correct") {
                            ConfirmationAdvancedDialog(
                                activity = this,
                                messageId = stringsR.string.billing_error_application_signature_not_correct,
                                positive = stringsR.string.get,
                                negative = R.string.cancel
                            ) { success ->
                                if (success) {
                                    val url = "https://apps.rustore.ru/app/$packageName"
                                    this.launchViewIntent(url)
                                }
                            }
                        } else event.availability.cause.resolveForBilling(this) //Show error dialog
                        //showErrorToast(event.availability.cause.message ?: "Process purchases unavailable", Toast.LENGTH_LONG)
                    }

                    else -> {}
                }
            }

            is StartPurchasesEvent.Error -> {
                showErrorToast(event.throwable.message ?: "Process unknown error", Toast.LENGTH_LONG)
            }
        }
    }

    private fun handleEventBilling(event: BillingEvent) {
        when (event) {
            is BillingEvent.ShowDialog -> {
                toast(event.dialogInfo.titleRes, Toast.LENGTH_LONG)
            }

            is BillingEvent.ShowError -> {
                if (event.error is RuStoreException) {
                    event.error.resolveForBilling(this)
                }
                showErrorToast(event.error.message.orEmpty(), Toast.LENGTH_LONG)
            }
        }
    }
}
