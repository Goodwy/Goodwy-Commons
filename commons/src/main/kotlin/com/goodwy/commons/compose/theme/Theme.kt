package com.goodwy.commons.compose.theme

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.DoNotInline
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Typeface
import com.goodwy.commons.compose.extensions.config
import com.goodwy.commons.compose.theme.model.Theme
import com.goodwy.commons.compose.theme.model.Theme.Companion.systemDefaultMaterialYou
import com.goodwy.commons.extensions.getContrastColor
import com.goodwy.commons.extensions.lightenColor
import com.goodwy.commons.helpers.FONT_TYPE_CUSTOM
import com.goodwy.commons.helpers.FONT_TYPE_MONOSPACE
import com.goodwy.commons.helpers.FontHelper
import com.goodwy.commons.helpers.isSPlus

@Composable
internal fun Theme(
    theme: Theme = systemDefaultMaterialYou(),
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    val context = LocalContext.current
//    val configuration = LocalConfiguration.current
    val baseConfig = remember { context.config }
    val isSystemInDarkTheme = isSystemInDarkTheme()

    val colorScheme = if (!view.isInEditMode) {
        when {
            theme is Theme.SystemDefaultMaterialYou && isSPlus() -> {
                if (isSystemInDarkTheme) {
                    dynamicDarkColorScheme(context).copy(
                        background = ColorResourceHelper.getColor(context, android.R.color.system_neutral1_1000),
                        surface = ColorResourceHelper.getColor(context, android.R.color.system_neutral1_900),
                        secondary = theme.accentColor,
                    )
                } else {
                    dynamicLightColorScheme(context).copy(
                        background = ColorResourceHelper.getColor(context, android.R.color.system_neutral1_50),
                        surface = ColorResourceHelper.getColor(context, android.R.color.system_neutral1_0), //system_neutral1_10
                        secondary = theme.accentColor,
                    )
                }
            }

            theme is Theme.Light -> lightColorScheme(
                primary = theme.primaryColor,
                onPrimary = Color(theme.primaryColorInt.getContrastColor()),
                secondary = theme.accentColor,
                background = theme.backgroundColor,
                onBackground = theme.textColor,
                surface = bottom_tabs_light_background, // card color
                onSurface = theme.textColor, // text color
                surfaceVariant = theme.surfaceVariant,
                primaryContainer = theme.primaryContainer,
                onPrimaryContainer = Color(theme.primaryContainerInt.getContrastColor())
            )

            theme is Theme.Gray -> lightColorScheme(
                primary = theme.primaryColor,
                onPrimary = Color(theme.primaryColorInt.getContrastColor()),
                secondary = theme.accentColor,
                background = theme.backgroundColor,
                onBackground = theme.textColor,
                surface = bottom_tabs_gray_background,
                onSurface = theme.textColor,
                surfaceVariant = theme.surfaceVariant,
                primaryContainer = theme.primaryContainer,
                onPrimaryContainer = Color(theme.primaryContainerInt.getContrastColor())
            )

            theme is Theme.Dark -> darkColorScheme(
                primary = theme.primaryColor,
                onPrimary = Color(theme.primaryColorInt.getContrastColor()),
                secondary = theme.accentColor,
                background = theme.backgroundColor,
                onBackground = theme.textColor,
                surface = bottom_tabs_dark_background,
                onSurface = theme.textColor,
                surfaceVariant = theme.surfaceVariant,
                primaryContainer = theme.primaryContainer,
                onPrimaryContainer = Color(theme.primaryContainerInt.getContrastColor())
            )

            theme is Theme.Black -> darkColorScheme(
                primary = theme.primaryColor,
                onPrimary = Color(theme.primaryColorInt.getContrastColor()),
                secondary = theme.accentColor,
                background = theme.backgroundColor,
                onBackground = theme.textColor,
                surface = bottom_tabs_black_background,
                onSurface = theme.textColor,
                surfaceVariant = theme.surfaceVariant,
                primaryContainer = theme.primaryContainer,
                onPrimaryContainer = Color(theme.primaryContainerInt.getContrastColor())
            )

            theme is Theme.Custom && theme.backgroundColor.isLitWell() -> lightColorScheme(
                primary = theme.primaryColor,
                onPrimary = Color(theme.primaryColorInt.getContrastColor()),
                secondary = theme.accentColor,
                background = theme.backgroundColor,
                onBackground = theme.textColor,
                surface = Color(theme.backgroundColorInt.lightenColor(8)),
                onSurface = theme.textColor, // text color
                surfaceVariant = theme.surfaceVariant,
                primaryContainer = theme.primaryContainer,
                onPrimaryContainer = Color(theme.primaryContainerInt.getContrastColor())
            )

            theme is Theme.Custom -> darkColorScheme(
                primary = theme.primaryColor,
                onPrimary = Color(theme.primaryColorInt.getContrastColor()),
                secondary = theme.accentColor,
                background = theme.backgroundColor,
                onBackground = theme.textColor,
                surface = Color(theme.backgroundColorInt.lightenColor(8)),
                onSurface = theme.textColor,
                surfaceVariant = theme.surfaceVariant,
                primaryContainer = theme.primaryContainer,
                onPrimaryContainer = Color(theme.primaryContainerInt.getContrastColor())
            )

            else -> lightColorScheme
        }
    } else {
        previewColorScheme()
    }

    SideEffect {
        updateRecentsAppIcon(baseConfig, context)
    }

    val dimensions = CommonDimensions

    val customTypography = if (!view.isInEditMode) {
        getCustomTypography(context)
    } else {
        Typography()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = customTypography,
        shapes = Shapes,
        content = {
            CompositionLocalProvider(
                LocalRippleConfiguration provides dynamicRippleConfiguration(),
                LocalTheme provides theme,
                LocalDimensions provides dimensions
            ) {
                content()
            }
        },
    )
}

@Composable
private fun getCustomTypography(context: Context): Typography {
    val baseConfig = context.config
    val fontType = baseConfig.fontType

    val fontFamily = when (fontType) {
        FONT_TYPE_MONOSPACE -> FontFamily.Monospace
        FONT_TYPE_CUSTOM -> {
            val typeface = FontHelper.getTypeface(context)
            FontFamily(Typeface(typeface))
        }
        else -> FontFamily.Default
    }

    val defaultTypography = Typography()
    return Typography(
        displayLarge = defaultTypography.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = defaultTypography.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = defaultTypography.titleLarge.copy(fontFamily = fontFamily),
        titleMedium = defaultTypography.titleMedium.copy(fontFamily = fontFamily),
        titleSmall = defaultTypography.titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = defaultTypography.bodySmall.copy(fontFamily = fontFamily),
        labelLarge = defaultTypography.labelLarge.copy(fontFamily = fontFamily),
        labelMedium = defaultTypography.labelMedium.copy(fontFamily = fontFamily),
        labelSmall = defaultTypography.labelSmall.copy(fontFamily = fontFamily),
    )
}

val LocalTheme: ProvidableCompositionLocal<Theme> =
    staticCompositionLocalOf { Theme.Custom(1, 1, 1, 1, 1, 1, 1) }

@Composable
private fun previewColorScheme() = if (isSystemInDarkTheme()) {
    darkColorScheme
} else {
    lightColorScheme
}

private object ColorResourceHelper {
    @DoNotInline
    fun getColor(context: Context, @ColorRes id: Int): Color {
        return Color(context.resources.getColor(id, context.theme))
    }
}

