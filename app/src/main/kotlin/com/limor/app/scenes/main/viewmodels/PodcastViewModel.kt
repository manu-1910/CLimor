package com.limor.app.scenes.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.uimodels.CastUIModel
import com.limor.app.usecases.GetPodcastByIDUseCase
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class PodcastViewModel @Inject constructor(
    private val getPodcastByIDUseCase: GetPodcastByIDUseCase
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

}