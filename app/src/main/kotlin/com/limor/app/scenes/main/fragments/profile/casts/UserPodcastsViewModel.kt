package com.limor.app.scenes.main.fragments.profile.casts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.uimodels.CastUIModel
import com.limor.app.usecases.GetPodcastsByUserUseCase
import com.limor.app.usecases.LikePodcastUseCase
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class UserPodcastsViewModel @Inject constructor(
    private val likePodcastUseCase: LikePodcastUseCase,
    private val getPodcastsByUserUseCase: GetPodcastsByUserUseCase
) : ViewModel() {

    private val _casts = MutableLiveData<List<CastUIModel>>()
    val casts: LiveData<List<CastUIModel>> get() = _casts

    fun loadCasts(
        userId: Int,
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0
    ) {
        viewModelScope.launch {
            Timber.d("casts -> $userId $limit $offset")
            getPodcastsByUserUseCase.execute(userId, limit, offset)
                .onSuccess {
                    _casts.value = it
                }
                .onFailure {
                    _casts.value = emptyList()
                    Timber.e(it, "Error while loading user casts")
                }
        }
    }

    fun likeCast(cast: CastUIModel, like: Boolean) {
        viewModelScope.launch {
            runCatching {
                likePodcastUseCase.execute(cast.id, like)
            }.onFailure {
                Timber.e(it, "Error while liking cast")
            }
        }
    }
}
