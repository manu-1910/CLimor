package com.limor.app.scenes.main_new.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.uimodels.UserUIModel
import com.limor.app.usecases.SearchUsersUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class UserMentionViewModel @Inject constructor(
    private val searchUsersUseCase: SearchUsersUseCase,
) : ViewModel() {

    private var searchJob: Job? = null

    private val _userMentionData = MutableLiveData<List<UserUIModel>>().apply { value = listOf() }
    val userMentionData: LiveData<List<UserUIModel>> get() = _userMentionData

    fun search(query: String) {
        if (query.isEmpty()) {
            _userMentionData.postValue(listOf())
            return
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            searchUsersUseCase.execute(query)
                .onSuccess {
                    _userMentionData.postValue(it)
                }
                .onFailure {
                    // TODO - should this send an empty list instead?
                    Timber.e(it, "Error while searching for users")
                }
        }
    }
}
