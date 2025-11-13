package com.goodwy.commons.samples.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goodwy.commons.activities.BaseSimpleActivity
import com.goodwy.commons.compose.extensions.config
import com.goodwy.commons.dialogs.WhatsNewDialog
import com.goodwy.commons.extensions.*
import com.goodwy.commons.models.Release
import com.goodwy.commons.samples.BuildConfig
import com.goodwy.commons.samples.R
import com.goodwy.commons.samples.adapters.TestAdapter
import com.goodwy.commons.samples.databinding.ActivityTestBinding
import com.goodwy.commons.views.MyRecyclerView

class TestActivity : BaseSimpleActivity() {
    override var isSearchBarEnabled = true

    private val binding by viewBinding(ActivityTestBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        appLaunched(BuildConfig.APPLICATION_ID)
        setupOptionsMenu()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = TestAdapter(
            activity = this,
            recyclerView = recyclerView as MyRecyclerView,
            itemClick = {}
        )

        binding.mainMenu.updateTitle(getAppLauncherName())
        scrollChange()
    }

    override fun onResume() {
        super.onResume()
        binding.mainMenu.updateColors(
            background = getStartRequiredStatusBarColor(),
            scrollOffset = scrollingView?.computeVerticalScrollOffset() ?: 0
        )
    }

    private fun scrollChange() {
        val myRecyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        scrollingView = myRecyclerView
        val scrollingViewOffset = scrollingView?.computeVerticalScrollOffset() ?: 0

        val useSurfaceColor = isDynamicTheme() && !isSystemInDarkMode()
        val backgroundColor = if (useSurfaceColor) getSurfaceColor() else getProperBackgroundColor()
        val statusBarColor = if (config.changeColourTopBar) getRequiredStatusBarColor(useSurfaceColor) else backgroundColor

        binding.mainMenu.updateColors(statusBarColor, scrollingViewOffset)
        setupSearchMenuScrollListener(myRecyclerView, binding.mainMenu, useSurfaceColor)
    }

    private fun setupOptionsMenu() {
        binding.mainMenu.requireToolbar().inflateMenu(com.goodwy.commons.R.menu.cab_delete_only)
        binding.mainMenu.requireToolbar().setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                com.goodwy.commons.R.id.cab_delete -> showWhatsNewDialog()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun showWhatsNewDialog() {
        arrayListOf<Release>().apply {
            add(Release(800, R.string.release_800))
            WhatsNewDialog(this@TestActivity, this)
        }
    }

    override fun getAppLauncherName() = getString(R.string.commons_app_name)

    override fun getAppIconIDs() = arrayListOf(
        R.mipmap.ic_launcher,
        R.mipmap.ic_launcher_one,
        R.mipmap.ic_launcher_two,
        R.mipmap.ic_launcher_three,
        R.mipmap.ic_launcher_four,
        R.mipmap.ic_launcher_five,
        R.mipmap.ic_launcher_six,
        R.mipmap.ic_launcher_seven,
        R.mipmap.ic_launcher_eight,
        R.mipmap.ic_launcher_nine,
        R.mipmap.ic_launcher_ten,
        R.mipmap.ic_launcher_eleven
    )

    override fun getRepositoryName() = "Gallery"
}
