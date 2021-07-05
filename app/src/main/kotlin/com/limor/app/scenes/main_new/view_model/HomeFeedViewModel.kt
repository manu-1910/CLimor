package com.limor.app.scenes.main_new.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.FeedItemsQuery
import com.limor.app.GetUserProfileByIdQuery
import com.limor.app.GetUserProfileQuery
import com.limor.app.apollo.GeneralInfoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeFeedViewModel @Inject constructor(
    val generalInfoRepository: GeneralInfoRepository
) : ViewModel() {

    private val _homeFeedLiveData =
        MutableLiveData<List<FeedItemsQuery.GetFeedItem>?>()
    val homeFeedLiveData: LiveData<List<FeedItemsQuery.GetFeedItem>?>
        get() = _homeFeedLiveData

    private var _homeFeedErrorLiveData =
        MutableLiveData<String>()
    val homeFeedErrorLiveData: LiveData<String>
        get() = _homeFeedErrorLiveData



    fun loadHomeFeed(offset:Int = 0) {
        viewModelScope.launch {
            try {
                val feedItems = generalInfoRepository.fetchHomeFeed(offset = offset)
                _homeFeedLiveData.postValue(feedItems)
                delay(300)
                _homeFeedLiveData.postValue(null)
            } catch (e: Exception) {
                _homeFeedErrorLiveData.postValue(e.localizedMessage)
            }
        }
    }




}