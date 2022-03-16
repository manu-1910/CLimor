package com.limor.app.scenes.main_new.adapters.vh

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverSearchAccountBinding
import com.limor.app.extensions.loadCircleImage
import com.limor.app.extensions.setRightDrawable
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileFragment
import com.limor.app.uimodels.UserUIModel

class RecastUserViewHolder(
    val viewBinding: ItemDiscoverSearchAccountBinding,
    val onFollowUserClick: (account: UserUIModel, follow: Boolean) -> Unit
) : RecyclerView.ViewHolder(viewBinding.root){

    fun bind(user: UserUIModel){
        viewBinding.accountName.text = user.getFullName()
        viewBinding.accountNickname.text = "@${user.username}";
        if(user.isVerified == true){
            viewBinding.accountName.setRightDrawable(R.drawable.ic_verified_badge, R.dimen.marginMedium)
        } else{
            viewBinding.accountName.setRightDrawable(0, R.dimen.marginMedium)
        }
        user.getAvatarUrl()?.let {
            viewBinding.accountImage.loadCircleImage(it)
        }
        viewBinding.accountContainer.setOnClickListener {
            val userProfileIntent =
                Intent(viewBinding.root.context, UserProfileActivity::class.java)
            userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY, user.username)
            userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY, user.id)
            viewBinding.root.context.startActivity(userProfileIntent)
        }
        viewBinding.followBtn.isFollowed = user.isFollowed!!
        viewBinding.followBtn.visibility = if(!signedInUserWith(user.id,viewBinding.root.context)) View.VISIBLE else View.INVISIBLE
        viewBinding.followBtn.setOnClickListener {
            onFollowUserClick(user, viewBinding.followBtn.isFollowed)
        }
    }

    private fun signedInUserWith(id: Int, context: Context): Boolean {
        return id == PrefsHandler.getCurrentUserId(context)
    }

}