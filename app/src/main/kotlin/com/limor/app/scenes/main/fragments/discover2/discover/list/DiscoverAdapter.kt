package com.limor.app.scenes.main.fragments.discover2.discover.list

import android.content.Context
import com.limor.app.scenes.main.fragments.discover2.common.mock.MockCast
import com.limor.app.scenes.main.fragments.discover2.common.mock.MockPerson
import com.limor.app.scenes.main.fragments.discover2.discover.list.categories.CategoriesSection
import com.limor.app.scenes.main.fragments.discover2.discover.list.featuredcasts.FeaturedCastsSection
import com.limor.app.scenes.main.fragments.discover2.discover.list.search.SearchItem
import com.limor.app.scenes.main.fragments.discover2.discover.list.suggestedpeople.SuggestedPeopleSection
import com.limor.app.scenes.main.fragments.discover2.discover.list.topcasts.TopCastsSection
import com.xwray.groupie.GroupieAdapter

class DiscoverAdapter(
    context: Context
) : GroupieAdapter() {

    companion object {
        private const val SPAN_COUNT = 2
    }

    private val categoriesSection = CategoriesSection(context)
    private val suggestedPeopleSection = SuggestedPeopleSection(context)
    private val featuredCastsSection = FeaturedCastsSection(context)
    private val topCastsSection = TopCastsSection(context)

    init {
        spanCount = SPAN_COUNT
        add(SearchItem())
        add(categoriesSection)
        add(suggestedPeopleSection)
        add(featuredCastsSection)
        add(topCastsSection)
    }

    fun updateCategories(categories: List<String>) {
        categoriesSection.updateCategories(categories)
    }
    fun updateSuggestedPeople(suggestedPeople: List<MockPerson>) {
        suggestedPeopleSection.updateSuggestedPeople(suggestedPeople)
    }
    fun updateFeaturedCasts(featuredCasts: List<MockCast>) {
        featuredCastsSection.updateFeaturedCasts(featuredCasts)
    }
    fun updateTopCasts(topCasts: List<MockCast>) {
        topCastsSection.updateTopCasts(topCasts)
    }
}