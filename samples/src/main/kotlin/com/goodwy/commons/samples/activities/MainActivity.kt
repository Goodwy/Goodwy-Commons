package com.goodwy.commons.samples.activities

import android.content.Intent
import android.os.Bundle
import com.goodwy.commons.activities.BaseSimpleActivity
import com.goodwy.commons.activities.ManageBlockedNumbersActivity
import com.goodwy.commons.dialogs.BottomSheetChooserDialog
import com.goodwy.commons.dialogs.SecurityDialog
import com.goodwy.commons.dialogs.OverflowIconDialog
import com.goodwy.commons.extensions.*
import com.goodwy.commons.models.SimpleListItem
import com.goodwy.commons.helpers.LICENSE_AUTOFITTEXTVIEW
import com.goodwy.commons.helpers.SHOW_ALL_TABS
import com.goodwy.commons.models.FAQItem
import com.goodwy.commons.samples.BuildConfig
import com.goodwy.commons.samples.R
import com.goodwy.commons.samples.databinding.ActivityMainBinding

class MainActivity : BaseSimpleActivity() {
    override fun getAppLauncherName() = getString(R.string.smtco_app_name)

    override fun getAppIconIDs(): ArrayList<Int> {
        val ids = ArrayList<Int>()
        ids.add(R.mipmap.ic_launcher)
        return ids
    }

    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        appLaunched(BuildConfig.APPLICATION_ID)

        setupOptionsMenu()
        updateMaterialActivityViews(binding.mainCoordinator, null, useTransparentNavigation = true, useTopSearchMenu = true)
        //setupMaterialScrollListener(binding.mainNestedScrollview, binding.mainToolbar)
        //binding.mainToolbar.updateTitle(getString(com.goodwy.commons.R.string.simple_commons))

        binding.mainColorCustomization.setOnClickListener {
            startCustomizationActivity(
                isCollection = false,
                playStoreInstalled = isPlayStoreInstalled(),
                ruStoreInstalled = isRuStoreInstalled(),
                )
        }
        binding.mainAbout.setOnClickListener {
            launchAbout()
        }
        binding.mainPurchase.setOnClickListener {
            startPurchaseActivity(R.string.app_name_g, "", "", "", "", "", "", "",
                showLifebuoy = false,
                playStoreInstalled = isPlayStoreInstalled(),
                ruStoreInstalled = isRuStoreInstalled(),
                showCollection = true)
        }
        binding.bottomSheetChooser.setOnClickListener {
            launchBottomSheetDemo()
        }
        binding.security.setOnClickListener {
            SecurityDialog(this, "", SHOW_ALL_TABS) { _, _, _ ->
            }
        }
        binding.overflowIcon.setOnClickListener {
            OverflowIconDialog(this) {
                binding.mainToolbar.updateColors()
            }
        }
        binding.manageBlockedNumbers.setOnClickListener {
            startActivity(Intent(this, ManageBlockedNumbersActivity::class.java))
        }
        binding.composeDialogs.setOnClickListener {
            startActivity(Intent(this, TestDialogActivity::class.java))
        }
    }

    private fun launchAbout() {
        val licenses = LICENSE_AUTOFITTEXTVIEW

        val faqItems = arrayListOf(
            FAQItem(com.goodwy.commons.R.string.faq_1_title_commons, com.goodwy.commons.R.string.faq_1_text_commons),
            FAQItem(com.goodwy.commons.R.string.faq_1_title_commons, com.goodwy.commons.R.string.faq_1_text_commons),
            FAQItem(com.goodwy.commons.R.string.faq_4_title_commons, com.goodwy.commons.R.string.faq_4_text_commons)
        )

        if (!resources.getBoolean(com.goodwy.commons.R.bool.hide_google_relations)) {
            faqItems.add(FAQItem(com.goodwy.commons.R.string.faq_2_title_commons, com.goodwy.commons.R.string.faq_2_text_commons))
            faqItems.add(FAQItem(com.goodwy.commons.R.string.faq_6_title_commons, com.goodwy.commons.R.string.faq_6_text_commons))
        }

        startAboutActivity(R.string.app_name_g, licenses, BuildConfig.VERSION_NAME, faqItems, true, "", "", "", "", "", "", "",
            playStoreInstalled = isPlayStoreInstalled(),
            ruStoreInstalled = isRuStoreInstalled())
    }

    private fun setupOptionsMenu() {
        binding.mainToolbar.getToolbar().inflateMenu(R.menu.menu)
    }

    private fun launchBottomSheetDemo() {
        BottomSheetChooserDialog.createChooser(
            fragmentManager = supportFragmentManager,
            title = com.goodwy.commons.R.string.please_select_destination,
            items = arrayOf(
                SimpleListItem(1, com.goodwy.commons.R.string.record_video, imageRes = com.goodwy.commons.R.drawable.ic_camera_vector),
                SimpleListItem(
                    2,
                    com.goodwy.commons.R.string.record_audio,
                    imageRes = com.goodwy.commons.R.drawable.ic_microphone_vector,
                    selected = true
                ),
                SimpleListItem(4, com.goodwy.commons.R.string.choose_contact, imageRes = com.goodwy.commons.R.drawable.ic_add_person_vector)
            )
        ) {
            toast("Clicked ${it.id}")
        }
    }

    override fun onResume() {
        super.onResume()
        //setupToolbar(binding.mainToolbar)

        updateStatusbarColor(getProperBackgroundColor())
        binding.mainToolbar.updateColors()

//        CallConfirmationDialog(this, callee = "Goodwy Common"){
//
//        }

        updateTextColors(binding.mainCoordinator)
        binding.switchOn.setColors(getProperTextColor(), getProperAccentColor(), getProperBackgroundColor())
    }
}
