package com.goodwy.commons.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.*
import android.os.Bundle
import android.util.Log
import androidx.core.net.toUri
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.BillingProcessor.ISkuDetailsResponseListener
import com.anjlab.android.iab.v3.PurchaseInfo
import com.anjlab.android.iab.v3.SkuDetails
import com.goodwy.commons.R
import com.goodwy.commons.dialogs.ConfirmationDialog
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.*
import com.goodwy.commons.models.Purchase
import kotlinx.android.synthetic.main.activity_purchase.*
import kotlinx.android.synthetic.main.top_view_purchase.*

class PurchaseActivity : BaseSimpleActivity(), BillingProcessor.IBillingHandler {

    private lateinit var billingProcessor: BillingProcessor
    private var appName = ""
    private var primaryColor = 0
    private var licensingKey = ""
    private var productIdX1 = ""
    private var productIdX2 = ""
    private var productIdX3 = ""
    private var showLifebuoy = true

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase)
        appName = intent.getStringExtra(APP_NAME) ?: ""
        licensingKey = intent.getStringExtra(GOOGLE_PLAY_LICENSING_KEY) ?: ""
        productIdX1 = intent.getStringExtra(PRODUCT_ID_X1) ?: ""
        productIdX2 = intent.getStringExtra(PRODUCT_ID_X2) ?: ""
        productIdX3 = intent.getStringExtra(PRODUCT_ID_X3) ?: ""
        primaryColor = getProperPrimaryColor()
        showLifebuoy = intent.getBooleanExtra(SHOW_LIFEBUOY, true)

        billingProcessor = BillingProcessor(this, licensingKey, this)
    }

    override fun onResume() {
        super.onResume()
        updateTextColors(purchase_holder)
        setupOptionsMenu()
        setupToolbar(purchase_toolbar, NavigationIcon.Arrow)
        collapsing_toolbar.setBackgroundColor(getProperStatusBarColor())
        purchase_apps.setTextColor(getProperTextColor())

        setupButtonOne()
        setupButtonTwo()
        setupButtonThree()
        setupEmail()
        setupParticipants()
        setupIcon()
    }

    private fun setupOptionsMenu() {
        purchase_toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.restorePurchases -> {
                    restorePurchase()
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

    private fun setupParticipants() {
        goodwy_logo.setOnClickListener {
            launchMoreAppsFromUsIntent()
        }
    }

    private fun setupButtonOne() {
        app_one_button.apply {
            setOnClickListener {
                billingProcessor.purchase(this@PurchaseActivity, productIdX1)
                /*try {
                    launchViewIntent("market://details?id=com.goodwy.audiobook")
                } catch (ignored: ActivityNotFoundException) {
                    launchViewIntent(getStoreUrl())
                }*/
            }
            val drawable = resources.getColoredDrawableWithColor(R.drawable.button_gray_bg, primaryColor)
            background = drawable
            //setTextColor(baseConfig.backgroundColor)
            setPadding(2,2,2,2)
        }
    }

    private fun setupButtonTwo() {
        app_two_button.apply {
            setOnClickListener {
                billingProcessor.purchase(this@PurchaseActivity, productIdX2)
                /*try {
                    launchViewIntent("market://details?id=com.goodwy.voicerecorder")
                } catch (ignored: ActivityNotFoundException) {
                    launchViewIntent(getStoreUrl())
                }*/
            }
            val drawable = resources.getColoredDrawableWithColor(R.drawable.button_gray_bg, primaryColor)
            background = drawable
            //setTextColor(baseConfig.backgroundColor)
            setPadding(2,2,2,2)
        }

    }

    private fun setupButtonThree() {
        app_three_button.apply {
            setOnClickListener {
                billingProcessor.purchase(this@PurchaseActivity, productIdX3)
                /*try {
                    launchViewIntent("market://details?id=com.goodwy.files")
                } catch (ignored: ActivityNotFoundException) {
                    launchViewIntent(getStoreUrl())
                }*/
            }
            val drawable = resources.getColoredDrawableWithColor(R.drawable.button_gray_bg, primaryColor)
            background = drawable
            //setTextColor(baseConfig.backgroundColor)
            setPadding(2,2,2,2)
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

    override fun onProductPurchased(productId: String, details: PurchaseInfo?) {
        toast(R.string.thank_you)
        setResult(RESULT_OK)
    }

    override fun onPurchaseHistoryRestored() {
        if (isProVersion()) {
            toast(R.string.restored_previous_purchase_please_restart)
            setResult(RESULT_OK)
        } else {
            toast(R.string.no_purchase_found)
        }
    }

    override fun onBillingInitialized() {
        loadSkuDetails()
        app_one_button.isEnabled = true
        app_two_button.isEnabled = true
        app_three_button.isEnabled = true
    }

    override fun onDestroy() {
        billingProcessor.release()
        super.onDestroy()
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        Log.e(TAG, "Billing error: code = $errorCode", error)
    }

    private fun isProVersion(): Boolean {
        return billingProcessor.isPurchased(productIdX1) || billingProcessor.isPurchased(productIdX2) || billingProcessor.isPurchased(productIdX3)
    }

    private fun restorePurchase() {
        toast(R.string.restoring_purchase)
        billingProcessor.loadOwnedPurchasesFromGoogleAsync(object :
            BillingProcessor.IPurchasesResponseListener {
            override fun onPurchasesSuccess() {
                onPurchaseHistoryRestored()
            }

            override fun onPurchasesError() {
                toast(R.string.could_not_restore_purchase)
            }
        })
    }

    private fun loadSkuDetails() {
        billingProcessor.getPurchaseListingDetailsAsync(ArrayList(listOf(productIdX1, productIdX2, productIdX3)), object : ISkuDetailsResponseListener {
            override fun onSkuDetailsResponse(products: MutableList<SkuDetails>?) {
                if (products.isNullOrEmpty()) {
                    return
                }
                val beans = java.util.ArrayList<Purchase>()
                products.sortWith { o1, o2 ->
                    o1.priceValue.compareTo(o2.priceValue)
                }
                products.forEach {
                    beans.add(Purchase(it.productId, it.title, it.priceText))
                }
                app_one_button.text = beans[0].price
                app_two_button.text = beans[1].price
                app_three_button.text = beans[2].price
            }

            override fun onSkuDetailsError(error: String?) {
                toast(error!!)
            }
        })
    }

    companion object {
        private const val TAG: String = "PurchaseActivity"
    }
}
