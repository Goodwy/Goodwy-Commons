package com.goodwy.commons.helpers.rustore

import android.content.Context
import android.util.Log
import com.goodwy.commons.helpers.rustore.model.*
import com.goodwy.strings.R as stringsR
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import ru.rustore.sdk.billingclient.RuStoreBillingClient
import ru.rustore.sdk.billingclient.model.product.Product
import ru.rustore.sdk.billingclient.model.purchase.PaymentResult
import ru.rustore.sdk.billingclient.model.purchase.PurchaseState
import ru.rustore.sdk.billingclient.utils.pub.checkPurchasesAvailability

class RuStoreHelper {

    private val billingClientRuStore: RuStoreBillingClient = RuStoreModule.provideRuStoreBillingClient()

    //Start Purchases
    private val _stateStart = MutableStateFlow(StartPurchasesState())
//    val stateStart = _stateStart.asStateFlow()

    private val _eventStart = MutableSharedFlow<StartPurchasesEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val eventStart = _eventStart.asSharedFlow()

    fun checkPurchasesAvailability(context: Context) {
        _stateStart.value = _stateStart.value.copy(isLoading = true)
        RuStoreBillingClient.checkPurchasesAvailability(context)
            .addOnSuccessListener { result ->
                _stateStart.value = _stateStart.value.copy(isLoading = false)
                _eventStart.tryEmit(StartPurchasesEvent.PurchasesAvailability(result))
            }
            .addOnFailureListener { throwable ->
                _stateStart.value = _stateStart.value.copy(isLoading = false)
                _eventStart.tryEmit(StartPurchasesEvent.Error(throwable))
            }
    }

    //Billing
    private val defaultProductIds = listOf(
        "product_x1",
        "product_x2",
        "product_x3",
        "subscription_x1",
        "subscription_x2",
        "subscription_x3",
        "subscription_year_x1",
        "subscription_year_x2",
        "subscription_year_x3"
    )

    private val _stateBilling = MutableStateFlow(BillingState())
    val stateBilling = _stateBilling.asStateFlow()

    private val _eventBilling = MutableSharedFlow<BillingEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val eventBilling = _eventBilling.asSharedFlow()

    private val _statePurchased = MutableStateFlow(PurchasedState())
    val statePurchased = _statePurchased.asStateFlow()

    fun getProducts(availableProductIds: List<String> = defaultProductIds) {
        _stateBilling.value = _stateBilling.value.copy(isLoading = true)
        _statePurchased.value = _statePurchased.value.copy(isLoading = true)
        CoroutineScope(Dispatchers.Main).launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val products = billingClientRuStore.products.getProducts(
                        productIds = availableProductIds
                    ).await()

                    val purchases = billingClientRuStore.purchases.getPurchases().await()

                    purchases.forEach { purchase ->
                        val purchaseId = purchase.purchaseId
                        if (purchase.developerPayload?.isNotEmpty() == true) {
                            Log.w("RuStoreBillingClient", "DeveloperPayloadInfo: ${purchase.developerPayload}")
                        }
                        if (purchaseId != null) {
                            when (purchase.purchaseState) {
                                PurchaseState.CREATED, PurchaseState.INVOICE_CREATED -> {
                                    billingClientRuStore.purchases.deletePurchase(purchaseId).await()
                                }

                                PurchaseState.PAID -> {
                                    billingClientRuStore.purchases.confirmPurchase(purchaseId).await()
                                }

                                else -> Unit
                            }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        _stateBilling.value = _stateBilling.value.copy(
                            products = products,
                            isLoading = false
                        )

                        val purchasedProducts = purchases.filter { it.purchaseState == PurchaseState.PAID || it.purchaseState == PurchaseState.CONFIRMED }
                        _statePurchased.value = _statePurchased.value.copy(
                            purchases = purchasedProducts,
                            isLoading = false
                        )
                    }
                }
            }.onFailure { throwable ->
                _eventBilling.tryEmit(BillingEvent.ShowError(throwable))
                _stateBilling.value = _stateBilling.value.copy(isLoading = false)
            }
        }
    }

    fun purchaseProduct(product: Product) {
        billingClientRuStore.purchases.purchaseProduct(productId = product.productId)
            .addOnSuccessListener { paymentResult ->
                handlePaymentResult(paymentResult)
            }
            .addOnFailureListener {
                setErrorStateOnFailure(it)
            }
    }

    private fun handlePaymentResult(paymentResult: PaymentResult) {
        when (paymentResult) {
            is PaymentResult.Cancelled -> {
                deletePurchase(paymentResult.purchaseId)
            }

            is PaymentResult.Failure -> {
                paymentResult.purchaseId?.let { deletePurchase(it) }
            }

            is PaymentResult.Success -> {
                confirmPurchase(paymentResult.purchaseId)
            }

            else -> Unit
        }
    }

    private fun confirmPurchase(purchaseId: String) {
        _stateBilling.value = _stateBilling.value.copy(
            isLoading = true,
            snackbarResId = stringsR.string.billing_purchase_confirm_in_progress
        )
        billingClientRuStore.purchases.confirmPurchase(purchaseId, null)
            .addOnSuccessListener { response ->
                _eventBilling.tryEmit(
                    BillingEvent.ShowDialog(
                        InfoDialogState(
                            titleRes = stringsR.string.billing_product_confirmed,
                            message = response.toString(),
                        )
                    )
                )
                _stateBilling.value = _stateBilling.value.copy(
                    isLoading = false,
                    snackbarResId = null
                )
            }
            .addOnFailureListener {
                setErrorStateOnFailure(it)
            }
    }

    private fun deletePurchase(purchaseId: String) {
        _stateBilling.value = _stateBilling.value.copy(
            isLoading = true,
            snackbarResId = stringsR.string.billing_purchase_delete_in_progress
        )
        billingClientRuStore.purchases.deletePurchase(purchaseId)
            .addOnSuccessListener { response ->
                _eventBilling.tryEmit(
                    BillingEvent.ShowDialog(InfoDialogState(
                        titleRes = stringsR.string.billing_product_deleted,
                        message = response.toString()
                    ))
                )
                _stateBilling.value = _stateBilling.value.copy(isLoading = false)
            }
            .addOnFailureListener {
                setErrorStateOnFailure(it)
            }
    }

    private fun setErrorStateOnFailure(error: Throwable) {
        _eventBilling.tryEmit(BillingEvent.ShowError(error))
        _stateBilling.value = _stateBilling.value.copy(isLoading = false)
    }
}
