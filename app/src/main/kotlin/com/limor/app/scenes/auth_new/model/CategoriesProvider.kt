package com.limor.app.scenes.auth_new.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.scenes.auth_new.data.CategoryWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class CategoriesProvider(private val scope: CoroutineScope) {

    private var categories: List<CategoryWrapper> = mutableListOf()

    fun downloadCategories() {
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
            }
        }
    }

    private val _categoriesLiveData =
        MutableLiveData<List<CategoryWrapper>>().apply { value = categories }

    val categoriesLiveData: LiveData<List<CategoryWrapper>>
        get() = _categoriesLiveData


    fun updateCategoriesSelection() {
        val anySelected = categories.any { it.isSelected }
        _categorySelectionDone.postValue(anySelected)
    }

    private val _categorySelectionDone =
        MutableLiveData<Boolean>().apply { value = false }

    val categorySelectionDone: LiveData<Boolean>
        get() = _categorySelectionDone
}