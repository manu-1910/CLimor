package com.limor.app.scenes.main.fragments.discover.discover

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.scenes.main.fragments.discover.common.mock.MockCast
import com.limor.app.uimodels.CategoryUIModel
import com.limor.app.uimodels.SuggestedPersonUIModel
import com.limor.app.usecases.GetCategoriesUseCase
import com.limor.app.usecases.GetSuggestedPeopleUseCase
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class
DiscoverViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getSuggestedPeopleUseCase: GetSuggestedPeopleUseCase
) : ViewModel() {

    private val _categories = MutableLiveData<List<CategoryUIModel>>()
    val categories: LiveData<List<CategoryUIModel>> = _categories

    private val _suggestedPeople = MutableLiveData<List<SuggestedPersonUIModel>>()
    val suggestedPeople: LiveData<List<SuggestedPersonUIModel>> = _suggestedPeople

    private val _featuredCasts = MutableLiveData<List<MockCast>>()
    val featuredCasts: LiveData<List<MockCast>> = _featuredCasts

    private val _topCasts = MutableLiveData<List<MockCast>>()
    val topCasts: LiveData<List<MockCast>> = _topCasts

    init {
        loadCategories()
        loadSuggestedPeople()
    }

    private fun loadSuggestedPeople() {
        viewModelScope.launch {
            getSuggestedPeopleUseCase.execute()
                .onSuccess {
                    _suggestedPeople.value = it
                }
                .onFailure {
                    Timber.e("Error while getting suggested people: $it")
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
                    Timber.e("Error while getting categories: $it")
                }
        }
    }
}
