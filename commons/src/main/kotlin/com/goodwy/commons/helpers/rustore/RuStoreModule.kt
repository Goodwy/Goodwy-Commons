package com.goodwy.commons.helpers.rustore

import android.app.Application
import android.graphics.Color
import com.goodwy.commons.R
import com.goodwy.commons.extensions.baseConfig
import com.goodwy.commons.extensions.getContrastColor
import com.goodwy.commons.extensions.isUsingSystemDarkTheme
import ru.rustore.sdk.billingclient.RuStoreBillingClient
import ru.rustore.sdk.billingclient.RuStoreBillingClientFactory
import ru.rustore.sdk.billingclient.presentation.BillingClientTheme
import ru.rustore.sdk.billingclient.provider.BillingClientThemeProvider

object RuStoreModule {
    private lateinit var ruStoreBillingClient: RuStoreBillingClient

    fun install(app: Application, consoleApplicationId: String) {
        ruStoreBillingClient = RuStoreBillingClientFactory.create(
            context = app,
            consoleApplicationId = consoleApplicationId,
            deeplinkScheme = "purchase-scheme",
            themeProvider = BillingClientThemeProviderImpl(app)
            //debugLogs  = true
        )
    }

    class BillingClientThemeProviderImpl(private val app: Application): BillingClientThemeProvider {
        override fun provide(): BillingClientTheme {
            return when {
                app.baseConfig.isUsingSystemTheme -> if (app.isUsingSystemDarkTheme()) {
                    BillingClientTheme.Dark
                } else {
                    BillingClientTheme.Light
                }
                app.baseConfig.backgroundColor.getContrastColor() == Color.WHITE -> BillingClientTheme.Dark
                else -> BillingClientTheme.Light
            }
        }
    }

    fun provideRuStoreBillingClient(): RuStoreBillingClient = ruStoreBillingClient

}
