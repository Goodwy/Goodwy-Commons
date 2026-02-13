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
    private var initializationAttempted = false

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

    // Fields for storing lists
    private var lastIapList: List<String> = emptyList()
    private var lastSubList: List<String> = emptyList()

    // Storage space for all purchases
    private val _allIapPurchases = MutableStateFlow<List<String>>(emptyList())
    private val _allSubPurchases = MutableStateFlow<List<String>>(emptyList())
    val allIapPurchases: StateFlow<List<String>> = _allIapPurchases
    val allSubPurchases: StateFlow<List<String>> = _allSubPurchases

    fun initBillingClient() {
        if (initializationAttempted) {
            return
        }

        initializationAttempted = true

        try {
            iapClient = Iap.getIapClient(activity)

//            activity.runOnUiThread {
//                activity.toast("IAP client created")
//            }

        } catch (e: Exception) {
            activity.runOnUiThread {
                activity.showErrorToast("Error creating IAP client: ${e.message}")
            }
            e.printStackTrace()
        }
    }

    fun isReady(): Boolean {
        return ::iapClient.isInitialized
    }

    // The main method for loading goods and checking purchases
    fun retrieveDonation(iapList: List<String>, subList: List<String>) {
        this.lastIapList = iapList
        this.lastSubList = subList

        if (!isReady()) {
            activity.runOnUiThread {
                activity.toast("IAP is not initialised, let's try...")
            }
            initBillingClient()

            // Try again in 1.5 seconds
            activity.window.decorView.postDelayed({
                retrieveDonationInternal(iapList, subList)
            }, 1500)
            return
        }

        retrieveDonationInternal(iapList, subList)
    }

    private fun retrieveDonationInternal(iapList: List<String>, subList: List<String>) {
        checkIapPurchases()
        checkSubPurchases()

        // Uploading product information
        if (iapList.isNotEmpty()) {
            loadIapProductInfos(iapList)
        } else {
            _iapSkuDetailsInitialized.value = true
        }

        if (subList.isNotEmpty()) {
            loadSubProductInfos(subList)
        } else {
            _subSkuDetailsInitialized.value = true
        }
    }

    private fun checkIapPurchases() {
        if (!isReady()) {
            println("❌ checkIapPurchases: IAP client not ready")
            return
        }
        println("📦 checkIapPurchases: Запрос покупок IAP...")

        val task = iapClient.obtainOwnedPurchases(OwnedPurchasesReq().apply {
            priceType = IapClient.PriceType.IN_APP_CONSUMABLE
        })

        task.addOnSuccessListener { result ->
            println("✅ checkIapPurchases: Успешно получен ответ")

            val ownedProductIds = mutableListOf<String>()

            result.inAppPurchaseDataList?.forEach { jsonString ->
                try {
                    val purchaseData = InAppPurchaseData(jsonString)
                    purchaseData.productId?.let { productId ->
                        ownedProductIds.add(productId)
                        println("   Найден продукт: $productId")
                    }
                } catch (e: Exception) {
                    println("   Ошибка парсинга: ${e.message}")
                    e.printStackTrace()
                }
            }

            // We save ALL purchases found
            _allIapPurchases.value = ownedProductIds
            println("📊 Всего IAP покупок: ${ownedProductIds.size}")
            println("📊 Наши IAP продукты: $lastIapList")

            // We filter only those that we need (from lastIapList)
            val filteredPurchases = ownedProductIds.filter { it in lastIapList }
            _isIapPurchasedList.value = filteredPurchases
            println("📊 Отфильтрованные IAP: $filteredPurchases")

            activity.runOnUiThread {
                when {
                    filteredPurchases.isNotEmpty() -> {
                        _isIapPurchased.value = Tipping.Succeeded
                        println("✅ Установка Tipping.Succeeded для IAP")
                    }
                    else -> {
                        _isIapPurchased.value = Tipping.NoTips
                        println("ℹ️ Установка Tipping.NoTips для IAP")
                    }
                }
            }
        }.addOnFailureListener { e ->
            println("❌ checkIapPurchases ОШИБКА:")
            println("   Сообщение: ${e.message}")
            if (e is IapApiException) {
                println("   Код статуса: ${e.statusCode}")
                println("   Сообщение статуса: ${e.statusMessage}")

                // Дополнительная информация
                when (e.statusCode) {
                    OrderStatusCode.ORDER_HWID_NOT_LOGIN ->
                        println("   ❗ Huawei ID не залогинен")
                    OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED ->
                        println("   ❗ Регион не поддерживается")
                    OrderStatusCode.ORDER_STATE_NET_ERROR ->
                        println("   ❗ Ошибка сети")
                    OrderStatusCode.ORDER_VR_UNINSTALL_ERROR ->
                        println("   ❗ HMS Core не установлен")
                    else ->
                        println("   ❗ Продукт не существует в AppGallery?")
                }
            }
            activity.runOnUiThread {
                _isIapPurchased.value = Tipping.FailedToLoad
                handleError(e)
            }
        }
    }

    private fun checkSubPurchases() {
        if (!isReady()) {
            println("❌ checkSubPurchases: IAP client not ready")
            return
        }

        println("📦 checkSubPurchases: Запрос подписок...")

        val task = iapClient.obtainOwnedPurchases(OwnedPurchasesReq().apply {
            priceType = IapClient.PriceType.IN_APP_SUBSCRIPTION
        })

        task.addOnSuccessListener { result ->
            println("✅ checkSubPurchases: Успешно получен ответ")

            val ownedProductIds = mutableListOf<String>()

            result.inAppPurchaseDataList?.forEach { jsonString ->
                try {
                    val purchaseData = InAppPurchaseData(jsonString)
                    purchaseData.productId?.let { productId ->
                        if (isSubscriptionActive(purchaseData)) {
                            ownedProductIds.add(productId)
                            println("   Активная подписка: $productId")
                        } else {
                            println("   Неактивная подписка: $productId")
                        }
                    }
                } catch (e: Exception) {
                    println("   Ошибка парсинга: ${e.message}")
                    e.printStackTrace()
                }
            }

            _allSubPurchases.value = ownedProductIds
            println("📊 Всего подписок: ${ownedProductIds.size}")
            println("📊 Наши подписки: $lastSubList")

            val filteredPurchases = ownedProductIds.filter { it in lastSubList }
            _isSupPurchasedList.value = filteredPurchases
            println("📊 Отфильтрованные подписки: $filteredPurchases")

            activity.runOnUiThread {
                when {
                    filteredPurchases.isNotEmpty() -> {
                        println("✅ Установка Tipping.Succeeded для подписок")
                        _isSupPurchased.value = Tipping.Succeeded
                    }
                    else -> {
                        println("ℹ️ Установка Tipping.NoTips для подписок")
                        _isSupPurchased.value = Tipping.NoTips
                    }
                }
            }
        }.addOnFailureListener { e ->
            println("❌ checkSubPurchases ОШИБКА:")
            println("   Сообщение: ${e.message}")
            if (e is IapApiException) {
                println("   Код статуса: ${e.statusCode}")
                println("   Сообщение статуса: ${e.statusMessage}")
            }

            activity.runOnUiThread {
                _isSupPurchased.value = Tipping.FailedToLoad
                handleError(e)
            }
        }
    }

    private fun loadIapProductInfos(products: List<String>) {
        if (!isReady()) {
            println("❌ loadIapProductInfos: IAP client not ready")
            return
        }

        if (products.isEmpty()) {
            println("⚠️ loadIapProductInfos: Список продуктов пуст")
            _iapSkuDetailsInitialized.value = true
            return
        }

        println("📦 loadIapProductInfos: Загрузка информации о продуктах: $products")

        val task = iapClient.obtainProductInfo(ProductInfoReq().apply {
            priceType = IapClient.PriceType.IN_APP_CONSUMABLE
            productIds = products
        })

        task.addOnSuccessListener { result ->
            iapSkuDetails = result.productInfoList?.associateBy { it.productId } ?: emptyMap()
            println("✅ loadIapProductInfos: Загружено ${iapSkuDetails.size} продуктов")

            iapSkuDetails.forEach { (id, info) ->
                println("   - $id: ${info.price} (${info.currency})")
            }

            _iapSkuDetailsInitialized.value = true
        }.addOnFailureListener { e ->
            println("❌ loadIapProductInfos ОШИБКА:")
            println("   Сообщение: ${e.message}")
            if (e is IapApiException) {
                println("   Код статуса: ${e.statusCode}")
                println("   Сообщение статуса: ${e.statusMessage}")
            }
            _iapSkuDetailsInitialized.value = false
            handleError(e)
        }
    }

    private fun loadSubProductInfos(products: List<String>) {
        if (!isReady()) {
            println("❌ loadSubProductInfos: IAP client not ready")
            return
        }

        if (products.isEmpty()) {
            println("⚠️ loadSubProductInfos: Список подписок пуст")
            _subSkuDetailsInitialized.value = true
            return
        }

        println("📦 loadSubProductInfos: Загрузка информации о подписках: $products")

        val task = iapClient.obtainProductInfo(ProductInfoReq().apply {
            priceType = IapClient.PriceType.IN_APP_SUBSCRIPTION
            productIds = products
        })

        task.addOnSuccessListener { result ->
            subSkuDetails = result.productInfoList?.associateBy { it.productId } ?: emptyMap()
            println("✅ loadSubProductInfos: Загружено ${subSkuDetails.size} подписок")

            subSkuDetails.forEach { (id, info) ->
                println("   - $id: ${info.price} (${info.currency})")
            }

            _subSkuDetailsInitialized.value = true
        }.addOnFailureListener { e ->
            println("❌ loadSubProductInfos ОШИБКА:")
            println("   Сообщение: ${e.message}")
            if (e is IapApiException) {
                println("   Код статуса: ${e.statusCode}")
                println("   Сообщение статуса: ${e.statusMessage}")
            }
            _subSkuDetailsInitialized.value = false
            handleError(e)
        }
    }

    private fun isSubscriptionActive(purchaseData: InAppPurchaseData): Boolean {
        return try {
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
        if (!isReady()) {
            activity.toast("IAP is not ready")
            return
        }

        val productInfo = iapSkuDetails[productId] ?: return

        val task = iapClient.createPurchaseIntent(createPurchaseReq(productInfo))
        task.addOnSuccessListener { result ->
            if (result.status != null && result.status.hasResolution()) {
                try {
                    result.status.startResolutionForResult(activity, PURCHASE_REQUEST_CODE)
                } catch (e: Exception) {
                    handleError(e)
                }
            } else {
                activity.toast("I am unable to initiate the purchase process")
            }
        }.addOnFailureListener { e ->
            handleError(e)
        }
    }

    fun getSubscription(productId: String) {
        if (!isReady()) {
            activity.toast("IAP is not ready")
            return
        }

        val productInfo = subSkuDetails[productId] ?: return

        val task = iapClient.createPurchaseIntent(createPurchaseReq(productInfo))
        task.addOnSuccessListener { result ->
            if (result.status != null && result.status.hasResolution()) {
                try {
                    result.status.startResolutionForResult(activity, PURCHASE_REQUEST_CODE)
                } catch (e: Exception) {
                    handleError(e)
                }
            } else {
                activity.toast("I am unable to initiate the subscription process.")
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
                    retrieveDonation(lastIapList, lastSubList)
                }
                OrderStatusCode.ORDER_STATE_CANCEL -> {
                    activity.toast(com.goodwy.strings.R.string.billing_product_deleted)
                }
                OrderStatusCode.ORDER_STATE_FAILED -> {
                    activity.toast(com.goodwy.strings.R.string.purchase_failed)
                }
                else -> {
                    activity.toast("Result of purchase: ${purchaseResultInfo.returnCode}")
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
                        "Install or update HMS Core"
                    OrderStatusCode.ORDER_STATE_PRODUCT_INVALID ->
                        "Incorrect product"
                    else -> "HMS error ${e.statusCode}: ${e.message}"
                }
                activity.toast(errorMessage)
            }
            else -> {
                activity.showErrorToast(e)
            }
        }
    }

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

        retrieveDonation(lastIapList, lastSubList)
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
