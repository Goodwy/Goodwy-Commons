package com.goodwy.commons.activities

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.*
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toUri
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.goodwy.commons.R
import com.goodwy.commons.compose.extensions.config
import com.goodwy.commons.databinding.ActivityPurchaseBinding
import com.goodwy.commons.dialogs.BottomSheetChooserDialog
import com.goodwy.commons.dialogs.ConfirmationDialog
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.*
import com.goodwy.commons.helpers.rustore.RuStoreHelper
import com.goodwy.commons.helpers.rustore.RuStoreModule
import com.goodwy.commons.helpers.rustore.model.BillingEvent
import com.goodwy.commons.helpers.rustore.model.BillingState
import com.goodwy.commons.helpers.rustore.model.StartPurchasesEvent
import com.goodwy.commons.models.SimpleListItem
import kotlinx.coroutines.*
import ru.rustore.sdk.billingclient.RuStoreBillingClient
import ru.rustore.sdk.billingclient.utils.resolveForBilling
import ru.rustore.sdk.core.exception.RuStoreException
import ru.rustore.sdk.core.feature.model.FeatureAvailabilityResult

class PurchaseActivity : BaseSimpleActivity() {

    private var appName = ""
    private var primaryColor = 0
    private var licensingKey = ""
    private var productIdX1 = ""
    private var productIdX2 = ""
    private var productIdX3 = ""
    private var subscriptionIdX1 = ""
    private var subscriptionIdX2 = ""
    private var subscriptionIdX3 = ""
    private var showLifebuoy = true
    private var playStoreInstalled = true
    private var ruStoreInstalled = false
    private var showCollection = false

    private var ruStoreIsConnected = false

    private val purchaseHelper = PurchaseHelper(this)
    //private val ruStoreBillingClient: RuStoreBillingClient = RuStoreModule.provideRuStoreBillingClient()

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    private val binding by viewBinding(ActivityPurchaseBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = false
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null && ruStoreInstalled) {
            RuStoreModule.provideRuStoreBillingClient().onNewIntent(intent)
        }
        setContentView(binding.root)
        appName = intent.getStringExtra(APP_NAME) ?: ""
        licensingKey = intent.getStringExtra(GOOGLE_PLAY_LICENSING_KEY) ?: ""
        productIdX1 = intent.getStringExtra(PRODUCT_ID_X1) ?: ""
        productIdX2 = intent.getStringExtra(PRODUCT_ID_X2) ?: ""
        productIdX3 = intent.getStringExtra(PRODUCT_ID_X3) ?: ""
        subscriptionIdX1 = intent.getStringExtra(SUBSCRIPTION_ID_X1) ?: ""
        subscriptionIdX2 = intent.getStringExtra(SUBSCRIPTION_ID_X2) ?: ""
        subscriptionIdX3 = intent.getStringExtra(SUBSCRIPTION_ID_X3) ?: ""
        primaryColor = getProperPrimaryColor()
        showLifebuoy = intent.getBooleanExtra(SHOW_LIFEBUOY, true)
        playStoreInstalled = intent.getBooleanExtra(PLAY_STORE_INSTALLED, true)
        ruStoreInstalled = intent.getBooleanExtra(RU_STORE, false)
        showCollection = intent.getBooleanExtra(SHOW_COLLECTION, false)

        // TODO TRANSPARENT Navigation Bar
        setWindowTransparency(true) { _, _, leftNavigationBarSize, rightNavigationBarSize ->
            binding.purchaseCoordinator.setPadding(leftNavigationBarSize, 0, rightNavigationBarSize, 0)
            updateNavigationBarColor(getProperBackgroundColor())
        }

        arrayOf(
            binding.purchaseNestedScrollview,
            binding.topDetails.root
        ).forEach {
            it.beInvisibleIf(!playStoreInstalled && !ruStoreInstalled)
        }

        arrayOf(
            binding.proHolder,
            binding.proDonateText,
            binding.proDonateButton
        ).forEach {
            it.beGoneIf(playStoreInstalled || ruStoreInstalled)
        }

        if ((playStoreInstalled && !ruStoreInstalled) || (playStoreInstalled && ruStoreInstalled && baseConfig.useGooglePlay)) {
            //PlayStore
            purchaseHelper.initBillingClient()
            val iapList: ArrayList<String> = arrayListOf(productIdX1, productIdX2, productIdX3)
            val subList: ArrayList<String> = arrayListOf(subscriptionIdX1, subscriptionIdX2, subscriptionIdX3)
            purchaseHelper.retrieveDonation(iapList, subList)

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
            RuStoreHelper(this).checkPurchasesAvailability()

//            lifecycleScope.launch {
//                ruStoreHelper.stateStart
//                    .flowWithLifecycle(lifecycle)
//                    .collect { state ->
//                        // update button
//                    }
//            }
            lifecycleScope.launch {
                RuStoreHelper(this@PurchaseActivity).eventStart
                    .flowWithLifecycle(lifecycle)
                    .collect { event ->
                        handleEventStart(event)
                    }
            }

            lifecycleScope.launch {
                RuStoreHelper(this@PurchaseActivity).stateBilling
                    .flowWithLifecycle(lifecycle)
                    .collect { state ->
                        if (!state.isLoading) {
                            //price update
                            setupButtonRuStore(state)
                        }
                    }
            }
            lifecycleScope.launch {
                RuStoreHelper(this@PurchaseActivity).eventBilling
                    .flowWithLifecycle(lifecycle)
                    .collect { event ->
                        handleEventBilling(event)
                    }
            }

            lifecycleScope.launch {
                RuStoreHelper(this@PurchaseActivity).statePurchased
                    .flowWithLifecycle(lifecycle)
                    .collect { state ->
                        if (!state.isLoading && ruStoreIsConnected) {
                            //update of purchased
                            setupButtonCheckedRuStore(state.purchases)
                            //update pro version
                            baseConfig.isProRuStore = state.purchases.firstOrNull() != null
                        }
                        binding.purchaseToolbar.menu.findItem(R.id.openSubscriptions).isVisible = ruStoreIsConnected
                    }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (ruStoreInstalled) {
            RuStoreModule.provideRuStoreBillingClient().onNewIntent(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateTextColors(binding.purchaseCoordinator)
        setupOptionsMenu()
        setupToolbar(binding.purchaseToolbar, NavigationIcon.Arrow)
        val backgroundColor = getProperBackgroundColor()
        binding.collapsingToolbar.setBackgroundColor(backgroundColor)
        updateTopBarColors(binding.purchaseToolbar, backgroundColor)

        setupChangeStoreMenu()
        setupEmail()
        if (showCollection) setupCollection()
        //setupParticipants()
        if (playStoreInstalled || ruStoreInstalled) {
            setupIcon()
        } else {
            setupNoPlayStoreInstalled()
        }

        val isProApp = resources.getBoolean(R.bool.is_pro_app)
        binding.themeHolder.beVisibleIf(!isProApp)
        binding.colorHolder.beVisibleIf(!isProApp)
    }

    private fun setupOptionsMenu() {
        binding.purchaseToolbar.menu.apply {
            findItem(R.id.restorePurchases).isVisible = playStoreInstalled || ruStoreInstalled
        }
        binding.purchaseToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.restorePurchases -> {
                    setupButtonReset()
                    if (ruStoreInstalled && !baseConfig.useGooglePlay) updateProducts()
                    else {
                        val iapList: ArrayList<String> = arrayListOf(productIdX1, productIdX2, productIdX3)
                        val subList: ArrayList<String> = arrayListOf(subscriptionIdX1, subscriptionIdX2, subscriptionIdX3)
                        purchaseHelper.retrieveDonation(iapList, subList)
                    }
                    true
                }
                R.id.openSubscriptions -> {
                    launchViewIntent("rustore://profile/subscriptions")
                    true
                }
                else -> false
            }
        }
    }

    private fun setupChangeStoreMenu() {
        binding.purchaseToolbar.menu.findItem(R.id.changeStore).apply {
            isVisible = playStoreInstalled && ruStoreInstalled
            title = if (baseConfig.useGooglePlay) getString(R.string.billing_change_to_ru_store) else getString(R.string.billing_change_to_google_play)
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
        /*val label = getString(R.string.lifebuoy_summary)
        val email = getString(R.string.my_email)

        val appVersion = String.format(getString(R.string.app_version, intent.getStringExtra(APP_VERSION_NAME)))
        val deviceOS = String.format(getString(R.string.device_os), Build.VERSION.RELEASE)
        val newline = "%0D%0A"
        val separator = "------------------------------"
        val body = "$appVersion$newline$deviceOS$newline$separator$newline$newline"
        val href = "$label<br><a href=\"mailto:$email?subject=$appName&body=$body\">$email</a>"
        lifebuoy_summary.text = Html.fromHtml(href)
        lifebuoy_summary.movementMethod = LinkMovementMethod.getInstance()*/

        binding.lifebuoyHolder.beVisibleIf(showLifebuoy)
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
            val price = purchaseHelper.getPriceDonation(productIdX1)
            isEnabled = price != getString(R.string.no_connection)
            text = price
            setOnClickListener {
                purchaseHelper.getDonation(productIdX1)
            }
            val drawable = resources.getColoredDrawableWithColor(context, R.drawable.button_gray_bg_8dp, primaryColor)
            background = drawable
            setPadding(2,2,2,2)
        }

        binding.appTwoButton.apply {
            val price = purchaseHelper.getPriceDonation(productIdX2)
            isEnabled = price != getString(R.string.no_connection)
            text = price
            setOnClickListener {
                purchaseHelper.getDonation(productIdX2)
            }
            val drawable = resources.getColoredDrawableWithColor(context, R.drawable.button_gray_bg_8dp, primaryColor)
            background = drawable
            setPadding(2,2,2,2)
        }

        binding.appThreeButton.apply {
            val price = purchaseHelper.getPriceDonation(productIdX3)
            isEnabled = price != getString(R.string.no_connection)
            text = price
            setOnClickListener {
                purchaseHelper.getDonation(productIdX3)
            }
            val drawable = resources.getColoredDrawableWithColor(context, R.drawable.button_gray_bg_8dp, primaryColor)
            background = drawable
            setPadding(2,2,2,2)
        }
    }

    private fun setupButtonIapChecked() {
        val check = AppCompatResources.getDrawable(this@PurchaseActivity, R.drawable.ic_check_circle_vector)
        if (purchaseHelper.isIapPurchased(productIdX1)) {
            binding.appOneButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appOneButton.isEnabled = false
        }
        if (purchaseHelper.isIapPurchased(productIdX2)) {
            binding.appTwoButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appTwoButton.isEnabled = false
        }
        if (purchaseHelper.isIapPurchased(productIdX3)) {
            binding.appThreeButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appThreeButton.isEnabled = false
        }
    }

    private fun setupButtonSupPurchased() {
        binding.appOneSubButton.apply {
            val price = purchaseHelper.getPriceSubscription(subscriptionIdX1)
            if (price != getString(R.string.no_connection)) {
                isEnabled = true
                val textPrice = String.format(getString(R.string.per_month), price)
                text = textPrice
                setOnClickListener {
                    purchaseHelper.getSubscription(subscriptionIdX1)
                }
            } else {
                text = price
            }
            val drawable = resources.getColoredDrawableWithColor(context, R.drawable.button_gray_bg_8dp, primaryColor)
            background = drawable
            setPadding(2,2,2,2)
        }

        binding.appTwoSubButton.apply {
            val price = purchaseHelper.getPriceSubscription(subscriptionIdX2)
            if (price != getString(R.string.no_connection)) {
                isEnabled = true
                val textPrice = String.format(getString(R.string.per_month), price)
                text = textPrice
                setOnClickListener {
                    purchaseHelper.getSubscription(subscriptionIdX2)
                }
            } else {
                text = price
            }
            val drawable = resources.getColoredDrawableWithColor(context, R.drawable.button_gray_bg_8dp, primaryColor)
            background = drawable
            setPadding(2,2,2,2)
        }

        binding.appThreeSubButton.apply {
            val price = purchaseHelper.getPriceSubscription(subscriptionIdX3)
            if (price != getString(R.string.no_connection)) {
                isEnabled = true
                val textPrice = String.format(getString(R.string.per_month), price)
                text = textPrice
                setOnClickListener {
                    purchaseHelper.getSubscription(subscriptionIdX3)
                }
            } else {
                text = price
            }
            val drawable = resources.getColoredDrawableWithColor(context, R.drawable.button_gray_bg_8dp, primaryColor)
            background = drawable
            setPadding(2,2,2,2)
        }
    }

    private fun setupButtonSupChecked() {
        val check = AppCompatResources.getDrawable(this@PurchaseActivity, R.drawable.ic_check_circle_vector)
        if (purchaseHelper.isSubPurchased(subscriptionIdX1)) {
            binding.appOneSubButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appOneSubButton.isEnabled = false
        }
        if (purchaseHelper.isSubPurchased(subscriptionIdX2)) {
            binding.appTwoSubButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appTwoSubButton.isEnabled = false
        }
        if (purchaseHelper.isSubPurchased(subscriptionIdX3)) {
            binding.appThreeSubButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appThreeSubButton.isEnabled = false
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

    private fun setupNoPlayStoreInstalled() {
        binding.proDonateText.text = Html.fromHtml(getString(R.string.donate_text_g))
        binding.proDonateButton.apply {
            setOnClickListener {
                launchViewIntent("https://sites.google.com/view/goodwy/support-project")
            }
            val drawable = resources.getColoredDrawableWithColor(context, R.drawable.button_gray_bg, primaryColor)
            background = drawable
            //setTextColor(baseConfig.backgroundColor)
            setPadding(2,2,2,2)
        }
        binding.proSwitch.isChecked = baseConfig.isPro
        binding.proSwitchHolder.setOnClickListener {
            binding.proSwitch.toggle()
            baseConfig.isPro = binding.proSwitch.isChecked
        }
    }

    @SuppressLint("NewApi", "SetTextI18n", "UseCompatTextViewDrawableApis")
    private fun setupCollection() {
        binding.collectionHolder.beVisible()
        val appDialerPackage = "com.goodwy.dialer"
        val appContactsPackage = "com.goodwy.contacts"
        val appSmsMessengerPackage = "com.goodwy.smsmessenger"
        val appGalleryPackage = "com.goodwy.gallery"
        //val appVoiceRecorderPackage = "com.goodwy.voicerecorder"
        val appAudiobookLitePackage = "com.goodwy.audiobooklite"
        val appFilesPackage = "com.goodwy.filemanager"

        val appDialerInstalled = isPackageInstalled(appDialerPackage)// || isPackageInstalled("com.goodwy.dialer.debug")
        val appContactsInstalled = isPackageInstalled(appContactsPackage)// || isPackageInstalled("com.goodwy.contacts.debug")
        val appSmsMessengerInstalled = isPackageInstalled(appSmsMessengerPackage)// || isPackageInstalled("com.goodwy.smsmessenger.debug")
        val appGalleryInstalled = isPackageInstalled(appGalleryPackage)// || isPackageInstalled("com.goodwy.voicerecorder.debug")
        //val appVoiceRecorderInstalled = isPackageInstalled(appVoiceRecorderPackage)// || isPackageInstalled("com.goodwy.voicerecorder.debug")
        val appAudiobookLiteInstalled = isPackageInstalled(appAudiobookLitePackage)// || isPackageInstalled("com.goodwy.voicerecorder.debug")
        val appFilesInstalled = isPackageInstalled(appFilesPackage)// || isPackageInstalled("com.goodwy.filemanager.debug")

        val appAllInstalled = appDialerInstalled && appContactsInstalled && appSmsMessengerInstalled && appGalleryInstalled && appAudiobookLiteInstalled && appFilesInstalled

        if (!appAllInstalled) binding.collectionLogo.applyColorFilter(primaryColor)
        binding.collectionChevron.applyColorFilter(getProperTextColor())
        binding.collectionSubtitle.background.applyColorFilter(getBottomNavigationBackgroundColor())

        val items = arrayOf(
            SimpleListItem(1, R.string.right_dialer, imageRes = R.drawable.ic_dialer, selected = appDialerInstalled, packageName = appDialerPackage),
            SimpleListItem(2, R.string.right_contacts, imageRes = R.drawable.ic_contacts, selected = appContactsInstalled, packageName = appContactsPackage),
            SimpleListItem(3, R.string.right_sms_messenger, imageRes = R.drawable.ic_sms_messenger, selected = appSmsMessengerInstalled, packageName = appSmsMessengerPackage),
            SimpleListItem(4, R.string.right_gallery, imageRes = R.drawable.ic_gallery, selected = appGalleryInstalled, packageName = appGalleryPackage),
            SimpleListItem(5, R.string.playbook, imageRes = R.drawable.ic_playbook, selected = appAudiobookLiteInstalled, packageName = appAudiobookLitePackage),
            //SimpleListItem(6, R.string.right_voice_recorder, R.drawable.ic_voice_recorder, selected = appVoiceRecorderInstalled, packageName = appVoiceRecorderPackage),
            SimpleListItem(5, R.string.right_files, imageRes = R.drawable.ic_files, selected = appFilesInstalled, packageName = appFilesPackage)
        )

        val percentage = items.filter { it.selected }.size.toString() + "/" + items.size.toString()
        binding.collectionTitle.text = getString(R.string.collection) + "  $percentage"

        binding.collectionHolder.setOnClickListener {
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
            val product = state.products.firstOrNull {  it.productId == productIdX1  }
            val price = product?.priceLabel ?: getString(R.string.no_connection)
            isEnabled = price != getString(R.string.no_connection)
            val resultPrice = price.replace(".00","",true)
            text = resultPrice
            setOnClickListener {
                if (product != null) {
                    RuStoreHelper(this@PurchaseActivity).purchaseProduct(product)
                }
            }
            val drawable = resources.getColoredDrawableWithColor(context, R.drawable.button_gray_bg_8dp, primaryColor)
            background = drawable
            setPadding(2,2,2,2)
        }

        binding.appTwoButton.apply {
            val product = state.products.firstOrNull {  it.productId == productIdX2  }
            val price = product?.priceLabel ?: getString(R.string.no_connection)
            isEnabled = price != getString(R.string.no_connection)
            val resultPrice = price.replace(".00","",true)
            text = resultPrice
            setOnClickListener {
                if (product != null) {
                    RuStoreHelper(this@PurchaseActivity).purchaseProduct(product)
                }
            }
            val drawable = resources.getColoredDrawableWithColor(context, R.drawable.button_gray_bg_8dp, primaryColor)
            background = drawable
            setPadding(2,2,2,2)
        }

        binding.appThreeButton.apply {
            val product = state.products.firstOrNull {  it.productId == productIdX3  }
            val price = product?.priceLabel ?: getString(R.string.no_connection)
            isEnabled = price != getString(R.string.no_connection)
            val resultPrice = price.replace(".00","",true)
            text = resultPrice
            setOnClickListener {
                if (product != null) {
                    RuStoreHelper(this@PurchaseActivity).purchaseProduct(product)
                }
            }
            val drawable = resources.getColoredDrawableWithColor(context, R.drawable.button_gray_bg_8dp, primaryColor)
            background = drawable
            setPadding(2,2,2,2)
        }

        binding.appOneSubButton.apply {
            val product = state.products.firstOrNull {  it.productId == subscriptionIdX1  }
            val price = product?.priceLabel ?: getString(R.string.no_connection)
            if (price != getString(R.string.no_connection)) {
                isEnabled = true
                val resultPrice = price.replace(".00","",true)
                val textPrice = String.format(getString(R.string.per_month), resultPrice)
                text = textPrice
                setOnClickListener {
                    if (product != null) {
                        RuStoreHelper(this@PurchaseActivity).purchaseProduct(product)
                    }
                }
            } else {
                text = price
            }
            val drawable = resources.getColoredDrawableWithColor(context, R.drawable.button_gray_bg_8dp, primaryColor)
            background = drawable
            setPadding(2,2,2,2)
        }

        binding.appTwoSubButton.apply {
            val product = state.products.firstOrNull {  it.productId == subscriptionIdX2  }
            val price = product?.priceLabel ?: getString(R.string.no_connection)
            if (price != getString(R.string.no_connection)) {
                isEnabled = true
                val resultPrice = price.replace(".00","",true)
                val textPrice = String.format(getString(R.string.per_month), resultPrice)
                text = textPrice
                setOnClickListener {
                    if (product != null) {
                        RuStoreHelper(this@PurchaseActivity).purchaseProduct(product)
                    }
                }
            } else {
                text = price
            }
            val drawable = resources.getColoredDrawableWithColor(context, R.drawable.button_gray_bg_8dp, primaryColor)
            background = drawable
            setPadding(2,2,2,2)
        }

        binding.appThreeSubButton.apply {
            val product = state.products.firstOrNull {  it.productId == subscriptionIdX3  }
            val price = product?.priceLabel ?: getString(R.string.no_connection)
            if (price != getString(R.string.no_connection)) {
                isEnabled = true
                val resultPrice = price.replace(".00","",true)
                val textPrice = String.format(getString(R.string.per_month), resultPrice)
                text = textPrice
                setOnClickListener {
                    if (product != null) {
                        RuStoreHelper(this@PurchaseActivity).purchaseProduct(product)
                    }
                }
            } else {
                text = price
            }
            val drawable = resources.getColoredDrawableWithColor(context, R.drawable.button_gray_bg_8dp, primaryColor)
            background = drawable
            setPadding(2,2,2,2)
        }
    }

    private fun setupButtonCheckedRuStore(state: List<ru.rustore.sdk.billingclient.model.purchase.Purchase>) {
        val check = AppCompatResources.getDrawable(this@PurchaseActivity, R.drawable.ic_check_circle_vector)
        if (state.firstOrNull {  it.productId == productIdX1  } != null) {
            binding.appOneButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appOneButton.isEnabled = false
        }
        if (state.firstOrNull {  it.productId == productIdX2  } != null) {
            binding.appTwoButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appTwoButton.isEnabled = false
        }
        if (state.firstOrNull {  it.productId == productIdX3  } != null) {
            binding.appThreeButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appThreeButton.isEnabled = false
        }
        if (state.firstOrNull {  it.productId == subscriptionIdX1  } != null) {
            binding.appOneSubButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appOneSubButton.isEnabled = false
        }
        if (state.firstOrNull {  it.productId == subscriptionIdX2  } != null) {
            binding.appTwoSubButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appTwoSubButton.isEnabled = false
        }
        if (state.firstOrNull {  it.productId == subscriptionIdX3  } != null) {
            binding.appThreeSubButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
            binding.appThreeSubButton.isEnabled = false
        }
    }

    private fun updateProducts() {
        val productList: ArrayList<String> = arrayListOf(productIdX1, productIdX2, productIdX3, subscriptionIdX1, subscriptionIdX2, subscriptionIdX3)
        RuStoreHelper(this@PurchaseActivity).getProducts(productList)
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
                        event.availability.cause.resolveForBilling(this) //Show error dialog
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

    companion object {
        private const val TAG: String = "PurchaseActivity"
    }
}
