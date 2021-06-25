package com.limor.app.scenes.utils.recycler

import android.graphics.Rect
import android.view.View
import androidx.annotation.Dimension
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TopGridSpacingItemDecoration(
    @Dimension(unit = Dimension.PX) private val spacing: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val layoutManager = (parent.layoutManager as GridLayoutManager)
        val itemSpanIndex =
            layoutManager.spanSizeLookup.getSpanIndex(position, layoutManager.spanCount)
        if (position == itemSpanIndex) {
            outRect.top = spacing
        }
    }
}
