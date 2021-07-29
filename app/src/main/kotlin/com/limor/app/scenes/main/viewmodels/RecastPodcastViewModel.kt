package com.limor.app.scenes.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.uimodels.CreateRecastUIModel
import com.limor.app.uimodels.DeleteRecastUIModel
import com.limor.app.usecases.RecastPodcastUseCase
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class RecastPodcastViewModel  @Inject constructor(
    private val recastPodcastUseCase: RecastPodcastUseCase) : ViewModel() {

    private var _recastedResponse =
        MutableLiveData<CreateRecastUIModel?>()
    val recastedResponse: LiveData<CreateRecastUIModel?>
        get() = _recastedResponse

    private var _deleteRecastResponse =
        MutableLiveData<DeleteRecastUIModel?>()
    val deleteRecastResponse: LiveData<DeleteRecastUIModel?>
        get() = _deleteRecastResponse

    fun reCast(castId: Int) {
        viewModelScope.launch {
            runCatching {
                val result = recastPodcastUseCase.executeRecast(castId)
                _recastedResponse.postValue(result)
            }.onFailure {
                Timber.e(it, "Error while recasting")
                _recastedResponse.postValue(null)
            }
        }
    }

    fun deleteRecast(castId: Int){
        viewModelScope.launch {
            runCatching {
                val result = recastPodcastUseCase.executeDeleteRecast(castId)
                _deleteRecastResponse.postValue(result)
            }.onFailure {
                Timber.e(it, "Error while deleting a recast")
                _deleteRecastResponse.postValue(null)
            }
        }
    }

}