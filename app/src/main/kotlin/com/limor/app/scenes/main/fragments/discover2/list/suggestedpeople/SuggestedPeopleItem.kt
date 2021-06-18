package com.limor.app.scenes.main.fragments.discover2.list.suggestedpeople

import android.view.View
import com.limor.app.R
import com.limor.app.databinding.ItemDiscoverSuggestedPeopleBinding
import com.limor.app.scenes.main.fragments.discover2.mock.MockPerson
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.viewbinding.BindableItem
import com.xwray.groupie.viewbinding.GroupieViewHolder

class SuggestedPeopleItem() : BindableItem<ItemDiscoverSuggestedPeopleBinding>() {

    private var suggestedPeopleListAdapter = GroupieAdapter()

    override fun bind(viewBinding: ItemDiscoverSuggestedPeopleBinding, position: Int) {
        viewBinding.suggestedPeopleList.adapter = suggestedPeopleListAdapter
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

    // Make SuggestedPeopleItem non-recyclable
    override fun isRecyclable(): Boolean = false
    override fun createViewHolder(itemView: View): GroupieViewHolder<ItemDiscoverSuggestedPeopleBinding> {
        return super.createViewHolder(itemView).apply {
            setIsRecyclable(false)
        }
    }
}
