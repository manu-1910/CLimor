package com.limor.app.scenes.patron.manage.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.uimodels.PatronCategoryUIModel
import com.limor.app.uimodels.UserUIModel
import com.limor.app.usecases.CategoriesUseCase
import com.limor.app.usecases.SearchUsersUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import javax.inject.Inject

class ManagePatronViewModel @Inject constructor(
    private val searchUsersUseCase: SearchUsersUseCase,
    private val categoriesUseCase: CategoriesUseCase
) : ViewModel() {

    private val buyers = mutableListOf<String>(
        "In",
        "computer",
        "programming",
        "a",
        "string",
        "is",
        "traditionally",
        "a",
        "sequence",
        "of",
        "characters",
        "either",
        "as",
        "a",
        "literal",
        "constant",
        "or",
        "as",
        "some",
        "kind",
        "of",
        "variable",
        "The",
        "latter",
        "may",
        "allow",
        "its",
        "elements",
        "to",
        "be",
        "mutated",
        "and",
        "the",
        "length",
        "changed"
    )

    var categorySelectedIdsList: ArrayList<Int> = arrayListOf()

    private val _buyersData = MutableLiveData<List<String>>()
    val buyersData: LiveData<List<String>> get() = _buyersData

    private val _earningsData = MutableLiveData<List<String>>()
    val earningsData: LiveData<List<String>> get() = _earningsData

    private val _priceChangeResult = MutableLiveData<Boolean>()
    val priceChangeResult: LiveData<Boolean> get() = _priceChangeResult

    private val _searchResult = MutableLiveData<List<UserUIModel>>()
    val searchResult: LiveData<List<UserUIModel>> = _searchResult

    private val _patronCategories = MutableLiveData<List<PatronCategoryUIModel?>>()
    val patronCategories: LiveData<List<PatronCategoryUIModel?>> = _patronCategories

    private val _categoryUpdateResult = MutableLiveData<Boolean>()
    val categoryUpdateResult: LiveData<Boolean> = _categoryUpdateResult

    private var searchJob: Job? = null

    fun loadCastBuyers(offset: Int = 0, limit: Int = 10) {
        if (offset < buyers.size) {
            _buyersData.postValue(
                buyers.subList(
                    offset,
                    if (offset + limit < buyers.size) offset + limit else buyers.size
                ).toList()
            )
        }
    }

    fun loadCastEarnings(offset: Int = 0, limit: Int = 10) {
        if (offset < buyers.size) {
            _earningsData.postValue(
                buyers.subList(
                    offset,
                    if (offset + limit < buyers.size) offset + limit else buyers.size
                ).toList()
            )
        }
    }

    fun updateCastPrice() {
        viewModelScope.launch {
            _priceChangeResult.postValue(true)
            delay(1000)
            _priceChangeResult.postValue(false)
        }
    }

    fun search(searchQuery: String) {
        Timber.d("Search for: $searchQuery")
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            searchUsersUseCase.execute(searchQuery)
                .onSuccess { _searchResult.value = it }
                .onFailure { Timber.e(it, "Error while searching for users") }
        }
    }

    fun loadPatronCategories() {
        viewModelScope.launch {
            categoriesUseCase.executeDownloadCategories()
                .onSuccess {
                    _patronCategories.value = it
                }
                .onFailure { Timber.e(it, "Error while searching for categories") }
        }
    }

    fun clearCategories(){
        _patronCategories.value = ArrayList()
    }

    fun updatePatronCategories()/* = callbackFlow<String?>*/{
        viewModelScope.launch {
            categoriesUseCase.executeAddPatronCategories(categorySelectedIdsList)
                .onSuccess {
                    _categoryUpdateResult.value = true
                    delay(1000)
                    _categoryUpdateResult.value = false
                }
                .onFailure {
                    _categoryUpdateResult.value = false
                }
        }
    }

}