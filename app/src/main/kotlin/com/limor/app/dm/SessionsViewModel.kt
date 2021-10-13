package com.limor.app.dm

import androidx.lifecycle.*
import com.limor.app.BuildConfig
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.uimodels.UserUIModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

class SessionsViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val chatManager: ChatManager,
    private val generalInfoRepository: GeneralInfoRepository,
) : ViewModel() {

    val sessions: LiveData<List<ChatSessionWithUser>> = chatRepository.getSessions().asLiveData()

    suspend fun getChat(limorUserId: Int): LiveData<ChatWithData>? {
        var session = chatRepository.getSessionByLimorUserId(limorUserId)
        if (session == null) {
            println("ZZZZ Session was null")
            session = createSession(limorUserId)
        }
        if (session == null) {
            println("ZZZZ Could not create session")
            return null
        }
        return chatRepository.getChat(sessionId = session.id).asLiveData()
    }

    private suspend fun createSession(limorUserId: Int): ChatSession? {
        val user = generalInfoRepository.getUserProfileById(limorUserId)
        if (user == null) {
            println("ZZZZ User is null.")
            return null;
        } else {
            chatRepository.insertChatUser(ChatUser(
                id = 0,
                limorUserId = user.id,
                limorUserName = user.username,
                limorDisplayName = user.getFullName(),
                limorProfileUrl = user.getAvatarUrl()
            ))
            val chatUser = chatRepository.getChatUserByLimorId(limorUserId) ?: return null

            val session = ChatSession(
                id = 0,
                chatUserId = chatUser.id
            )
            val id = chatRepository.insertSession(session)
            session.id = id.toInt()
            println("ZZZZ Created session with ID $id")
            return session
        }
    }

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

    fun addMyMessage(session: ChatSessionWithUser, content: String) {
        viewModelScope.launch {
            chatRepository.addMyMessage(content, session)

            val peerId = "${BuildConfig.CHAT_USER_ID_PREFIX}_${session.user.limorUserId}"
            chatManager.sendPeerMessage(peerId, content)
        }
    }
}