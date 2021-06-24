package com.limor.app.scenes.main.fragments.discover.suggestedpeople.list

import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverSuggestedPersonBigBinding
import com.limor.app.extensions.loadCircleImage
import com.limor.app.scenes.auth_new.util.ToastMaker
import com.limor.app.scenes.main.fragments.discover.common.mock.MockPerson
import com.xwray.groupie.viewbinding.BindableItem

class SuggestedPersonBigItem(
    val person: MockPerson
) : BindableItem<ItemDiscoverSuggestedPersonBigBinding>() {

    override fun bind(viewBinding: ItemDiscoverSuggestedPersonBigBinding, position: Int) {
        viewBinding.fullName.text = person.name
        viewBinding.personImage.loadCircleImage(person.imageUrl)
        viewBinding.description.text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum finibus metus et vestibulum finibus. Ut semper"
        viewBinding.followBtn.setOnClickListener {
            ToastMaker.showToast(it.context, "Not implemented")
        }
    }

    override fun getLayout() = R.layout.item_discover_suggested_person_big
    override fun initializeViewBinding(view: View) =
        ItemDiscoverSuggestedPersonBigBinding.bind(view)
}