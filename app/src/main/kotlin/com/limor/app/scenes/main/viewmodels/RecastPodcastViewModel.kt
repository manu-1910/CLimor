package com.limor.app.scenes.main.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.usecases.LikePodcastUseCase
import com.limor.app.usecases.RecastPodcastUseCase
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class RecastPodcastViewModel  @Inject constructor(
    private val recastPodcastUseCase: RecastPodcastUseCase) : ViewModel() {

    fun reCast(castId: Int) {
        viewModelScope.launch {
            runCatching {
                recastPodcastUseCase.execute(castId)
            }.onFailure {
                Timber.e(it, "Error while recasting")
            }
        }
    }

}