package com.limor.app.scenes.main_new.view.editpreview

import androidx.lifecycle.*
import com.limor.app.uimodels.CastUIModel
import com.limor.app.usecases.UpdatePreviewUseCase
import javax.inject.Inject

class UpdatePreviewViewModel @Inject constructor(
    private val updatePreviewUseCase: UpdatePreviewUseCase
) : ViewModel() {

    fun updatePreview(podcast: CastUIModel) = liveData {
        podcast.patronDetails?.let {
            emit(
                updatePreviewUseCase.execute(
                    podcast.id,
                    it.getDurationMillis(),
                    it.getStartsAtMillis(),
                    it.getEndsAtMillis(),
                )
            )
        }
    }
}
