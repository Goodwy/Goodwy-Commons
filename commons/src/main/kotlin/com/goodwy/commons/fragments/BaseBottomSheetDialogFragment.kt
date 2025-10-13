package com.goodwy.commons.fragments

import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.goodwy.commons.R
import com.goodwy.commons.databinding.DialogBottomSheetBinding
import com.goodwy.commons.extensions.*

abstract class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = DialogBottomSheetBinding.inflate(inflater, container, false)
        val context = requireContext()

        view.bottomSheetHolder.background = ResourcesCompat.getDrawable(context.resources, R.drawable.bottom_sheet_bg, context.theme).apply {
            val backgroundColor = if (requireContext().isBlackTheme()) context.getSurfaceColor() else context.getProperBackgroundColor()
            (this as LayerDrawable).findDrawableByLayerId(R.id.bottom_sheet_background).applyColorFilter(backgroundColor)
        }
        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = arguments?.getInt(BOTTOM_SHEET_TITLE).takeIf { it != 0 }
        DialogBottomSheetBinding.bind(view).apply {
            val textColor = view.context.getProperTextColor()
            bottomSheetTitle.setTextColor(textColor)
            bottomSheetTitle.setTextOrBeGone(title)
            setupContentView(bottomSheetContentHolder)

            bottomSheetCancel.applyColorFilter(textColor)
            bottomSheetCancel.setOnClickListener { dialog?.dismiss() }
        }
    }

    abstract fun setupContentView(parent: ViewGroup)

    companion object {
        const val BOTTOM_SHEET_TITLE = "title_string"
    }
}
