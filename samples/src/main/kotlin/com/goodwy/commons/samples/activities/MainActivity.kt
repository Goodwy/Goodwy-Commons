package com.goodwy.commons.samples.activities

import android.os.Bundle
import com.goodwy.commons.activities.BaseSimpleActivity
import com.goodwy.commons.dialogs.BottomSheetChooserDialog
import com.goodwy.commons.dialogs.SecurityDialog
import com.goodwy.commons.extensions.appLaunched
import com.goodwy.commons.extensions.updateTextColors
import com.goodwy.commons.extensions.toast
import com.goodwy.commons.models.SimpleListItem
import com.goodwy.commons.helpers.LICENSE_GLIDE
import com.goodwy.commons.helpers.SHOW_ALL_TABS
import com.goodwy.commons.models.FAQItem
import com.goodwy.commons.samples.BuildConfig
import com.goodwy.commons.samples.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseSimpleActivity() {
    override fun getAppLauncherName() = getString(R.string.smtco_app_name)

    override fun getAppIconIDs(): ArrayList<Int> {
        val ids = ArrayList<Int>()
        ids.add(R.mipmap.ic_launcher)
        return ids
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        appLaunched(BuildConfig.APPLICATION_ID)

        updateMaterialActivityViews(main_coordinator, main_holder, useTransparentNavigation = true, useTopSearchMenu = true)
        setupMaterialScrollListener(main_nested_scrollview, main_toolbar)

        main_color_customization.setOnClickListener {
            startCustomizationActivity()
        }

        main_about.setOnClickListener {
            val licenses = LICENSE_GLIDE

            val faqItems = arrayListOf(
                FAQItem(R.string.faq_2_title_commons, R.string.faq_2_text_commons),
                FAQItem(R.string.faq_7_title_commons, R.string.faq_7_text_commons),
                FAQItem(R.string.faq_9_title_commons, R.string.faq_9_text_commons)
            )
            startAboutActivity(R.string.app_name, licenses, BuildConfig.VERSION_NAME, faqItems, true, "", "", "", "", "", "", "", true)
        }

        main_purchase.setOnClickListener {
            startPurchaseActivity(R.string.app_name_g, "BuildConfig.GOOGLE_PLAY_LICENSING_KEY", "BuildConfig.PRODUCT_ID_X1", "BuildConfig.PRODUCT_ID_X2", "BuildConfig.PRODUCT_ID_X3", "", "", "", showLifebuoy = true, playStoreInstalled = true, showCollection = true)
        }

        bottom_sheet_chooser.setOnClickListener {
            launchBottomSheetDemo()
        }

        security.setOnClickListener {
            SecurityDialog(this, "", SHOW_ALL_TABS) { _, _, _ ->
            }
        }

        //startCustomizationActivity()
        //startAboutActivity(R.string.smtco_app_name, 3, "0.2", arrayListOf(FAQItem(R.string.faq_1_title_commons, R.string.faq_1_text_commons)), false)

        /*val letters = arrayListOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q")
        StringsAdapter(this, letters, media_grid, media_refresh_layout) {
        }.apply {
            media_grid.adapter = this
        }

        media_refresh_layout.setOnRefreshListener {
            Handler().postDelayed({
                media_refresh_layout.isRefreshing = false
            }, 1000L)
        }*/
    }

    private fun launchBottomSheetDemo() {
        BottomSheetChooserDialog.createChooser(
            fragmentManager = supportFragmentManager,
            title = R.string.please_select_destination,
            items = arrayOf(
                SimpleListItem(1, R.string.record_video, imageRes = R.drawable.ic_camera_vector),
                SimpleListItem(2, R.string.record_audio, imageRes = R.drawable.ic_microphone_vector, selected = true),
                SimpleListItem(4, R.string.choose_contact, imageRes = R.drawable.ic_add_person_vector)
            )
        ) {
            toast("Clicked ${it.id}")
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(main_toolbar)

        updateTextColors(main_coordinator)
    }
}
