package com.limor.app.scenes.main.fragments.discover.suggestedpeople.list

import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverSuggestedPersonBigBinding
import com.limor.app.extensions.loadCircleImage
import com.limor.app.scenes.auth_new.util.ToastMaker
import com.limor.app.uimodels.SuggestedPersonUIModel
import com.xwray.groupie.viewbinding.BindableItem

class SuggestedPersonBigItem(
    val person: SuggestedPersonUIModel,
    val onFollowClick: (person: SuggestedPersonUIModel) -> Unit
) : BindableItem<ItemDiscoverSuggestedPersonBigBinding>() {

    override fun bind(viewBinding: ItemDiscoverSuggestedPersonBigBinding, position: Int) {
        viewBinding.fullName.text = person.getFullName()
        viewBinding.personImage.loadCircleImage(person.imageLinks.small)
        viewBinding.description.text = person.description
        viewBinding.followBtn.apply {
            isFollowed = person.isFollowed
            setOnClickListener {
                onFollowClick(person)
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