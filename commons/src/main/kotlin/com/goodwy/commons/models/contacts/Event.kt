package com.goodwy.commons.models.contacts

import kotlinx.serialization.Serializable

@Serializable
data class Event(var value: String? = "", var type: Int, var label: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Event

        if (value != other.value) return false
        if (type != other.type) return false
        if (label != other.label) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + type
        result = 31 * result + label.hashCode()
        return result
    }
}
