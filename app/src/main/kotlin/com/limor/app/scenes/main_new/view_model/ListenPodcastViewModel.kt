package com.limor.app.scenes.main_new.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.usecases.ListenPodcastUseCase
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ListenPodcastViewModel @Inject constructor(
    private val listenPodcastUseCase: ListenPodcastUseCase
): ViewModel() {
    fun listenPodcast(castId: Int){
      viewModelScope.launch {
          runCatching {
              listenPodcastUseCase.execute(castId)
          }.onFailure {
              Timber.e(it, "Error while registering listen count")
          }
      }
    }
}