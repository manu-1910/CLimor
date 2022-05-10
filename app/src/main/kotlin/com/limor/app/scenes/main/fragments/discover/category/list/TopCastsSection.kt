package com.limor.app.scenes.main.fragments.discover.category.list

import android.content.Context
import com.limor.app.R
import com.limor.app.scenes.main.fragments.discover.common.HeaderItem
import com.limor.app.scenes.main.fragments.discover.common.casts.BigCastItem
import com.limor.app.scenes.main.fragments.discover.common.casts.SmallCastItem
import com.limor.app.uimodels.CastUIModel
import com.xwray.groupie.Section

/**
 * @param bigCastItemPattern - describes when to show the [BigCastItem]
 */
class TopCastsSection(
    private val context: Context,
    private val bigCastItemPattern: (itemIndex: Int) -> Boolean = { index -> (index + 1) % 5 == 0 }, // by default every fifth item is [BigCastItem],
    private val showTitle: Boolean = true
) : Section() {

    private val castsInnerSection = Section()

    fun updateTopCasts(topCasts: List<CastUIModel>) {
        if (topCasts.isNotEmpty()) {
            setHeaderIfNeeded()
        }
        castsInnerSection.update(
            topCasts.mapIndexed { index, cast ->
                when {
                    bigCastItemPattern(index) -> BigCastItem(cast)
                    else -> SmallCastItem(cast)
                }
            }
        )
    }

    private fun setHeaderIfNeeded() {
        if (groupCount == 0) {
            if (showTitle) {
                setHeader(
                    HeaderItem(
                        context.getString(R.string.top_casts)
                    )
                )
            }
            add(castsInnerSection)
        }
    }
}