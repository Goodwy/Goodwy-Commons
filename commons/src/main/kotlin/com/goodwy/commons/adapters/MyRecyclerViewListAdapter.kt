package com.goodwy.commons.adapters

import android.annotation.SuppressLint
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.ActionMenuView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.goodwy.commons.R
import com.goodwy.commons.activities.BaseSimpleActivity
import com.goodwy.commons.extensions.*
import com.goodwy.commons.helpers.CONTACT_THUMBNAILS_SIZE_EXTRA_LARGE
import com.goodwy.commons.helpers.CONTACT_THUMBNAILS_SIZE_LARGE
import com.goodwy.commons.helpers.CONTACT_THUMBNAILS_SIZE_SMALL
import com.goodwy.commons.interfaces.MyActionModeCallback
import com.goodwy.commons.models.RecyclerSelectionPayload
import com.goodwy.commons.views.BottomPaddingDecoration
import com.goodwy.commons.views.MyDividerDecoration
import com.goodwy.commons.views.MyRecyclerView
import kotlin.math.max
import kotlin.math.min

abstract class MyRecyclerViewListAdapter<T>(
    val activity: BaseSimpleActivity,
    val recyclerView: MyRecyclerView,
    diffUtil: DiffUtil.ItemCallback<T>,
    val itemClick: (T) -> Unit,
    val onRefresh: () -> Unit = {},
) : ListAdapter<T, MyRecyclerViewListAdapter<T>.ViewHolder>(diffUtil) {
    protected val baseConfig = activity.baseConfig
    protected val resources = activity.resources!!
    protected val layoutInflater = activity.layoutInflater
    protected var accentColor = activity.getProperAccentColor()
    protected var textColor = activity.getProperTextColor()
    protected var backgroundColor = activity.getProperBackgroundColor()
    protected var surfaceColor = activity.getSurfaceColor()
    protected var properPrimaryColor = activity.getProperPrimaryColor()
    protected var contrastColor = properPrimaryColor.getContrastColor()
    protected var contactThumbnailsSize = contactThumbnailsSize()
    protected var actModeCallback: MyActionModeCallback
    protected var selectedKeys = LinkedHashSet<Int>()
    protected var positionOffset = 0
    protected var actMode: ActionMode? = null

    private var actBarTextView: TextView? = null
    private var lastLongPressedItem = -1

    private var isDividersVisible = false
    private var dividerDecoration: MyDividerDecoration? = null
    private var bottomPaddingDecoration: BottomPaddingDecoration? = null

    abstract fun getActionMenuId(): Int

    abstract fun prepareActionMode(menu: Menu)

    abstract fun actionItemPressed(id: Int)

    abstract fun getSelectableItemCount(): Int

    abstract fun getIsItemSelectable(position: Int): Boolean

    abstract fun getItemSelectionKey(position: Int): Int?

    abstract fun getItemKeyPosition(key: Int): Int

    abstract fun onActionModeCreated()

    abstract fun onActionModeDestroyed()

    protected fun isOneItemSelected() = selectedKeys.size == 1

    init {
        actModeCallback = object : MyActionModeCallback() {
            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                actionItemPressed(item.itemId)
                return true
            }

            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onCreateActionMode(actionMode: ActionMode, menu: Menu?): Boolean {
                if (getActionMenuId() == 0) {
                    return true
                }

                isSelectable = true
                actMode = actionMode
                actBarTextView = layoutInflater.inflate(R.layout.actionbar_title, null) as TextView
                actBarTextView!!.layoutParams = ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
                actMode!!.customView = actBarTextView
                actBarTextView!!.setOnClickListener {
                    if (getSelectableItemCount() == selectedKeys.size) {
                        finishActMode()
                    } else {
                        selectAll()
                    }
                }

                activity.menuInflater.inflate(getActionMenuId(), menu)

//                val cabBackgroundColor = if (activity.isDynamicTheme()) {
//                    resources.getColor(R.color.you_contextual_status_bar_color, activity.theme)
//                } else {
//                    activity.getColoredMaterialStatusBarColor()
//                }
                val cabBackgroundColor = activity.getColoredMaterialStatusBarColor()
                val cabContrastColor = cabBackgroundColor.getContrastColor()

                val actModeBar = actMode!!.customView?.parent as? View
                actModeBar?.setBackgroundColor(cabBackgroundColor)

                actBarTextView!!.setTextColor(cabContrastColor)
                activity.updateMenuItemColors(menu, baseColor = cabBackgroundColor, forceWhiteIcons = false)
                onActionModeCreated()

                //if (activity.isDynamicTheme()) {
                    actBarTextView?.onGlobalLayout {
                        val backArrow = activity.findViewById<ImageView>(androidx.appcompat.R.id.action_mode_close_button)
                        backArrow?.setImageDrawable(resources.getDrawable(R.drawable.ic_chevron_left_vector, activity.theme))
                        backArrow?.applyColorFilter(cabContrastColor)

                        val cabView = backArrow?.parent as? ViewGroup
                        for (i in 0 until (cabView?.childCount ?: 0)) {
                            val child = cabView?.getChildAt(i)
                            if (child is ActionMenuView) {
                                // Colouring the overflow icon (three dots)
                                child.overflowIcon?.applyColorFilter(cabContrastColor)
                                break
                            }
                        }
                    }
                //}
                return true
            }

            override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                prepareActionMode(menu)
                return true
            }

            override fun onDestroyActionMode(actionMode: ActionMode) {
                isSelectable = false
                (selectedKeys.clone() as HashSet<Int>).forEach {
                    val position = getItemKeyPosition(it)
                    if (position != -1) {
                        toggleItemSelection(false, position, false)
                    }
                }

                updateTitle()
                selectedKeys.clear()
                actBarTextView?.text = ""
                actMode = null
                lastLongPressedItem = -1
                onActionModeDestroyed()
            }
        }
    }

    protected fun toggleItemSelection(select: Boolean, pos: Int, updateTitle: Boolean = true) {
        if (select && !getIsItemSelectable(pos)) {
            return
        }

        val itemKey = getItemSelectionKey(pos) ?: return
        if ((select && selectedKeys.contains(itemKey)) || (!select && !selectedKeys.contains(itemKey))) {
            return
        }

        if (select) {
            selectedKeys.add(itemKey)
        } else {
            selectedKeys.remove(itemKey)
        }

        notifyItemChanged(pos + positionOffset, RecyclerSelectionPayload(select))

        if (updateTitle) {
            updateTitle()
        }

        if (selectedKeys.isEmpty()) {
            finishActMode()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        val any = payloads.firstOrNull()
        if (any is RecyclerSelectionPayload) {
            holder.itemView.isSelected = any.selected
        } else {
            onBindViewHolder(holder, position)
        }
    }

    fun updateTitle() {
        val selectableItemCount = getSelectableItemCount()
        val selectedCount = min(selectedKeys.size, selectableItemCount)
        val oldTitle = actBarTextView?.text
        val newTitle = "$selectedCount / $selectableItemCount"
        if (oldTitle != newTitle) {
            actBarTextView?.text = newTitle
            actMode?.invalidate()
        }
    }

    fun itemLongClicked(position: Int) {
        recyclerView.setDragSelectActive(position)
        lastLongPressedItem = if (lastLongPressedItem == -1) {
            position
        } else {
            val min = min(lastLongPressedItem, position)
            val max = max(lastLongPressedItem, position)
            for (i in min..max) {
                toggleItemSelection(true, i, false)
            }
            updateTitle()
            position
        }
    }

    protected fun getSelectedItemPositions(sortDescending: Boolean = true): ArrayList<Int> {
        val positions = ArrayList<Int>()
        val keys = selectedKeys.toList()
        keys.forEach {
            val position = getItemKeyPosition(it)
            if (position != -1) {
                positions.add(position)
            }
        }

        if (sortDescending) {
            positions.sortDescending()
        }
        return positions
    }

    protected fun selectAll() {
        val cnt = itemCount - positionOffset
        for (i in 0 until cnt) {
            toggleItemSelection(true, i, false)
        }
        lastLongPressedItem = -1
        updateTitle()
    }

    protected fun setupDragListener(enable: Boolean) {
        if (enable) {
            recyclerView.setupDragListener(object : MyRecyclerView.MyDragListener {
                override fun selectItem(position: Int) {
                    toggleItemSelection(true, position, true)
                }

                override fun selectRange(initialSelection: Int, lastDraggedIndex: Int, minReached: Int, maxReached: Int) {
                    selectItemRange(
                        initialSelection,
                        max(0, lastDraggedIndex - positionOffset),
                        max(0, minReached - positionOffset),
                        maxReached - positionOffset
                    )
                    if (minReached != maxReached) {
                        lastLongPressedItem = -1
                    }
                }
            })
        } else {
            recyclerView.setupDragListener(null)
        }
    }

    protected fun selectItemRange(from: Int, to: Int, min: Int, max: Int) {
        if (from == to) {
            (min..max).filter { it != from }.forEach { toggleItemSelection(false, it, true) }
            return
        }

        if (to < from) {
            for (i in to..from) {
                toggleItemSelection(true, i, true)
            }

            if (min > -1 && min < to) {
                (min until to).filter { it != from }.forEach { toggleItemSelection(false, it, true) }
            }

            if (max > -1) {
                for (i in from + 1..max) {
                    toggleItemSelection(false, i, true)
                }
            }
        } else {
            for (i in from..to) {
                toggleItemSelection(true, i, true)
            }

            if (max > -1 && max > to) {
                (to + 1..max).filter { it != from }.forEach { toggleItemSelection(false, it, true) }
            }

            if (min > -1) {
                for (i in min until from) {
                    toggleItemSelection(false, i, true)
                }
            }
        }
    }

    fun setupZoomListener(zoomListener: MyRecyclerView.MyZoomListener?) {
        recyclerView.setupZoomListener(zoomListener)
    }

//    fun addVerticalDividers(add: Boolean) {
//        if (recyclerView.itemDecorationCount > 0) {
//            recyclerView.removeItemDecorationAt(0)
//        }
//
//        if (add) {
//            DividerItemDecoration(activity, DividerItemDecoration.VERTICAL).apply {
//                ContextCompat.getDrawable(activity, R.drawable.divider)?.let {
//                    setDrawable(it)
//                }
//                recyclerView.addItemDecoration(this)
//            }
//        }
//    }

    /**
     * Vertical separator between elements.
     *
     * @param visible Enabled.
     * @param paddingStartDp Left padding in Dp.
     * @param paddingEndDp Right padding in Dp.
     * @param dividerHeightDp Height of the divider in Dp.
     */
    fun setVerticalDividers(
        visible: Boolean,
        paddingStartDp: Int = 0,
        paddingEndDp: Int = 0,
        dividerHeightDp: Int = 1,
        color: Int = 0x33AAAAAA, // activity.getDividerColor(),
    ) {
        // Remove the old separator
        dividerDecoration?.let {
            recyclerView.removeItemDecoration(it)
            dividerDecoration = null
        }

        if (visible) {
            // Create or reuse a separator
            val decoration = MyDividerDecoration().apply {
                setConfiguration(
                    paddingStartDp = paddingStartDp,
                    paddingEndDp = paddingEndDp,
                    dividerHeightDp = dividerHeightDp,
                    color = color,
                    context = activity
                )
                setVisible(true)
            }

            recyclerView.addItemDecoration(decoration)
            dividerDecoration = decoration
            isDividersVisible = true
        } else {
            isDividersVisible = false
        }
    }

    /**
     * Creates the bottom margin of the adapter.
     * If there is no scrollbar in the Adapter, it is better to use:
     * ```
     *         android:scrollbars=“vertical”
     *         android:clipToPadding=“false”
     *         android:paddingBottom=“128dp”
     * ```
     *
     * @param bottomPaddingDp Height of the setback in Dp.
     */
    fun addBottomPadding(bottomPaddingDp: Int) {
        // Remove the old indent if there is one
        bottomPaddingDecoration?.let {
            recyclerView.removeItemDecoration(it)
            bottomPaddingDecoration = null
        }

        if (bottomPaddingDp > 0) {
            // Convert dp to pixels
            val paddingPx = bottomPaddingDp.dpToPx(activity)

            // Create and add decoration
            BottomPaddingDecoration(paddingPx).apply {
                recyclerView.addItemDecoration(this)
                bottomPaddingDecoration = this
            }

            // Updating display
            recyclerView.invalidateItemDecorations()
        }
    }

    fun finishActMode() {
        actMode?.finish()
    }

    fun updateTextColor(textColor: Int) {
        this.textColor = textColor
        onRefresh.invoke()
    }

    fun updatePrimaryColor() {
        properPrimaryColor = activity.getProperPrimaryColor()
        contrastColor = properPrimaryColor.getContrastColor()
        accentColor = activity.getProperAccentColor()
        onRefresh.invoke()
    }

    fun updateBackgroundColor(backgroundColor: Int) {
        this.backgroundColor = backgroundColor
        surfaceColor = activity.getSurfaceColor()
        onRefresh.invoke()
    }

    private fun contactThumbnailsSize(): Float {
        return when (activity.baseConfig.contactThumbnailsSize) {
            CONTACT_THUMBNAILS_SIZE_SMALL -> 0.9F
            CONTACT_THUMBNAILS_SIZE_LARGE -> 1.15F
            CONTACT_THUMBNAILS_SIZE_EXTRA_LARGE -> 1.3F
            else -> 1.0F
        }
    }

    protected fun createViewHolder(layoutType: Int, parent: ViewGroup?): ViewHolder {
        val view = layoutInflater.inflate(layoutType, parent, false)
        return ViewHolder(view)
    }

    protected fun createViewHolder(view: View): ViewHolder {
        return ViewHolder(view)
    }

    protected fun bindViewHolder(holder: ViewHolder) {
        holder.itemView.tag = holder
    }

    protected fun removeSelectedItems(positions: ArrayList<Int>) {
        positions.forEach {
            notifyItemRemoved(it)
        }
        finishActMode()
    }

    open inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bindView(item: T, allowSingleClick: Boolean, allowLongClick: Boolean, callback: (itemView: View, adapterPosition: Int) -> Unit): View {
            return itemView.apply {
                callback(this, adapterPosition)

                if (allowSingleClick) {
                    setOnClickListener { viewClicked(item) }
                    setOnLongClickListener { if (allowLongClick) viewLongClicked() else viewClicked(item); true }
                } else {
                    setOnClickListener(null)
                    setOnLongClickListener(null)
                }
            }
        }

        fun viewClicked(any: T) {
            if (actModeCallback.isSelectable) {
                val currentPosition = adapterPosition - positionOffset
                val isSelected = selectedKeys.contains(getItemSelectionKey(currentPosition))
                toggleItemSelection(!isSelected, currentPosition, true)
            } else {
                itemClick.invoke(any)
            }
            lastLongPressedItem = -1
        }

        fun viewLongClicked() {
            val currentPosition = adapterPosition - positionOffset
            if (!actModeCallback.isSelectable) {
                activity.startActionMode(actModeCallback)
            }

            toggleItemSelection(true, currentPosition, true)
            itemLongClicked(currentPosition)
        }
    }

    // Cleaning resources
    fun cleanup() {
        dividerDecoration?.let {
            recyclerView.removeItemDecoration(it)
            dividerDecoration = null
        }
        MyDividerDecoration.clearCache()

        bottomPaddingDecoration?.let {
            recyclerView.removeItemDecoration(it)
            bottomPaddingDecoration = null
        }
    }
}
