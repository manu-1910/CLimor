package com.limor.app.scenes.main.fragments.discover.discover.list.suggestedpeople

import android.content.Context
import androidx.navigation.NavController
import com.limor.app.R
import com.limor.app.scenes.main.fragments.discover.common.HeaderItem
import com.limor.app.scenes.main.fragments.discover.common.mock.MockPerson
import com.limor.app.uimodels.SuggestedPersonUIModel
import com.xwray.groupie.Section

class SuggestedPeopleSection(
    private val context: Context,
    private val navController: NavController
) : Section() {

    companion object {
        private const val SUGGESTED_PEOPLE_ITEM_POSITION = 1
    }

    fun updateSuggestedPeople(suggestedPeople: List<SuggestedPersonUIModel>) {
        if (suggestedPeople.isNotEmpty()) {
            setHeaderIfNeeded()
        }
        val suggestedPeopleItem = if (itemCount < SUGGESTED_PEOPLE_ITEM_POSITION + 1) {
            SuggestedPeopleItem().also { add(it) }
        } else {
            getItem(SUGGESTED_PEOPLE_ITEM_POSITION) as SuggestedPeopleItem
        }

        suggestedPeopleItem.update(suggestedPeople)
    }

    fun setHeaderIfNeeded() {
        if (groupCount == 0) {
            setHeader(
                HeaderItem(
                    context.getString(R.string.suggested_people),
                    action = HeaderItem.HeaderAction(
                        name = context.getString(R.string.see_all),
                        onActionClick = {
                            navController.navigate(R.id.action_navigation_discover_to_discoverSuggestedPeopleFragment)
                        }
                    )
                )
            )
        }
    }
}