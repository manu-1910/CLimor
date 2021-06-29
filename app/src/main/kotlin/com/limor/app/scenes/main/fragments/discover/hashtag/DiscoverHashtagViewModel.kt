package com.limor.app.scenes.main.fragments.discover.hashtag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.uimodels.CastUIModel
import com.limor.app.usecases.GetCastsByHashtagUseCase
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class DiscoverHashtagViewModel @Inject constructor(
    private val getCastsByHashtagUseCase: GetCastsByHashtagUseCase
): ViewModel() {

    private val _recentCasts = MutableLiveData<List<CastUIModel>>()
    val recentCasts: LiveData<List<CastUIModel>> = _recentCasts

    private val _topCasts = MutableLiveData<List<CastUIModel>>()
    val topCasts: LiveData<List<CastUIModel>> = _topCasts

    fun loadCasts(tagId: Int) {
        viewModelScope.launch {
            getCastsByHashtagUseCase.execute(tagId, limit = 5)
                .onSuccess {
                    _recentCasts.value = it.sortedBy { it.createdAt }
                }
                .onFailure {
                    Timber.e(it, "Error while getting casts by tag")
                }
        }
    }
}