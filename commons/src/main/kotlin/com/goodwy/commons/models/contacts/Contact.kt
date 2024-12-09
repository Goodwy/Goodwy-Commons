package com.goodwy.commons.models.contacts

import android.content.Context
import android.graphics.Bitmap
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.*
import com.goodwy.commons.models.PhoneNumber
import ezvcard.property.FormattedName
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.text.Collator
import java.util.Locale

@Serializable
data class Contact(
    var id: Int,
    var prefix: String= "",
    var firstName: String= "",
    var middleName: String= "",
    var surname: String= "",
    var suffix: String= "",
    var nickname: String= "",
    var photoUri: String= "",
    var phoneNumbers: ArrayList<PhoneNumber> = ArrayList(),
    var emails: ArrayList<Email> = ArrayList(),
    var addresses: ArrayList<Address> = ArrayList(),
    var events: ArrayList<Event> = ArrayList(),
    var source: String= "",
    var starred: Int = 0,
    var contactId: Int,
    var thumbnailUri: String= "",
    @Contextual
    var photo: Bitmap? = null,
    var notes: String= "",
    var groups: ArrayList<Group> = ArrayList(),
    var organization: Organization = Organization("",""),
    var websites: ArrayList<String> = ArrayList(),
    var relations: ArrayList<ContactRelation> = ArrayList(),
    var IMs: ArrayList<IM> = ArrayList(),
    var mimetype: String = "",
    var ringtone: String? = ""
) : Comparable<Contact> {
    val rawId = id
    val name = getNameToDisplay()
    var birthdays = events.filter { it.type == ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY }.map { it.value }.toMutableList() as ArrayList<String>
    var anniversaries =
        events.filter { it.type == ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY }.map { it.value }.toMutableList() as ArrayList<String>

    companion object {
        var sorting = 0
        var startWithSurname = false
        var sortingSymbolsFirst = false
        var collator: Collator? = null
    }

    override fun compareTo(other: Contact): Int {
        var result = when {
            sorting and SORT_BY_FIRST_NAME != 0 -> {
                val firstString = firstName.normalizeString()
                val secondString = other.firstName.normalizeString()
                compareUsingStrings(firstString, secondString, other)
            }

            sorting and SORT_BY_MIDDLE_NAME != 0 -> {
                val firstString = middleName.normalizeString()
                val secondString = other.middleName.normalizeString()
                compareUsingStrings(firstString, secondString, other)
            }

            sorting and SORT_BY_SURNAME != 0 -> {
                val firstString = surname.normalizeString()
                val secondString = other.surname.normalizeString()
                compareUsingStrings(firstString, secondString, other)
            }

            sorting and SORT_BY_FULL_NAME != 0 -> {
                val firstString = getNameToDisplay().normalizeString()
                val secondString = other.getNameToDisplay().normalizeString()
                compareUsingStrings(firstString, secondString, other)
            }

            else -> compareUsingIds(other)
        }

        if (sorting and SORT_DESCENDING != 0) {
            result *= -1
        }

        return result
    }

    private fun compareUsingStrings(firstString: String, secondString: String, other: Contact): Int {
        var firstValue = firstString
        var secondValue = secondString

        if (firstValue.isEmpty() && firstName.isEmpty() && middleName.isEmpty() && surname.isEmpty()) {
            val fullCompany = getFullCompany()
            if (fullCompany.isNotEmpty()) {
                firstValue = fullCompany.normalizeString()
            } else if (emails.isNotEmpty()) {
                firstValue = emails.first().value
            }
        }

        if (secondValue.isEmpty() && other.firstName.isEmpty() && other.middleName.isEmpty() && other.surname.isEmpty()) {
            val otherFullCompany = other.getFullCompany()
            if (otherFullCompany.isNotEmpty()) {
                secondValue = otherFullCompany.normalizeString()
            } else if (other.emails.isNotEmpty()) {
                secondValue = other.emails.first().value
            }
        }

        try {
            return if (sortingSymbolsFirst) {
                //TODO Contacts sorting: symbols at the top
                if (firstValue.firstOrNull()?.isLetter() == true && secondValue.firstOrNull()?.isDigit() == true) {
                    -1
                } else if (firstValue.firstOrNull()?.isDigit() == true && secondValue.firstOrNull()?.isLetter() == true) {
                    1
                } else {
                    if (firstValue.firstOrNull()?.isLetter() == true && secondValue.firstOrNull()?.isLetter() == true) {
                        collator?.compare(firstValue, secondValue) ?: firstValue.compareTo(secondValue, true)
                    } else {
                        if (firstValue.isEmpty() && secondValue.isNotEmpty()) {
                            1
                        } else if (firstValue.isNotEmpty() && secondValue.isEmpty()) {
                            -1
                        } else {
                            if (firstValue.equals(secondValue, ignoreCase = true)) {
                                collator?.compare(getNameToDisplay(), other.getNameToDisplay()) ?: getNameToDisplay().compareTo(other.getNameToDisplay(), true)
                            } else {
                                collator?.compare(firstValue, secondValue) ?: firstValue.compareTo(secondValue, true)
                            }
                        }
                    }
                }
            } else {
                if (firstValue.firstOrNull()?.isLetter() == true && secondValue.firstOrNull()?.isLetter() == false) {
                    -1
                } else if (firstValue.firstOrNull()?.isLetter() == false && secondValue.firstOrNull()?.isLetter() == true) {
                    1
                } else {
                    if (firstValue.isEmpty() && secondValue.isNotEmpty()) {
                        1
                    } else if (firstValue.isNotEmpty() && secondValue.isEmpty()) {
                        -1
                    } else {
                        if (firstValue.equals(secondValue, ignoreCase = true)) {
                            collator?.compare(getNameToDisplay(), other.getNameToDisplay()) ?: getNameToDisplay().compareTo(other.getNameToDisplay(), true)
                        } else {
                            collator?.compare(firstValue, secondValue) ?: firstValue.compareTo(secondValue, true)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            return 0
        }
    }

    private fun compareUsingIds(other: Contact): Int {
        val firstId = id
        val secondId = other.id
        return firstId.compareTo(secondId)
    }

    fun getBubbleText() = when {
        sorting and SORT_BY_FIRST_NAME != 0 -> firstName
        sorting and SORT_BY_MIDDLE_NAME != 0 -> middleName
        else -> surname
    }

    fun getNameToDisplay(): String {
        val firstMiddle = "$firstName $middleName".trim()
        val firstPart = if (startWithSurname) {
            if (surname.isNotEmpty() && firstMiddle.isNotEmpty()) {
                "$surname,"
            } else {
                surname
            }
        } else {
            firstMiddle
        }
        val lastPart = if (startWithSurname) firstMiddle else surname
        val suffixComma = if (suffix.isEmpty()) "" else ", $suffix"
        val fullName = "$prefix $firstPart $lastPart$suffixComma".trim()
        val organization = getFullCompany()
        val email = emails.firstOrNull()?.value?.trim()
        val phoneNumber = phoneNumbers.firstOrNull()?.normalizedNumber

        return when {
            fullName.isNotBlank() -> fullName
            organization.isNotBlank() -> organization
            !email.isNullOrBlank() -> email
            !phoneNumber.isNullOrBlank() -> phoneNumber
            else -> return ""
        }
    }

    // photos stored locally always have different hashcodes. Avoid constantly refreshing the contact lists as the app thinks something changed.
    fun getHashWithoutPrivatePhoto(): Int {
        val photoToUse = if (isPrivate()) null else photo
        return copy(photo = photoToUse).hashCode()
    }

    fun getStringToCompare(): String {
        val photoToUse = if (isPrivate()) null else photo
        return copy(
            id = 0,
            prefix = "",
            firstName = getNameToDisplay().lowercase(Locale.getDefault()),
            middleName = "",
            surname = "",
            suffix = "",
            nickname = "",
            photoUri = "",
            phoneNumbers = ArrayList(),
            emails = ArrayList(),
            events = ArrayList(),
            source = "",
            addresses = ArrayList(),
            starred = 0,
            contactId = 0,
            thumbnailUri = "",
            photo = photoToUse,
            notes = "",
            groups = ArrayList(),
            websites = ArrayList(),
            organization = Organization("", ""),
            relations= ArrayList(),
            IMs = ArrayList(),
            ringtone = ""
        ).toString()
    }

    fun getHashToCompare() = getStringToCompare().hashCode()

    fun getFullCompany(): String {
        var fullOrganization = if (organization.company.isEmpty()) "" else "${organization.company}, "
        fullOrganization += organization.jobPosition
        return fullOrganization.trim().trimEnd(',')
    }

    fun isABusinessContact() =
        prefix.isEmpty() && firstName.isEmpty() && middleName.isEmpty() && surname.isEmpty() && suffix.isEmpty() && organization.isNotEmpty()

    fun doesContainPhoneNumber(text: String, convertLetters: Boolean = false, search: Boolean = false): Boolean {
        return if (text.isNotEmpty()) {
            val normalizedText = if (convertLetters) text.normalizePhoneNumber() else text
            phoneNumbers.any {
                if (search) {
                    PhoneNumberUtils.compare(it.normalizedNumber, normalizedText) ||
                        it.value.contains(text) ||
                        it.normalizedNumber.contains(normalizedText) ||
                        it.value.normalizePhoneNumber().contains(normalizedText)
                } else {
                    // TODO does not work correctly if only some digits of the number match
                    PhoneNumberUtils.compare(it.normalizedNumber, normalizedText) ||
                        (it.value.contains(text) && text.length > 7) ||
                        (it.normalizedNumber.contains(normalizedText) && normalizedText.length > 7) ||
                        (it.value.normalizePhoneNumber().contains(normalizedText) && normalizedText.length > 7)
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

    fun isPrivate() = source == SMT_PRIVATE

    fun getSignatureKey() = if (photoUri.isNotEmpty()) photoUri else hashCode()

    fun getPrimaryNumber(): String? {
        val primaryNumber = phoneNumbers.firstOrNull { it.isPrimary }
        return primaryNumber?.normalizedNumber ?: phoneNumbers.firstOrNull()?.normalizedNumber
    }

    fun getContactToText(context: Context): String {
        val name = arrayOf(prefix, firstName, middleName, surname, suffix)
            .filter { it.isNotEmpty() }
            .joinToString(separator = " ")
        val formattedName = FormattedName(name).value
        var contactToText = if (formattedName.isNotEmpty()) formattedName + "\n" else ""

        if (nickname.isNotEmpty()) contactToText = contactToText + nickname + "\n"

        if (phoneNumbers.isNotEmpty()) phoneNumbers.forEach {
            contactToText = contactToText + context.getPhoneNumberTypeText(it.type, it.label) + " " + it.value + "\n"
        }

        if (emails.isNotEmpty()) emails.forEach {
            contactToText = contactToText + context.getEmailTypeText(it.type, it.label) + " " + it.value + "\n"
        }

        if (addresses.isNotEmpty()) addresses.forEach {
            contactToText = contactToText + it.value + "\n"
        }

        if (events.isNotEmpty()) events.forEach {
            contactToText = contactToText + context.getString(getEventTextId(it.type)) + " " + it.value + "\n"
        }

        if (notes.isNotEmpty()) contactToText = contactToText + notes + "\n"

        if (organization.company.isNotEmpty() && organization.jobPosition.isNotEmpty()) {
            contactToText = contactToText + organization.company + "" + organization.jobPosition + "\n"
        } else if (organization.company.isNotEmpty()) {
            contactToText = contactToText + organization.company + "\n"
        } else if (organization.jobPosition.isNotEmpty()) {
            contactToText = contactToText + organization.jobPosition + "\n"
        }

        if (websites.isNotEmpty()) websites.forEach {
            contactToText = contactToText + it + "\n"
        }

        if (relations.isNotEmpty()) relations.forEach {
            contactToText = contactToText + context.getRelationTypeText(it.type, it.label) + " " + it.name + "\n"
        }

        if (IMs.isNotEmpty()) IMs.forEach {
            contactToText = contactToText + context.getEmailTypeText(it.type, it.label) + " " + it.value
        }

        return contactToText.trim().replace("\n+".toRegex(), replacement = "\n")
    }
}
