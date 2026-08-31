package com.goodwy.commons.helpers.rustorepay

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.goodwy.commons.compose.theme.isDarkMode
import com.goodwy.commons.extensions.toast
import com.goodwy.commons.helpers.rustorepay.models.SubscriptionPeriod
import com.goodwy.commons.helpers.rustorepay.models.ProductInfo
import com.goodwy.commons.helpers.rustorepay.models.PurchaseInfo
import com.goodwy.commons.helpers.rustorepay.models.SubscriptionInfo
import com.goodwy.strings.R
import ru.rustore.sdk.pay.RuStorePayClient
import ru.rustore.sdk.pay.model.GracePeriod
import ru.rustore.sdk.pay.model.HoldPeriod
import ru.rustore.sdk.pay.model.MainPeriod
import ru.rustore.sdk.pay.model.PreferredPurchaseType
import ru.rustore.sdk.pay.model.ProductId
import ru.rustore.sdk.pay.model.ProductPurchase
import ru.rustore.sdk.pay.model.ProductPurchaseParams
import ru.rustore.sdk.pay.model.PromoPeriod
import ru.rustore.sdk.pay.model.PurchaseAvailabilityResult
import ru.rustore.sdk.pay.model.RuStorePaymentException
import ru.rustore.sdk.pay.model.SdkTheme
import ru.rustore.sdk.pay.model.SubscriptionPurchase
import ru.rustore.sdk.pay.model.TrialPeriod
import ru.rustore.sdk.pay.model.UserAuthorizationStatus

// https://www.rustore.ru/help/sdk/pay/kotlin-java
class RuStorePayHelper(
    private val activity: AppCompatActivity
) {

    private val intentInteractor by lazy {
        RuStorePayClient.instance.getIntentInteractor()
    }

    private val userInteractor by lazy {
        RuStorePayClient.instance.getUserInteractor()
    }

    private val productInteractor by lazy {
        RuStorePayClient.instance.getProductInteractor()
    }

    private val purchaseInteractor by lazy {
        RuStorePayClient.instance.getPurchaseInteractor()
    }

    // Initializing the Pay SDK in onCreate()
    fun initialize(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            intentInteractor.proceedIntent(
                intent = activity.intent,
                sdkTheme = if (activity.isDarkMode()) SdkTheme.DARK else SdkTheme.LIGHT
            )
        }
    }

    // Handling a new intent (deeplink) in onNewIntent()
    fun handleNewIntent(intent: Intent?) {
        intentInteractor.proceedIntent(
            intent,
            sdkTheme = if (activity.isDarkMode()) SdkTheme.DARK else SdkTheme.LIGHT
        )
    }

    // Checking payment availability
    fun checkPaymentAvailability(
        onAvailable: () -> Unit,
        onUnavailable: (Throwable) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        purchaseInteractor
            .getPurchaseAvailability()
            .addOnSuccessListener { result ->
                when (result) {
                    PurchaseAvailabilityResult.Available -> {
//                        activity.toast("Payments are available", Toast.LENGTH_LONG)
                        onAvailable()
                    }
                    is PurchaseAvailabilityResult.Unavailable -> {
//                        activity.toast("Payments unavailable: ${result.cause}", Toast.LENGTH_LONG)
                        onUnavailable(result.cause)
                    }
                }
            }
            .addOnFailureListener { error ->
//                activity.toast("Failed to check payment availability", Toast.LENGTH_LONG)
                onError(error)
            }
    }

    // Checking user authorization status.
    fun checkUserAuthorization(
        onAuthorized: () -> Unit,
        onUnauthorized: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        userInteractor
            .getUserAuthorizationStatus()
            .addOnSuccessListener { result ->
                when (result) {
                    UserAuthorizationStatus.AUTHORIZED -> {
                        // Пользователь авторизован в RuStore или через VK ID на платежной шторке
//                        activity.toast("User is authorized", Toast.LENGTH_LONG)
                        onAuthorized()
                    }
                    UserAuthorizationStatus.UNAUTHORIZED -> {
                        // Пользователь не авторизован
//                        activity.toast("User is not authorized", Toast.LENGTH_LONG)
                        onUnauthorized()
                    }
                }
            }
            .addOnFailureListener { error ->
                // Обработка ошибки
//                activity.toast("Failed to check user authorization: $error", Toast.LENGTH_LONG)
                onError(error)
            }
    }

    // Getting a list of products. Does not require user authorization.
    fun loadProducts(
        productIds: List<String>,
        onSuccess: (List<ProductInfo>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        productInteractor
            .getProducts(
                productsId = productIds.map { ProductId(it) }
            )
            .addOnSuccessListener { products ->
                val productInfoList = products.map { product ->
                    ProductInfo(
                        productId = product.productId.value,
                        type = product.type.name,
                        amountLabel = product.amountLabel.value,
                        price = product.price?.value ?: 0,
                        currency = product.currency.value,
                        imageUrl = product.imageUrl.value,
                        title = product.title.value,
                        description = product.description?.value,
                        subscriptionInfo = product.subscriptionInfo?.let { subInfo ->
                            SubscriptionInfo(
                                periods = subInfo.periods.map { period ->
                                    when (period) {
                                        is TrialPeriod ->
                                            SubscriptionPeriod("trial", period.duration, period.price.toString())

                                        is PromoPeriod ->
                                            SubscriptionPeriod("promo", period.duration, period.price.toString())

                                        is MainPeriod ->
                                            SubscriptionPeriod("main", period.duration, period.price.toString())

                                        is GracePeriod ->
                                            SubscriptionPeriod("grace", period.duration, null)

                                        is HoldPeriod ->
                                            SubscriptionPeriod("hold", period.duration, null)
                                    }
                                }
                            )
                        }
                    )
                }
//                activity.toast("${productInfoList.size} products received", Toast.LENGTH_LONG)
                onSuccess(productInfoList)
            }
            .addOnFailureListener { error ->
//                activity.toast("Failed to receive products: $error", Toast.LENGTH_LONG)
                onError(error)
            }
    }

    // Purchasing a product (one-step payment)
    fun buyProduct(
        productId: ProductId,
        onSuccess: (String) -> Unit,
        onCancelled: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val theme = if (activity.isDarkMode()) SdkTheme.DARK else SdkTheme.LIGHT
        val params = ProductPurchaseParams(
            productId = productId,
        )

        purchaseInteractor
            .purchase(
                params = params,
                preferredPurchaseType = PreferredPurchaseType.ONE_STEP,
                sdkTheme = theme
            )
            .addOnSuccessListener { result ->
                onSuccess(result.productId.value)
            }
            .addOnFailureListener { error ->
                when (error) {
                    is RuStorePaymentException.ProductPurchaseCancelled -> {
                        activity.toast(R.string.billing_product_deleted, Toast.LENGTH_LONG)
                        onCancelled()
                    }

                    is RuStorePaymentException.ProductPurchaseException -> {
                        activity.toast("Purchase exception: ${error.message}", Toast.LENGTH_LONG)
                        onError(error)
                    }

                    is RuStorePaymentException.RuStorePaymentNetworkException -> {
                        activity.toast("Network exception ${error.code}: ${error.message}", Toast.LENGTH_LONG)
                        onError(error)
                    }

                    else -> {
                        activity.toast("Unknown purchase error: $error", Toast.LENGTH_LONG)
                        onError(error)
                    }
                }
            }
    }

    // Getting a user's shopping list.
    // By default, filters are disabled; if no values are specified,
    // the method will return all the user's purchases with
    // the statuses PAID, CONFIRMED, ACTIVE, and PAUSED, regardless of the product type.
    fun checkPurchase(
        onSuccess: (List<PurchaseInfo>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        purchaseInteractor
            .getPurchases()
            .addOnSuccessListener { purchases ->
                val purchaseInfoList = purchases.mapNotNull { purchase ->
                    when (purchase) {
                        is ProductPurchase -> PurchaseInfo(
                            purchaseId = purchase.purchaseId.value,
                            invoiceId = purchase.invoiceId.value,
                            type = "product",
                            status = purchase.status.name,
                            purchaseTime = purchase.purchaseTime?.time ?: 0,
                            price = purchase.price.value,
                            currency = purchase.currency.value,
                            developerPayload = purchase.developerPayload?.value,
                            productId = purchase.productId.value,
                        )
                        is SubscriptionPurchase -> PurchaseInfo(
                            purchaseId = purchase.purchaseId.value,
                            invoiceId = purchase.invoiceId.value,
                            type = "subscription",
                            status = purchase.status.name,
                            purchaseTime = purchase.purchaseTime?.time ?: 0,
                            price = purchase.price.value,
                            currency = purchase.currency.value,
                            productId = purchase.productId.value,
                            expirationDate = purchase.expirationDate.time,
                            gracePeriodEnabled = purchase.gracePeriodEnabled,
                        )
                        else -> null
                    }
                }
                onSuccess(purchaseInfoList)
            }
            .addOnFailureListener { error ->
//                activity.toast("Failed to fetch purchases: $error", Toast.LENGTH_LONG)
                onError(error)
            }
    }
}
