package com.goodwy.commons.helpers

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.CommonDataKinds.Im
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.MediaStore
import com.goodwy.commons.R
import com.goodwy.commons.activities.BaseSimpleActivity
import com.goodwy.commons.extensions.getByteArray
import com.goodwy.commons.extensions.getDateTimeFromDateString
import com.goodwy.commons.extensions.showErrorToast
import com.goodwy.commons.extensions.toast
import com.goodwy.commons.models.contacts.Contact
import ezvcard.Ezvcard
import ezvcard.VCard
import ezvcard.VCardVersion
import ezvcard.parameter.ImageType
import ezvcard.property.Address
import ezvcard.property.Anniversary
import ezvcard.property.Birthday
import ezvcard.property.Categories
import ezvcard.property.Deathdate
import ezvcard.property.Email
import ezvcard.property.FormattedName
import ezvcard.property.Impp
import ezvcard.property.Organization
import ezvcard.property.Photo
import ezvcard.property.RawProperty
import ezvcard.property.StructuredName
import ezvcard.property.Telephone
import ezvcard.property.Title
import ezvcard.util.PartialDate
import java.io.OutputStream
import java.time.LocalDate
import androidx.core.net.toUri

class VcfExporter {

    private var contactsExported = 0
    private var contactsFailed = 0

    fun exportContacts(
        activity: BaseSimpleActivity,
        outputStream: OutputStream?,
        contacts: ArrayList<Contact>,
        showExportingToast: Boolean,
        callback: (result: ExportResult) -> Unit
    ) {
        try {
            if (outputStream == null) {
                callback(ExportResult.EXPORT_FAIL)
                return
            }

            if (showExportingToast) {
                activity.toast(R.string.exporting)
            }

            val cards = ArrayList<VCard>()
            for (contact in contacts) {
                val card = VCard()

                card.addProperty(RawProperty("X-PRODID", getAppInfo(activity)))

                val formattedName = arrayOf(contact.prefix, contact.firstName, contact.middleName, contact.surname, contact.suffix)
                    .filter { it.isNotEmpty() }
                    .joinToString(separator = " ")
                card.formattedName = FormattedName(formattedName)

                StructuredName().apply {
                    prefixes.add(contact.prefix)
                    given = contact.firstName
                    additionalNames.add(contact.middleName)
                    family = contact.surname
                    suffixes.add(contact.suffix)
                    card.structuredName = this
                }

                if (contact.nickname.isNotEmpty()) {
                    card.setNickname(contact.nickname)
                }

                contact.phoneNumbers.forEach {
                    val phoneNumber = Telephone(it.value)
                    phoneNumber.parameters.addType(getPhoneNumberTypeLabel(it.type, it.label))
                    card.addTelephoneNumber(phoneNumber)
                }

                contact.emails.forEach {
                    val email = Email(it.value)
                    email.parameters.addType(getEmailTypeLabel(it.type, it.label))
                    card.addEmail(email)
                }

                contact.events.forEach { event ->
                    val dateTime = event.value.getDateTimeFromDateString(false)
                    when (event.type) {
                        Event.TYPE_BIRTHDAY -> {
                            if (event.value.startsWith("--")) {
                                val partial = PartialDate.builder()
                                    .month(dateTime.monthOfYear)
                                    .date(dateTime.dayOfMonth)
                                    .build()
                                card.birthdays.add(Birthday(partial))
                            } else {
                                val date = LocalDate.of(dateTime.year, dateTime.monthOfYear, dateTime.dayOfMonth)
                                card.birthdays.add(Birthday(date))
                            }
                        }

                        Event.TYPE_ANNIVERSARY -> {
                            if (event.value.startsWith("--")) {
                                val partial = PartialDate.builder()
                                    .month(dateTime.monthOfYear)
                                    .date(dateTime.dayOfMonth)
                                    .build()
                                card.anniversaries.add(Anniversary(partial))
                            } else {
                                val date = LocalDate.of(dateTime.year, dateTime.monthOfYear, dateTime.dayOfMonth)
                                card.anniversaries.add(Anniversary(date))
                            }
                        }

                        else -> {
                            if (event.label == activity.getString(com.goodwy.strings.R.string.death)
                                || event.type == CUSTOM_EVENT_TYPE_DEATH
                            ) {
                                if (event.value.startsWith("--")) {
                                    val partial = PartialDate.builder()
                                        .month(dateTime.monthOfYear)
                                        .date(dateTime.dayOfMonth)
                                        .build()
                                    card.deathdates.add(Deathdate(partial))
                                } else {
                                    val date = LocalDate.of(dateTime.year, dateTime.monthOfYear, dateTime.dayOfMonth)
                                    card.deathdates.add(Deathdate(date))
                                }
                            } else {
                                val eventLabel = event.label.ifBlank { activity.getString(R.string.other) }

                                val dateString = if (event.value.startsWith("--")) {
                                    "--${dateTime.monthOfYear.toString().padStart(2, '0')}-${dateTime.dayOfMonth.toString().padStart(2, '0')}"
                                } else {
                                    LocalDate.of(dateTime.year, dateTime.monthOfYear, dateTime.dayOfMonth).toString()
                                }

                                card.addProperty(RawProperty("X-EVENT-DATE", dateString))
                                card.addProperty(RawProperty("X-EVENT-LABEL", eventLabel))
                            }
                        }
                    }
                }

                contact.addresses.forEach {
                    val address = Address()
                    if (listOf(it.country, it.region, it.city, it.postcode, it.pobox, it.street, it.neighborhood)
                            .map{it.isEmpty()}
                            .reduce{a, b -> a || b}) {
                        address.country = it.country
                        address.region = it.region
                        address.locality = it.city
                        address.postalCode = it.postcode
                        address.poBox = it.pobox
                        address.streetAddress = it.street
                        address.extendedAddress = it.neighborhood
                    } else {
                        address.streetAddress = it.value
                    }
                    address.parameters.addType(getAddressTypeLabel(it.type, it.label))
                    card.addAddress(address)
                }

                contact.IMs.forEach {
                    val impp = when (it.type) {
                        Im.PROTOCOL_AIM -> Impp.aim(it.value)
                        Im.PROTOCOL_YAHOO -> Impp.yahoo(it.value)
                        Im.PROTOCOL_MSN -> Impp.msn(it.value)
                        Im.PROTOCOL_ICQ -> Impp.icq(it.value)
                        Im.PROTOCOL_SKYPE -> Impp.skype(it.value)
                        Im.PROTOCOL_GOOGLE_TALK -> Impp(HANGOUTS, it.value)
                        Im.PROTOCOL_QQ -> Impp(QQ, it.value)
                        Im.PROTOCOL_JABBER -> Impp(JABBER, it.value)
                        else -> Impp(it.label, it.value)
                    }

                    card.addImpp(impp)
                }

                if (contact.notes.isNotEmpty()) {
                    card.addNote(contact.notes)
                }

                if (contact.organization.isNotEmpty()) {
                    val organization = Organization()
                    organization.values.add(contact.organization.company)
                    card.organization = organization
                    card.titles.add(Title(contact.organization.jobPosition))
                }

                contact.websites.forEach {
                    card.addUrl(it)
                }

                if (contact.thumbnailUri.isNotEmpty()) {
                    val photoByteArray = MediaStore.Images.Media.getBitmap(activity.contentResolver, contact.thumbnailUri.toUri()).getByteArray()
                    val photo = Photo(photoByteArray, ImageType.JPEG)
                    card.addPhoto(photo)
                }

                if (contact.groups.isNotEmpty()) {
                    val groupList = Categories()
                    contact.groups.forEach {
                        groupList.values.add(it.title)
                    }

                    card.categories = groupList
                }

                cards.add(card)
                contactsExported++
            }

            Ezvcard.write(cards).version(VCardVersion.V4_0).go(outputStream)
        } catch (e: Exception) {
            activity.showErrorToast(e)
        }

        callback(
            when {
                contactsExported == 0 -> ExportResult.EXPORT_FAIL
                contactsFailed > 0 -> ExportResult.EXPORT_PARTIAL
                else -> ExportResult.EXPORT_OK
            }
        )
    }

    private fun getPhoneNumberTypeLabel(type: Int, label: String) = when (type) {
        Phone.TYPE_MOBILE -> CELL
        Phone.TYPE_HOME -> HOME
        Phone.TYPE_WORK -> WORK
        Phone.TYPE_MAIN -> PREF
        Phone.TYPE_FAX_WORK -> WORK_FAX
        Phone.TYPE_FAX_HOME -> HOME_FAX
        Phone.TYPE_PAGER -> PAGER
        Phone.TYPE_OTHER -> OTHER
        else -> label
    }

    private fun getEmailTypeLabel(type: Int, label: String) = when (type) {
        CommonDataKinds.Email.TYPE_HOME -> HOME
        CommonDataKinds.Email.TYPE_WORK -> WORK
        CommonDataKinds.Email.TYPE_MOBILE -> MOBILE
        CommonDataKinds.Email.TYPE_OTHER -> OTHER
        else -> label
    }

    private fun getAddressTypeLabel(type: Int, label: String) = when (type) {
        StructuredPostal.TYPE_HOME -> HOME
        StructuredPostal.TYPE_WORK -> WORK
        StructuredPostal.TYPE_OTHER -> OTHER
        else -> label
    }

    private fun getAppInfo(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val packageName = packageInfo.packageName ?: "unknown"
            val versionName = packageInfo.versionName ?: "unknown"
            "//$packageName//$versionName"
        } catch (_: Exception) {
            "unknown"
        }
    }
}
