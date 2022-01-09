package com.limor.app.dm

import android.content.Context
import com.limor.app.BuildConfig
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatDao: ChatDao,
) {

    fun getSessions() = chatDao.getSessions()

    fun getChat(sessionId: Int) = chatDao.getChat(sessionId)

    private fun addMessage(chatMessage: ChatMessage, session: ChatSession) {
        chatDao.insertMessage(chatMessage)

        session.lastMessageDate = Calendar.getInstance()
        session.lastMessageContent = chatMessage.messageContent
        session.draftContent = ""
        session.unreadCount = chatDao.getUnreadCount(session.id, session.lastReadMessageId ?: 0)

        chatDao.updateSession(session)
    }

    fun setDraft(session: ChatSession, content: String) {
        session.draftContent = content
        chatDao.updateSession(session)
    }

    fun addOtherMessage(content: String, session: ChatSession) {
        ChatMessage(
            0,
            session.id,
            session.chatUserId,
            content
        ).also {
            addMessage(it, session)
        }
    }

    suspend fun addMyMessage(content: String, session: ChatSessionWithUser) {
        ChatMessage(
            0,
            session.session.id,
            null,
            content
        ).also {
            addMessage(it, session.session)
        }
    }

    fun getSessionByLimorUserId(limorUserId: Int) = chatDao.getSessionByLimorUserId(limorUserId)
    fun getSessionWithUserId(sessionId: Int) = chatDao.getSessionWithUserId(sessionId)

    fun getSessionByUserChatId(peerId: String): ChatSession? {
        if (peerId.isEmpty() || !peerId.contains('_')) {
            return null
        }
        val limorUserId = peerId.split('_').last().toInt()
        return chatDao.getSessionByLimorUserId(limorUserId)
    }

    fun insertSession(session: ChatSession) = chatDao.insertSession(session)
    fun insertChatUser(chatUser: ChatUser) = chatDao.insertChatUser(chatUser)
    fun getChatUserByLimorId(userId: Int) = chatDao.getChatUserByLimorId(userId)
    suspend fun searchSession(term: String) = chatDao.searchSession(term)

    suspend fun clearAllData(context: Context) {
        DirectMessagingDatabase.getInstance(context).clearAllTables()
    }

    fun updateSession(session: ChatSession) = chatDao.updateSession(session)
}