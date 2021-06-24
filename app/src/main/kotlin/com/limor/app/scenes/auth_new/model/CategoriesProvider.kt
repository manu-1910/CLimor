package com.limor.app.scenes.auth_new.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.apollo.showHumanizedErrorMessage
import com.limor.app.scenes.auth_new.data.CategoryWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class CategoriesProvider @Inject constructor (val generalInfoRepository :GeneralInfoRepository ) {
    private var categories: List<CategoryWrapper> = mutableListOf()
    private val _categoriesLiveData =
        MutableLiveData<List<CategoryWrapper>>().apply { value = categories }

    val categoriesLiveData: LiveData<List<CategoryWrapper>>
        get() = _categoriesLiveData

    private val _categorySelectionDone =
        MutableLiveData<Boolean>().apply { value = false }

    val categorySelectionDone: LiveData<Boolean>
        get() = _categorySelectionDone

    private val _categoryLiveDataError =
        MutableLiveData<String>().apply { value = "" }

    val categoryLiveDataError: LiveData<String>
        get() = _categoryLiveDataError

    fun downloadCategories(scope: CoroutineScope) {
        _categoryLiveDataError.postValue("")
        if (categories.isEmpty())
            loadCategoriesRepo(scope)
    }

    private fun loadCategoriesRepo(scope: CoroutineScope) {
        scope.launch(Dispatchers.Default) {
            try {
                val response = generalInfoRepository.fetchCategories()
                categories = response!!.map { CategoryWrapper(it, false) }
                _categoriesLiveData.postValue(categories)
            } catch (e: Exception) {
                _categoryLiveDataError.postValue(showHumanizedErrorMessage(e))
            }
        }
    }

    fun getActiveCategoriesIds(): List<Int?> {
        return categories.filter { it.isSelected }.map { it.queryCategory.id }
    }

    fun updateCategoriesSelection() {
        val anySelected = categories.any { it.isSelected }
        _categorySelectionDone.postValue(anySelected)
    }
}
