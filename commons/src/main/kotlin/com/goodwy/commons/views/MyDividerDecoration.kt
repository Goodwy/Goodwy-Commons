package com.goodwy.commons.views

import android.graphics.*
import android.view.View
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.goodwy.commons.extensions.dpToPx

class MyDividerDecoration : RecyclerView.ItemDecoration() {

    private val paint = Paint().apply {
        isAntiAlias = false // For speed
        style = Paint.Style.FILL
    }
    private val tempRect = Rect()

    private var leftPadding = 0
    private var rightPadding = 0
    private var dividerHeight = 1
    private var dividerColor = Color.GRAY

    // Visibility flag
    private var isVisible = false
    private var isConfigured = false

    fun setConfiguration(
        paddingStartDp: Int = 0,
        paddingEndDp: Int = 0,
        dividerHeightDp: Int = 1,
        @ColorInt color: Int = Color.GRAY,
        context: android.content.Context
    ) {
        leftPadding = paddingStartDp.dpToPx(context)
        rightPadding = paddingEndDp.dpToPx(context)
        dividerHeight = dividerHeightDp.dpToPx(context)
        dividerColor = color
        paint.color = dividerColor
        isConfigured = true
    }

    fun setVisible(visible: Boolean) {
        isVisible = visible && isConfigured
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (!isVisible || dividerHeight <= 0 || parent.childCount < 1) return

        val left = leftPadding
        val right = parent.width - rightPadding

        // Drawing dividers
        val childCount = parent.childCount
        val drawUntil = childCount - 1

        for (i in 0 until drawUntil) {
            val child = parent.getChildAt(i)
            parent.getDecoratedBoundsWithMargins(child, tempRect)

            val top = tempRect.bottom.toFloat()
            val bottom = top + dividerHeight

            c.drawRect(left.toFloat(), top, right.toFloat(), bottom, paint)
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (!isVisible) return

        val position = parent.getChildAdapterPosition(view)
        if (position != RecyclerView.NO_POSITION) {
            if (position < state.itemCount - 1) {
                outRect.bottom = dividerHeight
            }
        }
    }

    companion object {
        // Cache for frequently used configurations
        private val decorationsCache = mutableMapOf<String, MyDividerDecoration>()

        fun createCached(
            key: String,
            paddingStartDp: Int = 0,
            paddingEndDp: Int = 0,
            dividerHeightDp: Int = 1,
            @ColorInt color: Int = Color.GRAY,
            context: android.content.Context
        ): MyDividerDecoration {
            return decorationsCache.getOrPut(key) {
                MyDividerDecoration().apply {
                    setConfiguration(
                        paddingStartDp,
                        paddingEndDp,
                        dividerHeightDp,
                        color,
                        context
                    )
                }
            }
        }

        fun clearCache() {
            decorationsCache.clear()
        }
    }
}
