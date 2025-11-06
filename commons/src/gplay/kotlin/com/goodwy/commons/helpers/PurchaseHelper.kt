package com.goodwy.commons.helpers

import android.app.Application
import com.goodwy.commons.activities.BaseSimpleActivity
import com.goodwy.commons.extensions.baseConfig

class PurchaseHelper {
    fun initPurchaseIfNeed(app: Application, id: String) {
        //Not used for Google Play
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
        val playStoreHelper = PlayStoreHelper(activity)
        playStoreHelper.initBillingClient()
        playStoreHelper.retrieveDonation(iapList, subList)

        playStoreHelper.isIapPurchased.observe(activity) {
            when (it) {
                is Tipping.Succeeded -> {
                    activity.baseConfig.isPro = true
                    callback(true)
                }

                is Tipping.NoTips -> {
                    activity.baseConfig.isPro = false
                    callback(true)
                }

                is Tipping.FailedToLoad -> {}
            }
        }

        playStoreHelper.isSupPurchased.observe(activity) {
            when (it) {
                is Tipping.Succeeded -> {
                    activity.baseConfig.isProSubs = true
                    callback(true)
                }

                is Tipping.NoTips -> {
                    activity.baseConfig.isProSubs = false
                    callback(true)
                }

                is Tipping.FailedToLoad -> {}
            }
        }
    }
}
