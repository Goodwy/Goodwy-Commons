package com.goodwy.commons.helpers

import android.app.Application
import com.goodwy.commons.activities.BaseSimpleActivity

class PurchaseHelper {
    fun initPurchaseIfNeed(app: Application, id: String) {
        //Not used for Foss
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
        //Not used for Foss
    }
}
