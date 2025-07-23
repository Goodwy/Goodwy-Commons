package com.goodwy.commons.models.contacts

import kotlinx.serialization.Serializable

@Serializable
data class Address(
    var value: String,
    var type: Int,
    var label: String,
    var country: String = "",
    var region: String = "",
    var city: String = "",
    var postcode: String = "",
    var pobox: String = "",
    var street: String = "",
    var neighborhood: String = "",
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Address

        if (value != other.value) return false
        if (type != other.type) return false
        if (label != other.label) return false
        if (country != other.country) return false
        if (region != other.region) return false
        if (city != other.city) return false
        if (postcode != other.postcode) return false
        if (pobox != other.pobox) return false
        if (street != other.street) return false
        if (neighborhood != other.neighborhood) return false

        return true
    }

    override fun hashCode(): Int {
        var result = (value ?: "").hashCode()
        result = 31 * result + type
        result = 31 * result + (label ?: "").hashCode()
        result = 31 * result + (country ?: "").hashCode()
        result = 31 * result + (region ?: "").hashCode()
        result = 31 * result + (city ?: "").hashCode()
        result = 31 * result + (postcode ?: "").hashCode()
        result = 31 * result + (pobox ?: "").hashCode()
        result = 31 * result + (street ?: "").hashCode()
        result = 31 * result + (neighborhood ?: "").hashCode()
        return result
    }
}
