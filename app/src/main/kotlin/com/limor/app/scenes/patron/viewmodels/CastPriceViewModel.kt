package com.limor.app.scenes.patron.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.usecases.PatronPodcastUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class CastPriceViewModel @Inject constructor(
    private val patronPodcastUseCase: PatronPodcastUseCase
) : ViewModel() {

    private val _priceUpdated = MutableLiveData<Boolean>()
    val priceUpdated: LiveData<Boolean> = _priceUpdated

    fun updatePriceForACast(castId: Int, priceId: String) {
        viewModelScope.launch {
            patronPodcastUseCase.executeSingleCastPriceUpdate(castId, priceId)
                .onSuccess {
                    _priceUpdated.value = true
                    delay(1000)
                    _priceUpdated.value = false
                }
                .onFailure {
                    _priceUpdated.value = false
                }
        }
    }

    fun updateAllCastsPrice(priceId: String) {
        viewModelScope.launch {
            patronPodcastUseCase.executeAllCastsPriceUpdate(priceId)
                .onSuccess {
                    _priceUpdated.value = true
                    delay(1000)
                    _priceUpdated.value = false
                }
                .onFailure {
                    _priceUpdated.value = false
                }
        }
    }

}