package com.goodwy.commons.activities

import android.content.ContentValues
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.goodwy.commons.R
import com.goodwy.commons.databinding.ActivityCustomizationBinding
import com.goodwy.commons.dialogs.*
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.*
import com.goodwy.commons.helpers.MyContentProvider.COL_ACCENT_COLOR
import com.goodwy.commons.helpers.MyContentProvider.COL_APP_ICON_COLOR
import com.goodwy.commons.helpers.MyContentProvider.COL_BACKGROUND_COLOR
import com.goodwy.commons.helpers.MyContentProvider.COL_PRIMARY_COLOR
import com.goodwy.commons.helpers.MyContentProvider.COL_TEXT_COLOR
import com.goodwy.commons.helpers.MyContentProvider.COL_THEME_TYPE
import com.goodwy.commons.helpers.MyContentProvider.GLOBAL_THEME_AUTO
import com.goodwy.commons.helpers.MyContentProvider.GLOBAL_THEME_CUSTOM
import com.goodwy.commons.helpers.MyContentProvider.GLOBAL_THEME_DISABLED
import com.goodwy.commons.helpers.MyContentProvider.GLOBAL_THEME_SYSTEM
import com.goodwy.commons.helpers.NavigationIcon
import com.goodwy.commons.helpers.SAVE_DISCARD_PROMPT_INTERVAL
import com.goodwy.commons.helpers.isSPlus
import com.goodwy.commons.models.GlobalConfig
import com.goodwy.commons.models.MyTheme
import com.goodwy.commons.models.RadioItem
import com.goodwy.commons.models.isGlobalThemingEnabled
import com.goodwy.strings.R as stringsR
import com.google.android.material.snackbar.Snackbar
import com.mikhaellopez.rxanimation.RxAnimation
import com.mikhaellopez.rxanimation.shake
import kotlin.math.abs

class CustomizationActivity : BaseSimpleActivity() {
    companion object {
        private const val THEME_LIGHT = 0
        private const val THEME_DARK = 1
        private const val THEME_BLACK = 2
        private const val THEME_GRAY = 3
        private const val THEME_CUSTOM = 4
        private const val THEME_SYSTEM = 5    // Material You
        private const val THEME_AUTO = 6
    }

    private var curTextColor = 0
    private var curBackgroundColor = 0
    private var curPrimaryColor = 0
    private var curAccentColor = 0
    private var curAppIconColor = 0
    private var curSelectedThemeId = 0
    private var curTopAppBarColorIcon = false
    private var curTopAppBarColorTitle = false
    private var curIsUsingAccentColor = false
    private var curTextCursorColor = 0
    private var originalAppIconColor = 0
    private var lastSavePromptTS = 0L
    private var hasUnsavedChanges = false
    private val predefinedThemes = LinkedHashMap<Int, MyTheme>()
    private var curPrimaryGridColorPicker: GridColorPickerDialog? = null
    private var globalConfig: GlobalConfig? = null

    private fun getShowAccentColor() = intent.getBooleanExtra(SHOW_ACCENT_COLOR, false)
    private fun getShowAppIconColor() = intent.getBooleanExtra(SHOW_APP_ICON_COLOR, false)

    private fun isProVersion() = intent.getBooleanExtra(IS_COLLECTION, false) || isPro()
    private fun getProductIdList() = intent.getStringArrayListExtra(PRODUCT_ID_LIST) ?: arrayListOf("", "", "")
    private fun getProductIdListRu() = intent.getStringArrayListExtra(PRODUCT_ID_LIST_RU) ?: arrayListOf("", "", "")
    private fun getSubscriptionIdList() = intent.getStringArrayListExtra(SUBSCRIPTION_ID_LIST) ?: arrayListOf("", "", "")
    private fun getSubscriptionIdListRu() = intent.getStringArrayListExtra(SUBSCRIPTION_ID_LIST_RU) ?: arrayListOf("", "", "")
    private fun getSubscriptionYearIdList() = intent.getStringArrayListExtra(SUBSCRIPTION_YEAR_ID_LIST) ?: arrayListOf("", "", "")
    private fun getSubscriptionYearIdListRu() = intent.getStringArrayListExtra(SUBSCRIPTION_YEAR_ID_LIST_RU) ?: arrayListOf("", "", "")

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun getRepositoryName() = null

    private val binding by viewBinding(ActivityCustomizationBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupOptionsMenu()
        refreshMenuItems()

        setupEdgeToEdge(
            padTopSystem = listOf(binding.appBar),
            padBottomSystem = listOf(binding.customizationHolder)
        )
        setupMaterialScrollListener(
            scrollingView = binding.customizationNestedScrollview,
            topAppBar = binding.appBar
        )

        //TODO HIDE
        binding.applyToAllHolder.beGone()
        binding.customizationAppIconColorHolder.beVisibleIf(getShowAppIconColor())

        initColorVariables()
        if (canAccessGlobalConfig()) {
            withGlobalConfig {
                globalConfig = it
                baseConfig.isGlobalThemeEnabled = it.isGlobalThemingEnabled()
                runOnUiThread {
                    setupThemes()
                    showOrHideThankYouFeatures()
                }
            }
        } else {
            setupThemes()
            baseConfig.isGlobalThemeEnabled = false
        }

        originalAppIconColor = baseConfig.appIconColor
        updateLabelColors()
    }

    override fun onResume() {
        super.onResume()
        setTheme(getThemeId())

        if (!isDynamicTheme()) {
            updateBackgroundColor(getCurrentBackgroundColor())
        }

        curPrimaryGridColorPicker?.getSpecificColor()?.apply {
            setTheme(getThemeId())
        }

        setupTopAppBar(
            topAppBar = binding.appBar,
            navigationIcon = NavigationIcon.Arrow,
//            topBarColor = getColoredMaterialStatusBarColor()
        )
        updateApplyToAllColors()
        updateHoldersColor()

        setupPurchaseThankYou()
        updateAutoThemeFields()
        setupTopAppBarColorIcon()
        setupTopAppBarColorTitle()
        setupUseAccentColor()
    }

    private fun updateHoldersColor() {
        val color = getCurrentSurfaceColor()
        arrayOf(
            binding.themeHolder,
            binding.primaryColorsHolder,
            binding.colorsHolder
        ).forEach {
            it.setCardBackgroundColor(color)
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

    override fun onBackPressedCompat(): Boolean {
        return if (hasUnsavedChanges && System.currentTimeMillis() - lastSavePromptTS > SAVE_DISCARD_PROMPT_INTERVAL) {
            promptSaveDiscard()
            true
        } else {
            false
        }
    }

    private fun setupThemes() {
        predefinedThemes.apply {
            if (isSPlus()) put(
                THEME_SYSTEM,
                getSystemThemeColors()
            )
            put(
                THEME_AUTO,
                getAutoThemeColors()
            )
            put(
                THEME_LIGHT,
                MyTheme(
                    labelId = R.string.light_theme,
                    textColorId = R.color.theme_light_text_color,
                    backgroundColorId = R.color.theme_light_background_color,
                    primaryColorId = R.color.color_primary,
                    appIconColorId = curAppIconColor
                )
            )
            put(
                THEME_GRAY,
                MyTheme(
                    labelId = stringsR.string.gray_theme,
                    textColorId = R.color.theme_gray_text_color,
                    backgroundColorId = R.color.theme_gray_background_color,
                    primaryColorId = R.color.color_primary,
                    appIconColorId = curAppIconColor
                )
            )
            put(
                THEME_DARK,
                MyTheme(
                    labelId = R.string.dark_theme,
                    textColorId = R.color.theme_dark_text_color,
                    backgroundColorId = R.color.theme_dark_background_color,
                    primaryColorId = R.color.color_primary,
                    appIconColorId = curAppIconColor
                )
            )
            put(
                THEME_BLACK,
                MyTheme(
                    labelId = stringsR.string.black,
                    textColorId = R.color.theme_black_text_color,
                    backgroundColorId = R.color.theme_black_background_color,
                    primaryColorId = R.color.color_primary,
                    appIconColorId = curAppIconColor
                )
            )
//            put(THEME_CUSTOM, MyTheme(R.string.custom, 0, 0, 0, 0))
        }
        setupThemePicker()
        setupColorsPickers()
    }

    private fun setupThemePicker() {
        curSelectedThemeId = getCurrentThemeId()
        binding.customizationTheme.text = getThemeText()
        updateAutoThemeFields()
        handleAccentColorLayout()

        binding.customizationThemeDescription.setColors(
            getCurrentTextColor(),
            getCurrentPrimaryColor(),
            getCurrentBackgroundColor()
        )
        binding.customizationThemeHolder.setOnClickListener {
            if (isProVersion()) themePickerClicked()
            else {
                shakePurchase()
                RxAnimation.from(binding.themeHolder)
                    .shake(shakeTranslation = 2f)
                    .subscribe()

                showSnackbar(binding.root)
            }
        }
    }

    private fun themePickerClicked() {
        val items = arrayListOf<RadioItem>()
        for ((key, value) in predefinedThemes) {
            items.add(RadioItem(key, getString(value.labelId)))
        }

        RadioGroupDialog(this@CustomizationActivity, items, curSelectedThemeId, R.string.theme) {
            updateColorTheme(it as Int, true)
            if (it != THEME_CUSTOM && it != THEME_SYSTEM && !baseConfig.wasCustomThemeSwitchDescriptionShown) {
                baseConfig.wasCustomThemeSwitchDescriptionShown = true
                toast(R.string.changing_color_description)
            }

            updateTopBarColors()
        }
    }

    private fun updateColorTheme(themeId: Int, useStored: Boolean = false) {
        curSelectedThemeId = themeId
        binding.customizationTheme.text = getThemeText()

        if (curSelectedThemeId == THEME_CUSTOM) {
            if (useStored) {
                curTextColor = baseConfig.customTextColor
                curBackgroundColor = baseConfig.customBackgroundColor
                curPrimaryColor = baseConfig.customPrimaryColor
                curAccentColor = baseConfig.customAccentColor
                curAppIconColor = baseConfig.customAppIconColor

                curTopAppBarColorIcon = baseConfig.topAppBarColorIcon
                curTopAppBarColorTitle = baseConfig.topAppBarColorTitle
                curIsUsingAccentColor = binding.customizationUseAccentColor.isChecked
                curTextCursorColor = baseConfig.textCursorColor

                setTheme(getThemeId())
            } else {
                baseConfig.customPrimaryColor = curPrimaryColor
                baseConfig.customAccentColor = curAccentColor
                baseConfig.customBackgroundColor = curBackgroundColor
                baseConfig.customTextColor = curTextColor
                baseConfig.customAppIconColor = curAppIconColor

                baseConfig.topAppBarColorIcon = curTopAppBarColorIcon
                baseConfig.topAppBarColorTitle = curTopAppBarColorTitle
                baseConfig.isUsingAccentColor = curIsUsingAccentColor
                baseConfig.textCursorColor = curTextCursorColor
            }
        } else {
            val theme = predefinedThemes[curSelectedThemeId]!!
            curTextColor = getColor(theme.textColorId)
            curBackgroundColor = getColor(theme.backgroundColorId)
            curTopAppBarColorIcon = baseConfig.topAppBarColorIcon
            curTopAppBarColorTitle = baseConfig.topAppBarColorTitle
            curIsUsingAccentColor = binding.customizationUseAccentColor.isChecked
            curTextCursorColor = baseConfig.textCursorColor

//            if (curSelectedThemeId != THEME_SYSTEM && curSelectedThemeId != THEME_AUTO) {
//                curPrimaryColor = getColor(theme.primaryColorId)
//                curAccentColor = getColor(R.color.color_accent)
//            } else {
//                curPrimaryColor = getCurrentPrimaryColor()
//            }
            curPrimaryColor = getCurrentPrimaryColor()

            setTheme(getThemeId())
            colorChanged()
        }
        binding.settingsTopAppBarColorIcon.setColors(getCurrentTextColor(), getCurrentAccentOrPrimaryColor(), getCurrentBackgroundColor())
        binding.settingsTopAppBarColorTitle.setColors(getCurrentTextColor(), getCurrentAccentOrPrimaryColor(), getCurrentBackgroundColor())
        binding.customizationUseAccentColor.setColors(getCurrentTextColor(), getCurrentAccentOrPrimaryColor(), getCurrentBackgroundColor())
        binding.customizationUseAccentColorFaq.imageTintList = ColorStateList.valueOf(getCurrentTextColor())
        binding.customizationUseAccentColorLabel.setColors(getCurrentTextColor(), getCurrentPrimaryColor(), getCurrentBackgroundColor())
        binding.customizationThemeDescription.setColors(getCurrentTextColor(), getCurrentPrimaryColor(), getCurrentBackgroundColor())

        updateMenuItemColors(binding.customizationToolbar.menu, getCurrentBackgroundColor())
        setupTopAppBar(
            topAppBar = binding.appBar,
            navigationIcon = NavigationIcon.Arrow,
            topBarColor = getCurrentBackgroundColor()
        )

        updateHoldersColor()

        hasUnsavedChanges = true
        refreshMenuItems()
        updateLabelColors(getCurrentTextColor())
        updateBackgroundColor(getCurrentBackgroundColor())
        updateAutoThemeFields()
        updateApplyToAllColors()
        handleAccentColorLayout()
    }

    private fun getAutoThemeColors(): MyTheme {
        val isDarkTheme = isSystemInDarkMode()
        val textColor =
            if (isDarkTheme) R.color.theme_black_text_color else R.color.theme_light_text_color
        val backgroundColor =
            if (isDarkTheme) R.color.theme_black_background_color else R.color.theme_light_background_color
        return MyTheme(
            labelId = R.string.auto_light_dark_theme,
            textColorId = textColor,
            backgroundColorId = backgroundColor,
            primaryColorId = R.color.color_primary,
            appIconColorId = curAppIconColor
        )
    }

    // doesn't really matter what colors we use here, everything will be taken from the system.
    // Use the default dark theme values here.
    private fun getSystemThemeColors(): MyTheme {
        return MyTheme(
            labelId = R.string.system_default,
            textColorId = R.color.theme_black_text_color,
            backgroundColorId = R.color.theme_black_background_color,
            primaryColorId = R.color.color_primary,
            appIconColorId = curAppIconColor
        )
    }

    private fun getCurrentThemeId(): Int {
        if (
            (baseConfig.isSystemThemeEnabled && !hasUnsavedChanges)
            || curSelectedThemeId == THEME_SYSTEM
        ) {
            return THEME_SYSTEM
        } else if (
            (baseConfig.isAutoThemeEnabled && !hasUnsavedChanges)
            || curSelectedThemeId == THEME_AUTO
        ) {
            return THEME_AUTO
        }

        var themeId = THEME_CUSTOM
        resources.apply {
            for ((key, value) in predefinedThemes
                .filter { it.key != THEME_CUSTOM && it.key != THEME_SYSTEM && it.key != THEME_AUTO }) {
                if (curTextColor == getColor(value.textColorId) &&
                    curBackgroundColor == getColor(value.backgroundColorId) &&
//                    curPrimaryColor == getColor(value.primaryColorId) &&
                    curAppIconColor == value.appIconColorId
                ) {
                    themeId = key
                }
            }
        }

        return themeId
    }

    private fun getThemeText(): String {
        var label = R.string.custom
        for ((key, value) in predefinedThemes) {
            if (key == curSelectedThemeId) {
                label = value.labelId
            }
        }
        return getString(label)
    }

    private fun updateAutoThemeFields() {
        val isProVersion = isProVersion()
        binding.customizationThemeDescription.beVisibleIf(curSelectedThemeId == THEME_SYSTEM)
        binding.customizationThemeHolder.apply {
            alpha = if (!isProVersion) 0.3f else 1f
        }

        arrayOf(binding.customizationPrimaryColorHolder, binding.customizationTextCursorColorHolder).forEach {
            if (!isProVersion) {
                it.isEnabled = true
                it.alpha = 0.3f
            } else {
                if (curSelectedThemeId != THEME_SYSTEM || !isSPlus()) {
                    it.isEnabled = true
                    it.alpha = 1f
                } else {
                    it.isEnabled = false
                    it.alpha = 0.3f
                }
            }
        }

        binding.customizationAccentColorHolder.apply {
            if (!isProVersion) {
                this.isEnabled = true
                this.alpha = 0.3f
            } else {
                if (curIsUsingAccentColor) {
                    this.isEnabled = true
                    this.alpha = 1f
                } else {
                    this.isEnabled = false
                    this.alpha = 0.3f
                }
            }
        }

        binding.customizationAppIconColorHolder.apply {
            if (!isProVersion) {
                this.alpha = 0.3f
            } else {
                this.alpha = 1f
            }
        }

        arrayOf(binding.customizationTextColorHolder, binding.customizationBackgroundColorHolder).forEach {
            if (!isProVersion) {
                it.isEnabled = true
                it.alpha = 0.3f
            } else {
                if (curSelectedThemeId == THEME_SYSTEM || curSelectedThemeId == THEME_AUTO) {
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
        ConfirmationAdvancedDialog(
            activity = this,
            message = "",
            messageId = R.string.save_before_closing,
            positive = R.string.save,
            negative = R.string.discard
        ) {
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

            topAppBarColorIcon = curTopAppBarColorIcon
            topAppBarColorTitle = curTopAppBarColorTitle
            isUsingAccentColor = curIsUsingAccentColor
            textCursorColor = curTextCursorColor
        }

        if (didAppIconColorChange) {
            checkAppIconColor()
        }

        baseConfig.isGlobalThemeEnabled = binding.applyToAll.isChecked
        baseConfig.isSystemThemeEnabled = curSelectedThemeId == THEME_SYSTEM
        baseConfig.isAutoThemeEnabled = curSelectedThemeId == THEME_AUTO

        if (isPro()) {
            val globalThemeType = when {
                baseConfig.isGlobalThemeEnabled.not() -> GLOBAL_THEME_DISABLED
                baseConfig.isSystemThemeEnabled -> GLOBAL_THEME_SYSTEM
                baseConfig.isAutoThemeEnabled -> GLOBAL_THEME_AUTO
                else -> GLOBAL_THEME_CUSTOM
            }

            updateGlobalConfig(
                ContentValues().apply {
                    put(COL_THEME_TYPE, globalThemeType)
                    put(COL_TEXT_COLOR, curTextColor)
                    put(COL_BACKGROUND_COLOR, curBackgroundColor)
                    put(COL_PRIMARY_COLOR, curPrimaryColor)
                    put(COL_ACCENT_COLOR, curAccentColor)
                    put(COL_APP_ICON_COLOR, curAppIconColor)
                }
            )
        }

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
        refreshMenuItems()
        updateLabelColors(getCurrentTextColor())
        updateApplyToAllColors()
    }

    private fun initColorVariables() {
        curTextColor = baseConfig.textColor
        curBackgroundColor = baseConfig.backgroundColor
        curPrimaryColor = baseConfig.primaryColor
        curAccentColor = baseConfig.accentColor
        curAppIconColor = baseConfig.appIconColor

        curTopAppBarColorIcon = baseConfig.topAppBarColorIcon
        curTopAppBarColorTitle = baseConfig.topAppBarColorTitle
        curIsUsingAccentColor = baseConfig.isUsingAccentColor
        curTextCursorColor = baseConfig.textCursorColor
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
//        binding.customizationAppIconColor.setFillWithStroke(curAppIconColor, backgroundColor)
        binding.customizationAppIconColor.setImageDrawable(getAppIcon(curAppIconColor))
//        binding.applyToAll.setTextColor(primaryColor.getContrastColor())
        updateTextCursor(curTextCursorColor)

        binding.customizationTextColorHolder.setOnClickListener {
            if (isProVersion()) pickTextColor()
            else {
                shakePurchase()
                RxAnimation.from(it)
                    .shake(shakeTranslation = 2f)
                    .subscribe()

                showSnackbar(binding.root)
            }
        }
        binding.customizationTextCursorColorHolder.setOnClickListener {
            if (isProVersion()) pickTextCursorColor()
            else {
                shakePurchase()
                RxAnimation.from(it)
                    .shake(shakeTranslation = 2f)
                    .subscribe()

                showSnackbar(binding.root)
            }
        }
        binding.customizationBackgroundColorHolder.setOnClickListener {
            if (isProVersion()) pickBackgroundColor()
            else {
                shakePurchase()
                RxAnimation.from(it)
                    .shake(shakeTranslation = 2f)
                    .subscribe()

                showSnackbar(binding.root)
            }
        }
        binding.customizationPrimaryColorHolder.setOnClickListener {
            if (isProVersion()) pickPrimaryColor()
            else {
                shakePurchase()
                RxAnimation.from(it)
                    .shake(shakeTranslation = 2f)
                    .subscribe()

                showSnackbar(binding.root)
            }
        }
        binding.customizationAccentColorHolder.setOnClickListener {
            if (isProVersion()) pickAccentColor()
            else {
                shakePurchase()
                RxAnimation.from(it)
                    .shake(shakeTranslation = 2f)
                    .subscribe()

                showSnackbar(binding.root)
            }
        }

        binding.customizationAppIconColorHolder.setOnClickListener {
            if (baseConfig.wasAppIconCustomizationWarningShown || !isProVersion()) {
                pickAppIconColor()
            } else {
                val message = resources.getString(stringsR.string.app_icon_color_shortcuts_warning_g) + "\n\n" +
                    resources.getString(stringsR.string.app_icon_color_warning_g)
                ConfirmationDialog(
                    activity = this,
                    message = message,
                    messageId = stringsR.string.app_icon_color_warning_g,
                    positive = R.string.ok,
                    negative = 0
                ) {
                    baseConfig.wasAppIconCustomizationWarningShown = true
                    pickAppIconColor()
                }
            }
        }

        handleAccentColorLayout()
        binding.applyToAllHolder.setOnClickListener { applyToAll() }
    }

    private fun hasColorChanged(old: Int, new: Int) = abs(old - new) > 1

    private fun colorChanged() {
        hasUnsavedChanges = true
        setupColorsPickers()
        refreshMenuItems()
    }

    private fun setCurrentTextColor(color: Int) {
        curTextColor = color
        updateLabelColors(color)
        updateApplyToAllColors()
    }

    private fun setCurrentBackgroundColor(color: Int) {
        curBackgroundColor = color
        updateBackgroundColor(color)
        updateApplyToAllColors()
    }

    private fun setCurrentPrimaryColor(color: Int) {
        curPrimaryColor = color
        //updateApplyToAllColors()
    }

    private fun handleAccentColorLayout() {
        binding.customizationAccentColorHolder.beVisibleIf(getShowAccentColor())
        binding.customizationAccentColorLabel.text = getString(stringsR.string.accent_color)
    }

    private fun pickTextColor() {
        ColorPickerDialog(this, curTextColor,
            title = resources.getString(R.string.text_color)
        ) { wasPositivePressed, color, _ ->
            if (wasPositivePressed) {
                if (hasColorChanged(curTextColor, color)) {
                    setCurrentTextColor(color)
                    colorChanged()
                    updateColorTheme(getCurrentThemeId())
                }
            }
        }
    }

    private fun pickTextCursorColor() {
        ColorPickerDialog(this, curTextCursorColor,
            addDefaultColorButton = true,
            colorDefault = -3,
            title = resources.getString(stringsR.string.text_cursor_color)
        ) { wasPositivePressed, color, wasDefaultPressed ->
            if (wasPositivePressed) {
                if (hasColorChanged(curTextCursorColor, color)) {
                    curTextCursorColor = color
                    colorChanged()
                    binding.customizationTextCursorColor.setFillWithStroke(color, getCurrentBackgroundColor())
                    baseConfig.needRestart = true //without it the color of the cursor in the search menu does not change
                }
            }
            if (wasDefaultPressed) {
                val colorDefault = getCurrentPrimaryColor()
                if (hasColorChanged(curTextCursorColor, colorDefault)) {
                    curTextCursorColor = colorDefault
                    colorChanged()
                    binding.customizationTextCursorColor.setFillWithStroke(colorDefault, getCurrentBackgroundColor())
                    baseConfig.needRestart = true //without it the color of the cursor in the search menu does not change
                }
            }
        }
    }

    private fun updateTextCursor(color: Int) {
        binding.customizationTextCursorColor.setFillWithStroke(curTextCursorColor, getCurrentBackgroundColor())
        if (color == getCurrentPrimaryColor() || curSelectedThemeId == THEME_SYSTEM) {
            binding.customizationTextCursorColor.beGone()
            binding.customizationTextCursorColorDefault.beVisible()
        } else {
            binding.customizationTextCursorColorDefault.beGone()
            binding.customizationTextCursorColor.beVisible()
        }
    }

    private fun pickBackgroundColor() {
        ColorPickerDialog(this, curBackgroundColor,
            title = resources.getString(R.string.background_color)
        ) { wasPositivePressed, color, _ ->
            if (wasPositivePressed) {
                if (hasColorChanged(curBackgroundColor, color)) {
                    setCurrentBackgroundColor(color)
                    colorChanged()
                    updateColorTheme(getCurrentThemeId())
                    binding.customizationToolbar.setBackgroundColor(color)
                    binding.customizationToolbar.setTitleTextColor(color.getContrastColor())
                }
            }
        }
    }

    private fun pickPrimaryColor() {
        if (!packageName.startsWith("com.goodwy.", true) && baseConfig.appRunCount > 50 &&
            !packageName.startsWith("dev.goodwy.", true) && baseConfig.appRunCount > 50
        ) {
            finish()
            return
        }

        if (!packageName.startsWith("dev.goodwy")) {
            curPrimaryGridColorPicker = GridColorPickerDialog(
                activity = this,
                color = curPrimaryColor,
                colorBackground = curBackgroundColor,
                isPrimaryColorPicker = true,
            ) { wasPositivePressed, color ->
                curPrimaryGridColorPicker = null
                if (wasPositivePressed) {
                    if (hasColorChanged(curPrimaryColor, color)) {
                        setCurrentPrimaryColor(color)
                        colorChanged()
                        updateColorTheme(getCurrentThemeId())
                        setTheme(getThemeId())
                    }
                    val navigationIcon = if (hasUnsavedChanges) NavigationIcon.Cross else NavigationIcon.Arrow
                    setupTopAppBar(
                        topAppBar = binding.appBar,
                        navigationIcon = navigationIcon,
                        topBarColor = getCurrentBackgroundColor()
                    )
                    updateTopBarColors()
                } else {
                    setTheme(getThemeId())
                    setupTopAppBar(
                        topAppBar = binding.appBar,
                        navigationIcon = NavigationIcon.Arrow,
                        topBarColor = curBackgroundColor
                    )
                    updateTopBarColors()
                }
            }
        } else {
            ColorPickerDialog(
                activity = this,
                color = curPrimaryColor,
                addDefaultColorButton = true,
                colorDefault = resources.getColor(R.color.default_primary_color),
                title = resources.getString(R.string.primary_color)
            ) { wasPositivePressed, color, wasDefaultPressed ->
                if (wasPositivePressed || wasDefaultPressed) {
                    if (hasColorChanged(curPrimaryColor, color)) {
                        setCurrentPrimaryColor(color)
                        colorChanged()
                        updateColorTheme(getCurrentThemeId())
                        setTheme(getThemeId())
                    }
                    val navigationIcon = if (hasUnsavedChanges) NavigationIcon.Cross else NavigationIcon.Arrow
                    setupTopAppBar(
                        topAppBar = binding.appBar,
                        navigationIcon = navigationIcon,
                        topBarColor = getCurrentBackgroundColor()
                    )
                    updateTopBarColors()
                } else {
                    setTheme(getThemeId())
                    setupTopAppBar(
                        topAppBar = binding.appBar,
                        navigationIcon = NavigationIcon.Arrow,
                        topBarColor = curBackgroundColor
                    )
                    updateTopBarColors()
                }
            }
        }
    }

    private fun pickAccentColor() {
        ColorPickerDialog(
            activity = this,
            color = curAccentColor,
            addDefaultColorButton = true,
            colorDefault = resources.getColor(R.color.default_accent_color),
            title = resources.getString(stringsR.string.accent_color)
        ) { wasPositivePressed, color, wasDefaultPressed ->
            if (wasPositivePressed || wasDefaultPressed) {
                if (hasColorChanged(curAccentColor, color)) {
                    curAccentColor = color
                    colorChanged()

                    updateApplyToAllColors()
                    binding.settingsTopAppBarColorIcon.setColors(getCurrentTextColor(), getCurrentAccentOrPrimaryColor(), getCurrentBackgroundColor())
                    binding.settingsTopAppBarColorTitle.setColors(getCurrentTextColor(), getCurrentAccentOrPrimaryColor(), getCurrentBackgroundColor())
                    binding.customizationUseAccentColor.setColors(getCurrentTextColor(), getCurrentAccentOrPrimaryColor(), getCurrentBackgroundColor())
                }
            }
        }
    }

    private fun pickAppIconColor() {
        IconListDialog(
            activity = this@CustomizationActivity,
            items = getAppIconIDs(),
            checkedItemId = curAppIconColor + 1,
            defaultItemId = APP_ICON_ORIGINAL + 1,
            titleId = R.string.app_icon_color,
            descriptionId = resources.getString(stringsR.string.app_icon_color_shortcuts_warning_g) + "\n\n"
                + resources.getString(stringsR.string.app_icon_color_warning_g)
        ) { wasPositivePressed, newValue ->
            if (wasPositivePressed && isProVersion()) {
                if (curAppIconColor != newValue - 1) {
                    curAppIconColor = newValue - 1
                    colorChanged()
//                    updateColorTheme(getCurrentThemeId())
                    binding.customizationAppIconColor.setImageDrawable(getAppIcon(curAppIconColor))
                }
            } else {
                shakePurchase()
                RxAnimation.from(binding.customizationAppIconColorHolder)
                    .shake(shakeTranslation = 2f)
                    .subscribe()

                showSnackbar(binding.root)
            }
        }
    }

    private fun applyToAll() {
        when {
            canAccessGlobalConfig() && binding.applyToAll.isChecked -> {
                binding.applyToAll.isChecked = false
                updateColorTheme(getCurrentThemeId())
                saveChanges(false)
            }

            canAccessGlobalConfig() -> {
                binding.applyToAll.isChecked = true
                updateColorTheme(getCurrentThemeId())
                saveChanges(false)
                ConfirmationDialog(
                    activity = this,
                    message = "",
                    messageId = stringsR.string.global_theme_success_g,
                    positive = R.string.ok,
                    negative = 0,
                    callback = {}
                )
            }

            else -> {
                binding.applyToAll.isChecked = false
                shakePurchase()
                RxAnimation.from(binding.applyToAll)
                    .shake(shakeTranslation = 2f)
                    .subscribe()

                showSnackbar(binding.root)
            }
        }
    }

    private fun updateLabelColors(textColor: Int = getProperTextColor()) {
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
    }

    private fun updateApplyToAllColors() {
        binding.applyToAll.setColors(
            textColor = getCurrentTextColor(),
            accentColor = getCurrentAccentOrPrimaryColor(),
            backgroundColor = getCurrentBackgroundColor()
        )
    }

    private fun getCurrentTextColor() = when (binding.customizationTheme.value) {
        getMaterialYouString() -> resources.getColor(R.color.you_neutral_text_color)
        else -> curTextColor
    }

    private fun getCurrentBackgroundColor() = when (binding.customizationTheme.value) {
        getMaterialYouString() -> resources.getColor(R.color.you_background_color)
        else -> curBackgroundColor
    }

    private fun getCurrentPrimaryColor() = when (binding.customizationTheme.value) {
        getMaterialYouString() -> resources.getColor(R.color.you_primary_color)
        else -> curPrimaryColor
    }

    private fun getCurrentSurfaceColor(): Int {
        val bottomColor = when {
            binding.customizationTheme.value == getMaterialYouString() -> resources.getColor(R.color.you_surface_color, theme)
            curBackgroundColor == Color.WHITE -> resources.getColor(R.color.bottom_tabs_light_background, theme)
            curBackgroundColor == resources.getColor(R.color.theme_dark_background_color, theme) -> resources.getColor(R.color.bottom_tabs_dark_background, theme)
            curBackgroundColor == Color.BLACK -> resources.getColor(R.color.bottom_tabs_black_background, theme)
            else -> curBackgroundColor.lightenColor(8)
        }
        return bottomColor
    }

    private fun getCurrentAccentColor() = when (binding.customizationTheme.value) {
//        getMaterialYouString() -> resources.getColor(R.color.you_primary_dark_color)
        else -> curAccentColor
    }

    private fun getCurrentAccentOrPrimaryColor() = when {
        curIsUsingAccentColor -> getCurrentAccentColor()
        else -> getCurrentPrimaryColor()
    }

    private fun getMaterialYouString() = getString(R.string.system_default)

    private fun showOrHideThankYouFeatures() {
        val showThankYouFeatures = canAccessGlobalConfig()
        binding.applyToAllHolder.beVisibleIf(showThankYouFeatures)
        binding.applyToAll.isChecked = baseConfig.isGlobalThemeEnabled
        updateApplyToAllColors()
    }

    private fun setupPurchaseThankYou() {
        binding.settingsPurchaseThankYouHolder.beGoneIf(isProVersion())
        binding.settingsPurchaseThankYouHolder.onClick = { launchPurchase() }
    }

    private fun launchPurchase() {
        startPurchaseActivity(
            stringsR.string.app_name_g,
            getProductIdList(),
            getProductIdListRu(),
            getSubscriptionIdList(),
            getSubscriptionIdListRu(),
            getSubscriptionYearIdList(),
            getSubscriptionYearIdListRu(),
        )
    }

    private fun shakePurchase() {
        RxAnimation.from(binding.settingsPurchaseThankYouHolder)
            .shake()
            .subscribe()
    }

    private fun setupTopAppBarColorIcon() {
        binding.apply {
            updateTextColors(settingsTopAppBarColorIconHolder)
            settingsTopAppBarColorIcon.isChecked = baseConfig.topAppBarColorIcon
            settingsTopAppBarColorIconHolder.setOnClickListener {
                settingsTopAppBarColorIcon.toggle()
                curTopAppBarColorIcon = settingsTopAppBarColorIcon.isChecked
                colorChanged()
                updateTopBarColors()
            }
        }
    }

    private fun setupTopAppBarColorTitle() {
        binding.apply {
            updateTextColors(settingsTopAppBarColorTitleHolder)
            settingsTopAppBarColorTitle.isChecked = baseConfig.topAppBarColorTitle
            settingsTopAppBarColorTitleHolder.setOnClickListener {
                settingsTopAppBarColorTitle.toggle()
                curTopAppBarColorTitle = settingsTopAppBarColorTitle.isChecked
                colorChanged()
                updateTopBarColors()
            }
        }
    }

    private fun updateTopBarColors() {
        updateTopBarColors(
            topAppBar = binding.appBar,
            colorBackground = getCurrentBackgroundColor(),
            colorPrimary = getCurrentPrimaryColor(),
            topAppBarColorIcon = curTopAppBarColorIcon,
            topAppBarColorTitle = curTopAppBarColorTitle
        )
    }

    private fun setupUseAccentColor() {
        binding.apply {
            customizationUseAccentColorHolder.beVisibleIf(getShowAccentColor())
            updateTextColors(customizationUseAccentColorHolder)
            customizationUseAccentColor.isChecked = baseConfig.isUsingAccentColor
            customizationUseAccentColorHolder.setOnClickListener {
                customizationUseAccentColor.toggle()
                curIsUsingAccentColor = customizationUseAccentColor.isChecked
                colorChanged()
                updateAutoThemeFields()
                binding.settingsTopAppBarColorIcon.setColors(getCurrentTextColor(), getCurrentAccentOrPrimaryColor(), getCurrentBackgroundColor())
                binding.settingsTopAppBarColorTitle.setColors(getCurrentTextColor(), getCurrentAccentOrPrimaryColor(), getCurrentBackgroundColor())
                binding.customizationUseAccentColor.setColors(getCurrentTextColor(), getCurrentAccentOrPrimaryColor(), getCurrentBackgroundColor())
            }
            customizationUseAccentColorFaq.imageTintList = ColorStateList.valueOf(getProperTextColor())
            customizationUseAccentColorFaq.setOnClickListener {
                ConfirmationDialog(
                    activity = this@CustomizationActivity,
                    messageId = stringsR.string.use_accent_color_summary,
                    positive = R.string.ok,
                    negative = 0
                ) {}
            }
        }
    }

    private fun showSnackbar(view: View) {
        view.performHapticFeedback()

        val snackbar = Snackbar.make(view, stringsR.string.support_project_to_unlock, Snackbar.LENGTH_SHORT)
            .setAction(R.string.support) {
                launchPurchase()
            }

        val bgDrawable = ResourcesCompat.getDrawable(view.resources, R.drawable.button_background_16dp, null)
        snackbar.view.background = bgDrawable
        val properBackgroundColor = getProperBackgroundColor()
        val backgroundColor =
            if (properBackgroundColor == Color.BLACK) getSurfaceColor().lightenColor(6)
            else getSurfaceColor().darkenColor(6)
        snackbar.setBackgroundTint(backgroundColor)
        snackbar.setTextColor(getProperTextColor())
        snackbar.setActionTextColor(getProperPrimaryColor())
        snackbar.show()
    }
}

