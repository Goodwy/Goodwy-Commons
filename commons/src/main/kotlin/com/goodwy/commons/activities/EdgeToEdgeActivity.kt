package com.goodwy.commons.activities

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ScrollingView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.get
import androidx.core.view.size
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.goodwy.commons.R
import com.goodwy.commons.extensions.applyColorFilter
import com.goodwy.commons.extensions.baseConfig
import com.goodwy.commons.extensions.getColoredDrawableWithColor
import com.goodwy.commons.extensions.getColoredMaterialStatusBarColor
import com.goodwy.commons.extensions.getContrastColor
import com.goodwy.commons.extensions.getOverflowIcon
import com.goodwy.commons.extensions.getProperBackgroundColor
import com.goodwy.commons.extensions.getProperPrimaryColor
import com.goodwy.commons.extensions.getSurfaceColor
import com.goodwy.commons.extensions.isDynamicTheme
import com.goodwy.commons.extensions.isSystemInDarkMode
import com.goodwy.commons.extensions.onApplyWindowInsets
import com.goodwy.commons.extensions.setSystemBarsAppearance
import com.goodwy.commons.extensions.updateMarginWithBase
import com.goodwy.commons.extensions.updatePaddingWithBase
import com.goodwy.commons.helpers.OVERFLOW_ICON_VERTICAL
import com.goodwy.commons.views.MyAppBarLayout
import com.goodwy.commons.views.MySearchMenu

abstract class EdgeToEdgeActivity : AppCompatActivity() {
    open var isSearchBarEnabled = false
    open var updateSystemBarsAppearance = true
    open val padCutout: Boolean
        get() = true

    private var topAppBar: MyAppBarLayout? = null
    var mySearchMenu: MySearchMenu? = null
    var scrollingView: ScrollingView? = null
    private var materialScrollColorAnimation: ValueAnimator? = null
    var currentScrollY = 0
    var useOverflowIcon: Boolean = true

    private val contentRoot by lazy { findViewById<View>(android.R.id.content) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)
    }

    override fun onResume() {
        super.onResume()
        if (updateSystemBarsAppearance) window.setSystemBarsAppearance(getProperBackgroundColor())
    }

    /**
     * Helper for views that need to be edge to edge compatible.
     */
    fun setupEdgeToEdge(
        padTopSystem: List<View> = emptyList(),
        padBottomSystem: List<View> = emptyList(),
        padBottomImeAndSystem: List<View> = emptyList(),
        moveTopSystem: List<View> = emptyList(),
        moveBottomSystem: List<View> = emptyList(),
        animateIme: Boolean = false,
    ) {
        onApplyWindowInsets { insets ->
            val system = insets.getInsetsIgnoringVisibility(Type.systemBars())
            val imeAndSystem = insets.getInsets(Type.ime() or Type.systemBars())

            padTopSystem.forEach { it.updatePaddingWithBase(top = system.top) }
            padBottomSystem.forEach { it.updatePaddingWithBase(bottom = system.bottom) }
            padBottomImeAndSystem.forEach { it.updatePaddingWithBase(bottom = imeAndSystem.bottom) }
            moveTopSystem.forEach { it.updateMarginWithBase(top = system.top) }
            moveBottomSystem.forEach { it.updateMarginWithBase(bottom = system.bottom) }

            if (padCutout) {
                val cutout = insets.getInsets(Type.displayCutout())
                val sideLeft = maxOf(system.left, cutout.left)
                val sideRight = maxOf(system.right, cutout.right)
                contentRoot.updatePaddingWithBase(left = sideLeft, right = sideRight)
            }

            if (animateIme) {
                ViewCompat.setWindowInsetsAnimationCallback(
                    contentRoot,
                    object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE) {
                        override fun onProgress(
                            insets: WindowInsetsCompat,
                            runningAnimations: MutableList<WindowInsetsAnimationCompat>
                        ): WindowInsetsCompat {
                            val bottom = insets.getInsets(Type.systemBars() or Type.ime()).bottom
                            padBottomImeAndSystem.forEach {
                                it.updatePaddingWithBase(bottom = bottom)
                            }
                            return insets
                        }
                    }
                )
            }
        }
    }

    fun setupMaterialScrollListener(scrollingView: ScrollingView?, topAppBar: MyAppBarLayout, surfaceColor: Boolean = false) {
        this.scrollingView = scrollingView
        this.topAppBar = topAppBar
        if (scrollingView is RecyclerView) {
            scrollingView.setOnScrollChangeListener { _, _, _, _, _ ->
                val newScrollY = scrollingView.computeVerticalScrollOffset()
                if (newScrollY == 0 || currentScrollY == 0) scrollingChanged(newScrollY, currentScrollY, surfaceColor = surfaceColor)
                currentScrollY = newScrollY
            }
        } else if (scrollingView is NestedScrollView) {
            scrollingView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                if (scrollY == 0 || oldScrollY == 0) scrollingChanged(scrollY, oldScrollY, surfaceColor = surfaceColor)
            }
        }
    }

    fun setupSearchMenuScrollListener(scrollingView: ScrollingView?, searchMenu: MySearchMenu, surfaceColor: Boolean = false) {
        this.scrollingView = scrollingView
        this.mySearchMenu = searchMenu
        if (scrollingView is RecyclerView) {
            scrollingView.setOnScrollChangeListener { _, _, _, _, _ ->
                val newScrollY = scrollingView.computeVerticalScrollOffset()
                if (newScrollY == 0 || currentScrollY == 0) scrollingChanged(newScrollY, currentScrollY, true, surfaceColor)
                currentScrollY = newScrollY
            }
        } else if (scrollingView is NestedScrollView) {
            scrollingView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                if (scrollY == 0 || oldScrollY == 0) scrollingChanged(scrollY, oldScrollY, true, surfaceColor)
            }
        }
    }

    private fun scrollingChanged(newScrollY: Int, oldScrollY: Int, isMySearchMenu: Boolean = false, surfaceColor: Boolean) {
        if (newScrollY > 0 && oldScrollY == 0) {
            val colorFrom = if (surfaceColor) getSurfaceColor() else getProperBackgroundColor()
            val colorTo = getColoredMaterialStatusBarColor()
            if (isMySearchMenu) animateMySearchMenuColors(colorFrom, colorTo)
            else  animateTopBarColors(colorFrom, colorTo)
        } else if (newScrollY == 0 && oldScrollY > 0) {
            val colorFrom = if (surfaceColor) getSurfaceColor() else getProperBackgroundColor()
            val colorTo = getRequiredStatusBarColor(surfaceColor)
            if (isMySearchMenu) animateMySearchMenuColors(colorFrom, colorTo)
            else animateTopBarColors(colorFrom, colorTo)
        }
    }

    fun animateTopBarColors(colorFrom: Int, colorTo: Int) {
        if (topAppBar == null) {
            return
        }

        materialScrollColorAnimation?.end()
        materialScrollColorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        materialScrollColorAnimation!!.addUpdateListener { animator ->
            val color = animator.animatedValue as Int
            if (topAppBar != null) {
                updateTopBarColors(topAppBar!!, color)
            }
        }

        materialScrollColorAnimation!!.start()
    }

    fun animateMySearchMenuColors(colorFrom: Int, colorTo: Int) {
        if (mySearchMenu == null) {
            return
        }

        materialScrollColorAnimation?.end()
        materialScrollColorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        materialScrollColorAnimation!!.addUpdateListener { animator ->
            val color = animator.animatedValue as Int
            if (mySearchMenu != null) {
                mySearchMenu!!.updateColors(color, scrollingView?.computeVerticalScrollOffset() ?: 0)
            }
        }

        materialScrollColorAnimation!!.start()
    }

    fun getRequiredStatusBarColor(surfaceColor: Boolean = false): Int {
        val scrollingViewOffset = scrollingView?.computeVerticalScrollOffset() ?: 0
        return if ((scrollingView is RecyclerView || scrollingView is NestedScrollView) && scrollingViewOffset == 0) {
            if (surfaceColor) getSurfaceColor() else getProperBackgroundColor()
        } else {
            getColoredMaterialStatusBarColor()
        }
    }

    fun getStartRequiredStatusBarColor(): Int {
        val useSurfaceColor = isDynamicTheme() && !isSystemInDarkMode()
        val scrollingViewOffset = scrollingView?.computeVerticalScrollOffset() ?: 0
        return if (scrollingViewOffset == 0) {
            if (useSurfaceColor) getSurfaceColor() else getProperBackgroundColor()
        } else {
            getColoredMaterialStatusBarColor()
        }
    }

    fun getRequiredTopBarColor(): Int {
        return if (
            (scrollingView is RecyclerView || scrollingView is NestedScrollView)
            && scrollingView?.computeVerticalScrollOffset() == 0
        ) {
            getProperBackgroundColor()
        } else {
            getColoredMaterialStatusBarColor()
        }
    }

    fun updateTopBarColors(
        topAppBar: MyAppBarLayout,
        colorBackground: Int,
        colorPrimary: Int = getProperPrimaryColor(),
        topAppBarColorIcon: Boolean = baseConfig.topAppBarColorIcon,
        topAppBarColorTitle: Boolean = baseConfig.topAppBarColorTitle
    ) {

        val getProperBackgroundColor = getProperBackgroundColor()
        val contrastColor =
            if (colorBackground == Color.TRANSPARENT) getProperBackgroundColor.getContrastColor()
            else colorBackground.getContrastColor()
        val itemColor = if (topAppBarColorIcon) colorPrimary else contrastColor
        val titleColor = if (topAppBarColorTitle) colorPrimary else contrastColor

        window.setSystemBarsAppearance(colorBackground)
        topAppBar.setBackgroundColor(colorBackground)
        topAppBar.toolbar?.setTitleTextColor(titleColor)
        topAppBar.toolbar?.navigationIcon?.applyColorFilter(itemColor)
        topAppBar.toolbar?.collapseIcon =
            resources.getColoredDrawableWithColor(this, R.drawable.ic_chevron_left_vector, itemColor)
        val overflowIconRes =
            if (useOverflowIcon) getOverflowIcon(baseConfig.overflowIcon) else getOverflowIcon(OVERFLOW_ICON_VERTICAL)
        topAppBar.toolbar?.overflowIcon =
            resources.getColoredDrawableWithColor(this, overflowIconRes, itemColor)

        val menu = topAppBar.toolbar?.menu ?: return
        for (i in 0 until menu.size) {
            try {
                menu[i].icon?.setTint(itemColor)
            } catch (_: Exception) {
            }
        }
    }

    fun updateToolbarColors(
        toolbar: Toolbar,
        colorBackground: Int,
        colorPrimary: Int = getProperPrimaryColor(),
        useOverflowIcon: Boolean = true,
        topAppBarColorIcon: Boolean = baseConfig.topAppBarColorIcon,
        topAppBarColorTitle: Boolean = baseConfig.topAppBarColorTitle
    ) {
        val getProperBackgroundColor = getProperBackgroundColor()
        val contrastColor = if (colorBackground == Color.TRANSPARENT) getProperBackgroundColor.getContrastColor() else colorBackground.getContrastColor()
        val itemColor = if (topAppBarColorIcon) colorPrimary else contrastColor
        val titleColor = if (topAppBarColorTitle) colorPrimary else contrastColor

        window.setSystemBarsAppearance(colorBackground)
        toolbar.setTitleTextColor(titleColor)
        toolbar.navigationIcon?.applyColorFilter(itemColor)
        toolbar.collapseIcon = resources.getColoredDrawableWithColor(this, R.drawable.ic_chevron_left_vector, itemColor)

        val overflowIconRes = if (useOverflowIcon) getOverflowIcon(baseConfig.overflowIcon) else getOverflowIcon(OVERFLOW_ICON_VERTICAL)
        toolbar.overflowIcon = resources.getColoredDrawableWithColor(this, overflowIconRes, itemColor)

        val menu = toolbar.menu
        for (i in 0 until menu.size) {
            try {
                menu[i].icon?.setTint(itemColor)
            } catch (_: Exception) {
            }
        }
    }

    override fun onDestroy() {
        materialScrollColorAnimation?.cancel()
        materialScrollColorAnimation = null
        super.onDestroy()
    }
}
