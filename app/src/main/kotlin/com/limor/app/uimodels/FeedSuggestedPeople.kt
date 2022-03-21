package com.limor.app.uimodels

import com.limor.app.scenes.main_new.fragments.DataItem

class FeedSuggestedPeople(users: List<UserUIModel> = ArrayList()) :
    DataItem {
    override var itemType: DataItem.ItemType = DataItem.ItemType.SUGGESTED_USER_ITEM
    var suggestedPeople: List<UserUIModel> = users
    val id: Long
        get() {
            var id: Long = 0
            suggestedPeople.map { userUIModel -> id + userUIModel.id }
            return id
        }
}

fun FeedSuggestedPeople.isEqualTo(suggestedPeople: FeedSuggestedPeople): Boolean {
    if (this.suggestedPeople.size == suggestedPeople.suggestedPeople.size) {
        this.suggestedPeople.forEachIndexed { position, user ->
            if (user != suggestedPeople.suggestedPeople[position])
                return false
        }
        return true
    } else {
        return false
    }
}