package com.goodwy.commons.helpers.rustore.model

import ru.rustore.sdk.core.feature.model.FeatureAvailabilityResult

data class StartPurchasesState(
    val isLoading: Boolean = false,
    val purchasesAvailability: FeatureAvailabilityResult? = null,
    val error: Throwable? = null
)
