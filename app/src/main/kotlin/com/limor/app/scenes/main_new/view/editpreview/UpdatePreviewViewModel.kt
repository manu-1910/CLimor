package com.limor.app.scenes.main_new.view.editpreview

import androidx.lifecycle.*
import com.limor.app.uimodels.UIPodcast
import com.limor.app.usecases.GetHomeFeedCastsUseCase
import javax.inject.Inject

class UpdatePreviewViewModel @Inject constructor(
   private val getHomeFeedCastsUseCase: GetHomeFeedCastsUseCase
) : ViewModel() {


    fun updatePreview(podcast: UIPodcast) = liveData<Boolean> {

    }
}
