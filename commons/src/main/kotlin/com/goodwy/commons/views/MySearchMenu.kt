package com.goodwy.commons.views

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import com.google.android.material.appbar.MaterialToolbar
import com.goodwy.commons.R
import com.goodwy.commons.activities.BaseSimpleActivity
import com.goodwy.commons.databinding.MenuSearchBinding
import com.goodwy.commons.extensions.applyColorFilter
import com.goodwy.commons.extensions.baseConfig
import com.goodwy.commons.extensions.beVisibleIf
import com.goodwy.commons.extensions.getColoredMaterialSearchBarColor
import com.goodwy.commons.extensions.getContrastColor
import com.goodwy.commons.extensions.getProperBackgroundColor
import com.goodwy.commons.extensions.getProperPrimaryColor
import com.goodwy.commons.extensions.getProperTextCursorColor
import com.goodwy.commons.extensions.getSurfaceColor
import com.goodwy.commons.extensions.hideKeyboard
import com.goodwy.commons.extensions.isDynamicTheme
import com.goodwy.commons.extensions.isSystemInDarkMode
import com.goodwy.commons.extensions.onTextChangeListener
import com.goodwy.commons.extensions.showKeyboard

open class MySearchMenu(context: Context, attrs: AttributeSet) : MyAppBarLayout(context, attrs) {
    var isSearchOpen = false
    var useArrowIcon = false
    var onSearchOpenListener: (() -> Unit)? = null
    var onSearchClosedListener: (() -> Unit)? = null
    var onSearchTextChangedListener: ((text: String) -> Unit)? = null
    var onNavigateBackClickListener: (() -> Unit)? = null
    var showSpeechToText = false
    var onSpeechToTextClickListener: (() -> Unit)? = null
    var inFocus = false

    val binding = MenuSearchBinding.inflate(LayoutInflater.from(context), this)

    override val toolbar: MaterialToolbar?
        get() = binding.topToolbar

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

    @Suppress("unused", "EmptyFunctionBlock")
    @Deprecated("This feature is broken for now.")
    fun toggleHideOnScroll(hideOnScroll: Boolean) {}

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

    fun updateColors(background: Int = context.getProperBackgroundColor(), scrollOffset: Int = 0) {
        val contrastColor = background.getContrastColor()
        val primaryColor = context.getProperPrimaryColor()
        val surfaceColor = when {
            context.isDynamicTheme() && !context.isSystemInDarkMode() -> context.getProperBackgroundColor()
            else -> context.getSurfaceColor()
        }
        val searchHolderColor = if (scrollOffset == 0) surfaceColor else context.getColoredMaterialSearchBarColor()

        setBackgroundColor(background)
        binding.searchBarContainer.setBackgroundColor(background)
        binding.topToolbarSearchIcon.applyColorFilter(contrastColor)
        binding.topToolbarSearchSpeechToText.applyColorFilter(contrastColor)
        binding.topToolbarSearch.setColors(
            contrastColor,
            primaryColor,
            context.getProperTextCursorColor()
        )
        (context as? BaseSimpleActivity)?.updateTopBarColors(this, background)

        binding.topToolbarSearchHolder.setBackgroundResource(R.drawable.search_bg)
        binding.topToolbarSearchHolder.backgroundTintList = ColorStateList.valueOf(searchHolderColor)
        binding.topToolbarSearchClear.applyColorFilter(contrastColor)

        if (context.baseConfig.topAppBarColorTitle) binding.topToolbar.setTitleTextColor(ColorStateList.valueOf(primaryColor))
    }

    fun updateTitle(title: String) {
        binding.topToolbar.title = title
    }

    fun searchBeVisibleIf(visible: Boolean = true) {
        binding.topToolbarSearchHolder.beVisibleIf(visible)
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
