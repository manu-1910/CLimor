package com.limor.app.scenes.main.fragments.discover2.discover.list.topcasts

import android.content.Context
import com.limor.app.R
import com.limor.app.scenes.main.fragments.discover2.common.HeaderItem
import com.limor.app.scenes.main.fragments.discover2.common.casts.BigCastItem
import com.limor.app.scenes.main.fragments.discover2.common.mock.MockCast
import com.limor.app.scenes.main.fragments.discover2.common.casts.SmallCastItem
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
                context.getString(R.string.top_casts),
                action = HeaderItem.HeaderAction(
                    name = context.getString(R.string.see_all),
                    onActionClick = {
                        TODO()
                    }
                )
            )
        )
        add(castsInnerSection)
    }

    fun updateTopCasts(topCasts: List<MockCast>) {
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