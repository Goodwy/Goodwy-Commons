package com.goodwy.commons.views

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import com.google.android.material.appbar.AppBarLayout
import com.goodwy.commons.R
import com.goodwy.commons.activities.BaseSimpleActivity
import com.goodwy.commons.databinding.MenuSearchTopBinding
import com.goodwy.commons.extensions.*

open class MySearchMenuTop(context: Context, attrs: AttributeSet) : AppBarLayout(context, attrs) {
    var isSearchOpen = false
    var useArrowIcon = false
    var onSearchOpenListener: (() -> Unit)? = null
    var onSearchClosedListener: (() -> Unit)? = null
    var onSearchTextChangedListener: ((text: String) -> Unit)? = null
    var onNavigateBackClickListener: (() -> Unit)? = null
    var showSpeechToText = false
    var onSpeechToTextClickListener: (() -> Unit)? = null
    var inFocus = false

    val binding = MenuSearchTopBinding.inflate(LayoutInflater.from(context), this, true)

    fun getToolbar() = binding.topToolbar

    fun setupMenu() {
        binding.topToolbarSearchIcon.setOnClickListener {
            if (isSearchOpen) {
                closeSearch()
            } else if (useArrowIcon && onNavigateBackClickListener != null) {
                onNavigateBackClickListener!!()
            } else {
                binding.topToolbarSearch.requestFocus()
                (context as? Activity)?.showKeyboard(binding.topToolbarSearch)
            }
        }

        post {
            binding.topToolbarSearch.setOnFocusChangeListener { v, hasFocus ->
                inFocus = hasFocus
                if (hasFocus) {
                    openSearch()
                }
            }
        }

        binding.topToolbarSearch.onTextChangeListener { text ->
            onSearchTextChangedListener?.invoke(text)
        }

        binding.topToolbarSearchSpeechToText.apply {
            beVisibleIf(showSpeechToText && binding.topToolbarSearch.text!!.isEmpty())
            setOnClickListener {
                onSpeechToTextClickListener?.invoke()
            }
        }
    }

    fun focusView() {
        binding.topToolbarSearch.requestFocus()
    }

    private fun openSearch() {
        isSearchOpen = true
        onSearchOpenListener?.invoke()
        binding.topToolbarSearchIcon.setImageResource(R.drawable.ic_chevron_left_vector)
        binding.topToolbarSearchIcon.contentDescription = resources.getString(R.string.back)
    }

    fun closeSearch() {
        isSearchOpen = false
        onSearchClosedListener?.invoke()
        binding.topToolbarSearch.setText("")
        if (!useArrowIcon) {
            binding.topToolbarSearchIcon.setImageResource(R.drawable.ic_search_vector)
            binding.topToolbarSearchIcon.contentDescription = resources.getString(R.string.search)
        }
        (context as? Activity)?.hideKeyboard()
    }

    fun getCurrentQuery() = binding.topToolbarSearch.text.toString()

    fun updateHintText(text: String) {
        binding.topToolbarSearch.hint = text
    }

    fun toggleHideOnScroll(hideOnScroll: Boolean) {
        val params = binding.topAppBarLayout.layoutParams as LayoutParams
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

        binding.topToolbarSearchIcon.setImageResource(icon)
        binding.topToolbarSearchIcon.contentDescription = resources.getString(accessibilityString)
    }

    fun updateColors(background: Int = context.getProperBackgroundColor()) {
        val contrastColor = background.getContrastColor()
        val primaryColor = context.getProperPrimaryColor()
        val surfaceColor = context.getSurfaceColor()

        setBackgroundColor(background)
        binding.topAppBarLayout.setBackgroundColor(background)
        val searchIconColor = if (context.baseConfig.topAppBarColorIcon) primaryColor else contrastColor
        binding.topToolbarSearchIcon.applyColorFilter(searchIconColor)
        binding.topToolbarSearchSpeechToText.applyColorFilter(contrastColor)
        binding.topToolbarSearch.setColors(contrastColor, primaryColor, context.getProperTextCursorColor())
        (context as? BaseSimpleActivity)?.updateTopBarColors(binding.topToolbar, Color.TRANSPARENT, useColorForStatusBar = false)

        binding.topToolbarHolder.setBackgroundResource(R.drawable.search_bg)
        binding.topToolbarHolder.backgroundTintList = ColorStateList.valueOf(surfaceColor)
        binding.topToolbarSearchClear.applyColorFilter(contrastColor)
    }

    fun updateTitle(title: String) {
        binding.topToolbar.title = title
    }

    fun searchBeVisibleIf(visible: Boolean = true) {
        binding.topToolbarHolder.beVisibleIf(visible)
    }

    fun requestFocusAndShowKeyboard() {
        binding.topToolbarSearch.requestFocus()
        (context as? Activity)?.showKeyboard(binding.topToolbarSearch)
    }

    fun clearSearch() {
        val showClear = binding.topToolbarSearch.text!!.isNotEmpty()
        binding.topToolbarSearchSpeechToText.beVisibleIf(showSpeechToText && !showClear)
        binding.topToolbarSearchClear.beVisibleIf(showClear)
        binding.topToolbarSearchClear.setOnClickListener {
            if (inFocus) binding.topToolbarSearch.setText("")
            else closeSearch()
        }
    }

    fun setText(text: String) {
        binding.topToolbarSearch.setText(text)
        openSearch()
    }
}
