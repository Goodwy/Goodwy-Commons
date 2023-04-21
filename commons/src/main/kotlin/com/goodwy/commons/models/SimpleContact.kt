package com.goodwy.commons.models

import android.telephony.PhoneNumberUtils
import com.goodwy.commons.extensions.normalizePhoneNumber
import com.goodwy.commons.extensions.normalizeString
import com.goodwy.commons.helpers.SORT_BY_FULL_NAME
import com.goodwy.commons.helpers.SORT_DESCENDING

data class SimpleContact(
        val rawId: Int,
        val contactId: Int,
        var name: String,
        var photoUri: String,
        var phoneNumbers: ArrayList<PhoneNumber>,
        var birthdays: ArrayList<String>,
        var anniversaries: ArrayList<String>
    ) : Comparable<SimpleContact> {

    companion object {
        var sorting = -1
    }

    override fun compareTo(other: SimpleContact): Int {
        if (sorting == -1) {
            return compareByFullName(other)
        }

        var result = when {
            sorting and SORT_BY_FULL_NAME != 0 -> compareByFullName(other)
            else -> rawId.compareTo(other.rawId)
        }

        if (sorting and SORT_DESCENDING != 0) {
            result *= -1
        }

        return result
    }

    private fun compareByFullName(other: SimpleContact): Int {
        val firstString = name.normalizeString()
        val secondString = other.name.normalizeString()

        /*return if (firstString.firstOrNull()?.isLetter() == true && secondString.firstOrNull()?.isLetter() == false) {
            -1
        } else if (firstString.firstOrNull()?.isLetter() == false && secondString.firstOrNull()?.isLetter() == true) {
            1*/
        //TODO sorting: symbols at the top
        return if (firstString.firstOrNull()?.isLetter() == true && firstString.firstOrNull()?.isDigit() == false
            && secondString.firstOrNull()?.isLetter() == false && secondString.firstOrNull()?.isDigit() == true) {
            -1
        } else if (firstString.firstOrNull()?.isLetter() == false && firstString.firstOrNull()?.isDigit() == true
            && secondString.firstOrNull()?.isLetter() == true && secondString.firstOrNull()?.isDigit() == false) {
            1
        } else {
            if (firstString.isEmpty() && secondString.isNotEmpty()) {
                1
            } else if (firstString.isNotEmpty() && secondString.isEmpty()) {
                -1
            } else {
                firstString.compareTo(secondString, true)
            }
        }
    }

    fun doesContainPhoneNumber(text: String, search: Boolean = false): Boolean {
        return if (text.isNotEmpty()) {
            val normalizedText = text.normalizePhoneNumber()
            if (normalizedText.isEmpty()) {
                phoneNumbers.map { it.normalizedNumber }.any { phoneNumber ->
                    phoneNumber.contains(text)
                }
            } else if (search) {
                phoneNumbers.map { it.normalizedNumber }.any { phoneNumber ->
                    PhoneNumberUtils.compare(phoneNumber.normalizePhoneNumber(), normalizedText) ||
                            phoneNumber.contains(text) ||
                            phoneNumber.normalizePhoneNumber().contains(normalizedText) ||
                            phoneNumber.contains(normalizedText)
                }
            } else {
                phoneNumbers.map { it.normalizedNumber }.any { phoneNumber ->
                    PhoneNumberUtils.compare(phoneNumber.normalizePhoneNumber(), normalizedText)
                        // does not work correctly if only some digits of the number match
                        || (phoneNumber.contains(text) && text.length > 7)
                        || (phoneNumber.normalizePhoneNumber().contains(normalizedText) && normalizedText.length > 7)
                        || (phoneNumber.contains(normalizedText) && normalizedText.length > 7)
                }
            }
        } else {
            false
        }
    }

    fun doesHavePhoneNumber(text: String): Boolean {
        return if (text.isNotEmpty()) {
            val normalizedText = text.normalizePhoneNumber()
            if (normalizedText.isEmpty()) {
                phoneNumbers.map { it.normalizedNumber }.any { phoneNumber ->
                    phoneNumber == text
                }
            } else {
                phoneNumbers.map { it.normalizedNumber }.any { phoneNumber ->
                    PhoneNumberUtils.compare(phoneNumber.normalizePhoneNumber(), normalizedText) ||
                        phoneNumber == text ||
                        phoneNumber.normalizePhoneNumber() == normalizedText ||
                        phoneNumber == normalizedText
                }
            }
        } else {
            false
        }
    }
}

