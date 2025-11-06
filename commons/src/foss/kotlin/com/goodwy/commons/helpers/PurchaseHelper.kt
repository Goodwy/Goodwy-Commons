package com.goodwy.commons.helpers

import android.app.Application
import com.goodwy.commons.activities.BaseSimpleActivity
import com.goodwy.filemanager.App
import com.goodwy.filemanager.activities.SimpleActivity

class PurchaseHelper {
    fun initPurchaseIfNeed(app: Application) {
        //Not used for Foss
    }

    fun checkPurchase(
        activity: BaseSimpleActivity, 
        callback: (updatePro: Boolean) -> Unit,
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
    ) {
        //Not used for Foss
    }
}
