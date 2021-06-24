package com.limor.app.scenes.main.fragments.discover.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import repositories.categories.CategoriesRepository
import javax.inject.Inject

class DiscoverAllCategoriesViewModel @Inject constructor(
    private val categoriesRepository: CategoriesRepository
): ViewModel() {

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories

    init {
        viewModelScope.launch {
            delay(1000)
            _categories.value = categoriesNamesList
        }
    }

    private val categoriesNamesList = listOf(
        "Sport",
        "News",
        "Gaming",
        "Travel",
        "Voice",
        "Food",
        "Limor",
        "Education",
        "Health",
        "Beauty",
        "art",
        "Podcast",
        "Politics",
        "Social audio",
        "Makeup",
    )
}