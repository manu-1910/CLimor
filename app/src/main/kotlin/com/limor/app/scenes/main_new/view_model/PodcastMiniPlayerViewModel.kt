package com.limor.app.scenes.main_new.view_model

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.limor.app.FeedItemsQuery
import javax.inject.Inject

class PodcastMiniPlayerViewModel @Inject constructor() : ViewModel() {

    private val _showPodcastLiveData =
        MutableLiveData<FeedItemsQuery.FeedItem?>().apply { }
    val changePodcastFullScreenVisibility: LiveData<FeedItemsQuery.FeedItem?>
        get() = _showPodcastLiveData

    fun changePodcastFullScreenVisibility(feedItem: FeedItemsQuery.FeedItem) {
        _showPodcastLiveData.postValue(feedItem)
    }

    companion object {
        const val FEED_ITEMS_QUERY_LABEL = "feed_items_query_label"

        fun feedItemToBundle(item: FeedItemsQuery.FeedItem): Bundle {
            val json = Gson().toJson(item)
            return bundleOf(FEED_ITEMS_QUERY_LABEL to json)
        }

        fun feedItemFromBundle(bundle: Bundle): FeedItemsQuery.FeedItem {
            val json = bundle.getString(FEED_ITEMS_QUERY_LABEL, "{}")
            return Gson().fromJson(json, FeedItemsQuery.FeedItem::class.java)
        }
    }
}