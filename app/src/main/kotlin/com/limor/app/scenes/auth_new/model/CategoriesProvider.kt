package com.limor.app.scenes.auth_new.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.apollo.showHumanizedErrorMessage
import com.limor.app.scenes.auth_new.data.CategoryWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class CategoriesProvider(private val scope: CoroutineScope) {
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

    fun downloadCategories() {
        _categoryLiveDataError.postValue("")
        if (categories.isEmpty())
            loadCategoriesRepo()
    }

    private fun loadCategoriesRepo() {
        scope.launch {
            try {
                val response = GeneralInfoRepository.fetchCategories()
                categories = response!!.map { CategoryWrapper(it, false) }
                _categoriesLiveData.postValue(categories)
            } catch (e: Exception) {
                Timber.e(e)
                _categoryLiveDataError.postValue(showHumanizedErrorMessage(e))
            }
        }
    }

    fun updateCategoriesSelection() {
        val anySelected = categories.any { it.isSelected }
        _categorySelectionDone.postValue(anySelected)
    }
}
