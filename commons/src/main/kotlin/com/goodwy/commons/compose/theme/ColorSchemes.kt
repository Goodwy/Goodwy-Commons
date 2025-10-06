package com.goodwy.commons.compose.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

internal val darkColorScheme = darkColorScheme(
    primary = color_primary,
    onPrimary = color_on_primary,
    secondary = color_primary_dark,
    tertiary = color_accent,
    background = color_background_dark,
    surface = bottom_tabs_black_background,
    onSurface = color_text_dark,
    surfaceVariant = color_background_dark,
    primaryContainer = color_background_dark,
)
internal val lightColorScheme = lightColorScheme(
    primary = color_primary,
    onPrimary = color_on_primary,
    secondary = color_primary_dark,
    tertiary = color_accent,
    background = color_background_light,
    surface = bottom_tabs_light_background,
    onSurface = color_text_light,
    surfaceVariant = color_background_light,
    primaryContainer = color_background_light,
)
