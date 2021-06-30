package com.limor.app.scenes.main.fragments.discover.search.list.item

import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverSearchAccountBinding
import com.limor.app.extensions.loadCircleImage
import com.limor.app.uimodels.UserUIModel
import com.xwray.groupie.viewbinding.BindableItem

class AccountSearchItem(
    val account: UserUIModel
) : BindableItem<ItemDiscoverSearchAccountBinding>() {

    override fun bind(viewBinding: ItemDiscoverSearchAccountBinding, position: Int) {
        viewBinding.accountName.text = account.getFullName()
        viewBinding.accountNickname.text = account.username
        viewBinding.accountImage.loadCircleImage(account.imageLinks.small)
    }

    override fun getLayout() = R.layout.item_discover_search_account
    override fun initializeViewBinding(view: View) = ItemDiscoverSearchAccountBinding.bind(view)
}