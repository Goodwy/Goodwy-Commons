package com.goodwy.commons.helpers

import android.app.Application
import android.content.Context
import android.content.Intent
import com.goodwy.commons.activities.BaseSimpleActivity
import com.goodwy.commons.extensions.baseConfig
import com.goodwy.commons.extensions.toast
import com.goodwy.commons.helpers.AppUpdateHelper.isHmsAvailable
import com.huawei.hms.jos.AppUpdateClient
import com.huawei.hms.jos.JosApps
import com.huawei.updatesdk.service.appmgr.bean.ApkUpgradeInfo
import com.huawei.updatesdk.service.otaupdate.CheckUpdateCallBack
import com.huawei.updatesdk.service.otaupdate.UpdateKey

class PurchaseHelper {
    fun initPurchaseIfNeed(app: Application, id: String) {
        //Not used for HMS
    }

    fun checkPurchase(
        activity: BaseSimpleActivity,
        iapList: ArrayList<String> = arrayListOf("pro_version", "pro_version_x2", "pro_version_x3"),
        subList: ArrayList<String> = arrayListOf(
            "subscription_x1", "subscription_x2", "subscription_x3",
            "subscription_year_x1", "subscription_year_x2", "subscription_year_x3"
        ),
        ruStoreList: ArrayList<String> = arrayListOf(
            "pro_version", "pro_version_x2", "pro_version_x3",
            "subscription_x1", "subscription_x2", "subscription_x3",
            "subscription_year_x1", "subscription_year_x2", "subscription_year_x3"
        ),
        callback: (updatePro: Boolean) -> Unit,
    ) {
        // Check Update
        val client = JosApps.getAppUpdateClient(activity)

        if(isHmsAvailable(activity) == 0){ // Check if Huawei Mobile Services available (0 for available) --> Update HMS Core if not available
            client.checkAppUpdate(activity, UpdateCallBack(activity))
        }
        else {
            activity.toast("HMS not available")
        }

        val hmsHelper = HmsHelper(activity)
        hmsHelper.initBillingClient()
        hmsHelper.retrieveDonation(iapList, subList)

        hmsHelper.isIapPurchasedLiveData.observe(activity) {
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

        hmsHelper.isSupPurchasedLiveData.observe(activity) {
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

    /**
     * Class for handling the response after checking if an update is available
     * @param[context] Context for this function
     * @return handler for the response after checking if an update is available
     * @author samlach2222
     */
    private class UpdateCallBack(private var context: Context) : CheckUpdateCallBack {
        val client: AppUpdateClient = JosApps.getAppUpdateClient(context)

        /**
         * function for starting the update
         * @param[intent] Intent used by the update
         * @author samlach2222
         */
        override fun onUpdateInfo(intent: Intent?) {
            intent?.let {
                // Get the status
                val status = it.getIntExtra(UpdateKey.STATUS, -1)

                // Get error code and error message from extras
                val errorCode = it.getIntExtra(UpdateKey.FAIL_CODE, -1) // if you want to handle it
                val errorMessage = it.getStringExtra(UpdateKey.FAIL_REASON) // if you want to handle it

                // Get the info as serializable
                val info = it.getSerializableExtra(UpdateKey.INFO)

                // If info is an instance of ApkUpgradeInfo, there is an update available
                if (info is ApkUpgradeInfo) {

                    //Show update dialog with force update type (true/false)
                    client.showUpdateDialog(context, info, false)
                }
                else {
                    context.toast("No updates available")
                }
            }
        }

        override fun onMarketInstallInfo(intent: Intent?) {

        }

        override fun onMarketStoreError(errorCode: Int) {

        }

        override fun onUpdateStoreError(errorCode: Int) {

        }
    }
}
