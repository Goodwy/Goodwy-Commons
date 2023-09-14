package com.goodwy.commons.views

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.appbar.AppBarLayout
import com.goodwy.commons.R
import com.goodwy.commons.activities.BaseSimpleActivity
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.LOWER_ALPHA
import com.goodwy.commons.helpers.MEDIUM_ALPHA
import com.goodwy.commons.helpers.isQPlus
import kotlinx.android.synthetic.main.menu_search.view.*
import java.lang.reflect.Field

class MySearchMenu(context: Context, attrs: AttributeSet) : AppBarLayout(context, attrs) {
    var isSearchOpen = false
    var useArrowIcon = false
    var onSearchOpenListener: (() -> Unit)? = null
    var onSearchClosedListener: (() -> Unit)? = null
    var onSearchTextChangedListener: ((text: String) -> Unit)? = null
    var onNavigateBackClickListener: (() -> Unit)? = null

    init {
        inflate(context, R.layout.menu_search, this)
    }

    fun getToolbar() = top_toolbar

    fun setupMenu() {
        top_toolbar_search_icon.setOnClickListener {
            if (isSearchOpen) {
                closeSearch()
            } else if (useArrowIcon && onNavigateBackClickListener != null) {
                onNavigateBackClickListener!!()
            } else {
                top_toolbar_search.requestFocus()
                (context as? Activity)?.showKeyboard(top_toolbar_search)
            }
        }

        post {
            top_toolbar_search.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    openSearch()
                }
            }
        }

        top_toolbar_search.onTextChangeListener { text ->
            onSearchTextChangedListener?.invoke(text)
        }
    }

    fun focusView() {
        top_toolbar_search.requestFocus()
    }

    private fun openSearch() {
        isSearchOpen = true
        onSearchOpenListener?.invoke()
        top_toolbar_search_icon.setImageResource(R.drawable.ic_chevron_left_vector)
        top_toolbar_search_icon.contentDescription = resources.getString(R.string.back)
    }

    fun closeSearch() {
        isSearchOpen = false
        onSearchClosedListener?.invoke()
        top_toolbar_search.setText("")
        if (!useArrowIcon) {
            top_toolbar_search_icon.setImageResource(R.drawable.ic_search_vector)
            top_toolbar_search_icon.contentDescription = resources.getString(R.string.search)
        }
        (context as? Activity)?.hideKeyboard()
    }

    fun getCurrentQuery() = top_toolbar_search.text.toString()

    fun updateHintText(text: String) {
        top_toolbar_search.hint = text
    }

    fun toggleHideOnScroll(hideOnScroll: Boolean) {
        val params = top_app_bar_layout.layoutParams as LayoutParams
        if (hideOnScroll) {
            params.scrollFlags = LayoutParams.SCROLL_FLAG_SCROLL or LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        } else {
            params.scrollFlags = params.scrollFlags.removeBit(LayoutParams.SCROLL_FLAG_SCROLL or LayoutParams.SCROLL_FLAG_ENTER_ALWAYS)
        }
    }

    fun toggleForceArrowBackIcon(useArrowBack: Boolean) {
        this.useArrowIcon = useArrowBack
        val (icon, accessibilityString) = if (useArrowBack) {
            Pair(R.drawable.ic_chevron_left_vector, R.string.back)
        } else {
            Pair(R.drawable.ic_search_vector, R.string.search)
        }

        top_toolbar_search_icon.setImageResource(icon)
        top_toolbar_search_icon.contentDescription = resources.getString(accessibilityString)
    }

    fun updateColors() {
        val backgroundColor = context.getProperBackgroundColor()
        val contrastColor = backgroundColor.getContrastColor()
        val primaryColor = context.getProperPrimaryColor()

        setBackgroundColor(backgroundColor)
        top_app_bar_layout.setBackgroundColor(backgroundColor)
        top_toolbar_search_icon.applyColorFilter(contrastColor)
        top_toolbar_holder.background?.applyColorFilter(primaryColor.adjustAlpha(LOWER_ALPHA))
        //top_toolbar_search.setTextColor(contrastColor)
        //top_toolbar_search.setHintTextColor(contrastColor.adjustAlpha(MEDIUM_ALPHA))
        top_toolbar_search.setColors(contrastColor, primaryColor, context.getProperTextCursorColor())
        (context as? BaseSimpleActivity)?.updateTopBarColors(top_toolbar, backgroundColor)

        top_toolbar_search_holder.setBackgroundResource(R.drawable.search_bg)
        top_toolbar_search_holder.backgroundTintList = ColorStateList.valueOf(context.getBottomNavigationBackgroundColor())
        top_toolbar_search_clear.applyColorFilter(contrastColor)
    }

    fun updateTitle(title: String) {
        top_toolbar.title = title
    }

    fun searchBeVisibleIf(visible: Boolean = true) {
        top_toolbar_search_holder.beVisibleIf(visible)
    }

    fun requestFocusAndShowKeyboard() {
        top_toolbar_search.requestFocus()
        (context as? Activity)?.showKeyboard(top_toolbar_search)
    }

    fun clearSearch() {
        top_toolbar_search_clear.beVisibleIf(top_toolbar_search.text!!.isNotEmpty())
        top_toolbar_search_clear.setOnClickListener {
            top_toolbar_search.setText("")
        }
    }
}
