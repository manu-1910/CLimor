package com.limor.app.scenes.main.fragments.discover.featuredcasts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.scenes.main.fragments.discover.common.mock.MockCast
import com.limor.app.scenes.main.fragments.discover.common.mock.MockPerson
import com.limor.app.uimodels.CastUIModel
import com.limor.app.usecases.GetFeaturedCastsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

class DiscoverFeaturedCastsViewModel @Inject constructor(
    private val getFeaturedCastsUseCase: GetFeaturedCastsUseCase
): ViewModel() {

    private val _featuredCasts = MutableLiveData<List<CastUIModel>>()
    val featuredCasts: LiveData<List<CastUIModel>> = _featuredCasts

    init {
        viewModelScope.launch {
            getFeaturedCastsUseCase.execute(limit = 50)
                .onSuccess {
                    _featuredCasts.value = it
                }
                .onFailure {
                    Timber.e(it, "Error while getting featured casts")
                }
        }
    }
}