package com.limor.app.scenes.main.fragments.discover2.discover.list.search

import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverSearchBinding
import com.xwray.groupie.viewbinding.BindableItem

class SearchItem: BindableItem<ItemDiscoverSearchBinding>() {

    override fun bind(viewBinding: ItemDiscoverSearchBinding, position: Int) {
        viewBinding.searchBar.setOnQueryTextListener { newText ->
            TODO()
        }
    }

    override fun getLayout() = R.layout.item_discover_search
    override fun initializeViewBinding(view: View) = ItemDiscoverSearchBinding.bind(view)
}