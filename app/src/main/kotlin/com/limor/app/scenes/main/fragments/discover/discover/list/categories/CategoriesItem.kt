package com.limor.app.scenes.main.fragments.discover.discover.list.categories

import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverCategoriesBinding
import com.limor.app.extensions.px
import com.limor.app.scenes.utils.recycler.HorizontalSpacingItemDecoration
import com.limor.app.uimodels.CategoryUIModel
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.viewbinding.BindableItem

class CategoriesItem: BindableItem<ItemDiscoverCategoriesBinding>() {

    companion object {
        private val ITEM_WIDTH = 16.px
    }

    private var categoriesListAdapter = GroupieAdapter()

    override fun bind(viewBinding: ItemDiscoverCategoriesBinding, position: Int) {
        viewBinding.categoriesList.apply {
            adapter = categoriesListAdapter
            if (itemDecorationCount == 0) {
                addItemDecoration(HorizontalSpacingItemDecoration(ITEM_WIDTH))
            }
        }
    }

    fun update(categories: List<CategoryUIModel>) {
        categoriesListAdapter.update(
            categories.map {
                SingleCategoryItem(it)
            }
        )
    }

    override fun getLayout() = R.layout.item_discover_categories
    override fun initializeViewBinding(view: View) = ItemDiscoverCategoriesBinding.bind(view)
}
