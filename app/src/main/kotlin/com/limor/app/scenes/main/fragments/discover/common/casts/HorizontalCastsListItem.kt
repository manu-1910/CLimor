package com.limor.app.scenes.main.fragments.discover.common.casts

import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemHorizontalCastsListBinding
import com.limor.app.extensions.px
import com.limor.app.scenes.utils.recycler.HorizontalSpacingItemDecoration
import com.limor.app.uimodels.CastUIModel
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.viewbinding.BindableItem

class HorizontalCastsListItem : BindableItem<ItemHorizontalCastsListBinding>() {

    companion object {
        private val ITEM_WIDTH: Int = 308.px
        private val ITEM_SPACING: Int = 16.px
    }

    private var horizontalCastsListAdapter = GroupieAdapter()

    override fun bind(viewBinding: ItemHorizontalCastsListBinding, position: Int) {
        viewBinding.list.apply {
            adapter = horizontalCastsListAdapter
            if (itemDecorationCount == 0) {
                addItemDecoration(HorizontalSpacingItemDecoration(ITEM_SPACING))
            }
        }
    }

    fun update(featuredCasts: List<CastUIModel>) {
        horizontalCastsListAdapter.update(
            featuredCasts.map {
                BigCastItem(it, ITEM_WIDTH)
            }
        )
    }

    override fun getLayout() = R.layout.item_horizontal_casts_list
    override fun initializeViewBinding(view: View) = ItemHorizontalCastsListBinding.bind(view)
}