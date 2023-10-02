package com.goodwy.commons.compose.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

@get:ReadOnlyComposable
val disabledTextColor @Composable get() = if (isInDarkThemeOrSurfaceIsNotLitWell()) Color.DarkGray else Color.LightGray

@get:ReadOnlyComposable
val textSubTitleColor
    @Composable get() = if (isInDarkThemeOrSurfaceIsNotLitWell()) {
        Color.White.copy(0.5f)
    } else {
        Color.Black.copy(
            0.5f,
        )
    }

@get:ReadOnlyComposable
val iconsColor
    @Composable get() = if (isSurfaceNotLitWell()) {
        Color.White
    } else {
        Color.Black
    }


@Composable
@ReadOnlyComposable
fun preferenceValueColor(isEnabled: Boolean) =
    if (isEnabled) textSubTitleColor else disabledTextColor

@Composable
@ReadOnlyComposable
fun preferenceLabelColor(isEnabled: Boolean) = if (isEnabled) MaterialTheme.colorScheme.onSurface else disabledTextColor

fun Color.isLitWell(threshold: Float = LUMINANCE_THRESHOLD) = luminance() > threshold

fun Color.isNotLitWell(threshold: Float = LUMINANCE_THRESHOLD) = luminance() < threshold
