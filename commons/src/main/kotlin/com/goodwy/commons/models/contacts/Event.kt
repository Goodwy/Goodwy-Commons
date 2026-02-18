package com.goodwy.commons.models.contacts

import kotlinx.serialization.Serializable
import java.util.Objects

@Serializable
data class Event(
    var value: String? = null,
    var type: Int = 0,
    var label: String? = null
) {
    override fun hashCode(): Int {
        return Objects.hash(value, type, label)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Event

        if (type != other.type) return false
        if (value != other.value) return false
        if (label != other.label) return false

        return true
    }

    override fun toString(): String {
        return "Event(value=$value, type=$type, label=$label)"
    }
}
