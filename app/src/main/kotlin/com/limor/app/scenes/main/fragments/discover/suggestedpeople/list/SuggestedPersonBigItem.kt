package com.limor.app.scenes.main.fragments.discover.suggestedpeople.list

import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverSuggestedPersonBigBinding
import com.limor.app.extensions.loadCircleImage
import com.limor.app.scenes.auth_new.util.ToastMaker
import com.limor.app.uimodels.UserUIModel
import com.xwray.groupie.viewbinding.BindableItem

class SuggestedPersonBigItem(
    val person: UserUIModel,
    val onFollowClick: (person: UserUIModel) -> Unit
) : BindableItem<ItemDiscoverSuggestedPersonBigBinding>() {

    override fun bind(viewBinding: ItemDiscoverSuggestedPersonBigBinding, position: Int) {
        viewBinding.fullName.text = person.getFullName()
        person.imageLinks?.small?.let {
            viewBinding.personImage.loadCircleImage(it)
        }

        viewBinding.description.text = person.description
        person.isFollowed?.let {
            viewBinding.followBtn.apply {
                isFollowed = person.isFollowed
                setOnClickListener {
                    onFollowClick(person)
                }
            }
        }
        viewBinding.root.setOnClickListener {
            ToastMaker.showToast(it.context, "Not implemented")
        }
    }

    override fun getLayout() = R.layout.item_discover_suggested_person_big
    override fun initializeViewBinding(view: View) =
        ItemDiscoverSuggestedPersonBigBinding.bind(view)
}