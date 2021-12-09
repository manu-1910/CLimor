package com.limor.app.scenes.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.apollo.UserRepository
import com.limor.app.uimodels.CastUIModel
import com.limor.app.usecases.GetBlockedUsersUseCase
import com.limor.app.usecases.GetPodcastByIDUseCase
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class PodcastViewModel @Inject constructor(
    private val getPodcastByIDUseCase: GetPodcastByIDUseCase,
    private val getUserUseCase: UserRepository,
): ViewModel() {

    private val _cast = MutableLiveData<CastUIModel>()
    val cast: LiveData<CastUIModel> get() = _cast

    fun loadCast(id: Int) {
        viewModelScope.launch {
            getPodcastByIDUseCase.execute(id)
                .onSuccess {
                    _cast.value = it
                }
                .onFailure {
                    Timber.e(it, "Error while fetching cast with id: $id")
                }
        }
    }

    fun deleteCastById(id: Int) {
        viewModelScope.launch {
            getPodcastByIDUseCase.deleteCast(id)
        }
    }

    fun blockUser(userId: Int): LiveData<Boolean> {
        val liveData = MutableLiveData<Boolean>()
        viewModelScope.launch {
            liveData.postValue(getUserUseCase.blockUser(userId) == true)
        }
        return liveData
    }

    fun reportCast(s: String,id:Int?) {
        viewModelScope.launch {
            getPodcastByIDUseCase.reportCast(s, id)
        }
    }

}