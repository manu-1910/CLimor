package com.limor.app.scenes.auth_new.model

import android.os.Handler
import androidx.lifecycle.MutableLiveData
import com.limor.app.apollo.showHumanizedErrorMessage
import com.limor.app.scenes.auth_new.data.SuggestedUser
import com.limor.app.scenes.auth_new.data.createMockedSuggestedUsers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class SuggestedProvider(private val scope: CoroutineScope) {

    private var suggestedList: List<SuggestedUser> = mutableListOf()

    val suggestedLiveDataError =
        MutableLiveData<String>().apply { value = "" }

    val suggestedLiveData =
        MutableLiveData<List<SuggestedUser>>().apply { value = suggestedList }

    val suggestedSelectedLiveData =
        MutableLiveData<Boolean>().apply { value = false }

    val suggestedForwardNavigationLiveData =
        MutableLiveData<Boolean>().apply { value = false }

    fun downloadSuggested() {
        if (suggestedList.isEmpty())
            loadSuggestedFromRepo()
    }

    private fun loadSuggestedFromRepo() {
        scope.launch {
            try {
                delay(1000) //waiting for transition animation to end
                suggestedList = createMockedSuggestedUsers()
                suggestedLiveData.postValue(suggestedList)
            } catch (e: Exception) {
                Timber.e(e)
                suggestedLiveDataError.postValue(showHumanizedErrorMessage(e))
            }
        }
    }

    fun followUser(suggestedUser: SuggestedUser) {
        suggestedUser.selected = !suggestedUser.selected
        val someSelected = suggestedList.any { it.selected }
        suggestedSelectedLiveData.postValue(someSelected)
    }

    fun sendSuggestedPeopleSelectionResult() {
        scope.launch {
            try {
                val list = suggestedList.filter { it.selected }.map { it.id.toString() }
//                val result = UserRepository.updateFollowingUsersData(list)
                //TODO follow suggested people
                suggestedForwardNavigationLiveData.postValue(true)
                Handler().postDelayed({ suggestedForwardNavigationLiveData.postValue(true) }, 500)
            } catch (e: Exception) {
                Timber.e(e)
                suggestedLiveDataError.postValue(showHumanizedErrorMessage(e))
            }
        }
    }
}