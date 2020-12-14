package com.limor.app.components

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration


/**
 *  This Item decorator will draw the items in the following way: 1 large, 4 small, 1 large, 4 small..
 *  _________
 *  |       |
 *  |       |
 *  |_______|
 *  ____ ____
 *  |  | |  |
 *  |  | |  |
 *  |__| |__|
 *  ____ ____
 *  |  | |  |
 *  |  | |  |
 *  |__| |__|
 *  _________
 *  |       |
 *  |       |
 *  |_______|
 *  ____ ____
 *  |  | |  |
 *  |  | |  |
 *  |__| |__|
 *  ____
 *  |  |
 *  |  |
 *  |__|
 */
class GridSpacingItemDecoration(
    private val spacingMedium: Int,
    private val spacingSmall: Int = spacingMedium / 2,
    private val spacingTiny: Int = spacingMedium / 4
) :
    ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)

        // if the position is one of the large items
        if (position == 0 || (position != 0 && position % 5 == 0)) {
            outRect.top = spacingMedium
            outRect.bottom = spacingMedium
            outRect.left = 0
            outRect.right = 0

            // if it's one of the small items
        } else {
            outRect.top = 0
            outRect.bottom = 0
            val column = when {
                (position - 1) % 5 == 0 || (position - 3) % 5 == 0 -> 0
                else -> 1
            }
            val row = when {
                (position - 1) % 5 == 0 || (position - 2) % 5 == 0 -> 0
                else -> 1
            }
            if (column == 0) {
                outRect.left = 0
                outRect.right = spacingTiny
            } else {
                outRect.left = spacingTiny
                outRect.right = 0
            }

            if (row == 0) {
                outRect.bottom = spacingSmall
            }
        }
    }


}