package com.goodwy.commons.activities

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.*
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toUri
import com.goodwy.commons.R
import com.goodwy.commons.dialogs.BottomSheetChooserDialog
import com.goodwy.commons.dialogs.ConfirmationDialog
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.*
import com.goodwy.commons.models.Purchase
import com.goodwy.commons.models.SimpleListItem
import kotlinx.android.synthetic.main.activity_purchase.*
import kotlinx.android.synthetic.main.top_view_purchase.*
import kotlinx.coroutines.*

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
    private var showCollection = false

    private val purchaseHelper = PurchaseHelper(this)

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = false
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase)
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
        showCollection = intent.getBooleanExtra(SHOW_COLLECTION, false)

        if (playStoreInstalled) {
            purchaseHelper.initBillingClient()
            val iapList: ArrayList<String> = arrayListOf(productIdX1, productIdX2, productIdX3)
            val subList: ArrayList<String> = arrayListOf(subscriptionIdX1, subscriptionIdX2, subscriptionIdX3)
            purchaseHelper.retrieveDonation(iapList, subList)

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
                }
            }

            purchaseHelper.isIapPurchasedList.observe(this) {
                setupButtonIapPurchased()
            }
            purchaseHelper.isSupPurchasedList.observe(this) {
                setupButtonSupPurchased()
            }
        }

        // TODO TRANSPARENT Navigation Bar
        setWindowTransparency(true) { _, _, leftNavigationBarSize, rightNavigationBarSize ->
            purchase_coordinator.setPadding(leftNavigationBarSize, 0, rightNavigationBarSize, 0)
            updateNavigationBarColor(getProperBackgroundColor())
        }

        arrayOf(
            purchase_nested_scrollview,
            top_details
        ).forEach {
            it.beInvisibleIf(!playStoreInstalled)
        }

        arrayOf(
            pro_holder,
            pro_donate_text,
            pro_donate_button
        ).forEach {
            it.beGoneIf(playStoreInstalled)
        }
    }

    override fun onResume() {
        super.onResume()
        updateTextColors(purchase_coordinator)
        setupOptionsMenu()
        setupToolbar(purchase_toolbar, NavigationIcon.Arrow)
        val backgroundColor = getProperBackgroundColor()
        collapsing_toolbar.setBackgroundColor(backgroundColor)
        updateTopBarColors(purchase_toolbar, backgroundColor)

        setupEmail()
        if (showCollection) setupCollection()
        //setupParticipants()
        setupIcon()
        setupNoPlayStoreInstalled()

        val isProApp = resources.getBoolean(R.bool.is_pro_app)
        theme_holder.beVisibleIf(!isProApp)
        color_holder.beVisibleIf(!isProApp)
    }

    private fun setupOptionsMenu() {
        purchase_toolbar.menu.apply {
            findItem(R.id.restorePurchases).isVisible = playStoreInstalled
        }
        purchase_toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.restorePurchases -> {
                    //restorePurchase()
                    setupButtonReset()
                    val iapList: ArrayList<String> = arrayListOf(productIdX1, productIdX2, productIdX3)
                    val subList: ArrayList<String> = arrayListOf(subscriptionIdX1, subscriptionIdX2, subscriptionIdX3)
                    purchaseHelper.retrieveDonation(iapList, subList)
                    true
                }
                else -> false
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

        lifebuoy_holder.beVisibleIf(showLifebuoy)
        val lifebuoyButtonDrawable = resources.getColoredDrawableWithColor(R.drawable.ic_mail_vector, getProperTextColor())
        lifebuoy_button.setImageDrawable(lifebuoyButtonDrawable)
        lifebuoy_button.setOnClickListener {
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

//    private fun setupParticipants() {
//        goodwy_logo.setOnClickListener {
//            launchMoreAppsFromUsIntent()
//        }
//    }

//    private fun setupButton() {
//        setupButtonOne()
//        setupButtonTwo()
//        setupButtonThree()
//        Handler().postDelayed({
//            setupButtonPurchased()
//        }, 500)
//    }

    private fun setupButtonIapPurchased() {
        val check = AppCompatResources.getDrawable(this, R.drawable.ic_check_circle_vector)

        app_one_button.apply {
            val price = purchaseHelper.getPriceDonation(productIdX1)
            isEnabled = price != getString(R.string.no_connection)
            text = price
            setOnClickListener {
                purchaseHelper.getDonation(productIdX1)
            }
            val drawable = resources.getColoredDrawableWithColor(R.drawable.button_gray_bg_8dp, primaryColor)
            background = drawable
            setPadding(2,2,2,2)
            if (purchaseHelper.isIapPurchased(productIdX1)) {
                setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
                isEnabled = false
            }
        }

        app_two_button.apply {
            val price = purchaseHelper.getPriceDonation(productIdX2)
            isEnabled = price != getString(R.string.no_connection)
            text = price
            setOnClickListener {
                purchaseHelper.getDonation(productIdX2)
            }
            val drawable = resources.getColoredDrawableWithColor(R.drawable.button_gray_bg_8dp, primaryColor)
            background = drawable
            setPadding(2,2,2,2)
            if (purchaseHelper.isIapPurchased(productIdX2)) {
                setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
                isEnabled = false
            }
        }

        app_three_button.apply {
            val price = purchaseHelper.getPriceDonation(productIdX3)
            isEnabled = price != getString(R.string.no_connection)
            text = price
            setOnClickListener {
                purchaseHelper.getDonation(productIdX3)
            }
            val drawable = resources.getColoredDrawableWithColor(R.drawable.button_gray_bg_8dp, primaryColor)
            background = drawable
            setPadding(2,2,2,2)
            if (purchaseHelper.isIapPurchased(productIdX3)) {
                setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
                isEnabled = false
            }
        }
    }

    private fun setupButtonSupPurchased() {
        val check = AppCompatResources.getDrawable(this, R.drawable.ic_check_circle_vector)

        app_one_sub_button.apply {
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
            val drawable = resources.getColoredDrawableWithColor(R.drawable.button_gray_bg_8dp, primaryColor)
            background = drawable
            setPadding(2,2,2,2)
            if (purchaseHelper.isSubPurchased(subscriptionIdX1)) {
                setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
                isEnabled = false
            }
        }

        app_two_sub_button.apply {
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
            val drawable = resources.getColoredDrawableWithColor(R.drawable.button_gray_bg_8dp, primaryColor)
            background = drawable
            setPadding(2,2,2,2)
            if (purchaseHelper.isSubPurchased(subscriptionIdX2)) {
                setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
                isEnabled = false
            }
        }

        app_three_sub_button.apply {
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
            val drawable = resources.getColoredDrawableWithColor(R.drawable.button_gray_bg_8dp, primaryColor)
            background = drawable
            setPadding(2,2,2,2)
            if (purchaseHelper.isSubPurchased(subscriptionIdX3)) {
                setCompoundDrawablesWithIntrinsicBounds(null, null, null, check)
                isEnabled = false
            }
        }
    }

    private fun setupButtonReset() {
        app_one_button.apply {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            text = "..."
            isEnabled = false
        }
        app_one_sub_button.apply {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            text = "..."
            isEnabled = false
        }
        app_two_button.apply {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            text = "..."
            isEnabled = false
        }
        app_two_sub_button.apply {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            text = "..."
            isEnabled = false
        }
        app_three_button.apply {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            text = "..."
            isEnabled = false
        }
        app_three_sub_button.apply {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            text = "..."
            isEnabled = false
        }
    }

    private fun setupIcon() {
        val appDrawable = resources.getColoredDrawableWithColor(R.drawable.ic_plus_support, primaryColor)
        app_logo.setImageDrawable(appDrawable)
        val themeDrawable = resources.getColoredDrawableWithColor(R.drawable.ic_invert_colors, primaryColor)
        theme_logo.setImageDrawable(themeDrawable)
        val colorDrawable = resources.getColoredDrawableWithColor(R.drawable.ic_palette, primaryColor)
        color_logo.setImageDrawable(colorDrawable)
        val plusDrawable = resources.getColoredDrawableWithColor(R.drawable.ic_plus_round, primaryColor)
        plus_logo.setImageDrawable(plusDrawable)
        val lifebuoyDrawable = resources.getColoredDrawableWithColor(R.drawable.ic_lifebuoy, primaryColor)
        lifebuoy_logo.setImageDrawable(lifebuoyDrawable)
    }

//    override fun onProductPurchased(productId: String, details: PurchaseInfo?) {
//        toast(R.string.thank_you)
//        setResult(RESULT_OK)
//    }
//
//    override fun onPurchaseHistoryRestored() {
//        if (isProVersion()) {
//            toast(R.string.restored_previous_purchase_please_restart)
//            setResult(RESULT_OK)
//        } else {
//            toast(R.string.no_purchase_found)
//        }
//    }

//    override fun onBillingInitialized() {
//        loadSkuDetails()
//        app_one_button.isEnabled = true
//        app_two_button.isEnabled = true
//        app_three_button.isEnabled = true
//    }
//
//    override fun onDestroy() {
//        billingProcessor.release()
//        super.onDestroy()
//    }
//
//    override fun onBillingError(errorCode: Int, error: Throwable?) {
//        Log.e(TAG, "Billing error: code = $errorCode", error)
//    }

//    private fun isProVersion(): Boolean {
//        return billingProcessor.isPurchased(productIdX1) || billingProcessor.isPurchased(productIdX2) || billingProcessor.isPurchased(productIdX3)
//    }
//
//    private fun restorePurchase() {
//        toast(R.string.restoring_purchase)
//        billingProcessor.loadOwnedPurchasesFromGoogleAsync(object :
//            BillingProcessor.IPurchasesResponseListener {
//            override fun onPurchasesSuccess() {
//                onPurchaseHistoryRestored()
//            }
//
//            override fun onPurchasesError() {
//                toast(R.string.could_not_restore_purchase)
//            }
//        })
//    }

//    private fun loadSkuDetails() {
//        billingProcessor.getPurchaseListingDetailsAsync(ArrayList(listOf(productIdX1, productIdX2, productIdX3)), object : ISkuDetailsResponseListener {
//            override fun onSkuDetailsResponse(products: MutableList<SkuDetails>?) {
//                if (products.isNullOrEmpty()) {
//                    return
//                }
//                val beans = java.util.ArrayList<Purchase>()
//                products.sortWith { o1, o2 ->
//                    o1.priceValue.compareTo(o2.priceValue)
//                }
//                products.forEach {
//                    beans.add(Purchase(it.productId, it.title, it.priceText))
//                }
//                app_one_button.text = beans[0].price
//                app_two_button.text = beans[1].price
//                app_three_button.text = beans[2].price
//            }
//
//            override fun onSkuDetailsError(error: String?) {
//                toast(error!!)
//            }
//        })
//    }

    private fun setupNoPlayStoreInstalled() {
        pro_donate_text.text = Html.fromHtml(getString(R.string.donate_text_g))
        pro_donate_button.apply {
            setOnClickListener {
                launchViewIntent("https://sites.google.com/view/goodwy/support-project")
            }
            val drawable = resources.getColoredDrawableWithColor(R.drawable.button_gray_bg, primaryColor)
            background = drawable
            //setTextColor(baseConfig.backgroundColor)
            setPadding(2,2,2,2)
        }
        pro_switch.isChecked = baseConfig.isPro
        pro_switch_holder.setOnClickListener {
            pro_switch.toggle()
            baseConfig.isPro = pro_switch.isChecked
        }
    }

    @SuppressLint("NewApi", "SetTextI18n", "UseCompatTextViewDrawableApis")
    private fun setupCollection() {
        collection_holder.beVisible()
        val appDialerPackage = "com.goodwy.dialer"
        val appContactsPackage = "com.goodwy.contacts"
        val appSmsMessengerPackage = "com.goodwy.smsmessenger"
        val appGalleryPackage = "com.goodwy.gallery"
        //val appVoiceRecorderPackage = "com.goodwy.voicerecorder"
        val appAudiobookLitePackage = "com.goodwy.audiobooklite"

        val appDialerInstalled = isPackageInstalled(appDialerPackage)// || isPackageInstalled("com.goodwy.dialer.debug")
        val appContactsInstalled = isPackageInstalled(appContactsPackage)// || isPackageInstalled("com.goodwy.contacts.debug")
        val appSmsMessengerInstalled = isPackageInstalled(appSmsMessengerPackage)// || isPackageInstalled("com.goodwy.smsmessenger.debug")
        val appGalleryInstalled = isPackageInstalled(appGalleryPackage)// || isPackageInstalled("com.goodwy.voicerecorder.debug")
        //val appVoiceRecorderInstalled = isPackageInstalled(appVoiceRecorderPackage)// || isPackageInstalled("com.goodwy.voicerecorder.debug")
        val appAudiobookLiteInstalled = isPackageInstalled(appAudiobookLitePackage)// || isPackageInstalled("com.goodwy.voicerecorder.debug")

        val appAllInstalled = appDialerInstalled && appContactsInstalled && appSmsMessengerInstalled && appGalleryInstalled && appAudiobookLiteInstalled

        if (!appAllInstalled) collection_logo.applyColorFilter(primaryColor)
        collection_chevron.applyColorFilter(getProperTextColor())
        collection_subtitle.background.applyColorFilter(getBottomNavigationBackgroundColor())

        val items = arrayOf(
            SimpleListItem(1, R.string.right_dialer, imageRes = R.mipmap.ic_dialer, selected = appDialerInstalled, packageName = appDialerPackage),
            SimpleListItem(2, R.string.right_contacts, imageRes = R.mipmap.ic_contacts, selected = appContactsInstalled, packageName = appContactsPackage),
            SimpleListItem(3, R.string.right_sms_messenger, imageRes = R.mipmap.ic_sms_messenger, selected = appSmsMessengerInstalled, packageName = appSmsMessengerPackage),
            SimpleListItem(4, R.string.right_gallery, imageRes = R.mipmap.ic_gallery, selected = appGalleryInstalled, packageName = appGalleryPackage),
            SimpleListItem(5, R.string.playbook, imageRes = R.mipmap.ic_playbook, selected = appAudiobookLiteInstalled, packageName = appAudiobookLitePackage),
            //SimpleListItem(6, R.string.right_voice_recorder, R.mipmap.ic_voice_recorder, selected = appVoiceRecorderInstalled, packageName = appVoiceRecorderPackage)
        )

        val percentage = items.filter { it.selected }.size.toString() + "/" + items.size.toString()
        collection_title.text = getString(R.string.collection) + "  $percentage"

        collection_holder.setOnClickListener {
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

    companion object {
        private const val TAG: String = "PurchaseActivity"
    }
}
