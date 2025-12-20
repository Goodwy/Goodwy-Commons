package com.goodwy.commons.helpers

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.huawei.hms.adapter.AvailableAdapter
import com.huawei.hms.adapter.internal.AvailableCode
import com.huawei.hms.api.ConnectionResult

/**
 * Check for HMS Update
 */
object AppUpdateHelper {
    private const val tag = "com.goodwy.commons.helpers.AppUpdateHelper"
    private var isInitialized = false
    private var versionCheckResult = 12

    /**
     * Check if HMS needs update
     *
     * @param context context
     * @return result，0 Available, 1 not Available
     */
    fun isHmsAvailable(context: Context): Int {
        if (versionCheckResult == ConnectionResult.SUCCESS) {
            return ConnectionResult.SUCCESS
        }
        Log.d(tag, "isInitialized is:$isInitialized")
        if (isInitialized) {
            return 1
        }

        // minimum HMS version, if less than this version, result will not be 0
        val baseVersion = 60600311
        val availableAdapter = AvailableAdapter(baseVersion)
        val result = availableAdapter.isHuaweiMobileServicesAvailable(context)
        Log.i(tag, "HMS update result is: $result")
        isInitialized = true
        if (result == ConnectionResult.SUCCESS) {
            Log.i(tag, "HMS is available")
        } else {
            if (availableAdapter.isUserResolvableError(result)) {
                resolution(availableAdapter, context)
            } else {
                Log.e(tag, "HMS is not available " + AvailableCode.ERROR_NO_ACTIVITY)
            }
        }
        versionCheckResult = result
        return result
    }

    private fun resolution(availableAdapter: AvailableAdapter, context: Context) {
        Log.i(tag, "HMS update start :")
        val activity = findActivity(context)
        if (activity == null) {
            Log.e(tag, "HMS is not available" + AvailableCode.ERROR_NO_ACTIVITY)
            return
        }

        // this method will be call upgrade dialog box.
        availableAdapter.startResolution(
            activity
        ) { result ->
            if (result == AvailableCode.SUCCESS) {
                versionCheckResult = result
                Log.i(tag, "HMS update start success")
            } else {
                Log.e(tag, "HMS update failed: $result")
                isInitialized = false
            }
        }
    }

    /**
     * Get Activity by Context
     * @param context context
     * @return Activity
     */
    private fun findActivity(context: Context?): Activity? {
        val activity: Activity? = null
        if (context is Activity) {
            return context
        }
        return if (context is ContextWrapper) {
            findActivity(context.baseContext)
        } else {
            activity
        }
    }
}
