package com.goodwy.commons.helpers

import android.app.Application
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.goodwy.commons.activities.BaseSimpleActivity
import com.goodwy.commons.extensions.baseConfig
import com.goodwy.commons.extensions.isRuStoreInstalled
import com.goodwy.commons.helpers.rustore.RuStoreHelper
import com.goodwy.commons.helpers.rustore.RuStoreModule
import com.goodwy.commons.helpers.rustore.model.StartPurchasesEvent
import kotlinx.coroutines.launch
import ru.rustore.sdk.core.feature.model.FeatureAvailabilityResult
import kotlin.collections.firstOrNull

class PurchaseHelper {
    private var ruStoreHelper: RuStoreHelper? = null
    private var ruStoreIsConnected = false

    fun initPurchaseIfNeed(app: Application, id: String) {
        if (app.isRuStoreInstalled()) RuStoreModule.install(app, id)
    }

    fun checkPurchase(
        activity: BaseSimpleActivity,
        iapList: ArrayList<String> = arrayListOf("pro_version", "pro_version_x2", "pro_version_x3"),
        subList: ArrayList<String> =
            arrayListOf("subscription_x1", "subscription_x2", "subscription_x3",
                "subscription_year_x1", "subscription_year_x2", "subscription_year_x3"),
        ruStoreList: ArrayList<String> =
            arrayListOf(
                "pro_version", "pro_version_x2", "pro_version_x3",
                "subscription_x1", "subscription_x2", "subscription_x3",
                "subscription_year_x1", "subscription_year_x2", "subscription_year_x3"
            ),
        callback: (updatePro: Boolean) -> Unit,
    ) {
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
                            activity.baseConfig.isProRuStore = state.purchases.firstOrNull() != null
                            callback(true)
                        }
                    }
            }
        }
    }

    private fun handleEventStart(event: StartPurchasesEvent, productList: ArrayList<String>) {
        when (event) {
            is StartPurchasesEvent.PurchasesAvailability -> {
                when (event.availability) {
                    is FeatureAvailabilityResult.Available -> {
                        //Process purchases available
                        updateProducts(productList)
                        ruStoreIsConnected = true
                    }

                    is FeatureAvailabilityResult.Unavailable -> {
                        //toast(event.availability.cause.message ?: "Process purchases unavailable", Toast.LENGTH_LONG)
                    }

//                    else -> {}
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
