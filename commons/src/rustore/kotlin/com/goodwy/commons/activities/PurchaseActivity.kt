package com.goodwy.commons.activities

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.goodwy.commons.models.MyTheme
import com.goodwy.commons.models.SimpleListItem
import com.goodwy.strings.R as stringsR
import kotlinx.coroutines.*
import ru.rustore.sdk.billingclient.RuStoreBillingClient
import ru.rustore.sdk.billingclient.utils.resolveForBilling
import ru.rustore.sdk.core.exception.RuStoreException
import ru.rustore.sdk.core.feature.model.FeatureAvailabilityResult

class PurchaseActivity : BaseSimpleActivity() {

    private var firstVersionClickTS = 0L
    private var clicksSinceFirstClick = 0

    companion object {
        private const val EASTER_EGG_TIME_LIMIT = 8000L
        private const val EASTER_EGG_REQUIRED_CLICKS = 7
        private const val EASTER_EGG_REQUIRED_CLICKS_NEXT = 10
        private const val THEME_LIGHT = 0
        private const val THEME_DARK = 1
        private const val THEME_BLACK = 2
        private const val THEME_GRAY = 3
    }

    private var appName = ""
    private var primaryColor = 0
    private var productIdList: ArrayList<String> = ArrayList()
    private var productIdListRu: ArrayList<String> = ArrayList()
    private var subscriptionIdList: ArrayList<String> = ArrayList()
    private var subscriptionIdListRu: ArrayList<String> = ArrayList()
    private var subscriptionYearIdList: ArrayList<String> = ArrayList()
    private var subscriptionYearIdListRu: ArrayList<String> = ArrayList()
    private var showLifebuoy = true
    private var showCollection = false
    private val predefinedThemes = LinkedHashMap<Int, MyTheme>()

    private var ruStoreIsConnected = false

    private var ruStoreHelper: RuStoreHelper? = null
    private var ruStoreBillingClient: RuStoreBillingClient? = null

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun getRepositoryName() = null

    private val binding by viewBinding(ActivityPurchaseBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
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
        showCollection = intent.getBooleanExtra(SHOW_COLLECTION, false)

        ruStoreHelper = try {
            RuStoreHelper()
        } catch (_: Exception) {
            null
        }

        ruStoreBillingClient = try {
            RuStoreModule.provideRuStoreBillingClient()
        } catch (_: Exception) {
            null
        }

        if (savedInstanceState == null) {
            ruStoreBillingClient?.onNewIntent(intent)
        }

        ruStoreHelper?.checkPurchasesAvailability(this)

//            lifecycleScope.launch {
//                ruStoreHelper?.stateStart
//                    .flowWithLifecycle(lifecycle)
//                    .collect { state ->
//                        // update button
//                    }
//            }
        if (ruStoreHelper != null) {
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
        ruStoreBillingClient?.onNewIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        updateTextColors(binding.purchaseCoordinator)
        setupOptionsMenu()

        val backgroundColor = getProperBackgroundColor()
        setupToolbar(binding.purchaseToolbar, NavigationIcon.Arrow)
        updateToolbarColors(binding.purchaseToolbar, backgroundColor, useOverflowIcon = false)
        binding.purchaseAppBarLayout.setBackgroundColor(backgroundColor)
        binding.collapsingToolbar.setBackgroundColor(backgroundColor)

        setupTheme()
        setupEmail()

        if (showCollection) setupCollection()
        setupIcon()

        val isProApp = resources.getBoolean(R.bool.is_pro_app)
        binding.themeHolder.beVisibleIf(!isProApp)
        binding.colorHolder.beVisibleIf(!isProApp)
    }

    private fun setupOptionsMenu() {
        binding.purchaseToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.restorePurchases -> {
                    setupButtonReset()
                    updateProducts()
                    true
                }
                R.id.openSubscriptions -> {
                    val url = "rustore://profile/subscriptions"
                    launchViewIntent(url)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupTheme() {
        binding.themeHolder.setOnClickListener {
            onThemeClick()
        }
    }

    private fun setupEmail() {
        binding.lifebuoyHolder.beVisibleIf(showLifebuoy)
        val lifebuoyButtonDrawable = resources.getColoredDrawableWithColor(this, R.drawable.ic_mail_vector, getProperTextColor())
        binding.lifebuoyButton.setImageDrawable(lifebuoyButtonDrawable)
        binding.lifebuoyButton.setOnClickListener {
            ConfirmationDialog(this, getString(R.string.send_email)) {
                val body = "$appName : Lifebuoy"
                val address = getMyMailString()
                val selectorIntent = Intent(ACTION_SENDTO)
                    .setData("mailto:$address".toUri())
                val emailIntent = Intent(ACTION_SEND).apply {
                    putExtra(EXTRA_EMAIL, arrayOf(address))
                    putExtra(EXTRA_SUBJECT, body)
                    selector = selectorIntent
                }

                try {
                    startActivity(emailIntent)
                } catch (_: ActivityNotFoundException) {
                    toast(R.string.no_app_found)
                } catch (e: Exception) {
                    showErrorToast(e)
                }
            }
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

        binding.goodwyLogo.apply {
            applyColorFilter(getProperTextColor())
            setOnClickListener {
                launchViewIntent(getString(R.string.my_website))
            }
        }
        binding.goodwyTitle.setOnClickListener {
            launchViewIntent(getString(R.string.my_website))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupCollection() {
        binding.collectionHolder.beVisible()
        val prefix = appPrefix()
        val appDialerPackage = prefix + "goodwy.dialer"
        val appContactsPackage = prefix + "goodwy.contacts"
        val appSmsMessengerPackage = prefix + "goodwy.smsmessenger"
        val appGalleryPackage = prefix + "goodwy.gallery"
        val appAudiobookLitePackage = prefix + "goodwy.audiobooklite"
        val appFilesPackage = prefix + "goodwy.filemanager"
        val appKeyboardPackage = prefix + "goodwy.keyboard"
        val appCalendarPackage = prefix + "goodwy.calendar"
//        val appVoiceRecorderPackage = prefix + "goodwy.voicerecorderfree"

        val appDialerInstalled = isPackageInstalled(appDialerPackage)
        val appContactsInstalled = isPackageInstalled(appContactsPackage)
        val appSmsMessengerInstalled = isPackageInstalled(appSmsMessengerPackage)
        val appGalleryInstalled = isPackageInstalled(appGalleryPackage)
        val appAudiobookLiteInstalled = isPackageInstalled(appAudiobookLitePackage)
        val appFilesInstalled = isPackageInstalled(appFilesPackage)
        val appKeyboardInstalled = isPackageInstalled(appKeyboardPackage)
        val appCalendarInstalled = isPackageInstalled(appCalendarPackage)
//        val appVoiceRecorderInstalled = isPackageInstalled(appVoiceRecorderPackage)

        val appAllInstalled = appDialerInstalled && appContactsInstalled && appSmsMessengerInstalled && appGalleryInstalled &&
            appAudiobookLiteInstalled && appFilesInstalled && appKeyboardInstalled && appCalendarInstalled //&& appVoiceRecorderInstalled

        if (!appAllInstalled) binding.collectionLogo.applyColorFilter(primaryColor)
        binding.collectionChevron.applyColorFilter(getProperTextColor())
        binding.collectionSubtitle.background.applyColorFilter(getSurfaceColor())

        val items = arrayOf(
            SimpleListItem(1, R.string.right_dialer, imageRes = R.drawable.ic_dialer, selected = appDialerInstalled, packageName = appDialerPackage),
            SimpleListItem(2, R.string.right_contacts, imageRes = R.drawable.ic_contacts, selected = appContactsInstalled, packageName = appContactsPackage),
            SimpleListItem(3, R.string.right_sms_messenger, imageRes = R.drawable.ic_sms_messenger, selected = appSmsMessengerInstalled, packageName = appSmsMessengerPackage),
            SimpleListItem(4, R.string.right_gallery, imageRes = R.drawable.ic_gallery, selected = appGalleryInstalled, packageName = appGalleryPackage),
            SimpleListItem(5, R.string.right_files, imageRes = R.drawable.ic_files, selected = appFilesInstalled, packageName = appFilesPackage),
            SimpleListItem(6, R.string.playbook, imageRes = R.drawable.ic_playbook, selected = appAudiobookLiteInstalled, packageName = appAudiobookLitePackage),
            SimpleListItem(7, R.string.right_keyboard, imageRes = R.drawable.ic_inkwell, selected = appKeyboardInstalled, packageName = appKeyboardPackage),
            SimpleListItem(8, R.string.right_calendar, imageRes = R.drawable.ic_calendar_app, selected = appCalendarInstalled, packageName = appCalendarPackage),
            //SimpleListItem(9, R.string.right_voice_recorder, imageRes = R.drawable.ic_voice_recorder, selected = appVoiceRecorderInstalled, packageName = appVoiceRecorderPackage)
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
                    val urlRS = "https://www.rustore.ru/catalog/app/${it.packageName}"
                    launchViewIntent(urlRS)
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
        } catch (_: Exception) {
            try {
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                startActivity(launchIntent)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }

    private fun setupButtonRuStore(state: BillingState) {
        binding.appOneButton.apply {
            val product = state.products.firstOrNull {  it.productId == productIdListRu[0]  }
            val price = product?.priceLabel ?: getString(stringsR.string.no_connection)
            isEnabled = price != getString(stringsR.string.no_connection)
            val resultPrice = price.replace(".00","",true)
            text = resultPrice
            setOnClickListener {
                if (product != null) {
                    ruStoreHelper?.purchaseProduct(product)
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
                    ruStoreHelper?.purchaseProduct(product)
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
                    ruStoreHelper?.purchaseProduct(product)
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
                        ruStoreHelper?.purchaseProduct(product)
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
                        ruStoreHelper?.purchaseProduct(product)
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
                        ruStoreHelper?.purchaseProduct(product)
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
                        ruStoreHelper?.purchaseProduct(product)
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
                        ruStoreHelper?.purchaseProduct(product)
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
                        ruStoreHelper?.purchaseProduct(product)
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
        ruStoreHelper?.getProducts(productList)
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

    private fun onThemeClick() {
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
        } else if (clicksSinceFirstClick >= EASTER_EGG_REQUIRED_CLICKS_NEXT && !isPro()) {
            firstVersionClickTS = 0L
            clicksSinceFirstClick = 0

            if ((0..50).random() == 10 || baseConfig.appRunCount % 100 == 0) {
                toast("You did not hack the system ;(")
            } else if (!isAutoTheme() && !isDynamicTheme()) {
                val text = when {
                    isLightTheme() -> "You hacked the system ;("
                    isGrayTheme() -> "It got dark"
                    isDarkTheme() -> "Blackness"
                    else -> "Light"
                }
                toast(text)

                val themeId = when {
                    isLightTheme() -> THEME_GRAY
                    isGrayTheme() -> THEME_DARK
                    isDarkTheme() -> THEME_BLACK
                    else -> THEME_LIGHT
                }
                updateTheme(themeId)
            } else {
                toast(R.string.hello)
            }
        }
    }

    private fun updateTheme(themeId: Int) {
        setupThemes()
        val theme = predefinedThemes[themeId]!!
        baseConfig.textColor = getColor(theme.textColorId)
        baseConfig.backgroundColor = getColor(theme.backgroundColorId)
        baseConfig.primaryColor = getColor(theme.primaryColorId)

        setTheme(getThemeId())
        recreate()
    }

    private fun setupThemes() {
        predefinedThemes.apply {
            put(
                THEME_LIGHT,
                MyTheme(
                    labelId = R.string.light_theme,
                    textColorId = R.color.theme_light_text_color,
                    backgroundColorId = R.color.theme_light_background_color,
                    primaryColorId = R.color.color_primary,
                    appIconColorId = baseConfig.customAppIconColor
                )
            )
            put(
                THEME_GRAY,
                MyTheme(
                    labelId = stringsR.string.gray_theme,
                    textColorId = R.color.theme_gray_text_color,
                    backgroundColorId = R.color.theme_gray_background_color,
                    primaryColorId = R.color.color_primary,
                    appIconColorId = baseConfig.customAppIconColor
                )
            )
            put(
                THEME_DARK,
                MyTheme(
                    labelId = R.string.dark_theme,
                    textColorId = R.color.theme_dark_text_color,
                    backgroundColorId = R.color.theme_dark_background_color,
                    primaryColorId = R.color.color_primary,
                    appIconColorId = baseConfig.customAppIconColor
                )
            )
            put(
                THEME_BLACK,
                MyTheme(
                    labelId = stringsR.string.black,
                    textColorId = R.color.theme_black_text_color,
                    backgroundColorId = R.color.theme_black_background_color,
                    primaryColorId = R.color.color_primary,
                    appIconColorId = baseConfig.customAppIconColor
                )
            )
        }
    }
}
