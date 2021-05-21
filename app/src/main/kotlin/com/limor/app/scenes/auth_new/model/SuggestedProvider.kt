package com.limor.app.scenes.auth_new.model

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

    fun downloadSuggested() {
        if (suggestedList.isEmpty())
            loadSuggestedFromRepo()
    }

    fun followUser(suggestedUser: SuggestedUser){
        suggestedUser.selected = !suggestedUser.selected
        val someSelected = suggestedList.any { it.selected }
        suggestedSelectedLiveData.postValue(someSelected)
    }

    fun sendSuggestedPeopleSelectionResult(){
        val selected = suggestedList.filter { it.selected }
        if(selected.isNotEmpty()){
            //TODO send selected list to backend
        }
    }

    private fun loadSuggestedFromRepo() {
        scope.launch {
            try {
                delay(300) //waiting for transition animation to end
                suggestedList = createMockedSuggestedUsers()
                suggestedLiveData.postValue(suggestedList)
            } catch (e: Exception) {
                Timber.e(e)
                suggestedLiveDataError.postValue(showHumanizedErrorMessage(e))
            }
        }
    }
}