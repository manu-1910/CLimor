package com.limor.app.scenes.main_new.fragments

interface DataItem {

    var itemType: ItemType

    enum class ItemType(val type: Int) {
        NONE(0),
        FEED_ITEM(1),
        FEED_RECASTED_ITEM(2),
        SUGGESTED_USER_ITEM(3),
        FEATURED_CAST_ITEM(4)
    }

}