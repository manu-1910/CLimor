package com.limor.app.scenes.main.fragments.discover.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.uimodels.CategoryUIModel
import com.limor.app.usecases.GetCategoriesUseCase
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class DiscoverAllCategoriesViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase
): ViewModel() {

    private val _categories = MutableLiveData<List<CategoryUIModel>>()
    val categories: LiveData<List<CategoryUIModel>> = _categories

    init {
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
