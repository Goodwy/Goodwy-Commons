package com.goodwy.commons.models

import androidx.compose.runtime.Immutable
import android.graphics.drawable.Drawable

@Immutable
data class RadioItem(val id: Int, val title: String, val value: Any = id, val icon: Int? = null, val drawable: Drawable? = null)
