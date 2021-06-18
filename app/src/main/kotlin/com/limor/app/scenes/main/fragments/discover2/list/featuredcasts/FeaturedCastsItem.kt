package com.limor.app.scenes.main.fragments.discover2.list.featuredcasts

import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverFeaturedCastsBinding
import com.limor.app.scenes.main.fragments.discover2.mock.MockCast
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.viewbinding.BindableItem
import com.xwray.groupie.viewbinding.GroupieViewHolder

class FeaturedCastsItem: BindableItem<ItemDiscoverFeaturedCastsBinding>() {

    private var featuredCastsListAdapter = GroupieAdapter()

    override fun bind(viewBinding: ItemDiscoverFeaturedCastsBinding, position: Int) {
        viewBinding.featuredCastsList.adapter = featuredCastsListAdapter
    }

    fun update(featuredCasts: List<MockCast>) {
        featuredCastsListAdapter.update(
            featuredCasts.map {
                FeaturedCastItem(it)
            }
        )
    }

    override fun getLayout() = R.layout.item_discover_featured_casts
    override fun initializeViewBinding(view: View) = ItemDiscoverFeaturedCastsBinding.bind(view)

    // Make FeaturedCastsItem non-recyclable
    override fun isRecyclable(): Boolean = false
    override fun createViewHolder(itemView: View): GroupieViewHolder<ItemDiscoverFeaturedCastsBinding> {
        return super.createViewHolder(itemView).apply {
            setIsRecyclable(false)
        }
    }
}
