package com.goodwy.commons.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.updateLayoutParams
import com.behaviorule.arturdumchev.library.pixels
import com.goodwy.commons.R
import com.goodwy.commons.databinding.ActivityCustomizationBinding
import com.goodwy.commons.dialogs.*
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.*
import com.goodwy.commons.models.MyTheme
import com.goodwy.commons.models.RadioItem
import com.goodwy.commons.models.SharedTheme
import com.mikhaellopez.rxanimation.RxAnimation
import com.mikhaellopez.rxanimation.shake

class CustomizationActivity : BaseSimpleActivity() {
    private val THEME_LIGHT = 0
    private val THEME_DARK = 1
    private val THEME_BLACK_WHITE = 4
    private val THEME_CUSTOM = 5
    private val THEME_SHARED = 6
    private val THEME_WHITE = 7
    private val THEME_BLACK = 8
    private val THEME_GRAY = 9
    private val THEME_AUTO = 2
    private val THEME_SYSTEM = 3    // Material You

    private var curTextColor = 0
    private var curBackgroundColor = 0
    private var curPrimaryColor = 0
    private var curAccentColor = 0
    private var curAppIconColor = 0
    private var curSelectedThemeId = 0
    private var originalAppIconColor = 0
    private var lastSavePromptTS = 0L
    private var hasUnsavedChanges = false
    private var isThankYou = false      // show "Apply colors to all Simple apps" in Simple Thank You itself even with "Hide Google relations" enabled
    private var predefinedThemes = LinkedHashMap<Int, MyTheme>()
    private var curPrimaryLineColorPicker: LineColorPickerDialog? = null
    private var curPrimaryGridColorPicker: GridColorPickerDialog? = null
    private var storedSharedTheme: SharedTheme? = null

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    private fun getShowAccentColor() = intent.getBooleanExtra(SHOW_ACCENT_COLOR, true)

    private fun isProVersion() = intent.getBooleanExtra(IS_COLLECTION, false) || isPro()
    private fun getLicensingKey() = intent.getStringExtra(GOOGLE_PLAY_LICENSING_KEY) ?: ""
    private fun getProductIdList() = intent.getStringArrayListExtra(PRODUCT_ID_LIST) ?: arrayListOf("", "", "")
    private fun getProductIdListRu() = intent.getStringArrayListExtra(PRODUCT_ID_LIST_RU) ?: arrayListOf("", "", "")
    private fun getSubscriptionIdList() = intent.getStringArrayListExtra(SUBSCRIPTION_ID_LIST) ?: arrayListOf("", "", "")
    private fun getSubscriptionIdListRu() = intent.getStringArrayListExtra(SUBSCRIPTION_ID_LIST_RU) ?: arrayListOf("", "", "")
    private fun getSubscriptionYearIdList() = intent.getStringArrayListExtra(SUBSCRIPTION_YEAR_ID_LIST) ?: arrayListOf("", "", "")
    private fun getSubscriptionYearIdListRu() = intent.getStringArrayListExtra(SUBSCRIPTION_YEAR_ID_LIST_RU) ?: arrayListOf("", "", "")

    private fun playStoreInstalled() = intent.getBooleanExtra(PLAY_STORE_INSTALLED, true)
    private fun ruStoreInstalled() = intent.getBooleanExtra(RU_STORE, false)

    private val binding by viewBinding(ActivityCustomizationBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupOptionsMenu()
        refreshMenuItems()

        updateMaterialActivityViews(binding.customizationCoordinator, binding.customizationHolder, useTransparentNavigation = true, useTopSearchMenu = false)
        setupMaterialScrollListener(binding.customizationNestedScrollview, binding.customizationToolbar)

//        // TODO TRANSPARENT Navigation Bar
//        setWindowTransparency(true) { _, bottomNavigationBarSize, leftNavigationBarSize, rightNavigationBarSize ->
//            binding.customizationCoordinator.setPadding(leftNavigationBarSize, 0, rightNavigationBarSize, 0)
//            updateNavigationBarColor(getProperBackgroundColor())
//            binding.customizationHolder.updateLayoutParams<ViewGroup.MarginLayoutParams> {
//                setMargins(0, 0, 0, bottomNavigationBarSize + pixels(R.dimen.activity_margin).toInt())
//            }
//        }

        isThankYou = packageName.removeSuffix(".debug") == "com.goodwy.sharedtheme"
        initColorVariables()

        //TODO HIDE
        binding.applyToAllHolder.beGone()
        binding.customizationAppIconColorHolder.beGone()

        if (isSharedThemeInstalled()) {
            val cursorLoader = getMyContentProviderCursorLoader()
            ensureBackgroundThread {
                try {
                    storedSharedTheme = getSharedThemeSync(cursorLoader)
                    if (storedSharedTheme == null) {
                        baseConfig.isUsingSharedTheme = false
                    } else {
                        baseConfig.wasSharedThemeEverActivated = true
                    }

                    runOnUiThread {
                        setupThemes()
                        //TODO HIDE
                        //val hideGoogleRelations = resources.getBoolean(R.bool.hide_google_relations) && !isThankYou
                        //binding.applyToAllHolder.beVisibleIf(
                        //    storedSharedTheme == null && curSelectedThemeId != THEME_AUTO && curSelectedThemeId != THEME_SYSTEM && !hideGoogleRelations
                        //)
                    }
                } catch (e: Exception) {
                    toast(R.string.update_thank_you_g)
                    finish()
                }
            }
        } else {
            setupThemes()
            baseConfig.isUsingSharedTheme = false
        }

        //supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_cross_vector)
        val textColor = if (baseConfig.isUsingSystemTheme) {
            getProperTextColor()
        } else {
            baseConfig.textColor
        }

        updateLabelColors(textColor)
        originalAppIconColor = baseConfig.appIconColor

//        if (resources.getBoolean(R.bool.hide_google_relations) && !isThankYou) {
//            binding.applyToAllHolder.beGone()
//        }
    }

    override fun onResume() {
        super.onResume()
        setTheme(getThemeId(getCurrentPrimaryColor()))

        if (!baseConfig.isUsingSystemTheme) {
            updateBackgroundColor(getCurrentBackgroundColor())
            updateActionbarColor(getCurrentBackgroundColor()) //TODO actionbar color
        }

       /* curPrimaryLineColorPicker?.getSpecificColor()?.apply {
            updateActionbarColor(this)
            setTheme(getThemeId(this))
        }*/
        curPrimaryGridColorPicker?.getSpecificColor()?.apply {
           // updateActionbarColor(this)
            setTheme(getThemeId(this))
        }

        setupToolbar(binding.customizationToolbar, NavigationIcon.Arrow)
        updateHoldersColor()

        setupPurchaseThankYou()
        updateAutoThemeFields()
        setupTopAppBarColorIcon()
        setupTopAppBarColorTitle()
        setupUseAccentColor()
    }

    private fun updateHoldersColor(color: Int = getBottomNavigationBackgroundColor()) {
        arrayOf(
            binding.themeHolder,
            binding.primaryColorsHolder,
            binding.colorsHolder
        ).forEach {
            it.background.applyColorFilter(color)
        }
    }

    private fun refreshMenuItems() {
        binding.customizationToolbar.menu.findItem(R.id.save).isVisible = hasUnsavedChanges
    }

    private fun setupOptionsMenu() {
        binding.customizationToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.save -> {
                    saveChanges(true)
                    true
                }

                else -> false
            }
        }
    }

    override fun onBackPressed() {
        if (hasUnsavedChanges && System.currentTimeMillis() - lastSavePromptTS > SAVE_DISCARD_PROMPT_INTERVAL) {
            promptSaveDiscard()
        } else {
            super.onBackPressed()
        }
    }

    private fun setupThemes() {
        predefinedThemes.apply {
            if (isSPlus()) {
                put(THEME_SYSTEM, getSystemThemeColors())
            }

            put(THEME_AUTO, getAutoThemeColors())
            put(
                THEME_LIGHT,
                MyTheme(
                    getString(R.string.light_theme),
                    R.color.theme_light_text_color,
                    R.color.theme_light_background_color,
                    R.color.color_primary,
                    R.color.color_primary
                )
            )
            put(
                THEME_GRAY,
                MyTheme(
                    getString(R.string.gray_theme),
                    R.color.theme_gray_text_color,
                    R.color.theme_gray_background_color,
                    R.color.color_primary,
                    R.color.color_primary
                )
            )
            put(
                THEME_DARK,
                MyTheme(
                    getString(R.string.dark_theme),
                    R.color.theme_dark_text_color,
                    R.color.theme_dark_background_color,
                    R.color.color_primary,
                    R.color.color_primary
                )
            )
            put(
                THEME_BLACK,
                MyTheme(
                    getString(R.string.black),
                    R.color.theme_black_text_color,
                    R.color.theme_black_background_color,
                    R.color.color_primary,
                    R.color.color_primary
                )
            )
            // TODO HIDE
            /*put(
                THEME_DARK_RED,
                MyTheme(
                    getString(R.string.dark_red),
                    R.color.theme_dark_text_color,
                    R.color.theme_dark_background_color,
                    R.color.theme_dark_red_primary_color,
                    R.color.md_red_700
                )
            )
            put(THEME_WHITE, MyTheme(R.string.white, R.color.dark_grey, android.R.color.white, android.R.color.white, R.color.color_primary))
            put(
                THEME_BLACK_WHITE,
                MyTheme(getString(R.string.black_white), android.R.color.white, android.R.color.black, android.R.color.black, R.color.md_grey_black)
            )
            put(THEME_CUSTOM, MyTheme(R.string.custom, 0, 0, 0, 0))*/

            if (storedSharedTheme != null) {
                put(THEME_SHARED, MyTheme(getString(R.string.shared), 0, 0, 0, 0))
            }
        }
        setupThemePicker()
        setupColorsPickers()
    }

    private fun setupThemePicker() {
        curSelectedThemeId = getCurrentThemeId()
        binding.customizationTheme.text = getThemeText()
        updateAutoThemeFields()
        handleAccentColorLayout()

        binding.customizationThemeDescription.setColors(getCurrentTextColor(), getCurrentPrimaryColor(), getCurrentBackgroundColor())
        binding.customizationThemeHolder.setOnClickListener {
            if (isProVersion()) themePickerClicked() else shakePurchase()
        }

//        if (binding.customizationTheme.value == getMaterialYouString()) {
//            binding.applyToAllHolder.beGone()
//        }
    }

    private fun themePickerClicked() {
        val items = arrayListOf<RadioItem>()
        for ((key, value) in predefinedThemes) {
            items.add(RadioItem(key, value.label))
        }

        RadioGroupDialog(this@CustomizationActivity, items, curSelectedThemeId) {
            if (it == THEME_SHARED && !isSharedThemeInstalled()) {
                PurchaseThankYouDialog(this)
                return@RadioGroupDialog
            }

            updateColorTheme(it as Int, true)
            if (it != THEME_CUSTOM && it != THEME_SHARED && it != THEME_AUTO && it != THEME_SYSTEM && !baseConfig.wasCustomThemeSwitchDescriptionShown) {
                baseConfig.wasCustomThemeSwitchDescriptionShown = true
                toast(R.string.changing_color_description)
            }

            updateMenuItemColors(binding.customizationToolbar.menu, getCurrentStatusBarColor())
            setupToolbar(binding.customizationToolbar, NavigationIcon.Cross, getCurrentStatusBarColor())
            updateTopBarColors(binding.customizationToolbar, getCurrentBackgroundColor(), curPrimaryColor)
        }
    }

    private fun updateColorTheme(themeId: Int, useStored: Boolean = false) {
        curSelectedThemeId = themeId
        binding.customizationTheme.text = getThemeText()

        resources.apply {
            if (curSelectedThemeId == THEME_CUSTOM) {
                if (useStored) {
                    curTextColor = baseConfig.customTextColor
                    curBackgroundColor = baseConfig.customBackgroundColor
                    curPrimaryColor = baseConfig.customPrimaryColor
                    curAccentColor = baseConfig.customAccentColor
                    curAppIconColor = baseConfig.customAppIconColor
                    setTheme(getThemeId(curPrimaryColor))
                    updateMenuItemColors(binding.customizationToolbar.menu, curBackgroundColor) //curPrimaryColor
                    setupToolbar(binding.customizationToolbar, NavigationIcon.Cross, curBackgroundColor) //curPrimaryColor
                    setupColorsPickers()
                    updateActionbarColor(curBackgroundColor)
                } else {
                    baseConfig.customPrimaryColor = curPrimaryColor
                    baseConfig.customAccentColor = curAccentColor
                    baseConfig.customBackgroundColor = curBackgroundColor
                    baseConfig.customTextColor = curTextColor
                    baseConfig.customAppIconColor = curAppIconColor
                }
            } else if (curSelectedThemeId == THEME_SHARED) {
                if (useStored) {
                    storedSharedTheme?.apply {
                        curTextColor = textColor
                        curBackgroundColor = backgroundColor
                        curPrimaryColor = primaryColor
                        curAccentColor = accentColor
                        curAppIconColor = appIconColor
                    }
                    setTheme(getThemeId(curPrimaryColor))
                    setupColorsPickers()
                    updateMenuItemColors(binding.customizationToolbar.menu, curBackgroundColor) //curPrimaryColor
                    setupToolbar(binding.customizationToolbar, NavigationIcon.Cross, curBackgroundColor) //curPrimaryColor
                    updateActionbarColor(curBackgroundColor)
                }
            } else {
                val theme = predefinedThemes[curSelectedThemeId]!!
                curTextColor = getColor(theme.textColorId)
                curBackgroundColor = getColor(theme.backgroundColorId)

                if (curSelectedThemeId != THEME_AUTO && curSelectedThemeId != THEME_SYSTEM) {
                    curPrimaryColor = getColor(theme.primaryColorId)
                    curAccentColor = getColor(R.color.color_accent) // (R.color.color_primary) TODO accent color when choosing a theme R.color.color_primary
                    curAppIconColor = getColor(theme.appIconColorId)
                } else if (curSelectedThemeId == THEME_SYSTEM) {
                    curPrimaryColor = getColor(R.color.you_primary_color)
                }

                setTheme(getThemeId(getCurrentPrimaryColor())) //setTheme(getThemeId(curPrimaryColor))
                colorChanged()
                updateMenuItemColors(binding.customizationToolbar.menu, getCurrentStatusBarColor())
                setupToolbar(binding.customizationToolbar, NavigationIcon.Cross, getCurrentStatusBarColor())
                updateActionbarColor(curBackgroundColor)
            }
            binding.settingsTopAppBarColorIcon.setColors(getCurrentTextColor(), getCurrentPrimaryColor(), getCurrentBackgroundColor())
            binding.settingsTopAppBarColorTitle.setColors(getCurrentTextColor(), getCurrentPrimaryColor(), getCurrentBackgroundColor())
            binding.customizationUseAccentColor.setColors(getCurrentTextColor(), getCurrentPrimaryColor(), getCurrentBackgroundColor())
            binding.customizationThemeDescription.setColors(getCurrentTextColor(), getCurrentPrimaryColor(), getCurrentBackgroundColor())
        }

        val holdersColor = when {
            curSelectedThemeId == THEME_SYSTEM -> resources.getColor(R.color.you_status_bar_color)
            curBackgroundColor == Color.WHITE -> resources.getColor(R.color.bottom_tabs_light_background)
            curBackgroundColor == Color.BLACK -> resources.getColor(R.color.bottom_tabs_black_background)
            else -> curBackgroundColor.lightenColor(4)
        }
        updateHoldersColor(holdersColor)

        hasUnsavedChanges = true
        refreshMenuItems()
        updateLabelColors(getCurrentTextColor()) //updateTextColors(customization_holder, curTextColor)
        updateBackgroundColor(getCurrentBackgroundColor()) //curBackgroundColor
        val actionbarColor = if (curSelectedThemeId == THEME_SYSTEM) getCurrentStatusBarColor() else getCurrentBackgroundColor()
        updateActionbarColor(actionbarColor) //curBackgroundColor //TODO actionbar color
        updateAutoThemeFields()
        updateApplyToAllColors(getCurrentPrimaryColor())
        handleAccentColorLayout()
    }

    private fun getAutoThemeColors(): MyTheme {
        val isUsingSystemDarkTheme = isUsingSystemDarkTheme()
        val textColor = if (isUsingSystemDarkTheme) R.color.theme_black_text_color else R.color.theme_light_text_color
        val backgroundColor = if (isUsingSystemDarkTheme) R.color.theme_black_background_color else R.color.theme_light_background_color
        return MyTheme(getString(R.string.auto_light_dark_theme), textColor, backgroundColor, R.color.color_primary, R.color.color_primary)
    }

    // doesn't really matter what colors we use here, everything will be taken from the system. Use the default dark theme values here.
    private fun getSystemThemeColors(): MyTheme {
        return MyTheme(
            getMaterialYouString(),
            R.color.theme_dark_text_color,
            R.color.theme_dark_background_color,
            R.color.color_primary,
            R.color.color_primary
        )
    }

    private fun getCurrentThemeId(): Int {
        if (baseConfig.isUsingSharedTheme) {
            return THEME_SHARED
        } else if ((baseConfig.isUsingSystemTheme && !hasUnsavedChanges) || curSelectedThemeId == THEME_SYSTEM) {
            return THEME_SYSTEM
        } else if (baseConfig.isUsingAutoTheme || curSelectedThemeId == THEME_AUTO) {
            return THEME_AUTO
        }

        var themeId = THEME_CUSTOM
        resources.apply {
            for ((key, value) in predefinedThemes.filter { it.key != THEME_CUSTOM && it.key != THEME_SHARED && it.key != THEME_AUTO && it.key != THEME_SYSTEM }) {
                if (curTextColor == getColor(value.textColorId) &&
                    curBackgroundColor == getColor(value.backgroundColorId) &&
                    curPrimaryColor == getColor(value.primaryColorId) &&
                    curAppIconColor == getColor(value.appIconColorId)
                ) {
                    themeId = key
                }
            }
        }

        return themeId
    }

    private fun getThemeText(): String {
        var label = getString(R.string.custom)
        for ((key, value) in predefinedThemes) {
            if (key == curSelectedThemeId) {
                label = value.label
            }
        }
        return label
    }

    private fun updateAutoThemeFields() {
        binding.customizationThemeDescription.beVisibleIf(curSelectedThemeId == THEME_SYSTEM)
        binding.customizationThemeHolder.apply {
            alpha = if (!isProVersion()) 0.3f else 1f
        }
        //binding.customizationPrimaryColorHolder.beVisibleIf(curSelectedThemeId != THEME_SYSTEM)
        arrayOf(binding.customizationPrimaryColorHolder, binding.customizationTextCursorColorHolder).forEach {
            if (!isProVersion()) {
                it.isEnabled = true
                it.alpha = 0.3f
            } else {
                if (curSelectedThemeId == THEME_SYSTEM) {
                    it.isEnabled = false
                    it.alpha = 0.3f
                } else {
                    it.isEnabled = true
                    it.alpha = 1f
                }
            }
        }
        binding.customizationAccentColorHolder.apply {
            if (!isProVersion()) {
                this.isEnabled = true
                this.alpha = 0.3f
            } else {
                if (curSelectedThemeId == THEME_SYSTEM || !baseConfig.isUsingAccentColor) {
                    this.isEnabled = false
                    this.alpha = 0.3f
                } else {
                    this.isEnabled = true
                    this.alpha = 1f
                }
            }
        }
        arrayOf(binding.customizationTextColorHolder, binding.customizationBackgroundColorHolder).forEach {
            if (!isProVersion()) {
                it.isEnabled = true
                it.alpha = 0.3f
            } else {
                if (curSelectedThemeId == THEME_AUTO || curSelectedThemeId == THEME_SYSTEM) {
                    it.isEnabled = false
                    it.alpha = 0.3f
                } else {
                    it.isEnabled = true
                    it.alpha = 1f
                }
            }
        }
    }

    private fun promptSaveDiscard() {
        lastSavePromptTS = System.currentTimeMillis()
        ConfirmationAdvancedDialog(this, "", R.string.save_before_closing, R.string.save, R.string.discard) {
            if (it) {
                saveChanges(true)
            } else {
                resetColors()
                finish()
            }
        }
    }

    private fun saveChanges(finishAfterSave: Boolean) {
        val didAppIconColorChange = curAppIconColor != originalAppIconColor
        baseConfig.apply {
            textColor = curTextColor
            backgroundColor = curBackgroundColor
            primaryColor = curPrimaryColor
            accentColor = curAccentColor
            appIconColor = curAppIconColor
        }

        if (didAppIconColorChange) {
            checkAppIconColor()
        }

        if (curSelectedThemeId == THEME_SHARED) {
            val newSharedTheme = SharedTheme(curTextColor, curBackgroundColor, curPrimaryColor, curAppIconColor, 0, curAccentColor)
            updateSharedTheme(newSharedTheme)
            Intent().apply {
                action = MyContentProvider.SHARED_THEME_UPDATED
                sendBroadcast(this)
            }
        }

        baseConfig.isUsingSharedTheme = curSelectedThemeId == THEME_SHARED
        baseConfig.shouldUseSharedTheme = curSelectedThemeId == THEME_SHARED
        baseConfig.isUsingAutoTheme = curSelectedThemeId == THEME_AUTO
        baseConfig.isUsingSystemTheme = curSelectedThemeId == THEME_SYSTEM

        hasUnsavedChanges = false
        if (finishAfterSave) {
            finish()
        } else {
            refreshMenuItems()
        }
    }

    private fun resetColors() {
        hasUnsavedChanges = false
        initColorVariables()
        setupColorsPickers()
        updateBackgroundColor()
        updateActionbarColor()
        refreshMenuItems()
        updateLabelColors(getCurrentTextColor())
    }

    private fun initColorVariables() {
        curTextColor = baseConfig.textColor
        curBackgroundColor = baseConfig.backgroundColor
        curPrimaryColor = baseConfig.primaryColor
        curAccentColor = baseConfig.accentColor
        curAppIconColor = baseConfig.appIconColor
    }

    private fun setupColorsPickers() {
        val textColor = getCurrentTextColor()
        val backgroundColor = getCurrentBackgroundColor()
        val primaryColor = getCurrentPrimaryColor()
        val accentColor = getCurrentAccentColor()
        binding.customizationTextColor.setFillWithStroke(textColor, backgroundColor)
        binding.customizationPrimaryColor.setFillWithStroke(primaryColor, backgroundColor)
        binding.customizationAccentColor.setFillWithStroke(accentColor, backgroundColor)
        binding.customizationBackgroundColor.setFillWithStroke(backgroundColor, backgroundColor)
        binding.customizationAppIconColor.setFillWithStroke(curAppIconColor, backgroundColor)
        //binding.applyToAll.setTextColor(primaryColor.getContrastColor())
        updateTextCursor(baseConfig.textCursorColor)

        binding.customizationTextColorHolder.setOnClickListener { if (isProVersion()) pickTextColor() else shakePurchase() }
        binding.customizationTextCursorColorHolder.setOnClickListener { if (isProVersion()) pickTextCursorColor() else shakePurchase() }
        binding.customizationBackgroundColorHolder.setOnClickListener { if (isProVersion()) pickBackgroundColor() else shakePurchase() }
        binding.customizationPrimaryColorHolder.setOnClickListener { if (isProVersion()) pickPrimaryColor() else shakePurchase() }
        binding.customizationAccentColorHolder.setOnClickListener { if (isProVersion()) pickAccentColor() else shakePurchase() }

        handleAccentColorLayout()
        //binding.applyToAllHolder.setOnClickListener { applyToAll() }
        binding.customizationAppIconColorHolder.setOnClickListener {
            if (baseConfig.wasAppIconCustomizationWarningShown) {
                pickAppIconColor()
            } else {
                ConfirmationDialog(this, "", R.string.app_icon_color_warning, R.string.ok, 0) {
                    baseConfig.wasAppIconCustomizationWarningShown = true
                    pickAppIconColor()
                }
            }
        }
    }

    private fun hasColorChanged(old: Int, new: Int) = Math.abs(old - new) > 1

    private fun colorChanged() {
        hasUnsavedChanges = true
        setupColorsPickers()
        refreshMenuItems()
    }

    private fun setCurrentTextColor(color: Int) {
        curTextColor = color
        updateLabelColors(color) //updateTextColors(customization_holder, color)
    }

    private fun setCurrentBackgroundColor(color: Int) {
        curBackgroundColor = color
        updateBackgroundColor(color)
        updateActionbarColor(color)
    }

    private fun setCurrentPrimaryColor(color: Int) {
        curPrimaryColor = color
        //TODO actionbar color
        //updateActionbarColor(color)
        //updateApplyToAllColors(color)
    }

    private fun updateApplyToAllColors(newColor: Int) {
        if (newColor == baseConfig.primaryColor && !baseConfig.isUsingSystemTheme) {
            binding.applyToAll.setBackgroundResource(R.drawable.button_background_rounded)
        } else {
            val applyBackground = ResourcesCompat.getDrawable(resources, R.drawable.button_background_rounded, theme) as RippleDrawable
            (applyBackground as LayerDrawable).findDrawableByLayerId(R.id.button_background_holder).applyColorFilter(newColor)
            binding.applyToAll.background = applyBackground
        }
    }

    private fun handleAccentColorLayout() {
        binding.customizationAccentColorHolder.beVisibleIf(getShowAccentColor()) //(curSelectedThemeId == THEME_WHITE || isCurrentWhiteTheme() || curSelectedThemeId == THEME_BLACK_WHITE || isCurrentBlackAndWhiteTheme())
        binding.customizationAccentColorLabel.text = getString(R.string.accent_color)
        /*binding.customizationAccentColorLabel.text = getString(if (curSelectedThemeId == THEME_WHITE || isCurrentWhiteTheme()) {
            R.string.accent_color_white
        } else {
            R.string.accent_color_black_and_white
        })*/
    }

    private fun isCurrentWhiteTheme() = curTextColor == DARK_GREY && curPrimaryColor == Color.WHITE && curBackgroundColor == Color.WHITE

    private fun isCurrentBlackAndWhiteTheme() = curTextColor == Color.WHITE && curPrimaryColor == Color.BLACK && curBackgroundColor == Color.BLACK

    private fun pickTextColor() {
        ColorPickerDialog(this, curTextColor, title = resources.getString(R.string.text_color)) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curTextColor, color)) {
                    setCurrentTextColor(color)
                    colorChanged()
                    updateColorTheme(getUpdatedTheme())
                }
            }
        }
    }

    private fun pickTextCursorColor() {
        val textCursorColor = if (baseConfig.textCursorColor == -2) baseConfig.primaryColor else baseConfig.textCursorColor
        ColorPickerDialog(this, textCursorColor, addDefaultColorButton = true, colorDefault = -2, title = resources.getString(R.string.text_cursor_color)) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                updateTextCursor(color)
                val newColor = if (color == -2) getCurrentPrimaryColor() else color
                baseConfig.textCursorColor = color
                binding.customizationTextCursorColor.setFillWithStroke(newColor, getCurrentBackgroundColor())
                baseConfig.tabsChanged = true //without it the color of the cursor in the search menu does not change
            }
        }
    }

    private fun updateTextCursor(color: Int) {
        binding.customizationTextCursorColor.setFillWithStroke(baseConfig.textCursorColor, getCurrentBackgroundColor())
        if (color == -2) {
            binding.customizationTextCursorColor.beGone()
            binding.customizationTextCursorColorDefault.beVisible()
        } else {
            binding.customizationTextCursorColorDefault.beGone()
            binding.customizationTextCursorColor.beVisible()
        }
    }

    private fun pickBackgroundColor() {
        ColorPickerDialog(this, curBackgroundColor, title = resources.getString(R.string.background_color)) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curBackgroundColor, color)) {
                    setCurrentBackgroundColor(color)
                    colorChanged()
                    updateColorTheme(getUpdatedTheme())
                    updateActionbarColor(color)
                    binding.customizationToolbar.setBackgroundColor(color)
                    binding.customizationToolbar.setTitleTextColor(color.getContrastColor())
                }
            }
        }
    }

    private fun pickPrimaryColor() {
        if (!packageName.startsWith("com.goodwy.", true) && baseConfig.appRunCount > 50) {
            finish()
            return
        }

        curPrimaryGridColorPicker = GridColorPickerDialog(
            this,
            color = curPrimaryColor,
            colorBackground = curBackgroundColor,
            true,
            showUseDefaultButton = true,
            toolbar = binding.customizationToolbar,
            title = resources.getString(R.string.primary_color)) { wasPositivePressed, color ->
            curPrimaryGridColorPicker = null
            if (wasPositivePressed) {
                if (hasColorChanged(curPrimaryColor, color)) {
                    setCurrentPrimaryColor(color)
                    colorChanged()
                    updateColorTheme(getUpdatedTheme())
                    setTheme(getThemeId(color))
                }
                updateMenuItemColors(binding.customizationToolbar.menu, getCurrentStatusBarColor())
                val navigationIcon = if (hasUnsavedChanges) NavigationIcon.Cross else NavigationIcon.Arrow
                setupToolbar(binding.customizationToolbar, navigationIcon, getCurrentStatusBarColor())
                updateTopBarColors(binding.customizationToolbar, getCurrentBackgroundColor(), curPrimaryColor)
            } else {
                //TODO actionbar color
                updateActionbarColor(curBackgroundColor)//curPrimaryColor
                setTheme(getThemeId(curPrimaryColor))
                updateMenuItemColors(binding.customizationToolbar.menu, curBackgroundColor) //curPrimaryColor
                setupToolbar(binding.customizationToolbar, NavigationIcon.Arrow, curBackgroundColor) //curPrimaryColor
                updateTopBarColors(binding.customizationToolbar, getCurrentBackgroundColor(), curPrimaryColor) //curPrimaryColor
            }
        }
    }

    private fun pickAccentColor() {
        ColorPickerDialog(this, curAccentColor, addDefaultColorButton = true, colorDefault = resources.getColor(R.color.default_accent_color), title = resources.getString(R.string.accent_color)) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curAccentColor, color)) {
                    curAccentColor = color
                    colorChanged()

                    /*if (isCurrentWhiteTheme() || isCurrentBlackAndWhiteTheme()) {
                        updateActionbarColor(getCurrentBackgroundColor())
                    }*/
                    updateActionbarColor(curBackgroundColor)
                }
            }
        }
    }

    private fun pickAppIconColor() {
        LineColorPickerDialog(this, curAppIconColor, false, R.array.md_app_icon_colors, getAppIconIDs()) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curAppIconColor, color)) {
                    curAppIconColor = color
                    colorChanged()
                    updateColorTheme(getUpdatedTheme())
                }
            }
        }
    }

    private fun getUpdatedTheme() = if (curSelectedThemeId == THEME_SHARED) THEME_SHARED else getCurrentThemeId()

    private fun applyToAll() {
        if (isSharedThemeInstalled()) {
            ConfirmationDialog(this, "", R.string.share_colors_success, R.string.ok, 0) {
                Intent().apply {
                    action = MyContentProvider.SHARED_THEME_ACTIVATED
                    sendBroadcast(this)
                }

                if (!predefinedThemes.containsKey(THEME_SHARED)) {
                    predefinedThemes[THEME_SHARED] = MyTheme(getString(R.string.shared), 0, 0, 0, 0)
                }
                baseConfig.wasSharedThemeEverActivated = true
                binding.applyToAllHolder.beGone()
                updateColorTheme(THEME_SHARED)
                saveChanges(false)
            }
        } else {
            PurchaseThankYouDialog(this)
        }
    }

    private fun updateLabelColors(textColor: Int) {
        arrayListOf(
            binding.customizationThemeLabel,
            binding.customizationTheme,
            binding.settingsCustomizeColorsSummary,
            binding.customizationTextColorLabel,
            binding.customizationTextCursorColorLabel,
            binding.customizationTextCursorColorDefault,
            binding.customizationBackgroundColorLabel,
            binding.customizationPrimaryColorLabel,
            binding.customizationAccentColorLabel,
            binding.customizationAppIconColorLabel
        ).forEach {
            it.setTextColor(textColor)
        }

        val primaryColor = getCurrentPrimaryColor()
        binding.applyToAll.setTextColor(primaryColor.getContrastColor())
        updateApplyToAllColors(primaryColor)
    }

    private fun getCurrentTextColor() = if (curSelectedThemeId == THEME_SYSTEM) {
        resources.getColor(R.color.you_neutral_text_color)
    } else {
        curTextColor
    }

    private fun getCurrentBackgroundColor() = if (curSelectedThemeId == THEME_SYSTEM) {
        resources.getColor(R.color.you_background_color)
    } else {
        curBackgroundColor
    }

    private fun getCurrentPrimaryColor() = if (curSelectedThemeId == THEME_SYSTEM) {
        resources.getColor(R.color.you_primary_color)
    } else {
        curPrimaryColor
    }

    private fun getCurrentAccentColor() = if (curSelectedThemeId == THEME_SYSTEM) {
        resources.getColor(R.color.you_primary_dark_color)
    } else {
        curAccentColor
    }

    private fun getCurrentStatusBarColor() = if (curSelectedThemeId == THEME_SYSTEM) {
        resources.getColor(R.color.you_status_bar_color)
    } else {
        curBackgroundColor
    }

    private fun getMaterialYouString() = "${getString(R.string.system_default)} (${getString(R.string.material_you)})"

    private fun setupPurchaseThankYou() {
        binding.settingsPurchaseThankYou.setTextColor(getProperTextColor())
        binding.aboutAppVersion.setTextColor(getProperTextColor())
        binding.settingsPurchaseThankYouHolder.beGoneIf(isProVersion())
        binding.settingsPurchaseThankYouHolder.setOnClickListener {
            launchPurchase()
        }
        binding.moreButton.setOnClickListener {
            launchPurchase()
        }
        val appDrawable = resources.getColoredDrawableWithColor(this, R.drawable.ic_plus_support, getProperPrimaryColor())
        binding.purchaseLogo.setImageDrawable(appDrawable)
        val drawable = resources.getColoredDrawableWithColor(this, R.drawable.button_gray_bg, getProperPrimaryColor())
        binding.moreButton.background = drawable
        binding.moreButton.setTextColor(getProperBackgroundColor())
        binding.moreButton.setPadding(2,2,2,2)
    }

    private fun launchPurchase() {
        startPurchaseActivity(
            R.string.app_name_g,
            getLicensingKey(),
            getProductIdList(), getProductIdListRu(),
            getSubscriptionIdList(), getSubscriptionIdListRu(),
            getSubscriptionYearIdList(), getSubscriptionYearIdListRu(),
            playStoreInstalled = playStoreInstalled(),
            ruStoreInstalled = ruStoreInstalled())
    }

    private fun shakePurchase() {
        RxAnimation.from(binding.settingsPurchaseThankYouHolder)
            .shake()
            .subscribe()
    }

    private fun setupTopAppBarColored() {
        binding.apply {
            updateTextColors(settingsTopAppBarColoredHolder)
            settingsTopAppBarColored.isChecked = baseConfig.topAppBarColored
            settingsTopAppBarColoredHolder.setOnClickListener {
                settingsTopAppBarColored.toggle()
                baseConfig.topAppBarColored = settingsTopAppBarColored.isChecked
                updateTopBarColors(binding.customizationToolbar, getCurrentBackgroundColor(), getCurrentPrimaryColor())
            }
        }
    }

    private fun setupTopAppBarColorIcon() {
        binding.apply {
            updateTextColors(settingsTopAppBarColorIconHolder)
            settingsTopAppBarColorIcon.isChecked = baseConfig.topAppBarColorIcon
            settingsTopAppBarColorIconHolder.setOnClickListener {
                settingsTopAppBarColorIcon.toggle()
                baseConfig.topAppBarColorIcon = settingsTopAppBarColorIcon.isChecked
                updateTopBarColors(binding.customizationToolbar, getCurrentBackgroundColor(), getCurrentPrimaryColor())
            }
        }
    }

    private fun setupTopAppBarColorTitle() {
        binding.apply {
            updateTextColors(settingsTopAppBarColorTitleHolder)
            settingsTopAppBarColorTitle.isChecked = baseConfig.topAppBarColorTitle
            settingsTopAppBarColorTitleHolder.setOnClickListener {
                settingsTopAppBarColorTitle.toggle()
                baseConfig.topAppBarColorTitle = settingsTopAppBarColorTitle.isChecked
                updateTopBarColors(binding.customizationToolbar, getCurrentBackgroundColor(), getCurrentPrimaryColor())
            }
        }
    }

    private fun setupUseAccentColor() {
        binding.apply {
            customizationUseAccentColorHolder.beVisibleIf(getShowAccentColor())
            updateTextColors(customizationUseAccentColorHolder)
            customizationUseAccentColor.isChecked = baseConfig.isUsingAccentColor
            customizationUseAccentColorHolder.setOnClickListener {
                customizationUseAccentColor.toggle()
                baseConfig.isUsingAccentColor = customizationUseAccentColor.isChecked
                updateAutoThemeFields()
            }
        }
    }
}

