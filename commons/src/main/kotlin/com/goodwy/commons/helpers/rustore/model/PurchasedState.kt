package com.goodwy.commons.helpers.rustore.model

import androidx.annotation.StringRes
import ru.rustore.sdk.billingclient.model.purchase.Purchase

data class PurchasedState(
    val isLoading: Boolean = false,
    val purchases: List<Purchase> = emptyList(),
    @StringRes val snackbarResId: Int? = null
) {
    val isEmpty: Boolean = purchases.isEmpty() && !isLoading
}
