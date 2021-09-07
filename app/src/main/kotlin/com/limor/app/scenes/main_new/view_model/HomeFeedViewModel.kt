package com.limor.app.scenes.main_new.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.common.Constants
import com.limor.app.uimodels.CastUIModel
import com.limor.app.usecases.GetHomeFeedCastsUseCase
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class HomeFeedViewModel @Inject constructor(
   private val getHomeFeedCastsUseCase: GetHomeFeedCastsUseCase
) : ViewModel() {

    private val _homeFeedData = MutableLiveData<List<CastUIModel>>()
    val homeFeedData: LiveData<List<CastUIModel>> get() = _homeFeedData

    init {
        loadHomeFeed()
    }

    fun loadHomeFeed(offset: Int = 0, limit: Int = Constants.HOME_FEED_ITEM_BATCH_SIZE) {
        viewModelScope.launch {
            getHomeFeedCastsUseCase.execute(limit = limit, offset = offset)
                .onSuccess {
                    Timber.d("${it.size} home feed items")
                    _homeFeedData.value = it
                }
                .onFailure {
                    Timber.e(it, "Error while fetching home feed")
                }
        }
    }
}
