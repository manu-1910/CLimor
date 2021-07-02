package com.limor.app.scenes.main_new.view_model

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.FeedItemsQuery
import com.limor.app.GetCommentsByPodcastsQuery
import com.limor.app.apollo.PodcastInteractionsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class PodcastFullPlayerViewModel @Inject constructor(
    private val podcastInteractionsRepository: PodcastInteractionsRepository
) : ViewModel() {

    private val _showPodcastLiveData =
        MutableLiveData<FeedItemsQuery.Podcast?>()
    val showPodcastLiveData: LiveData<FeedItemsQuery.Podcast?>
        get() = _showPodcastLiveData

    private val _commentsLiveData =
        MutableLiveData<List<GetCommentsByPodcastsQuery.GetCommentsByPodcast>?>()
    val commentsLiveData: LiveData<List<GetCommentsByPodcastsQuery.GetCommentsByPodcast>?>
        get() = _commentsLiveData


    fun setArgs(bundle: Bundle?) {
        if (bundle == null) return
        viewModelScope.launch {
            try {
                val feedItem = PodcastControlViewModel.feedItemFromBundle(bundle)
                _showPodcastLiveData.value = feedItem.podcast
                delay(300)
                _showPodcastLiveData.value = null
            } catch (e: Exception) {
                //TODO deliver error to user
            }
        }
    }

    fun getPodcastById(podcastId: Int) {
        if (podcastId == 0) return

        viewModelScope.launch {
            try {
                val podcast = podcastInteractionsRepository.getPodcastById(podcastId)
                _showPodcastLiveData.value = podcast
                delay(300)
                _showPodcastLiveData.value = null
            } catch (e: Exception) {
                //TODO deliver error to user
            }
        }
    }

    fun loadComments(podcastId: Int) {
        if (podcastId == 0)
            return
        viewModelScope.launch {
            try {
                val comments = podcastInteractionsRepository.getCommentsByPodcast(podcastId)
                _commentsLiveData.value = comments
                delay(300)
                _commentsLiveData.value = null
            } catch (e: Exception) {
                //TODO deliver error to user
            }
        }
    }
}