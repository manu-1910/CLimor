package com.limor.app.scenes.main.fragments.discover2.discover.list.categories

import android.content.Context
import com.limor.app.R
import com.limor.app.scenes.main.fragments.discover2.common.HeaderItem
import com.xwray.groupie.Section

class CategoriesSection(context: Context) : Section() {

    companion object {
        private const val CATEGORIES_ITEM_POSITION = 1
    }

    init {
        setHeader(
            HeaderItem(
                context.getString(R.string.categories),
                action = HeaderItem.HeaderAction(
                    name = context.getString(R.string.see_all),
                    onActionClick = {
                        TODO()
                    }
                )
            )
        )
    }

    fun updateCategories(categories: List<String>) {
        val categoriesItem = if (itemCount < CATEGORIES_ITEM_POSITION + 1) {
            CategoriesItem().also { add(it) }
        } else {
            getItem(CATEGORIES_ITEM_POSITION) as CategoriesItem
        }

        categoriesItem.update(categories)
    }
}