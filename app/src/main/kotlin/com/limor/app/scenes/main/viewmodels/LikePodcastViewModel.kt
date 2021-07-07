package com.limor.app.scenes.main.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.uimodels.CastUIModel
import com.limor.app.usecases.LikePodcastUseCase
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class LikePodcastViewModel @Inject constructor(
    private val likePodcastUseCase: LikePodcastUseCase
): ViewModel() {

    fun likeCast(castId: Int, like: Boolean) {
        viewModelScope.launch {
            runCatching {
                likePodcastUseCase.execute(castId, like)
            }.onFailure {
                Timber.e(it, "Error while liking cast")
            }
        }
    }
}