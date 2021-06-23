package com.limor.app.scenes.main.fragments.discover.discover.list.suggestedpeople

import android.view.View
import com.bumptech.glide.Glide
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverSuggestedPersonBinding
import com.limor.app.scenes.main.fragments.discover.common.mock.MockPerson
import com.xwray.groupie.Item
import com.xwray.groupie.viewbinding.BindableItem

class SuggestedPersonItem(val mockSuggestedPerson: MockPerson) : BindableItem<ItemDiscoverSuggestedPersonBinding>() {

    override fun bind(viewBinding: ItemDiscoverSuggestedPersonBinding, position: Int) {
        viewBinding.root.setOnClickListener {
            TODO()
        }
        viewBinding.personName.text = mockSuggestedPerson.name
        viewBinding.personNickname.text = mockSuggestedPerson.nickName

        Glide.with(viewBinding.personImage)
            .load(mockSuggestedPerson.imageUrl)
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

        if (mockSuggestedPerson != other.mockSuggestedPerson) return false

        return true
    }

    override fun hashCode(): Int {
        return mockSuggestedPerson.hashCode()
    }
}
