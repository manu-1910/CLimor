package com.limor.app.scenes.main_new.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.GetCommentsByPodcastsQuery
import com.limor.app.apollo.PodcastInteractionsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class PodcastFullPlayerViewModel @Inject constructor(
    private val podcastInteractionsRepository: PodcastInteractionsRepository
) : ViewModel() {

    private val _commentsLiveData =
        MutableLiveData<List<GetCommentsByPodcastsQuery.GetCommentsByPodcast>?>()
    val commentsLiveData: LiveData<List<GetCommentsByPodcastsQuery.GetCommentsByPodcast>?>
        get() = _commentsLiveData

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