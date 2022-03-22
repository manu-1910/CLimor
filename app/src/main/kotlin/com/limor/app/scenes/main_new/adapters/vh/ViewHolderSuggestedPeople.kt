package com.limor.app.scenes.main_new.adapters.vh

import androidx.navigation.findNavController
import com.limor.app.R
import com.limor.app.databinding.ItemFeedSuggestedPeopleBinding
import com.limor.app.extensions.px
import com.limor.app.scenes.main.fragments.discover.discover.list.suggestedpeople.SuggestedPersonItem
import com.limor.app.scenes.main_new.fragments.DataItem
import com.limor.app.scenes.utils.recycler.HorizontalSpacingItemDecoration
import com.limor.app.uimodels.FeedSuggestedPeople
import com.limor.app.uimodels.UserUIModel
import com.xwray.groupie.GroupieAdapter

class ViewHolderSuggestedPeople(
    val binding: ItemFeedSuggestedPeopleBinding
) : ViewHolderBindable<DataItem>(binding) {

    companion object {
        private val ITEM_SPACING: Int = 16.px
    }

    private var suggestedPeopleListAdapter = GroupieAdapter()

    override fun bind(item: DataItem) {
        val suggestedPeople = item as FeedSuggestedPeople
        setAdapter()
        updateUsers(suggestedPeople.suggestedPeople)
        setClicks()
    }

    private fun setAdapter() {
        binding.suggestedPeopleList.apply {
            adapter = suggestedPeopleListAdapter
            if (itemDecorationCount == 0) {
                addItemDecoration(HorizontalSpacingItemDecoration(ITEM_SPACING))
            }
        }
    }

    private fun updateUsers(users: List<UserUIModel>) {
        suggestedPeopleListAdapter.update(
            users.map {
                SuggestedPersonItem(it)
            }
        )
    }

    private fun setClicks() {
        binding.seeAllTextView.setOnClickListener {
            it.findNavController()
                .navigate(R.id.action_navigation_home_to_feedSuggestedPeopleFragment)
        }
    }

}
