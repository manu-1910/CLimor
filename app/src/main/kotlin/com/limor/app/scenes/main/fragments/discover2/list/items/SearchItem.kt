package com.limor.app.scenes.main.fragments.discover2.list.items

import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverSearchBinding
import com.xwray.groupie.viewbinding.BindableItem

class SearchItem(
    private val onQueryTextChange: (newText: String) -> Unit
): BindableItem<ItemDiscoverSearchBinding>() {

    override fun bind(viewBinding: ItemDiscoverSearchBinding, position: Int) {
        viewBinding.searchBar.setOnQueryTextListener(onQueryTextChange = onQueryTextChange)
    }

    override fun getLayout() = R.layout.item_discover_search
    override fun initializeViewBinding(view: View) = ItemDiscoverSearchBinding.bind(view)
}