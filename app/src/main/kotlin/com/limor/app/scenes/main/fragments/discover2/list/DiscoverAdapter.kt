package com.limor.app.scenes.main.fragments.discover2.list

import android.content.Context
import com.limor.app.scenes.main.fragments.discover2.list.items.SearchItem
import com.limor.app.scenes.main.fragments.discover2.list.sections.CategoriesSection
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.Section

class DiscoverAdapter(
    context: Context,
    onSearchViewTextChange: (newText: String) -> Unit,
    onCategoriesHeaderActionClick: () -> Unit,
    onCategoriesItemClick: (String) -> Unit
) : GroupieAdapter() {

    private val categoriesSection =
        CategoriesSection(context, onCategoriesHeaderActionClick, onCategoriesItemClick)
    private val suggestedPeopleSection = Section()
    private val featuredCastsSection = Section()
    private val topCastsSection = Section()

    init {
        add(SearchItem(onSearchViewTextChange))
        add(categoriesSection)
        add(suggestedPeopleSection)
        add(featuredCastsSection)
        add(topCastsSection)
    }

    fun updateCategories(categories: List<String>) {
        categoriesSection.updateCategories(categories)
    }
}