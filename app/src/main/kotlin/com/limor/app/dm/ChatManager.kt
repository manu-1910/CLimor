package com.limor.app.dm

import android.content.Context
import android.os.Looper
import android.util.Log
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.scenes.auth_new.util.PrefsHandler
import io.agora.rtm.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import java.lang.RuntimeException
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class ChatManager @Inject constructor(
    private val context: Context,
    private val chatRepository: ChatRepository,
    private val generalInfoRepository: GeneralInfoRepository
) : RtmClientListener {

    private val chatJob = SupervisorJob()
    private val chatScope = CoroutineScope(Dispatchers.IO + chatJob)

    private val cachedLeanUsers = mutableMapOf<String, LeanUser>();

    private var mToken: String? = null
    private var mLastTokenFetchTime = 0L

    private var rtmClient: RtmClient? = null
    private var sendMsgOptions: SendMessageOptions = SendMessageOptions().apply {
        enableOfflineMessaging = true
    }

    private val messageQueue = mutableListOf<AddMessageJob>()
    private val processing = AtomicBoolean(false)

    init {
        val appID = context.getString(R.string.agora_app_id)
        println("Will start agora client with app id - $appID")
        try {
            rtmClient = RtmClient.createInstance(context, appID, this)

        } catch (e: Exception) {
            Timber.e(Log.getStackTraceString(e))
            throw RuntimeException(
                """
                NEED TO check rtm sdk init fatal error
                ${Log.getStackTraceString(e)}
                """.trimIndent()
            )
        }
    }

    fun loginCurrentUser() {
        val limorUserId = PrefsHandler.getCurrentUserId(context)
        if (limorUserId == 0) {
            return
        }
        val peerId = "${BuildConfig.CHAT_USER_ID_PREFIX}_$limorUserId"
        println("Agora1: Logging in current user $limorUserId as $peerId")
        chatScope.launch {
            getNewToken()?.let {
                println("Agora1: Got new token $it, now logging in.")
                mToken = it
                login(it, peerId)
            }
        }
    }

    private suspend fun refreshCurrentToken() {
        val token = mToken ?: return
        val oneHour = 1 * 60 * 60 * 1000
        val delta = System.currentTimeMillis() - mLastTokenFetchTime
        if (delta > oneHour) {
            // more than 1 hour since the last refresh, so refresh the token
            refresh(token)
        }
    }

    private suspend fun getNewToken(): String? {
        mLastTokenFetchTime = System.currentTimeMillis()
        return generalInfoRepository.getMessagingToken()
    }

    private suspend fun login(token: String, userId: String): Int = suspendCoroutine { cont ->
        val client = rtmClient
        if (client == null) {
            cont.resume(-1)
            return@suspendCoroutine
        }
        client.login(null, userId, object : ResultCallback<Void?> {
            override fun onSuccess(responseInfo: Void?) {
                cont.resume(RtmStatusCode.LoginError.LOGIN_ERR_OK)
            }

            override fun onFailure(errorInfo: ErrorInfo) {
                Timber.i("Agora login failed: %s", errorInfo.errorCode)
                RtmStatusCode.LoginError.LOGIN_ERR_INVALID_TOKEN
                cont.resume(errorInfo.errorCode)
            }
        })
    }

    private suspend fun refresh(token: String): Int = suspendCoroutine { cont ->
        val client = rtmClient
        if (client == null) {
            cont.resume(-1)
            return@suspendCoroutine
        }
        client.renewToken(token, object : ResultCallback<Void?> {
            override fun onSuccess(responseInfo: Void?) {
                mLastTokenFetchTime = System.currentTimeMillis()
                println("Agora refresh success")
                cont.resume(0)
            }

            override fun onFailure(errorInfo: ErrorInfo) {
                Timber.i("Agora renewToken failed: %s", errorInfo.errorCode)
                cont.resume(errorInfo.errorCode)
            }
        })
    }

    suspend fun sendPeerMessage(peerId: String, message: String): Int {
        refreshCurrentToken()
        return sendMessage(peerId, message)
    }

    private suspend fun sendMessage(peerId: String, message: String): Int =
        suspendCoroutine { cont ->
            val client = rtmClient
            if (client == null) {
                cont.resume(-1)
                return@suspendCoroutine
            }
            val rtmMessage = client.createMessage()
            rtmMessage.text = message
            client.sendMessageToPeer(peerId, rtmMessage, sendMsgOptions,
                object : ResultCallback<Void?> {
                    override fun onSuccess(aVoid: Void?) {
                        cont.resume(RtmStatusCode.PeerMessageError.PEER_MESSAGE_ERR_OK)
                    }

                    override fun onFailure(errorInfo: ErrorInfo) {
                        println("Agora1: Error Sending Message -> $errorInfo")
                        cont.resume(errorInfo.errorCode)
                    }
                })
        }

    suspend fun logout() {
        chatRepository.clearAllData(context)
        rtmClient?.logout(null)
    }

    override fun onConnectionStateChanged(state: Int, reason: Int) {
        // TODO
    }

    private suspend fun getLeanUser(peerId: String): LeanUser? {
        cachedLeanUsers[peerId]?.let {
            return it
        } ?: run {
            val id = peerId.split('_').last().toInt()
            val user = generalInfoRepository.getUserProfileById(id)
            if (user == null) {
                return null;
            } else {
                chatRepository.insertChatUser(
                    ChatUser(
                        id = 0,
                        limorUserId = user.id,
                        limorUserName = user.username,
                        limorDisplayName = user.getFullName(),
                        limorProfileUrl = user.getAvatarUrl()
                    )
                )
                val leanUser = LeanUser(
                    limorUserId = id,
                    userName = user.username,
                    displayName = user.getFullName(),
                    profileUrl = user.getAvatarUrl()
                )
                cachedLeanUsers[peerId] = leanUser
                return leanUser
            }
        }
    }

    @Synchronized
    private suspend fun addMessage(text: String, peerId: String) {
        var session = chatRepository.getSessionByUserChatId(peerId)

        if (session == null) {
            // someone just messages me, so need to create a session, but first need to get their
            // profile.
            val leanUser = getLeanUser(peerId) ?: return
            val chatUser = chatRepository.getChatUserByLimorId(leanUser.limorUserId) ?: return

            session = ChatSession(
                id = 0,
                chatUserId = chatUser.id,
                lastMessageContent = text
            )
            val id = chatRepository.insertSession(session)
            session.id = id.toInt()
        }

        chatRepository.addOtherMessage(text, session);
    }

    private fun processQueue() {
        if (processing.get()) {
            println("Not processing because already processing.")
            return
        }
        processing.set(true)
        synchronized(this) {
            chatScope.launch {
                while (true) {
                    val first = messageQueue.firstOrNull()
                    if (first == null) {
                        processing.set(false)
                        return@launch
                    }
                    addMessage(first.text, first.peerId)
                    messageQueue.removeAt(0)
                }
            }
        }
    }

    override fun onMessageReceived(rtmMessage: RtmMessage, peerId: String) {
        println("Agora1: Received Chat Message from $peerId -> ${rtmMessage.text}. Is on main -> ${Looper.getMainLooper().isCurrentThread}")

        synchronized(this) {
            messageQueue.add(AddMessageJob(text = rtmMessage.text, peerId = peerId))
            processQueue()
        }
    }

    override fun onTokenExpired() {
        // When the token has expired we need to re-login and cannot just refresh it
        loginCurrentUser()
    }

    override fun onImageMessageReceivedFromPeer(p0: RtmImageMessage?, p1: String?) {
        // Not used in DM 1.1
    }

    override fun onFileMessageReceivedFromPeer(p0: RtmFileMessage?, p1: String?) {
        // Not used in DM 1.1
    }

    override fun onMediaUploadingProgress(p0: RtmMediaOperationProgress?, p1: Long) {
        // Not used in DM 1.1
    }

    override fun onMediaDownloadingProgress(p0: RtmMediaOperationProgress?, p1: Long) {
        // Not used in DM 1.1
    }

    override fun onPeersOnlineStatusChanged(status: Map<String, Int>) {
        // Not used in DM 1.1
    }

}