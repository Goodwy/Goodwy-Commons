package com.goodwy.commons.helpers.rustore.model

import ru.rustore.sdk.billingclient.model.purchase.PurchaseAvailabilityResult

sealed class StartPurchasesEvent {
    data class PurchasesAvailability(val availability: PurchaseAvailabilityResult) : StartPurchasesEvent()
    data class Error(val throwable: Throwable): StartPurchasesEvent()
}
