package com.limor.app.dm

import androidx.lifecycle.*
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.uimodels.UserUIModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class SessionsViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val generalInfoRepository: GeneralInfoRepository,
) : ViewModel() {

    val sessions: LiveData<List<ChatSessionWithUser>> = chatRepository.getSessions().asLiveData()

    private var _chatTargets = MutableLiveData<List<ChatTarget>>()
    val chatTargets: LiveData<List<ChatTarget>>
        get() = _chatTargets

    fun searchFollowers(searchTerm: String) {
        println("Will search for $searchTerm")

        if (searchTerm.isEmpty()) {
            _chatTargets.postValue(emptyList())
            return
        }

        viewModelScope.launch {
            try {

                val sessions = chatRepository
                    .searchSession(searchTerm.lowercase())
                    .map(ChatTarget::fromSession)

                val targets = mutableListOf<ChatTarget>().apply {
                    addAll(sessions)
                }

                println("Sessions -> $sessions")

                val followers = generalInfoRepository.searchFollowers(
                    term = searchTerm,
                    limit = Int.MAX_VALUE,
                    offset = 0
                )

                print("Search followers -> $followers")

                followers.filterNotNull()
                    .map(ChatTarget::fromSearch)
                    .filter { searchTarget ->
                        targets.none { chatTarget ->
                            chatTarget.limorUserId == searchTarget.limorUserId
                        }
                    }.also { searchTargets ->
                        targets.addAll(searchTargets)
                    }

                _chatTargets.postValue(targets)
            } catch (e: Exception) {
                e.printStackTrace()
                _chatTargets.postValue(emptyList())
            }
        }
    }
}