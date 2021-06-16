package com.limor.app.scenes.main.fragments.discover2.list.sections

import android.content.Context
import com.limor.app.R
import com.limor.app.scenes.main.fragments.discover2.list.items.categories.CategoriesItem
import com.limor.app.scenes.main.fragments.discover2.list.items.HeaderItem
import com.xwray.groupie.Section

class CategoriesSection(
    context: Context,
    onHeaderActionClick: () -> Unit,
    private val onCategoriesItemClick: (String) -> Unit
) : Section() {

    companion object {
        private const val CATEGORIES_ITEM_POSITION = 0
    }

    init {
        setHeader(
            HeaderItem(
                context.getString(R.string.categories),
                action = HeaderItem.HeaderAction(
                    name = context.getString(R.string.see_all),
                    onActionClick = onHeaderActionClick
                )
            )
        )
    }

    fun updateCategories(categories: List<String>) {
        val categoriesItem = if (itemCount < CATEGORIES_ITEM_POSITION + 1) {
            CategoriesItem(onCategoriesItemClick)
        } else {
            getItem(CATEGORIES_ITEM_POSITION) as CategoriesItem
        }

        categoriesItem.update(categories)
    }
}