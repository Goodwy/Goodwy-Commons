package com.goodwy.commons.helpers

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapApiException
import com.huawei.hms.iap.IapClient
import com.huawei.hms.iap.entity.*
import com.goodwy.commons.activities.BaseSimpleActivity
import com.goodwy.commons.extensions.showErrorToast
import com.goodwy.commons.extensions.toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HmsHelper(private val activity: BaseSimpleActivity) {
    private lateinit var iapClient: IapClient

    // StateFlow for compatibility with existing code
    private val _iapSkuDetailsInitialized = MutableStateFlow(false)
    private val _subSkuDetailsInitialized = MutableStateFlow(false)
    private val _isIapPurchased = MutableStateFlow<Tipping>(Tipping.FailedToLoad)
    private val _isSupPurchased = MutableStateFlow<Tipping>(Tipping.FailedToLoad)
    private val _isIapPurchasedList = MutableStateFlow<List<String>>(emptyList())
    private val _isSupPurchasedList = MutableStateFlow<List<String>>(emptyList())

    // StateFlow compatibility properties
    val iapSkuDetailsInitialized: StateFlow<Boolean> = _iapSkuDetailsInitialized
    val subSkuDetailsInitialized: StateFlow<Boolean> = _subSkuDetailsInitialized
    val isIapPurchased: StateFlow<Tipping> = _isIapPurchased
    val isSupPurchased: StateFlow<Tipping> = _isSupPurchased
    val isIapPurchasedList: StateFlow<List<String>> = _isIapPurchasedList
    val isSupPurchasedList: StateFlow<List<String>> = _isSupPurchasedList

    // LiveData properties for observe
    val iapSkuDetailsInitializedLiveData: LiveData<Boolean> = _iapSkuDetailsInitialized.asLiveData()
    val subSkuDetailsInitializedLiveData: LiveData<Boolean> = _subSkuDetailsInitialized.asLiveData()
    val isIapPurchasedLiveData: LiveData<Tipping> = _isIapPurchased.asLiveData()
    val isSupPurchasedLiveData: LiveData<Tipping> = _isSupPurchased.asLiveData()
    val isIapPurchasedListLiveData: LiveData<List<String>> = _isIapPurchasedList.asLiveData()
    val isSupPurchasedListLiveData: LiveData<List<String>> = _isSupPurchasedList.asLiveData()

    private var iapSkuDetails: Map<String, ProductInfo> = emptyMap()
    private var subSkuDetails: Map<String, ProductInfo> = emptyMap()

    fun initBillingClient() {
        iapClient = Iap.getIapClient(activity)
        loadProductInfos()
    }

    private fun loadProductInfos() {
        loadIapProductInfos()
        loadSubProductInfos()
    }

    private fun loadIapProductInfos() {
        val products = listOf(
            "com.goodwy.dialer.pro",
            "com.goodwy.dialer.pro2",
            "com.goodwy.dialer.pro3"
        )

        val task = iapClient.obtainProductInfo(ProductInfoReq().apply {
            priceType = IapClient.PriceType.IN_APP_CONSUMABLE
            productIds = products
        })

        task.addOnSuccessListener { result ->
            iapSkuDetails = result.productInfoList?.associateBy { it.productId } ?: emptyMap()
            _iapSkuDetailsInitialized.value = true
        }.addOnFailureListener { e ->
            _iapSkuDetailsInitialized.value = false
            handleError(e)
        }
    }

    private fun loadSubProductInfos() {
        val subscriptions = listOf(
            "com.goodwy.dialer.sub.monthly",
            "com.goodwy.dialer.sub.monthly2",
            "com.goodwy.dialer.sub.monthly3",
            "com.goodwy.dialer.sub.yearly",
            "com.goodwy.dialer.sub.yearly2",
            "com.goodwy.dialer.sub.yearly3"
        )

        val task = iapClient.obtainProductInfo(ProductInfoReq().apply {
            priceType = IapClient.PriceType.IN_APP_SUBSCRIPTION
            productIds = subscriptions
        })

        task.addOnSuccessListener { result ->
            subSkuDetails = result.productInfoList?.associateBy { it.productId } ?: emptyMap()
            _subSkuDetailsInitialized.value = true
        }.addOnFailureListener { e ->
            _subSkuDetailsInitialized.value = false
            handleError(e)
        }
    }

    fun retrieveDonation(iapList: List<String>, subList: List<String>) {
        checkIapPurchases(iapList)
        checkSubPurchases(subList)
    }

    private fun checkIapPurchases(productIds: List<String>) {
        val task = iapClient.obtainOwnedPurchases(OwnedPurchasesReq().apply {
            priceType = IapClient.PriceType.IN_APP_CONSUMABLE
        })

        task.addOnSuccessListener { result ->
            val ownedProductIds = mutableListOf<String>()

            result.inAppPurchaseDataList?.forEach { jsonString ->
                try {
                    val purchaseData = InAppPurchaseData(jsonString)
                    purchaseData.productId?.let { productId ->
                        ownedProductIds.add(productId)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            _isIapPurchasedList.value = ownedProductIds

            when {
                ownedProductIds.isNotEmpty() -> {
                    _isIapPurchased.value = Tipping.Succeeded
                }
                else -> {
                    _isIapPurchased.value = Tipping.NoTips
                }
            }
        }.addOnFailureListener { e ->
            _isIapPurchased.value = Tipping.FailedToLoad
            handleError(e)
        }
    }

    private fun checkSubPurchases(productIds: List<String>) {
        val task = iapClient.obtainOwnedPurchases(OwnedPurchasesReq().apply {
            priceType = IapClient.PriceType.IN_APP_SUBSCRIPTION
        })

        task.addOnSuccessListener { result ->
            val ownedProductIds = mutableListOf<String>()
            val validSubscriptions = mutableListOf<String>()

            result.inAppPurchaseDataList?.forEach { jsonString ->
                try {
                    val purchaseData = InAppPurchaseData(jsonString)
                    purchaseData.productId?.let { productId ->
                        ownedProductIds.add(productId)

                        // Checking whether the subscription is active
                        if (isSubscriptionActive(purchaseData)) {
                            validSubscriptions.add(productId)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            _isSupPurchasedList.value = validSubscriptions

            when {
                validSubscriptions.isNotEmpty() -> {
                    _isSupPurchased.value = Tipping.Succeeded
                }
                else -> {
                    _isSupPurchased.value = Tipping.NoTips
                }
            }
        }.addOnFailureListener { e ->
            _isSupPurchased.value = Tipping.FailedToLoad
            handleError(e)
        }
    }

    private fun isSubscriptionActive(purchaseData: InAppPurchaseData): Boolean {
        return try {
            // Check if the subscription is active
            // 1. Subscription is paid (purchaseState == 0)
            // 2. Subscription is not cancelled
            // 3. Check autoRenewing and expiryDate

            val purchaseState = purchaseData.purchaseState
            val autoRenewing = purchaseData.isAutoRenewing
            val expiryDate = purchaseData.expirationDate

            val currentTime = System.currentTimeMillis()

            purchaseState == 0 && (autoRenewing || (expiryDate?.let { it > currentTime } ?: false))
        } catch (e: Exception) {
            false
        }
    }

    fun getPriceDonation(productId: String): String {
        return iapSkuDetails[productId]?.price?.takeIf { it.isNotEmpty() }
            ?: activity.getString(com.goodwy.strings.R.string.no_connection)
    }

    fun getPriceSubscription(productId: String): String {
        return subSkuDetails[productId]?.price?.takeIf { it.isNotEmpty() }
            ?: activity.getString(com.goodwy.strings.R.string.no_connection)
    }

    fun getDonation(productId: String) {
        val productInfo = iapSkuDetails[productId] ?: return

        val task = iapClient.createPurchaseIntent(createPurchaseReq(productInfo))
        task.addOnSuccessListener { result ->
            if (result.status != null && result.status.hasResolution()) {
                // Launching the Activity for payment
                try {
                    result.status.startResolutionForResult(activity, PURCHASE_REQUEST_CODE)
                } catch (e: Exception) {
                    handleError(e)
                }
            } else {
                activity.toast("Cannot start purchase flow")
            }
        }.addOnFailureListener { e ->
            handleError(e)
        }
    }

    fun getSubscription(productId: String) {
        val productInfo = subSkuDetails[productId] ?: return

        val task = iapClient.createPurchaseIntent(createPurchaseReq(productInfo))
        task.addOnSuccessListener { result ->
            if (result.status != null && result.status.hasResolution()) {
                // Launching the Activity for payment
                try {
                    result.status.startResolutionForResult(activity, PURCHASE_REQUEST_CODE)
                } catch (e: Exception) {
                    handleError(e)
                }
            } else {
                activity.toast("Cannot start subscription flow")
            }
        }.addOnFailureListener { e ->
            handleError(e)
        }
    }

    private fun createPurchaseReq(productInfo: ProductInfo): PurchaseIntentReq {
        return PurchaseIntentReq().apply {
            this.productId = productInfo.productId
            priceType = productInfo.priceType
            developerPayload = activity.packageName.toString()
            // Additional options can be added for subscriptions.
//            if (priceType == IapClient.PriceType.IN_APP_SUBSCRIPTION) {
//            }
        }
    }

    fun isIapPurchased(productId: String): Boolean {
        return _isIapPurchasedList.value.contains(productId)
    }

    fun isSubPurchased(productId: String): Boolean {
        return _isSupPurchasedList.value.contains(productId)
    }

    fun handlePurchaseResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != PURCHASE_REQUEST_CODE) {
            return
        }

        try {
            val purchaseResultInfo = iapClient.parsePurchaseResultInfoFromIntent(data)

            when (purchaseResultInfo.returnCode) {
                OrderStatusCode.ORDER_STATE_SUCCESS -> {
                    activity.toast(com.goodwy.strings.R.string.purchase_successful)

                    // After a successful purchase, we update the lists.
                    retrieveDonation(emptyList(), emptyList())
                }
                OrderStatusCode.ORDER_STATE_CANCEL -> {
                    activity.toast(com.goodwy.strings.R.string.billing_product_deleted)
                }
                OrderStatusCode.ORDER_STATE_FAILED -> {
                    activity.toast(com.goodwy.strings.R.string.purchase_failed)
                }
                else -> {
                    activity.toast("Purchase result: ${purchaseResultInfo.returnCode}")
                }
            }
        } catch (e: Exception) {
            handleError(e)
        }
    }

    private fun handleError(e: Exception) {
        when (e) {
            is IapApiException -> {
                val errorMessage = when (e.statusCode) {
                    OrderStatusCode.ORDER_HWID_NOT_LOGIN ->
                        activity.getString(com.goodwy.strings.R.string.huawei_id_not_logged_in)
                    OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED ->
                        activity.getString(com.goodwy.strings.R.string.region_not_supported)
                    OrderStatusCode.ORDER_PRODUCT_OWNED ->
                        activity.getString(com.goodwy.strings.R.string.product_already_owned)
                    OrderStatusCode.ORDER_STATE_NET_ERROR ->
                        activity.getString(com.goodwy.strings.R.string.network_error)
                    OrderStatusCode.ORDER_VR_UNINSTALL_ERROR ->
                        "Please install or update HMS Core"
                    OrderStatusCode.ORDER_STATE_PRODUCT_INVALID ->
                        "Invalid product"
                    else -> "HMS Error ${e.statusCode}: ${e.message}"
                }
                activity.toast(errorMessage)
            }
            else -> {
                activity.showErrorToast(e)
            }
        }
    }

    // Supporting methods for obtaining product information
    fun getProductInfo(productId: String): ProductInfo? {
        return iapSkuDetails[productId] ?: subSkuDetails[productId]
    }

    fun getSubscriptionInfo(productId: String): ProductInfo? {
        return subSkuDetails[productId]
    }

    fun restorePurchases() {
        _isIapPurchasedList.value = emptyList()
        _isSupPurchasedList.value = emptyList()
        _isIapPurchased.value = Tipping.FailedToLoad
        _isSupPurchased.value = Tipping.FailedToLoad

        retrieveDonation(emptyList(), emptyList())
    }

    companion object {
        const val PURCHASE_REQUEST_CODE = 1001
    }
}

sealed class Tipping {
    data object FailedToLoad : Tipping()
    data object Succeeded : Tipping()
    data object NoTips : Tipping()
}
