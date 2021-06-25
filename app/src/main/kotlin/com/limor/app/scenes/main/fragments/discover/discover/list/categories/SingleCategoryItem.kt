package com.limor.app.scenes.main.fragments.discover.discover.list.categories

import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.limor.app.R
import com.limor.app.databinding.ItemChipCategoryBinding
import com.limor.app.scenes.main.fragments.discover.category.DiscoverCategoryFragment
import com.limor.app.uimodels.CategoryUIModel
import com.xwray.groupie.Item
import com.xwray.groupie.viewbinding.BindableItem

class SingleCategoryItem(val category: CategoryUIModel) : BindableItem<ItemChipCategoryBinding>() {

    override fun bind(viewBinding: ItemChipCategoryBinding, position: Int) {
        viewBinding.chip.text = category.name
        viewBinding.chip.setOnClickListener {
            it.findNavController().navigate(
                R.id.action_navigation_discover_to_discoverCategoryFragment, bundleOf(
                    DiscoverCategoryFragment.CATEGORY_KEY to category
                )
            )
        }
    }

    override fun getLayout() = R.layout.item_chip_category
    override fun initializeViewBinding(view: View) = ItemChipCategoryBinding.bind(view)


    override fun isSameAs(other: Item<*>): Boolean {
        if (other is SingleCategoryItem) {
            return other == this
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SingleCategoryItem

        if (category != other.category) return false

        return true
    }

    override fun hashCode(): Int {
        return category.hashCode()
    }
}