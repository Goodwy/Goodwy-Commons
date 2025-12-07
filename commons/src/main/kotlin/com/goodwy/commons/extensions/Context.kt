package com.goodwy.commons.extensions

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.pm.ShortcutManager
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.media.MediaMetadataRetriever
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.BaseColumns
import android.provider.BlockedNumberContract.BlockedNumbers
import android.provider.ContactsContract.CommonDataKinds.*
import android.provider.DocumentsContract
import android.provider.MediaStore.Audio
import android.provider.MediaStore.Files
import android.provider.MediaStore.Images
import android.provider.MediaStore.MediaColumns
import android.provider.MediaStore.Video
import android.provider.OpenableColumns
import android.provider.Settings
import android.telecom.TelecomManager
import android.telephony.PhoneNumberUtils
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.text.BidiFormatter
import androidx.exifinterface.media.ExifInterface
import androidx.loader.content.CursorLoader
import com.github.ajalt.reprint.core.Reprint
import com.goodwy.commons.R
import com.goodwy.commons.helpers.*
import com.goodwy.commons.helpers.MyContentProvider.PERMISSION_WRITE_GLOBAL_SETTINGS
import com.goodwy.commons.models.AlarmSound
import com.goodwy.commons.models.BlockedNumber
import com.goodwy.commons.models.contacts.ContactRelation
import com.goodwy.strings.R as stringsR
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.DateTimeConstants
import androidx.core.graphics.toColorInt

fun Context.getSharedPrefs() = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

val Context.isRTLLayout: Boolean get() = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

val Context.areSystemAnimationsEnabled: Boolean get() = Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 0f) > 0f

val Context.appLockManager
    get() = AppLockManager.getInstance(applicationContext as Application)

fun Context.toast(id: Int, length: Int = Toast.LENGTH_SHORT) {
    toast(getString(id), length)
}

fun Context.toast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    try {
        if (isOnMainThread()) {
            doToast(this, msg, length)
        } else {
            Handler(Looper.getMainLooper()).post {
                doToast(this, msg, length)
            }
        }
    } catch (_: Exception) {
    }
}

private fun doToast(context: Context, message: String, length: Int) {
    if (context is Activity) {
        if (!context.isFinishing && !context.isDestroyed) {
            Toast.makeText(context, message, length).show()
        }
    } else {
        Toast.makeText(context, message, length).show()
    }
}

fun Context.showErrorToast(msg: String, length: Int = Toast.LENGTH_LONG) {
    toast(String.format(getString(R.string.error), msg), length)
}

fun Context.showErrorToast(exception: Exception, length: Int = Toast.LENGTH_LONG) {
    showErrorToast(exception.toString(), length)
}

val Context.baseConfig: BaseConfig get() = BaseConfig.newInstance(this)
val Context.sdCardPath: String get() = baseConfig.sdCardPath
val Context.internalStoragePath: String get() = baseConfig.internalStoragePath
val Context.otgPath: String get() = baseConfig.OTGPath

fun isFingerPrintSensorAvailable() = Reprint.isHardwarePresent()

fun Context.isBiometricIdAvailable(): Boolean = when (BiometricManager.from(this).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
    BiometricManager.BIOMETRIC_SUCCESS, BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> true
    else -> false
}

fun Context.isBiometricAuthSupported(): Boolean {
    return if (isRPlus()) {
        isBiometricIdAvailable()
    } else {
        isFingerPrintSensorAvailable()
    }
}

fun Context.getLatestMediaId(uri: Uri = Files.getContentUri("external")): Long {
    val projection = arrayOf(
        BaseColumns._ID
    )
    try {
        val cursor = queryCursorDesc(uri, projection, BaseColumns._ID, 1)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getLongValue(BaseColumns._ID)
            }
        }
    } catch (_: Exception) {
    }
    return 0
}

private fun Context.queryCursorDesc(
    uri: Uri,
    projection: Array<String>,
    sortColumn: String,
    limit: Int,
): Cursor? {
    return if (isRPlus()) {
        val queryArgs = bundleOf(
            ContentResolver.QUERY_ARG_LIMIT to limit,
            ContentResolver.QUERY_ARG_SORT_DIRECTION to ContentResolver.QUERY_SORT_DIRECTION_DESCENDING,
            ContentResolver.QUERY_ARG_SORT_COLUMNS to arrayOf(sortColumn),
        )
        contentResolver.query(uri, projection, queryArgs, null)
    } else {
        val sortOrder = "$sortColumn DESC LIMIT $limit"
        contentResolver.query(uri, projection, null, null, sortOrder)
    }
}

fun Context.getLatestMediaByDateId(uri: Uri = Files.getContentUri("external")): Long {
    val projection = arrayOf(
        BaseColumns._ID
    )
    try {
        val cursor = queryCursorDesc(uri, projection, Images.ImageColumns.DATE_TAKEN, 1)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getLongValue(BaseColumns._ID)
            }
        }
    } catch (_: Exception) {
    }
    return 0
}

// some helper functions were taken from https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
fun Context.getRealPathFromURI(uri: Uri): String? {
    if (uri.scheme == "file") {
        return uri.path
    }

    if (isDownloadsDocument(uri)) {
        val id = DocumentsContract.getDocumentId(uri)
        if (id.areDigitsOnly()) {
            val newUri = ContentUris.withAppendedId("content://downloads/public_downloads".toUri(), id.toLong())
            val path = getDataColumn(newUri)
            if (path != null) {
                return path
            }
        }
    } else if (isExternalStorageDocument(uri)) {
        val documentId = DocumentsContract.getDocumentId(uri)
        val parts = documentId.split(":")
        if (parts[0].equals("primary", true)) {
            return "${Environment.getExternalStorageDirectory().absolutePath}/${parts[1]}"
        }
    } else if (isMediaDocument(uri)) {
        val documentId = DocumentsContract.getDocumentId(uri)
        val split = documentId.split(":").dropLastWhile { it.isEmpty() }.toTypedArray()
        val type = split[0]

        val contentUri = when (type) {
            "video" -> Video.Media.EXTERNAL_CONTENT_URI
            "audio" -> Audio.Media.EXTERNAL_CONTENT_URI
            else -> Images.Media.EXTERNAL_CONTENT_URI
        }

        val selection = "_id=?"
        val selectionArgs = arrayOf(split[1])
        val path = getDataColumn(contentUri, selection, selectionArgs)
        if (path != null) {
            return path
        }
    }

    return getDataColumn(uri)
}

fun Context.getDataColumn(uri: Uri, selection: String? = null, selectionArgs: Array<String>? = null): String? {
    try {
        val projection = arrayOf(Files.FileColumns.DATA)
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                val data = cursor.getStringValue(Files.FileColumns.DATA)
                if (data != "null") {
                    return data
                }
            }
        }
    } catch (_: Exception) {
    }
    return null
}

private fun isMediaDocument(uri: Uri) = uri.authority == "com.android.providers.media.documents"

private fun isDownloadsDocument(uri: Uri) = uri.authority == "com.android.providers.downloads.documents"

private fun isExternalStorageDocument(uri: Uri) = uri.authority == "com.android.externalstorage.documents"

fun Context.hasPermission(permId: Int) = ContextCompat.checkSelfPermission(this, getPermissionString(permId)) == PERMISSION_GRANTED

fun Context.hasAllPermissions(permIds: Collection<Int>) = permIds.all(this::hasPermission)

@SuppressLint("InlinedApi")
fun getPermissionString(id: Int) = when (id) {
    PERMISSION_READ_STORAGE -> Manifest.permission.READ_EXTERNAL_STORAGE
    PERMISSION_WRITE_STORAGE -> Manifest.permission.WRITE_EXTERNAL_STORAGE
    PERMISSION_CAMERA -> Manifest.permission.CAMERA
    PERMISSION_RECORD_AUDIO -> Manifest.permission.RECORD_AUDIO
    PERMISSION_READ_CONTACTS -> Manifest.permission.READ_CONTACTS
    PERMISSION_WRITE_CONTACTS -> Manifest.permission.WRITE_CONTACTS
    PERMISSION_READ_CALENDAR -> Manifest.permission.READ_CALENDAR
    PERMISSION_WRITE_CALENDAR -> Manifest.permission.WRITE_CALENDAR
    PERMISSION_CALL_PHONE -> Manifest.permission.CALL_PHONE
    PERMISSION_READ_CALL_LOG -> Manifest.permission.READ_CALL_LOG
    PERMISSION_WRITE_CALL_LOG -> Manifest.permission.WRITE_CALL_LOG
    PERMISSION_GET_ACCOUNTS -> Manifest.permission.GET_ACCOUNTS
    PERMISSION_READ_SMS -> Manifest.permission.READ_SMS
    PERMISSION_SEND_SMS -> Manifest.permission.SEND_SMS
    PERMISSION_READ_PHONE_STATE -> Manifest.permission.READ_PHONE_STATE
    PERMISSION_MEDIA_LOCATION -> if (isQPlus()) Manifest.permission.ACCESS_MEDIA_LOCATION else ""
    PERMISSION_POST_NOTIFICATIONS -> Manifest.permission.POST_NOTIFICATIONS
    PERMISSION_READ_MEDIA_IMAGES -> Manifest.permission.READ_MEDIA_IMAGES
    PERMISSION_READ_MEDIA_VIDEO -> Manifest.permission.READ_MEDIA_VIDEO
    PERMISSION_READ_MEDIA_AUDIO -> Manifest.permission.READ_MEDIA_AUDIO
    PERMISSION_ACCESS_COARSE_LOCATION -> Manifest.permission.ACCESS_COARSE_LOCATION
    PERMISSION_ACCESS_FINE_LOCATION -> Manifest.permission.ACCESS_FINE_LOCATION
    PERMISSION_READ_MEDIA_VISUAL_USER_SELECTED -> Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    PERMISSION_READ_SYNC_SETTINGS -> Manifest.permission.READ_SYNC_SETTINGS
    else -> ""
}

fun Context.launchActivityIntent(intent: Intent) {
    try {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        toast(R.string.no_app_found)
    } catch (e: Exception) {
        showErrorToast(e)
    }
}

fun Context.getFilePublicUri(file: File, applicationId: String): Uri {
    // for images/videos/gifs try getting a media content uri first, like content://media/external/images/media/438
    // if media content uri is null, get our custom uri like content://com.goodwy.gallery.provider/external_files/emulated/0/DCIM/IMG_20171104_233915.jpg
    var uri = if (file.isMediaFile()) {
        getMediaContentUri(file.absolutePath)
    } else {
        getMediaContent(file.absolutePath, Files.getContentUri("external"))
    }

    if (uri == null) {
        uri = FileProvider.getUriForFile(this, "$applicationId.provider", file)
    }

    return uri!!
}

fun Context.getMediaContentUri(path: String): Uri? {
    val uri = when {
        path.isImageFast() -> Images.Media.EXTERNAL_CONTENT_URI
        path.isVideoFast() -> Video.Media.EXTERNAL_CONTENT_URI
        else -> Files.getContentUri("external")
    }

    return getMediaContent(path, uri)
}

fun Context.getMediaContent(path: String, uri: Uri): Uri? {
    val projection = arrayOf(Images.Media._ID)
    val selection = Images.Media.DATA + "= ?"
    val selectionArgs = arrayOf(path)
    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                val id = cursor.getIntValue(Images.Media._ID).toString()
                return Uri.withAppendedPath(uri, id)
            }
        }
    } catch (_: Exception) {
    }
    return null
}

fun Context.queryCursor(
    uri: Uri,
    projection: Array<String>,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
    sortOrder: String? = null,
    showErrors: Boolean = false,
    callback: (cursor: Cursor) -> Unit
) {
    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
        cursor?.use {
            if (cursor.moveToFirst()) {
                do {
                    callback(cursor)
                } while (cursor.moveToNext())
            }
        }
    } catch (e: Exception) {
        if (showErrors) {
            showErrorToast(e)
        }
    }
}

fun Context.queryCursor(
    uri: Uri,
    projection: Array<String>,
    queryArgs: Bundle,
    showErrors: Boolean = false,
    callback: (cursor: Cursor) -> Unit
) {
    try {
        val cursor = contentResolver.query(uri, projection, queryArgs, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                do {
                    callback(cursor)
                } while (cursor.moveToNext())
            }
        }
    } catch (e: Exception) {
        if (showErrors) {
            showErrorToast(e)
        }
    }
}

fun Context.getFilenameFromUri(uri: Uri): String {
    return if (uri.scheme == "file") {
        File(uri.toString()).name
    } else {
        getFilenameFromContentUri(uri) ?: uri.lastPathSegment ?: ""
    }
}

fun Context.getMimeTypeFromUri(uri: Uri): String {
    var mimetype = uri.path?.getMimeType() ?: ""
    if (mimetype.isEmpty()) {
        try {
            mimetype = contentResolver.getType(uri) ?: ""
        } catch (_: IllegalStateException) {
        }
    }
    return mimetype
}

fun Context.ensurePublicUri(path: String, applicationId: String): Uri? {
    return when {
        hasProperStoredAndroidTreeUri(path) && isRestrictedSAFOnlyRoot(path) -> {
            getAndroidSAFUri(path)
        }

        hasProperStoredDocumentUriSdk30(path) && isAccessibleWithSAFSdk30(path) -> {
            createDocumentUriUsingFirstParentTreeUri(path)
        }

        isPathOnOTG(path) -> {
            getDocumentFile(path)?.uri
        }

        else -> {
            val uri = path.toUri()
            if (uri.scheme == "content") {
                uri
            } else {
                val newPath = if (uri.toString().startsWith("/")) uri.toString() else uri.path ?: ""
                val file = File(newPath)
                getFilePublicUri(file, applicationId)
            }
        }
    }
}

fun Context.ensurePublicUri(uri: Uri, applicationId: String): Uri {
    return if (uri.scheme == "content") {
        uri
    } else {
        val file = File(uri.path!!)
        getFilePublicUri(file, applicationId)
    }
}

fun Context.getFilenameFromContentUri(uri: Uri): String? {
    val projection = arrayOf(
        OpenableColumns.DISPLAY_NAME
    )

    try {
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getStringValue(OpenableColumns.DISPLAY_NAME)
            }
        }
    } catch (_: Exception) {
    }
    return null
}

fun Context.getSizeFromContentUri(uri: Uri): Long {
    val projection = arrayOf(OpenableColumns.SIZE)
    try {
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getLongValue(OpenableColumns.SIZE)
            }
        }
    } catch (_: Exception) {
    }
    return 0L
}

fun Context.getMyContentProviderCursorLoader() = CursorLoader(this, MyContentProvider.MY_CONTENT_URI, null, null, null, null)

fun Context.getMyContactsCursor(favoritesOnly: Boolean, withPhoneNumbersOnly: Boolean) = try {
    val getFavoritesOnly = if (favoritesOnly) "1" else "0"
    val getWithPhoneNumbersOnly = if (withPhoneNumbersOnly) "1" else "0"
    val args = arrayOf(getFavoritesOnly, getWithPhoneNumbersOnly)
    CursorLoader(this, MyContactsContentProvider.CONTACTS_CONTENT_URI, null, null, args, null).loadInBackground()
} catch (_: Exception) {
    null
}

fun getCurrentFormattedDateTime(): String {
    val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    return simpleDateFormat.format(Date(System.currentTimeMillis()))
}

fun Context.updateSDCardPath() {
        ensureBackgroundThread {
        val oldPath = baseConfig.sdCardPath
        baseConfig.sdCardPath = getSDCardPath()
        if (oldPath != baseConfig.sdCardPath) {
            baseConfig.sdTreeUri = ""
        }
    }
}

fun Context.getUriMimeType(path: String, newUri: Uri): String {
    var mimeType = path.getMimeType()
    if (mimeType.isEmpty()) {
        mimeType = getMimeTypeFromUri(newUri)
    }
    return mimeType
}

fun Context.canAccessGlobalConfig(): Boolean {
    return isPro() && ContextCompat.checkSelfPermission(this, PERMISSION_WRITE_GLOBAL_SETTINGS) == PERMISSION_GRANTED
}

fun Context.isOrWasThankYouInstalled(allowPretend: Boolean = true): Boolean {
    return when {
        isPackageInstalled("com.goodwy.audiobook")
            || isPackageInstalled("com.goodwy.voicerecorder")
            || isPackageInstalled("com.goodwy.files") -> {
            if (!baseConfig.hadThankYouInstalled) {
                baseConfig.hadThankYouInstalled = true
            }
            true
        }

        baseConfig.hadThankYouInstalled -> true
        /*resources.getBoolean(R.bool.pretend_thank_you_installed) && */allowPretend -> true
        else -> false
    }
}

fun PackageManager.isAppInstalled(packageName: String): Boolean =
    getInstalledApplications(PackageManager.GET_META_DATA)
        .firstOrNull { it.packageName == packageName } != null

fun Context.addLockedLabelIfNeeded(stringId: Int, lock: Boolean = false): String {
    return if (lock) {
        getString(stringId)
    } else {
        "${getString(stringId)} (${getString(R.string.feature_locked)})"
    }
}

fun Context.isPackageInstalled(packageName: String?): Boolean {
    val packageManager = packageManager
    val intent = packageManager.getLaunchIntentForPackage(packageName!!) ?: return false
    val list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
    return !list.isEmpty()
}

/*fun Context.isPackageInstalled(pkgName: String): Boolean {
    return try {
        packageManager.getPackageInfo(pkgName, 0)
        true
    } catch (e: Exception) {
        false
    }
}*/

// format day bits to strings like "Mon, Tue, Wed"
fun Context.getSelectedDaysString(bitMask: Int): String {
    val dayBits = arrayListOf(MONDAY_BIT, TUESDAY_BIT, WEDNESDAY_BIT, THURSDAY_BIT, FRIDAY_BIT, SATURDAY_BIT, SUNDAY_BIT)
    val weekDays = resources.getStringArray(R.array.week_days_short).toList() as ArrayList<String>

    if (baseConfig.isSundayFirst) {
        dayBits.moveLastItemToFront()
        weekDays.moveLastItemToFront()
    }

    var days = ""
    dayBits.forEachIndexed { index, bit ->
        if (bitMask and bit != 0) {
            days += "${weekDays[index]}, "
        }
    }
    return days.trim().trimEnd(',')
}

fun Context.formatMinutesToTimeString(totalMinutes: Int) = formatSecondsToTimeString(totalMinutes * 60)

fun Context.formatSecondsToTimeString(totalSeconds: Int): String {
    val days = totalSeconds / DAY_SECONDS
    val hours = (totalSeconds % DAY_SECONDS) / HOUR_SECONDS
    val minutes = (totalSeconds % HOUR_SECONDS) / MINUTE_SECONDS
    val seconds = totalSeconds % MINUTE_SECONDS
    val timesString = StringBuilder()
    if (days > 0) {
        val daysString = String.format(resources.getQuantityString(R.plurals.days, days, days))
        timesString.append("$daysString, ")
    }

    if (hours > 0) {
        val hoursString = String.format(resources.getQuantityString(R.plurals.hours, hours, hours))
        timesString.append("$hoursString, ")
    }

    if (minutes > 0) {
        val minutesString = String.format(resources.getQuantityString(R.plurals.minutes, minutes, minutes))
        timesString.append("$minutesString, ")
    }

    if (seconds > 0) {
        val secondsString = String.format(resources.getQuantityString(R.plurals.seconds, seconds, seconds))
        timesString.append(secondsString)
    }

    var result = timesString.toString().trim().trimEnd(',')
    if (result.isEmpty()) {
        result = String.format(resources.getQuantityString(R.plurals.minutes, 0, 0))
    }
    return result
}

fun Context.formatMinutesToShortTimeString(totalMinutes: Int) = formatSecondsToShortTimeString(totalMinutes * 60)

fun Context.formatSecondsToShortTimeString(totalSeconds: Int): String {
    val days = totalSeconds / DAY_SECONDS
    val hours = (totalSeconds % DAY_SECONDS) / HOUR_SECONDS
    val minutes = (totalSeconds % HOUR_SECONDS) / MINUTE_SECONDS
    val seconds = totalSeconds % MINUTE_SECONDS
    val timesString = StringBuilder()
    if (days > 0) {
        val daysString = String.format(resources.getString(R.string.days_letter), days)
        timesString.append("$daysString ")
    }

    if (hours > 0) {
        val hoursString = String.format(resources.getString(R.string.hours_letter), hours)
        timesString.append("$hoursString ")
    }

    if (minutes > 0) {
        val minutesString = String.format(resources.getString(R.string.minutes_letter), minutes)
        timesString.append("$minutesString ")
    }

    if (seconds > 0) {
        val secondsString = String.format(resources.getString(R.string.seconds_letter), seconds)
        timesString.append(secondsString)
    }

    var result = timesString.toString().trim()
    if (result.isEmpty()) {
        result = String.format(resources.getString(R.string.minutes_letter), 0)
    }
    return result
}

fun Context.getFormattedMinutes(minutes: Int, showBefore: Boolean = true) = getFormattedSeconds(if (minutes == -1) minutes else minutes * 60, showBefore)

fun Context.getFormattedSeconds(seconds: Int, showBefore: Boolean = true) = when (seconds) {
    -1 -> getString(R.string.no_reminder)
    0 -> getString(R.string.at_start)
    else -> {
        when {
            seconds < 0 && seconds > -60 * 60 * 24 -> {
                val minutes = -seconds / 60
                getString(R.string.during_day_at, minutes / 60, minutes % 60)
            }

            seconds % YEAR_SECONDS == 0 -> {
                val base = if (showBefore) R.plurals.years_before else R.plurals.by_years
                resources.getQuantityString(base, seconds / YEAR_SECONDS, seconds / YEAR_SECONDS)
            }

            seconds % MONTH_SECONDS == 0 -> {
                val base = if (showBefore) R.plurals.months_before else R.plurals.by_months
                resources.getQuantityString(base, seconds / MONTH_SECONDS, seconds / MONTH_SECONDS)
            }

            seconds % WEEK_SECONDS == 0 -> {
                val base = if (showBefore) R.plurals.weeks_before else R.plurals.by_weeks
                resources.getQuantityString(base, seconds / WEEK_SECONDS, seconds / WEEK_SECONDS)
            }

            seconds % DAY_SECONDS == 0 -> {
                val base = if (showBefore) R.plurals.days_before else R.plurals.by_days
                resources.getQuantityString(base, seconds / DAY_SECONDS, seconds / DAY_SECONDS)
            }

            seconds % HOUR_SECONDS == 0 -> {
                val base = if (showBefore) R.plurals.hours_before else R.plurals.by_hours
                resources.getQuantityString(base, seconds / HOUR_SECONDS, seconds / HOUR_SECONDS)
            }

            seconds % MINUTE_SECONDS == 0 -> {
                val base = if (showBefore) R.plurals.minutes_before else R.plurals.by_minutes
                resources.getQuantityString(base, seconds / MINUTE_SECONDS, seconds / MINUTE_SECONDS)
            }

            else -> {
                val base = if (showBefore) R.plurals.seconds_before else R.plurals.by_seconds
                resources.getQuantityString(base, seconds, seconds)
            }
        }
    }
}

fun Context.getDefaultAlarmTitle(type: Int): String {
    val alarmString = getString(R.string.alarm)
    return try {
        RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(type))?.getTitle(this) ?: alarmString
    } catch (_: Exception) {
        alarmString
    }
}

fun Context.getDefaultAlarmSound(type: Int) = AlarmSound(0, getDefaultAlarmTitle(type), RingtoneManager.getDefaultUri(type).toString())

fun Context.grantReadUriPermission(uriString: String) {
    try {
        // ensure custom reminder sounds play well
        grantUriPermission(
            "com.android.systemui",
            uriString.toUri(),
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    } catch (_: Exception) {
    }
}

fun Context.storeNewYourAlarmSound(resultData: Intent): AlarmSound {
    val uri = resultData.data
    var filename = getFilenameFromUri(uri!!)
    if (filename.isEmpty()) {
        filename = getString(R.string.alarm)
    }

    val token = object : TypeToken<ArrayList<AlarmSound>>() {}.type
    val yourAlarmSounds = Gson().fromJson<ArrayList<AlarmSound>>(baseConfig.yourAlarmSounds, token)
        ?: ArrayList()
    val newAlarmSoundId = (yourAlarmSounds.maxByOrNull { it.id }?.id ?: YOUR_ALARM_SOUNDS_MIN_ID) + 1
    val newAlarmSound = AlarmSound(newAlarmSoundId, filename, uri.toString())
    if (yourAlarmSounds.firstOrNull { it.uri == uri.toString() } == null) {
        yourAlarmSounds.add(newAlarmSound)
    }

    baseConfig.yourAlarmSounds = Gson().toJson(yourAlarmSounds)

    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    contentResolver.takePersistableUriPermission(uri, takeFlags)

    return newAlarmSound
}

fun Context.saveImageRotation(path: String, degrees: Int): Boolean {
    return if (!needsStupidWritePermissions(path)) {
        saveExifRotation(ExifInterface(path), degrees)
        true
    } else {
        val documentFile = getSomeDocumentFile(path)
        if (documentFile != null) {
            val parcelFileDescriptor =
                contentResolver.openFileDescriptor(documentFile.uri, "rw")
            val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
            saveExifRotation(ExifInterface(fileDescriptor), degrees)
            true
        } else {
            false
        }
    }
}

fun saveExifRotation(exif: ExifInterface, degrees: Int) {
    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    val orientationDegrees = (orientation.degreesFromOrientation() + degrees) % 360
    exif.setAttribute(ExifInterface.TAG_ORIENTATION, orientationDegrees.orientationFromDegrees())
    exif.saveAttributes()
}

fun Context.getLaunchIntent() = packageManager.getLaunchIntentForPackage(baseConfig.appId)

fun Context.getCanAppBeUpgraded() = proPackages.contains(baseConfig.appId.removeSuffix(".debug").removePrefix("com.goodwy."))

fun Context.getStoreUrl() = "https://play.google.com/store/apps/details?id=${packageName.removeSuffix(".debug")}"

fun Context.getRuStoreUrl() = "https://www.rustore.ru/catalog/app/${packageName.removeSuffix(".debug")}"

fun Context.getTimeFormat() = if (baseConfig.use24HourFormat) TIME_FORMAT_24 else TIME_FORMAT_12

fun Context.getTimeFormatWithSeconds() = if (baseConfig.use24HourFormat) {
    TIME_FORMAT_24_WITH_SECS
} else {
    TIME_FORMAT_12_WITH_SECS
}

fun Context.getResolution(path: String): Point? {
    return if (path.isImageFast() || path.isImageSlow()) {
        getImageResolution(path)
    } else if (path.isVideoFast() || path.isVideoSlow()) {
        getVideoResolution(path)
    } else {
        null
    }
}

fun Context.getImageResolution(path: String): Point? {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    if (isRestrictedSAFOnlyRoot(path)) {
        BitmapFactory.decodeStream(contentResolver.openInputStream(getAndroidSAFUri(path)), null, options)
    } else {
        BitmapFactory.decodeFile(path, options)
    }

    val width = options.outWidth
    val height = options.outHeight
    return if (width > 0 && height > 0) {
        Point(options.outWidth, options.outHeight)
    } else {
        null
    }
}

fun Context.getVideoResolution(path: String): Point? {
    var point = try {
        val retriever = MediaMetadataRetriever()
        if (isRestrictedSAFOnlyRoot(path)) {
            retriever.setDataSource(this, getAndroidSAFUri(path))
        } else {
            retriever.setDataSource(path)
        }

        val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!.toInt()
        val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!.toInt()
        Point(width, height)
    } catch (_: Exception) {
        null
    }

    if (point == null && path.startsWith("content://", true)) {
        try {
            val fd = contentResolver.openFileDescriptor(path.toUri(), "r")?.fileDescriptor
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(fd)
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!.toInt()
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!.toInt()
            point = Point(width, height)
        } catch (_: Exception) {
        }
    }

    return point
}

fun Context.getDuration(path: String): Int? {
    val projection = arrayOf(
        MediaColumns.DURATION
    )

    val uri = getFileUri(path)
    val selection = if (path.startsWith("content://")) "${BaseColumns._ID} = ?" else "${MediaColumns.DATA} = ?"
    val selectionArgs = if (path.startsWith("content://")) arrayOf(path.substringAfterLast("/")) else arrayOf(path)

    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return (cursor.getIntValue(MediaColumns.DURATION) / 1000.toDouble()).roundToInt()
            }
        }
    } catch (_: Exception) {
    }

    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toInt() / 1000f).roundToInt()
    } catch (_: Exception) {
        null
    }
}

fun Context.getTitle(path: String): String? {
    val projection = arrayOf(
        MediaColumns.TITLE
    )

    val uri = getFileUri(path)
    val selection = if (path.startsWith("content://")) "${BaseColumns._ID} = ?" else "${MediaColumns.DATA} = ?"
    val selectionArgs = if (path.startsWith("content://")) arrayOf(path.substringAfterLast("/")) else arrayOf(path)

    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getStringValue(MediaColumns.TITLE)
            }
        }
    } catch (_: Exception) {
    }

    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
    } catch (_: Exception) {
        null
    }
}

fun Context.getArtist(path: String): String? {
    val projection = arrayOf(
        Audio.Media.ARTIST
    )

    val uri = getFileUri(path)
    val selection = if (path.startsWith("content://")) "${BaseColumns._ID} = ?" else "${MediaColumns.DATA} = ?"
    val selectionArgs = if (path.startsWith("content://")) arrayOf(path.substringAfterLast("/")) else arrayOf(path)

    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getStringValue(Audio.Media.ARTIST)
            }
        }
    } catch (_: Exception) {
    }

    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
    } catch (_: Exception) {
        null
    }
}

fun Context.getAlbum(path: String): String? {
    val projection = arrayOf(
        Audio.Media.ALBUM
    )

    val uri = getFileUri(path)
    val selection = if (path.startsWith("content://")) "${BaseColumns._ID} = ?" else "${MediaColumns.DATA} = ?"
    val selectionArgs = if (path.startsWith("content://")) arrayOf(path.substringAfterLast("/")) else arrayOf(path)

    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getStringValue(Audio.Media.ALBUM)
            }
        }
    } catch (_: Exception) {
    }

    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
    } catch (_: Exception) {
        null
    }
}

fun Context.getMediaStoreLastModified(path: String): Long {
    val projection = arrayOf(
        MediaColumns.DATE_MODIFIED
    )

    val uri = getFileUri(path)
    val selection = "${BaseColumns._ID} = ?"
    val selectionArgs = arrayOf(path.substringAfterLast("/"))

    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getLongValue(MediaColumns.DATE_MODIFIED) * 1000
            }
        }
    } catch (_: Exception) {
    }
    return 0
}

fun Context.getStringsPackageName() = getString(R.string.package_name)

fun Context.getFontSizeText() = getString(
    when (baseConfig.fontSize) {
        FONT_SIZE_SMALL -> R.string.small
        FONT_SIZE_MEDIUM -> R.string.medium
        FONT_SIZE_LARGE -> R.string.large
        else -> R.string.extra_large
    }
)

fun Context.getTextSize() = when (baseConfig.fontSize) {
    FONT_SIZE_SMALL -> resources.getDimension(R.dimen.normal_text_size)
    FONT_SIZE_MEDIUM -> resources.getDimension(R.dimen.bigger_text_size)
    FONT_SIZE_LARGE -> resources.getDimension(R.dimen.big_text_size)
    else -> resources.getDimension(R.dimen.extra_big_text_size)
}

fun Context.getTextSizeSmall() = when (baseConfig.fontSize) {
    FONT_SIZE_SMALL -> resources.getDimension(R.dimen.small_text_size)
    FONT_SIZE_MEDIUM -> resources.getDimension(R.dimen.smaller_text_size)
    FONT_SIZE_LARGE -> resources.getDimension(R.dimen.bigger_text_size)
    else -> resources.getDimension(R.dimen.big_text_size)
}

val Context.telecomManager: TelecomManager get() = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
val Context.windowManager: WindowManager get() = getSystemService(Context.WINDOW_SERVICE) as WindowManager
val Context.notificationManager: NotificationManager get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
val Context.shortcutManager: ShortcutManager get() = getSystemService(ShortcutManager::class.java) as ShortcutManager

val Context.portrait get() = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

val Context.usableScreenSize: Point
    get() {
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        return size
    }

val Context.realScreenSize: Point
    get() {
        val size = Point()
        windowManager.defaultDisplay.getRealSize(size)
        return size
    }

// we need the Default Dialer functionality only in Simple Dialer and in Simple Contacts for now
fun Context.isDefaultDialer(): Boolean {
    return if (!packageName.startsWith("com.goodwy.contacts") && !packageName.startsWith("com.goodwy.dialer") &&
        !packageName.startsWith("dev.goodwy.contacts") && !packageName.startsWith("dev.goodwy.dialer")) {
        true
    } else if ((packageName.startsWith("com.goodwy.contacts") || packageName.startsWith("com.goodwy.dialer") ||
            packageName.startsWith("dev.goodwy.contacts") || packageName.startsWith("dev.goodwy.dialer")) && isQPlus()) {
        val roleManager = getSystemService(RoleManager::class.java)
        roleManager!!.isRoleAvailable(RoleManager.ROLE_DIALER) && roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
    } else {
        telecomManager.defaultDialerPackage == packageName
    }
}

fun Context.getContactsHasMap(withComparableNumbers: Boolean = false, callback: (HashMap<String, String>) -> Unit) {
    ContactsHelper(this).getContacts(showOnlyContactsWithNumbers = true) { contactList ->
        val privateContacts: HashMap<String, String> = HashMap()
        for (contact in contactList) {
            for (phoneNumber in contact.phoneNumbers) {
                var number = PhoneNumberUtils.stripSeparators(phoneNumber.value)
                if (withComparableNumbers) {
                    number = number.trimToComparableNumber()
                }

                privateContacts[number] = contact.name
            }
        }
        callback(privateContacts)
    }
}

fun Context.getBlockedNumbersWithContact(callback: (ArrayList<BlockedNumber>) -> Unit) {
    getContactsHasMap(true) { contacts ->
        val blockedNumbers = ArrayList<BlockedNumber>()
        if (!isDefaultDialer()) {
            callback(blockedNumbers)
        }

        val uri = BlockedNumbers.CONTENT_URI
        val projection = arrayOf(
            BlockedNumbers.COLUMN_ID,
            BlockedNumbers.COLUMN_ORIGINAL_NUMBER,
            BlockedNumbers.COLUMN_E164_NUMBER,
        )

        queryCursor(uri, projection) { cursor ->
            val id = cursor.getLongValue(BlockedNumbers.COLUMN_ID)
            val number = cursor.getStringValue(BlockedNumbers.COLUMN_ORIGINAL_NUMBER) ?: ""
            val normalizedNumber = cursor.getStringValue(BlockedNumbers.COLUMN_E164_NUMBER) ?: number
            val comparableNumber = normalizedNumber.trimToComparableNumber()

            val contactName = contacts[comparableNumber]
            val blockedNumber = BlockedNumber(id, number, normalizedNumber, comparableNumber, contactName)
            blockedNumbers.add(blockedNumber)
        }

        val blockedNumbersPair = blockedNumbers.partition { it.contactName != null }
        val blockedNumbersWithNameSorted = blockedNumbersPair.first.sortedBy { it.contactName }
        val blockedNumbersNoNameSorted = blockedNumbersPair.second.sortedBy { it.number }

        callback(ArrayList(blockedNumbersWithNameSorted + blockedNumbersNoNameSorted))
    }
}

fun Context.getBlockedNumbers(): ArrayList<BlockedNumber> {
    val blockedNumbers = ArrayList<BlockedNumber>()
    if (!isDefaultDialer()) {
        return blockedNumbers
    }

    val uri = BlockedNumbers.CONTENT_URI
    val projection = arrayOf(
        BlockedNumbers.COLUMN_ID,
        BlockedNumbers.COLUMN_ORIGINAL_NUMBER,
        BlockedNumbers.COLUMN_E164_NUMBER
    )

    queryCursor(uri, projection) { cursor ->
        val id = cursor.getLongValue(BlockedNumbers.COLUMN_ID)
        val number = cursor.getStringValue(BlockedNumbers.COLUMN_ORIGINAL_NUMBER) ?: ""
        val normalizedNumber = cursor.getStringValue(BlockedNumbers.COLUMN_E164_NUMBER) ?: number
        val comparableNumber = normalizedNumber.trimToComparableNumber()
        val blockedNumber = BlockedNumber(id, number, normalizedNumber, comparableNumber)
        blockedNumbers.add(blockedNumber)
    }

    return blockedNumbers
}

fun Context.addBlockedNumber(number: String): Boolean {
    ContentValues().apply {
        put(BlockedNumbers.COLUMN_ORIGINAL_NUMBER, number)
        if (number.isPhoneNumber()) {
            put(BlockedNumbers.COLUMN_E164_NUMBER, PhoneNumberUtils.normalizeNumber(number))
        }
        try {
            contentResolver.insert(BlockedNumbers.CONTENT_URI, this)
        } catch (e: Exception) {
            showErrorToast(e)
            return false
        }
    }
    return true
}

fun Context.deleteBlockedNumber(number: String): Boolean {
    val selection = "${BlockedNumbers.COLUMN_ORIGINAL_NUMBER} = ?"
    val selectionArgs = arrayOf(number)

    return if (isNumberBlocked(number)) {
        val deletedRowCount = contentResolver.delete(BlockedNumbers.CONTENT_URI, selection, selectionArgs)

        deletedRowCount > 0
    } else {
        true
    }
}

fun Context.isNumberBlocked(number: String, blockedNumbers: ArrayList<BlockedNumber> = getBlockedNumbers()): Boolean {
    val numberToCompare = number.trimToComparableNumber()

    return blockedNumbers.any {
        numberToCompare == it.numberToCompare ||
            numberToCompare == it.number ||
            PhoneNumberUtils.stripSeparators(number) == it.number
    } || isNumberBlockedByPattern(number, blockedNumbers)
}

fun Context.isNumberBlockedByPattern(number: String, blockedNumbers: ArrayList<BlockedNumber> = getBlockedNumbers()): Boolean {
    for (blockedNumber in blockedNumbers) {
        val num = blockedNumber.number
        if (num.isBlockedNumberPattern()) {
            val pattern = num.replace("+", "\\+").replace("*", ".*")
            if (number.matches(pattern.toRegex())) {
                return true
            }
        }
    }
    return false
}

fun Context.copyToClipboard(text: String) {
    val clip = ClipData.newPlainText(getString(R.string.simple_commons), text)
    (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
    val toastText = String.format(getString(R.string.value_copied_to_clipboard_show), text)
    toast(toastText)
}

fun Context.getPhoneNumberTypeText(type: Int, label: String): String {
    return if (type == BaseTypes.TYPE_CUSTOM) {
        label
    } else {
        getString(
            when (type) {
                Phone.TYPE_MOBILE -> R.string.mobile
                Phone.TYPE_HOME -> R.string.home
                Phone.TYPE_WORK -> R.string.work
                Phone.TYPE_MAIN -> R.string.main_number
                Phone.TYPE_FAX_WORK -> R.string.work_fax
                Phone.TYPE_FAX_HOME -> R.string.home_fax
                Phone.TYPE_PAGER -> R.string.pager
                else -> R.string.other
            }
        )
    }
}

fun Context.updateBottomTabItemColors(view: View?, isActive: Boolean, drawableId: Int? = null) {
    val color = if (isActive) {
        getProperPrimaryColor()
    } else {
        getProperTextColor().adjustAlpha(0.6f)
    }

    if (drawableId != null) {
        val drawable = ResourcesCompat.getDrawable(resources, drawableId, theme)
        view?.findViewById<ImageView>(R.id.tab_item_icon)?.setImageDrawable(drawable)
    }

    view?.findViewById<ImageView>(R.id.tab_item_icon)?.applyColorFilter(color)
    view?.findViewById<TextView>(R.id.tab_item_label)?.setTextColor(color)
}

fun Context.sendEmailIntent(recipient: String) {
    Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.fromParts(KEY_MAILTO, recipient, null)
        launchActivityIntent(this)
    }
}

fun Context.openNotificationSettings() {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
    intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    startActivity(intent)
}

fun Context.getTempFile(folderName: String, filename: String): File? {
    val folder = File(cacheDir, folderName)
    if (!folder.exists()) {
        if (!folder.mkdir()) {
            toast(R.string.unknown_error_occurred)
            return null
        }
    }

    return File(folder, filename)
}

fun Context.openDeviceSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    }

    try {
        startActivity(intent)
    } catch (e: Exception) {
        showErrorToast(e)
    }
}

@RequiresApi(Build.VERSION_CODES.S)
fun Context.openRequestExactAlarmSettings(appId: String) {
    if (isSPlus()) {
        val uri = Uri.fromParts("package", appId, null)
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        intent.data = uri
        startActivity(intent)
    }
}

fun Context.canUseFullScreenIntent(): Boolean {
    return !isUpsideDownCakePlus() || notificationManager.canUseFullScreenIntent()
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun Context.openFullScreenIntentSettings(appId: String) {
    if (isUpsideDownCakePlus()) {
        val uri = Uri.fromParts("package", appId, null)
        val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
        intent.data = uri
        startActivity(intent)
    }
}

fun Context.getDayOfWeekString(dayOfWeek: Int): String {
    val dayOfWeekResId = when (dayOfWeek) {
        DateTimeConstants.MONDAY -> R.string.monday
        DateTimeConstants.TUESDAY -> R.string.tuesday
        DateTimeConstants.WEDNESDAY -> R.string.wednesday
        DateTimeConstants.THURSDAY -> R.string.thursday
        DateTimeConstants.FRIDAY -> R.string.friday
        DateTimeConstants.SATURDAY -> R.string.saturday
        DateTimeConstants.SUNDAY -> R.string.sunday
        else -> throw IllegalArgumentException("Invalid day: $dayOfWeek")
    }

    return getString(dayOfWeekResId)
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun Context.formatWithDeprecatedBadge(
    @StringRes labelRes: Int,
    vararg labelArgs: Any
): String {
    val label = if (labelArgs.isEmpty()) {
        getString(labelRes)
    } else {
        getString(labelRes, *labelArgs)
    }

    val badge = BidiFormatter.getInstance().unicodeWrap(getString(R.string.badge_deprecated))
    return getString(R.string.label_with_badge, label, badge)
}

//Goodwy
fun Context.getEmailTypeText(type: Int, label: String): String {
    return if (type == BaseTypes.TYPE_CUSTOM) {
        label
    } else {
        getString(
            when (type) {
                Email.TYPE_HOME -> R.string.home
                Email.TYPE_WORK -> R.string.work
                Email.TYPE_MOBILE -> R.string.mobile
                else -> R.string.other
            }
        )
    }
}

fun Context.getRelationTypeText(type: Int, label: String): String {
    return if (type == BaseTypes.TYPE_CUSTOM) {
        label
    } else {
        getString(
            when (type) {
                // Relation.TYPE_CUSTOM   -> stringsR.string.custom
                Relation.TYPE_ASSISTANT   -> stringsR.string.relation_assistant_g
                Relation.TYPE_BROTHER     -> stringsR.string.relation_brother_g
                Relation.TYPE_CHILD       -> stringsR.string.relation_child_g
                Relation.TYPE_DOMESTIC_PARTNER -> stringsR.string.relation_domestic_partner_g
                Relation.TYPE_FATHER      -> stringsR.string.relation_father_g
                Relation.TYPE_FRIEND      -> stringsR.string.relation_friend_g
                Relation.TYPE_MANAGER     -> stringsR.string.relation_manager_g
                Relation.TYPE_MOTHER      -> stringsR.string.relation_mother_g
                Relation.TYPE_PARENT      -> stringsR.string.relation_parent_g
                Relation.TYPE_PARTNER     -> stringsR.string.relation_partner_g
                Relation.TYPE_REFERRED_BY -> stringsR.string.relation_referred_by_g
                Relation.TYPE_RELATIVE    -> stringsR.string.relation_relative_g
                Relation.TYPE_SISTER      -> stringsR.string.relation_sister_g
                Relation.TYPE_SPOUSE      -> stringsR.string.relation_spouse_g

                // Relation types defined in vCard 4.0
                ContactRelation.TYPE_CONTACT -> stringsR.string.relation_contact_g
                ContactRelation.TYPE_ACQUAINTANCE -> stringsR.string.relation_acquaintance_g
                // ContactRelation.TYPE_FRIEND -> stringsR.string.relation_friend
                ContactRelation.TYPE_MET -> stringsR.string.relation_met_g
                ContactRelation.TYPE_CO_WORKER -> stringsR.string.relation_co_worker_g
                ContactRelation.TYPE_COLLEAGUE -> stringsR.string.relation_colleague_g
                ContactRelation.TYPE_CO_RESIDENT -> stringsR.string.relation_co_resident_g
                ContactRelation.TYPE_NEIGHBOR -> stringsR.string.relation_neighbor_g
                // ContactRelation.TYPE_CHILD -> stringsR.string.relation_child
                // ContactRelation.TYPE_PARENT -> stringsR.string.relation_parent
                ContactRelation.TYPE_SIBLING -> stringsR.string.relation_sibling_g
                // ContactRelation.TYPE_SPOUSE -> stringsR.string.relation_spouse
                ContactRelation.TYPE_KIN -> stringsR.string.relation_kin_g
                ContactRelation.TYPE_MUSE -> stringsR.string.relation_muse_g
                ContactRelation.TYPE_CRUSH -> stringsR.string.relation_crush_g
                ContactRelation.TYPE_DATE -> stringsR.string.relation_date_g
                ContactRelation.TYPE_SWEETHEART -> stringsR.string.relation_sweetheart_g
                ContactRelation.TYPE_ME -> stringsR.string.relation_me_g
                ContactRelation.TYPE_AGENT -> stringsR.string.relation_agent_g
                ContactRelation.TYPE_EMERGENCY -> stringsR.string.relation_emergency_g

                ContactRelation.TYPE_SUPERIOR -> stringsR.string.relation_superior_g
                ContactRelation.TYPE_SUBORDINATE -> stringsR.string.relation_subordinate_g
                ContactRelation.TYPE_HUSBAND -> stringsR.string.relation_husband_g
                ContactRelation.TYPE_WIFE -> stringsR.string.relation_wife_g
                ContactRelation.TYPE_SON -> stringsR.string.relation_son_g
                ContactRelation.TYPE_DAUGHTER -> stringsR.string.relation_daughter_g
                ContactRelation.TYPE_GRANDPARENT -> stringsR.string.relation_grandparent_g
                ContactRelation.TYPE_GRANDFATHER -> stringsR.string.relation_grandfather_g
                ContactRelation.TYPE_GRANDMOTHER -> stringsR.string.relation_grandmother_g
                ContactRelation.TYPE_GRANDCHILD -> stringsR.string.relation_grandchild_g
                ContactRelation.TYPE_GRANDSON -> stringsR.string.relation_grandson_g
                ContactRelation.TYPE_GRANDDAUGHTER -> stringsR.string.relation_granddaughter_g
                ContactRelation.TYPE_UNCLE -> stringsR.string.relation_uncle_g
                ContactRelation.TYPE_AUNT -> stringsR.string.relation_aunt_g
                ContactRelation.TYPE_NEPHEW -> stringsR.string.relation_nephew_g
                ContactRelation.TYPE_NIECE -> stringsR.string.relation_niece_g
                ContactRelation.TYPE_FATHER_IN_LAW -> stringsR.string.relation_father_in_law_g
                ContactRelation.TYPE_MOTHER_IN_LAW -> stringsR.string.relation_mother_in_law_g
                ContactRelation.TYPE_SON_IN_LAW -> stringsR.string.relation_son_in_law_g
                ContactRelation.TYPE_DAUGHTER_IN_LAW -> stringsR.string.relation_daughter_in_law_g
                ContactRelation.TYPE_BROTHER_IN_LAW -> stringsR.string.relation_brother_in_law_g
                ContactRelation.TYPE_SISTER_IN_LAW -> stringsR.string.relation_sister_in_law_g

                else -> R.string.other
            }
        )
    }
}

fun Context.getEventTypeText(type: Int, label: String): String {
    return if (type == BaseTypes.TYPE_CUSTOM) {
        label
    } else {
        getString(
            when (type) {
                Event.TYPE_ANNIVERSARY -> R.string.anniversary
                Event.TYPE_BIRTHDAY -> R.string.birthday
                CUSTOM_EVENT_TYPE_DEATH -> stringsR.string.death
                else -> R.string.other
            }
        )
    }
}


fun Context.getTextFromClipboard(): CharSequence? {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = clipboard.primaryClip
    return if (clip != null && clip.itemCount > 0) {
        clip.getItemAt(0).coerceToText(this)
    } else null
}

fun Context.getScreenSlideAnimationText() = getString(
    when (baseConfig.screenSlideAnimation) {
        1 -> stringsR.string.screen_slide_animation_zoomout
        2 -> stringsR.string.screen_slide_animation_depth
        else -> R.string.no
    }
)

fun Context.getNavigationBarStyleText() = getString(
    when (baseConfig.bottomNavigationBar) {
        true -> stringsR.string.bottom
        else -> stringsR.string.top
    }
)

fun Context.startCallPendingIntent(recipient: String, key: String = ""): PendingIntent {
    return PendingIntent.getActivity(
        this,
        0,
        Intent(Intent.ACTION_CALL, Uri.fromParts("tel", recipient, null))
            .putExtra(IS_RIGHT_APP, key),
        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

fun Context.sendSMSPendingIntent(recipient: String): PendingIntent {
    return PendingIntent.getActivity(this, 0,
        Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", recipient, null)), PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

fun Context.getLetterBackgroundColors(): ArrayList<Long> {
    return when (baseConfig.contactColorList) {
        LBC_ORIGINAL -> letterBackgroundColors
        LBC_IOS -> letterBackgroundColorsIOS
        LBC_ARC -> letterBackgroundColorsArc
        else -> letterBackgroundColorsAndroid
    }
}

suspend fun isMiUi(): Boolean {
    val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
    val isXiaomi = manufacturer.contains(Regex(pattern = "xiaomi|redmi|poco"))

    if (!isXiaomi) {
        return false
    }

    return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"))
}

suspend fun isEmui(): Boolean {
    // Quick check by manufacturer
    val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
    val isHuawei = manufacturer.contains(Regex(pattern = "huawei|honor"))

    if (!isHuawei) {
        return false
    }

    // Accurate verification via system property
    return !TextUtils.isEmpty(getSystemProperty("ro.build.version.emui"))
}

suspend fun getSystemProperty(propName: String): String? = withContext(Dispatchers.IO) {
    val line: String
    var input: BufferedReader? = null
    try {
        val process = Runtime.getRuntime().exec("getprop $propName")
        // Add a timeout to avoid freezes
        if (!process.waitFor(3, TimeUnit.SECONDS)) {
            process.destroy()
            return@withContext null
        }
        input = BufferedReader(InputStreamReader(process.inputStream), 1024)
        line = input.readLine()
        input.close()
    } catch (_: IOException) {
        return@withContext null
    } finally {
        if (input != null) {
            try {
                input.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    return@withContext line
}

fun Context.isPlayStoreInstalled(): Boolean {
    return isPackageInstalled("com.android.vending")
        || isPackageInstalled("com.google.market")
}

fun Context.isRuStoreInstalled(): Boolean {
    return isPackageInstalled("ru.vk.store")
}

fun Context.isPro() =
    if (packageName.startsWith("dev.")) {
        if (resources.getBoolean(R.bool.is_foss)) baseConfig.isProNoGP
        else baseConfig.isPro || baseConfig.isProSubs || baseConfig.isProRuStore
    } else {
        baseConfig.isPro || baseConfig.isProSubs || baseConfig.isProRuStore ||
            (resources.getBoolean(R.bool.using_no_gp) && baseConfig.isProNoGP)
    }

fun Context.isCollection(): Boolean {
    val prefix = appPrefix()
    return isPackageInstalled(prefix + "goodwy.dialer")
        && isPackageInstalled(prefix + "goodwy.contacts")
        && isPackageInstalled(prefix + "goodwy.smsmessenger")
        && isPackageInstalled(prefix + "goodwy.gallery")
        && isPackageInstalled(prefix + "goodwy.audiobooklite")
        && isPackageInstalled(prefix + "goodwy.filemanager")
        && isPackageInstalled(prefix + "goodwy.keyboard")
        && isPackageInstalled(prefix + "goodwy.calendar")
        && isPackageInstalled(prefix + "goodwy.voicerecorderfree")
}

fun Context.appPrefix(): String = if (packageName.startsWith("dev.goodwy")) "dev." else "com."

fun Context.isTalkBackOn(): Boolean {
    val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    if (am.isEnabled) {
        val serviceInfoList =
            am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN)
        if (serviceInfoList.isNotEmpty())
            return true
    }
    return false
}

fun Context.sysLocale(): Locale? {
    val config = this.resources.configuration
    return getSystemLocale(config)
}

private fun getSystemLocale(config: Configuration) = config.locales.get(0)

fun Context.googlePlayDevUrlRes(): Int {
    return when {
        packageName.startsWith("dev.goodwy") -> R.string.google_play_dev_url
        packageName.startsWith("com.goodwy") -> R.string.google_play_dev_url_old
        else -> R.string.google_play_dev_url_fake
    }
}

fun Context.googlePlayDevUrlString(): String {
    return getString(googlePlayDevUrlRes())
}

fun Context.myMailRes(): Int {
    return when {
        packageName.startsWith("dev.goodwy") -> R.string.my_email
        packageName.startsWith("com.goodwy") -> R.string.my_email_old
        else -> R.string.my_fake_email
    }
}

fun Context.getMyMailString(): String {
    return getString(myMailRes())
}

fun Context.getDividerColor(): Int {
    return if (isDarkTheme() && (isSystemInDarkMode() || isAutoTheme())) {
        "#444444".toColorInt()
    } else {
        "#E0E0E0".toColorInt()
    }
}
