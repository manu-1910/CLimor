package com.limor.app.scenes.main.fragments.discover.hashtag.list.item

import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverHashtagPostsCounterBinding
import com.limor.app.extensions.formatHumanReadable
import com.limor.app.uimodels.TagUIModel
import com.xwray.groupie.viewbinding.BindableItem

class PostsCountItem(
    val tag: TagUIModel
) : BindableItem<ItemDiscoverHashtagPostsCounterBinding>() {

    override fun bind(viewBinding: ItemDiscoverHashtagPostsCounterBinding, position: Int) {
        viewBinding.postsCount.text = tag.count.toLong().formatHumanReadable
    }

    override fun getLayout() = R.layout.item_discover_hashtag_posts_counter
    override fun initializeViewBinding(view: View) =
        ItemDiscoverHashtagPostsCounterBinding.bind(view)
}
