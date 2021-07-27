package com.limor.app.scenes.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.uimodels.CreateRecastUIModel
import com.limor.app.uimodels.ShareCastUIModel
import com.limor.app.usecases.SharePodcastUseCase
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SharePodcastViewModel @Inject constructor(
    private val sharePodcastUseCase: SharePodcastUseCase
) : ViewModel(){
    private var _sharedResponse =
        MutableLiveData<ShareCastUIModel?>()
    val sharedResponse: LiveData<ShareCastUIModel?>
        get() = _sharedResponse

    fun share(castId : Int){
        viewModelScope.launch {
            runCatching {
                val result = sharePodcastUseCase.execute(castId)
                _sharedResponse.postValue(result)
            }.onFailure {
                Timber.e(it, "Error while recasting")
                _sharedResponse.postValue(null)
            }
        }
    }
}