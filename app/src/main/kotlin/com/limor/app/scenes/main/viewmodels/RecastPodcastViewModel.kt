package com.limor.app.scenes.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.CreateRecastMutation
import com.limor.app.usecases.RecastPodcastUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class RecastPodcastViewModel  @Inject constructor(
    private val recastPodcastUseCase: RecastPodcastUseCase) : ViewModel() {

    private var _recastedResponse =
        MutableLiveData<CreateRecastMutation.CreateRecast?>()
    val recatedResponse: LiveData<CreateRecastMutation.CreateRecast?>
        get() = _recastedResponse

    fun reCast(castId: Int) {
        viewModelScope.launch {
            runCatching {
                val result = recastPodcastUseCase.execute(castId)
                _recastedResponse.postValue(result)
            }.onFailure {
                Timber.e(it, "Error while recasting")
                _recastedResponse.postValue(null)
            }
        }
    }

}