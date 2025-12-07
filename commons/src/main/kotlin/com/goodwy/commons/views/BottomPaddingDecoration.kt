package com.goodwy.commons.views

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class BottomPaddingDecoration(
    private val paddingBottomPx: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        // Add padding only to the last element
        if (position == state.itemCount - 1) {
            outRect.bottom = paddingBottomPx
        }
    }
}
