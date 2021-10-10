package com.limor.app.dm

import android.content.Context
import android.content.Intent
import android.util.Log
import com.limor.app.BuildConfig
import com.limor.app.R
import io.agora.rtm.*
import timber.log.Timber
import java.lang.Exception
import java.lang.RuntimeException
import java.util.ArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ChatManager(private val context: Context, private val chatRepository: ChatRepository) :
    RtmClientListener {

    private val TAG = ChatManager::class.java.simpleName

    private var mRtmClient: RtmClient? = null
    private var mSendMsgOptions: SendMessageOptions = SendMessageOptions().apply {
        enableOfflineMessaging = true
    }

    init {
        val appID = context.getString(R.string.agora_app_id)
        try {
            mRtmClient = RtmClient.createInstance(context, appID, this).also {
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
        val client = mRtmClient
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
        val client = mRtmClient
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

    fun logout() {
        mRtmClient?.logout(null);
        // TODO remove chats from DB
    }

    override fun onConnectionStateChanged(state: Int, reason: Int) {
        // TODO
    }

    override fun onMessageReceived(rtmMessage: RtmMessage, peerId: String) {
        // TODO - insert into DB
    }

    override fun onTokenExpired() {
        // TODO get new token
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