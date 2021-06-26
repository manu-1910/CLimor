package com.limor.app.scenes.main.fragments.discover.search.list.item

import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverSearchAccountBinding
import com.limor.app.extensions.loadCircleImage
import com.limor.app.scenes.main.fragments.discover.common.mock.MockPerson
import com.xwray.groupie.viewbinding.BindableItem

class AccountSearchItem(
    val account: MockPerson
) : BindableItem<ItemDiscoverSearchAccountBinding>() {

    override fun bind(viewBinding: ItemDiscoverSearchAccountBinding, position: Int) {
        viewBinding.accountName.text = account.name
        viewBinding.accountNickname.text = account.nickName
        viewBinding.accountImage.loadCircleImage(account.imageUrl)
    }

    override fun getLayout() = R.layout.item_discover_search_account
    override fun initializeViewBinding(view: View) = ItemDiscoverSearchAccountBinding.bind(view)
}