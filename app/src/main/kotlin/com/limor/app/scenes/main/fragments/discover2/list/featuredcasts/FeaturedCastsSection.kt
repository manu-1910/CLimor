package com.limor.app.scenes.main.fragments.discover2.list.featuredcasts

import android.content.Context
import com.limor.app.R
import com.limor.app.scenes.main.fragments.discover2.list.items.header.HeaderItem
import com.limor.app.scenes.main.fragments.discover2.mock.MockCast
import com.xwray.groupie.Section

class FeaturedCastsSection(context: Context) : Section() {

    companion object {
        private const val FEATURED_CASTS_ITEM_POSITION = 0
    }

    init {
        setHeader(
            HeaderItem(
                context.getString(R.string.featured_casts),
                action = HeaderItem.HeaderAction(
                    name = context.getString(R.string.see_all),
                    onActionClick = {
                        TODO()
                    }
                )
            )
        )
    }

    fun updateFeaturedCasts(featuredCasts: List<MockCast>) {
        val featuredCastsItem = if (itemCount < FEATURED_CASTS_ITEM_POSITION + 1) {
            FeaturedCastsItem().also { add(it) }
        } else {
            getItem(FEATURED_CASTS_ITEM_POSITION) as FeaturedCastsItem
        }

        featuredCastsItem.update(featuredCasts)
    }
}