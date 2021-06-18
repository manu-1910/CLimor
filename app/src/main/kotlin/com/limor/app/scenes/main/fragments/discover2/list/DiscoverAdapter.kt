package com.limor.app.scenes.main.fragments.discover2.list

import android.content.Context
import com.limor.app.scenes.main.fragments.discover2.list.categories.CategoriesSection
import com.limor.app.scenes.main.fragments.discover2.list.suggestedpeople.SuggestedPeopleSection
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.Section

class DiscoverAdapter(
    context: Context
) : GroupieAdapter() {

    private val categoriesSection = CategoriesSection(context)
    private val suggestedPeopleSection = SuggestedPeopleSection(context)

    private val featuredCastsSection = Section()
    private val topCastsSection = Section()

    init {
        add(SearchItem())
        add(categoriesSection)
        add(suggestedPeopleSection)
        add(featuredCastsSection)
        add(topCastsSection)
    }

    fun updateCategories(categories: List<String>) {
        categoriesSection.updateCategories(categories)
    }
}