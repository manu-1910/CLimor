package com.limor.app.scenes.main.fragments.discover.category.list

import android.content.Context
import com.limor.app.scenes.main.fragments.discover.common.mock.MockCast
import com.xwray.groupie.GroupieAdapter

class DiscoverCategoryAdapter(
    context: Context
) : GroupieAdapter() {

    companion object {
        private const val SPAN_COUNT = 2
    }

    private val featuredCastsSection = FeaturedCastsSection(context)
    private val topCastsSection = TopCastsSection(context)

    init {
        spanCount = SPAN_COUNT
        add(featuredCastsSection)
        add(topCastsSection)
    }

    fun updateFeaturedCasts(featuredCasts: List<MockCast>) {
        featuredCastsSection.updateFeaturedCasts(featuredCasts)
    }
    fun updateTopCasts(topCasts: List<MockCast>) {
        topCastsSection.updateTopCasts(topCasts)
    }
}