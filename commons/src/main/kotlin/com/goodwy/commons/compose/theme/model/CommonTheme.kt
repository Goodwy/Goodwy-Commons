package com.goodwy.commons.compose.theme.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
interface CommonTheme {
    val accentColorInt: Int
    val primaryColorInt: Int
    val backgroundColorInt: Int
    val appIconColorInt: Int
    val textColorInt: Int
    val surfaceVariantInt: Int
    val primaryContainerInt: Int

    val accentColor get() = Color(accentColorInt)
    val primaryColor get() = Color(primaryColorInt)
    val backgroundColor get() = Color(backgroundColorInt)
    val appIconColor get() = Color(appIconColorInt)
    val textColor get() = Color(textColorInt)
    val surfaceVariant get() = Color(surfaceVariantInt)
    val primaryContainer get() = Color(primaryContainerInt)

}
