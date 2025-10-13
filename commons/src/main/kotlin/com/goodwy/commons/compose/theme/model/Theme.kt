package com.goodwy.commons.compose.theme.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import com.goodwy.commons.R
import com.goodwy.commons.compose.extensions.config
import com.goodwy.commons.compose.theme.isInDarkThemeAndSurfaceIsNotLitWell
import com.goodwy.commons.helpers.isSPlus

@Stable
sealed class Theme : CommonTheme {

    @Stable
    data class SystemDefaultMaterialYou(
        override val accentColorInt: Int,
        override val primaryColorInt: Int,
        override val backgroundColorInt: Int,
        override val appIconColorInt: Int,
        override val textColorInt: Int,
        override val surfaceVariantInt: Int,
        override val primaryContainerInt: Int
    ) : Theme()

    @Stable
    data class White(
        override val accentColorInt: Int,
        override val primaryColorInt: Int,
        override val backgroundColorInt: Int,
        override val appIconColorInt: Int,
        override val textColorInt: Int,
        override val surfaceVariantInt: Int,
        override val primaryContainerInt: Int
    ) : Theme()

    @Stable
    data class Light(
        override val accentColorInt: Int,
        override val primaryColorInt: Int,
        override val backgroundColorInt: Int,
        override val appIconColorInt: Int,
        override val textColorInt: Int,
        override val surfaceVariantInt: Int,
        override val primaryContainerInt: Int
    ) : Theme()

    @Stable
    data class Gray(
        override val accentColorInt: Int,
        override val primaryColorInt: Int,
        override val backgroundColorInt: Int,
        override val appIconColorInt: Int,
        override val textColorInt: Int,
        override val surfaceVariantInt: Int,
        override val primaryContainerInt: Int
    ) : Theme()

    @Stable
    data class Dark(
        override val accentColorInt: Int,
        override val primaryColorInt: Int,
        override val backgroundColorInt: Int,
        override val appIconColorInt: Int,
        override val textColorInt: Int,
        override val surfaceVariantInt: Int,
        override val primaryContainerInt: Int
    ) : Theme()

    @Stable
    data class Black(
        override val accentColorInt: Int,
        override val primaryColorInt: Int,
        override val backgroundColorInt: Int,
        override val appIconColorInt: Int,
        override val textColorInt: Int,
        override val surfaceVariantInt: Int,
        override val primaryContainerInt: Int
    ) : Theme()

    @Stable
    data class BlackAndWhite(
        override val accentColorInt: Int,
        override val primaryColorInt: Int,
        override val backgroundColorInt: Int,
        override val appIconColorInt: Int,
        override val textColorInt: Int,
        override val surfaceVariantInt: Int,
        override val primaryContainerInt: Int
    ) : Theme()

    @Stable
    data class Custom(
        override val accentColorInt: Int,
        override val primaryColorInt: Int,
        override val backgroundColorInt: Int,
        override val appIconColorInt: Int,
        override val textColorInt: Int,
        override val surfaceVariantInt: Int,
        override val primaryContainerInt: Int
    ) : Theme()
    
    companion object {
        @Composable
        fun systemDefaultMaterialYou(): SystemDefaultMaterialYou {
            val context = LocalContext.current
            val config = remember { context.config }
            return SystemDefaultMaterialYou(
                accentColorInt = if (config.isUsingAccentColor) config.accentColor else config.primaryColor,
                appIconColorInt = config.appIconColor,
                primaryColorInt = config.primaryColor,
                backgroundColorInt = config.backgroundColor,
                textColorInt = if (isSPlus()) colorResource(R.color.you_neutral_text_color).toArgb() else (if (isInDarkThemeAndSurfaceIsNotLitWell()) Color.White else Color.Black).toArgb(),
                surfaceVariantInt = if (isSPlus()) colorResource(R.color.you_status_bar_color).toArgb() else (if (isInDarkThemeAndSurfaceIsNotLitWell()) Color.White else Color.Black).toArgb(),
                primaryContainerInt = if (isSPlus()) colorResource(R.color.you_primary_container).toArgb() else (if (isInDarkThemeAndSurfaceIsNotLitWell()) Color.White else Color.Black).toArgb()
            )
        }
    }
}
