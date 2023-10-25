package com.goodwy.commons.helpers

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Handler
import android.provider.Settings
import android.util.TypedValue
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsParams.Product
import com.goodwy.commons.extensions.toast
import com.goodwy.commons.R
import com.goodwy.commons.activities.PurchaseActivity
import com.goodwy.commons.dialogs.ConfirmationAdvancedDialog
import com.goodwy.commons.extensions.baseConfig
import com.goodwy.commons.models.Android30RenameFormat
import kotlinx.coroutines.*
import java.util.*

class PurchaseHelper constructor(
    val activity: Activity,
    ) {
    private lateinit var billingClient: BillingClient
    private val iapSkuDetails: ArrayList<ProductDetails> = arrayListOf()
    private val subSkuDetails: ArrayList<ProductDetails> = arrayListOf()
    val iapSkuDetailsInitialized = MutableLiveData<Boolean>(false)
    val subSkuDetailsInitialized = MutableLiveData<Boolean>(false)

    private var iapList = ArrayList<String>() // = arrayListOf(BuildConfig.PRODUCT_ID_X1, BuildConfig.PRODUCT_ID_X2, BuildConfig.PRODUCT_ID_X3)
    private var subList = ArrayList<String>() // = arrayListOf(BuildConfig.SUBSCRIPTION_ID_X1, BuildConfig.SUBSCRIPTION_ID_X2, BuildConfig.SUBSCRIPTION_ID_X3)
    private var iapPurchased: ArrayList<String> = arrayListOf()
    private var subPurchased: ArrayList<String> = arrayListOf()
    val isIapPurchasedList = MutableLiveData<ArrayList<String>>()
    val isSupPurchasedList = MutableLiveData<ArrayList<String>>()
    val isIapPurchased = MutableLiveData<Tipping>()
    val isSupPurchased = MutableLiveData<Tipping>()

    private val consumeResponseListener =
        ConsumeResponseListener { _: BillingResult?, _: String? ->
            //activity.toast(R.string.donation_thanks)
        }

    private fun handlePurchaseIAP(purchase: Purchase) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
        billingClient.consumeAsync(consumeParams.build(), consumeResponseListener)
    }

    private val acknowledgePurchaseResponseListener =
        AcknowledgePurchaseResponseListener {
            //activity.toast(R.string.donation_thanks)
        }

    private fun handlePurchaseSub(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams
            .newBuilder().setPurchaseToken(purchase.purchaseToken)
        billingClient.acknowledgePurchase(
            acknowledgePurchaseParams.build(),
            acknowledgePurchaseResponseListener
        )
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                iapList.forEach {
                    isIapPurchasedList.postValue(iapPurchased)
                    if (purchase.products.contains(it)) handlePurchaseIAP(purchase)
                }
                subList.forEach {
                    isSupPurchasedList.postValue(subPurchased)
                    if (purchase.products.contains(it)) handlePurchaseSub(purchase)
                }
            }
        }
    }

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult: BillingResult, purchases: List<Purchase>? ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases?.forEach {
                    handlePurchase(it)
                }
            }
        }
    private val subsPurchased: ArrayList<String> = arrayListOf()
    private val subsOwnedListener = PurchasesResponseListener {
            billingResult: BillingResult, purchases: List<Purchase> ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                run breaking@{
                    purchases.forEach {
                        it.products.forEach { sku ->
                            if (subsPurchased.contains(sku)) {
                                //activity.baseConfig.isPro = true
                                isSupPurchased.postValue(Tipping.Succeeded)
                                subPurchased.add(sku)
                                isSupPurchasedList.postValue(subPurchased)
                                return@breaking
                            } else {//activity.baseConfig.isPro = false
                                isSupPurchased.postValue(Tipping.NoTips)
                                isSupPurchasedList.postValue(subPurchased)
                            }
                        }
                    }
                }
            } else {
                isSupPurchased.postValue(Tipping.FailedToLoad)
                isSupPurchasedList.postValue(subPurchased)
            }
        }
    private val subHistoryListener = PurchaseHistoryResponseListener {
            billingResult: BillingResult, purchases: List<PurchaseHistoryRecord>? ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases?.forEach {
                    subsPurchased.addAll(it.products)
                    isSupPurchasedList.postValue(subPurchased)
                }
                billingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder().setProductType(
                        BillingClient.ProductType.SUBS
                    ).build(), subsOwnedListener
                )
            } else {
                isSupPurchased.postValue(Tipping.FailedToLoad)
                isSupPurchasedList.postValue(subPurchased)
            }
        }
    private val iapHistoryListener = PurchaseHistoryResponseListener {
            billingResult: BillingResult, purchases: List<PurchaseHistoryRecord>? ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                run breaking@{
                    purchases?.forEach {
                        it.products.forEach { sku ->
                            if (sku != null) {
                                isIapPurchased.postValue(Tipping.Succeeded)
                            } else {
                                isIapPurchased.postValue(Tipping.NoTips)
                            }
                            //activity.baseConfig.isPro = sku != null
                            iapPurchased.add(sku)
                            isIapPurchasedList.postValue(iapPurchased)
                            if (sku.split("_").toTypedArray()[1].toInt() >= 10) {
                                return@breaking
                            }
                        }
                    }
                }
            } else {
                isIapPurchased.postValue(Tipping.FailedToLoad)
                isIapPurchasedList.postValue(iapPurchased)
            }
        }

    fun initBillingClient() {
        billingClient = BillingClient.newBuilder(activity)
            .setListener(purchasesUpdatedListener).enablePendingPurchases().build()
    }

    fun retrieveDonation(iaps: ArrayList<String>, subs: ArrayList<String>) {
//        billingClient = BillingClient.newBuilder(activity)
//            .setListener(purchasesUpdatedListener).enablePendingPurchases().build()

       // CoroutineScope(Dispatchers.IO).launch(Dispatchers.IO) {
            iapSkuDetails.clear()
            subSkuDetails.clear()

            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                    billingClient.endConnection()
                }
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    iapList = iaps
                    subList = subs
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        CoroutineScope(Dispatchers.Main).launch {
                            val job1 = async {
                                iapList.forEach {
                                    val productList = Product.newBuilder().setProductId(it)
                                        .setProductType(BillingClient.ProductType.INAPP).build()
                                    val params = QueryProductDetailsParams
                                        .newBuilder().setProductList(listOf(productList))
                                    billingClient.queryProductDetailsAsync(
                                        params.build()
                                    ) { _: BillingResult?, productDetailsList: List<ProductDetails> ->
                                        iapSkuDetails.addAll(productDetailsList)
                                        iapSkuDetailsInitialized.postValue(true)
                                        billingClient.queryPurchaseHistoryAsync(
                                            QueryPurchaseHistoryParams.newBuilder().setProductType(
                                                BillingClient.ProductType.INAPP
                                            ).build(), iapHistoryListener
                                        )
                                    }
                                }
                            }
                            val job2 = async {
                                subList.forEach {
                                    val productList = Product.newBuilder().setProductId(it)
                                        .setProductType(BillingClient.ProductType.SUBS).build()
                                    val params = QueryProductDetailsParams
                                        .newBuilder().setProductList(listOf(productList))
                                    billingClient.queryProductDetailsAsync(
                                        params.build()
                                    ) { _: BillingResult?, productDetailsList: List<ProductDetails> ->
                                        subSkuDetails.addAll(productDetailsList)
                                        subSkuDetailsInitialized.postValue(true)
                                        billingClient.queryPurchaseHistoryAsync(
                                            QueryPurchaseHistoryParams.newBuilder().setProductType(
                                                BillingClient.ProductType.SUBS
                                            ).build(), subHistoryListener
                                        )
                                    }
                                }
                            }
                            joinAll(job1, job2)
                        }
                    }
                }
            })
       // }
    }

    fun getPriceDonation(product: String): String {
        val iapSku = iapSkuDetails.firstOrNull { it.productId == product }
        return if (iapSku != null) iapSku.oneTimePurchaseOfferDetails!!.formattedPrice
        else activity.getString(R.string.no_connection)
    }

    fun getDonation(product: String) {
        val iapSku = iapSkuDetails.firstOrNull {  it.productId == product  }
        if (iapSku != null) {
            val productDetailsParamsList = ProductDetailsParams
                .newBuilder().setProductDetails(iapSku).build()
            billingClient.launchBillingFlow(
                activity, BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(listOf(productDetailsParamsList))
                    .build()
            )
        } else activity.toast("Product not found")
    }

    fun getPriceSubscription(product: String, planId: String? = null): String {
        val subSku = subSkuDetails.firstOrNull { it.productId == product }
        val noConnect= activity.getString(R.string.no_connection)
        return if (subSku != null) {
            val plan =
                if (planId != null) subSku.subscriptionOfferDetails!!.firstOrNull { it.basePlanId == planId }
                else subSku.subscriptionOfferDetails!![0]
            if (plan != null) {
                plan.pricingPhases
                    .pricingPhaseList[0].formattedPrice
            } else noConnect
        } else noConnect
    }

    fun getSubscription(product: String, planId: String? = null) {
        val subSku = subSkuDetails.firstOrNull { it.productId == product }
        if (subSku != null) {
            val plan = if (planId != null) subSku.subscriptionOfferDetails!!.firstOrNull { it.basePlanId == planId }
                                else subSku.subscriptionOfferDetails!![0]
            if (plan != null) {
                val productDetailsParamsList = ProductDetailsParams.newBuilder()
                    .setOfferToken(plan.offerToken)
                    .setProductDetails(subSku).build()
                billingClient.launchBillingFlow(
                    activity, BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(listOf(productDetailsParamsList))
                        .build()
                )
            } else activity.toast("Plan not found")
        } else activity.toast("Subscription not found")
    }

    fun isIapPurchased(product: String): Boolean {
        return iapPurchased.contains(product)
    }

    fun isSubPurchased(product: String): Boolean {
        return subPurchased.contains(product)
    }
}

sealed class Tipping {
    object FailedToLoad : Tipping()
    object Succeeded : Tipping()
    object NoTips : Tipping()
}
