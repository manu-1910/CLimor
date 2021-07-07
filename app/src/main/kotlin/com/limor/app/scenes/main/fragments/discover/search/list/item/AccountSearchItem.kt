package com.limor.app.scenes.main.fragments.discover.search.list.item

import android.content.Intent
import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverSearchAccountBinding
import com.limor.app.extensions.loadCircleImage
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileFragment
import com.limor.app.uimodels.UserUIModel
import com.xwray.groupie.viewbinding.BindableItem

class AccountSearchItem(
    val account: UserUIModel,
    val onFollowClick: (account: UserUIModel, follow: Boolean) -> Unit
) : BindableItem<ItemDiscoverSearchAccountBinding>() {

    override fun bind(viewBinding: ItemDiscoverSearchAccountBinding, position: Int) {
        viewBinding.accountName.text = account.getFullName()
        viewBinding.accountNickname.text = account.username
        account.imageLinks?.small?.let {
            viewBinding.accountImage.loadCircleImage(it)
        }
        viewBinding.accountContainer.setOnClickListener {
            val userProfileIntent =
                Intent(viewBinding.root.context, UserProfileActivity::class.java)
            userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY, account.username)
            userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY, account.id)
            viewBinding.root.context.startActivity(userProfileIntent)
        }
        viewBinding.followBtn.isFollowed = account.isFollowed!!
        viewBinding.followBtn.setOnClickListener {
            onFollowClick(account, viewBinding.followBtn.isFollowed)
        }
    }

    override fun getLayout() = R.layout.item_discover_search_account
    override fun initializeViewBinding(view: View) = ItemDiscoverSearchAccountBinding.bind(view)
}