package com.limor.app.dm

import android.content.Context
import android.util.Log
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.apollo.GeneralInfoRepository
import io.agora.rtm.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import java.lang.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ChatManager(
    private val context: Context,
    private val chatRepository: ChatRepository,
    private val generalInfoRepository: GeneralInfoRepository
) : RtmClientListener {

    private val chatJob = SupervisorJob()
    private val chatScope = CoroutineScope(Dispatchers.IO + chatJob)

    private val cachedLeanUsers = mutableMapOf<String, LeanUser>();

    private var rtmClient: RtmClient? = null
    private var sendMsgOptions: SendMessageOptions = SendMessageOptions().apply {
        enableOfflineMessaging = true
    }

    init {
        val appID = context.getString(R.string.agora_app_id)
        try {
            rtmClient = RtmClient.createInstance(context, appID, this).also {
                if (BuildConfig.DEBUG) {
                    it.setParameters("{\"rtm.log_filter\": 65535}")
                }
            }

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

    suspend fun login(token: String, userId: String): Int = suspendCoroutine { cont ->
        val client = rtmClient
        if (client == null) {
            cont.resume(-1)
            return@suspendCoroutine
        }
        client.login(token, userId, object : ResultCallback<Void?> {
            override fun onSuccess(responseInfo: Void?) {
                cont.resume(RtmStatusCode.LoginError.LOGIN_ERR_OK)
            }

            override fun onFailure(errorInfo: ErrorInfo) {
                Timber.i("Agora login failed: %s", errorInfo.errorCode)
                cont.resume(errorInfo.errorCode)
            }
        })
    }

    suspend fun refresh(token: String): Int = suspendCoroutine { cont ->
        val client = rtmClient
        if (client == null) {
            cont.resume(-1)
            return@suspendCoroutine
        }
        client.renewToken(token, object : ResultCallback<Void?> {
            override fun onSuccess(responseInfo: Void?) {
                cont.resume(0)
            }

            override fun onFailure(errorInfo: ErrorInfo) {
                Timber.i("Agora login failed: %s", errorInfo.errorCode)
                cont.resume(errorInfo.errorCode)
            }
        })
    }

    suspend fun sendPeerMessage(peerId: String, message: String): Int = suspendCoroutine { cont ->
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
                    cont.resume(errorInfo.errorCode)
                }
            })
    }

    fun logout() {
        rtmClient?.logout(null);
        // TODO remove chats from DB
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
                chatRepository.insertChatUser(ChatUser(
                    id = 0,
                    limorUserId = user.id,
                    limorUserName = user.username,
                    limorDisplayName = user.getFullName(),
                    limorProfileUrl = user.getAvatarUrl()
                ))
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

    private suspend fun addMessage(text: String, peerId: String) {
        var session = chatRepository.getSessionByUserChatId(peerId)

        if (session == null) {
            // someone just messages me, so need to create a session, but first need to get their
            // profile.
            val leanUser = getLeanUser(peerId) ?: return
            val chatUser = chatRepository.getChatUserByLimorId(leanUser.limorUserId) ?: return

            session = ChatSession(
                id = 0,
                chatUserId = chatUser.id
            )
            val id = chatRepository.insertSession(session)
            session.id = id.toInt()
        }

        chatRepository.addOtherMessage(text, session);
    }

    override fun onMessageReceived(rtmMessage: RtmMessage, peerId: String) {
        chatScope.launch {
            addMessage(rtmMessage.text, peerId)
        }
    }

    override fun onTokenExpired() {
        // When the token has expired we need to re-login and cannot just refresh it
        // TODO login
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