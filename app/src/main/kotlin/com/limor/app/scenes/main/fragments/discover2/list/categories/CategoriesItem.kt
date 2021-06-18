package com.limor.app.scenes.main.fragments.discover2.list.categories

import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverCategoriesBinding
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.viewbinding.BindableItem
import com.xwray.groupie.viewbinding.GroupieViewHolder

class CategoriesItem: BindableItem<ItemDiscoverCategoriesBinding>() {

    private var categoriesListAdapter = GroupieAdapter()

    override fun bind(viewBinding: ItemDiscoverCategoriesBinding, position: Int) {
        viewBinding.categoriesList.adapter = categoriesListAdapter
    }

    fun update(categories: List<String>) {
        categoriesListAdapter.update(
            categories.map {
                SingleCategoryItem(it)
            }
        )
    }

    override fun getLayout() = R.layout.item_discover_categories
    override fun initializeViewBinding(view: View) = ItemDiscoverCategoriesBinding.bind(view)

    // Make CategoriesItem non-recyclable
    override fun isRecyclable(): Boolean = false
    override fun createViewHolder(itemView: View): GroupieViewHolder<ItemDiscoverCategoriesBinding> {
        return super.createViewHolder(itemView).apply {
            setIsRecyclable(false)
        }
    }
}
