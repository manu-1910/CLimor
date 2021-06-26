package com.limor.app.scenes.main.fragments.discover.search.list.item

import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverSearchHashtagBinding
import com.xwray.groupie.viewbinding.BindableItem

class HashtagSearchItem(
    val hashtag: String
) : BindableItem<ItemDiscoverSearchHashtagBinding>() {

    override fun bind(viewBinding: ItemDiscoverSearchHashtagBinding, position: Int) {
        viewBinding.hashtagName.text = hashtag
        viewBinding.castCount.text = "165 Casts"
    }

    override fun getLayout() = R.layout.item_discover_search_hashtag
    override fun initializeViewBinding(view: View) = ItemDiscoverSearchHashtagBinding.bind(view)
}