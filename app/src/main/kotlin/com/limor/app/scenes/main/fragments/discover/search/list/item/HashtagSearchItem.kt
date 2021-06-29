package com.limor.app.scenes.main.fragments.discover.search.list.item

import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverSearchHashtagBinding
import com.limor.app.scenes.main.fragments.discover.hashtag.DiscoverHashtagFragment
import com.limor.app.uimodels.TagUIModel
import com.xwray.groupie.viewbinding.BindableItem

class HashtagSearchItem(
    val hashtag: TagUIModel
) : BindableItem<ItemDiscoverSearchHashtagBinding>() {

    override fun bind(viewBinding: ItemDiscoverSearchHashtagBinding, position: Int) {
        viewBinding.hashtagName.text = hashtag.tag
        viewBinding.castCount.text = String.format("%d Casts", hashtag.count)
        viewBinding.root.setOnClickListener {
            it.findNavController()
                .navigate(
                    R.id.action_discoverSearchFragment_to_discoverHashtagFragment,
                    bundleOf(DiscoverHashtagFragment.HASHTAG_KEY to hashtag)
                )
        }
    }

    override fun getLayout() = R.layout.item_discover_search_hashtag
    override fun initializeViewBinding(view: View) = ItemDiscoverSearchHashtagBinding.bind(view)
}