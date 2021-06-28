package com.limor.app.scenes.main.fragments.discover.category.list

import android.content.Context
import com.limor.app.R
import com.limor.app.scenes.auth_new.util.ToastMaker
import com.limor.app.scenes.main.fragments.discover.common.HeaderItem
import com.limor.app.scenes.main.fragments.discover.common.casts.BigCastItem
import com.limor.app.scenes.main.fragments.discover.common.mock.MockCast
import com.limor.app.scenes.main.fragments.discover.common.casts.SmallCastItem
import com.limor.app.uimodels.CastUIModel
import com.xwray.groupie.Section

/**
 * @param bigCastItemPattern - describes when to show the [BigCastItem]
 */
class TopCastsSection(
    context: Context,
    private val bigCastItemPattern: (itemIndex: Int) -> Boolean = { index -> (index + 1) % 5 == 0 } // by default every fifth item is [BigCastItem]
): Section() {

    private val castsInnerSection = Section()

    init {
        setHeader(
            HeaderItem(
                context.getString(R.string.top_casts)
            )
        )
        add(castsInnerSection)
    }

    fun updateTopCasts(topCasts: List<CastUIModel>) {
        castsInnerSection.update(
            topCasts.mapIndexed { index, cast ->
                when {
                    bigCastItemPattern(index) -> BigCastItem(cast)
                    else -> SmallCastItem(cast)
                }
            }
        )
    }
}