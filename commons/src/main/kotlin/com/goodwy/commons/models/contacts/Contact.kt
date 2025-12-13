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
        var showNicknameInsteadNames = false
        var sortingSymbolsFirst = false
        var collator: Collator? = null

        // Optimised comparator for fast sorting
        val optimizedComparator = Comparator<Contact> { c1, c2 ->
            // Quick comparison without creating unnecessary objects
            compareContacts(c1, c2, sorting, startWithSurname, showNicknameInsteadNames, sortingSymbolsFirst, collator)
        }

        // Quick comparator in name only (for Recents)
        val nameOnlyComparator = Comparator<Contact> { c1, c2 ->
            c1.getNameToDisplay().compareTo(c2.getNameToDisplay(), ignoreCase = true)
        }

        private fun compareContacts(
            c1: Contact,
            c2: Contact,
            sorting: Int,
            startWithSurname: Boolean,
            showNicknameInsteadNames: Boolean,
            sortingSymbolsFirst: Boolean,
            collator: Collator?
        ): Int {
            var result = when {
                sorting and SORT_BY_FIRST_NAME != 0 -> {
                    compareStringsFast(c1.getCompareStringForFirstNameFast(), c2.getCompareStringForFirstNameFast())
                }
                sorting and SORT_BY_MIDDLE_NAME != 0 -> {
                    compareStringsFast(c1.getCompareStringForMiddleNameFast(), c2.getCompareStringForMiddleNameFast())
                }
                sorting and SORT_BY_SURNAME != 0 -> {
                    compareStringsFast(c1.getCompareStringForSurnameFast(), c2.getCompareStringForSurnameFast())
                }
                sorting and SORT_BY_FULL_NAME != 0 -> {
                    compareStringsFast(c1.getCompareStringForFullNameFast(), c2.getCompareStringForFullNameFast())
                }
                else -> c1.id.compareTo(c2.id)
            }

            if (sorting and SORT_DESCENDING != 0) {
                result *= -1
            }

            return result
        }

        private fun compareStringsFast(first: String, second: String): Int {
            if (first.isEmpty() && second.isEmpty()) return 0
            if (first.isEmpty()) return 1
            if (second.isEmpty()) return -1

            // A simple quick comparison
            return collator?.compare(first, second) ?: first.compareTo(second, true)
        }
    }

    // Fast versions of methods without normaliseString()
    private fun getCompareStringForFirstNameFast(): String {
        val firstNameOrNickname = if (showNicknameInsteadNames && nickname.isNotBlank()) nickname else firstName
        return if (firstNameOrNickname.isEmpty() && isNameEmpty()) getFallbackCompareStringFast()
        else firstNameOrNickname.lowercase()
    }

    private fun getCompareStringForMiddleNameFast(): String {
        return if (middleName.isEmpty() && isNameEmpty()) getFallbackCompareStringFast()
        else middleName.lowercase()
    }

    private fun getCompareStringForSurnameFast(): String {
        return if (surname.isEmpty() && isNameEmpty()) getFallbackCompareStringFast()
        else surname.lowercase()
    }

    private fun getCompareStringForFullNameFast(): String {
        return if (getNameToDisplay().isEmpty()) getFallbackCompareStringFast()
        else getNameToDisplay().lowercase()
    }

    private fun getFallbackCompareStringFast(): String {
        return when {
            getFullCompany().isNotEmpty() -> getFullCompany().lowercase()
            emails.isNotEmpty() -> emails.first().value.lowercase()
            else -> ""
        }
    }

    override fun compareTo(other: Contact): Int {
        var result = when {
            sorting and SORT_BY_FIRST_NAME != 0 -> {
                compareUsingStrings(getCompareStringForFirstName(), other.getCompareStringForFirstName(), other)
            }
            sorting and SORT_BY_MIDDLE_NAME != 0 -> {
                compareUsingStrings(getCompareStringForMiddleName(), other.getCompareStringForMiddleName(), other)
            }
            sorting and SORT_BY_SURNAME != 0 -> {
                compareUsingStrings(getCompareStringForSurname(), other.getCompareStringForSurname(), other)
            }
            sorting and SORT_BY_FULL_NAME != 0 -> {
                compareUsingStrings(getCompareStringForFullName(), other.getCompareStringForFullName(), other)
            }
            else -> compareUsingIds(other)
        }

        if (sorting and SORT_DESCENDING != 0) {
            result *= -1
        }

        return result
    }

    private fun getCompareStringForFirstName(): String {
        val firstNameOrNickname = if (showNicknameInsteadNames && nickname.isNotBlank()) nickname else firstName
        return if (firstNameOrNickname.isEmpty() && isNameEmpty()) getFallbackCompareString() else firstNameOrNickname.normalizeString()
    }

    private fun getCompareStringForMiddleName(): String {
        return if (middleName.isEmpty() && isNameEmpty()) getFallbackCompareString() else middleName.normalizeString()
    }

    private fun getCompareStringForSurname(): String {
        return if (surname.isEmpty() && isNameEmpty()) getFallbackCompareString() else surname.normalizeString()
    }

    private fun getCompareStringForFullName(): String {
        return if (getNameToDisplay().isEmpty()) getFallbackCompareString() else getNameToDisplay().normalizeString()
    }

    private fun isNameEmpty(): Boolean {
        val firstNameOrNickname = if (showNicknameInsteadNames && nickname.isNotBlank()) nickname else firstName
        return firstNameOrNickname.isEmpty() && middleName.isEmpty() && surname.isEmpty()
    }

    private fun getFallbackCompareString(): String {
        return when {
            getFullCompany().isNotEmpty() -> getFullCompany().normalizeString()
            emails.isNotEmpty() -> emails.first().value.normalizeString()
            else -> "" // Guarantee a non-null string for comparison
        }
    }

    private fun compareUsingStrings(firstString: String, secondString: String, other: Contact): Int {
        // First, we compare taking into account sortingSymbolsFirst
        val primaryComparison = compareStringsWithSymbols(firstString, secondString)
        if (primaryComparison != 0) {
            return primaryComparison
        }

        // If the main strings are equal, we compare by full name for sort stability.
        return compareStringsWithSymbols(
            getNameToDisplay().normalizeString(),
            other.getNameToDisplay().normalizeString()
        )
    }

    private fun compareStringsWithSymbols(first: String, second: String): Int {
        if (first.isEmpty() && second.isEmpty()) return 0
        if (first.isEmpty()) return 1
        if (second.isEmpty()) return -1

        return if (sortingSymbolsFirst) {
            compareWithSymbolsFirst(first, second)
        } else {
            compareWithLettersFirst(first, second)
        }
    }

    private fun compareWithSymbolsFirst(first: String, second: String): Int {
        if (first.isEmpty() && second.isEmpty()) return 0
        if (first.isEmpty()) return 1
        if (second.isEmpty()) return -1

        val firstCharType = getCharType(first.first())
        val secondCharType = getCharType(second.first())

        // Non-letters come before letters
        return when {
            firstCharType == CharType.LETTER && secondCharType != CharType.LETTER -> 1
            firstCharType != CharType.LETTER && secondCharType == CharType.LETTER -> -1
            firstCharType == CharType.DIGIT && secondCharType == CharType.SYMBOL -> 1
            firstCharType == CharType.SYMBOL && secondCharType == CharType.DIGIT -> -1
            else -> collator?.compare(first, second) ?: first.compareTo(second, true)
        }
    }

    private fun compareWithLettersFirst(first: String, second: String): Int {
        if (first.isEmpty() && second.isEmpty()) return 0
        if (first.isEmpty()) return 1
        if (second.isEmpty()) return -1

        val firstCharType = getCharType(first.first())
        val secondCharType = getCharType(second.first())

        // Letters come before non-letters
        return when {
            firstCharType == CharType.LETTER && secondCharType != CharType.LETTER -> -1
            firstCharType != CharType.LETTER && secondCharType == CharType.LETTER -> 1
            else -> collator?.compare(first, second) ?: first.compareTo(second, true)
        }
    }

    private fun getCharType(char: Char?): CharType {
        return when {
            char == null -> CharType.SYMBOL
            char.isLetter() -> CharType.LETTER
            char.isDigit() -> CharType.DIGIT
            else -> CharType.SYMBOL
        }
    }

    private enum class CharType {
        LETTER, DIGIT, SYMBOL
    }

    private fun compareUsingIds(other: Contact): Int {
        return id.compareTo(other.id)
    }

    fun getBubbleText(): String {
        return try {
            val firstName = if (showNicknameInsteadNames && nickname.isNotBlank()) nickname else firstName
            var name = when {
                isABusinessContact() -> getFullCompany()
                sorting and SORT_BY_SURNAME != 0 && surname.isNotEmpty() -> surname
                sorting and SORT_BY_MIDDLE_NAME != 0 && middleName.isNotEmpty() -> middleName
                sorting and SORT_BY_FIRST_NAME != 0 && firstName.isNotEmpty() -> firstName
                startWithSurname -> surname
                else -> firstName
            }

            if (name.isEmpty()) {
                name = getNameToDisplay()
            }

            name
        } catch (_: Exception) {
            ""
        }
    }

    fun getFirstLetter(): String {
        val bubbleText = getBubbleText()
        val emoji = bubbleText.take(2)
        val character = if (emoji.isEmoji()) emoji else if (bubbleText.isNotEmpty()) bubbleText.substring(0, 1) else ""
        return character.uppercase(Locale.getDefault()).normalizeString()
    }

    fun getNameToDisplay(): String {
        val firstName = if (showNicknameInsteadNames && nickname.isNotBlank()) nickname else firstName
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
        val phoneNumber = phoneNumbers.firstOrNull()?.value

        return when {
            fullName.isNotBlank() -> fullName
            nickname.isNotBlank() -> nickname
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
        if (text.isEmpty()) return false

        if (doesContainPhoneNumberCheck(text, convertLetters, search)) {
            return true
        }

        if (search) return false // We do not make additional replacements for the search.

        return checkWithCountrySpecificReplacements(text, convertLetters)
    }

    // If the number in the contacts is written without + or 8 instead of +7
    // https://en.wikipedia.org/wiki/National_conventions_for_writing_telephone_numbers
    private fun checkWithCountrySpecificReplacements(text: String, convertLetters: Boolean): Boolean {
        return when {
            text.startsWith("+7") -> doesContainPhoneNumberCheck(text.replace("+7", "8"), convertLetters) //Russia
            text.startsWith("+31") -> doesContainPhoneNumberCheck(text.replace("+31", "0"), convertLetters) //Netherlands
            text.startsWith("+32") -> doesContainPhoneNumberCheck(text.replace("+32", "0"), convertLetters) //Belgium
            text.startsWith("+33") -> doesContainPhoneNumberCheck(text.replace("+33", "0"), convertLetters) //France
            text.startsWith("+34") -> doesContainPhoneNumberCheck(text.replace("+34", ""), convertLetters) //Spain
            text.startsWith("+39") -> doesContainPhoneNumberCheck(text.replace("+39", "0"), convertLetters) //Italy
            text.startsWith("+44") -> doesContainPhoneNumberCheck(text.replace("+44", "0"), convertLetters) //United Kingdom
            text.startsWith("+49") -> doesContainPhoneNumberCheck(text.replace("+49", "0"), convertLetters) //Germany
            text.startsWith("+91") -> doesContainPhoneNumberCheck(text.replace("+91", "0"), convertLetters) //India
            text.startsWith("+351") -> doesContainPhoneNumberCheck(text.replace("+351", ""), convertLetters) //Portugal
            text.startsWith("+374") -> doesContainPhoneNumberCheck(text.replace("+374", "0"), convertLetters) //Armenia
            text.startsWith("+375") -> doesContainPhoneNumberCheck(text.replace("+375", "0"), convertLetters) //Belarus
            text.startsWith("+380") -> doesContainPhoneNumberCheck(text.replace("+380", "0"), convertLetters) //Ukraine
            else -> false
        }
    }

    fun doesContainPhoneNumberCheck(text: String, convertLetters: Boolean = false, search: Boolean = false): Boolean {
        if (text.isEmpty() || phoneNumbers.isEmpty()) return false

        val normalizedText = if (convertLetters) text.normalizePhoneNumber() else text

        return if (search) {
            phoneNumbers.any { phoneNumber ->
                PhoneNumberUtils.compare(phoneNumber.normalizedNumber, normalizedText) ||
                    phoneNumber.value.contains(text) ||
                    phoneNumber.normalizedNumber.contains(normalizedText) ||
                    phoneNumber.value.normalizePhoneNumber().contains(normalizedText)
            }
        } else {
            phoneNumbers.any { phoneNumber ->
                // TODO Does not work correctly if only some digits of the number match
                // TODO Replaced contains with endsWith, may be helpful
                PhoneNumberUtils.compare(phoneNumber.normalizedNumber, normalizedText)
//                    (phoneNumber.value.contains(text) && text.length > 7) ||
//                    (phoneNumber.normalizedNumber.contains(normalizedText) && normalizedText.length > 7) ||
//                    (phoneNumber.value.normalizePhoneNumber().contains(normalizedText) && normalizedText.length > 7)
                    || (phoneNumber.value.endsWith(text) && text.length > 7)
                    || (phoneNumber.normalizedNumber.endsWith(normalizedText) && normalizedText.length > 7)
                    // TODO I think the following line is unnecessary
//                    || (phoneNumber.value.normalizePhoneNumber().endsWith(normalizedText) && normalizedText.length > 7)
            }
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

    fun getSignatureKey() = photoUri.ifEmpty { hashCode() }

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
            contactToText = contactToText + context.getEventTypeText(it.type, it.label) + " " + it.value + "\n"
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
