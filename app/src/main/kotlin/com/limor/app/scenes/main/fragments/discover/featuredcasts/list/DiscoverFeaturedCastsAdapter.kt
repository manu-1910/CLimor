package com.limor.app.scenes.main.fragments.discover.featuredcasts.list

import com.limor.app.scenes.main.fragments.discover.common.casts.BigCastItem
import com.limor.app.scenes.main.fragments.discover.common.casts.SmallCastItem
import com.limor.app.uimodels.CastUIModel
import com.xwray.groupie.GroupieAdapter

class DiscoverFeaturedCastsAdapter(
    private val bigCastItemPattern: (itemIndex: Int) -> Boolean = { index -> (index + 1) % 5 == 0 } // by default every fifth item is [BigCastItem]
) : GroupieAdapter() {

    companion object {
        private const val SPAN_COUNT = 2
    }

    init {
        spanCount = SPAN_COUNT
    }

    fun updateFeaturedCasts(featuredCasts: List<CastUIModel>) {
        update(
            featuredCasts.mapIndexed { index, cast ->
                when {
                    bigCastItemPattern(index) -> BigCastItem(cast)
                    else -> SmallCastItem(cast)
                }
            }
        )
    }
}
