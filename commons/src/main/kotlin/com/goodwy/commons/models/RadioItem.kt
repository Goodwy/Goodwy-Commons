package com.goodwy.commons.models

import android.graphics.drawable.Drawable

data class RadioItem(val id: Int, val title: String, val value: Any = id, val icon: Int? = null, val drawable: Drawable? = null)
