package com.goodwy.commons.helpers

import android.app.Application
import android.os.Bundle
import com.goodwy.commons.activities.BaseSimpleActivity
import com.goodwy.commons.extensions.baseConfig

class PurchaseHelper {
    fun initPurchaseIfNeed(app: Application, id: String) {
        //Not used for Google Play
    }

    fun initPurchaseToActivityIfNeed(
        savedInstanceState: Bundle?,
        callback: (initialized: Boolean) -> Unit
    ) {
        //Not used for Google Play
    }

    fun checkPurchase(
        activity: BaseSimpleActivity,
        iapList: ArrayList<String> = arrayListOf(),
        subList: ArrayList<String> = arrayListOf(),
        ruStoreList: ArrayList<String> = arrayListOf(),
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
