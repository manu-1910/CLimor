package com.limor.app.scenes.main_new.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.FeedItemsQuery
import com.limor.app.apollo.GeneralInfoRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeFeedViewModel @Inject constructor(
    val generalInfoRepository: GeneralInfoRepository
) : ViewModel() {

    private var _homeFeedLiveData =
        MutableLiveData<List<FeedItemsQuery.FeedItem>?>()
    val homeFeedLiveData: LiveData<List<FeedItemsQuery.FeedItem>?>
        get() = _homeFeedLiveData

    private var _homeFeedErrorLiveData =
        MutableLiveData<String>()
    val homeFeedErrorLiveData: LiveData<String>
        get() = _homeFeedErrorLiveData

    fun loadHomeFeed() {
        viewModelScope.launch {
            try {
                val feedItems = generalInfoRepository.fetchHomeFeed()
                _homeFeedLiveData.postValue(feedItems)
            } catch (e: Exception) {
                _homeFeedErrorLiveData.postValue(e.localizedMessage)
            }

        }
    }

}