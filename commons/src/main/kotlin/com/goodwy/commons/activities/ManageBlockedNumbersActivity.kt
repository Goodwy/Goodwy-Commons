package com.goodwy.commons.activities

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.goodwy.commons.R
import com.goodwy.commons.compose.alert_dialog.rememberAlertDialogState
import com.goodwy.commons.compose.extensions.enableEdgeToEdgeSimple
import com.goodwy.commons.compose.extensions.onEventValue
import com.goodwy.commons.compose.screens.ManageBlockedNumbersScreen
import com.goodwy.commons.compose.theme.AppThemeSurface
import com.goodwy.commons.dialogs.AddOrEditBlockedNumberAlertDialog
import com.goodwy.commons.dialogs.ExportBlockedNumbersDialog
import com.goodwy.commons.dialogs.RadioGroupAlertDialog
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.*
import com.goodwy.commons.models.BlockedNumber
import com.goodwy.commons.models.RadioItem
import java.io.FileOutputStream
import java.io.OutputStream
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageBlockedNumbersActivity : BaseSimpleActivity() {
    companion object {
        const val SET_DEFAULT_CALLER_ID = "SET_DEFAULT_CALLER_ID"
    }

    private val config by lazy { baseConfig }

    private val blockedNumberMimeTypes = buildList {
        add("text/plain")
        if (!isQPlus()) {
            add("application/octet-stream")
        }
    }.toTypedArray()

    private val openDocument = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            tryImportBlockedNumbersFromFile(uri)
        }
    }

    private val createDocument = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            val outputStream = contentResolver.openOutputStream(uri)
            exportBlockedNumbersTo(outputStream)
        }
    }

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun getRepositoryName() = null

    private val manageBlockedNumbersViewModel by viewModels<ManageBlockedNumbersViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.action == SET_DEFAULT_CALLER_ID) {
            maybeSetDefaultCallerIdApp()
        }

        enableEdgeToEdgeSimple()
        setContent {
            val context = LocalContext.current
            val blockedNumbers by manageBlockedNumbersViewModel.blockedNumbers.collectAsStateWithLifecycle()
            LaunchedEffect(blockedNumbers) {
                if (blockedNumbers?.any { blockedNumber -> blockedNumber.number.isBlockedNumberPattern() } == true) {
                    maybeSetDefaultCallerIdApp()
                }
            }
            val isBlockingHiddenNumbers by config.isBlockingHiddenNumbers.collectAsStateWithLifecycle(initialValue = config.blockHiddenNumbers)
            val isBlockingUnknownNumbers by config.isBlockingUnknownNumbers.collectAsStateWithLifecycle(initialValue = config.blockUnknownNumbers)
            val showCheckmarksOnSwitches by config.showCheckmarksOnSwitchesFlow.collectAsStateWithLifecycle(initialValue = config.showCheckmarksOnSwitches)
            val isTopAppBarColorIcon by config.isTopAppBarColorIcon.collectAsStateWithLifecycle(initialValue = config.topAppBarColorIcon)
            val isTopAppBarColorTitle by config.isTopAppBarColorTitle.collectAsStateWithLifecycle(initialValue = config.topAppBarColorTitle)
            val isBlockingType by config.isBlockingType.collectAsStateWithLifecycle(initialValue = config.blockingType)
            val isBlockingEnabled by config.isBlockingEnabled.collectAsStateWithLifecycle(initialValue = config.blockingEnabled)
            val isDoNotBlockContactsAndRecent by config.isDoNotBlockContactsAndRecent.collectAsStateWithLifecycle(initialValue = config.doNotBlockContactsAndRecent)
            val prefix = appPrefix()
            val isDialer = remember {
                config.appId.startsWith(prefix + "goodwy.dialer")
            }
            val isDefaultDialer: Boolean = onEventValue {
                context.isDefaultDialer()
            }

            AppThemeSurface {
                var clickedBlockedNumber by remember { mutableStateOf<BlockedNumber?>(null) }
                val addBlockedNumberDialogState = rememberAlertDialogState()

                addBlockedNumberDialogState.DialogMember {
                    AddOrEditBlockedNumberAlertDialog(
                        alertDialogState = addBlockedNumberDialogState,
                        blockedNumber = clickedBlockedNumber,
                        deleteBlockedNumber = { blockedNumber ->
                            deleteBlockedNumber(blockedNumber)
                            updateBlockedNumbers()
                        }
                    ) { blockedNumber ->
                        addBlockedNumber(blockedNumber)
                        clickedBlockedNumber = null
                        updateBlockedNumbers()
                    }
                }

                val blockingTypeDialogState = rememberAlertDialogState()

                blockingTypeDialogState.DialogMember {
                    RadioGroupAlertDialog(
                        alertDialogState = blockingTypeDialogState,
                        items = if (isQPlus()) {
                            listOf(
                                RadioItem(BLOCKING_TYPE_REJECT, stringResource(id = com.goodwy.strings.R.string.blocking_type_reject)),
                                RadioItem(BLOCKING_TYPE_DO_NOT_REJECT, stringResource(id = com.goodwy.strings.R.string.blocking_type_do_not_reject)),
                                RadioItem(BLOCKING_TYPE_SILENCE, stringResource(id = com.goodwy.strings.R.string.blocking_type_silence)),
                            ).toImmutableList()
                        } else {
                            listOf(
                                RadioItem(BLOCKING_TYPE_REJECT, stringResource(id = com.goodwy.strings.R.string.blocking_type_reject)),
                                RadioItem(BLOCKING_TYPE_DO_NOT_REJECT, stringResource(id = com.goodwy.strings.R.string.blocking_type_do_not_reject)),
                            ).toImmutableList()
                        },
                        selectedItemId = isBlockingType,
                        titleId = com.goodwy.strings.R.string.blocking_type,
                        cancelCallback = {}
                    ) { item ->
                        config.blockingType = item.toInt()
                    }
                }

                ManageBlockedNumbersScreen(
                    goBack = ::finish,
                    onAdd = {
                        clickedBlockedNumber = null
                        addBlockedNumberDialogState.show()
                    },
                    onImportBlockedNumbers = ::tryImportBlockedNumbers,
                    onExportBlockedNumbers = ::tryExportBlockedNumbers,
                    setAsDefault = ::maybeSetDefaultCallerIdApp,
                    isTopAppBarColorIcon = isTopAppBarColorIcon,
                    isTopAppBarColorTitle = isTopAppBarColorTitle,
                    isDialer = isDialer,
                    hasGivenPermissionToBlock = isDefaultDialer,
                    isBlockUnknownSelected = isBlockingUnknownNumbers,
                    showCheckmarksOnSwitches = showCheckmarksOnSwitches,
                    onBlockUnknownSelectedChange = { isChecked ->
                        config.blockUnknownNumbers = isChecked
                        onCheckedSetCallerIdAsDefault(isChecked)
                    },
                    isHiddenSelected = isBlockingHiddenNumbers,
                    onHiddenSelectedChange = { isChecked ->
                        config.blockHiddenNumbers = isChecked
                        onCheckedSetCallerIdAsDefault(isChecked)
                    },
                    blockedNumbers = blockedNumbers,
                    onDelete = { selectedKeys ->
                        deleteBlockedNumbers(blockedNumbers, selectedKeys)
                    },
                    onEdit = { blockedNumber ->
                        clickedBlockedNumber = blockedNumber
                        addBlockedNumberDialogState.show()
                    },
                    onCopy = { blockedNumber ->
                        copyToClipboard(blockedNumber.number)
                    },
                    isBlockingType = isBlockingType,
                    onBlockingType = {
                        blockingTypeDialogState.show()
                    },
                    isBlockingEnabled = isBlockingEnabled,
                    onBlockingEnabledChange = { isChecked ->
                        config.blockingEnabled = isChecked
                        onCheckedSetCallerIdAsDefault(isChecked)
                    },
                    isDoNotBlockContactsAndRecent = isDoNotBlockContactsAndRecent,
                    onDoNotBlockContactsAndRecentChange = { isChecked ->
                        config.doNotBlockContactsAndRecent = isChecked
                        onCheckedSetCallerIdAsDefault(isChecked)
                    },
                )
            }
        }
    }

    private fun deleteBlockedNumbers(
        blockedNumbers: ImmutableList<BlockedNumber>?,
        selectedKeys: Set<Long>
    ) {
        if (blockedNumbers.isNullOrEmpty()) return
        blockedNumbers.filter { blockedNumber -> selectedKeys.contains(blockedNumber.id) }
            .forEach { blockedNumber ->
                deleteBlockedNumber(blockedNumber.number)
            }
        manageBlockedNumbersViewModel.updateBlockedNumbers()
    }

    private fun tryImportBlockedNumbers() {
        try {
            openDocument.launch(blockedNumberMimeTypes)
        } catch (_: ActivityNotFoundException) {
            toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
        } catch (e: Exception) {
            showErrorToast(e)
        }
    }

    private fun tryImportBlockedNumbersFromFile(uri: Uri) {
        when (uri.scheme) {
            "file" -> importBlockedNumbers(uri.path!!)
            "content" -> {
                val tempFile = getTempFile("blocked", "blocked_numbers.txt")
                if (tempFile == null) {
                    toast(R.string.unknown_error_occurred)
                    return
                }

                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val out = FileOutputStream(tempFile)
                    inputStream!!.copyTo(out)
                    importBlockedNumbers(tempFile.absolutePath)
                } catch (e: Exception) {
                    showErrorToast(e)
                }
            }

            else -> toast(R.string.invalid_file_format)
        }
    }

    private fun importBlockedNumbers(path: String) {
        ensureBackgroundThread {
            val result = BlockedNumbersImporter(this).importBlockedNumbers(path)
            toast(
                when (result) {
                BlockedNumbersImporter.ImportResult.IMPORT_OK -> R.string.importing_successful
                BlockedNumbersImporter.ImportResult.IMPORT_FAIL -> R.string.no_items_found
                }
            )
            updateBlockedNumbers()
        }
    }

    private fun updateBlockedNumbers() {
        manageBlockedNumbersViewModel.updateBlockedNumbers()
    }

    private fun onCheckedSetCallerIdAsDefault(isChecked: Boolean) {
        if (isChecked) {
            maybeSetDefaultCallerIdApp()
        }
    }

    private fun maybeSetDefaultCallerIdApp() {
        val prefix = appPrefix()
        if (isQPlus() && baseConfig.appId.startsWith(prefix + "goodwy.dialer")) {
            setDefaultCallerIdApp()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        when {
            requestCode == REQUEST_CODE_SET_DEFAULT_DIALER && isDefaultDialer() -> {
                updateBlockedNumbers()
            }

            requestCode == REQUEST_CODE_SET_DEFAULT_CALLER_ID && resultCode != RESULT_OK -> {
                toast(R.string.must_make_default_caller_id_app, length = Toast.LENGTH_LONG)
                baseConfig.blockingEnabled = false
                baseConfig.blockUnknownNumbers = false
                baseConfig.blockHiddenNumbers = false
                intent.action = null
            }

            intent.action == SET_DEFAULT_CALLER_ID && resultCode == RESULT_OK -> {
                baseConfig.blockingEnabled = true
            }
        }
    }

    private fun exportBlockedNumbersTo(outputStream: OutputStream?) {
        ensureBackgroundThread {
            val blockedNumbers = getBlockedNumbers()
            if (blockedNumbers.isEmpty()) {
                toast(R.string.no_entries_for_exporting)
            } else {
                BlockedNumbersExporter.exportBlockedNumbers(blockedNumbers, outputStream) {
                    toast(
                        when (it) {
                            ExportResult.EXPORT_OK -> R.string.exporting_successful
                            else -> R.string.exporting_failed
                        }
                    )
                }
            }
        }
    }

    private fun tryExportBlockedNumbers() {
        ExportBlockedNumbersDialog(
            activity = this,
            path = baseConfig.lastBlockedNumbersExportPath,
            hidePath = true
        ) { file ->
            try {
                createDocument.launch(file.name)
            } catch (_: ActivityNotFoundException) {
                toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }

    internal class ManageBlockedNumbersViewModel(
        private val application: Application
    ) : AndroidViewModel(application) {


        private val _blockedNumbers: MutableStateFlow<ImmutableList<BlockedNumber>?> = MutableStateFlow(null)
        val blockedNumbers = _blockedNumbers.asStateFlow()

        init {
            updateBlockedNumbers()
        }

        fun updateBlockedNumbers() {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    application.getBlockedNumbersWithContact { list ->
                        _blockedNumbers.update { list.toImmutableList() }
                    }
                }
            }
        }
    }
}
