package com.limor.app.scenes.main.fragments.discover.suggestedpeople.list

import android.content.Intent
import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverSuggestedPersonBigBinding
import com.limor.app.extensions.loadCircleImage
import com.limor.app.extensions.setRightDrawable
import com.limor.app.scenes.auth_new.util.ToastMaker
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileFragment
import com.limor.app.uimodels.UserUIModel
import com.xwray.groupie.viewbinding.BindableItem

class SuggestedPersonBigItem(
    val person: UserUIModel,
    val onFollowClick: (person: UserUIModel, follow: Boolean) -> Unit
) : BindableItem<ItemDiscoverSuggestedPersonBigBinding>() {

    override fun bind(viewBinding: ItemDiscoverSuggestedPersonBigBinding, position: Int) {
        viewBinding.fullName.text = person.getFullName()
        if (person.isVerified == true) {
            viewBinding.fullName.setRightDrawable(
                R.drawable.ic_verified_badge,
                R.dimen.chip_close_icon_size
            )
        } else {
            viewBinding.fullName.setRightDrawable(0, R.dimen.chip_close_icon_size)
        }
        person.getAvatarUrl()?.let {
            viewBinding.personImage.loadCircleImage(it)
        }

        viewBinding.description.text = person.description
        person.isFollowed?.let {
            viewBinding.followBtn.apply {
                isFollowed = person.isFollowed
                setOnClickListener {
                    onFollowClick(person, viewBinding.followBtn.isFollowed)
                }
            }
        }
    }

    override fun getLayout() = R.layout.item_discover_suggested_person_big
    override fun initializeViewBinding(view: View) =
        ItemDiscoverSuggestedPersonBigBinding.bind(view)
}