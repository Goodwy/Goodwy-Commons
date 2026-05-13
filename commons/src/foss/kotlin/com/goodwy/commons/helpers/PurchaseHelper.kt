package com.goodwy.commons.helpers

import android.app.Application
import android.os.Bundle
import com.goodwy.commons.activities.BaseSimpleActivity

class PurchaseHelper {
    fun initPurchaseIfNeed(app: Application, id: String) {
        //Not used for Foss
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
        //Not used for Foss
    }
}
