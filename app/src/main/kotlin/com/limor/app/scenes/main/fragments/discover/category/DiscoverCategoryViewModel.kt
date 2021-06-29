package com.limor.app.scenes.main.fragments.discover.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.scenes.main.fragments.discover.common.mock.MockCast
import com.limor.app.uimodels.CastUIModel
import com.limor.app.usecases.GetCategoriesUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

class DiscoverCategoryViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase
): ViewModel() {

    private val _featuredCasts = MutableLiveData<List<CastUIModel>>()
    val featuredCasts: LiveData<List<CastUIModel>> = _featuredCasts

    private val _topCasts = MutableLiveData<List<CastUIModel>>()
    val topCasts: LiveData<List<CastUIModel>> = _topCasts

    init {
        viewModelScope.launch {

        }
    }
}