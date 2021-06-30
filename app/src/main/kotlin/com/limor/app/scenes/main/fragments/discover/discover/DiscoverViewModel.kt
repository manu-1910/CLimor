package com.limor.app.scenes.main.fragments.discover.discover

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.CategoryUIModel
import com.limor.app.uimodels.UserUIModel
import com.limor.app.usecases.GetCategoriesUseCase
import com.limor.app.usecases.GetFeaturedCastsUseCase
import com.limor.app.usecases.GetSuggestedPeopleUseCase
import com.limor.app.usecases.GetTopCastsUseCase
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class DiscoverViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getSuggestedPeopleUseCase: GetSuggestedPeopleUseCase,
    private val getFeaturedCastsUseCase: GetFeaturedCastsUseCase,
    private val getTopCastsUseCase: GetTopCastsUseCase,
) : ViewModel() {

    private val _categories = MutableLiveData<List<CategoryUIModel>>()
    val categories: LiveData<List<CategoryUIModel>> = _categories

    private val _suggestedPeople = MutableLiveData<List<UserUIModel>>()
    val suggestedPeople: LiveData<List<UserUIModel>> = _suggestedPeople

    private val _featuredCasts = MutableLiveData<List<CastUIModel>>()
    val featuredCasts: LiveData<List<CastUIModel>> = _featuredCasts

    private val _topCasts = MutableLiveData<List<CastUIModel>>()
    val topCasts: LiveData<List<CastUIModel>> = _topCasts

    init {
        loadCategories()
        loadSuggestedPeople()
        loadFeaturedCasts()
        loadTopCasts()
    }

    private fun loadTopCasts() {
        viewModelScope.launch {
            getTopCastsUseCase.execute()
                .onSuccess {
                    _topCasts.value = it
                }
                .onFailure {
                    Timber.e(it, "Error while getting featured casts")
                }
        }
    }

    private fun loadFeaturedCasts() {
        viewModelScope.launch {
            getFeaturedCastsUseCase.execute(limit = 5)
                .onSuccess {
                    _featuredCasts.value = it
                }
                .onFailure {
                    Timber.e(it, "Error while getting featured casts")
                }
        }
    }

    private fun loadSuggestedPeople() {
        viewModelScope.launch {
            getSuggestedPeopleUseCase.execute()
                .onSuccess {
                    _suggestedPeople.value = it
                }
                .onFailure {
                    Timber.e(it, "Error while getting suggested people")
                }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase.execute()
                .onSuccess {
                    _categories.value = it
                }
                .onFailure {
                    Timber.e(it, "Error while getting categories")
                }
        }
    }
}
