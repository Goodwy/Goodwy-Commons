package com.goodwy.commons.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Looper
import android.provider.ContactsContract
import android.util.Log
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.StringRes
import com.goodwy.commons.R
import com.goodwy.commons.extensions.normalizeString
import com.goodwy.commons.models.contacts.LocalContact
import com.goodwy.commons.overloads.times

const val EXTERNAL_STORAGE_PROVIDER_AUTHORITY = "com.android.externalstorage.documents"
const val EXTRA_SHOW_ADVANCED = "android.content.extra.SHOW_ADVANCED"

const val APP_NAME = "app_name"
const val APP_LICENSES = "app_licenses"
const val APP_FAQ = "app_faq"
const val APP_VERSION_NAME = "app_version_name"
const val APP_PACKAGE_NAME = "app_package_name"
const val APP_REPOSITORY_NAME = "app_repo_name"
const val APP_ICON_IDS = "app_icon_ids"
const val APP_ID = "app_id"
const val APP_LAUNCHER_NAME = "app_launcher_name"
const val REAL_FILE_PATH = "real_file_path_2"
const val IS_FROM_GALLERY = "is_from_gallery"
const val BROADCAST_REFRESH_MEDIA = "com.goodwy.REFRESH_MEDIA"
const val REFRESH_PATH = "refresh_path"
const val IS_CUSTOMIZING_COLORS = "is_customizing_colors"
const val BLOCKED_NUMBERS_EXPORT_DELIMITER = ","
const val BLOCKED_NUMBERS_EXPORT_EXTENSION = ".txt"
const val NOMEDIA = ".nomedia"
const val YOUR_ALARM_SOUNDS_MIN_ID = 1000
const val SHOW_FAQ_BEFORE_MAIL = "show_faq_before_mail"
const val CHOPPED_LIST_DEFAULT_SIZE = 50
const val SAVE_DISCARD_PROMPT_INTERVAL = 1000L
const val SD_OTG_PATTERN = "^/storage/[A-Za-z0-9]{4}-[A-Za-z0-9]{4}$"
const val SD_OTG_SHORT = "^[A-Za-z0-9]{4}-[A-Za-z0-9]{4}$"
const val KEY_PHONE = "phone"
const val KEY_MAILTO = "mailto"
const val CONTACT_ID = "contact_id"
const val IS_PRIVATE = "is_private"
const val SMT_PRIVATE = "smt_private"   // used at the contact source of local contacts hidden from other apps
const val FIRST_GROUP_ID = 10000L
const val MD5 = "MD5"
const val SHA1 = "SHA-1"
const val SHA256 = "SHA-256"
const val SHORT_ANIMATION_DURATION = 150L
const val DARK_GREY = 0xFF333333.toInt()
const val CURRENT_PHONE_NUMBER = "number"

const val ZERO_ALPHA = 0f
const val LOWER_ALPHA = 0.25f
const val MEDIUM_ALPHA = 0.5f
const val HIGHER_ALPHA = 0.75f

// alpha values on a scale 0 - 255
const val LOWER_ALPHA_INT = 30
const val MEDIUM_ALPHA_INT = 90

const val WCAG_AA_NORMAL = 4.5
const val WCAG_AA_LARGE = 3.0

const val HOUR_MINUTES = 60
const val DAY_MINUTES = 24 * HOUR_MINUTES
const val WEEK_MINUTES = DAY_MINUTES * 7
const val MONTH_MINUTES = DAY_MINUTES * 30
const val YEAR_MINUTES = DAY_MINUTES * 365

const val MINUTE_SECONDS = 60
const val HOUR_SECONDS = HOUR_MINUTES * 60
const val DAY_SECONDS = DAY_MINUTES * 60
const val WEEK_SECONDS = WEEK_MINUTES * 60
const val MONTH_SECONDS = MONTH_MINUTES * 60
const val YEAR_SECONDS = YEAR_MINUTES * 60

// shared preferences
const val PREFS_KEY = "Prefs"
const val APP_RUN_COUNT = "app_run_count"
const val LAST_VERSION = "last_version"
const val SD_TREE_URI = "tree_uri_2"
const val PRIMARY_ANDROID_DATA_TREE_URI = "primary_android_data_tree_uri_2"
const val OTG_ANDROID_DATA_TREE_URI = "otg_android_data_tree__uri_2"
const val SD_ANDROID_DATA_TREE_URI = "sd_android_data_tree_uri_2"
const val PRIMARY_ANDROID_OBB_TREE_URI = "primary_android_obb_tree_uri_2"
const val OTG_ANDROID_OBB_TREE_URI = "otg_android_obb_tree_uri_2"
const val SD_ANDROID_OBB_TREE_URI = "sd_android_obb_tree_uri_2"
const val OTG_TREE_URI = "otg_tree_uri_2"
const val SD_CARD_PATH = "sd_card_path_2"
const val OTG_REAL_PATH = "otg_real_path_2"
const val INTERNAL_STORAGE_PATH = "internal_storage_path"
const val TEXT_COLOR = "text_color"
const val BACKGROUND_COLOR = "background_color"
const val PRIMARY_COLOR = "primary_color_2"
const val ACCENT_COLOR = "accent_color"
const val APP_ICON_COLOR = "app_icon_color"
const val LAST_HANDLED_SHORTCUT_COLOR = "last_handled_shortcut_color"
const val LAST_ICON_COLOR = "last_icon_color"
const val CUSTOM_TEXT_COLOR = "custom_text_color"
const val CUSTOM_BACKGROUND_COLOR = "custom_background_color"
const val CUSTOM_PRIMARY_COLOR = "custom_primary_color"
const val CUSTOM_ACCENT_COLOR = "custom_accent_color"
const val CUSTOM_APP_ICON_COLOR = "custom_app_icon_color"
const val WIDGET_BG_COLOR = "widget_bg_color"
const val WIDGET_TEXT_COLOR = "widget_text_color"
const val WIDGET_LABEL_COLOR = "widget_label_color"
const val PASSWORD_PROTECTION = "password_protection"
const val PASSWORD_HASH = "password_hash"
const val PROTECTION_TYPE = "protection_type"
const val APP_PASSWORD_PROTECTION = "app_password_protection"
const val APP_PASSWORD_HASH = "app_password_hash"
const val APP_PROTECTION_TYPE = "app_protection_type"
const val DELETE_PASSWORD_PROTECTION = "delete_password_protection"
const val DELETE_PASSWORD_HASH = "delete_password_hash"
const val DELETE_PROTECTION_TYPE = "delete_protection_type"
const val PROTECTED_FOLDER_PATH = "protected_folder_path_"
const val PROTECTED_FOLDER_HASH = "protected_folder_hash_"
const val PROTECTED_FOLDER_TYPE = "protected_folder_type_"
const val KEEP_LAST_MODIFIED = "keep_last_modified"
const val USE_ENGLISH = "use_english"
const val USE_ICON_TABS = "use_icon_tabs"
const val USE_DIVIDERS = "use_dividers"
const val USE_COLORED_CONTACTS = "colored_contacts"
const val TABS_CHANGED = "tabs_changed"
const val WAS_USE_ENGLISH_TOGGLED = "was_use_english_toggled"
const val IS_GLOBAL_THEME_ENABLED = "is_global_theme_enabled"
const val IS_SYSTEM_THEME_ENABLED = "is_using_system_theme"
const val WAS_CUSTOM_THEME_SWITCH_DESCRIPTION_SHOWN = "was_custom_theme_switch_description_shown"
const val SHOW_INFO_BUBBLE = "show_info_bubble"
const val LAST_CONFLICT_RESOLUTION = "last_conflict_resolution"
const val LAST_CONFLICT_APPLY_TO_ALL = "last_conflict_apply_to_all"
const val LAST_COPY_PATH = "last_copy_path"
const val HAD_THANK_YOU_INSTALLED = "had_thank_you_installed"
const val SKIP_DELETE_CONFIRMATION = "skip_delete_confirmation"
const val ENABLE_PULL_TO_REFRESH = "enable_pull_to_refresh"
const val SCROLL_HORIZONTALLY = "scroll_horizontally"
const val PREVENT_PHONE_FROM_SLEEPING = "prevent_phone_from_sleeping"
const val LAST_USED_VIEW_PAGER_PAGE = "last_used_view_pager_page"
const val USE_24_HOUR_FORMAT = "use_24_hour_format"
const val SUNDAY_FIRST = "sunday_first"
const val WAS_ALARM_WARNING_SHOWN = "was_alarm_warning_shown"
const val WAS_REMINDER_WARNING_SHOWN = "was_reminder_warning_shown"
const val USE_SAME_SNOOZE = "use_same_snooze"
const val SNOOZE_TIME = "snooze_delay"
const val VIBRATE_ON_BUTTON_PRESS = "vibrate_on_button_press"
const val YOUR_ALARM_SOUNDS = "your_alarm_sounds"
const val SILENT = "silent"
const val OTG_PARTITION = "otg_partition_2"
const val IS_USING_MODIFIED_APP_ICON = "is_using_modified_app_icon"
const val INITIAL_WIDGET_HEIGHT = "initial_widget_height"
const val WIDGET_ID_TO_MEASURE = "widget_id_to_measure"
const val WAS_ORANGE_ICON_CHECKED = "was_orange_icon_checked"
const val WAS_APP_ON_SD_SHOWN = "was_app_on_sd_shown"
const val WAS_BEFORE_ASKING_SHOWN = "was_before_asking_shown"
const val WAS_BEFORE_RATE_SHOWN = "was_before_rate_shown"
const val WAS_INITIAL_UPGRADE_TO_PRO_SHOWN = "was_initial_upgrade_to_pro_shown"
const val WAS_APP_ICON_CUSTOMIZATION_WARNING_SHOWN = "was_app_icon_customization_warning_shown"
const val APP_SIDELOADING_STATUS = "app_sideloading_status"
const val DATE_FORMAT = "date_format"
const val WAS_OTG_HANDLED = "was_otg_handled_2"
const val WAS_UPGRADED_FROM_FREE_SHOWN = "was_upgraded_from_free_shown"
const val WAS_APP_RATED = "was_app_rated"
const val WAS_SORTING_BY_NUMERIC_VALUE_ADDED = "was_sorting_by_numeric_value_added"
const val WAS_FOLDER_LOCKING_NOTICE_SHOWN = "was_folder_locking_notice_shown"
const val LAST_RENAME_USED = "last_rename_used"
const val LAST_RENAME_PATTERN_USED = "last_rename_pattern_used"
const val LAST_EXPORTED_SETTINGS_FOLDER = "last_exported_settings_folder"
const val LAST_EXPORTED_SETTINGS_FILE = "last_exported_settings_file"
const val LAST_BLOCKED_NUMBERS_EXPORT_PATH = "last_blocked_numbers_export_path"
const val BLOCK_UNKNOWN_NUMBERS = "block_unknown_numbers"
const val BLOCK_HIDDEN_NUMBERS = "block_hidden_numbers"
const val FONT_SIZE = "font_size"
const val WAS_MESSENGER_RECORDER_SHOWN = "was_messenger_recorder_shown"
const val DEFAULT_TAB = "default_tab"
const val START_NAME_WITH_SURNAME = "start_name_with_surname"
const val FAVORITES = "favorites"
const val SHOW_CALL_CONFIRMATION = "show_call_confirmation"
const val COLOR_PICKER_RECENT_COLORS = "color_picker_recent_colors"
const val SHOW_CONTACT_THUMBNAILS = "show_contact_thumbnails"
const val SHOW_PHONE_NUMBERS = "show_phone_numbers"
const val FORMAT_PHONE_NUMBERS = "format_phone_numbers"
const val SHOW_ONLY_CONTACTS_WITH_NUMBERS = "show_only_contacts_with_numbers"
const val IGNORED_CONTACT_SOURCES = "ignored_contact_sources_2"
const val LAST_USED_CONTACT_SOURCE = "last_used_contact_source"
const val ON_CONTACT_CLICK = "on_contact_click"
const val SHOW_CONTACT_FIELDS = "show_contact_fields"
const val SHOW_TABS = "show_tabs"
const val SHOW_DIALPAD_BUTTON = "show_dialpad_button"
const val SPEED_DIAL = "speed_dial"
const val LAST_EXPORT_PATH = "last_export_path"
const val WAS_LOCAL_ACCOUNT_INITIALIZED = "was_local_account_initialized"
const val SHOW_PRIVATE_CONTACTS = "show_private_contacts"
const val MERGE_DUPLICATE_CONTACTS = "merge_duplicate_contacts"
const val FAVORITES_CONTACTS_ORDER = "favorites_contacts_order"
const val FAVORITES_CUSTOM_ORDER_SELECTED = "favorites_custom_order_selected"
const val VIEW_TYPE = "view_type"
const val CONTACTS_GRID_COLUMN_COUNT = "contacts_grid_column_count"
const val AUTO_BACKUP = "auto_backup"
const val AUTO_BACKUP_FOLDER = "auto_backup_folder"
const val AUTO_BACKUP_FILENAME = "auto_backup_filename"
const val LAST_AUTO_BACKUP_TIME = "last_auto_backup_time"
const val PASSWORD_RETRY_COUNT = "password_retry_count"
const val PASSWORD_COUNTDOWN_START_MS = "password_count_down_start_ms"
const val LAST_UNLOCK_TIMESTAMP_MS = "last_unlock_timestamp_ms"
const val UNLOCK_TIMEOUT_DURATION_MS = "unlock_timeout_duration_ms"
const val SHOW_CHECKMARKS_ON_SWITCHES = "show_checkmarks_on_switches"
const val FIRST_DAY_OF_WEEK = "first_day_of_week"

const val MAX_PASSWORD_RETRY_COUNT = 3
const val DEFAULT_PASSWORD_COUNTDOWN = 5
const val MINIMUM_PIN_LENGTH = 4
const val DEFAULT_UNLOCK_TIMEOUT_DURATION = 30000L

//Goodwy
const val SETTINGS_ICON = "settings_icon"
const val OVERFLOW_ICON = "overflow_icon"
const val SCREEN_SLIDE_ANIMATION = "Screen_slide_animation"
const val MATERIAL_DESIGN3 = "material_design3"
const val BOTTOM_NAVIGATION_BAR = "bottom_navigation_bar"
const val TRANSPARENT_NAVI_BAR = "transparent_navi_bar"
const val APP_RECOMMENDATION_DIALOG_COUNT = "app_recommendation_dialog_count"
const val USE_RELATIVE_DATE = "use_relative_date"
const val COLOR_SIM_ICON = "color_sim_icons"
const val OPEN_SEARCH = "open_search"
const val CLOSE_SEARCH = "close_search"
const val SHOW_ACCENT_COLOR = "show_accent_color"
const val SHOW_LIFEBUOY = "show_lifebuoy"
const val SHOW_COLLECTION = "show_collection"
const val SHOW_APP_ICON_COLOR = "show_app_icon_color"
const val IS_COLLECTION = "is_collection"
const val PLAY_STORE_INSTALLED = "play_store_installed"
const val CONTACT_COLOR_LIST = "contact_color_list"
const val SIM_ICON_COLORS = "sim_icons_colors"
const val TEXT_CURSOR_COLOR = "text_cursor_color"
const val TOP_APP_BAR_COLORED = "top_app_bar_colored"
const val TOP_APP_BAR_COLOR_ICON = "top_app_bar_color_icon"
const val TOP_APP_BAR_COLOR_TITLE = "top_app_bar_color_title"
const val LINES_COUNT = "lines_count"
const val SHOW_BLOCK_NUMBERS = "show_blocked_numbers"
const val SHOW_BUTTON_BLOCK_NUMBERS = "show_button_blocked_numbers"
const val FLASH_FOR_ALERTS = "flash_for_alerts"
const val RU_STORE = "ru_store"
const val USE_GOOGLE_PLAY = "use_google_play"
const val CURRENT_SIM_CARD_INDEX = "current_sim_card_index"
const val IS_USING_ACCENT_COLOR = "is_using_accent_color"
const val AUTO_BACKUP_TIME = "auto_backup_time"
const val AUTO_BACKUP_INTERVAL = "auto_backup_interval"
const val NEXT_AUTO_BACKUP_TIME = "next_auto_backup_time"
const val HIDE_TOP_BAR_WHEN_SCROLL = "hide_top_bar_when_scroll"
const val SKIP_ARCHIVE_CONFIRMATION = "skip_archive_confirmation"
const val USE_SWIPE_TO_ACTION = "use_swipe_to_action"
const val CONTACT_THUMBNAILS_SIZE = "contact_thumbnails_size"
const val CHANGE_COLOUR_TOP_BAR = "change_colour_top_bar"

const val IS_RIGHT_APP = "is_right_app"
const val IS_PRO_VERSION = "is_pro_version"
const val IS_PRO_SUBS_VERSION = "is_pro_subs_version"
const val IS_PRO_RUSTORE_VERSION = "is_pro_rustore_version"
const val IS_PRO_NO_GP_VERSION = "is_pro_no_gp_version"
const val PRODUCT_ID_LIST = "product_id_list"
const val PRODUCT_ID_LIST_RU = "product_id_list_ru"
const val SUBSCRIPTION_ID_LIST = "subscription_id_list"
const val SUBSCRIPTION_ID_LIST_RU = "subscription_id_list_ru"
const val SUBSCRIPTION_YEAR_ID_LIST = "subscription_year_id_list"
const val SUBSCRIPTION_YEAR_ID_LIST_RU = "subscription_year_id_list_ru"

// contact grid view constants
const val CONTACTS_GRID_MAX_COLUMNS_COUNT = 10

// phone number/email types
const val CELL = "CELL"
const val WORK = "WORK"
const val HOME = "HOME"
const val OTHER = "OTHER"
const val PREF = "PREF"
const val MAIN = "MAIN"
const val FAX = "FAX"
const val WORK_FAX = "WORK;FAX"
const val HOME_FAX = "HOME;FAX"
const val PAGER = "PAGER"
const val MOBILE = "MOBILE"

// IMs not supported by Ez-vcard
const val HANGOUTS = "Hangouts"
const val QQ = "QQ"
const val JABBER = "Jabber"

// licenses
internal const val LICENSE_KOTLIN = 1L
const val LICENSE_SUBSAMPLING = 2L
const val LICENSE_GLIDE = 4L
const val LICENSE_CROPPER = 8L
const val LICENSE_FILTERS = 16L
const val LICENSE_RTL = 32L
const val LICENSE_JODA = 64L
const val LICENSE_STETHO = 128L
const val LICENSE_OTTO = 256L
const val LICENSE_PHOTOVIEW = 512L
const val LICENSE_PICASSO = 1024L
const val LICENSE_PATTERN = 2048L
const val LICENSE_REPRINT = 4096L
const val LICENSE_GIF_DRAWABLE = 8192L
const val LICENSE_AUTOFITTEXTVIEW = 16384L
const val LICENSE_ROBOLECTRIC = 32768L
const val LICENSE_ESPRESSO = 65536L
const val LICENSE_GSON = 131072L
const val LICENSE_LEAK_CANARY = 262144L
const val LICENSE_NUMBER_PICKER = 524288L
const val LICENSE_EXOPLAYER = 1048576L
const val LICENSE_PANORAMA_VIEW = 2097152L
const val LICENSE_SANSELAN = 4194304L
const val LICENSE_GESTURE_VIEWS = 8388608L
const val LICENSE_INDICATOR_FAST_SCROLL = 16777216L
const val LICENSE_EVENT_BUS = 33554432L
const val LICENSE_AUDIO_RECORD_VIEW = 67108864L
const val LICENSE_SMS_MMS = 134217728L
const val LICENSE_APNG = 268435456L
const val LICENSE_PDF_VIEW_PAGER = 536870912L
const val LICENSE_M3U_PARSER = 1073741824L
const val LICENSE_ANDROID_LAME = 2147483648L
const val LICENSE_PDF_VIEWER = 4294967296L
const val LICENSE_ZIP4J = 8589934592L
const val LICENSE_EVALEX = 17179869184L

// global intents
const val OPEN_DOCUMENT_TREE_FOR_ANDROID_DATA_OR_OBB = 1000
const val OPEN_DOCUMENT_TREE_OTG = 1001
const val OPEN_DOCUMENT_TREE_SD = 1002
const val OPEN_DOCUMENT_TREE_FOR_SDK_30 = 1003
const val REQUEST_SET_AS = 1004
const val REQUEST_EDIT_IMAGE = 1005
const val SELECT_EXPORT_SETTINGS_FILE_INTENT = 1006
const val REQUEST_CODE_SET_DEFAULT_DIALER = 1007
const val CREATE_DOCUMENT_SDK_30 = 1008
const val REQUEST_CODE_SET_DEFAULT_CALLER_ID = 1010
const val REQUEST_APP_UNLOCK = 1012

// sorting
const val SORT_ORDER = "sort_order"
const val SORT_FOLDER_PREFIX = "sort_folder_"       // storing folder specific values at using "Use for this folder only"
const val SORT_BY_NAME = 1
const val SORT_BY_DATE_MODIFIED = 2
const val SORT_BY_SIZE = 4
const val SORT_BY_DATE_TAKEN = 8
const val SORT_BY_EXTENSION = 16
const val SORT_BY_PATH = 32
const val SORT_BY_NUMBER = 64
const val SORT_BY_FIRST_NAME = 128
const val SORT_BY_MIDDLE_NAME = 256
const val SORT_BY_SURNAME = 512
const val SORT_DESCENDING = 1024
const val SORT_BY_TITLE = 2048
const val SORT_BY_ARTIST = 4096
const val SORT_BY_DURATION = 8192
const val SORT_BY_RANDOM = 16384
const val SORT_USE_NUMERIC_VALUE = 32768
const val SORT_BY_FULL_NAME = 65536
const val SORT_BY_CUSTOM = 131072
const val SORT_BY_DATE_CREATED = 262144
const val SORT_BY_COUNT = 524288
const val SORT_SYMBOLS_FIRST = "sort_symbols_first"

// security
const val PROTECTION_NONE = -1
const val PROTECTION_PATTERN = 0
const val PROTECTION_PIN = 1
const val PROTECTION_FINGERPRINT = 2

// renaming
const val RENAME_SIMPLE = 0
const val RENAME_PATTERN = 1

const val SHOW_ALL_TABS = -1
const val SHOW_PATTERN = 0
const val SHOW_PIN = 1
const val SHOW_FINGERPRINT = 2

// permissions
const val PERMISSION_READ_STORAGE = 1
const val PERMISSION_WRITE_STORAGE = 2
const val PERMISSION_CAMERA = 3
const val PERMISSION_RECORD_AUDIO = 4
const val PERMISSION_READ_CONTACTS = 5
const val PERMISSION_WRITE_CONTACTS = 6
const val PERMISSION_READ_CALENDAR = 7
const val PERMISSION_WRITE_CALENDAR = 8
const val PERMISSION_CALL_PHONE = 9
const val PERMISSION_READ_CALL_LOG = 10
const val PERMISSION_WRITE_CALL_LOG = 11
const val PERMISSION_GET_ACCOUNTS = 12
const val PERMISSION_READ_SMS = 13
const val PERMISSION_SEND_SMS = 14
const val PERMISSION_READ_PHONE_STATE = 15
const val PERMISSION_MEDIA_LOCATION = 16
const val PERMISSION_POST_NOTIFICATIONS = 17
const val PERMISSION_READ_MEDIA_IMAGES = 18
const val PERMISSION_READ_MEDIA_VIDEO = 19
const val PERMISSION_READ_MEDIA_AUDIO = 20
const val PERMISSION_ACCESS_COARSE_LOCATION = 21
const val PERMISSION_ACCESS_FINE_LOCATION = 22
const val PERMISSION_READ_MEDIA_VISUAL_USER_SELECTED = 23
const val PERMISSION_READ_SYNC_SETTINGS = 24

// conflict resolving
const val CONFLICT_SKIP = 1
const val CONFLICT_OVERWRITE = 2
const val CONFLICT_MERGE = 3
const val CONFLICT_KEEP_BOTH = 4

// font sizes
const val FONT_SIZE_SMALL = 0
const val FONT_SIZE_MEDIUM = 1
const val FONT_SIZE_LARGE = 2
const val FONT_SIZE_EXTRA_LARGE = 3

const val MONDAY_BIT = 1
const val TUESDAY_BIT = 2
const val WEDNESDAY_BIT = 4
const val THURSDAY_BIT = 8
const val FRIDAY_BIT = 16
const val SATURDAY_BIT = 32
const val SUNDAY_BIT = 64
const val EVERY_DAY_BIT =
    MONDAY_BIT or TUESDAY_BIT or WEDNESDAY_BIT or THURSDAY_BIT or FRIDAY_BIT or SATURDAY_BIT or SUNDAY_BIT
const val WEEK_DAYS_BIT = MONDAY_BIT or TUESDAY_BIT or WEDNESDAY_BIT or THURSDAY_BIT or FRIDAY_BIT
const val WEEKENDS_BIT = SATURDAY_BIT or SUNDAY_BIT

const val SIDELOADING_UNCHECKED = 0
const val SIDELOADING_TRUE = 1
const val SIDELOADING_FALSE = 2

// default tabs
const val TAB_LAST_USED = 0
const val TAB_CONTACTS = 1
const val TAB_FAVORITES = 2
const val TAB_CALL_HISTORY = 4
const val TAB_GROUPS = 8
const val TAB_FILES = 16
const val TAB_RECENT_FILES = 32
const val TAB_STORAGE_ANALYSIS = 64

val photoExtensions: Array<String>
    get() = arrayOf(
        ".jpg",
        ".png",
        ".jpeg",
        ".bmp",
        ".webp",
        ".heic",
        ".heif",
        ".apng",
        ".avif",
        ".jxl"
    )

val videoExtensions: Array<String>
    get() = arrayOf(
        ".mp4",
        ".mkv",
        ".webm",
        ".avi",
        ".3gp",
        ".mov",
        ".m4v",
        ".3gpp"
    )

val audioExtensions: Array<String>
    get() = arrayOf(
        ".mp3",
        ".wav",
        ".wma",
        ".ogg",
        ".m4a",
        ".opus",
        ".flac",
        ".aac",
        ".m4b"
    )

val rawExtensions: Array<String>
    get() = arrayOf(
        ".dng",
        ".orf",
        ".nef",
        ".arw",
        ".rw2",
        ".cr2",
        ".cr3"
    )

val extensionsSupportingEXIF: Array<String>
    get() = arrayOf(
        ".jpg",
        ".jpeg",
        ".png",
        ".webp",
        ".dng"
    )

const val DATE_FORMAT_ONE = "dd.MM.yyyy"
const val DATE_FORMAT_TWO = "dd/MM/yyyy"
const val DATE_FORMAT_THREE = "MM/dd/yyyy"
const val DATE_FORMAT_FOUR = "yyyy-MM-dd"
const val DATE_FORMAT_FIVE = "d MMMM yyyy"
const val DATE_FORMAT_SIX = "MMMM d yyyy"
const val DATE_FORMAT_SEVEN = "MM-dd-yyyy"
const val DATE_FORMAT_EIGHT = "dd-MM-yyyy"
const val DATE_FORMAT_NINE = "yyyyMMdd"
const val DATE_FORMAT_TEN = "yyyy.MM.dd"
const val DATE_FORMAT_ELEVEN = "yy-MM-dd"
const val DATE_FORMAT_TWELVE = "yyMMdd"
const val DATE_FORMAT_THIRTEEN = "yy.MM.dd"
const val DATE_FORMAT_FOURTEEN = "yy/MM/dd"

const val TIME_FORMAT_12 = "hh:mm a"
const val TIME_FORMAT_24 = "HH:mm"

// possible icons at the top left corner
enum class NavigationIcon(@StringRes val accessibilityResId: Int) {
    Cross(R.string.close),
    Arrow(R.string.back),
    None(0)
}

val appIconColorStrings = arrayListOf(
    ".Original",
    ".One",
    ".Two",
    ".Three",
    ".Four",
    ".Five",
    ".Six",
    ".Seven",
    ".Eight",
    ".Nine",
    ".Ten",
    ".Eleven"
)

// app icon color
const val APP_ICON_ORIGINAL = 0
const val APP_ICON_ONE = 1
const val APP_ICON_TWO = 2
const val APP_ICON_THREE = 3
const val APP_ICON_FOUR = 4
const val APP_ICON_FIVE = 5
const val APP_ICON_SIX = 6
const val APP_ICON_SEVEN = 7
const val APP_ICON_EIGHT = 8
const val APP_ICON_NINE = 9
const val APP_ICON_TEN = 10
const val APP_ICON_ELEVEN = 11

// letter background colors
const val LBC_ORIGINAL = 1 //letterBackgroundColors
const val LBC_ANDROID = 2 //letterBackgroundColorsAndroid
const val LBC_IOS = 3 //letterBackgroundColorsIOS
const val LBC_ARC = 4 //letterBackgroundColorsArc

//Overflow icon
const val OVERFLOW_ICON_HORIZONTAL = 0
const val OVERFLOW_ICON_VERTICAL = 1
const val OVERFLOW_ICON_HORIZONTAL_ROUND = 2

// most app icon colors from md_app_icon_colors with reduced alpha
// used at showing contact placeholders without image
val letterBackgroundColors = arrayListOf(
    0xFF8CDAFA,
    0xFF5D99FF,
    0xFF6D73ED,
    0xFFA589EF,
    0xFFFF6969,
    0xFFFF8963,
    0xFFF1AF28,
    0xFFF7DF32,
    0xFF06C9AF
)

//TODO Random color
val letterBackgroundColorsAndroid = arrayListOf( // Android
    0xFF00D0EA,
    0xFF0075F0,
    0xFF7C4DFF,
    0xFFBC56FF,
    0xFFFF54BB,
    0xFFFF5B55,
    0xFFFF891D,
    0xFFFFC600,
    0xFF26BC6D
)

val letterBackgroundColorsArc = arrayListOf( // Arc
    0xFF6DBAD9,
    0xFF666789,
    0xFFC5C6E5,
    0xFFA6729E,
    0xFFF29BBB,
    0xFFF25E6B,
    0xFFFF8558,
    0xFFF8D558,
    0xFF34E895
)

val letterBackgroundColorsIOS = arrayListOf( // iOS
    0xFF64D1FF,
    0xFF0B84FE,
    0xFF5D5CE6,
    0xFFBF5AF3,
    0xFFFE375E,
    0xFFFF5713,
    0xFFFE9F0C,
    0xFFFECB00,
    0xFF2ED158
)
/*val letterBackgroundColors = arrayListOf(
    0xCCD32F2F,
    0xCCC2185B,
    0xCC1976D2,
    0xCC0288D1,
    0xCC0097A7,
    0xCC00796B,
    0xCC388E3C,
    0xCC689F38,
    0xCCF57C00,
    0xCCE64A19
)*/

// view types
const val VIEW_TYPE_GRID = 1
const val VIEW_TYPE_LIST = 2
const val VIEW_TYPE_UNEVEN_GRID = 3

fun isOnMainThread() = Looper.myLooper() == Looper.getMainLooper()

fun ensureBackgroundThread(callback: () -> Unit) {
    if (isOnMainThread()) {
        Thread {
            callback()
        }.start()
    } else {
        callback()
    }
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N)
fun isNougatPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N_MR1)
fun isNougatMR1Plus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
fun isOreoPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O_MR1)
fun isOreoMr1Plus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.P)
fun isPiePlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
fun isQPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.R)
fun isRPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
fun isSPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
fun isTiramisuPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun isUpsideDownCakePlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

fun getDateFormats() = arrayListOf(
    "--MM-dd",
    "yyyy-MM-dd",
    "yyyyMMdd",
    "yyyy.MM.dd",
    "yy-MM-dd",
    "yyMMdd",
    "yy.MM.dd",
    "yy/MM/dd",
    "MM-dd",
    "MMdd",
    "MM/dd",
    "MM.dd"
)

fun getDateFormatsWithYear() = arrayListOf(
    DATE_FORMAT_FOUR,
    DATE_FORMAT_NINE,
    DATE_FORMAT_TEN,
    DATE_FORMAT_ELEVEN,
    DATE_FORMAT_TWELVE,
    DATE_FORMAT_THIRTEEN,
    DATE_FORMAT_FOURTEEN,
)

val normalizeRegex = "\\p{InCombiningDiacriticalMarks}+".toRegex()

fun getConflictResolution(resolutions: LinkedHashMap<String, Int>, path: String): Int {
    return if (resolutions.size == 1 && resolutions.containsKey("")) {
        resolutions[""]!!
    } else if (resolutions.containsKey(path)) {
        resolutions[path]!!
    } else {
        CONFLICT_SKIP
    }
}

val proPackages = arrayListOf<String>()

fun mydebug(message: String) = Log.e("DEBUG", message)

fun getQuestionMarks(size: Int) = ("?," * size).trimEnd(',')

@SuppressLint("UseCompatLoadingForDrawables")
fun getFilePlaceholderDrawables(context: Context): HashMap<String, Drawable> {
    val fileDrawables = HashMap<String, Drawable>()
    hashMapOf<String, Int>().apply {
        put("aep", R.drawable.ic_file_aep)
        put("ai", R.drawable.ic_file_ai)
        put("avi", R.drawable.ic_file_avi)
        put("css", R.drawable.ic_file_css)
        put("csv", R.drawable.ic_file_csv)
        put("dbf", R.drawable.ic_file_dbf)
        put("doc", R.drawable.ic_file_doc)
        put("docx", R.drawable.ic_file_doc)
        put("dwg", R.drawable.ic_file_dwg)
        put("exe", R.drawable.ic_file_exe)
        put("fla", R.drawable.ic_file_fla)
        put("flv", R.drawable.ic_file_flv)
        put("htm", R.drawable.ic_file_html)
        put("html", R.drawable.ic_file_html)
        put("ics", R.drawable.ic_file_ics)
        put("indd", R.drawable.ic_file_indd)
        put("iso", R.drawable.ic_file_iso)
        put("jpg", R.drawable.ic_file_jpg)
        put("jpeg", R.drawable.ic_file_jpg)
        put("js", R.drawable.ic_file_js)
        put("json", R.drawable.ic_file_json)
        put("m4a", R.drawable.ic_file_m4a)
        put("mp3", R.drawable.ic_file_mp3)
        put("mp4", R.drawable.ic_file_mp4)
        put("ogg", R.drawable.ic_file_ogg)
        put("pdf", R.drawable.ic_file_pdf)
        put("plproj", R.drawable.ic_file_plproj)
        put("ppt", R.drawable.ic_file_ppt)
        put("pptx", R.drawable.ic_file_ppt)
        put("prproj", R.drawable.ic_file_prproj)
        put("psd", R.drawable.ic_file_psd)
        put("rtf", R.drawable.ic_file_rtf)
        put("sesx", R.drawable.ic_file_sesx)
        put("sql", R.drawable.ic_file_sql)
        put("svg", R.drawable.ic_file_svg)
        put("txt", R.drawable.ic_file_txt)
        put("vcf", R.drawable.ic_file_vcf)
        put("wav", R.drawable.ic_file_wav)
        put("wmv", R.drawable.ic_file_wmv)
        put("xls", R.drawable.ic_file_xls)
        put("xlsx", R.drawable.ic_file_xls)
        put("xml", R.drawable.ic_file_xml)
        put("zip", R.drawable.ic_file_zip)
    }.forEach { (key, value) ->
        fileDrawables[key] = context.resources.getDrawable(value)
    }
    return fileDrawables
}

const val FIRST_CONTACT_ID = 1000000
const val DEFAULT_FILE_NAME = "contacts.vcf"

// visible fields filtering
const val SHOW_PREFIX_FIELD = 1
const val SHOW_FIRST_NAME_FIELD = 2
const val SHOW_MIDDLE_NAME_FIELD = 4
const val SHOW_SURNAME_FIELD = 8
const val SHOW_SUFFIX_FIELD = 16
const val SHOW_PHONE_NUMBERS_FIELD = 32
const val SHOW_EMAILS_FIELD = 64
const val SHOW_ADDRESSES_FIELD = 128
const val SHOW_EVENTS_FIELD = 256
const val SHOW_NOTES_FIELD = 512
const val SHOW_ORGANIZATION_FIELD = 1024
const val SHOW_GROUPS_FIELD = 2048
const val SHOW_CONTACT_SOURCE_FIELD = 4096
const val SHOW_WEBSITES_FIELD = 8192
const val SHOW_NICKNAME_FIELD = 16384
const val SHOW_IMS_FIELD = 32768
const val SHOW_RINGTONE_FIELD = 65536
const val SHOW_MESSENGERS_ACTIONS_FIELD = 131072
const val SHOW_RELATIONS_FIELD = 262144

const val DEFAULT_EMAIL_TYPE = ContactsContract.CommonDataKinds.Email.TYPE_HOME
const val DEFAULT_PHONE_NUMBER_TYPE = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
const val DEFAULT_ADDRESS_TYPE = ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME
const val DEFAULT_EVENT_TYPE = ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY
const val DEFAULT_ORGANIZATION_TYPE = ContactsContract.CommonDataKinds.Organization.TYPE_WORK
const val DEFAULT_WEBSITE_TYPE = ContactsContract.CommonDataKinds.Website.TYPE_HOMEPAGE
const val DEFAULT_RELATION_TYPE = ContactsContract.CommonDataKinds.Relation.TYPE_FRIEND
const val DEFAULT_IM_TYPE = ContactsContract.CommonDataKinds.Im.PROTOCOL_SKYPE
const val DEFAULT_MIMETYPE = ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE

// contact photo changes
const val PHOTO_ADDED = 1
const val PHOTO_REMOVED = 2
const val PHOTO_CHANGED = 3
const val PHOTO_UNCHANGED = 4

// contact photo sizes
const val CONTACT_THUMBNAILS_SIZE_SMALL = 0
const val CONTACT_THUMBNAILS_SIZE_MEDIUM = 1
const val CONTACT_THUMBNAILS_SIZE_LARGE = 2
const val CONTACT_THUMBNAILS_SIZE_EXTRA_LARGE = 3

const val ON_CLICK_CALL_CONTACT = 1
const val ON_CLICK_VIEW_CONTACT = 2
const val ON_CLICK_EDIT_CONTACT = 3

// apps with special handling
const val TELEGRAM_PACKAGE = "org.telegram.messenger"
const val SIGNAL_PACKAGE = "org.thoughtcrime.securesms"
const val WHATSAPP_PACKAGE = "com.whatsapp"
const val VIBER_PACKAGE = "com.viber.voip"
const val THREEMA_PACKAGE = "ch.threema.app"

const val SOCIAL_VOICE_CALL = 0
const val SOCIAL_VIDEO_CALL = 1
const val SOCIAL_MESSAGE = 2

fun getEmptyLocalContact() = LocalContact(
    0,
    "",
    "",
    "",
    "",
    "",
    "",
    null,
    "",
    ArrayList(),
    ArrayList(),
    ArrayList(),
    0,
    ArrayList(),
    "",
    ArrayList(),
    "",
    "",
    ArrayList(),
    ArrayList(),
    ArrayList(),
    null
)

fun getProperText(text: String, shouldNormalize: Boolean) =
    when {
        shouldNormalize -> text.normalizeString()
        else -> text
    }

fun getISODayOfWeekFromJava(javaDayOfWeek: Int): Int {
    if (javaDayOfWeek !in 1..7) {
        throw IllegalArgumentException("Invalid Java day of week: $javaDayOfWeek")
    }

    // Java: Sun=1, ..., Sat=7
    // ISO:  Mon=1, ..., Sun=7
    return (javaDayOfWeek + 5) % 7 + 1
}

fun getJavaDayOfWeekFromISO(isoDayOfWeek: Int): Int {
    if (isoDayOfWeek !in 1..7) {
        throw IllegalArgumentException("Invalid ISO day of week: $isoDayOfWeek")
    }

    // ISO:  Mon=1, ..., Sun=7
    // Java: Sun=1, ..., Sat=7
    return (isoDayOfWeek % 7) + 1
}
