package com.goodwy.commons.helpers.rustore.model

sealed class BillingEvent {
    data class ShowDialog(val dialogInfo: InfoDialogState): BillingEvent()
    data class ShowError(val error: Throwable): BillingEvent()
}
