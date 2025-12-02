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
import com.goodwy.commons.compose.extensions.config
import com.goodwy.commons.compose.theme.model.Theme
import com.goodwy.commons.compose.theme.model.Theme.Companion.systemDefaultMaterialYou
import com.goodwy.commons.extensions.getContrastColor
import com.goodwy.commons.extensions.lightenColor
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

    MaterialTheme(
        colorScheme = colorScheme,
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

