package com.limor.app.scenes.main.fragments.discover.category.list

import android.content.Context
import com.limor.app.R
import com.limor.app.scenes.auth_new.util.ToastMaker
import com.limor.app.scenes.main.fragments.discover.common.HeaderItem
import com.limor.app.scenes.main.fragments.discover.common.casts.HorizontalCastsListItem
import com.limor.app.scenes.main.fragments.discover.common.mock.MockCast
import com.limor.app.uimodels.CastUIModel
import com.xwray.groupie.Section

class FeaturedCastsSection(context: Context) : Section() {

    companion object {
        private const val FEATURED_CASTS_ITEM_POSITION = 1
    }

    init {
        setHeader(
            HeaderItem(
                context.getString(R.string.featured_casts),
                action = HeaderItem.HeaderAction(
                    name = context.getString(R.string.see_all),
                    onActionClick = {
                        ToastMaker.showToast(context, "Not implemented")
                    }
                )
            )
        )
    }

    fun updateFeaturedCasts(featuredCasts: List<CastUIModel>) {
        val featuredCastsItem = if (itemCount < FEATURED_CASTS_ITEM_POSITION + 1) {
            HorizontalCastsListItem().also { add(it) }
        } else {
            getItem(FEATURED_CASTS_ITEM_POSITION) as HorizontalCastsListItem
        }

        featuredCastsItem.update(featuredCasts)
    }
}