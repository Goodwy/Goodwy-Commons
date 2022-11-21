package com.goodwy.commons.extensions

import android.content.Context
import com.goodwy.commons.models.FileDirItem

fun FileDirItem.isRecycleBinPath(context: Context): Boolean {
    return path.startsWith(context.recycleBinPath)
}
