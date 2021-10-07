package com.limor.app.scenes.main.fragments.discover.discover.list.suggestedpeople

import android.content.Intent
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverSuggestedPersonBinding
import com.limor.app.scenes.auth_new.util.ToastMaker
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileFragment
import com.limor.app.uimodels.UserUIModel
import com.xwray.groupie.Item
import com.xwray.groupie.viewbinding.BindableItem

class SuggestedPersonItem(val suggestedPerson: UserUIModel) :
    BindableItem<ItemDiscoverSuggestedPersonBinding>() {

    override fun bind(viewBinding: ItemDiscoverSuggestedPersonBinding, position: Int) {
        viewBinding.root.setOnClickListener {
            val userProfileIntent = Intent(viewBinding.root.context, UserProfileActivity::class.java)
            userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY, suggestedPerson.username)
            userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY, suggestedPerson.id)
            viewBinding.root.context.startActivity(userProfileIntent)
        }
        viewBinding.personName.text =
            String.format("%s %s", suggestedPerson.firstName, suggestedPerson.lastName)
        viewBinding.personNickname.text = suggestedPerson.username
        if(suggestedPerson.isVerified == true){
            viewBinding.personName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verified_badge, 0)
        } else{
            viewBinding.personName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

        Glide.with(viewBinding.personImage)
            .load(suggestedPerson.getAvatarUrl())
            .signature(ObjectKey(suggestedPerson.getAvatarUrl() ?: ""))
            .circleCrop()
            .into(viewBinding.personImage)
    }

    override fun getLayout() = R.layout.item_discover_suggested_person
    override fun initializeViewBinding(view: View) = ItemDiscoverSuggestedPersonBinding.bind(view)

    override fun isSameAs(other: Item<*>): Boolean {
        if (other is SuggestedPersonItem) {
            return other == this
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SuggestedPersonItem

        if (suggestedPerson != other.suggestedPerson) return false

        return true
    }

    override fun hashCode(): Int {
        return suggestedPerson.hashCode()
    }
}
