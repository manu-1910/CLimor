package com.limor.app.scenes.main_new.view_model

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.limor.app.FeedItemsQuery
import com.limor.app.apollo.PodcastInteractionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PodcastControlViewModel
@Inject constructor(private val podcastInteractionsRepository: PodcastInteractionsRepository) :
    ViewModel() {

    private val _showPodcastLiveData =
        MutableLiveData<FeedItemsQuery.GetFeedItem?>().apply { }
    val changePodcastFullScreenVisibility: LiveData<FeedItemsQuery.GetFeedItem?>
        get() = _showPodcastLiveData

    private val _podcastUpdatedLiveData =
        MutableLiveData<Int?>().apply { }
    val podcastUpdatedLiveData: LiveData<Int?>
        get() = _podcastUpdatedLiveData

    fun changePodcastFullScreenVisibility(feedItem: FeedItemsQuery.GetFeedItem) {
        viewModelScope.launch {
            _showPodcastLiveData.postValue(feedItem)
            delay(300)
            _showPodcastLiveData.postValue(null)
        }
    }

    fun likePodcast(podcastId: Int) {
        if (podcastId == 0) return
        viewModelScope.launch {
            try {
                val result = podcastInteractionsRepository.likePodcast(podcastId)
                _podcastUpdatedLiveData.postValue(result)
            } catch (e: Exception) {
                //TODO(deliver error to the user)
            }

            delay(300)
            _podcastUpdatedLiveData.postValue(null)
        }
    }

    fun unlikePodcast(podcastId: Int) {
        if (podcastId == 0) return
        viewModelScope.launch {
            try {
                val result = podcastInteractionsRepository.unLikePodcast(podcastId)
                _podcastUpdatedLiveData.postValue(result)
            } catch (e: Exception) {
                //TODO(deliver error to the user)
            }

            delay(300)
            _podcastUpdatedLiveData.postValue(null)
        }
    }

    companion object {
        const val FEED_ITEMS_QUERY_LABEL = "feed_items_query_label"

        suspend fun feedItemToBundle(item: FeedItemsQuery.GetFeedItem): Bundle {
            return withContext(Dispatchers.Default) {
                val json = Gson().toJson(item)
                bundleOf(FEED_ITEMS_QUERY_LABEL to json)
            }
        }

        suspend fun feedItemFromBundle(bundle: Bundle): FeedItemsQuery.GetFeedItem {
            return withContext(Dispatchers.Default) {
                val json = bundle.getString(FEED_ITEMS_QUERY_LABEL, "{}")
                Gson().fromJson(json, FeedItemsQuery.GetFeedItem::class.java)
            }
        }
    }
}