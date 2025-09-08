package com.goodwy.commons.activities

import android.content.Context
import androidx.activity.ComponentActivity
import com.goodwy.commons.extensions.baseConfig
import com.goodwy.commons.helpers.MyContextWrapper
import com.goodwy.commons.helpers.REQUEST_APP_UNLOCK
import com.goodwy.commons.helpers.isTiramisuPlus

abstract class BaseComposeActivity : ComponentActivity() {

    override fun onResume() {
        super.onResume()
        maybeLaunchAppUnlockActivity(REQUEST_APP_UNLOCK)
    }

    override fun attachBaseContext(newBase: Context) {
        if (newBase.baseConfig.useEnglish && !isTiramisuPlus()) {
            super.attachBaseContext(MyContextWrapper(newBase).wrap(newBase, "en"))
        } else {
            super.attachBaseContext(newBase)
        }
    }
}
