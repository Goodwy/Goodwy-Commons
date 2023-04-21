package com.goodwy.commons.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.goodwy.commons.R
import com.goodwy.commons.extensions.*
import com.goodwy.commons.models.SimpleListItem
import kotlinx.android.synthetic.main.item_collection_list.view.*
import kotlinx.android.synthetic.main.item_simple_list.view.bottom_sheet_item_icon
import kotlinx.android.synthetic.main.item_simple_list.view.bottom_sheet_item_title
import kotlinx.android.synthetic.main.item_simple_list.view.bottom_sheet_selected_icon

open class SimpleListItemAdapter(val activity: Activity, val onItemClicked: (SimpleListItem) -> Unit) :
    ListAdapter<SimpleListItem, SimpleListItemAdapter.SimpleItemViewHolder>(SimpleListItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleItemViewHolder {
        val view = activity.layoutInflater.inflate(R.layout.item_simple_list, parent, false)
        return SimpleItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: SimpleItemViewHolder, position: Int) {
        val route = getItem(position)
        holder.bindView(route)
    }

    open inner class SimpleItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindView(item: SimpleListItem) {
            setupSimpleListItem(itemView, item, false, onItemClicked)
        }
    }

    private class SimpleListItemDiffCallback : DiffUtil.ItemCallback<SimpleListItem>() {
        override fun areItemsTheSame(oldItem: SimpleListItem, newItem: SimpleListItem): Boolean {
            return SimpleListItem.areItemsTheSame(oldItem, newItem)
        }

        override fun areContentsTheSame(oldItem: SimpleListItem, newItem: SimpleListItem): Boolean {
            return SimpleListItem.areContentsTheSame(oldItem, newItem)
        }
    }
}

fun setupSimpleListItem(view: View, item: SimpleListItem, collection: Boolean, onItemClicked: (SimpleListItem) -> Unit) {
    view.apply {
        if (collection) {
            bottom_sheet_item_title.setText(item.textRes)
            bottom_sheet_item_title.setTextColor(context.getProperTextColor())
            bottom_sheet_item_icon.setImageResourceOrBeGone(item.imageRes)
            //bottom_sheet_item_icon.applyColorFilter(color)

            val textButton = if (item.selected) R.string.open else R.string.get
            bottom_sheet_button.setText(textButton)
            val drawable = resources.getColoredDrawableWithColor(R.drawable.button_gray_bg, context.getProperPrimaryColor())
            bottom_sheet_button.background = drawable
            bottom_sheet_button.setTextColor(context.getProperBackgroundColor())
            bottom_sheet_button.setPadding(2,2,2,2)
            if (!item.selected) {
                bottom_sheet_item_title.alpha = 0.4f
                bottom_sheet_item_icon.alpha = 0.4f
            }
        } else {
            val color = if (item.selected) {
                context.getProperPrimaryColor()
            } else {
                context.getProperTextColor()
            }

            bottom_sheet_item_title.setText(item.textRes)
            bottom_sheet_item_title.setTextColor(color)
            bottom_sheet_item_icon.setImageResourceOrBeGone(item.imageRes)
            bottom_sheet_item_icon.applyColorFilter(color)
            //bottom_sheet_selected_icon.beVisibleIf(item.selected)
            val selectedIcon = if (item.selected) R.drawable.ic_radio_button else R.drawable.ic_circle
            bottom_sheet_selected_icon.setImageResource(selectedIcon)
            bottom_sheet_selected_icon.applyColorFilter(color)
        }

        setOnClickListener {
            onItemClicked(item)
        }
    }
}
