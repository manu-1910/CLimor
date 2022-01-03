package com.limor.app.scenes.main_new.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.limor.app.apollo.CastsRepository
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.mapToUIModel
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(
    private val castRepository: CastsRepository,
) : ViewModel() {

    fun loadCast(id: Int) = liveData {
        emit(castRepository.getCastById(id)?.mapToUIModel())
    }

}