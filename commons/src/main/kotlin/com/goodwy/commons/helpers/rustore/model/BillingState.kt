package com.goodwy.commons.helpers.rustore.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.StringRes
import ru.rustore.sdk.billingclient.model.product.Product

data class BillingState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    @StringRes val snackbarResId: Int? = null
) : Parcelable {
    val isEmpty: Boolean = products.isEmpty() && !isLoading

    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(),
        TODO("products"),
        parcel.readValue(Int::class.java.classLoader) as? Int
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (isLoading) 1 else 0)
        parcel.writeValue(snackbarResId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BillingState> {
        override fun createFromParcel(parcel: Parcel): BillingState {
            return BillingState(parcel)
        }

        override fun newArray(size: Int): Array<BillingState?> {
            return arrayOfNulls(size)
        }
    }
}
