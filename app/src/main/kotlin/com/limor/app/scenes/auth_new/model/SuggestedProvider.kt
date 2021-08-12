package com.limor.app.scenes.auth_new.model

import androidx.lifecycle.MutableLiveData
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.apollo.UserRepository
import com.limor.app.apollo.showHumanizedErrorMessage
import com.limor.app.scenes.auth_new.data.SuggestedUser
import com.limor.app.usecases.GetSuggestedPeopleUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SuggestedProvider @Inject constructor(
    val generalInfoRepository: GeneralInfoRepository,
    private val suggestedUserUseCase: GetSuggestedPeopleUseCase,
    private val userRepository: UserRepository
    ) {

    private var suggestedList: List<SuggestedUser> = mutableListOf()

    val suggestedLiveDataError =
        MutableLiveData<String>().apply { value = "" }

    val suggestedLiveData =
        MutableLiveData<List<SuggestedUser>>().apply { value = suggestedList }

    val suggestedSelectedLiveData =
        MutableLiveData<Boolean>().apply { value = false }

    val suggestedForwardNavigationLiveData =
        MutableLiveData<Boolean>().apply { value = false }

    fun downloadSuggested(scope: CoroutineScope) {
        if (suggestedList.isEmpty())
            loadSuggestedFromRepo(scope)
    }

    private fun loadSuggestedFromRepo(scope: CoroutineScope) {

        scope.launch(Dispatchers.Default) {
            // waiting for transition animation to end
            delay(1000)

            suggestedUserUseCase.execute()
                .onSuccess {
                    suggestedList = it.map(SuggestedUser::fromUserUIUser)
                    suggestedLiveData.postValue(suggestedList)
                }
                .onFailure {
                    Timber.e(it)
                    suggestedLiveDataError.postValue(showHumanizedErrorMessage(it))
                }
        }
    }

    fun followUser(suggestedUser: SuggestedUser, scope: CoroutineScope) {
        suggestedUser.selected = !suggestedUser.selected
        val someSelected = suggestedList.any { it.selected }
        suggestedSelectedLiveData.postValue(someSelected)

        scope.launch(Dispatchers.Default) {
            if (suggestedUser.selected) {
                userRepository.startFollowingUser(suggestedUser.id)
            } else {
                userRepository.unFollowUser(suggestedUser.id)
            }
        }
    }

    fun sendSuggestedPeopleSelectionResult(scope: CoroutineScope) {
        scope.launch(Dispatchers.Default) {
            try {
                val list = suggestedList.filter { it.selected }.map { it.id.toString() }
                println("Will follow $list")
                val result = userRepository.updateFollowingUsersData(list)
                // TODO follow suggested people
                // suggestedForwardNavigationLiveData.postValue(true)
                // delay(500)
                suggestedForwardNavigationLiveData.postValue(true)
            } catch (e: Exception) {
                Timber.e(e)
                suggestedLiveDataError.postValue(showHumanizedErrorMessage(e))
            }
        }
    }
}