package com.limor.app.scenes.main.fragments.discover.discover.list

import android.content.Context
import androidx.navigation.NavController
import com.limor.app.scenes.main.fragments.discover.discover.list.categories.CategoriesSection
import com.limor.app.scenes.main.fragments.discover.discover.list.featuredcasts.FeaturedCastsSection
import com.limor.app.scenes.main.fragments.discover.discover.list.search.SearchItem
import com.limor.app.scenes.main.fragments.discover.discover.list.suggestedpeople.SuggestedPeopleSection
import com.limor.app.scenes.main.fragments.discover.discover.list.topcasts.TopCastsSection
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.CategoryUIModel
import com.limor.app.uimodels.UserUIModel
import com.xwray.groupie.GroupieAdapter

class DiscoverAdapter(
    context: Context,
    navController: NavController
) : GroupieAdapter() {

    companion object {
        private const val SPAN_COUNT = 2
    }

    private val categoriesSection = CategoriesSection(context, navController)
    private val suggestedPeopleSection = SuggestedPeopleSection(context, navController)
    private val featuredCastsSection = FeaturedCastsSection(context, navController)
    private val topCastsSection = TopCastsSection(context)

    init {
        spanCount = SPAN_COUNT
        add(SearchItem())
        add(categoriesSection)
        add(suggestedPeopleSection)
        add(featuredCastsSection)
        add(topCastsSection)
    }

    fun updateCategories(categories: List<CategoryUIModel>) {
        categoriesSection.updateCategories(categories)
    }
    fun updateSuggestedPeople(suggestedPeople: List<UserUIModel>) {
        suggestedPeopleSection.updateSuggestedPeople(suggestedPeople)
    }
    fun updateFeaturedCasts(featuredCasts: List<CastUIModel>) {
        featuredCastsSection.updateFeaturedCasts(featuredCasts)
    }
    fun updateTopCasts(topCasts: List<CastUIModel>) {
        topCastsSection.updateTopCasts(topCasts)
    }
}