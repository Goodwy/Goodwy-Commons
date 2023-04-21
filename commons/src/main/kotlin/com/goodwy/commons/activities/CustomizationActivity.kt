package com.goodwy.commons.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.behaviorule.arturdumchev.library.pixels
import com.goodwy.commons.R
import com.goodwy.commons.dialogs.*
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.*
import com.goodwy.commons.models.MyTheme
import com.goodwy.commons.models.RadioItem
import com.goodwy.commons.models.SharedTheme
import com.goodwy.commons.views.MyTextView
import com.mikhaellopez.rxanimation.RxAnimation
import com.mikhaellopez.rxanimation.shake
import kotlinx.android.synthetic.main.activity_customization.*

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

    private fun isProVersion() = intent.getBooleanExtra(IS_PRO_VERSION, false)
    private fun getLicensingKey() = intent.getStringExtra(GOOGLE_PLAY_LICENSING_KEY) ?: ""
    private fun getProductIdX1() = intent.getStringExtra(PRODUCT_ID_X1) ?: ""
    private fun getProductIdX2() = intent.getStringExtra(PRODUCT_ID_X2) ?: ""
    private fun getProductIdX3() = intent.getStringExtra(PRODUCT_ID_X3) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customization)

        setupOptionsMenu()
        refreshMenuItems()

        updateMaterialActivityViews(customization_coordinator, customization_holder, useTransparentNavigation = false, useTopSearchMenu = false)
        setupMaterialScrollListener(customization_nested_scrollview, customization_toolbar)

        // TODO TRANSPARENT Navigation Bar
        setWindowTransparency(true) { _, bottomNavigationBarSize, leftNavigationBarSize, rightNavigationBarSize ->
            customization_coordinator.setPadding(leftNavigationBarSize, 0, rightNavigationBarSize, 0)
            updateNavigationBarColor(getProperBackgroundColor())
            customization_holder.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                setMargins(0, 0, 0, bottomNavigationBarSize + pixels(R.dimen.activity_margin).toInt())
            }
        }

        isThankYou = packageName.removeSuffix(".debug") == "com.goodwy.thankyou"
        initColorVariables()

        //TODO HIDE
        apply_to_all_holder.beGone()
        customization_app_icon_color_holder.beGone()

        if (isThankYouInstalled()) {
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
                        //apply_to_all_holder.beVisibleIf(
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

        setupPurchaseThankYou()

        //supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_cross_vector)
        val textColor = if (baseConfig.isUsingSystemTheme) {
            getProperTextColor()
        } else {
            baseConfig.textColor
        }

        updateLabelColors(textColor)
        originalAppIconColor = baseConfig.appIconColor

        if (resources.getBoolean(R.bool.hide_google_relations) && !isThankYou) {
            apply_to_all_holder.beGone()
        }
    }

    override fun onResume() {
        super.onResume()
        setTheme(getThemeId(getCurrentPrimaryColor()))

        if (!baseConfig.isUsingSystemTheme) {
            updateBackgroundColor(getCurrentBackgroundColor())
            updateActionbarColor(getCurrentStatusBarColor()) //TODO actionbar color
        }

       /* curPrimaryLineColorPicker?.getSpecificColor()?.apply {
            updateActionbarColor(this)
            setTheme(getThemeId(this))
        }*/
        curPrimaryGridColorPicker?.getSpecificColor()?.apply {
           // updateActionbarColor(this)
            setTheme(getThemeId(this))
        }

        setupToolbar(customization_toolbar, NavigationIcon.Arrow)
        updateHoldersColor()
    }

    private fun updateHoldersColor(color: Int = getBottomNavigationBackgroundColor()) {
        arrayOf(
            theme_holder,
            colors_holder
        ).forEach {
            it.background.applyColorFilter(color)
        }
    }

    private fun refreshMenuItems() {
        customization_toolbar.menu.findItem(R.id.save).isVisible = hasUnsavedChanges
    }

    private fun setupOptionsMenu() {
        customization_toolbar.setOnMenuItemClickListener { menuItem ->
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
        customization_theme.text = getThemeText()
        updateAutoThemeFields()
        handleAccentColorLayout()
        customization_theme_holder.setOnClickListener {
            if (isProVersion()) themePickerClicked() else shakePurchase()
            // TODO HIDE
            /*if (baseConfig.wasAppIconCustomizationWarningShown) {
                themePickerClicked()
            } else {
                ConfirmationDialog(this, "", R.string.app_icon_color_warning, R.string.ok, 0) {
                    baseConfig.wasAppIconCustomizationWarningShown = true
                    themePickerClicked()
                }
            }*/

        }

        if (customization_theme.value == getMaterialYouString()) {
            apply_to_all_holder.beGone()
        }
    }

    private fun themePickerClicked() {
        val items = arrayListOf<RadioItem>()
        for ((key, value) in predefinedThemes) {
            items.add(RadioItem(key, value.label))
        }

        RadioGroupDialog(this@CustomizationActivity, items, curSelectedThemeId) {
            if (it == THEME_SHARED && !isThankYouInstalled()) {
                PurchaseThankYouDialog(this)
                return@RadioGroupDialog
            }

            updateColorTheme(it as Int, true)
            if (it != THEME_CUSTOM && it != THEME_SHARED && it != THEME_AUTO && it != THEME_SYSTEM && !baseConfig.wasCustomThemeSwitchDescriptionShown) {
                baseConfig.wasCustomThemeSwitchDescriptionShown = true
                toast(R.string.changing_color_description)
            }

            //val hideGoogleRelations = resources.getBoolean(R.bool.hide_google_relations) && !isThankYou
            //apply_to_all_holder.beVisibleIf(
            //    curSelectedThemeId != THEME_AUTO && curSelectedThemeId != THEME_SYSTEM && curSelectedThemeId != THEME_SHARED && !hideGoogleRelations
            //)
            updateMenuItemColors(customization_toolbar.menu, getCurrentStatusBarColor())
            setupToolbar(customization_toolbar, NavigationIcon.Cross, getCurrentStatusBarColor())
        }
    }

    private fun updateColorTheme(themeId: Int, useStored: Boolean = false) {
        curSelectedThemeId = themeId
        customization_theme.text = getThemeText()

        resources.apply {
            if (curSelectedThemeId == THEME_CUSTOM) {
                if (useStored) {
                    curTextColor = baseConfig.customTextColor
                    curBackgroundColor = baseConfig.customBackgroundColor
                    curPrimaryColor = baseConfig.customPrimaryColor
                    curAccentColor = baseConfig.customAccentColor
                    curAppIconColor = baseConfig.customAppIconColor
                    setTheme(getThemeId(curPrimaryColor))
                    updateMenuItemColors(customization_toolbar.menu, curBackgroundColor) //curPrimaryColor
                    setupToolbar(customization_toolbar, NavigationIcon.Cross, curBackgroundColor) //curPrimaryColor
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
                    updateMenuItemColors(customization_toolbar.menu, curBackgroundColor) //curPrimaryColor
                    setupToolbar(customization_toolbar, NavigationIcon.Cross, curBackgroundColor) //curPrimaryColor
                    updateActionbarColor(curBackgroundColor)
                }
            } else {
                val theme = predefinedThemes[curSelectedThemeId]!!
                curTextColor = getColor(theme.textColorId)
                curBackgroundColor = getColor(theme.backgroundColorId)

                if (curSelectedThemeId != THEME_AUTO && curSelectedThemeId != THEME_SYSTEM) {
                    curPrimaryColor = getColor(theme.primaryColorId)
                    curAccentColor = getColor(R.color.color_accent) // (R.color.color_primary) TODO цвет акцента при выборе темы R.color.color_primary
                    curAppIconColor = getColor(theme.appIconColorId)
                }

                setTheme(getThemeId(getCurrentPrimaryColor())) //setTheme(getThemeId(curPrimaryColor))
                colorChanged()
                updateMenuItemColors(customization_toolbar.menu, getCurrentStatusBarColor())
                setupToolbar(customization_toolbar, NavigationIcon.Cross, getCurrentStatusBarColor())
                updateActionbarColor(curBackgroundColor)
            }
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
        customization_theme_holder.apply {
            alpha = if (!isProVersion()) 0.3f else 1f
        }
        //customization_primary_color_holder.beVisibleIf(curSelectedThemeId != THEME_SYSTEM)
        customization_primary_color_holder.apply {
            if (!isProVersion()) {
                isEnabled = true
                alpha = 0.3f
            } else {
                if (curSelectedThemeId == THEME_SYSTEM) {
                    isEnabled = false
                    alpha = 0.3f
                } else {
                    isEnabled = true
                    alpha = 1f
                }
            }
        }
        arrayOf(customization_text_color_holder, customization_background_color_holder).forEach {
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
        /*val cornerRadius = getCornerRadius()
        customization_text_color.setFillWithStroke(curTextColor, curBackgroundColor, cornerRadius)
        customization_primary_color.setFillWithStroke(curPrimaryColor, curBackgroundColor, cornerRadius)
        customization_accent_color.setFillWithStroke(curAccentColor, curBackgroundColor, cornerRadius)
        customization_background_color.setFillWithStroke(curBackgroundColor, curBackgroundColor, cornerRadius)
        customization_app_icon_color.setFillWithStroke(curAppIconColor, curBackgroundColor, cornerRadius)*/
        val textColor = getCurrentTextColor()
        val backgroundColor = getCurrentBackgroundColor()
        val primaryColor = getCurrentPrimaryColor()
        customization_text_color.setFillWithStroke(textColor, backgroundColor)
        customization_primary_color.setFillWithStroke(primaryColor, backgroundColor)
        customization_accent_color.setFillWithStroke(curAccentColor, backgroundColor)
        customization_background_color.setFillWithStroke(backgroundColor, backgroundColor)
        customization_app_icon_color.setFillWithStroke(curAppIconColor, backgroundColor)
        //apply_to_all.setTextColor(primaryColor.getContrastColor())

        customization_text_color_holder.setOnClickListener { if (isProVersion()) pickTextColor() else shakePurchase() }
        customization_background_color_holder.setOnClickListener { if (isProVersion()) pickBackgroundColor() else shakePurchase() }
        customization_primary_color_holder.setOnClickListener { if (isProVersion()) pickPrimaryColor() else shakePurchase() }
        customization_accent_color_holder.setOnClickListener { pickAccentColor() }

        handleAccentColorLayout()
        apply_to_all_holder.setOnClickListener { applyToAll() }
        customization_app_icon_color_holder.setOnClickListener {
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
            apply_to_all.setBackgroundResource(R.drawable.button_background_rounded)
        } else {
            val applyBackground = resources.getDrawable(R.drawable.button_background_rounded, theme) as RippleDrawable
            (applyBackground as LayerDrawable).findDrawableByLayerId(R.id.button_background_holder).applyColorFilter(newColor)
            apply_to_all.background = applyBackground
        }
    }

    private fun handleAccentColorLayout() {
        customization_accent_color_holder.beVisibleIf(getShowAccentColor()) //(curSelectedThemeId == THEME_WHITE || isCurrentWhiteTheme() || curSelectedThemeId == THEME_BLACK_WHITE || isCurrentBlackAndWhiteTheme())
        customization_accent_color_label.text = getString(R.string.accent_color)
        /*customization_accent_color_label.text = getString(if (curSelectedThemeId == THEME_WHITE || isCurrentWhiteTheme()) {
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

    private fun pickBackgroundColor() {
        ColorPickerDialog(this, curBackgroundColor, title = resources.getString(R.string.background_color)) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curBackgroundColor, color)) {
                    setCurrentBackgroundColor(color)
                    colorChanged()
                    updateColorTheme(getUpdatedTheme())
                    updateActionbarColor(color)
                    customization_toolbar.setBackgroundColor(color)
                    customization_toolbar.setTitleTextColor(color.getContrastColor())
                }
            }
        }
    }

    private fun pickPrimaryColor() {
        if (!packageName.startsWith("com.goodwy.", true) && baseConfig.appRunCount > 50) {
            finish()
            return
        }

        curPrimaryGridColorPicker = GridColorPickerDialog(this, curPrimaryColor, true,
            showUseDefaultButton = true, toolbar = customization_toolbar, title = resources.getString(R.string.primary_color)) { wasPositivePressed, color ->
            curPrimaryGridColorPicker = null
            if (wasPositivePressed) {
                if (hasColorChanged(curPrimaryColor, color)) {
                    setCurrentPrimaryColor(color)
                    colorChanged()
                    updateColorTheme(getUpdatedTheme())
                    setTheme(getThemeId(color))
                }
                updateMenuItemColors(customization_toolbar.menu, getCurrentStatusBarColor())
                val navigationIcon = if (hasUnsavedChanges) NavigationIcon.Cross else NavigationIcon.Arrow
                setupToolbar(customization_toolbar, navigationIcon, getCurrentStatusBarColor())
                //updateMenuItemColors(menu, true, color)
            } else {
                //TODO actionbar color
                updateActionbarColor(curBackgroundColor)//curPrimaryColor
                setTheme(getThemeId(curPrimaryColor))
                updateMenuItemColors(customization_toolbar.menu, curBackgroundColor) //curPrimaryColor
                setupToolbar(customization_toolbar, NavigationIcon.Arrow, curBackgroundColor) //curPrimaryColor
                updateTopBarColors(customization_toolbar, curBackgroundColor) //curPrimaryColor
            }
        }
    }

    private fun pickAccentColor() {
        ColorPickerDialog(this, curAccentColor, showUseDefaultButton = true, colorDefault = resources.getColor(R.color.default_accent_color), title = resources.getString(R.string.accent_color)) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curAccentColor, color)) {
                    curAccentColor = color
                    colorChanged()

                    /*if (isCurrentWhiteTheme() || isCurrentBlackAndWhiteTheme()) {
                        updateActionbarColor(getCurrentBackgroundColor())
                    }*/
                    updateActionbarColor(getCurrentBackgroundColor())
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
        if (isThankYouInstalled()) {
            ConfirmationDialog(this, "", R.string.share_colors_success, R.string.ok, 0) {
                Intent().apply {
                    action = MyContentProvider.SHARED_THEME_ACTIVATED
                    sendBroadcast(this)
                }

                if (!predefinedThemes.containsKey(THEME_SHARED)) {
                    predefinedThemes[THEME_SHARED] = MyTheme(getString(R.string.shared), 0, 0, 0, 0)
                }
                baseConfig.wasSharedThemeEverActivated = true
                apply_to_all_holder.beGone()
                updateColorTheme(THEME_SHARED)
                saveChanges(false)
            }
        } else {
            PurchaseThankYouDialog(this)
        }
    }

    private fun updateLabelColors(textColor: Int) {
        arrayListOf<MyTextView>(
            customization_theme_label,
            customization_theme,
            settings_customize_colors_summary,
            customization_text_color_label,
            customization_background_color_label,
            customization_primary_color_label,
            customization_accent_color_label,
            customization_app_icon_color_label
        ).forEach {
            it.setTextColor(textColor)
        }

        val primaryColor = getCurrentPrimaryColor()
        apply_to_all.setTextColor(primaryColor.getContrastColor())
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

    private fun getCurrentStatusBarColor() = if (curSelectedThemeId == THEME_SYSTEM) {
        resources.getColor(R.color.you_status_bar_color)
    } else {
        curBackgroundColor
    }

    private fun getMaterialYouString() = "${getString(R.string.system_default)} (${getString(R.string.material_you)})"

    private fun setupPurchaseThankYou() {
        settings_purchase_thank_you.setTextColor(getProperTextColor())
        about_app_version.setTextColor(getProperTextColor())
        settings_purchase_thank_you_holder.beGoneIf(isProVersion())
        settings_purchase_thank_you_holder.setOnClickListener {
            launchPurchase()
        }
        moreButton.setOnClickListener {
            launchPurchase()
        }
        val appDrawable = resources.getColoredDrawableWithColor(R.drawable.ic_plus_support, getProperPrimaryColor())
        purchase_logo.setImageDrawable(appDrawable)
        val drawable = resources.getColoredDrawableWithColor(R.drawable.button_gray_bg, getProperPrimaryColor())
        moreButton.background = drawable
        moreButton.setTextColor(getProperBackgroundColor())
        moreButton.setPadding(2,2,2,2)
    }

    private fun launchPurchase() {
        startPurchaseActivity(R.string.app_name_g, getLicensingKey(), getProductIdX1(), getProductIdX2(), getProductIdX3())
    }

    private fun shakePurchase() {
        RxAnimation.from(settings_purchase_thank_you_holder)
            .shake()
            .subscribe()
    }
}

