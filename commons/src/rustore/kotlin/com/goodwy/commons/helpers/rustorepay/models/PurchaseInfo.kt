package com.goodwy.commons.helpers.rustorepay.models

import kotlinx.serialization.Serializable

@Serializable
data class PurchaseInfo(
    val purchaseId: String,
    val invoiceId: String,
    val type: String,
    val status: String, // product or subscription
    val purchaseTime: Long,
    val price: Int,
    val currency: String,
    val developerPayload: String? = null,
    val productId: String,
    val expirationDate: Long? = null,
    val gracePeriodEnabled: Boolean? = null
)
