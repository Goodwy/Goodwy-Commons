package com.goodwy.commons.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Environment
import android.text.format.DateFormat
import androidx.core.content.ContextCompat
import com.goodwy.commons.R
import com.goodwy.commons.extensions.getInternalStoragePath
import com.goodwy.commons.extensions.getSDCardPath
import com.goodwy.commons.extensions.getSharedPrefs
import com.goodwy.commons.extensions.sharedPreferencesCallback
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.LinkedList
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlin.reflect.KProperty0
import androidx.core.content.edit

open class BaseConfig(val context: Context) {
//    protected val prefs = context.getSharedPrefs()
    protected val prefs: SharedPreferences by lazy {
        try {
            context.getSharedPrefs()
        } catch (_: IllegalStateException) {
            // Return SharedPreferences in read-only mode or use the default values
            // In some cases, services fail to start and an error occurs:
            // IllegalStateException: SharedPreferences in credential encrypted storage are not available until after user (id 0) is unlocked
            context.getSharedPreferences("temp_prefs", Context.MODE_PRIVATE)
        }
    }

    companion object {
        fun newInstance(context: Context) = BaseConfig(context)
    }

    var appRunCount: Int
        get() = prefs.getInt(APP_RUN_COUNT, 0)
        set(appRunCount) = prefs.edit { putInt(APP_RUN_COUNT, appRunCount) }

    var lastVersion: Int
        get() = prefs.getInt(LAST_VERSION, 0)
        set(lastVersion) = prefs.edit { putInt(LAST_VERSION, lastVersion) }

    var primaryAndroidDataTreeUri: String
        get() = prefs.getString(PRIMARY_ANDROID_DATA_TREE_URI, "")!!
        set(uri) = prefs.edit { putString(PRIMARY_ANDROID_DATA_TREE_URI, uri) }

    var sdAndroidDataTreeUri: String
        get() = prefs.getString(SD_ANDROID_DATA_TREE_URI, "")!!
        set(uri) = prefs.edit { putString(SD_ANDROID_DATA_TREE_URI, uri) }

    var otgAndroidDataTreeUri: String
        get() = prefs.getString(OTG_ANDROID_DATA_TREE_URI, "")!!
        set(uri) = prefs.edit { putString(OTG_ANDROID_DATA_TREE_URI, uri) }

    var primaryAndroidObbTreeUri: String
        get() = prefs.getString(PRIMARY_ANDROID_OBB_TREE_URI, "")!!
        set(uri) = prefs.edit { putString(PRIMARY_ANDROID_OBB_TREE_URI, uri) }

    var sdAndroidObbTreeUri: String
        get() = prefs.getString(SD_ANDROID_OBB_TREE_URI, "")!!
        set(uri) = prefs.edit { putString(SD_ANDROID_OBB_TREE_URI, uri) }

    var otgAndroidObbTreeUri: String
        get() = prefs.getString(OTG_ANDROID_OBB_TREE_URI, "")!!
        set(uri) = prefs.edit { putString(OTG_ANDROID_OBB_TREE_URI, uri) }

    var sdTreeUri: String
        get() = prefs.getString(SD_TREE_URI, "")!!
        set(uri) = prefs.edit { putString(SD_TREE_URI, uri) }

    var OTGTreeUri: String
        get() = prefs.getString(OTG_TREE_URI, "")!!
        set(OTGTreeUri) = prefs.edit { putString(OTG_TREE_URI, OTGTreeUri) }

    var OTGPartition: String
        get() = prefs.getString(OTG_PARTITION, "")!!
        set(OTGPartition) = prefs.edit { putString(OTG_PARTITION, OTGPartition) }

    var OTGPath: String
        get() = prefs.getString(OTG_REAL_PATH, "")!!
        set(OTGPath) = prefs.edit { putString(OTG_REAL_PATH, OTGPath) }

    var sdCardPath: String
        get() = prefs.getString(SD_CARD_PATH, getDefaultSDCardPath())!!
        set(sdCardPath) = prefs.edit { putString(SD_CARD_PATH, sdCardPath) }

    private fun getDefaultSDCardPath() = if (prefs.contains(SD_CARD_PATH)) "" else context.getSDCardPath()

    var internalStoragePath: String
        get() = prefs.getString(INTERNAL_STORAGE_PATH, getDefaultInternalPath())!!
        set(internalStoragePath) = prefs.edit { putString(INTERNAL_STORAGE_PATH, internalStoragePath) }

    private fun getDefaultInternalPath() = if (prefs.contains(INTERNAL_STORAGE_PATH)) "" else getInternalStoragePath()

    var textColor: Int
        get() = prefs.getInt(TEXT_COLOR, ContextCompat.getColor(context, R.color.default_text_color))
        set(textColor) = prefs.edit { putInt(TEXT_COLOR, textColor) }

    var backgroundColor: Int
        get() = prefs.getInt(BACKGROUND_COLOR, ContextCompat.getColor(context, R.color.default_background_color))
        set(backgroundColor) = prefs.edit { putInt(BACKGROUND_COLOR, backgroundColor) }

    var primaryColor: Int
        get() = prefs.getInt(PRIMARY_COLOR, ContextCompat.getColor(context, R.color.default_primary_color))
        set(primaryColor) = prefs.edit { putInt(PRIMARY_COLOR, primaryColor) }

    var accentColor: Int
        get() = prefs.getInt(ACCENT_COLOR, ContextCompat.getColor(context, R.color.default_accent_color))
        set(accentColor) = prefs.edit { putInt(ACCENT_COLOR, accentColor) }

    var lastHandledShortcutColor: Int
        get() = prefs.getInt(LAST_HANDLED_SHORTCUT_COLOR, 1)
        set(lastHandledShortcutColor) = prefs.edit { putInt(LAST_HANDLED_SHORTCUT_COLOR, lastHandledShortcutColor) }

    var appIconColor: Int
        get() = prefs.getInt(APP_ICON_COLOR, APP_ICON_ORIGINAL) // TODO APP ICON DEFAULT CUR
        set(appIconColor) {
            isUsingModifiedAppIcon = appIconColor != APP_ICON_ORIGINAL
            prefs.edit { putInt(APP_ICON_COLOR, appIconColor) }
        }

    var lastIconColor: Int
        get() = prefs.getInt(LAST_ICON_COLOR, ContextCompat.getColor(context, R.color.color_primary))
        set(lastIconColor) = prefs.edit { putInt(LAST_ICON_COLOR, lastIconColor) }

    var customTextColor: Int
        get() = prefs.getInt(CUSTOM_TEXT_COLOR, textColor)
        set(customTextColor) = prefs.edit { putInt(CUSTOM_TEXT_COLOR, customTextColor) }

    var customBackgroundColor: Int
        get() = prefs.getInt(CUSTOM_BACKGROUND_COLOR, backgroundColor)
        set(customBackgroundColor) = prefs.edit { putInt(CUSTOM_BACKGROUND_COLOR, customBackgroundColor) }

    var customPrimaryColor: Int
        get() = prefs.getInt(CUSTOM_PRIMARY_COLOR, primaryColor)
        set(customPrimaryColor) = prefs.edit { putInt(CUSTOM_PRIMARY_COLOR, customPrimaryColor) }

    var customAccentColor: Int
        get() = prefs.getInt(CUSTOM_ACCENT_COLOR, accentColor)
        set(customAccentColor) = prefs.edit { putInt(CUSTOM_ACCENT_COLOR, customAccentColor) }

    var customAppIconColor: Int
        get() = prefs.getInt(CUSTOM_APP_ICON_COLOR, appIconColor)
        set(customAppIconColor) = prefs.edit { putInt(CUSTOM_APP_ICON_COLOR, customAppIconColor) }

    var widgetBgColor: Int
        get() = prefs.getInt(WIDGET_BG_COLOR, ContextCompat.getColor(context, R.color.default_widget_bg_color))
        set(widgetBgColor) = prefs.edit { putInt(WIDGET_BG_COLOR, widgetBgColor) }

    var widgetTextColor: Int
        get() = prefs.getInt(WIDGET_TEXT_COLOR, ContextCompat.getColor(context, R.color.default_widget_text_color))
        set(widgetTextColor) = prefs.edit { putInt(WIDGET_TEXT_COLOR, widgetTextColor) }

    var widgetLabelColor: Int
        get() = prefs.getInt(WIDGET_LABEL_COLOR, ContextCompat.getColor(context, R.color.default_widget_label_color))
        set(widgetLabelColor) = prefs.edit { putInt(WIDGET_LABEL_COLOR, widgetLabelColor) }

    // hidden folder visibility protection
    var isHiddenPasswordProtectionOn: Boolean
        get() = prefs.getBoolean(PASSWORD_PROTECTION, false)
        set(isHiddenPasswordProtectionOn) = prefs.edit { putBoolean(PASSWORD_PROTECTION, isHiddenPasswordProtectionOn) }

    var hiddenPasswordHash: String
        get() = prefs.getString(PASSWORD_HASH, "")!!
        set(hiddenPasswordHash) = prefs.edit { putString(PASSWORD_HASH, hiddenPasswordHash) }

    var hiddenProtectionType: Int
        get() = prefs.getInt(PROTECTION_TYPE, PROTECTION_PATTERN)
        set(hiddenProtectionType) = prefs.edit { putInt(PROTECTION_TYPE, hiddenProtectionType) }

    // whole app launch protection
    var isAppPasswordProtectionOn: Boolean
        get() = prefs.getBoolean(APP_PASSWORD_PROTECTION, false)
        set(isAppPasswordProtectionOn) = prefs.edit { putBoolean(APP_PASSWORD_PROTECTION, isAppPasswordProtectionOn) }

    var appPasswordHash: String
        get() = prefs.getString(APP_PASSWORD_HASH, "")!!
        set(appPasswordHash) = prefs.edit { putString(APP_PASSWORD_HASH, appPasswordHash) }

    var appProtectionType: Int
        get() = prefs.getInt(APP_PROTECTION_TYPE, PROTECTION_PATTERN)
        set(appProtectionType) = prefs.edit { putInt(APP_PROTECTION_TYPE, appProtectionType) }

    var lastUnlockTimestampMs: Long
        get() = prefs.getLong(LAST_UNLOCK_TIMESTAMP_MS, 0L)
        set(value) = prefs.edit { putLong(LAST_UNLOCK_TIMESTAMP_MS, value) }

    var unlockTimeoutDurationMs: Long
        get() = prefs.getLong(UNLOCK_TIMEOUT_DURATION_MS, DEFAULT_UNLOCK_TIMEOUT_DURATION)
        set(value) = prefs.edit { putLong(UNLOCK_TIMEOUT_DURATION_MS, value) }

    // file delete and move protection
    var isDeletePasswordProtectionOn: Boolean
        get() = prefs.getBoolean(DELETE_PASSWORD_PROTECTION, false)
        set(isDeletePasswordProtectionOn) = prefs.edit { putBoolean(DELETE_PASSWORD_PROTECTION, isDeletePasswordProtectionOn) }

    var deletePasswordHash: String
        get() = prefs.getString(DELETE_PASSWORD_HASH, "")!!
        set(deletePasswordHash) = prefs.edit { putString(DELETE_PASSWORD_HASH, deletePasswordHash) }

    var deleteProtectionType: Int
        get() = prefs.getInt(DELETE_PROTECTION_TYPE, PROTECTION_PATTERN)
        set(deleteProtectionType) = prefs.edit { putInt(DELETE_PROTECTION_TYPE, deleteProtectionType) }

    // folder locking
    fun addFolderProtection(path: String, hash: String, type: Int) {
        prefs.edit {
            putString("$PROTECTED_FOLDER_HASH$path", hash)
                .putInt("$PROTECTED_FOLDER_TYPE$path", type)
        }
    }

    fun removeFolderProtection(path: String) {
        prefs.edit {
            remove("$PROTECTED_FOLDER_HASH$path")
                .remove("$PROTECTED_FOLDER_TYPE$path")
        }
    }

    fun isFolderProtected(path: String) = getFolderProtectionType(path) != PROTECTION_NONE

    fun getFolderProtectionHash(path: String) = prefs.getString("$PROTECTED_FOLDER_HASH$path", "") ?: ""

    fun getFolderProtectionType(path: String) = prefs.getInt("$PROTECTED_FOLDER_TYPE$path", PROTECTION_NONE)

    var lastCopyPath: String
        get() = prefs.getString(LAST_COPY_PATH, "")!!
        set(lastCopyPath) = prefs.edit { putString(LAST_COPY_PATH, lastCopyPath) }

    var keepLastModified: Boolean
        get() = prefs.getBoolean(KEEP_LAST_MODIFIED, true)
        set(keepLastModified) = prefs.edit { putBoolean(KEEP_LAST_MODIFIED, keepLastModified) }

    var useEnglish: Boolean
        get() = prefs.getBoolean(USE_ENGLISH, false)
        set(useEnglish) {
            wasUseEnglishToggled = true
            prefs.edit { putBoolean(USE_ENGLISH, useEnglish) }
        }

    val useEnglishFlow = ::useEnglish.asFlowNonNull()

    var wasUseEnglishToggled: Boolean
        get() = prefs.getBoolean(WAS_USE_ENGLISH_TOGGLED, false)
        set(wasUseEnglishToggled) = prefs.edit { putBoolean(WAS_USE_ENGLISH_TOGGLED, wasUseEnglishToggled) }

    val wasUseEnglishToggledFlow = ::wasUseEnglishToggled.asFlowNonNull()

    var useIconTabs: Boolean
        get() = prefs.getBoolean(USE_ICON_TABS, false)
        set(useIconTabs) = prefs.edit { putBoolean(USE_ICON_TABS, useIconTabs) }

    var needRestart: Boolean
        get() = prefs.getBoolean(NEED_RESTART, true)
        set(needRestart) = prefs.edit { putBoolean(NEED_RESTART, needRestart) }

    var useDividers: Boolean
        get() = prefs.getBoolean(USE_DIVIDERS, false)
        set(useDividers) = prefs.edit { putBoolean(USE_DIVIDERS, useDividers) }

    var useColoredContacts: Boolean
        get() = prefs.getBoolean(USE_COLORED_CONTACTS, false)
        set(useColoredContacts) = prefs.edit { putBoolean(USE_COLORED_CONTACTS, useColoredContacts) }

    var contactColorList: Int
        get() = prefs.getInt(CONTACT_COLOR_LIST, LBC_ANDROID)
        set(contactsColorList) = prefs.edit { putInt(CONTACT_COLOR_LIST, contactsColorList) }

    var isGlobalThemeEnabled: Boolean
        get() = prefs.getBoolean(IS_GLOBAL_THEME_ENABLED, false)
        set(isGlobalThemeEnabled) = prefs.edit { putBoolean(IS_GLOBAL_THEME_ENABLED, isGlobalThemeEnabled) }

    var isSystemThemeEnabled: Boolean
        get() = prefs.getBoolean(IS_SYSTEM_THEME_ENABLED, false) //isSPlus()
        set(isSystemThemeEnabled) = prefs.edit { putBoolean(IS_SYSTEM_THEME_ENABLED, isSystemThemeEnabled) }

    var isAutoThemeEnabled: Boolean
        get() = prefs.getBoolean(IS_AUTO_THEME_ENABLED, false)
        set(isAutoThemeEnabled) = prefs.edit { putBoolean(IS_AUTO_THEME_ENABLED, isAutoThemeEnabled) }

    var wasCustomThemeSwitchDescriptionShown: Boolean
        get() = prefs.getBoolean(WAS_CUSTOM_THEME_SWITCH_DESCRIPTION_SHOWN, false)
        set(wasCustomThemeSwitchDescriptionShown) = prefs.edit {
            putBoolean(WAS_CUSTOM_THEME_SWITCH_DESCRIPTION_SHOWN, wasCustomThemeSwitchDescriptionShown)
        }

    var showInfoBubble: Boolean
        get() = prefs.getBoolean(SHOW_INFO_BUBBLE, true)
        set(showInfoBubble) = prefs.edit { putBoolean(SHOW_INFO_BUBBLE, showInfoBubble) }

    var lastConflictApplyToAll: Boolean
        get() = prefs.getBoolean(LAST_CONFLICT_APPLY_TO_ALL, true)
        set(lastConflictApplyToAll) = prefs.edit { putBoolean(LAST_CONFLICT_APPLY_TO_ALL, lastConflictApplyToAll) }

    var lastConflictResolution: Int
        get() = prefs.getInt(LAST_CONFLICT_RESOLUTION, CONFLICT_SKIP)
        set(lastConflictResolution) = prefs.edit { putInt(LAST_CONFLICT_RESOLUTION, lastConflictResolution) }

    var sorting: Int
        get() = prefs.getInt(SORT_ORDER, context.resources.getInteger(R.integer.default_sorting))
        @SuppressLint("UseKtx")
        set(sorting) = prefs.edit { putInt(SORT_ORDER, sorting) }

    fun saveCustomSorting(path: String, value: Int) {
        if (path.isEmpty()) {
            sorting = value
        } else {
            prefs.edit { putInt(SORT_FOLDER_PREFIX + path.lowercase(Locale.getDefault()), value) }
        }
    }

    fun getFolderSorting(path: String) = prefs.getInt(SORT_FOLDER_PREFIX + path.lowercase(Locale.getDefault()), sorting)

    fun removeCustomSorting(path: String) {
        prefs.edit { remove(SORT_FOLDER_PREFIX + path.lowercase(Locale.getDefault())) }
    }

    fun hasCustomSorting(path: String) = prefs.contains(SORT_FOLDER_PREFIX + path.lowercase(Locale.getDefault()))

    var hadThankYouInstalled: Boolean
        get() = prefs.getBoolean(HAD_THANK_YOU_INSTALLED, false)
        set(hadThankYouInstalled) = prefs.edit { putBoolean(HAD_THANK_YOU_INSTALLED, hadThankYouInstalled) }

    var skipDeleteConfirmation: Boolean
        get() = prefs.getBoolean(SKIP_DELETE_CONFIRMATION, false)
        set(skipDeleteConfirmation) = prefs.edit { putBoolean(SKIP_DELETE_CONFIRMATION, skipDeleteConfirmation) }

    var enablePullToRefresh: Boolean
        get() = prefs.getBoolean(ENABLE_PULL_TO_REFRESH, true)
        set(enablePullToRefresh) = prefs.edit { putBoolean(ENABLE_PULL_TO_REFRESH, enablePullToRefresh) }

    var scrollHorizontally: Boolean
        get() = prefs.getBoolean(SCROLL_HORIZONTALLY, false)
        set(scrollHorizontally) = prefs.edit { putBoolean(SCROLL_HORIZONTALLY, scrollHorizontally) }

    var preventPhoneFromSleeping: Boolean
        get() = prefs.getBoolean(PREVENT_PHONE_FROM_SLEEPING, true)
        set(preventPhoneFromSleeping) = prefs.edit { putBoolean(PREVENT_PHONE_FROM_SLEEPING, preventPhoneFromSleeping) }

    var lastUsedViewPagerPage: Int
        get() = prefs.getInt(LAST_USED_VIEW_PAGER_PAGE, context.resources.getInteger(R.integer.default_viewpager_page))
        set(lastUsedViewPagerPage) = prefs.edit { putInt(LAST_USED_VIEW_PAGER_PAGE, lastUsedViewPagerPage) }

    var use24HourFormat: Boolean
        get() = prefs.getBoolean(USE_24_HOUR_FORMAT, DateFormat.is24HourFormat(context))
        set(use24HourFormat) = prefs.edit { putBoolean(USE_24_HOUR_FORMAT, use24HourFormat) }

    val isUse24HourFormat: Flow<Boolean> = ::use24HourFormat.asFlowNonNull()

    var isSundayFirst: Boolean
        get() {
            val isSundayFirst = Calendar.getInstance(Locale.getDefault()).firstDayOfWeek == Calendar.SUNDAY
            return prefs.getBoolean(SUNDAY_FIRST, isSundayFirst)
        }
        set(sundayFirst) = prefs.edit { putBoolean(SUNDAY_FIRST, sundayFirst) }

    var wasAlarmWarningShown: Boolean
        get() = prefs.getBoolean(WAS_ALARM_WARNING_SHOWN, false)
        set(wasAlarmWarningShown) = prefs.edit { putBoolean(WAS_ALARM_WARNING_SHOWN, wasAlarmWarningShown) }

    var wasReminderWarningShown: Boolean
        get() = prefs.getBoolean(WAS_REMINDER_WARNING_SHOWN, false)
        set(wasReminderWarningShown) = prefs.edit { putBoolean(WAS_REMINDER_WARNING_SHOWN, wasReminderWarningShown) }

    var useSameSnooze: Boolean
        get() = prefs.getBoolean(USE_SAME_SNOOZE, true)
        set(useSameSnooze) = prefs.edit { putBoolean(USE_SAME_SNOOZE, useSameSnooze) }

    var snoozeTime: Int
        get() = prefs.getInt(SNOOZE_TIME, 10)
        set(snoozeDelay) = prefs.edit { putInt(SNOOZE_TIME, snoozeDelay) }

    var vibrateOnButtonPress: Boolean
        get() = prefs.getBoolean(VIBRATE_ON_BUTTON_PRESS, context.resources.getBoolean(R.bool.default_vibrate_on_press))
        set(vibrateOnButton) = prefs.edit { putBoolean(VIBRATE_ON_BUTTON_PRESS, vibrateOnButton) }

    var yourAlarmSounds: String
        get() = prefs.getString(YOUR_ALARM_SOUNDS, "")!!
        set(yourAlarmSounds) = prefs.edit { putString(YOUR_ALARM_SOUNDS, yourAlarmSounds) }

    var isUsingModifiedAppIcon: Boolean
        get() = prefs.getBoolean(IS_USING_MODIFIED_APP_ICON, false)
        set(isUsingModifiedAppIcon) = prefs.edit { putBoolean(IS_USING_MODIFIED_APP_ICON, isUsingModifiedAppIcon) }

    var appId: String
        get() = prefs.getString(APP_ID, "")!!
        set(appId) = prefs.edit { putString(APP_ID, appId) }

    var initialWidgetHeight: Int
        get() = prefs.getInt(INITIAL_WIDGET_HEIGHT, 0)
        set(initialWidgetHeight) = prefs.edit { putInt(INITIAL_WIDGET_HEIGHT, initialWidgetHeight) }

    var widgetIdToMeasure: Int
        get() = prefs.getInt(WIDGET_ID_TO_MEASURE, 0)
        set(widgetIdToMeasure) = prefs.edit { putInt(WIDGET_ID_TO_MEASURE, widgetIdToMeasure) }

    var wasOrangeIconChecked: Boolean
        get() = prefs.getBoolean(WAS_ORANGE_ICON_CHECKED, false)
        set(wasOrangeIconChecked) = prefs.edit { putBoolean(WAS_ORANGE_ICON_CHECKED, wasOrangeIconChecked) }

    var wasAppOnSDShown: Boolean
        get() = prefs.getBoolean(WAS_APP_ON_SD_SHOWN, false)
        set(wasAppOnSDShown) = prefs.edit { putBoolean(WAS_APP_ON_SD_SHOWN, wasAppOnSDShown) }

    var wasBeforeAskingShown: Boolean
        get() = prefs.getBoolean(WAS_BEFORE_ASKING_SHOWN, false)
        set(wasBeforeAskingShown) = prefs.edit { putBoolean(WAS_BEFORE_ASKING_SHOWN, wasBeforeAskingShown) }

    var wasBeforeRateShown: Boolean
        get() = prefs.getBoolean(WAS_BEFORE_RATE_SHOWN, false)
        set(wasBeforeRateShown) = prefs.edit { putBoolean(WAS_BEFORE_RATE_SHOWN, wasBeforeRateShown) }

    var wasInitialUpgradeToProShown: Boolean
        get() = prefs.getBoolean(WAS_INITIAL_UPGRADE_TO_PRO_SHOWN, false)
        set(wasInitialUpgradeToProShown) = prefs.edit { putBoolean(WAS_INITIAL_UPGRADE_TO_PRO_SHOWN, wasInitialUpgradeToProShown) }

    var wasAppIconCustomizationWarningShown: Boolean
        get() = prefs.getBoolean(WAS_APP_ICON_CUSTOMIZATION_WARNING_SHOWN, false)
        set(wasAppIconCustomizationWarningShown) = prefs.edit {
            putBoolean(WAS_APP_ICON_CUSTOMIZATION_WARNING_SHOWN, wasAppIconCustomizationWarningShown)
        }

    var appSideloadingStatus: Int
        get() = prefs.getInt(APP_SIDELOADING_STATUS, SIDELOADING_UNCHECKED)
        set(appSideloadingStatus) = prefs.edit { putInt(APP_SIDELOADING_STATUS, appSideloadingStatus)}

    var dateFormat: String
        get() = prefs.getString(DATE_FORMAT, getDefaultDateFormat())!!
        set(dateFormat) = prefs.edit { putString(DATE_FORMAT, dateFormat) }

    val isDateFormat: Flow<String> = ::dateFormat.asFlowNonNull()

    private fun getDefaultDateFormat(): String {
        val format = DateFormat.getDateFormat(context)
        val pattern = (format as SimpleDateFormat).toLocalizedPattern()
        return when (pattern.lowercase().replace(" ", "")) {
            "d.M.y" -> DATE_FORMAT_ONE
            "dd/mm/y" -> DATE_FORMAT_TWO
            "mm/dd/y" -> DATE_FORMAT_THREE
            "y-mm-dd" -> DATE_FORMAT_FOUR
            "dmmmmy" -> DATE_FORMAT_FIVE
            "mmmmdy" -> DATE_FORMAT_SIX
            "mm-dd-y" -> DATE_FORMAT_SEVEN
            "dd-mm-y" -> DATE_FORMAT_EIGHT
            "y.mm.dd" -> DATE_FORMAT_TEN
            else -> DATE_FORMAT_ONE
        }
    }

    var wasOTGHandled: Boolean
        get() = prefs.getBoolean(WAS_OTG_HANDLED, false)
        @SuppressLint("UseKtx")
        set(wasOTGHandled) = prefs.edit { putBoolean(WAS_OTG_HANDLED, wasOTGHandled) }

    var wasUpgradedFromFreeShown: Boolean
        get() = prefs.getBoolean(WAS_UPGRADED_FROM_FREE_SHOWN, false)
        set(wasUpgradedFromFreeShown) = prefs.edit { putBoolean(WAS_UPGRADED_FROM_FREE_SHOWN, wasUpgradedFromFreeShown) }

    var wasAppRated: Boolean
        get() = prefs.getBoolean(WAS_APP_RATED, false)
        set(wasAppRated) = prefs.edit { putBoolean(WAS_APP_RATED, wasAppRated) }

    var wasSortingByNumericValueAdded: Boolean
        get() = prefs.getBoolean(WAS_SORTING_BY_NUMERIC_VALUE_ADDED, false)
        set(wasSortingByNumericValueAdded) = prefs.edit { putBoolean(WAS_SORTING_BY_NUMERIC_VALUE_ADDED, wasSortingByNumericValueAdded) }

    var wasFolderLockingNoticeShown: Boolean
        get() = prefs.getBoolean(WAS_FOLDER_LOCKING_NOTICE_SHOWN, false)
        set(wasFolderLockingNoticeShown) = prefs.edit { putBoolean(WAS_FOLDER_LOCKING_NOTICE_SHOWN, wasFolderLockingNoticeShown) }

    var lastRenameUsed: Int
        get() = prefs.getInt(LAST_RENAME_USED, RENAME_SIMPLE)
        set(lastRenameUsed) = prefs.edit { putInt(LAST_RENAME_USED, lastRenameUsed) }

    var lastRenamePatternUsed: String
        get() = prefs.getString(LAST_RENAME_PATTERN_USED, "")!!
        set(lastRenamePatternUsed) = prefs.edit { putString(LAST_RENAME_PATTERN_USED, lastRenamePatternUsed) }

    var lastExportedSettingsFolder: String
        get() = prefs.getString(LAST_EXPORTED_SETTINGS_FOLDER, "")!!
        set(lastExportedSettingsFolder) = prefs.edit { putString(LAST_EXPORTED_SETTINGS_FOLDER, lastExportedSettingsFolder) }

    var lastBlockedNumbersExportPath: String
        get() = prefs.getString(LAST_BLOCKED_NUMBERS_EXPORT_PATH, "")!!
        set(lastBlockedNumbersExportPath) = prefs.edit { putString(LAST_BLOCKED_NUMBERS_EXPORT_PATH, lastBlockedNumbersExportPath) }

    var blockUnknownNumbers: Boolean
        get() = prefs.getBoolean(BLOCK_UNKNOWN_NUMBERS, false)
        set(blockUnknownNumbers) = prefs.edit { putBoolean(BLOCK_UNKNOWN_NUMBERS, blockUnknownNumbers) }

    val isBlockingUnknownNumbers: Flow<Boolean> = ::blockUnknownNumbers.asFlowNonNull()

    var blockHiddenNumbers: Boolean
        get() = prefs.getBoolean(BLOCK_HIDDEN_NUMBERS, false)
        set(blockHiddenNumbers) = prefs.edit { putBoolean(BLOCK_HIDDEN_NUMBERS, blockHiddenNumbers) }

    val isBlockingHiddenNumbers: Flow<Boolean> = ::blockHiddenNumbers.asFlowNonNull()

    var fontSize: Int
        get() = prefs.getInt(FONT_SIZE, 1) //context.resources.getInteger(R.integer.default_font_size)
        set(fontSize) = prefs.edit { putInt(FONT_SIZE, fontSize) }

    // notify the users about new SMS Messenger and Voice Recorder released
    var wasMessengerRecorderShown: Boolean
        get() = prefs.getBoolean(WAS_MESSENGER_RECORDER_SHOWN, false)
        set(wasMessengerRecorderShown) = prefs.edit { putBoolean(WAS_MESSENGER_RECORDER_SHOWN, wasMessengerRecorderShown) }

    var defaultTab: Int
        get() = prefs.getInt(DEFAULT_TAB, TAB_LAST_USED)
        set(defaultTab) = prefs.edit { putInt(DEFAULT_TAB, defaultTab) }

    var startNameWithSurname: Boolean
        get() = prefs.getBoolean(START_NAME_WITH_SURNAME, false)
        set(startNameWithSurname) = prefs.edit { putBoolean(START_NAME_WITH_SURNAME, startNameWithSurname) }

    var favorites: MutableSet<String>
        get() = prefs.getStringSet(FAVORITES, HashSet())!!
        set(favorites) = prefs.edit { remove(FAVORITES).putStringSet(FAVORITES, favorites) }

    var showCallConfirmation: Boolean
        get() = prefs.getBoolean(SHOW_CALL_CONFIRMATION, false)
        set(showCallConfirmation) = prefs.edit { putBoolean(SHOW_CALL_CONFIRMATION, showCallConfirmation) }

    // color picker last used colors
    var colorPickerRecentColors: LinkedList<Int>
        get(): LinkedList<Int> {
            val defaultList = arrayListOf(
                ContextCompat.getColor(context, R.color.md_red_700),
                ContextCompat.getColor(context, R.color.md_blue_700),
                ContextCompat.getColor(context, R.color.md_green_700),
                ContextCompat.getColor(context, R.color.md_yellow_700),
                ContextCompat.getColor(context, R.color.md_orange_700)
            )
            return LinkedList(prefs.getString(COLOR_PICKER_RECENT_COLORS, null)?.lines()?.map { it.toInt() } ?: defaultList)
        }
        set(recentColors) = prefs.edit { putString(COLOR_PICKER_RECENT_COLORS, recentColors.joinToString(separator = "\n")) }

    val colorPickerRecentColorsFlow = ::colorPickerRecentColors.asFlowNonNull()

    var ignoredContactSources: HashSet<String>
        get() = prefs.getStringSet(IGNORED_CONTACT_SOURCES, hashSetOf(".")) as HashSet
        set(ignoreContactSources) = prefs.edit { remove(IGNORED_CONTACT_SOURCES).putStringSet(IGNORED_CONTACT_SOURCES, ignoreContactSources) }

    var showContactThumbnails: Boolean
        get() = prefs.getBoolean(SHOW_CONTACT_THUMBNAILS, true)
        set(showContactThumbnails) = prefs.edit { putBoolean(SHOW_CONTACT_THUMBNAILS, showContactThumbnails) }

    var showPhoneNumbers: Boolean
        get() = prefs.getBoolean(SHOW_PHONE_NUMBERS, false)
        set(showPhoneNumbers) = prefs.edit { putBoolean(SHOW_PHONE_NUMBERS, showPhoneNumbers) }

    var formatPhoneNumbers: Boolean
        get() = prefs.getBoolean(FORMAT_PHONE_NUMBERS, true)
        set(formatPhoneNumbers) = prefs.edit { putBoolean(FORMAT_PHONE_NUMBERS, formatPhoneNumbers) }

    var showOnlyContactsWithNumbers: Boolean
        get() = prefs.getBoolean(SHOW_ONLY_CONTACTS_WITH_NUMBERS, false)
        set(showOnlyContactsWithNumbers) = prefs.edit { putBoolean(SHOW_ONLY_CONTACTS_WITH_NUMBERS, showOnlyContactsWithNumbers) }

    var lastUsedContactSource: String
        get() = prefs.getString(LAST_USED_CONTACT_SOURCE, "")!!
        set(lastUsedContactSource) = prefs.edit { putString(LAST_USED_CONTACT_SOURCE, lastUsedContactSource) }

    var showContactFields: Int
        get() = prefs.getInt(
            SHOW_CONTACT_FIELDS,
            SHOW_FIRST_NAME_FIELD or SHOW_SURNAME_FIELD or SHOW_PHONE_NUMBERS_FIELD or SHOW_IMS_FIELD or
                SHOW_MESSENGERS_ACTIONS_FIELD or SHOW_EMAILS_FIELD or SHOW_ADDRESSES_FIELD or SHOW_EVENTS_FIELD or
                SHOW_NOTES_FIELD or SHOW_GROUPS_FIELD or SHOW_CONTACT_SOURCE_FIELD or SHOW_RINGTONE_FIELD or SHOW_ORGANIZATION_FIELD
        )
        set(showContactFields) = prefs.edit { putInt(SHOW_CONTACT_FIELDS, showContactFields) }

    var showDialpadButton: Boolean
        get() = prefs.getBoolean(SHOW_DIALPAD_BUTTON, true)
        set(showDialpadButton) = prefs.edit { putBoolean(SHOW_DIALPAD_BUTTON, showDialpadButton) }

    var wasLocalAccountInitialized: Boolean
        get() = prefs.getBoolean(WAS_LOCAL_ACCOUNT_INITIALIZED, false)
        set(wasLocalAccountInitialized) = prefs.edit { putBoolean(WAS_LOCAL_ACCOUNT_INITIALIZED, wasLocalAccountInitialized) }

    var lastExportPath: String
        get() = prefs.getString(LAST_EXPORT_PATH, "")!!
        set(lastExportPath) = prefs.edit { putString(LAST_EXPORT_PATH, lastExportPath) }

    var speedDial: String
        get() = prefs.getString(SPEED_DIAL, "")!!
        set(speedDial) = prefs.edit { putString(SPEED_DIAL, speedDial) }

    var showPrivateContacts: Boolean
        get() = prefs.getBoolean(SHOW_PRIVATE_CONTACTS, true)
        set(showPrivateContacts) = prefs.edit { putBoolean(SHOW_PRIVATE_CONTACTS, showPrivateContacts) }

    var mergeDuplicateContacts: Boolean
        get() = prefs.getBoolean(MERGE_DUPLICATE_CONTACTS, true)
        set(mergeDuplicateContacts) = prefs.edit { putBoolean(MERGE_DUPLICATE_CONTACTS, mergeDuplicateContacts) }

    var favoritesContactsOrder: String
        get() = prefs.getString(FAVORITES_CONTACTS_ORDER, "")!!
        set(order) = prefs.edit { putString(FAVORITES_CONTACTS_ORDER, order) }

    var isCustomOrderSelected: Boolean
        get() = prefs.getBoolean(FAVORITES_CUSTOM_ORDER_SELECTED, false)
        set(selected) = prefs.edit { putBoolean(FAVORITES_CUSTOM_ORDER_SELECTED, selected) }

    var viewType: Int
        get() = prefs.getInt(VIEW_TYPE, VIEW_TYPE_LIST)
        set(viewType) = prefs.edit { putInt(VIEW_TYPE, viewType) }

    var contactsGridColumnCount: Int
        get() = prefs.getInt(CONTACTS_GRID_COLUMN_COUNT, getDefaultContactColumnsCount())
        set(contactsGridColumnCount) = prefs.edit { putInt(CONTACTS_GRID_COLUMN_COUNT, contactsGridColumnCount) }

    private fun getDefaultContactColumnsCount(): Int {
        val isPortrait = context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        return if (isPortrait) {
            context.resources.getInteger(R.integer.contacts_grid_columns_count_portrait)
        } else {
            context.resources.getInteger(R.integer.contacts_grid_columns_count_landscape)
        }
    }

    var autoBackup: Boolean
        get() = prefs.getBoolean(AUTO_BACKUP, false)
        set(autoBackup) = prefs.edit { putBoolean(AUTO_BACKUP, autoBackup) }

    var autoBackupFolder: String
        get() = prefs.getString(AUTO_BACKUP_FOLDER, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath)!!
        set(autoBackupFolder) = prefs.edit { putString(AUTO_BACKUP_FOLDER, autoBackupFolder) }

    var autoBackupFilename: String
        get() = prefs.getString(AUTO_BACKUP_FILENAME, "")!!
        set(autoBackupFilename) = prefs.edit { putString(AUTO_BACKUP_FILENAME, autoBackupFilename) }

    var lastAutoBackupTime: Long
        get() = prefs.getLong(LAST_AUTO_BACKUP_TIME, 0L)
        set(lastAutoBackupTime) = prefs.edit { putLong(LAST_AUTO_BACKUP_TIME, lastAutoBackupTime) }

    var passwordRetryCount: Int
        get() = prefs.getInt(PASSWORD_RETRY_COUNT, 0)
        set(passwordRetryCount) = prefs.edit { putInt(PASSWORD_RETRY_COUNT, passwordRetryCount) }

    var passwordCountdownStartMs: Long
        get() = prefs.getLong(PASSWORD_COUNTDOWN_START_MS, 0L)
        set(passwordCountdownStartMs) = prefs.edit { putLong(PASSWORD_COUNTDOWN_START_MS, passwordCountdownStartMs) }

    // Returns the first day of week, indexing follows ISO 8601: Mon=1, ..., Sun=7
    var firstDayOfWeek: Int
        get() {
            val defaultFirstDayOfWeek = Calendar.getInstance(Locale.getDefault()).firstDayOfWeek
            return prefs.getInt(FIRST_DAY_OF_WEEK, getISODayOfWeekFromJava(defaultFirstDayOfWeek))
        }
        set(firstDayOfWeek) = prefs.edit { putInt(FIRST_DAY_OF_WEEK, firstDayOfWeek) }

    // Accessibility
    var showCheckmarksOnSwitches: Boolean
        get() = prefs.getBoolean(SHOW_CHECKMARKS_ON_SWITCHES, false)
        set(showCheckmarksOnSwitches) = prefs.edit { putBoolean(SHOW_CHECKMARKS_ON_SWITCHES, showCheckmarksOnSwitches) }

    var showCheckmarksOnSwitchesFlow = ::showCheckmarksOnSwitches.asFlowNonNull()

    protected fun <T> KProperty0<T>.asFlow(emitOnCollect: Boolean = false): Flow<T?> =
        prefs.run { sharedPreferencesCallback(sendOnCollect = emitOnCollect) { this@asFlow.get() } }

    protected fun <T> KProperty0<T>.asFlowNonNull(emitOnCollect: Boolean = false): Flow<T> = asFlow(emitOnCollect).filterNotNull()

    //Goodwy
    var overflowIcon: Int
        get() = prefs.getInt(OVERFLOW_ICON, OVERFLOW_ICON_HORIZONTAL)
        set(overflowIcon) = prefs.edit { putInt(OVERFLOW_ICON, overflowIcon) }

    var screenSlideAnimation: Int
        get() = prefs.getInt(SCREEN_SLIDE_ANIMATION, 1)
        set(screenSlideAnimation) = prefs.edit { putInt(SCREEN_SLIDE_ANIMATION, screenSlideAnimation) }

    var materialDesign3: Boolean
        get() = prefs.getBoolean(MATERIAL_DESIGN3, false)
        set(materialDesign3) = prefs.edit { putBoolean(MATERIAL_DESIGN3, materialDesign3) }

    var useRelativeDate: Boolean
        get() = prefs.getBoolean(USE_RELATIVE_DATE, false)
        set(useRelativeDate) = prefs.edit { putBoolean(USE_RELATIVE_DATE, useRelativeDate) }

    var colorSimIcons: Boolean
        get() = prefs.getBoolean(COLOR_SIM_ICON, true)
        set(colorSimIcons) = prefs.edit { putBoolean(COLOR_SIM_ICON, colorSimIcons) }

    // Tab bar
    var bottomNavigationBar: Boolean
        get() = prefs.getBoolean(BOTTOM_NAVIGATION_BAR, true)
        set(bottomNavigationBar) = prefs.edit { putBoolean(BOTTOM_NAVIGATION_BAR, bottomNavigationBar) }

    var transparentNavigationBar: Boolean
        get() = prefs.getBoolean(TRANSPARENT_NAVI_BAR, true)
        set(transparentNavigationBar) = prefs.edit { putBoolean(TRANSPARENT_NAVI_BAR, transparentNavigationBar) }

    var appRecommendationDialogCount: Int
        get() = prefs.getInt(APP_RECOMMENDATION_DIALOG_COUNT, 3)
        set(appRecommendationDialogCount) = prefs.edit { putInt(APP_RECOMMENDATION_DIALOG_COUNT, appRecommendationDialogCount) }

    var openSearch: Boolean
        get() = prefs.getBoolean(CLOSE_SEARCH, false)
        set(openSearch) = prefs.edit { putBoolean(CLOSE_SEARCH, openSearch) }

    var closeSearch: Boolean
        get() = prefs.getBoolean(OPEN_SEARCH, false)
        set(closeSearch) = prefs.edit { putBoolean(OPEN_SEARCH, closeSearch) }

    var isPro: Boolean
        get() = prefs.getBoolean(IS_PRO_VERSION, false)
        set(isPro) = prefs.edit { putBoolean(IS_PRO_VERSION, isPro) }

    var isProSubs: Boolean
        get() = prefs.getBoolean(IS_PRO_SUBS_VERSION, false)
        set(isProSubs) = prefs.edit { putBoolean(IS_PRO_SUBS_VERSION, isProSubs) }

    var isProRuStore: Boolean
        get() = prefs.getBoolean(IS_PRO_RUSTORE_VERSION, false)
        set(isProSubs) = prefs.edit { putBoolean(IS_PRO_RUSTORE_VERSION, isProSubs) }

    var isProNoGP: Boolean
        get() = prefs.getBoolean(IS_PRO_NO_GP_VERSION, false)
        set(isProNoGP) = prefs.edit { putBoolean(IS_PRO_NO_GP_VERSION, isProNoGP) }

    var simIconsColors: LinkedList<Int>
        get(): LinkedList<Int> {
            val defaultList = arrayListOf(
                ContextCompat.getColor(context, R.color.md_red_500),
                ContextCompat.getColor(context, R.color.ic_dialer),
                ContextCompat.getColor(context, R.color.color_primary),
                ContextCompat.getColor(context, R.color.md_yellow_500),
                ContextCompat.getColor(context, R.color.md_orange_500)
            )
            return LinkedList(prefs.getString(SIM_ICON_COLORS, null)?.lines()?.map { it.toInt() } ?: defaultList)
        }
        set(simIconsColors) = prefs.edit { putString(SIM_ICON_COLORS, simIconsColors.joinToString(separator = "\n")) }

    var textCursorColor: Int
        get() = prefs.getInt(TEXT_CURSOR_COLOR, ContextCompat.getColor(context, R.color.default_primary_color))
        set(textCursorColor) = prefs.edit { putInt(TEXT_CURSOR_COLOR, textCursorColor) }

    var linesCount: Int
        get() = prefs.getInt(LINES_COUNT, 2)
        set(linesCount) = prefs.edit { putInt(LINES_COUNT, linesCount) }

    var showBlockedNumbers: Boolean
        get() = prefs.getBoolean(SHOW_BLOCK_NUMBERS, false)
        set(showBlockedNumbers) = prefs.edit { putBoolean(SHOW_BLOCK_NUMBERS, showBlockedNumbers) }

    var showButtonBlockedNumbers: Boolean
        get() = prefs.getBoolean(SHOW_BUTTON_BLOCK_NUMBERS, false)
        set(showButtonBlockedNumbers) = prefs.edit { putBoolean(SHOW_BUTTON_BLOCK_NUMBERS, showButtonBlockedNumbers) }

    var flashForAlerts: Boolean
        get() = prefs.getBoolean(FLASH_FOR_ALERTS, false)
        set(flashForAlerts) = prefs.edit { putBoolean(FLASH_FOR_ALERTS, flashForAlerts) }

    var currentSIMCardIndex: Int
        get() = prefs.getInt(CURRENT_SIM_CARD_INDEX, 0) //0 - sim1, 1 - sim2
        set(currentSIMCardIndex) = prefs.edit { putInt(CURRENT_SIM_CARD_INDEX, currentSIMCardIndex) }

    var isUsingAccentColor: Boolean
        get() = prefs.getBoolean(IS_USING_ACCENT_COLOR, context.resources.getBoolean(R.bool.using_accent_color))
        set(isUsingAccentColor) = prefs.edit { putBoolean(IS_USING_ACCENT_COLOR, isUsingAccentColor) }

    var topAppBarColorIcon: Boolean
        get() = prefs.getBoolean(TOP_APP_BAR_COLOR_ICON, false)
        set(topAppBarColorIcon) = prefs.edit { putBoolean(TOP_APP_BAR_COLOR_ICON, topAppBarColorIcon) }

    val isTopAppBarColorIcon: Flow<Boolean> = ::topAppBarColorIcon.asFlowNonNull()

    var topAppBarColorTitle: Boolean
        get() = prefs.getBoolean(TOP_APP_BAR_COLOR_TITLE, false)
        set(topAppBarColorTitle) = prefs.edit { putBoolean(TOP_APP_BAR_COLOR_TITLE, topAppBarColorTitle) }

    val isTopAppBarColorTitle: Flow<Boolean> = ::topAppBarColorTitle.asFlowNonNull()

    var autoBackupTime: Int
        get() = prefs.getInt(AUTO_BACKUP_TIME, 360)
        set(autoBackupTime) = prefs.edit { putInt(AUTO_BACKUP_TIME, autoBackupTime) }

    var autoBackupInterval: Int
        get() = prefs.getInt(AUTO_BACKUP_INTERVAL, 10)
        set(autoBackupInterval) = prefs.edit { putInt(AUTO_BACKUP_INTERVAL, autoBackupInterval) }

    var nextAutoBackupTime: Long
        get() = prefs.getLong(NEXT_AUTO_BACKUP_TIME, 0L)
        set(nextAutoBackupTime) = prefs.edit { putLong(NEXT_AUTO_BACKUP_TIME, nextAutoBackupTime) }

    var sortingSymbolsFirst: Boolean
        get() = prefs.getBoolean(SORT_SYMBOLS_FIRST, false)
        set(sortingSymbolsFirst) = prefs.edit { putBoolean(SORT_SYMBOLS_FIRST, sortingSymbolsFirst) }

    var hideTopBarWhenScroll: Boolean
        get() = prefs.getBoolean(HIDE_TOP_BAR_WHEN_SCROLL, false)
        set(hideTopBarWhenScroll) = prefs.edit { putBoolean(HIDE_TOP_BAR_WHEN_SCROLL, hideTopBarWhenScroll) }

    var skipArchiveConfirmation: Boolean
        get() = prefs.getBoolean(SKIP_ARCHIVE_CONFIRMATION, false)
        set(skipArchiveConfirmation) = prefs.edit { putBoolean(SKIP_ARCHIVE_CONFIRMATION, skipArchiveConfirmation) }

    var useSwipeToAction: Boolean
        get() = prefs.getBoolean(USE_SWIPE_TO_ACTION, true)
        set(swipeToAction) = prefs.edit { putBoolean(USE_SWIPE_TO_ACTION, swipeToAction) }

    var contactThumbnailsSize: Int
        get() = prefs.getInt(CONTACT_THUMBNAILS_SIZE, CONTACT_THUMBNAILS_SIZE_MEDIUM) //context.resources.getDimension(R.dimen.normal_icon_size).toInt()
        set(contactThumbnailsSize) = prefs.edit { putInt(CONTACT_THUMBNAILS_SIZE, contactThumbnailsSize) }

    var changeColourTopBar: Boolean
        get() = prefs.getBoolean(CHANGE_COLOUR_TOP_BAR, true)
        set(changeColourTopBar) = prefs.edit { putBoolean(CHANGE_COLOUR_TOP_BAR, changeColourTopBar) }

    var useShamsi: Boolean //Persian Calendar
        get() = prefs.getBoolean(USE_SHAMSI, false)
        set(useShamsi) = prefs.edit { putBoolean(USE_SHAMSI, useShamsi) }

    val isUseShamsi: Flow<Boolean> = ::useShamsi.asFlowNonNull()

    var needInit: Boolean
        get() = prefs.getBoolean(NEED_INIT, true)
        set(needInit) = prefs.edit { putBoolean(NEED_INIT, needInit) }

    var isMiui: Boolean
        get() = prefs.getBoolean(IS_MIUI, false)
        set(isMiui) = prefs.edit { putBoolean(IS_MIUI, isMiui) }

    var isEmui: Boolean
        get() = prefs.getBoolean(IS_EMUI, false)
        set(isEmui) = prefs.edit { putBoolean(IS_EMUI, isEmui) }

    var blockingType: Int
        get() = prefs.getInt(BLOCKING_TYPE, BLOCKING_TYPE_REJECT)
        set(blockingType) = prefs.edit { putInt(BLOCKING_TYPE, blockingType) }

    val isBlockingType: Flow<Int> = ::blockingType.asFlowNonNull()

    var blockingEnabled: Boolean
        get() = prefs.getBoolean(BLOCKING_ENABLED, false)
        set(blockingEnabled) = prefs.edit { putBoolean(BLOCKING_ENABLED, blockingEnabled) }

    val isBlockingEnabled: Flow<Boolean> = ::blockingEnabled.asFlowNonNull()

    var doNotBlockContactsAndRecent: Boolean
        get() = prefs.getBoolean(DO_NOT_BLOCK_CONTACTS_AND_RECENT, false)
        set(doNotBlockContactsAndRecent) = prefs.edit { putBoolean(DO_NOT_BLOCK_CONTACTS_AND_RECENT, doNotBlockContactsAndRecent) }

    val isDoNotBlockContactsAndRecent: Flow<Boolean> = ::doNotBlockContactsAndRecent.asFlowNonNull()

    var textAlignment: Int
        get() = prefs.getInt(TEXT_ALIGNMENT, TEXT_ALIGNMENT_START)
        set(textAlignment) = prefs.edit { putInt(TEXT_ALIGNMENT, textAlignment) }

    var useSpeechToText: Boolean
        get() = prefs.getBoolean(USE_SPEECH_TO_TEXT, true)
        set(useSpeechToText) = prefs.edit { putBoolean(USE_SPEECH_TO_TEXT, useSpeechToText) }

    var showNicknameInsteadNames: Boolean
        get() = prefs.getBoolean(SHOW_NICKNAME_INSTEAD_NAME, false)
        set(showNicknameInsteadNames) = prefs.edit { putBoolean(SHOW_NICKNAME_INSTEAD_NAME, showNicknameInsteadNames) }
}


