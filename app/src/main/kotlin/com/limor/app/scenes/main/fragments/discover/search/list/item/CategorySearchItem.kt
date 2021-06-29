package com.limor.app.scenes.main.fragments.discover.search.list.item

import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverSearchCategoryBinding
import com.limor.app.uimodels.CategoryUIModel
import com.xwray.groupie.viewbinding.BindableItem

class CategorySearchItem(
    val category: CategoryUIModel
) : BindableItem<ItemDiscoverSearchCategoryBinding>() {

    override fun bind(viewBinding: ItemDiscoverSearchCategoryBinding, position: Int) {
        viewBinding.categoryName.text = category.name
    }

    override fun getLayout() = R.layout.item_discover_search_category
    override fun initializeViewBinding(view: View) = ItemDiscoverSearchCategoryBinding.bind(view)
}