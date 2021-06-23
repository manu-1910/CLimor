package com.limor.app.scenes.utils.recycler

import android.graphics.Rect
import android.view.View
import androidx.annotation.Dimension
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.extensions.px

class HorizontalSpacingItemDecoration(
    @Dimension(unit = Dimension.PX) private val spacing: Int,
    private val includeFirstItem: Boolean = true,
    private val includeLastItem: Boolean = true
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        // Set the same spacing for the first item
        if (position == 0 && includeFirstItem) {
            outRect.left = spacing
        }
        if (position == parent.childCount - 1 && !includeLastItem) {
            return
        }
        outRect.right = spacing
    }
}