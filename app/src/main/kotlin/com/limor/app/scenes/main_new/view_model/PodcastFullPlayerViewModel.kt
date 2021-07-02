package com.limor.app.scenes.main_new.view_model

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.limor.app.FeedItemsQuery
import javax.inject.Inject

class PodcastFullPlayerViewModel @Inject constructor(): ViewModel() {

    private val _showPodcastLiveData =
        MutableLiveData<FeedItemsQuery.FeedItem?>().apply { }
    val showPodcastLiveData: LiveData<FeedItemsQuery.FeedItem?>
        get() = _showPodcastLiveData


    fun setArgs(bundle:  Bundle?){
        if(bundle == null) return
        val feedItem = PodcastMiniPlayerViewModel.feedItemFromBundle(bundle)
        _showPodcastLiveData.postValue(feedItem)
    }
}