package com.limor.app.scenes.main.fragments.discover.search.list.item

import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverSearchCategoryBinding
import com.xwray.groupie.viewbinding.BindableItem

class CategorySearchItem(
    val category: String
) : BindableItem<ItemDiscoverSearchCategoryBinding>() {

    override fun bind(viewBinding: ItemDiscoverSearchCategoryBinding, position: Int) {
        viewBinding.categoryName.text = category
    }

    override fun getLayout() = R.layout.item_discover_search_category
    override fun initializeViewBinding(view: View) = ItemDiscoverSearchCategoryBinding.bind(view)
}