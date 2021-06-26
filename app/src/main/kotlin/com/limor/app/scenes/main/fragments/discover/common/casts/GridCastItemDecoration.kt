package com.limor.app.scenes.main.fragments.discover.common.casts

import androidx.annotation.Dimension
import com.limor.app.extensions.px
import com.limor.app.scenes.utils.recycler.InsetItemDecoration

class GridCastItemDecoration(
    @Dimension val padding: Int = 16.px
) : InsetItemDecoration(padding, GRID_CAST_ITEM_TYPE_KEY, GRID_CAST_ITEM) {
    companion object {
        const val GRID_CAST_ITEM_TYPE_KEY = "grid_cast_item_type"
        const val GRID_CAST_ITEM = "grid_cast_item"
    }
}