package com.limor.app.dm

import androidx.lifecycle.*
import com.limor.app.BuildConfig
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SessionsViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val chatManager: ChatManager,
    private val generalInfoRepository: GeneralInfoRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    val sessions: LiveData<List<ChatSessionWithUser>> = chatRepository.getSessions().asLiveData()

    suspend fun getChat(limorUserId: Int): LiveData<ChatWithData>? {
        var session = chatRepository.getSessionByLimorUserId(limorUserId)
        if (session == null) {
            session = createSession(limorUserId, "")
        }
        if (session == null) {
            return null
        }
        return chatRepository.getChat(sessionId = session.id).asLiveData()
    }

    private suspend fun createSession(limorUserId: Int, message: String): ChatSession? {
        val user = generalInfoRepository.getUserProfileById(limorUserId)
        if (user == null) {
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
                chatUserId = chatUser.id,
                lastMessageContent = message,
                unreadCount =  0,
                lastReadMessageId = 0,
            )
            val id = chatRepository.insertSession(session)
            session.id = id.toInt()
            if (BuildConfig.DEBUG) {
                println("ZZZZ Created session with ID $id")
            }
            return session
        }
    }

    private var _chatTargets = MutableLiveData<List<ChatTarget>>()
    val chatTargets: LiveData<List<ChatTarget>>
        get() = _chatTargets

    fun searchFollowers(searchTerm: String) {
        if (BuildConfig.DEBUG) {
            println("Will search for $searchTerm")
        }

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

                if (BuildConfig.DEBUG) {
                    println("Sessions -> $sessions")
                }

                val followers = generalInfoRepository.searchFollowers(
                    term = searchTerm,
                    limit = Int.MAX_VALUE,
                    offset = 0
                )

                if (BuildConfig.DEBUG) {
                    println("Search followers -> $followers")
                }

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
        if (content.isEmpty()) {
            return
        }
        if (BuildConfig.DEBUG) {
            println("Adding my message ($content) in $session")
        }
        viewModelScope.launch {
            withContext(dispatcherProvider.io) {
                chatRepository.addMyMessage(content, session)

                val peerId = "${BuildConfig.CHAT_USER_ID_PREFIX}_${session.user.limorUserId}"
                val result = chatManager.sendPeerMessage(peerId, content)
                if (BuildConfig.DEBUG) {
                    println("Result from sending message: $result")
                }
            }
        }
    }

    private suspend fun share(user: LeanUser, url: String) {
        var session = chatRepository.getSessionByLimorUserId(user.limorUserId)
        if (session == null) {
            session = createSession(user.limorUserId, url)
        }
        if (session == null) {
            return
        }
        val sessionWithUser = chatRepository.getSessionWithUserId(session.id)
        if (BuildConfig.DEBUG) {
            println("Will share with session ${sessionWithUser.session.id}")
        }
        addMyMessage(sessionWithUser, url)
    }

    suspend fun shareAsDirectMessage(selected: List<LeanUser>, url: String?): Boolean = suspendCoroutine { cont ->
        if (BuildConfig.DEBUG) {
            println("Sharing as direct -> ${selected.size} -> $url")
        }
        if (url.isNullOrEmpty()) {
            cont.resume(false)
            return@suspendCoroutine
        }
        viewModelScope.launch {
            withContext(dispatcherProvider.io) {
                selected.forEach {
                    share(it, url)
                }
                cont.resume(true)
            }
        }
    }

    fun setDraft(session: ChatSession, text: String) {
        viewModelScope.launch {
            withContext(dispatcherProvider.io) {
                chatRepository.setDraft(session, text)
            }
        }
    }

    fun updateSession(session: ChatSession) {
        viewModelScope.launch {
            withContext(dispatcherProvider.io) {
                chatRepository.updateSession(session)
            }
        }
    }
}