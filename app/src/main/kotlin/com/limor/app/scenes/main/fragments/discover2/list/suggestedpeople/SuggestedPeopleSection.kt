package com.limor.app.scenes.main.fragments.discover2.list.suggestedpeople

import android.content.Context
import com.limor.app.R
import com.limor.app.scenes.main.fragments.discover2.list.items.header.HeaderItem
import com.limor.app.scenes.main.fragments.discover2.mock.MockPerson
import com.xwray.groupie.Section

class SuggestedPeopleSection(context: Context) : Section() {

    companion object {
        private const val SUGGESTED_PEOPLE_ITEM_POSITION = 0
    }

    init {
        setHeader(
            HeaderItem(
                context.getString(R.string.suggested_people),
                action = HeaderItem.HeaderAction(
                    name = context.getString(R.string.see_all),
                    onActionClick = {
                        TODO()
                    }
                )
            )
        )
    }

    fun updateSuggestedPeople(suggestedPeople: List<MockPerson>) {
        val suggestedPeopleItem = if (itemCount < SUGGESTED_PEOPLE_ITEM_POSITION + 1) {
            SuggestedPeopleItem().also { add(it) }
        } else {
            getItem(SUGGESTED_PEOPLE_ITEM_POSITION) as SuggestedPeopleItem
        }

        suggestedPeopleItem.update(suggestedPeople)
    }
}