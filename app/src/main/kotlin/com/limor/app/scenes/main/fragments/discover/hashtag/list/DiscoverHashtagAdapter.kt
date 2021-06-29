package com.limor.app.scenes.main.fragments.discover.hashtag.list

import android.content.Context
import androidx.navigation.NavController
import com.limor.app.scenes.main.fragments.discover.hashtag.list.item.PostsCountItem
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.TagUIModel
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.Section

class DiscoverHashtagAdapter(
    private val context: Context,
    private val navController: NavController
) : GroupieAdapter() {
    companion object {
        private const val SPAN_COUNT = 2
    }

    private val postsCountSection = Section()
    private val recentCastsSection = RecentCastsSection(context)
    private val topCastsSection = TopCastsSection(context)

    init {
        spanCount = SPAN_COUNT
        add(postsCountSection)
        add(recentCastsSection)
        add(topCastsSection)
    }

    fun updatePostsCount(tag: TagUIModel) {
        postsCountSection.update(
            listOf(PostsCountItem(tag))
        )
    }

    fun updateRecentCasts(recentCasts: List<CastUIModel>) {
        recentCastsSection.updateRecentCasts(recentCasts)
    }

    fun updateTopCasts(topCasts: List<CastUIModel>) {
        topCastsSection.updateTopCasts(topCasts)
    }
}