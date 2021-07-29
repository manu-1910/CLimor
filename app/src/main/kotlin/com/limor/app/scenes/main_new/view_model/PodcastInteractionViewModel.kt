package com.limor.app.scenes.main_new.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.limor.app.uimodels.CastUIModel
import javax.inject.Inject

class PodcastInteractionViewModel @Inject constructor(): ViewModel() {
    val reload = MutableLiveData<Boolean>()
}