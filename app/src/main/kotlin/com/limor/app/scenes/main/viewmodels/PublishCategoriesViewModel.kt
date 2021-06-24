package com.limor.app.scenes.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.scenes.auth_new.data.CategoryWrapper
import com.limor.app.scenes.auth_new.model.CategoriesProvider
import javax.inject.Inject

class PublishCategoriesViewModel @Inject constructor(val categoriesProvider: CategoriesProvider) :
    ViewModel() {

    fun downloadCategories() = categoriesProvider.downloadCategories(viewModelScope)

    fun updateCategoriesSelection() =
        categoriesProvider.updateCategoriesSelection()

    val categoriesLiveData: LiveData<List<CategoryWrapper>>
        get() = categoriesProvider.categoriesLiveData

    val categorySelectionDone: LiveData<Boolean>
        get() = categoriesProvider.categorySelectionDone

    val categoryLiveDataError: LiveData<String>
        get() = categoriesProvider.categoryLiveDataError

}