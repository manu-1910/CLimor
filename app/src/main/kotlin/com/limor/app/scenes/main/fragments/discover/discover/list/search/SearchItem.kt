package com.limor.app.scenes.main.fragments.discover.discover.list.search

import android.view.View
import androidx.navigation.findNavController
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverSearchBinding
import com.xwray.groupie.viewbinding.BindableItem

class SearchItem: BindableItem<ItemDiscoverSearchBinding>() {

    override fun bind(viewBinding: ItemDiscoverSearchBinding, position: Int) {
        viewBinding.searchBar.processFocus = true
        viewBinding.searchBar.setOnClickListener {
            it.findNavController().navigate(R.id.action_navigation_discover_to_discoverSearchFragment)
        }
    }

    override fun getLayout() = R.layout.item_discover_search
    override fun initializeViewBinding(view: View) = ItemDiscoverSearchBinding.bind(view)
}
