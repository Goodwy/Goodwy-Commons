package com.goodwy.commons.helpers

import android.app.Application
import android.app.role.RoleManager
import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.goodwy.commons.activities.BaseSimpleActivity
import com.goodwy.commons.compose.extensions.config
import com.goodwy.commons.dialogs.ConfirmationAdvancedDialog
import com.goodwy.commons.extensions.baseConfig
import com.goodwy.commons.extensions.isRuStoreInstalled
import com.goodwy.commons.extensions.toast
import com.goodwy.commons.helpers.rustore.RuStoreHelper
import com.goodwy.commons.helpers.rustore.RuStoreModule
import com.goodwy.commons.helpers.rustore.model.StartPurchasesEvent
import com.goodwy.commons.helpers.rustorepay.RuStorePayHelper
import com.goodwy.strings.R
import kotlinx.coroutines.launch
import ru.rustore.sdk.billingclient.model.purchase.PurchaseAvailabilityResult
import ru.rustore.sdk.core.feature.model.FeatureAvailabilityResult
import kotlin.collections.firstOrNull

class PurchaseHelper {
    private var ruStoreHelper: RuStoreHelper? = null
    private var ruStoreIsConnected = false
    private var ruStorePayHelper: RuStorePayHelper? = null

    fun initPurchaseIfNeed(app: Application, id: String) {
        if (app.isRuStoreInstalled()) RuStoreModule.install(app, id)
    }

    fun initPurchaseToActivityIfNeed(
        savedInstanceState: Bundle?,
        callback: (initialized: Boolean) -> Unit
    ) {
        if (savedInstanceState == null) {
            ruStorePayHelper?.initialize(savedInstanceState)
            callback(true)
        }
    }

    fun checkPurchase(
        activity: BaseSimpleActivity,
        iapList: ArrayList<String> = arrayListOf(),
        subList: ArrayList<String> = arrayListOf(),
        ruStoreList: ArrayList<String> = arrayListOf(),
        callback: (updatePro: Boolean) -> Unit,
    ) {
        // old
        ruStoreHelper = try {
            RuStoreHelper()
        } catch (_: Exception) {
            null
        }

        if (ruStoreHelper != null) {
            ruStoreHelper!!.checkPurchasesAvailability(activity)

            activity.lifecycleScope.launch {
                ruStoreHelper!!.eventStart
                    .flowWithLifecycle(activity.lifecycle)
                    .collect { event ->
                        handleEventStart(event, ruStoreList)
                    }
            }

            activity.lifecycleScope.launch {
                ruStoreHelper!!.statePurchased
                    .flowWithLifecycle(activity.lifecycle)
                    .collect { state ->
                        //update of purchased
                        if (!state.isLoading && ruStoreIsConnected) {
//                            try {
//                                if (state.purchases.firstOrNull() != null) {
//                                    val subscriptionIds = ruStoreList.filter {
//                                        it.contains("subscription", ignoreCase = true)
//                                    }
//
//                                    val isSubscription = state.purchases.any { purchase ->
//                                        purchase.purchaseId?.let { subscriptionIds.contains(it) } == true
//                                    }
//
//                                    if (isSubscription) {
//                                        ConfirmationAdvancedDialog(
//                                            activity = activity,
//                                            messageId = R.string.rustore_subscription_warning,
//                                            fromHtml = true,
//                                            positive = com.goodwy.commons.R.string.ok,
//                                            negative = com.goodwy.commons.R.string.later
//                                        ) {
//                                            if (it) {
//                                            } else {
//                                            }
//                                        }
//                                    }
//                                }
//                            } catch (_: Exception) {}

                            activity.baseConfig.isProRuStoreOld = state.purchases.firstOrNull() != null
                            callback(true)
                        }
                    }
            }
        }

        // new
    }

    private fun handleEventStart(event: StartPurchasesEvent, productList: ArrayList<String>) {
        when (event) {
            is StartPurchasesEvent.PurchasesAvailability -> {
                when (event.availability) {
                    is PurchaseAvailabilityResult.Available -> {
                        //Process purchases available
                        updateProducts(productList)
                        ruStoreIsConnected = true
                    }

                    is PurchaseAvailabilityResult.Unavailable -> {
                        //toast(event.availability.cause.message ?: "Process purchases unavailable", Toast.LENGTH_LONG)
                    }

                    else -> {}
                }
            }

            is StartPurchasesEvent.Error -> {
                //toast(event.throwable.message ?: "Process unknown error", Toast.LENGTH_LONG)
            }
        }
    }

    private fun updateProducts(productList: ArrayList<String>) {
        ruStoreHelper!!.getProducts(productList)
    }
}
