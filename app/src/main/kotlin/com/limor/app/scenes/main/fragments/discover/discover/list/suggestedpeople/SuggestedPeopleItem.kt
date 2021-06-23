package com.limor.app.scenes.main.fragments.discover.discover.list.suggestedpeople

import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverSuggestedPeopleBinding
import com.limor.app.extensions.px
import com.limor.app.scenes.main.fragments.discover.common.mock.MockPerson
import com.limor.app.scenes.utils.recycler.HorizontalSpacingItemDecoration
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.viewbinding.BindableItem

class SuggestedPeopleItem() : BindableItem<ItemDiscoverSuggestedPeopleBinding>() {

    companion object {
        private val ITEM_SPACING: Int = 16.px
    }

    private var suggestedPeopleListAdapter = GroupieAdapter()

    override fun bind(viewBinding: ItemDiscoverSuggestedPeopleBinding, position: Int) {
        viewBinding.suggestedPeopleList.apply {
            adapter = suggestedPeopleListAdapter
            if (itemDecorationCount == 0) {
                addItemDecoration(HorizontalSpacingItemDecoration(ITEM_SPACING))
            }
        }
    }

    fun update(suggestedPeople: List<MockPerson>) {
        suggestedPeopleListAdapter.update(
            suggestedPeople.map {
                SuggestedPersonItem(it)
            }
        )
    }

    override fun getLayout() = R.layout.item_discover_suggested_people
    override fun initializeViewBinding(view: View) = ItemDiscoverSuggestedPeopleBinding.bind(view)
}
