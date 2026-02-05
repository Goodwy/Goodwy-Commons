package com.goodwy.commons.helpers

import android.app.Activity
import com.goodwy.commons.extensions.addBlockedNumber
import com.goodwy.commons.extensions.showErrorToast
import java.io.File

class BlockedNumbersImporter(
    private val activity: Activity,
) {
    enum class ImportResult {
        IMPORT_FAIL, IMPORT_OK
    }

    fun importBlockedNumbers(path: String): ImportResult {
        return try {
            val numbers = File(path)
                .bufferedReader()
                .use { reader ->
                    reader.readText()
                        .split(BLOCKED_NUMBERS_EXPORT_DELIMITER)
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                }

            if (numbers.isEmpty()) {
                ImportResult.IMPORT_FAIL
            } else {
                numbers.forEach(activity::addBlockedNumber)
                ImportResult.IMPORT_OK
            }

        } catch (e: Exception) {
            activity.showErrorToast(e)
            ImportResult.IMPORT_FAIL
        }
    }
}
