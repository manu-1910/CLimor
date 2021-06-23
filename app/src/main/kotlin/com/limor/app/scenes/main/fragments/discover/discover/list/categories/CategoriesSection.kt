package com.limor.app.scenes.main.fragments.discover.discover.list.categories

import android.content.Context
import com.limor.app.R
import com.limor.app.scenes.main.fragments.discover.common.HeaderItem
import com.xwray.groupie.Section

class CategoriesSection(private val context: Context) : Section() {

    companion object {
        private const val CATEGORIES_ITEM_POSITION = 1
    }

    fun updateCategories(categories: List<String>) {
        if (categories.isNotEmpty()) {
            setHeaderIfNeeded()
        }

        val categoriesItem = if (itemCount < CATEGORIES_ITEM_POSITION + 1) {
            CategoriesItem().also { add(it) }
        } else {
            getItem(CATEGORIES_ITEM_POSITION) as CategoriesItem
        }

        categoriesItem.update(categories)
    }

    fun setHeaderIfNeeded() {
        if (groupCount == 0) {
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
    }
}