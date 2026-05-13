package com.goodwy.commons.helpers.rustorepay.models

import kotlinx.serialization.Serializable

@Serializable
data class ProductInfo(
    val productId: String,
    val type: String,
    val amountLabel: String,
    val price: Int,
    val currency: String,
    val imageUrl: String?,
    val title: String,
    val description: String?,
    val subscriptionInfo: SubscriptionInfo? = null
)

@Serializable
data class SubscriptionInfo(
    val periods: List<SubscriptionPeriod>
)

@Serializable
data class SubscriptionPeriod(
    val type: String,
    val duration: String,
    val price: String?
)
