package com.limor.app.scenes.main.fragments.discover.suggestedpeople

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.uimodels.UserUIModel
import com.limor.app.usecases.FollowPersonUseCase
import com.limor.app.usecases.GetSuggestedPeopleUseCase
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class DiscoverSuggestedPeopleViewModel @Inject constructor(
    private val getSuggestedPeopleUseCase: GetSuggestedPeopleUseCase,
    private val followPersonUseCase: FollowPersonUseCase
): ViewModel() {

    private val _suggestedPeople = MutableLiveData<List<UserUIModel>>()
    val suggestedPeople: LiveData<List<UserUIModel>> = _suggestedPeople

    init {
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

    fun onFollowClick(person: UserUIModel, follow: Boolean) {
        viewModelScope.launch {
            try {
                followPersonUseCase.execute(person, follow)
            } catch (ex: Exception) {
                Timber.e(ex, "Error while following person")
            }
        }
    }
}
