package com.goodwy.commons.models

data class SharedTheme(val textColor: Int, val backgroundColor: Int, val primaryColor: Int, val appIconColor: Int, val navigationBarColor: Int,
                       val lastUpdatedTS: Int = 0, val accentColor: Int)
