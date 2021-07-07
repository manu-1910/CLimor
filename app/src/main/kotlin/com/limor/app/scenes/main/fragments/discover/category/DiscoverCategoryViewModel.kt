package com.limor.app.scenes.main.fragments.discover.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.uimodels.CastUIModel
import com.limor.app.usecases.GetCastsByCategoryUseCase
import com.limor.app.usecases.GetFeaturedCastsUseCase
import com.limor.app.usecases.GetTopCastsUseCase
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class DiscoverCategoryViewModel @Inject constructor(
    private val getFeaturedCastsUseCase: GetFeaturedCastsUseCase,
    private val getTopCastsUseCase: GetTopCastsUseCase,
    private val getPodcastsByCategoryUseCase: GetCastsByCategoryUseCase
) : ViewModel() {

    private val _featuredCasts = MutableLiveData<List<CastUIModel>>()
    val featuredCasts: LiveData<List<CastUIModel>> = _featuredCasts

    private val _topCasts = MutableLiveData<List<CastUIModel>>()
    val topCasts: LiveData<List<CastUIModel>> = _topCasts

    fun loadCasts(categoryId: Int) {
        loadTopCasts(categoryId)
        loadFeaturedCasts(categoryId)
    }

    private fun loadTopCasts(categoryId: Int) {
        viewModelScope.launch {
            // FIXME when backend will provide "category" field for Cast model
            /*getTopCastsUseCase.execute()
                .onSuccess {
                    *//*_topCasts.value = it.filter {
                        it.categoryId = categoryId
                    }*//*
                }
                .onFailure {
                    Timber.e(it, "Error while getting featured casts")
                }*/

            getPodcastsByCategoryUseCase.execute(categoryId, limit = 5, offset = 0)
                .onSuccess {
                    _topCasts.value = it
                }
                .onFailure {
                    Timber.e(it, "Error while getting casts by category")
                }
        }
    }

    private fun loadFeaturedCasts(categoryId: Int) {
        // FIXME when backend will provide "category" field for Cast model
        /*
        viewModelScope.launch {
            getFeaturedCastsUseCase.execute(limit = 5)
                .onSuccess {
                    /*_featuredCasts.value = it.filter {
                        it.categoryId = categoryId
                    }*/
                }
                .onFailure {
                    Timber.e(it, "Error while getting featured casts")
                }
        }
        */
    }
}