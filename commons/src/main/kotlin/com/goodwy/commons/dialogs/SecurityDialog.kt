package com.goodwy.commons.dialogs

import android.app.Activity
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.goodwy.commons.R
import com.goodwy.commons.adapters.PasswordTypesAdapter
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.PROTECTION_FINGERPRINT
import com.goodwy.commons.helpers.PROTECTION_PATTERN
import com.goodwy.commons.helpers.PROTECTION_PIN
import com.goodwy.commons.helpers.SHOW_ALL_TABS
import com.goodwy.commons.interfaces.HashListener
import com.goodwy.commons.views.MyDialogViewPager
import kotlinx.android.synthetic.main.dialog_security.view.*

class SecurityDialog(val activity: Activity, val requiredHash: String, val showTabIndex: Int, val callback: (hash: String, type: Int, success: Boolean) -> Unit)
    : HashListener {
    var dialog: AlertDialog? = null
    val view = LayoutInflater.from(activity).inflate(R.layout.dialog_security, null)
    var tabsAdapter: PasswordTypesAdapter
    var viewPager: MyDialogViewPager

    init {
        view.apply {
            viewPager = findViewById(R.id.dialog_tab_view_pager)
            viewPager.offscreenPageLimit = 2
            tabsAdapter = PasswordTypesAdapter(context, requiredHash, this@SecurityDialog, dialog_scrollview)
            viewPager.adapter = tabsAdapter
            viewPager.onPageChangeListener {
                dialog_tab_layout.getTabAt(it)!!.select()
            }

            viewPager.onGlobalLayout {
                updateTabVisibility()
            }

            if (showTabIndex == SHOW_ALL_TABS) {
                val textColor = context.baseConfig.textColor

                if (!activity.isFingerPrintSensorAvailable())
                    dialog_tab_layout.removeTabAt(PROTECTION_FINGERPRINT)

                dialog_tab_layout.setTabTextColors(textColor, textColor)
                dialog_tab_layout.setSelectedTabIndicatorColor(context.getAdjustedPrimaryColor())
                dialog_tab_layout.onTabSelectionChanged(tabSelectedAction = {
                    viewPager.currentItem = when {
                        it.text.toString().equals(resources.getString(R.string.pattern), true) -> PROTECTION_PATTERN
                        it.text.toString().equals(resources.getString(R.string.pin), true) -> PROTECTION_PIN
                        else -> PROTECTION_FINGERPRINT
                    }
                    updateTabVisibility()
                })
            } else {
                dialog_tab_layout.beGone()
                viewPager.currentItem = showTabIndex
                viewPager.allowSwiping = false
            }
        }

        dialog = AlertDialog.Builder(activity)
            .setOnCancelListener { onCancelFail() }
            .setNegativeButton(R.string.cancel) { dialog, which -> onCancelFail() }
            .create().apply {
                activity.setupDialogStuff(view, this)
            }
    }

    private fun onCancelFail() {
        callback("", 0, false)
        dialog!!.dismiss()
    }

    override fun receivedHash(hash: String, type: Int) {
        callback(hash, type, true)
        if (!activity.isFinishing) {
            dialog?.dismiss()
        }
    }

    private fun updateTabVisibility() {
        for (i in 0..2) {
            tabsAdapter.isTabVisible(i, viewPager.currentItem == i)
        }
    }
}
