package com.goodwy.commons.samples.adapters

import android.view.Menu
import android.view.ViewGroup
import android.widget.TextView
import com.goodwy.commons.samples.R as simpleR
import com.goodwy.commons.R
import com.goodwy.commons.activities.BaseSimpleActivity
import com.goodwy.commons.adapters.MyRecyclerViewAdapter
import com.goodwy.commons.views.MyRecyclerView

class TestAdapter(activity: BaseSimpleActivity, recyclerView: MyRecyclerView, itemClick: (Any) -> Unit) :
    MyRecyclerViewAdapter(activity, recyclerView, itemClick) {

    private val items = List(20) { "Element ${it + 1}" }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return createViewHolder(simpleR.layout.item_simple, parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentPosition = position - positionOffset
        val element = items[currentPosition]

        val textView = holder.itemView.findViewById<TextView>(simpleR.id.textView)
        textView.text = element

        // Set the background to visually highlight selected items
        val isSelected = selectedKeys.contains(getItemSelectionKey(currentPosition))
        holder.itemView.setBackgroundColor(if (isSelected) 0x33AAAAAA else 0)

        holder.bindView(element, allowSingleClick = true, allowLongClick = true) { itemView, adapterPosition ->
            // The body is not required, as we have already configured the appearance above.
        }
    }

    override fun getActionMenuId() = R.menu.cab_delete_only

    override fun prepareActionMode(menu: Menu) {
        val deleteItem = menu.findItem(R.id.cab_delete)
        deleteItem?.isVisible = selectedKeys.isNotEmpty()
    }

    override fun actionItemPressed(id: Int) {
        when (id) {
            R.id.cab_delete -> {
                // For the test, we simply remove the selection
                removeSelectedItems(getSelectedItemPositions())
            }
        }
    }

    override fun getSelectableItemCount() = items.size

    override fun getIsItemSelectable(position: Int) = true

    override fun getItemSelectionKey(position: Int) = position + 1

    override fun getItemKeyPosition(key: Int) = key - 1

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}
}
