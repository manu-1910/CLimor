package com.limor.app.scenes.main.fragments.discover.discover.list.featuredcasts

import android.content.Context
import androidx.navigation.NavController
import com.limor.app.R
import com.limor.app.scenes.main.fragments.discover.common.HeaderItem
import com.limor.app.scenes.main.fragments.discover.common.casts.HorizontalCastsListItem
import com.limor.app.scenes.main.fragments.discover.common.mock.MockCast
import com.xwray.groupie.Section

class FeaturedCastsSection(
    private val context: Context,
    private val navController: NavController
) : Section() {

    companion object {
        private const val FEATURED_CASTS_ITEM_POSITION = 1
    }

    fun updateFeaturedCasts(featuredCasts: List<MockCast>) {
        if (featuredCasts.isNotEmpty()) {
            setHeaderIfNeeded()
        }
        val featuredCastsItem = if (itemCount < FEATURED_CASTS_ITEM_POSITION + 1) {
            HorizontalCastsListItem().also { add(it) }
        } else {
            getItem(FEATURED_CASTS_ITEM_POSITION) as HorizontalCastsListItem
        }

        featuredCastsItem.update(featuredCasts)
    }

    fun setHeaderIfNeeded() {
        if (groupCount == 0) {
            setHeader(
                HeaderItem(
                    context.getString(R.string.featured_casts),
                    action = HeaderItem.HeaderAction(
                        name = context.getString(R.string.see_all),
                        onActionClick = {
                            navController.navigate(R.id.action_navigation_discover_to_discoverFeaturedCastsFragment)
                        }
                    )
                )
            )
        }
    }
}